package com.rapidx.jca.db.spi;

import com.rapidx.jca.db.cci.DatabaseConnectionFactoryImpl;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/**
 * JCA ManagedConnectionFactory implementation creating physical database connections and ConnectionFactory instances.
 */
public class DatabaseManagedConnectionFactory implements ManagedConnectionFactory {
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DatabaseManagedConnectionFactory.class);

    private String databaseUrl = "jdbc:h2:mem:jcadb;DB_CLOSE_DELAY=-1";
    private String userName = "sa";
    private String password = "";
    private String driverClass = "org.h2.Driver";
    private PrintWriter logWriter;

    public DatabaseManagedConnectionFactory() {
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.info("Creating ConnectionFactory with ConnectionManager");
        return new DatabaseConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        log.info("Creating non-managed ConnectionFactory");
        return new DatabaseConnectionFactoryImpl(this, null);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.info("Connecting to physical database: {}", databaseUrl);
        try {
            Class.forName(driverClass);
            Connection jdbcConn = DriverManager.getConnection(databaseUrl, userName, password);
            return new DatabaseManagedConnection(jdbcConn, userName);
        } catch (ClassNotFoundException e) {
            throw new ResourceException("JDBC Driver class not found: " + driverClass, e);
        } catch (SQLException e) {
            throw new ResourceException("Failed to open physical database connection: " + e.getMessage(), e);
        }
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        for (Object obj : connectionSet) {
            if (obj instanceof DatabaseManagedConnection mc) {
                return mc;
            }
        }
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseManagedConnectionFactory that = (DatabaseManagedConnectionFactory) o;
        return Objects.equals(databaseUrl, that.databaseUrl) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(driverClass, that.driverClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseUrl, userName, driverClass);
    }
}
