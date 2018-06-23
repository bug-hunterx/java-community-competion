package com.epam.coderunner;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TaskStorage;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.AfterClass;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.epam.coderunner.model.Status.FAIL;
import static com.epam.coderunner.model.Status.PASS;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class CodeRunnerApplicationTests {
    private static final Logger LOG = LoggerFactory.getLogger(CodeRunnerApplicationTests.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(8);

    @Autowired private WebTestClient webTestClient;
    @Autowired private TaskStorage taskStorage;

    @AfterClass
    public static void shutsown() {
        MoreExecutors.shutdownAndAwaitTermination(executor, 2, TimeUnit.SECONDS);
    }

    @Before
    public void setup() {
        for (int i = 0; i < 5; i++) {
            if (taskStorage.getTask(i) == null) {
                final Map<String, String> inOut = ImmutableMap.of(
                        "1", "1",
                        "21", "21"
                );
                final Task task = new Task();
                task.setAcceptanceTests(inOut);
                taskStorage.saveTask(i, task);
            }
        }
    }

    @Test
    public void runTask() {
        final String taskJson = InternalUtils.toJson(readTask(1));
        LOG.info("Request specs:{}", taskJson);
        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(taskJson))
                .exchange()
                .returnResult(String.class).getResponseBody();

        final String response = result.blockFirst(Duration.ofSeconds(2));
        assertThat(response).isNotEmpty();
        LOG.info("Response specs:{}", response);
        final TestingStatus testingStatus = TestingStatus.fromJson(response);

        assertThat(testingStatus.getTestsStatuses()).containsExactly(FAIL, PASS);
    }

    @Test
    public void noTask() {
        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(InternalUtils.toJson(readTask(9999))))
                .exchange()
                .returnResult(String.class).getResponseBody();
        final String response = result.blockFirst(Duration.ofSeconds(2));
        LOG.info("Response specs:{}", response);
        final TestingStatus testingStatus = InternalUtils.fromJson(response, TestingStatus.class);
        assertThat(testingStatus.getErrorType()).isEqualTo("java.lang.NullPointerException");
        assertThat(testingStatus.getErrorMsg()).contains("No task");
    }

    @Test
    public void longRunningTaskPressureTesting() {
        final List<Future<Flux<String>>> futures = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            futures.add(
                    executor.submit(() -> webTestClient
                            .post().uri("task/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromObject(InternalUtils.toJson(readTask(2))))
                            .exchange()
                            .returnResult(String.class).getResponseBody()
                    )
            );
        }
        LOG.debug("All requests fired!");
        futures.forEach(f -> {
            try {
                final String response = f.get().blockFirst(Duration.ofSeconds(3));
                final TestingStatus testingStatus = TestingStatus.fromJson(response);
                assertThat(testingStatus.getErrorType()).isEqualTo(TimeoutException.class.getName());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static TaskRequest readTask(final int taskId) {
        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(taskId);
        taskRequest.setUserId("user2@epam.com");
        final String source;
        try {
            source = Resources.toString(Resources.getResource("Solution" + taskId + ".java"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        taskRequest.setSource(source);
        return taskRequest;
    }
}
