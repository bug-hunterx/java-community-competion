package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
final class JavaCodeRunner implements CodeRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);
    private static final AtomicLong classId = new AtomicLong(1000000);

    private final TasksStorage tasksStorage;
    private final int taskTimeoutSeconds;

    @Autowired
    JavaCodeRunner(final TasksStorage tasksStorage,
                   @Value("${task.timeout}") final int taskTimeoutSeconds) {
        this.tasksStorage = tasksStorage;
        this.taskTimeoutSeconds = taskTimeoutSeconds;
    }

    @Override
    public Mono<TestingStatus> run(final TaskRequest taskRequest) {
        final long taskId = taskRequest.getTaskId();
        final String userId = taskRequest.getUserId();
        final String sourceCode = taskRequest.getSource();
        final String className = "LoadedClass" + classId.getAndIncrement();
        LOG.debug("Begin to compile task source, taskId={}, userId={}, classId={}, source:\n{}",
                taskId, userId, classId.get(), sourceCode);
        try {
            final Function<String, String> function = RuntimeCodeCompiler.compile(className, sourceCode);
            final Task task = checkNotNull(tasksStorage.getTask(taskId), "No task for id=%s found", taskId);
            final Map<String, String> inputOutputs = task.getAcceptanceTests();
            LOG.debug("Source compiled, task fetched, schedule test..");
            return Mono.fromCallable(() -> SolutionChecker.checkSolution(inputOutputs, function))
                    .timeout(Duration.ofSeconds(taskTimeoutSeconds))
                    .doOnSuccess(r -> LOG.debug("Testing completed, taskId={}, userId={}, result:{}", taskId, userId, r.toJson()))
                    .doOnError(e -> LOG.debug("Testing failed, taskId={}, userId={}, error:", taskId, userId, e));
        } catch (Exception e) {
            LOG.error("Error while running task, taskId={}, userId={}, error:", taskId, userId, e);
            return Mono.just(new TestingStatus());
        }
    }
}
