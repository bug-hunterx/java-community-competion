package com.epam.coderunner.model;

public final class TaskRequest implements RequestSignature{
    private long taskId;
    private String userId;
    private String source;

    public long getTaskId() {
        return taskId;
    }
    public void setTaskId(final long taskId) {
        this.taskId = taskId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(final String userId) {
        this.userId = userId;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
}
