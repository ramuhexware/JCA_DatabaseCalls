package com.rapidx.jca.db.spi;

import com.rapidx.jca.db.cci.DatabaseConnectionImpl;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JCA ManagedConnection implementation wrapping physical JDBC database connection and managing connection listeners.
 */
public class DatabaseManagedConnection implements ManagedConnection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManagedConnection.class);

    private final Connection physicalConnection;
    private final String userName;
    private final List<ConnectionEventListener> listeners = new ArrayList<>();
    private final List<DatabaseConnectionImpl> handles = new ArrayList<>();
    private final DatabaseLocalTransaction localTransaction;
    private PrintWriter logWriter;

    public DatabaseManagedConnection(Connection physicalConnection, String userName) {
        this.physicalConnection = physicalConnection;
        this.userName = userName;
        this.localTransaction = new DatabaseLocalTransaction(this);
    }

    public Connection getPhysicalConnection() {
        return physicalConnection;
    }

    public DatabaseLocalTransaction getLocalTransactionCCI() {
        return localTransaction;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.debug("Creating new JCA connection handle from ManagedConnection");
        DatabaseConnectionImpl handle = new DatabaseConnectionImpl(this);
        handles.add(handle);
        return handle;
    }

    @Override
    public void destroy() throws ResourceException {
        log.info("Destroying ManagedConnection and physical database connection");
        try {
            if (physicalConnection != null && !physicalConnection.isClosed()) {
                physicalConnection.close();
            }
        } catch (SQLException e) {
            throw new ResourceException("Error closing physical connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void cleanup() throws ResourceException {
        log.debug("Cleaning up ManagedConnection handles");
        handles.clear();
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof DatabaseConnectionImpl handle)) {
            throw new ResourceException("Invalid connection handle type");
        }
        handles.add(handle);
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new DatabaseManagedConnectionMetaData(userName);
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return localTransaction;
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null; // Local transactions supported
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    // Listener Notifications
    public void notifyConnectionClosed(DatabaseConnectionImpl handle) {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        for (ConnectionEventListener listener : new ArrayList<>(listeners)) {
            listener.connectionClosed(event);
        }
    }

    public void notifyLocalTransactionStarted() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
        for (ConnectionEventListener listener : new ArrayList<>(listeners)) {
            listener.localTransactionStarted(event);
        }
    }

    public void notifyLocalTransactionCommitted() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
        for (ConnectionEventListener listener : new ArrayList<>(listeners)) {
            listener.localTransactionCommitted(event);
        }
    }

    public void notifyLocalTransactionRolledback() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
        for (ConnectionEventListener listener : new ArrayList<>(listeners)) {
            listener.localTransactionRolledback(event);
        }
    }
}
