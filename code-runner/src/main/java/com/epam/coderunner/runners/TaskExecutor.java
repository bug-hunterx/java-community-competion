package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

interface TaskExecutor {
    Mono<TestingStatus> submit(final Callable<TestingStatus> task);
}
