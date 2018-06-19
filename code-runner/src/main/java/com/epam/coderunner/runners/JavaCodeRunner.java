package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.util.concurrent.MoreExecutors;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component
public class JavaCodeRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);

    private final TasksStorage tasksStorage;
    private final TaskExecutor taskExecutor;

    @Autowired
    JavaCodeRunner(final TasksStorage tasksStorage, final TaskExecutor taskExecutor) {
        this.tasksStorage = tasksStorage;
        this.taskExecutor = taskExecutor;
    }

    @SuppressWarnings("unchecked")
    public String runCode(String className, String source, Map<String, String> inputOutputs) {
        try {
            SourceCodeGuard.check(source);
            Object obj = Reflect.compile(className, source).create().get();
            LOG.debug("Source code has type of {}", obj.getClass());
            Function<String, String> function = (Function<String, String>)obj;
            String submissionId = "" + System.currentTimeMillis();

            LOG.debug("Checking code with submission id {}", submissionId);
            taskExecutor.submit(() -> {
                final TestingStatus testingStatus = SolutionChecker.checkSolution(inputOutputs, function, submissionId);
                tasksStorage.updateTestStatus(submissionId, testingStatus);
            });
            return submissionId;
        } catch(Exception e){
            LOG.error("Error while compiling: ", e);
            return "COMPILATION_ERROR: " + e.getMessage();
        }
    }
}
