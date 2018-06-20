package com.epam.coderunner.model;

import java.util.Collection;
import java.util.LinkedList;

public class TestingStatus {

    private Collection<Status> testsStatuses = new LinkedList<>();
    private boolean allTestsDone = false;
    private boolean allTestsPassed = false;

    public void addStatus(Status status){
        testsStatuses.add(status);
    }

    public void setAllTestsDone(boolean allTestsDone) {
        this.allTestsDone = allTestsDone;
    }

    public Collection<Status> getTestsStatuses() {
        return testsStatuses;
    }

    public void setTestsStatuses(Collection<Status> testsStatuses) {
        this.testsStatuses = testsStatuses;
    }

    public boolean isAllTestsDone() {
        return allTestsDone;
    }

    public boolean isAllTestsPassed() {
        return allTestsPassed;
    }

    public void setAllTestsPassed(boolean allTestsPassed) {
        this.allTestsPassed = allTestsPassed;
    }
}
