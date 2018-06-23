package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkArgument;

@Service
final class TaskExecutorImpl implements TaskExecutor {

    private final int taskTimeoutMs;
    private final Scheduler scheduler = provideScheduler();

    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutMs) {
        this.taskTimeoutMs = taskTimeoutMs;
    }

    @Override
    public Mono<TestingStatus> submit(final Callable<TestingStatus> task) {
        return Mono.fromCallable(task)
                .publishOn(scheduler)
                .timeout(Duration.ofMillis(taskTimeoutMs))
                .onErrorResume(e -> Mono.just(TestingStatus.error(e)));
    }

    private static Scheduler provideScheduler() {
        final int threadCnt = Runtime.getRuntime().availableProcessors();
        checkArgument(threadCnt > 1, "This service cannot be running on single threaded platform");
        return Schedulers.newParallel("TaskScheduler", threadCnt - 1);
    }
}
