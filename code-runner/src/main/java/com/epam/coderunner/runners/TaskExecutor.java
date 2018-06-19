package com.epam.coderunner.runners;

interface TaskExecutor {
    void submit(final Runnable task);
}
