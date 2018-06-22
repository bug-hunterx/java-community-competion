package com.epam.coderunner.runners;

import com.epam.coderunner.model.TestingStatus;
import reactor.core.publisher.Mono;

public interface CodeRunner {
    Mono<TestingStatus> run(final long taskId, final String sourceCode);
}
