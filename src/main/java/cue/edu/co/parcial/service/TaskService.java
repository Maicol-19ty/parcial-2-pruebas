package cue.edu.co.parcial.service;

import cue.edu.co.parcial.dto.TaskDTO;
import cue.edu.co.parcial.exception.ResourceNotFoundException;
import cue.edu.co.parcial.model.Task;
import cue.edu.co.parcial.model.User;
import cue.edu.co.parcial.repository.TaskRepository;
import cue.edu.co.parcial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO) {
        User user = userRepository.findById(taskDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", taskDTO.getUserId()));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setIsCompleted(taskDTO.getIsCompleted() != null ? taskDTO.getIsCompleted() : false);
        task.setUser(user);

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return convertToDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return taskRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        if (taskDTO.getIsCompleted() != null) {
            task.setIsCompleted(taskDTO.getIsCompleted());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, Boolean isCompleted) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.setIsCompleted(isCompleted);
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", "id", id);
        }
        taskRepository.deleteById(id);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setIsCompleted(task.getIsCompleted());
        dto.setUserId(task.getUser().getId());
        return dto;
    }
}
