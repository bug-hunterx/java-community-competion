package com.epam.coderunner.runners;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
final class TaskExecutorImpl implements TaskExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutorImpl.class);

    private final ExecutorService taskExecutor = provideExecutorService();
    private final ScheduledExecutorService watcherExecutor = Executors.newSingleThreadScheduledExecutor();
    //todo: shutdown executor services
    private final Queue<TimedTask> tasks = new ConcurrentLinkedQueue<>();
    /** Only eventual consistency is required. */
    private final AtomicInteger taskCnt = new AtomicInteger();
    private final long taskTimeout;

    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutSeconds) {
        this.taskTimeout = taskTimeoutSeconds * 1000;
        watcherExecutor.scheduleAtFixedRate(this::terminateTimeoutTasks, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void submit(final Runnable task) {
        tasks.add(new TimedTask(taskExecutor.submit(task)));
        taskCnt.incrementAndGet();
        LOG.debug("Task submitted, watched task cnt={}", taskCnt.get());
    }

    private void terminateTimeoutTasks() {
        final int cnt = taskCnt.get();
        for (int i = 0; i < cnt; i++) {
            final TimedTask timedTask = tasks.poll();
            if (timedTask != null && !timedTask.future.isDone() && !timedTask.future.isCancelled()) {
                if (timedTask.getAge() >= taskTimeout) {
                    timedTask.future.cancel(true);
                    taskCnt.decrementAndGet();
                } else {
                    tasks.offer(timedTask);
                }
            }
        }
    }

    private static ExecutorService provideExecutorService() {
        final int threadCnt = Runtime.getRuntime().availableProcessors();
        Preconditions.checkArgument(threadCnt > 1, "This service cannot be running on single threaded env");
        return Executors.newFixedThreadPool(threadCnt - 1);
    }

    private static final class TimedTask {
        private final long beginTimestmp;
        private final Future<?> future;

        private TimedTask(final Future<?> future) {
            this.future = future;
            this.beginTimestmp = System.currentTimeMillis();
        }

        long getAge() {
            return System.currentTimeMillis() - beginTimestmp;
        }
    }

    @VisibleForTesting
    Queue<TimedTask> getTasks() {
        return tasks;
    }
}
