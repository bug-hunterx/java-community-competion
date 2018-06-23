package com.epam.coderunner.model;

import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CompiledTask implements RequestSignature{
    private final String userId;
    private final long taskId;
    private final Map<String, String> inputOutputs;
    private final Function<String, String> function;

    public CompiledTask(final String userId,
                        final long taskId,
                        final Map<String, String> inputOutputs,
                        final Function<String, String> function) {
        this.userId = userId;
        this.taskId = taskId;
        this.inputOutputs = checkNotNull(inputOutputs);
        this.function = checkNotNull(function);
    }

    public String getUserId() {
        return userId;
    }
    public long getTaskId() {
        return taskId;
    }
    public Map<String, String> getInputOutputs() {
        return inputOutputs;
    }
    public Function<String, String> getFunction() {
        return function;
    }
}
