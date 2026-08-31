package com.rapidx.jca;

import com.rapidx.jca.db.spi.DatabaseManagedConnectionFactory;
import com.rapidx.jca.db.spi.DatabaseResourceAdapter;
import com.rapidx.jca.model.User;
import com.rapidx.jca.model.UserJcaRepository;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.cci.LocalTransaction;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JcaDatabaseAdapterTest {

    private DatabaseResourceAdapter resourceAdapter;
    private ConnectionFactory connectionFactory;
    private UserJcaRepository repository;

    @BeforeAll
    void setUp() throws Exception {
        resourceAdapter = new DatabaseResourceAdapter();
        resourceAdapter.start(null);

        DatabaseManagedConnectionFactory mcf = new DatabaseManagedConnectionFactory();
        mcf.setDatabaseUrl("jdbc:h2:mem:jcatest_" + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
        mcf.setUserName("sa");
        mcf.setPassword("");

        connectionFactory = (ConnectionFactory) mcf.createConnectionFactory();
        repository = new UserJcaRepository(connectionFactory);
        repository.createTable();
    }

    @AfterAll
    void tearDown() {
        if (resourceAdapter != null) {
            resourceAdapter.stop();
        }
    }

    @Test
    @DisplayName("Test Persistence and Retrieval of User via JCA Adapter")
    void testSaveAndFindById() throws Exception {
        User user = new User(1L, "John Doe", "john.doe@test.com", "Engineering", 95000.0);
        repository.save(user);

        Optional<User> retrieved = repository.findById(1L);
        assertTrue(retrieved.isPresent(), "User should be retrieved from DB via JCA");
        assertEquals("John Doe", retrieved.get().getName());
        assertEquals("john.doe@test.com", retrieved.get().getEmail());
        assertEquals("Engineering", retrieved.get().getDepartment());
        assertEquals(95000.0, retrieved.get().getSalary());
    }

    @Test
    @DisplayName("Test Retrieval of Multiple Users via JCA ListRecord")
    void testFindAll() throws Exception {
        User u1 = new User(2L, "User Two", "u2@test.com", "Sales", 60000.0);
        User u2 = new User(3L, "User Three", "u3@test.com", "Marketing", 70000.0);

        repository.save(u1);
        repository.save(u2);

        List<User> list = repository.findAll();
        assertTrue(list.size() >= 2, "List should contain persisted users");
    }

    @Test
    @DisplayName("Test Update User Record via JCA Adapter")
    void testUpdate() throws Exception {
        User user = new User(4L, "Original Name", "orig@test.com", "Dev", 80000.0);
        repository.save(user);

        user.setName("Updated Name");
        user.setSalary(90000.0);
        repository.update(user);

        Optional<User> updated = repository.findById(4L);
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals(90000.0, updated.get().getSalary());
    }

    @Test
    @DisplayName("Test Delete User Record via JCA Adapter")
    void testDelete() throws Exception {
        User user = new User(5L, "To Delete", "delete@test.com", "HR", 50000.0);
        repository.save(user);

        assertTrue(repository.findById(5L).isPresent());

        repository.deleteById(5L);
        assertFalse(repository.findById(5L).isPresent(), "Deleted user should no longer exist in DB");
    }

    @Test
    @DisplayName("Test JCA Local Transaction Rollback")
    void testTransactionRollback() throws Exception {
        Connection conn = connectionFactory.getConnection();
        LocalTransaction tx = conn.getLocalTransaction();

        tx.begin();
        User user = new User(6L, "Tx User", "tx@test.com", "QA", 75000.0);
        repository.save(conn, user);

        // Rollback transaction
        tx.rollback();
        conn.close();

        Optional<User> result = repository.findById(6L);
        assertFalse(result.isPresent(), "Rolled back transaction changes should not persist");
    }
}
