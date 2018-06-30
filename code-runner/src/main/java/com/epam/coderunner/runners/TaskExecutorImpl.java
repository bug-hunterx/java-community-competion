package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@Service
final class TaskExecutorImpl implements TaskExecutor {

    private final int taskTimeoutMs;
    private final Supplier<Scheduler> schedulerSupplier;

    @Autowired
    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutMs) {
        this.taskTimeoutMs = taskTimeoutMs;
        this.schedulerSupplier = () -> Schedulers.newSingle("task-exec");
    }

    @VisibleForTesting
    TaskExecutorImpl(final Supplier<Scheduler> schedulerSupplier){
        this.taskTimeoutMs = 100;
        this.schedulerSupplier = schedulerSupplier;
    }

    @Override
    public Mono<TestingStatus> submit(final Callable<TestingStatus> task) {
        final Scheduler scheduler = schedulerSupplier.get();
        return Mono.fromCallable(task)
                .publishOn(scheduler).doFinally(st -> scheduler.dispose())
                .timeout(Duration.ofMillis(taskTimeoutMs));
    }
}
