package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.storage.TasksStorage;
import com.google.common.annotations.VisibleForTesting;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Component
public class JavaCodeRunner {

    @Autowired
    private TasksStorage tasksStorage;

    private Executor executor = Executors.newCachedThreadPool();

    private static final Logger LOG = LoggerFactory.getLogger(JavaCodeRunner.class);

    @SuppressWarnings("unchecked")
    public String runCode(String className, String source, Map<String, String> inputOutputs) {
        try {
            Object obj = Reflect.compile(className, source).create().get();
            LOG.debug("Source code has type of {}", obj.getClass());
            Function<String, String> function = (Function<String, String>)obj;
            String submissionId = "" + System.currentTimeMillis();

            LOG.debug("Checking code with submission id {}", submissionId);
            executor.execute(() -> {
                final TestingStatus testingStatus = SolutionChecker.checkSolution(inputOutputs, function, submissionId);
                tasksStorage.updateTestStatus(submissionId, testingStatus);
            });
            return submissionId;
        } catch(Exception e){
            LOG.error("Error while compiling: ", e);
            return "COMPILATION_ERROR: " + e.getMessage();
        }
    }

    @VisibleForTesting
    public void setTasksStorage(TasksStorage tasksStorage) {
        this.tasksStorage = tasksStorage;
    }
}
