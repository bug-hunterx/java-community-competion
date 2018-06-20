package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;

public interface TasksStorage {

    Task getTask(final long taskId);
    void updateTestStatus(String submissionId, TestingStatus testingStatus);

    static TasksStorage redisTasksStorage(final String redisHost){
        return new RedisTasksStorage(redisHost);
    }
}
