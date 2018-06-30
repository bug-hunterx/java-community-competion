package com.epam.coderunner.runners;

import com.epam.coderunner.model.*;
import com.epam.coderunner.storage.TaskStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
final class JavaCodeRunner implements CodeRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);

    private final TaskExecutor taskExecutor;
    private final TaskStorage taskStorage;
    private final SourceCodeGuard sourceCodeGuard;
    private final RuntimeCodeCompiler codeCompiler;

    @Autowired
    JavaCodeRunner(final TaskExecutor taskExecutor,
                   final TaskStorage taskStorage,
                   final SourceCodeGuard sourceCodeGuard,
                   final RuntimeCodeCompiler codeCompiler) {
        this.taskExecutor = taskExecutor;
        this.taskStorage = taskStorage;
        this.sourceCodeGuard = sourceCodeGuard;
        this.codeCompiler = codeCompiler;
    }

    @Override
    public Mono<TestingStatus> run(final TaskRequest taskRequest) {
        final long taskId = taskRequest.getTaskId();
        final String userId = taskRequest.getUserId();
        final String sourceCode = taskRequest.getSource();
        final String signature = taskRequest.signature();
        LOG.debug("{}Begin to compile task source, source:\n{}", signature, sourceCode);
        final CompiledTask compiledTask;
        try {
            final Task task = checkNotNull(taskStorage.getTask(taskId), "No task[id:%s] found", taskId);
            final SourceCode checkedSource = sourceCodeGuard.proguard(sourceCode);
            final Supplier<Function<String, String>> functionSupplier = codeCompiler.compile(checkedSource);
            compiledTask = new CompiledTask(userId, taskId, task.getAcceptanceTests(), functionSupplier);
        } catch (final Exception e) {
            LOG.error("{}Error while preparing task:", signature, e);
            return Mono.just(TestingStatus.error(e));
        }
        LOG.debug("{}SourceCode compiled, task fetched, start scheduling tests..", signature);

        return taskExecutor.submit(() -> SolutionChecker.checkSolution(compiledTask))
                .doOnSuccess(r -> LOG.debug("{}Testing completed, result:{}", signature, r.toJson()))
                .doOnError(e -> LOG.debug("{}Testing failed, error:", signature, e))
                .onErrorResume(e -> Mono.just(TestingStatus.error(e)));
    }
}
