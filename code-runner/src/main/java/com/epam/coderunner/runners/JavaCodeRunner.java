package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
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
    public Mono<TestingStatus> run(final long taskId, final String sourceCode) {
        final String className = "LoadedClass" + classId.getAndIncrement();
        try {
            final Function<String, String> function = RuntimeCodeCompiler.compile(className, sourceCode);
            final Task task = checkNotNull(tasksStorage.getTask(taskId), "No task for id=%s found", taskId);
            final Map<String, String> inputOutputs = task.getAcceptanceTests();

            return Mono.fromCallable(() -> SolutionChecker.checkSolution(inputOutputs, function))
                    .timeout(Duration.ofSeconds(taskTimeoutSeconds));
        } catch (Exception e) {
            LOG.error("Error while running task: ", e);
            return Mono.just(new TestingStatus());
        }
    }
}
