package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.Callable;

@Service
final class TaskExecutorImpl implements TaskExecutor {

    private final int taskTimeoutMs;

    TaskExecutorImpl(@Value("${task.timeout}") final int taskTimeoutMs) {
        this.taskTimeoutMs = taskTimeoutMs;
    }

    @Override
    public Mono<TestingStatus> submit(final Callable<TestingStatus> task) {
        return Mono.fromCallable(task)
                .publishOn(Schedulers.single())
                .timeout(Duration.ofMillis(taskTimeoutMs));
    }

    @PreDestroy
    void dispose(){
        Schedulers.single().dispose();
    }
}
