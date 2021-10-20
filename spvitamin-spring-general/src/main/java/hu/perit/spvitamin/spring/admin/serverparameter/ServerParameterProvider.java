/*
 * Copyright 2020-2021 the original author or authors.
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

package hu.perit.spvitamin.spring.admin.serverparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Peter Nagy
 */


@Component
public class ServerParameterProvider
{
    @Autowired
    ApplicationContext applicationContext;

    public List<ServerParameter> getServerParameters()
    {
        String[] beanNames = this.applicationContext.getBeanNamesForType(ServerParameterList.class);

        List<ServerParameter> retval = new ArrayList<>();
        for (String beanName : beanNames)
        {
            ServerParameterList parameterList = (ServerParameterList) this.applicationContext.getBean(beanName);
            retval.addAll(parameterList.getParameter());
        }

        retval.sort(Comparator.<ServerParameter>naturalOrder());

        return retval;
    }

}
