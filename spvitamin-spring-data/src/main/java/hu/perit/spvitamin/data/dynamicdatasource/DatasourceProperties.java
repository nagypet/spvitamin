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

import hu.perit.spvitamin.spring.config.ConfigProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * Loads datasource properties from the properties file and sets defaults.
 *
 * @author Peter Nagy
 */

@Getter
@Setter
public class DatasourceProperties {

    protected String dbType = "sqlserver";
    protected String host = "localhost";
    protected String port;
    @NotNull
    protected String dbName;
    @NotNull
    protected String username;
    @NotNull
    @ConfigProperty(hidden = true)
    protected String encryptedPassword;
    protected String dialect;
    protected Integer maxPoolSize = 10;

    /*
    connectionTimeout controls the maximum number of milliseconds that a client (that's you) will wait for a connection
    from the pool. If this time is exceeded without a connection becoming available, a SQLException will be thrown.
    Lowest acceptable connection timeout is 250 ms. Default: 30000 (30 seconds)
    */
    private Long connectionTimeout = 30000L;

    /*
    leakDetectionThreshold controls the amount of time that a connection can be out of the pool before a message is
    logged indicating a possible connection leak. A value of 0 means leak detection is disabled. Lowest acceptable
    value for enabling leak detection is 2000 (2 seconds). Default: 0
    */
    private Long leakDetectionThreshold = 0L;

    /*
    HikariCP recommends that the driver-level socket timeout be set to (at least) 2-3x the longest running
    SQL transaction, or 30 seconds, whichever is longer. However, your own recovery time targets should determine
    the appropriate timeout for your application.
    */
    private Long socketTimeout = 100000L;
    private String ddlAuto = "none";
}
