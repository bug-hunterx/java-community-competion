package com.epam.coderunner;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CodeRunnerApplicationTests {

    @Autowired private WebTestClient webTestClient;
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
        final String taskJson = InternalUtils.toJson(readTask(1));

        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(taskJson))
                .exchange()
                .returnResult(String.class).getResponseBody();


        final String response = result.blockFirst();
        assertThat(response).isNotEmpty();
        final TestingStatus testingStatus = InternalUtils.fromJson(response, TestingStatus.class);

        assertThat(testingStatus.getTestsStatuses()).containsExactly(FAIL, PASS);
    }

    @Test
    public void noTask() throws Exception {
        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(InternalUtils.toJson(readTask(9999))))
                .exchange()
                .returnResult(String.class).getResponseBody();

        System.out.println(result.last().block());
        //assertThat(result.getResponse().getContentAsString()).contains("ERROR: No task");
    }

    private static TaskRequest readTask(final int taskId) throws IOException {
        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(1);
        taskRequest.setUserId("user2@epam.com");
        final String source =
                Resources.toString(Resources.getResource("Solution" + taskId + ".java"), StandardCharsets.UTF_8);
        taskRequest.setSource(source);
        return taskRequest;
    }
}
