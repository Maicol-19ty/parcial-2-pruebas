package cue.edu.co.parcial.repository;

import cue.edu.co.parcial.model.Task;
import cue.edu.co.parcial.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser = userRepository.save(testUser);

        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setIsCompleted(false);
        testTask.setUser(testUser);
    }

    @Test
    void saveTask_Success() {
        Task savedTask = taskRepository.save(testTask);

        assertNotNull(savedTask.getId());
        assertEquals(testTask.getTitle(), savedTask.getTitle());
        assertEquals(testTask.getDescription(), savedTask.getDescription());
        assertEquals(testTask.getIsCompleted(), savedTask.getIsCompleted());
        assertNotNull(savedTask.getUser());
    }

    @Test
    void findByUserId_ReturnsUserTasks() {
        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setIsCompleted(true);
        task2.setUser(testUser);

        taskRepository.save(testTask);
        taskRepository.save(task2);

        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        assertEquals(2, tasks.size());
    }

    @Test
    void findByUserId_ReturnsEmptyList() {
        List<Task> tasks = taskRepository.findByUserId(999L);

        assertTrue(tasks.isEmpty());
    }

    @Test
    void findByUserIdAndIsCompleted_ReturnsFilteredTasks() {
        Task completedTask = new Task();
        completedTask.setTitle("Completed Task");
        completedTask.setDescription("Completed");
        completedTask.setIsCompleted(true);
        completedTask.setUser(testUser);

        taskRepository.save(testTask); // isCompleted = false
        taskRepository.save(completedTask); // isCompleted = true

        List<Task> incompleteTasks = taskRepository.findByUserIdAndIsCompleted(testUser.getId(), false);
        List<Task> completedTasks = taskRepository.findByUserIdAndIsCompleted(testUser.getId(), true);

        assertEquals(1, incompleteTasks.size());
        assertFalse(incompleteTasks.get(0).getIsCompleted());

        assertEquals(1, completedTasks.size());
        assertTrue(completedTasks.get(0).getIsCompleted());
    }

    @Test
    void deleteTask_Success() {
        Task savedTask = taskRepository.save(testTask);
        Long taskId = savedTask.getId();

        taskRepository.deleteById(taskId);

        assertFalse(taskRepository.existsById(taskId));
    }

    @Test
    void cascadeDelete_TasksDeletedWhenUserDeleted() {
        Task savedTask = taskRepository.save(testTask);
        entityManager.flush();
        entityManager.clear();

        Long taskId = savedTask.getId();
        Long userId = testUser.getId();

        assertTrue(taskRepository.existsById(taskId));

        userRepository.deleteById(userId);
        entityManager.flush();

        assertFalse(taskRepository.existsById(taskId));
    }
}
