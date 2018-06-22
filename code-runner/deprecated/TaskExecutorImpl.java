package com.epam.coderunner.runners;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
final class TaskExecutorImpl implements TaskExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutorImpl.class);

    private final ListeningExecutorService taskExecutor = provideExecutorService();
    private final ScheduledExecutorService watcherExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Queue<TimedTask> tasks = new ConcurrentLinkedQueue<>();
    /** Only eventual consistency is required. */
    private final AtomicInteger taskCnt = new AtomicInteger();
    private final long taskTimeout;

    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutSeconds) {
        this.taskTimeout = taskTimeoutSeconds * 1000;
        watcherExecutor.scheduleAtFixedRate(this::terminateTimeoutTasks, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public ListenableFuture<?> submit(final Runnable task) {
        final ListenableFuture<?> submittedTask = taskExecutor.submit(task);
        tasks.add(new TimedTask(submittedTask));
        taskCnt.incrementAndGet();
        LOG.debug("Task submitted, watched task cnt={}", taskCnt.get());
        return submittedTask;
    }

    @PreDestroy
    private void shutdown(){
        MoreExecutors.shutdownAndAwaitTermination(taskExecutor, 5, TimeUnit.SECONDS);
        MoreExecutors.shutdownAndAwaitTermination(watcherExecutor, 5, TimeUnit.SECONDS);
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
        final int currentWatchedCnt = taskCnt.get();
        LOG.debug("{} timeout tasks terminated, {} watched tasks left.(Count may Not be precise)", cnt - currentWatchedCnt, currentWatchedCnt);
    }

    private static ListeningExecutorService provideExecutorService() {
        final int threadCnt = Runtime.getRuntime().availableProcessors();
        Preconditions.checkArgument(threadCnt > 1, "This service cannot be running on single threaded env");
        return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(threadCnt - 1));
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
