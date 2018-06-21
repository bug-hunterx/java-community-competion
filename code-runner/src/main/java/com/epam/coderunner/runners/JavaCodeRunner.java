package com.epam.coderunner.runners;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
final class JavaCodeRunner implements CodeRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);

    private final TasksStorage tasksStorage;
    private final TaskExecutor taskExecutor;
    private final AtomicLong classId = new AtomicLong(1000000);

    @Autowired
    JavaCodeRunner(final TasksStorage tasksStorage, final TaskExecutor taskExecutor) {
        this.tasksStorage = tasksStorage;
        this.taskExecutor = taskExecutor;
    }

    @Override public String run(final long taskId, final String sourceCode) {
        final String className = "LoadedClass" + classId.getAndIncrement();
        try {
            final Function<String, String> function = RuntimeCodeCompiler.compile(className, sourceCode);
            final long submissionId = System.currentTimeMillis();
            final Task task = checkNotNull(tasksStorage.getTask(taskId), "No task for id=%s found", taskId);
            final Map<String, String> inputOutputs = task.getAcceptanceTests();
            LOG.debug("Checking code with submission id {}", submissionId);
            final ListenableFuture<?> taskFuture = taskExecutor.submit(() -> {
                final TestingStatus testingStatus = SolutionChecker.checkSolution(inputOutputs, function, submissionId);
                tasksStorage.updateTestStatus(submissionId, testingStatus);
            });
            taskFuture.addListener(() -> RuntimeCodeCompiler.disposeClass(className), MoreExecutors.directExecutor());
            return String.valueOf(submissionId);
        } catch (Exception e) {
            LOG.error("Error while compiling: ", e);
            return "COMPILATION_ERROR: " + e.getMessage();
        }
    }
}
