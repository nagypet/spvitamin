package hu.perit.spvitamin.spring.data.dynamicdatasource;

import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}