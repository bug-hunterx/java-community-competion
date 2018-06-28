package com.epam.coderunner.model;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CompiledTask implements RequestSignature{
    private final String userId;
    private final long taskId;
    private final Map<String, String> inputOutputs;
    private final Supplier<Function<String, String>> functionSupplier;

    public CompiledTask(final String userId,
                        final long taskId,
                        final Map<String, String> inputOutputs,
                        final Supplier<Function<String, String>> functionSupplier) {
        this.userId = userId;
        this.taskId = taskId;
        this.inputOutputs = checkNotNull(inputOutputs);
        this.functionSupplier = checkNotNull(functionSupplier);
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
        return functionSupplier.get();
    }
}
