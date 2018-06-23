package com.epam.coderunner.model;

import com.google.common.base.Throwables;

import java.util.ArrayList;
import java.util.Collection;

public class TestingStatusBuilder {
    private Collection<Status> testsStatuses = new ArrayList<>(20);
    private boolean allTestsDone;
    private boolean allTestsPassed;
    private String currentFailedInput;
    private String errorType;
    private String errorMsg;

    TestingStatusBuilder(){}

    public TestingStatusBuilder addStatus(Status status){
        testsStatuses.add(status);
        return this;
    }
    public TestingStatusBuilder setAllTestsDone(final boolean allTestsDone) {
        this.allTestsDone = allTestsDone;
        return this;
    }
    public TestingStatusBuilder setAllTestsPassed(final boolean allTestsPassed) {
        this.allTestsPassed = allTestsPassed;
        return this;
    }
    public TestingStatusBuilder setCurrentFailedInputIfAbsent(final String currentFailedInput) {
        if(this.currentFailedInput == null){
            this.currentFailedInput = currentFailedInput;
        }
        return this;
    }
    TestingStatusBuilder setErrorInfo(final Throwable exception) {
        final Throwable e = Throwables.getRootCause(exception);
        this.errorType = e.getClass().getName();
        this.errorMsg = e.getMessage();
        return this;
    }
    public TestingStatus build() {
        return new TestingStatus(testsStatuses, allTestsDone, allTestsPassed, currentFailedInput, errorType, errorMsg);
    }
}