package com.epam.coderunner.runners;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

public final class RuntimeCodeCompilerTest {

    private final SourceCodeGuard sourceCodeGuard = mock(SourceCodeGuard.class);
    private final RuntimeCodeCompiler codeCompiler = new RuntimeCodeCompiler(sourceCodeGuard);



    @Test
    public void badSourceCode() {
        codeCompiler.compile("asdf", "bullshit");
    }

    //todo: class loading & unloading
}