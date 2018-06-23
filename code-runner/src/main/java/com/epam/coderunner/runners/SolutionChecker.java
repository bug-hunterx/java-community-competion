package com.epam.coderunner.runners;

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

    static TestingStatus checkSolution(final Map<String, String> inputOutputs,
                                       final Function<String, String> function) {
        final TestingStatusBuilder testingStatusBuilder = TestingStatus.builder();
        try {
            boolean allTestsPassed = true;
            for (Map.Entry<String, String> entry : inputOutputs.entrySet()) {
                final String input = entry.getKey();
                final String expected = entry.getValue();
                final String actual = function.apply(input);
                if (!actual.equals(expected)) {
                    testingStatusBuilder.addStatus(Status.FAIL);
                    allTestsPassed = false;
                    LOG.info("Failed on test [{}]. Expected: [{}], actual: [{}]", input, expected, actual);
                } else {
                    testingStatusBuilder.addStatus(Status.PASS);
                }
            }
            testingStatusBuilder.setAllTestsDone(true);
            testingStatusBuilder.setAllTestsPassed(allTestsPassed);
            LOG.info("Submission id is checked. All tests passed: {}", allTestsPassed);
        } catch (Throwable th){
            LOG.error("Error while checking", th);
        }
        return testingStatusBuilder.build();
    }
}
