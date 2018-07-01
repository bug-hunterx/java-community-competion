package com.epam.coderunner.runners;

import com.epam.coderunner.model.Status;
import com.epam.coderunner.model.TestingStatus;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

public final class TaskExecutorImplTest {
    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutorImplTest.class);

    private final TaskExecutor taskExecutor = new TaskExecutorImpl(100);

    @After
    public void checkSchedulers(){
        System.out.println("------------------------------");
    }

    @Test
    public void passThrough() {
        final TestingStatus testingStatus = TestingStatus.builder()
                .addStatus(Status.FAIL)
                .setCurrentFailedInputIfAbsent("someInput").build();
        final Callable<TestingStatus> task = () -> {
            LOG.debug("Start to executing simple task...");
            return testingStatus;
        };
        final TestingStatus result = taskExecutor.submit(task).block(Duration.ofSeconds(1));
        assertThat(result).isEqualTo(testingStatus);
        LOG.debug("Simple task executed and returned with success.");
    }

    @Test
    public void timeoutTaskShouldHasItsWorkerDisposed() {
        final Callable<TestingStatus> badTask = () -> {
            LOG.debug("Start to executing infinite loop...");
            //noinspection InfiniteLoopStatement
            while (true) {}
        };
        try{
            taskExecutor.submit(badTask).block(Duration.ofSeconds(1));
            throw new AssertionError("Test failed, no exception thrown.");
        }catch (final Exception e){
            assertThat(e).describedAs("Actual exception:%s", e).hasCauseInstanceOf(TimeoutException.class);
        }
        LOG.debug("After failed task, try to test another pass through task...");
        passThrough();
        LOG.debug("Subsequent task executed successfully.");
    }

    @Rule public ExpectedException thrown = ExpectedException.none();
    @Test
    public void throwStackOverflowError() {
        final Callable<TestingStatus> task = this::infiniteRecursion;
        thrown.expect(StackOverflowError.class);
        taskExecutor.submit(task).block(Duration.ofSeconds(2));
    }
    private TestingStatus infiniteRecursion() {
        return infiniteRecursion();
    }

    @Test
    public void throwNoClassDefFoundError() {
        final Callable<TestingStatus> task = () -> {throw new NoClassDefFoundError();};
        thrown.expect(NoClassDefFoundError.class);
        taskExecutor.submit(task).block(Duration.ofSeconds(2));
    }
}
