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

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import hu.perit.spvitamin.spring.data.mapper.ConnectionParamMapper;
import hu.perit.spvitamin.spring.exception.InvalidInputException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;

import java.util.concurrent.TimeUnit;

/**
 * A convenience class, which creates a JDBC connection string using the raw datasource properties for the popular database systems.
 *
 * @author Peter Nagy
 */

@Getter
@Setter
@ToString
@Slf4j
public class ConnectionParam extends DatasourceProperties
{

    public static final String DBTYPE_SQLSERVER = "sqlserver";
    public static final String DBTYPE_ORACLE = "oracle";
    public static final String DBTYPE_MYSQL = "mysql";
    public static final String DBTYPE_H2 = "h2";
    public static final String DBTYPE_POSTGRESQL = "postgresql";

    private static final String NOT_YET_SUPPORTED = "'%s' not yet supported";

    private final String portDelimiter;

    public ConnectionParam(DatasourceProperties properties)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("DatasourceProperties is 'null'! Most probably the application cannot access the database configuration. Please make sure, the application.properties is available.");
        }

        ConnectionParamMapper mapper = Mappers.getMapper(ConnectionParamMapper.class);
        mapper.copy(properties, this);

        if (dialect == null)
        {
            dialect = getDefaultDialect();
        }

        if (port == null)
        {
            port = getDefaultPortString();
        }

        portDelimiter = port.isBlank() ? "" : ":";
    }


    public String getPassword()
    {
        if (StringUtils.isNotBlank(this.encryptedPassword))
        {
            CryptoUtil crypto = new CryptoUtil();
            return crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), this.encryptedPassword);
        }

        return this.password;
    }

    public String getJdbcUrl()
    {
        if (StringUtils.isNotBlank(jdbcUrl))
        {
            log.info(this.jdbcUrl);
            return this.jdbcUrl;
        }

        //jdbc:sqlserver://localhost;databaseName=FLC_DB;socketTimeout=10000
        //jdbc:mysql://192.168.1.7:3306/lms
        //jdbc:oracle:thin:@192.168.7.25:1521/XE
        /*
        jdbc:oracle:thin:@(DESCRIPTION =
          (CONNECT_TIMEOUT=90)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=50)(RETRY_DELAY=3)
          (ADDRESS = (PROTOCOL = TCP)(HOST = host1)(PORT = port1 ))
          (ADDRESS = (PROTOCOL = TCP)(HOST = host2 )(PORT = port2 ))
          (CONNECT_DATA = (SERVICE_NAME = dbName ))) 
        */
        //jdbc:h2:mem:testdb
        //jdbc:postgresql://localhost:5432/postgres_demo
        String url = "jdbc" + this.getDbTypeString() + this.getHostString() + this.getDbNameString() + this.getOptionsString();
        log.info(url);
        return url;
    }


    private String getOptionsString()
    {
        return switch (this.dbType)
        {
            case DBTYPE_SQLSERVER -> getOptions(true, TimeUnit.MILLISECONDS, ";", ";");
            case DBTYPE_POSTGRESQL -> getOptions(true, TimeUnit.SECONDS, "?", "&");
            case DBTYPE_H2 -> getOptions(false, null, ";", ";");
            case DBTYPE_ORACLE, DBTYPE_MYSQL -> "";
            default -> throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        };
    }


    private String getOptions(boolean includeSocketTimeout, TimeUnit timeUnit, String firstSeparator, String furtherSeparator)
    {
        StringBuilder sb = new StringBuilder();
        if (includeSocketTimeout)
        {
            long socketTimeout = LongUtils.get(this.getSocketTimeout());
            if (timeUnit == TimeUnit.SECONDS)
            {
                socketTimeout /= 1000;
            }
            sb.append(firstSeparator).append(String.format("socketTimeout=%d", socketTimeout));
        }
        if (this.options != null)
        {
            sb.append(furtherSeparator).append(this.options);
        }
        return sb.toString();
    }


    public String getDriverClassName()
    {
        return switch (this.dbType)
        {
            case DBTYPE_SQLSERVER -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case DBTYPE_ORACLE -> "oracle.jdbc.driver.OracleDriver";
            case DBTYPE_MYSQL -> "com.mysql.cj.jdbc.Driver";
            case DBTYPE_H2 -> "org.h2.Driver";
            case DBTYPE_POSTGRESQL -> "org.postgresql.Driver";
            default -> throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        };
    }


    public String getDefaultDialect()
    {
        return switch (this.dbType)
        {
            case DBTYPE_SQLSERVER -> "org.hibernate.dialect.SQLServerDialect";
            case DBTYPE_ORACLE -> "org.hibernate.dialect.Oracle10gDialect";
            case DBTYPE_MYSQL -> "org.hibernate.dialect.MySQL5Dialect";
            case DBTYPE_H2 -> "org.hibernate.dialect.H2Dialect";
            case DBTYPE_POSTGRESQL -> "org.hibernate.dialect.PostgreSQLDialect";
            default -> throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        };
    }


    private String getDefaultPortString()
    {
        return switch (dbType)
        {
            case DBTYPE_SQLSERVER -> "1433";
            case DBTYPE_ORACLE -> "1521";
            case DBTYPE_MYSQL -> "3306";
            case DBTYPE_H2 -> "";
            case DBTYPE_POSTGRESQL -> "5432";
            default -> throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        };
    }


    private String getDbTypeString()
    {
        if (dbType == null || dbType.isEmpty())
        {
            return "";
        }

        return switch (dbType)
        {
            case DBTYPE_SQLSERVER, DBTYPE_MYSQL, DBTYPE_H2, DBTYPE_POSTGRESQL -> ":" + dbType;
            case DBTYPE_ORACLE -> ":" + dbType + ":thin";
            default -> throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, dbType));
        };
    }

    private String getHostString()
    {
        if (host == null || host.isEmpty())
        {
            return "";
        }

        if (DBTYPE_ORACLE.equalsIgnoreCase(dbType))
        {
            return getHostStringOracle();
        }
        else if (DBTYPE_H2.equalsIgnoreCase(dbType))
        {
            return ":" + host;
        }
        else if (DBTYPE_SQLSERVER.equalsIgnoreCase(dbType))
        {
            if (StringUtils.isBlank(this.instance))
            {
                return "://" + host + portDelimiter + port;
            }

            return String.format("://%s;instanceName=%s", host, instance);
        }
        else
        {
            return "://" + host + portDelimiter + port;
        }
    }

    private String getHostStringOracle()
    {
        if (host == null || host.isEmpty())
        {
            return "";
        }

        if (host2 == null || port2 == null)
        {
            return ":@" + host + portDelimiter + port;
        }
        
        /*
        jdbc:oracle:thin:@(DESCRIPTION =
          (CONNECT_TIMEOUT=90)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=50)(RETRY_DELAY=3)
          (ADDRESS = (PROTOCOL = TCP)(HOST = host1)(PORT = port1 ))
          (ADDRESS = (PROTOCOL = TCP)(HOST = host2 )(PORT = port2 ))
          (CONNECT_DATA = (SERVICE_NAME = dbName ))) 
        */

        return ":@(DESCRIPTION = " +
                String.format("(CONNECT_TIMEOUT = %d)", getConnectionTimeout() / 1000) +
                String.format("(TRANSPORT_CONNECT_TIMEOUT = %d)(RETRY_COUNT=50)(RETRY_DELAY=3)", getSocketTimeout() / 1000) +
                String.format("(ADDRESS = (PROTOCOL = TCP)(HOST = %s)(PORT = %s ))", host, port) +
                String.format("(ADDRESS = (PROTOCOL = TCP)(HOST = %s)(PORT = %s ))", host2, port2) +
                String.format("(CONNECT_DATA = (SERVICE_NAME = %s )))", dbName);
    }

    private String getDbNameString()
    {
        if (this.dbName == null || this.dbName.isEmpty())
        {
            return "";
        }

        switch (this.dbType)
        {
            case DBTYPE_SQLSERVER:
                return ";databaseName=" + this.dbName;

            case DBTYPE_ORACLE:
                if (host2 != null && port2 != null)
                {
                    return "";
                }
                return "/" + dbName;
            case DBTYPE_POSTGRESQL:
                return "/" + this.dbName;

            case DBTYPE_MYSQL:
                return "/" + this.dbName + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

            case DBTYPE_H2:
                return ":" + this.dbName;

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }
    }
}
