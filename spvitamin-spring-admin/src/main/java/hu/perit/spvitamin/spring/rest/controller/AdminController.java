/*
 * Copyright 2020-2025 the original author or authors.
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

package hu.perit.spvitamin.spring.rest.controller;

import hu.perit.spvitamin.spring.admin.ShutdownManager;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterProvider;
import hu.perit.spvitamin.spring.config.AdminProperties;
import hu.perit.spvitamin.spring.config.Constants;
import hu.perit.spvitamin.spring.manifest.ManifestReader;
import hu.perit.spvitamin.spring.rest.api.AdminApi;
import hu.perit.spvitamin.spring.rest.model.ServerSettingsResponse;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Peter Nagy
 */

@RestController
@Slf4j
@RequiredArgsConstructor
public class AdminController implements AdminApi
{
    private final ShutdownManager sm;
    private final ServerParameterProvider serverParameterProvider;
    private final AdminProperties adminProperties;


    @Override
    @LoggedRestMethod(eventId = 1, subsystem = Constants.SUBSYSTEM_NAME)
    public ServerSettingsResponse retrieveServerSettingsUsingGET()
    {
        Map<String, Set<ServerParameter>> serverParameters = this.serverParameterProvider.getServerParameters();
        ServerSettingsResponse serverSettingsResponse = new ServerSettingsResponse();
        serverSettingsResponse.setServerParameters(serverParameters);
        return serverSettingsResponse;
    }


    @Override
    public Properties retrieveVersionInfoUsingGET()
    {
        Properties manifest = ManifestReader.getManifestAttributes();
        String name = manifest.getProperty("Implementation-Title", "Application Title");
        String version = manifest.getProperty("Implementation-Version", "");
        //String svnVersion = manifest.getProperty("SVN-REVISION", "");
        String buildTime = manifest.getProperty("Build-Time", "");
        String build;
        if (StringUtils.isNoneBlank(buildTime))
        {
            build = String.format("%s", buildTime);
        }
        else
        {
            build = "Started from IDE, no build info is available!";
        }

        Properties props = new Properties();
        props.setProperty("Title", name);
        props.setProperty("Version", version);
        props.setProperty("Build", build);
        props.setProperty("Copyright", this.adminProperties.getCopyright());
        props.setProperty("KeystoreAdminEnabled", this.adminProperties.getKeystoreAdminEnabled());

        return props;
    }


    @Override
    public void shutdown()
    {
        this.sm.start();
    }


    @Override
    public void cspViolationsUsingPOST(String request)
    {
        // do nothing
    }
}
