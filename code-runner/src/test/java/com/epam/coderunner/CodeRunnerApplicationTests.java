package com.epam.coderunner;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TaskStorage;
import com.epam.coderunner.support.InternalUtils;
import com.google.common.collect.ImmutableMap;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
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
            final Map<String, String> inOut = ImmutableMap.of(
                    "1,2,3", "6",
                    "21", "21",
                    "1,4", "3"
            );
            final Task task = new Task();
            task.setAcceptanceTests(inOut);
            taskStorage.saveTask(i, task);
        }
    }

    @Test
    public void runTask() {
        final String taskJson = InternalUtils.toJson(TestData.readTaskFromResources(1));
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

        assertThat(testingStatus.getTestsStatuses()).containsExactly(PASS, PASS, FAIL);
    }

    @Test
    public void noTask() {
        final Flux<String> result = webTestClient
                .post().uri("task/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(InternalUtils.toJson(TestData.readTaskFromResources(9999))))
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
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Map<Long, Future<String>> futures = new HashMap<>(20);
        for (int i = 0; i < 20; i++) {
            final TaskRequest request = TestData.readTaskFromResources(random.nextInt(1, 3));
            futures.put(
                    request.getTaskId(),
                    executor.submit(() -> webTestClient
                            .post().uri("task/submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromObject(InternalUtils.toJson(request)))
                            .exchange().expectStatus().is2xxSuccessful()
                            .expectBody(String.class).returnResult().getResponseBody()
                    )
            );
        }
        LOG.debug("All requests fired!");
        futures.forEach((index, future) -> {
            try {
                final String response = future.get();
                LOG.debug("Response:{}", response);
                final TestingStatus testingStatus = TestingStatus.fromJson(response);
                if (index == 2) {
                    assertThat(testingStatus.getErrorType()).isEqualTo(TimeoutException.class.getName());
                } else if (index == 1) {
                    assertThat(testingStatus.getErrorType()).isNullOrEmpty();
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
