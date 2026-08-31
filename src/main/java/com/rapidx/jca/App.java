package com.rapidx.jca;

import com.rapidx.jca.db.spi.DatabaseManagedConnectionFactory;
import com.rapidx.jca.db.spi.DatabaseResourceAdapter;
import com.rapidx.jca.model.User;
import com.rapidx.jca.model.UserJcaRepository;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.LocalTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Main application demonstrating JCA Adapter usage for Database Call, Persistence, and Retrieval.
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("================================================================================");
        log.info("       STARTING JCA DATABASE ADAPTER PERSISTENCE & RETRIEVAL DEMO              ");
        log.info("================================================================================");

        try {
            // Step 1: Bootstrap JCA Resource Adapter
            DatabaseResourceAdapter resourceAdapter = new DatabaseResourceAdapter();
            resourceAdapter.start(null);

            // Step 2: Configure ManagedConnectionFactory (SPI layer)
            DatabaseManagedConnectionFactory mcf = new DatabaseManagedConnectionFactory();
            mcf.setDatabaseUrl("jdbc:h2:mem:jcademo;DB_CLOSE_DELAY=-1");
            mcf.setUserName("sa");
            mcf.setPassword("");

            // Step 3: Obtain ConnectionFactory (CCI client entry point)
            ConnectionFactory connectionFactory = (ConnectionFactory) mcf.createConnectionFactory();

            // Step 4: Initialize JCA Repository
            UserJcaRepository repository = new UserJcaRepository(connectionFactory);

            // Step 5: Database Call - Schema Creation
            log.info("\n--- [1] INITIALIZING DATABASE SCHEMA VIA JCA INTERACTION ---");
            repository.createTable();

            // Step 6: Database Persistence - Insert records
            log.info("\n--- [2] PERSISTING USER RECORDS TO DATABASE VIA JCA ---");
            User user1 = new User(101L, "Alice Smith", "alice.smith@rapidx.com", "Engineering", 125000.0);
            User user2 = new User(102L, "Bob Jones", "bob.jones@rapidx.com", "Finance", 98000.0);
            User user3 = new User(103L, "Charlie Brown", "charlie.brown@rapidx.com", "Product", 110000.0);

            repository.save(user1);
            repository.save(user2);
            repository.save(user3);

            // Step 7: Database Retrieval - Fetch Single Record
            log.info("\n--- [3] RETRIEVING SINGLE RECORD BY ID VIA JCA ---");
            Optional<User> retrievedUser = repository.findById(101L);
            retrievedUser.ifPresent(u -> log.info("Successfully retrieved user: {}", u));

            // Step 8: Database Retrieval - Fetch All Records
            log.info("\n--- [4] RETRIEVING ALL RECORDS VIA JCA INDEXED RECORD ---");
            List<User> allUsers = repository.findAll();
            allUsers.forEach(u -> log.info(" -> User item: {}", u));

            // Step 9: Database Update
            log.info("\n--- [5] UPDATING RECORD VIA JCA ---");
            user2.setDepartment("Executive Finance");
            user2.setSalary(115000.0);
            repository.update(user2);

            Optional<User> updatedUser = repository.findById(102L);
            updatedUser.ifPresent(u -> log.info("Updated record in DB: {}", u));

            // Step 10: Demonstrating JCA Local Transaction Commit & Rollback
            log.info("\n--- [6] DEMONSTRATING JCA TRANSACTION MANAGEMENT (COMMIT & ROLLBACK) ---");
            demoTransactionCommit(connectionFactory, repository);
            demoTransactionRollback(connectionFactory, repository);

            // Step 11: Database Deletion
            log.info("\n--- [7] DELETING RECORD VIA JCA ---");
            repository.deleteById(103L);
            log.info("Remaining users count: {}", repository.findAll().size());

            // Step 12: Shutdown JCA Adapter
            resourceAdapter.stop();
            log.info("\n================================================================================");
            log.info("       JCA DATABASE ADAPTER DEMONSTRATION COMPLETED SUCCESSFULLY               ");
            log.info("================================================================================");

        } catch (Exception e) {
            log.error("Error executing JCA database application", e);
        }
    }

    private static void demoTransactionCommit(ConnectionFactory cf, UserJcaRepository repo) throws Exception {
        log.info("Starting JCA Local Transaction (COMMITTED)...");
        Connection conn = cf.getConnection();
        LocalTransaction tx = conn.getLocalTransaction();
        try {
            tx.begin();
            User txUser = new User(104L, "Diana Prince", "diana.prince@rapidx.com", "Security", 130000.0);
            repo.save(conn, txUser);
            tx.commit();
            log.info("JCA Transaction committed. Verify persisted record: {}", repo.findById(104L).isPresent());
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    private static void demoTransactionRollback(ConnectionFactory cf, UserJcaRepository repo) throws Exception {
        log.info("Starting JCA Local Transaction (ROLLEDBACK)...");
        Connection conn = cf.getConnection();
        LocalTransaction tx = conn.getLocalTransaction();
        try {
            tx.begin();
            User tempUser = new User(999L, "Temp User", "temp@rapidx.com", "Testing", 50000.0);
            repo.save(conn, tempUser);
            log.info("Rolling back JCA Transaction...");
            tx.rollback();
            log.info("JCA Transaction rolled back. Verify record exists (expected false): {}", repo.findById(999L).isPresent());
        } finally {
            conn.close();
        }
    }
}
