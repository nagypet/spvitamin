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

import com.zaxxer.hikari.HikariDataSource;
import hu.perit.spvitamin.core.StackTracer;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Ez az implementáció ténylegesen close-olható. Close hívása esetén új DataSource objektumot hoz létre, így a korábbitól
 * különböző paraméterekkel újrainicializálható.
 *
 * @author Peter Nagy
 */

@Log4j
public class DynamicDataSource implements DataSource, Closeable {

    private HikariDataSource dataSource;
    @Getter
    private boolean connected;
    private boolean initialized = false;

    public DynamicDataSource() {
        this.dataSource = new HikariDataSource();
        this.init();
    }

    public void setConnectionParam(ConnectionParam connParam) {
        this.dataSource.setDriverClassName(connParam.getDriverClassName());
        this.dataSource.setJdbcUrl(connParam.getJdbcUrl());
        this.dataSource.setUsername(connParam.getUsername());
        this.dataSource.setPassword(connParam.getPassword());
        this.dataSource.setMaximumPoolSize(connParam.getMaxPoolSize());
        this.dataSource.setConnectionTimeout(connParam.getConnectionTimeout());
        this.dataSource.setLeakDetectionThreshold(connParam.getLeakDetectionThreshold());
        try {
            dataSource.setLoginTimeout(5);
        }
        catch (SQLException e) {
            log.error(StackTracer.toString(e));
        }
        finally {
            this.initialized = true;
        }
    }

    private void init() {
        this.connected = false;
        // Ez a minimális konfig, amivel már elindul a szerver. A valódi kapcsolati paramétereket később állítjuk be.
        //this.dataSource.setJdbcUrl("jdbc:sqlserver://localhost");
        //this.dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Override
    public void close() {
        this.dataSource.close();
        this.dataSource = new HikariDataSource();
        this.init();
        this.initialized = false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (!this.initialized) {
            throw new SQLException("Data source connection parameter is not set!");
        }

        try {
            Connection connection = this.dataSource.getConnection();
            log.debug("DynamicDataSource created a new Connection.");
            this.connected = true;
            return connection;
        }
        catch (SQLException ex) {
            this.connected = false;
            throw ex;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            Connection connection = this.dataSource.getConnection(username, password);
            log.debug("DynamicDataSource created a new Connection.");
            this.connected = true;
            return connection;
        }
        catch (SQLException ex) {
            this.connected = false;
            throw ex;
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.dataSource.getParentLogger();
    }
}
