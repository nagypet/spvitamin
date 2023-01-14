/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.security.keycloak;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.BasicAuthRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.authentication.ClientCredentialsProviderUtils;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;

public class CustomBasicAuthRequestAuthenticator extends BasicAuthRequestAuthenticator
{

    public CustomBasicAuthRequestAuthenticator(KeycloakDeployment deployment)
    {
        super(deployment);
    }


    @Override
    protected AccessTokenResponse getToken(String username, String password) throws Exception
    {
        AccessTokenResponse tokenResponse = null;
        HttpClient client = deployment.getClient();

        HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri(deployment.getAuthServerBaseUrl()).path(ServiceUrlConstants.TOKEN_PATH) //
            .build(deployment.getRealm()));
        java.util.List<NameValuePair> formparams = new java.util.ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
        formparams.add(new BasicNameValuePair("username", username));
        formparams.add(new BasicNameValuePair("password", password));

        ClientCredentialsProviderUtils.setClientCredentials(deployment, post, formparams);

        UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(form);

        HttpResponse response = client.execute(post);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (status != 200)
        {
            EntityUtils.consumeQuietly(entity);
            throw new IOException(
                String.format("Keycloak returned with status: %s; URL: %s", response.getStatusLine(), post.getURI().toString()));
        }
        if (entity == null)
        {
            throw new IOException("No Entity");
        }
        java.io.InputStream is = entity.getContent();
        try
        {
            tokenResponse = JsonSerialization.readValue(is, AccessTokenResponse.class);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (java.io.IOException ignored)
            {
            }
        }

        return (tokenResponse);
    }


}
