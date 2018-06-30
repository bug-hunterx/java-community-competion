package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.google.common.annotations.VisibleForTesting;

public interface TaskStorage {

    Task getTask(final long taskId);

    @VisibleForTesting
    Task saveTask(final long taskId, final Task task);
}
