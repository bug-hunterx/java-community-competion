package com.epam.coderunner;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TaskStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.Duration;
import java.util.Map;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CodeRunnerApplicationTests {
    private static final Logger LOG = LoggerFactory.getLogger(CodeRunnerApplicationTests.class);

    @Autowired private WebTestClient webTestClient;
    @Autowired private TaskStorage taskStorage;

    @Before
    public void setup() {
        if (taskStorage.getTask(1) == null) {
            final Map<String, String> inOut = ImmutableMap.of(
                    "1", "1",
                    "21", "21"
            );
            final Task task = new Task();
            task.setAcceptanceTests(inOut);
            taskStorage.saveTask(1, task);
        }
    }

    @Test
    public void runTask() throws Exception {
        final String taskJson = InternalUtils.toJson(readTask(1));
        LOG.info("Request specs:{}", taskJson);
        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(taskJson))
                .exchange()
                .returnResult(String.class).getResponseBody();


        final String response = result.blockFirst();
        assertThat(response).isNotEmpty();
        LOG.info("Response specs:{}", response);
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
        final String response = result.last().block(Duration.ofSeconds(2));
        LOG.info("Response specs:{}", response);
        final TestingStatus testingStatus = InternalUtils.fromJson(response, TestingStatus.class);
        assertThat(testingStatus.getErrorType()).isEqualTo("java.lang.NullPointerException");
        assertThat(testingStatus.getErrorMsg()).contains("No task");
    }

    private static TaskRequest readTask(final int taskId) throws IOException {
        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(taskId);
        taskRequest.setUserId("user2@epam.com");
        final String source =
                Resources.toString(Resources.getResource("Solution" + taskId + ".java"), StandardCharsets.UTF_8);
        taskRequest.setSource(source);
        return taskRequest;
    }
}
