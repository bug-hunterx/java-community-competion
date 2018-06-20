package com.epam.coderunner.runners;

import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.TestingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

final class SolutionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(SolutionChecker.class);

    private SolutionChecker(){}

    static TestingStatus checkSolution(final Map<String, String> inputOutputs,
                                       final Function<String, String> function,
                                       final long submissionId) {
        final TestingStatus testingStatus = new TestingStatus();
        try {
            boolean allTestsPassed = true;
            for (Map.Entry<String, String> entry : inputOutputs.entrySet()) {
                final String input = entry.getKey();
                final String expected = entry.getValue();
                final String actual = function.apply(input);
                if (!actual.equals(expected)) {
                    testingStatus.addStatus(Status.FAIL);
                    allTestsPassed = false;
                    LOG.info("Submission id {} failed on test [{}]. Expected: [{}], actual: [{}]", submissionId, input, expected, actual);
                } else {
                    testingStatus.addStatus(Status.PASS);
                }
            }
            testingStatus.setAllTestsDone(true);
            testingStatus.setAllTestsPassed(allTestsPassed);
            LOG.info("Submission id is checked. All tests passed: {}", allTestsPassed);
        } catch (Throwable th){
            LOG.error("Error while checking submission {}", submissionId, th);
        }
        return testingStatus;
    }
}
