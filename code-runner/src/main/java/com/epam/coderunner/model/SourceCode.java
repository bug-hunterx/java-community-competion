package com.epam.coderunner.model;

public final class SourceCode {
    private final String className;
    private final String code;

    public SourceCode(final String className, final String code) {
        this.className = className;
        this.code = code;
    }

    public String getClassName() {
        return className;
    }
    public String getCode() {
        return code;
    }
}
