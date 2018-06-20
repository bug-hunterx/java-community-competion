package com.epam.coderunner;

import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CodeRunnerApplicationTests {

    private final Gson gson = new Gson();

    @Autowired private MockMvc mockMvc;
    @Autowired private TasksStorage tasksStorage;

    @Before
    public void setup() {
        if (tasksStorage.getTask(1) == null) {
            final Map<String, String> inOut = ImmutableMap.of(
                    "1", "1",
                    "21", "21"
            );
            final Task task = new Task();
            task.setAcceptanceTests(inOut);
            tasksStorage.saveTask(1, task);
        }
    }

    @Test
    public void runTask() throws Exception {
        final MvcResult result = mockMvc.perform(
                post("/task/1")
                        .content(gson.toJson(readTask(1)))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        final long id = Long.valueOf(result.getResponse().getContentAsString());
        Awaitility.waitAtMost(3, TimeUnit.SECONDS)
                .pollDelay(200, TimeUnit.MILLISECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> tasksStorage.getTestStatus(id) != null);
        final TestingStatus testingStatus = tasksStorage.getTestStatus(id);
        assertThat(testingStatus.getTestsStatuses()).containsExactly(FAIL, PASS);
    }

    @Test
    public void noTask() throws Exception {
        final MvcResult result = mockMvc.perform(
                post("/task/9999")
                        .content(gson.toJson(readTask(9999)))
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("COMPILATION_ERROR: No task");
    }

    private static TaskRequest readTask(final int taskId) throws IOException {
        final TaskRequest taskRequest = new TaskRequest();
        final String source =
                Resources.toString(Resources.getResource("Solution" + taskId + ".java"), StandardCharsets.UTF_8);
        taskRequest.setSource(source);
        return taskRequest;
    }
}
