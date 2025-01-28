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

import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import com.zaxxer.hikari.HikariDataSource;

import hu.perit.spvitamin.core.StackTracer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * This implementation can actually be closed. When you call close(), it creates a new DataSource object 
 * so that it can be re-initialized with different parameters than before.
 *
 * @author Peter Nagy
 */

@Slf4j
public class DynamicDataSource implements DataSource, Closeable
{

    private HikariDataSource dataSource;
    @Getter
    private boolean connected;
    private boolean initialized = false;

    public DynamicDataSource()
    {
        this.dataSource = new HikariDataSource();
        this.init();
    }

    public void setConnectionParam(ConnectionParam connParam)
    {
        this.dataSource.setDriverClassName(connParam.getDriverClassName());
        this.dataSource.setJdbcUrl(connParam.getJdbcUrl());
        this.dataSource.setUsername(connParam.getUsername());
        this.dataSource.setPassword(connParam.getPassword());
        this.dataSource.setMaximumPoolSize(connParam.getMaxPoolSize());
        this.dataSource.setConnectionTimeout(connParam.getConnectionTimeout());
        this.dataSource.setLeakDetectionThreshold(connParam.getLeakDetectionThreshold());
        this.dataSource.setConnectionInitSql(connParam.getConnectionInitSql());
        try
        {
            dataSource.setLoginTimeout(5);
        }
        catch (SQLException e)
        {
            log.error(StackTracer.toString(e));
        }
        finally
        {
            this.initialized = true;
        }
    }

    private void init()
    {
        this.connected = false;
        // Ez a minimális konfig, amivel már elindul a szerver. A valódi kapcsolati paramétereket később állítjuk be.
        //this.dataSource.setJdbcUrl("jdbc:sqlserver://localhost");
        //this.dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Override
    public void close()
    {
        this.dataSource.close();
        this.dataSource = new HikariDataSource();
        this.init();
        this.initialized = false;
    }

    @Override
    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 10_000, multiplier = 2.0, maxDelay = 60_000))
    public Connection getConnection() throws SQLException
    {
        if (!this.initialized)
        {
            throw new SQLException("Data source connection parameter is not set!");
        }

        try
        {
            Connection connection = this.dataSource.getConnection();
            log.debug("DynamicDataSource created a new Connection.");
            this.connected = true;
            return connection;
        }
        catch (SQLException ex)
        {
            log.error(ex.toString());
            this.connected = false;
            throw ex;
        }
    }

    @Override
    @Retryable(maxAttempts = 4, backoff = @Backoff(delay = 10_000, multiplier = 2.0, maxDelay = 60_000))
    public Connection getConnection(String username, String password) throws SQLException
    {
        try
        {
            Connection connection = this.dataSource.getConnection(username, password);
            log.debug("DynamicDataSource created a new Connection.");
            this.connected = true;
            return connection;
        }
        catch (SQLException ex)
        {
            log.error(ex.toString());
            this.connected = false;
            throw ex;
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return this.dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return this.dataSource.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException
    {
        return this.dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        this.dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException
    {
        this.dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException
    {
        return this.dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return this.dataSource.getParentLogger();
    }
}
