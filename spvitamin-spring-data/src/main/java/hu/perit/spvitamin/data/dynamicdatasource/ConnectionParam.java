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

package hu.perit.spvitamin.data.dynamicdatasource;

import hu.perit.spvitamin.core.crypto.CryptoUtil;
import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.exception.InvalidInputException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import org.modelmapper.ModelMapper;

/**
 * A convenience class, which creates a JDBC connection string using the raw datasource properties for the popular database systems.
 *
 * @author Peter Nagy
 */

@Getter
@Setter
@ToString
@Log4j
public class ConnectionParam extends DatasourceProperties {

    public static final String DBTYPE_SQLSERVER = "sqlserver";
    public static final String DBTYPE_ORACLE = "oracle";
    public static final String DBTYPE_MYSQL = "mysql";
    public static final String DBTYPE_H2 = "h2";
    public static final String DBTYPE_POSTGRESQL = "postgresql";

    private static final String NOT_YET_SUPPORTED = "'%s' not yet supported";


    public ConnectionParam(DatasourceProperties properties) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(properties, this);

        if (this.dialect == null) {
            this.dialect = this.getDefaultDialect();
        }

        if (this.port == null) {
            this.port = this.getDefaultPortString();
        }
        else if (!this.port.isBlank() && !this.port.startsWith(":")) {
            this.port = ":" + this.port;
        }
    }


    public String getPassword() {
        CryptoUtil crypto = new CryptoUtil();

        return crypto.decrypt(SysConfig.getCryptoProperties().getSecret(), this.getEncryptedPassword());
    }

    public String getJdbcUrl() {
        //jdbc:sqlserver://localhost;databaseName=FLC_DB;socketTimeout=10000
        //jdbc:mysql://192.168.1.7:3306/lms
        //jdbc:oracle:thin:@192.168.7.25:1521/XE
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
        switch (this.dbType) {
            case DBTYPE_SQLSERVER:
                return ":1433";

            case DBTYPE_ORACLE:
                return ":1521";

            case DBTYPE_MYSQL:
                return ":3306";

            case DBTYPE_H2:
                return "";

            case DBTYPE_POSTGRESQL:
                return ":5432";

            default:
                throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
        }
    }


    private String getDbTypeString() {
        if (this.dbType == null || this.dbType.isEmpty()) {
            return "";
        }
        else {
            switch (this.dbType) {
                case DBTYPE_SQLSERVER:
                case DBTYPE_MYSQL:
                case DBTYPE_H2:
                case DBTYPE_POSTGRESQL:
                    return ":" + this.dbType;

                case DBTYPE_ORACLE:
                    return ":" + this.dbType + ":thin";

                default:
                    throw new InvalidInputException(String.format(NOT_YET_SUPPORTED, this.dbType));
            }
        }
    }

    private String getHostString() {
        if (this.host == null || this.host.isEmpty()) {
            return "";
        }
        else {
            if (DBTYPE_ORACLE.equalsIgnoreCase(this.dbType)) {
                return ":@" + this.host + this.port;
            }
            else if (DBTYPE_H2.equalsIgnoreCase(this.dbType)) {
                return ":" + this.host;
            }
            else {
                return "://" + this.host + this.port;
            }
        }
    }

    private String getDbNameString() {
        if (this.dbName == null || this.dbName.isEmpty()) {
            return "";
        }
        else {
            switch (this.dbType) {
                case DBTYPE_SQLSERVER:
                    return ";databaseName=" + this.dbName;

                case DBTYPE_ORACLE:
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
}
