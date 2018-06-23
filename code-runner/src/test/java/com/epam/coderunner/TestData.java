package com.epam.coderunner;

import com.epam.coderunner.model.TaskRequest;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class TestData {
    private TestData(){}

    public static TaskRequest readTaskFromResources(final int taskId) {
        final TaskRequest taskRequest = new TaskRequest();
        taskRequest.setTaskId(taskId);
        taskRequest.setUserId("user2@epam.com");
        final String source;
        try {
            source = Resources.toString(Resources.getResource("Solution" + taskId + ".java"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        taskRequest.setSource(source);
        return taskRequest;
    }
}
