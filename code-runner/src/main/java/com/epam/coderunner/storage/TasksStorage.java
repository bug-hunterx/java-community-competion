package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.google.common.annotations.VisibleForTesting;

public interface TasksStorage {

    Task getTask(final long taskId);

    @VisibleForTesting
    void saveTask(final long taskId, final Task task);
}
