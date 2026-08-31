package com.rapidx.jca.db.cci;

import com.rapidx.jca.db.record.ListRecord;
import com.rapidx.jca.db.record.MapRecord;
import com.rapidx.jca.db.spi.DatabaseManagedConnection;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.ResourceWarning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JCA CCI Interaction implementation executing database queries and updates via underlying ManagedConnection.
 */
public class DatabaseInteractionImpl implements Interaction {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInteractionImpl.class);

    private final DatabaseConnectionImpl connection;
    private final DatabaseManagedConnection managedConnection;

    public DatabaseInteractionImpl(DatabaseConnectionImpl connection, DatabaseManagedConnection managedConnection) {
        this.connection = connection;
        this.managedConnection = managedConnection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() throws ResourceException {
        log.debug("Closing DatabaseInteraction handle");
    }

    @Override
    public jakarta.resource.cci.Record execute(InteractionSpec ispec, jakarta.resource.cci.Record input) throws ResourceException {
        jakarta.resource.cci.Record output = new MapRecord();
        execute(ispec, input, output);
        return output;
    }

    @Override
    public boolean execute(InteractionSpec ispec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output) throws ResourceException {
        if (!(ispec instanceof DatabaseInteractionSpec dbSpec)) {
            throw new ResourceException("Unsupported InteractionSpec: expected DatabaseInteractionSpec");
        }

        String function = dbSpec.getFunctionName();
        java.sql.Connection sqlConn = managedConnection.getPhysicalConnection();

        try {
            switch (function) {
                case DatabaseInteractionSpec.OP_CREATE_TABLE -> executeCreateTable(sqlConn, dbSpec);
                case DatabaseInteractionSpec.OP_INSERT -> executeInsert(sqlConn, dbSpec, input, output);
                case DatabaseInteractionSpec.OP_FIND_BY_ID -> executeFindById(sqlConn, dbSpec, input, output);
                case DatabaseInteractionSpec.OP_FIND_ALL -> executeFindAll(sqlConn, dbSpec, output);
                case DatabaseInteractionSpec.OP_UPDATE -> executeUpdate(sqlConn, dbSpec, input, output);
                case DatabaseInteractionSpec.OP_DELETE -> executeDelete(sqlConn, dbSpec, input, output);
                default -> throw new ResourceException("Unknown operation function: " + function);
            }
            return true;
        } catch (SQLException e) {
            log.error("JCA Database Interaction failed for function {}: {}", function, e.getMessage(), e);
            throw new ResourceException("Database execution error: " + e.getMessage(), e);
        }
    }

    @Override
    public ResourceWarning getWarnings() throws ResourceException {
        return null;
    }

    @Override
    public void clearWarnings() throws ResourceException {
    }

    private void executeCreateTable(java.sql.Connection conn, DatabaseInteractionSpec spec) throws SQLException {
        String sql = spec.getSqlQuery();
        log.info("JCA Interaction [CREATE_TABLE]: {}", sql);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @SuppressWarnings("rawtypes")
    private void executeInsert(java.sql.Connection conn, DatabaseInteractionSpec spec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output) throws SQLException, ResourceException {
        if (!(input instanceof MappedRecord mappedInput)) {
            throw new ResourceException("INSERT operation requires MappedRecord input");
        }

        String tableName = spec.getTableName();
        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Object obj : mappedInput.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            keys.add(entry.getKey().toString());
            values.add(entry.getValue());
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        sql.append(String.join(", ", keys));
        sql.append(") VALUES (");
        sql.append("?, ".repeat(keys.size()));
        sql.setLength(sql.length() - 2); // trim trailing comma
        sql.append(")");

        log.info("JCA Interaction [INSERT]: {} with params {}", sql, values);

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            int rowsAffected = pstmt.executeUpdate();
            if (output instanceof MappedRecord mappedOutput) {
                mappedOutput.put("ROWS_AFFECTED", rowsAffected);
                mappedOutput.put("STATUS", "SUCCESS");
            }
        }
    }

    private void executeFindById(java.sql.Connection conn, DatabaseInteractionSpec spec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output) throws SQLException, ResourceException {
        if (!(input instanceof MappedRecord mappedInput)) {
            throw new ResourceException("FIND_BY_ID operation requires MappedRecord input containing 'id'");
        }

        Object id = mappedInput.get("id");
        if (id == null) {
            id = mappedInput.get("ID");
        }
        if (id == null) {
            throw new ResourceException("FIND_BY_ID missing 'id' attribute in input record");
        }

        String tableName = spec.getTableName();
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        log.info("JCA Interaction [FIND_BY_ID]: {} (id={})", sql, id);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && output instanceof MappedRecord mappedOutput) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String colName = metaData.getColumnName(i).toLowerCase();
                        mappedOutput.put(colName, rs.getObject(i));
                    }
                }
            }
        }
    }

    private void executeFindAll(java.sql.Connection conn, DatabaseInteractionSpec spec, jakarta.resource.cci.Record output) throws SQLException, ResourceException {
        if (!(output instanceof ListRecord listOutput)) {
            throw new ResourceException("FIND_ALL operation requires ListRecord output target");
        }

        String tableName = spec.getTableName();
        String sql = "SELECT * FROM " + tableName;
        log.info("JCA Interaction [FIND_ALL]: {}", sql);

        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                MapRecord rowRecord = new MapRecord("UserRecord", "Single Database Row");
                for (int i = 1; i <= columnCount; i++) {
                    String colName = metaData.getColumnName(i).toLowerCase();
                    rowRecord.put(colName, rs.getObject(i));
                }
                listOutput.add(rowRecord);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void executeUpdate(java.sql.Connection conn, DatabaseInteractionSpec spec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output) throws SQLException, ResourceException {
        if (!(input instanceof MappedRecord mappedInput)) {
            throw new ResourceException("UPDATE operation requires MappedRecord input");
        }

        Object id = mappedInput.get("id");
        if (id == null) {
            id = mappedInput.get("ID");
        }
        if (id == null) {
            throw new ResourceException("UPDATE missing 'id' attribute in input record");
        }

        String tableName = spec.getTableName();
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Object obj : mappedInput.entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            String key = entry.getKey().toString();
            if (!key.equalsIgnoreCase("id")) {
                setClauses.add(key + " = ?");
                values.add(entry.getValue());
            }
        }

        if (setClauses.isEmpty()) {
            throw new ResourceException("No columns to update");
        }

        values.add(id); // for WHERE id = ?
        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setClauses) + " WHERE id = ?";
        log.info("JCA Interaction [UPDATE]: {} with params {}", sql, values);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            int rowsAffected = pstmt.executeUpdate();
            if (output instanceof MappedRecord mappedOutput) {
                mappedOutput.put("ROWS_AFFECTED", rowsAffected);
                mappedOutput.put("STATUS", "SUCCESS");
            }
        }
    }

    private void executeDelete(java.sql.Connection conn, DatabaseInteractionSpec spec, jakarta.resource.cci.Record input, jakarta.resource.cci.Record output) throws SQLException, ResourceException {
        if (!(input instanceof MappedRecord mappedInput)) {
            throw new ResourceException("DELETE operation requires MappedRecord input containing 'id'");
        }

        Object id = mappedInput.get("id");
        if (id == null) {
            id = mappedInput.get("ID");
        }
        if (id == null) {
            throw new ResourceException("DELETE missing 'id' attribute in input record");
        }

        String tableName = spec.getTableName();
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        log.info("JCA Interaction [DELETE]: {} (id={})", sql, id);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (output instanceof MappedRecord mappedOutput) {
                mappedOutput.put("ROWS_AFFECTED", rowsAffected);
                mappedOutput.put("STATUS", "SUCCESS");
            }
        }
    }
}
