package com.epam.coderunner.runners;

import com.google.common.util.concurrent.ListenableFuture;

interface TaskExecutor {
    ListenableFuture<?> submit(final Runnable task);
}
