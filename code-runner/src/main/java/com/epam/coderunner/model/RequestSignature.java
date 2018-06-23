package com.epam.coderunner.model;

public interface RequestSignature {
    String getUserId();
    long getTaskId();

    default String signature(){
        return String.format("[%s/%s]", getUserId(), getTaskId());
    }
}
