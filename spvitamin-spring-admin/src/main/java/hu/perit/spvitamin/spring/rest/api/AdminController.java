/*
 * Copyright 2020-2020 the original author or authors.
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

package hu.perit.spvitamin.spring.rest.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.reflect.AbstractInvocationHandler;

import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.admin.ShutdownManager;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameter;
import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterProvider;
import hu.perit.spvitamin.spring.config.Constants;
import hu.perit.spvitamin.spring.logging.AbstractInterfaceLogger;
import hu.perit.spvitamin.spring.rest.session.AdminSession;
import hu.perit.spvitamin.spring.security.auth.SpvitaminAuthorizationService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Peter Nagy
 */

@RestController
@Slf4j
public class AdminController implements AdminApi
{
    private final AdminApi proxy;

    // Injecting dependencies
    public AdminController(ShutdownManager sm, ServerParameterProvider serverParameterProvider, SpvitaminAuthorizationService authorizationService, HttpServletRequest httpRequest)
    {
        this.proxy = (AdminApi) Proxy.newProxyInstance(
                AdminApi.class.getClassLoader(),
                new Class[]{AdminApi.class},
                new ProxyImpl(sm, serverParameterProvider, authorizationService, httpRequest));
    }


    @Override
    public List<ServerParameter> retrieveServerSettingsUsingGET()
    {
        return this.proxy.retrieveServerSettingsUsingGET();
    }


    @Override
    public Properties retrieveVersionInfoUsingGET()
    {
        return this.proxy.retrieveVersionInfoUsingGET();
    }


    @Override
    public void shutdown()
    {
        this.proxy.shutdown();
    }


    @Override
    public void cspViolationsUsingPOST(String request)
    {
        this.proxy.cspViolationsUsingPOST(request);
    }


    /*
     * ============== PROXY Implementation =============================================================================
     */

    @Slf4j
    private static class ProxyImpl extends AbstractInvocationHandler {
        private final ShutdownManager sm;
        private final ServerParameterProvider serverParameterProvider;
        private final SpvitaminAuthorizationService authorizationService;
        private final ProxyImpl.Logger logger;

        public ProxyImpl(ShutdownManager sm, ServerParameterProvider serverParameterProvider, SpvitaminAuthorizationService authorizationService, HttpServletRequest httpRequest) {
            this.logger = new ProxyImpl.Logger(httpRequest);
            this.sm = sm;
            this.serverParameterProvider = serverParameterProvider;
            this.authorizationService = authorizationService;
        }


        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            return this.invokeWithExtras(method, args);
        }


        private Object invokeWithExtras(Method method, Object[] args) throws Throwable {
            UserDetails user = this.authorizationService.getAuthenticatedUser();
            try (Took took = new Took(method)) {
                this.logger.traceIn(null, user.getUsername(), method, args);
                AdminSession session = new AdminSession(this.sm, this.serverParameterProvider);
                Object retval = method.invoke(session, args);
                return retval;
            }
            catch (IllegalAccessException ex) {
                this.logger.traceOut(null, user.getUsername(), method, ex);
                throw ex;
            }
            catch (InvocationTargetException ex) {
                this.logger.traceOut(null, user.getUsername(), method, ex.getTargetException());
                throw ex.getTargetException();
            }
        }

        @Slf4j
        private static class Logger extends AbstractInterfaceLogger {

            Logger(HttpServletRequest httpRequest) {
                super(httpRequest);
            }

            @Override
            protected String getSubsystemName() {
                return Constants.SUBSYSTEM_NAME;
            }
        }
    }
}
