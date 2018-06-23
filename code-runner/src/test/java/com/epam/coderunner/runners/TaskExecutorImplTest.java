package com.epam.coderunner.runners;

import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.TestingStatus;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskExecutorImplTest {

    private final TaskExecutor taskExecutor = new TaskExecutorImpl(1);

    @Test
    public void passThrough() {
        final TestingStatus testingStatus = TestingStatus.builder()
                .addStatus(Status.FAIL)
                .setCurrentFailedInput("someInput").build();
        final Callable<TestingStatus> task = () -> testingStatus;
        final TestingStatus result = taskExecutor.submit(task).block(Duration.ofSeconds(1));
        assertThat(result).isEqualTo(testingStatus);
    }

    @Test
    public void timeoutTaskShouldBeCanceled(){
        final Callable<TestingStatus> task = () -> {
            while (true) {}
        };

        final TestingStatus result = taskExecutor.submit(task).block(Duration.ofSeconds(1));
        assertThat(result).isNotNull();
        assertThat(result.getErrorType()).isEqualTo(TimeoutException.class.getName());
    }
}