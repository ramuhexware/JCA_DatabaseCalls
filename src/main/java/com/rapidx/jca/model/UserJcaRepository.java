package com.rapidx.jca.model;

import com.rapidx.jca.db.cci.DatabaseInteractionSpec;
import com.rapidx.jca.db.record.ListRecord;
import com.rapidx.jca.db.record.MapRecord;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository layer performing database persistence, retrieval, updates, and deletion strictly via JCA CCI interfaces.
 */
public class UserJcaRepository {
    private static final Logger log = LoggerFactory.getLogger(UserJcaRepository.class);
    private static final String TABLE_NAME = "users";

    private final ConnectionFactory connectionFactory;

    public UserJcaRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Initializes the database schema using JCA Interaction.
     */
    public void createTable() throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            createTable(conn);
        } finally {
            conn.close();
        }
    }

    public void createTable(Connection conn) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_CREATE_TABLE, TABLE_NAME);
            spec.setSqlQuery("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "department VARCHAR(50), " +
                    "salary DOUBLE)");
            interaction.execute(spec, new MapRecord());
            log.info("Table '{}' initialized successfully via JCA", TABLE_NAME);
        } finally {
            interaction.close();
        }
    }

    /**
     * Persists a User entity into the database using JCA CCI MappedRecord.
     */
    public void save(User user) throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            save(conn, user);
        } finally {
            conn.close();
        }
    }

    public void save(Connection conn, User user) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_INSERT, TABLE_NAME);
            MappedRecord inputRecord = (MappedRecord) connectionFactory.getRecordFactory().createMappedRecord("UserInputRecord");
            inputRecord.put("id", user.getId());
            inputRecord.put("name", user.getName());
            inputRecord.put("email", user.getEmail());
            inputRecord.put("department", user.getDepartment());
            inputRecord.put("salary", user.getSalary());

            MappedRecord outputRecord = new MapRecord();
            interaction.execute(spec, inputRecord, outputRecord);
            log.info("Persisted User [ID={}] via JCA. Output: {}", user.getId(), outputRecord);
        } finally {
            interaction.close();
        }
    }

    /**
     * Retrieves a User by ID using JCA CCI interaction.
     */
    public Optional<User> findById(Long id) throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            return findById(conn, id);
        } finally {
            conn.close();
        }
    }

    public Optional<User> findById(Connection conn, Long id) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_FIND_BY_ID, TABLE_NAME);
            MappedRecord inputRecord = new MapRecord();
            inputRecord.put("id", id);

            MappedRecord outputRecord = new MapRecord();
            interaction.execute(spec, inputRecord, outputRecord);

            if (outputRecord.isEmpty()) {
                return Optional.empty();
            }

            User user = mapRecordToUser(outputRecord);
            log.info("Retrieved User [ID={}] via JCA: {}", id, user);
            return Optional.of(user);
        } finally {
            interaction.close();
        }
    }

    /**
     * Retrieves all Users using JCA CCI IndexedRecord list.
     */
    public List<User> findAll() throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            return findAll(conn);
        } finally {
            conn.close();
        }
    }

    public List<User> findAll(Connection conn) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_FIND_ALL, TABLE_NAME);
            ListRecord outputRecord = new ListRecord();

            interaction.execute(spec, new MapRecord(), outputRecord);

            List<User> users = new ArrayList<>();
            for (Object item : outputRecord) {
                if (item instanceof MappedRecord mappedRow) {
                    users.add(mapRecordToUser(mappedRow));
                }
            }
            log.info("Retrieved {} Users via JCA", users.size());
            return users;
        } finally {
            interaction.close();
        }
    }

    /**
     * Updates an existing User using JCA CCI interaction.
     */
    public void update(User user) throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            update(conn, user);
        } finally {
            conn.close();
        }
    }

    public void update(Connection conn, User user) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_UPDATE, TABLE_NAME);
            MappedRecord inputRecord = new MapRecord();
            inputRecord.put("id", user.getId());
            inputRecord.put("name", user.getName());
            inputRecord.put("email", user.getEmail());
            inputRecord.put("department", user.getDepartment());
            inputRecord.put("salary", user.getSalary());

            MappedRecord outputRecord = new MapRecord();
            interaction.execute(spec, inputRecord, outputRecord);
            log.info("Updated User [ID={}] via JCA. Result: {}", user.getId(), outputRecord);
        } finally {
            interaction.close();
        }
    }

    /**
     * Deletes a User by ID using JCA CCI interaction.
     */
    public void deleteById(Long id) throws ResourceException {
        Connection conn = connectionFactory.getConnection();
        try {
            deleteById(conn, id);
        } finally {
            conn.close();
        }
    }

    public void deleteById(Connection conn, Long id) throws ResourceException {
        Interaction interaction = conn.createInteraction();
        try {
            DatabaseInteractionSpec spec = new DatabaseInteractionSpec(DatabaseInteractionSpec.OP_DELETE, TABLE_NAME);
            MappedRecord inputRecord = new MapRecord();
            inputRecord.put("id", id);

            MappedRecord outputRecord = new MapRecord();
            interaction.execute(spec, inputRecord, outputRecord);
            log.info("Deleted User [ID={}] via JCA. Result: {}", id, outputRecord);
        } finally {
            interaction.close();
        }
    }

    private User mapRecordToUser(MappedRecord record) {
        User user = new User();
        if (record.get("id") != null) user.setId(((Number) record.get("id")).longValue());
        if (record.get("name") != null) user.setName(record.get("name").toString());
        if (record.get("email") != null) user.setEmail(record.get("email").toString());
        if (record.get("department") != null) user.setDepartment(record.get("department").toString());
        if (record.get("salary") != null) user.setSalary(((Number) record.get("salary")).doubleValue());
        return user;
    }
}
