package com.rapidx.jca.db.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * JCA LocalTransaction implementation managing transaction boundaries for database calls.
 */
public class DatabaseLocalTransaction implements LocalTransaction, jakarta.resource.cci.LocalTransaction {
    private static final Logger log = LoggerFactory.getLogger(DatabaseLocalTransaction.class);

    private final DatabaseManagedConnection mc;

    public DatabaseLocalTransaction(DatabaseManagedConnection mc) {
        this.mc = mc;
    }

    @Override
    public void begin() throws ResourceException {
        log.info("JCA Transaction: BEGIN");
        try {
            Connection conn = mc.getPhysicalConnection();
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(false);
            }
            mc.notifyLocalTransactionStarted();
        } catch (SQLException e) {
            throw new ResourceException("Failed to begin JCA transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public void commit() throws ResourceException {
        log.info("JCA Transaction: COMMIT");
        try {
            Connection conn = mc.getPhysicalConnection();
            if (conn != null && !conn.isClosed()) {
                conn.commit();
                conn.setAutoCommit(true);
            }
            mc.notifyLocalTransactionCommitted();
        } catch (SQLException e) {
            throw new ResourceException("Failed to commit JCA transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public void rollback() throws ResourceException {
        log.info("JCA Transaction: ROLLBACK");
        try {
            Connection conn = mc.getPhysicalConnection();
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
            mc.notifyLocalTransactionRolledback();
        } catch (SQLException e) {
            throw new ResourceException("Failed to rollback JCA transaction: " + e.getMessage(), e);
        }
    }
}
