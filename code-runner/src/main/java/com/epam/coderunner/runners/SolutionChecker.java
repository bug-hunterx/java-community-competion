package com.epam.coderunner.runners;

import com.epam.coderunner.model.CompiledTask;
import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.model.TestingStatusBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

final class SolutionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(SolutionChecker.class);

    private SolutionChecker(){}

    static TestingStatus checkSolution(final CompiledTask compiledTask) {
        final Map<String, String> inputOutputs = compiledTask.getInputOutputs();
        final Function<String, String> function = compiledTask.getFunction();

        final TestingStatusBuilder testingStatusBuilder = TestingStatus.builder();
        try {
            boolean allTestsPassed = true;
            for (final Map.Entry<String, String> entry : inputOutputs.entrySet()) {
                final String input = entry.getKey();
                final String expected = entry.getValue();
                final String actual = function.apply(input);
                if (!actual.equals(expected)) {
                    testingStatusBuilder.addStatus(Status.FAIL).setCurrentFailedInputIfAbsent(input);
                    allTestsPassed = false;
                    LOG.trace("{}Failed on test [{}]. Expected: [{}], actual: [{}]", compiledTask.signature(), input, expected, actual);
                } else {
                    testingStatusBuilder.addStatus(Status.PASS);
                }
            }
            testingStatusBuilder.setAllTestsDone(true).setAllTestsPassed(allTestsPassed);
            LOG.debug("{}Task checked. All tests passed: {}", compiledTask.signature(), allTestsPassed);
        } catch (final Throwable th){
            LOG.warn("{}Error while checking solution:", compiledTask.signature(), th);
        }
        return testingStatusBuilder.build();
    }
}
