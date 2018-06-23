package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TaskStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
final class JavaCodeRunner implements CodeRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);
    private static final AtomicLong classId = new AtomicLong(1000000);

    private final TaskExecutor taskExecutor;
    private final TaskStorage taskStorage;

    @Autowired
    JavaCodeRunner(final TaskExecutor taskExecutor,
                   final TaskStorage taskStorage) {
        this.taskExecutor = taskExecutor;
        this.taskStorage = taskStorage;
    }

    @Override
    public Mono<TestingStatus> run(final TaskRequest taskRequest) {
        final long taskId = taskRequest.getTaskId();
        final String userId = taskRequest.getUserId();
        final String sourceCode = taskRequest.getSource();
        final String className = "LoadedClass" + classId.getAndIncrement();
        LOG.debug("Begin to compile task source, taskId={}, userId={}, classId={}, source:\n{}",
                taskId, userId, classId.get(), sourceCode);
        final Function<String, String> function;
        final Task task;
        try {
            function = RuntimeCodeCompiler.compile(className, sourceCode);
            task = checkNotNull(taskStorage.getTask(taskId), "No task[id:%s] found", taskId);
        } catch (final Exception e) {
            LOG.error("Error while preparing task, taskId={}, userId={}, error:", taskId, userId, e);
            return Mono.just(TestingStatus.error(e));
        }
        LOG.debug("Source compiled, task fetched, start scheduling tests..");
        return taskExecutor.submit(() -> SolutionChecker.checkSolution(task.getAcceptanceTests(), function))
                .doOnSuccess(r -> LOG.debug("Testing completed, taskId={}, userId={}, result:{}", taskId, userId, r.toJson()))
                .doOnError(e -> LOG.debug("Testing failed, taskId={}, userId={}, error:", taskId, userId, e));
    }
}
