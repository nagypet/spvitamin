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

package hu.perit.spvitamin.spring.data.dynamicdatasource;

import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ConnectionParamTest
{
    @Test
    void testSqlServer()
    {
        DatasourceProperties properties = new DatasourceProperties();
        properties.setDbType("sqlserver");
        properties.setHost("db_host");
        properties.setDbName("test");
        properties.setMaxPoolSize(30);
        properties.setSocketTimeout(1800000L);
        ConnectionParam connectionParam = new ConnectionParam(properties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        assertThat(jdbcUrl).isEqualTo("jdbc:sqlserver://db_host:1433;databaseName=test;socketTimeout=1800000");
    }


    @Test
    void testSqlServerWithInstance()
    {
        DatasourceProperties properties = new DatasourceProperties();
        properties.setDbType("sqlserver");
        properties.setHost("db_host");
        properties.setInstance("inst1");
        properties.setDbName("test");
        properties.setMaxPoolSize(30);
        properties.setSocketTimeout(1800000L);
        ConnectionParam connectionParam = new ConnectionParam(properties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        assertThat(jdbcUrl).isEqualTo("jdbc:sqlserver://db_host;instanceName=inst1;databaseName=test;socketTimeout=1800000");
    }


    @Test
    void testOracle()
    {
        DatasourceProperties properties = new DatasourceProperties();
        properties.setDbType("oracle");
        properties.setHost("db_host");
        properties.setDbName("test");
        properties.setMaxPoolSize(30);
        properties.setSocketTimeout(1800000L);
        ConnectionParam connectionParam = new ConnectionParam(properties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        assertThat(jdbcUrl).isEqualTo("jdbc:oracle:thin:@db_host:1521/test");
    }


    @Test
    void testNativeJdbc()
    {
        DatasourceProperties properties = new DatasourceProperties();
        properties.setJdbcUrl("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = server-host)(PORT = 1521))(CONNECT_DATA = (SERVER = DEDICATED)(SERVICE_NAME = service-name)))");
        ConnectionParam connectionParam = new ConnectionParam(properties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        assertThat(jdbcUrl).isEqualTo("jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = server-host)(PORT = 1521))(CONNECT_DATA = (SERVER = DEDICATED)(SERVICE_NAME = service-name)))");
    }


    @Test
    void testUnencryptedConnection()
    {
        DatasourceProperties datasourceProperties = new DatasourceProperties();
        datasourceProperties.setDbType("oracle");
        datasourceProperties.setHost("192.168.62.222");
        datasourceProperties.setPort("1530");
        datasourceProperties.setDbName("otplls");
        datasourceProperties.setEncryptedConnection(false);

        ConnectionParam connectionParam = new ConnectionParam(datasourceProperties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        log.debug(jdbcUrl);
        assertThat(jdbcUrl).isEqualTo("jdbc:oracle:thin:@192.168.62.222:1530/otplls");
    }


    @Test
    void testEncryptedConnection()
    {
        // jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=192.168.62.222)(PORT=1530))(CONNECT_DATA=(SERVICE_NAME=otplls)))
        DatasourceProperties datasourceProperties = new DatasourceProperties();
        datasourceProperties.setDbType("oracle");
        datasourceProperties.setHost("192.168.62.222");
        datasourceProperties.setPort("1530");
        datasourceProperties.setDbName("otplls");
        datasourceProperties.setEncryptedConnection(true);

        ConnectionParam connectionParam = new ConnectionParam(datasourceProperties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        log.debug(jdbcUrl);
        assertThat(jdbcUrl).isEqualTo("jdbc:oracle:thin:@(DESCRIPTION=(CONNECT_TIMEOUT=5)(TRANSPORT_CONNECT_TIMEOUT=100)(RETRY_COUNT=50)(RETRY_DELAY=3)(ADDRESS=(PROTOCOL=TCPS)(HOST=192.168.62.222)(PORT=1530))(CONNECT_DATA=(SERVICE_NAME=otplls)))");
    }


    @Test
    void testEncryptedConnectionWith2Hosts()
    {
        // jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=192.168.62.222)(PORT=1530))(CONNECT_DATA=(SERVICE_NAME=otplls)))
        DatasourceProperties datasourceProperties = new DatasourceProperties();
        datasourceProperties.setDbType("oracle");
        datasourceProperties.setHost("192.168.62.222");
        datasourceProperties.setPort("1530");
        datasourceProperties.setHost2("192.168.62.223");
        datasourceProperties.setPort2("1530");
        datasourceProperties.setDbName("otplls");
        datasourceProperties.setEncryptedConnection(true);

        ConnectionParam connectionParam = new ConnectionParam(datasourceProperties);
        String jdbcUrl = connectionParam.getJdbcUrl();
        log.debug(jdbcUrl);
        assertThat(jdbcUrl).isEqualTo("jdbc:oracle:thin:@(DESCRIPTION=(CONNECT_TIMEOUT=5)(TRANSPORT_CONNECT_TIMEOUT=100)(RETRY_COUNT=50)(RETRY_DELAY=3)(ADDRESS=(PROTOCOL=TCPS)(HOST=192.168.62.222)(PORT=1530))(ADDRESS=(PROTOCOL=TCPS)(HOST=192.168.62.223)(PORT=1530))(CONNECT_DATA=(SERVICE_NAME=otplls)))");
    }

}
