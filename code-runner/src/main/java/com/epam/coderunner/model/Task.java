package com.epam.coderunner.model;

import java.util.Map;

public final class Task {

    private String taskTitle;
    private String taskDescription;
    private Map<String, String> testExamples;
    private Map<String, String> acceptanceTests;

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Map<String, String> getTestExamples() {
        return testExamples;
    }

    public void setTestExamples(Map<String, String> testExamples) {
        this.testExamples = testExamples;
    }

    public Map<String, String> getAcceptanceTests() {
        return acceptanceTests;
    }

    public void setAcceptanceTests(Map<String, String> acceptanceTests) {
        this.acceptanceTests = acceptanceTests;
    }
}
