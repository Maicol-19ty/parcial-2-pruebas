package cue.edu.co.parcial.service;

import cue.edu.co.parcial.dto.TaskDTO;
import cue.edu.co.parcial.exception.ResourceNotFoundException;
import cue.edu.co.parcial.model.Task;
import cue.edu.co.parcial.model.User;
import cue.edu.co.parcial.repository.TaskRepository;
import cue.edu.co.parcial.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;
    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setIsCompleted(false);
        testTask.setUser(testUser);

        testTaskDTO = new TaskDTO();
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setIsCompleted(false);
        testTaskDTO.setUserId(1L);
    }

    @Test
    void createTask_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.createTask(testTaskDTO);

        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
        assertEquals(testTask.getDescription(), result.getDescription());
        assertEquals(testTask.getIsCompleted(), result.getIsCompleted());

        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(testTaskDTO));

        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getTaskById_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void getAllTasks_Success() {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setIsCompleted(true);
        task2.setUser(testUser);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2));

        List<TaskDTO> results = taskService.getAllTasks();

        assertNotNull(results);
        assertEquals(2, results.size());

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void getTasksByUserId_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByUserId(1L)).thenReturn(Collections.singletonList(testTask));

        List<TaskDTO> results = taskService.getTasksByUserId(1L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testTask.getTitle(), results.getFirst().getTitle());

        verify(userRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getTasksByUserId_UserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByUserId(1L));

        verify(userRepository, times(1)).existsById(1L);
        verify(taskRepository, never()).findByUserId(1L);
    }

    @Test
    void updateTask_Success() {
        TaskDTO updateDTO = new TaskDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Description");
        updateDTO.setIsCompleted(true);
        updateDTO.setUserId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.updateTask(1L, updateDTO);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskDTO result = taskService.updateTaskStatus(1L, true);

        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_NotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(1L));

        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, never()).deleteById(1L);
    }
}
