package com.epam.coderunner.runners;

import com.epam.coderunner.model.CompiledTask;
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
    private final RuntimeCodeCompiler codeCompiler;

    @Autowired
    JavaCodeRunner(final TaskExecutor taskExecutor,
                   final TaskStorage taskStorage,
                   final RuntimeCodeCompiler codeCompiler) {
        this.taskExecutor = taskExecutor;
        this.taskStorage = taskStorage;
        this.codeCompiler = codeCompiler;
    }

    @Override
    public Mono<TestingStatus> run(final TaskRequest taskRequest) {
        final long taskId = taskRequest.getTaskId();
        final String userId = taskRequest.getUserId();
        final String sourceCode = taskRequest.getSource();
        final String className = "LoadedClass" + classId.getAndIncrement();
        final String signature = taskRequest.signature();
        LOG.debug("{}Begin to compile task source, classId={}, source:\n{}", signature, classId.get(), sourceCode);
        final CompiledTask compiledTask;
        try {
            final Function<String, String> function = codeCompiler.compile(className, sourceCode);
            final Task task = checkNotNull(taskStorage.getTask(taskId), "No task[id:%s] found", taskId);
            compiledTask = new CompiledTask(userId, taskId, task.getAcceptanceTests(), function);
        } catch (final Exception e) {
            LOG.error("{}Error while preparing task:", signature, e);
            return Mono.just(TestingStatus.error(e));
        }
        LOG.debug("{}Source compiled, task fetched, start scheduling tests..", signature);

        return taskExecutor.submit(() -> SolutionChecker.checkSolution(compiledTask))
                .doOnSuccess(r -> LOG.debug("{}Testing completed, result:{}", signature, r.toJson()))
                .doOnError(e -> LOG.debug("{}Testing failed, error:", signature, e))
                .onErrorResume(e -> Mono.just(TestingStatus.error(e)));
    }
}
