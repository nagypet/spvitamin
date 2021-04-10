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

package hu.perit.spvitamin.spring.data.admin;

import hu.perit.spvitamin.spring.admin.serverparameter.ServerParameterList;
import hu.perit.spvitamin.spring.data.config.DatasourceCollectionProperties;
import hu.perit.spvitamin.spring.data.config.DatasourceProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class DataServerParameters
{

    private final DatasourceCollectionProperties datasourceCollectionProperties;

    @Autowired
    public DataServerParameters(DatasourceCollectionProperties datasourceCollectionProperties) {
        this.datasourceCollectionProperties = datasourceCollectionProperties;
    }

    @Bean(name = "DataServerParameters")
    public ServerParameterList getParameterList()
    {
        ServerParameterList params = new ServerParameterList();

        for (Map.Entry<String, DatasourceProperties> entry : this.datasourceCollectionProperties.getDatasource().entrySet()) {
            params.add(ServerParameterList.of(entry.getValue(), "datasource." + entry.getKey()));
        }

        return params;
    }
}