package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.google.common.annotations.VisibleForTesting;

public interface TasksStorage {

    Task getTask(final long taskId);
    void updateTestStatus(final long submissionId, final TestingStatus testingStatus);

    @VisibleForTesting
    void saveTask(final long taskId, final Task task);
    @VisibleForTesting
    TestingStatus getTestStatus(final long submissionId);
//    @VisibleForTesting
//    void clearTestStatus(final long submissionId);
}
