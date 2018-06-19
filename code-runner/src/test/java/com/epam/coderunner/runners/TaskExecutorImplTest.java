package com.epam.coderunner.runners;

import org.awaitility.Awaitility;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

public final class TaskExecutorImplTest {

    private final int TASK_TIMEOUT_SECONDS = 1;

    private final TaskExecutorImpl taskExecutor = new TaskExecutorImpl(TASK_TIMEOUT_SECONDS);

    private final AtomicBoolean task1Marker = new AtomicBoolean();
    private final Runnable task1 = () -> task1Marker.set(true);

    private final AtomicBoolean task2Marker = new AtomicBoolean();
    private final Runnable task2 = () -> {
        while (true) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                task2Marker.set(true);
            }
        }
    };

    @Test
    public void submit() {
        taskExecutor.submit(task1);
        taskExecutor.submit(task2);
        Awaitility.waitAtMost(TASK_TIMEOUT_SECONDS * 3, TimeUnit.SECONDS).until(() ->
                task1Marker.get() && task2Marker.get()
        );

        assertThat(taskExecutor.getTasks()).isEmpty();
    }

}