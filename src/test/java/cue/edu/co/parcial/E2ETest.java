package cue.edu.co.parcial;

import cue.edu.co.parcial.dto.TaskDTO;
import cue.edu.co.parcial.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * End-to-End test that validates the complete workflow:
 * 1. Create a user
 * 2. Create multiple tasks for that user
 * 3. List all tasks for the user
 * 4. Update task status
 * 5. Delete a task
 * 6. Verify final state
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class E2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setName("Alice Johnson");
        userDTO.setEmail("alice@example.com");
    }

    @Test
    void completeWorkflow_CreateUserAndManageTasks() throws Exception {
        // Step 1: Create a user
        MvcResult userResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andReturn();

        // Extract user ID from response
        String userResponse = userResult.getResponse().getContentAsString();
        UserDTO createdUser = objectMapper.readValue(userResponse, UserDTO.class);
        Long userId = createdUser.getId();

        // Step 2: Create first task for the user
        TaskDTO task1 = new TaskDTO();
        task1.setTitle("Complete project documentation");
        task1.setDescription("Write comprehensive docs for the API");
        task1.setIsCompleted(false);
        task1.setUserId(userId);

        MvcResult task1Result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Complete project documentation"))
                .andExpect(jsonPath("$.isCompleted").value(false))
                .andReturn();

        String task1Response = task1Result.getResponse().getContentAsString();
        TaskDTO createdTask1 = objectMapper.readValue(task1Response, TaskDTO.class);
        Long task1Id = createdTask1.getId();

        // Step 3: Create second task for the user
        TaskDTO task2 = new TaskDTO();
        task2.setTitle("Write unit tests");
        task2.setDescription("Cover all service methods");
        task2.setIsCompleted(false);
        task2.setUserId(userId);

        MvcResult task2Result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Write unit tests"))
                .andReturn();

        String task2Response = task2Result.getResponse().getContentAsString();
        TaskDTO createdTask2 = objectMapper.readValue(task2Response, TaskDTO.class);
        Long task2Id = createdTask2.getId();

        // Step 4: Create third task for the user
        TaskDTO task3 = new TaskDTO();
        task3.setTitle("Deploy to production");
        task3.setDescription("Deploy the application");
        task3.setIsCompleted(false);
        task3.setUserId(userId);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        // Step 5: List all tasks for the user
        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(
                        "Complete project documentation",
                        "Write unit tests",
                        "Deploy to production"
                )));

        // Step 6: Update status of first task to completed
        String statusUpdate = "{\"isCompleted\": true}";
        mockMvc.perform(patch("/api/tasks/" + task1Id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true));

        // Step 7: Verify the task was updated
        mockMvc.perform(get("/api/tasks/" + task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isCompleted").value(true));

        // Step 8: Update the second task
        task2.setTitle("Write unit and integration tests");
        task2.setDescription("Cover all layers");
        task2.setIsCompleted(true);

        mockMvc.perform(put("/api/tasks/" + task2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write unit and integration tests"))
                .andExpect(jsonPath("$.description").value("Cover all layers"))
                .andExpect(jsonPath("$.isCompleted").value(true));

        // Step 9: Delete the third task
        mockMvc.perform(delete("/api/tasks/" + task2Id))
                .andExpect(status().isNoContent());

        // Step 10: Verify only 2 tasks remain
        mockMvc.perform(get("/api/tasks/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Step 11: Get all tasks globally
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

        // Step 12: Delete the user (should cascade delete remaining tasks)
        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());

        // Step 13: Verify user is deleted
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidation_CreateUserWithInvalidEmail() throws Exception {
        UserDTO invalidUser = new UserDTO();
        invalidUser.setName("Bob");
        invalidUser.setEmail("invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testValidation_CreateTaskWithoutTitle() throws Exception {
        TaskDTO invalidTask = new TaskDTO();
        invalidTask.setDescription("No title task");
        invalidTask.setUserId(1L);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testNotFound_GetNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testNotFound_CreateTaskForNonExistentUser() throws Exception {
        TaskDTO task = new TaskDTO();
        task.setTitle("Orphan task");
        task.setDescription("This task has no user");
        task.setUserId(99999L);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isNotFound());
    }
}
