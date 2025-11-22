package cue.edu.co.parcial.repository;

import cue.edu.co.parcial.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
    }

    @Test
    void saveUser_Success() {
        User savedUser = userRepository.save(testUser);

        assertNotNull(savedUser.getId());
        assertEquals(testUser.getName(), savedUser.getName());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
    }

    @Test
    void findByEmail_Success() {
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertTrue(found.isPresent());
        assertEquals(testUser.getName(), found.get().getName());
        assertEquals(testUser.getEmail(), found.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        userRepository.save(testUser);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_ReturnsFalse() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertFalse(exists);
    }

    @Test
    void deleteUser_Success() {
        User savedUser = userRepository.save(testUser);
        Long userId = savedUser.getId();

        userRepository.deleteById(userId);

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void findAll_ReturnsMultipleUsers() {
        User user2 = new User();
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        userRepository.save(testUser);
        userRepository.save(user2);

        assertEquals(2, userRepository.findAll().size());
    }
}
