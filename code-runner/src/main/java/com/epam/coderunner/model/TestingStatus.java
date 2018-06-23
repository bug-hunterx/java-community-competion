package com.epam.coderunner.model;

import com.epam.coderunner.InternalUtils;
import com.google.common.base.Throwables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public final class TestingStatus {

    private final Collection<Status> testsStatuses;
    private final boolean allTestsDone;
    private final boolean allTestsPassed;
    private final String currentFailedInput;
    private final String errorType;
    private final String errorMsg;

    TestingStatus(final Collection<Status> testsStatuses,
                  final boolean allTestsDone,
                  final boolean allTestsPassed,
                  final String currentFailedInput,
                  final String errorType,
                  final String errorMsg) {
        this.testsStatuses = testsStatuses;
        this.allTestsDone = allTestsDone;
        this.allTestsPassed = allTestsPassed;
        this.currentFailedInput = currentFailedInput;
        this.errorType = errorType;
        this.errorMsg = errorMsg;
    }

    public static TestingStatusBuilder builder() {
        return new TestingStatusBuilder();
    }

    public static TestingStatus error(final Throwable e) {
        return new TestingStatusBuilder().setErrorInfo(e).build();
    }

    public Collection<Status> getTestsStatuses() {
        return testsStatuses;
    }
    public boolean isAllTestsDone() {
        return allTestsDone;
    }
    public boolean isAllTestsPassed() {
        return allTestsPassed;
    }
    public String getCurrentFailedInput() {
        return currentFailedInput;
    }
    public String getErrorType() {
        return errorType;
    }
    public String getErrorMsg() {
        return errorMsg;
    }

    public String toJson() {
        return InternalUtils.toJson(this);
    }
    public static TestingStatus fromJson(final String json){
        return InternalUtils.fromJson(json, TestingStatus.class);
    }
}
