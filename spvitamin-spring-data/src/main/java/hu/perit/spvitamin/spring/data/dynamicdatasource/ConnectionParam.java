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

package hu.perit.spvitamin.spring.data.dynamicdatasource;

import org.modelmapper.ModelMapper;

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import hu.perit.spvitamin.spring.exception.InvalidInputException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A convenience class, which creates a JDBC connection string using the raw datasource properties for the popular database systems.
 *
 * @author Peter Nagy
 */

@Getter
@Setter
@ToString
@Slf4j
public class ConnectionParam extends DatasourceProperties {

    public static final String DBTYPE_SQLSERVER = "sqlserver";
    public static final String DBTYPE_ORACLE = "oracle";
    public static final String DBTYPE_MYSQL = "mysql";
    public static final String DBTYPE_H2 = "h2";
    public static final String DBTYPE_POSTGRESQL = "postgresql";

    private static final String NOT_YET_SUPPORTED = "'%s' not yet supported";

    private final String portDelimiter;

    public ConnectionParam(DatasourceProperties properties) {
    	if (properties == null) {
    		throw new IllegalArgumentException("DatasourceProperties is 'null'! Most probably the application cannot access the database configuration. Please make sure, the application.properties is available.");
    	}
    	
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(properties, this);

        if (dialect == null) {
            dialect = getDefaultDialect();
        }

        if (port == null) {
            port = getDefaultPortString();
        }
        
        portDelimiter = port.isBlank() ? "" : ":";
    }


    public String getPassword() {
        CryptoUtil crypto = new CryptoUtil();

        return crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), this.getEncryptedPassword());
    }

    public String getJdbcUrl() {
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
        String url = "jdbc" + this.getDbTypeString() + this.getHostString() + this.getDbNameString() + this.getOptions();
        log.info(url);
        return url;
    }

    private String getOptions() {
        switch (this.dbType) {
            case DBTYPE_SQLSERVER:
                return String.format(";socketTimeout=%d", this.getSocketTimeout());

            case DBTYPE_ORACLE:
            case DBTYPE_MYSQL:
            case DBTYPE_H2:
            case DBTYPE_POSTGRESQL:
                break;

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }

        return "";
    }


    public String getDriverClassName() {
        switch (this.dbType) {
            case DBTYPE_SQLSERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";

            case DBTYPE_ORACLE:
                return "oracle.jdbc.driver.OracleDriver";

            case DBTYPE_MYSQL:
                return "com.mysql.cj.jdbc.Driver";

            case DBTYPE_H2:
                return "org.h2.Driver";

            case DBTYPE_POSTGRESQL:
                return "org.postgresql.Driver";

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }
    }


    public String getDefaultDialect() {
        switch (this.dbType) {
            case DBTYPE_SQLSERVER:
                return "org.hibernate.dialect.SQLServer2012Dialect";

            case DBTYPE_ORACLE:
                return "org.hibernate.dialect.Oracle10gDialect";

            case DBTYPE_MYSQL:
                return "org.hibernate.dialect.MySQL5Dialect";

            case DBTYPE_H2:
                return "org.hibernate.dialect.H2Dialect";

            case DBTYPE_POSTGRESQL:
                return "org.hibernate.dialect.PostgreSQLDialect";

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }
    }


    private String getDefaultPortString() {
        switch (dbType) {
            case DBTYPE_SQLSERVER:
                return "1433";

            case DBTYPE_ORACLE:
                return "1521";

            case DBTYPE_MYSQL:
                return "3306";

            case DBTYPE_H2:
                return "";

            case DBTYPE_POSTGRESQL:
                return "5432";

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }
    }


    private String getDbTypeString() {
        if (dbType == null || dbType.isEmpty()) {
            return "";
        }
        
        switch (dbType) {
            case DBTYPE_SQLSERVER:
            case DBTYPE_MYSQL:
            case DBTYPE_H2:
            case DBTYPE_POSTGRESQL:
                return ":" + dbType;

            case DBTYPE_ORACLE:
                return ":" + dbType + ":thin";

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, dbType));
        }
    }

    private String getHostString() {
        if (host == null || host.isEmpty()) {
            return "";
        }
        
        if (DBTYPE_ORACLE.equalsIgnoreCase(dbType)) {
            return getHostStringOracle();
        }
        else if (DBTYPE_H2.equalsIgnoreCase(dbType)) {
            return ":" + host;
        }
        else {
            return "://" + host + portDelimiter + port;
        }
    }
    
    private String getHostStringOracle()  {
        if (host == null || host.isEmpty()) {
            return "";
        }
        
        if(host2 == null || port2 == null) {
            return ":@" + host + portDelimiter + port; 
        }
        
        /*
        jdbc:oracle:thin:@(DESCRIPTION =
          (CONNECT_TIMEOUT=90)(TRANSPORT_CONNECT_TIMEOUT=3)(RETRY_COUNT=50)(RETRY_DELAY=3)
          (ADDRESS = (PROTOCOL = TCP)(HOST = host1)(PORT = port1 ))
          (ADDRESS = (PROTOCOL = TCP)(HOST = host2 )(PORT = port2 ))
          (CONNECT_DATA = (SERVICE_NAME = dbName ))) 
        */
        
        StringBuilder b = new  StringBuilder();
        b.append(":@(DESCRIPTION = ");
        b.append(String.format("(CONNECT_TIMEOUT = %d)", getConnectionTimeout() / 1000));
        b.append(String.format("(TRANSPORT_CONNECT_TIMEOUT = %d)(RETRY_COUNT=50)(RETRY_DELAY=3)", getSocketTimeout() / 1000));
        b.append(String.format("(ADDRESS = (PROTOCOL = TCP)(HOST = %s)(PORT = %s ))", host, port));
        b.append(String.format("(ADDRESS = (PROTOCOL = TCP)(HOST = %s)(PORT = %s ))", host2, port2));
        b.append(String.format("(CONNECT_DATA = (SERVICE_NAME = %s )))", dbName));
        return b.toString();
    }

    private String getDbNameString() {
        if (this.dbName == null || this.dbName.isEmpty()) {
            return "";
        }
        
        switch (this.dbType) {
            case DBTYPE_SQLSERVER:
                return ";databaseName=" + this.dbName;

            case DBTYPE_ORACLE:
                if(host2 != null && port2 != null) {
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
