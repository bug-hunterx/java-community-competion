package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Callable;

@Service
final class TaskExecutorImpl implements TaskExecutor {

    private final int taskTimeoutMs;
    private final Scheduler scheduler = Schedulers.newElastic("task-exec");
    @Autowired
    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutMs) {
        this.taskTimeoutMs = taskTimeoutMs;
    }

    @PreDestroy
    private void shutdown(){
        scheduler.dispose();
    }

    @Override
    public Mono<TestingStatus> submit(final Callable<TestingStatus> task) {
        return Mono.fromCallable(task)
                .publishOn(scheduler)
                .timeout(Duration.ofMillis(taskTimeoutMs));
    }
}
