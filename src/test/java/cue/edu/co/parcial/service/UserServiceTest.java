package cue.edu.co.parcial.service;

import cue.edu.co.parcial.dto.UserDTO;
import cue.edu.co.parcial.exception.ResourceNotFoundException;
import cue.edu.co.parcial.model.User;
import cue.edu.co.parcial.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testUserDTO = new UserDTO();
        testUserDTO.setName("John Doe");
        testUserDTO.setEmail("john@example.com");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.createUser(testUserDTO);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail(testUserDTO.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUserDTO));

        verify(userRepository, times(1)).existsByEmail(testUserDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getAllUsers_Success() {
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserDTO> results = userService.getAllUsers();

        assertNotNull(results);
        assertEquals(2, results.size());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_Success() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setName("John Updated");
        updateDTO.setEmail("john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, testUserDTO));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, never()).deleteById(1L);
    }
}
