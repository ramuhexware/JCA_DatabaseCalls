package com.rapidx.jca.db.cci;

import com.rapidx.jca.db.spi.DatabaseManagedConnection;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA CCI Connection implementation providing connection handle to application clients.
 */
public class DatabaseConnectionImpl implements Connection {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionImpl.class);

    private final DatabaseManagedConnection mc;
    private boolean closed = false;

    public DatabaseConnectionImpl(DatabaseManagedConnection mc) {
        this.mc = mc;
    }

    public DatabaseManagedConnection getManagedConnection() {
        return mc;
    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        checkClosed();
        return new DatabaseInteractionImpl(this, mc);
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        checkClosed();
        return mc.getLocalTransactionCCI();
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        checkClosed();
        return new ConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return "JCA Custom Database Connector";
            }

            @Override
            public String getEISProductVersion() throws ResourceException {
                return "1.0.0";
            }

            @Override
            public String getUserName() throws ResourceException {
                return mc.getMetaData().getUserName();
            }
        };
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        return null;
    }

    @Override
    public void close() throws ResourceException {
        if (!closed) {
            closed = true;
            log.info("Closing JCA Connection handle");
            mc.notifyConnectionClosed(this);
        }
    }

    private void checkClosed() throws ResourceException {
        if (closed) {
            throw new ResourceException("JCA Connection handle is closed");
        }
    }
}
