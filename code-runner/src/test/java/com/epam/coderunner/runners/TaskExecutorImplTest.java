package com.epam.coderunner.runners;

import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.TestingStatus;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskExecutorImplTest {

    private static final TaskExecutorImpl taskExecutor = new TaskExecutorImpl(1000);

    @AfterClass
    public static void dispose(){
        taskExecutor.dispose();
    }

    @Test
    public void passThrough() {
        final TestingStatus testingStatus = TestingStatus.builder()
                .addStatus(Status.FAIL)
                .setCurrentFailedInputIfAbsent("someInput").build();
        final Callable<TestingStatus> task = () -> testingStatus;
        final TestingStatus result = taskExecutor.submit(task).block(Duration.ofSeconds(1));
        assertThat(result).isEqualTo(testingStatus);
    }

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void timeoutTaskShouldBeCanceled(){
        final Callable<TestingStatus> task = () -> {
            while (true) {}
        };

        thrown.expectMessage("Timeout");
        taskExecutor.submit(task).block(Duration.ofSeconds(1));
    }
}
