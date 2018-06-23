package com.epam.coderunner.runners;

import com.epam.coderunner.TestData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RuntimeCodeCompilerTest {

    private final SourceCodeGuard sourceCodeGuard = mock(SourceCodeGuard.class);
    private final RuntimeCodeCompiler codeCompiler = new RuntimeCodeCompiler(sourceCodeGuard);

    @Before
    public void setup(){
        when(sourceCodeGuard.check(anyString()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(sourceCodeGuard.renameClass(anyString(), anyString()))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void badSourceCode() {
        thrown.expect(org.joor.ReflectException.class);
        codeCompiler.compile("asdf", "bullshit");
    }

    @Test
    public void classLoadingPressureTesting(){
        final String source = TestData.readTaskFromResources(1).getSource();

        for (int i = 0; i < 100000; i++) {
           final Function<String, String> function = codeCompiler.compile("Solution1", source);
           function.apply("someInput");
        }
    }
}