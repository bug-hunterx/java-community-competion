package com.epam.coderunner.runners;

public interface CodeRunner {
    String run(final long taskId, final String sourceCode);
}
