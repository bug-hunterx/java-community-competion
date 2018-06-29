package com.epam.coderunner.runners;

import com.epam.coderunner.TestData;
import com.epam.coderunner.model.SourceCode;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Function;

public final class RuntimeCodeCompilerTest {

    private final RuntimeCodeCompiler codeCompiler = new RuntimeCodeCompiler();

    @Before
    public void setup() {

    }

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void compileWithPackage() {
        final String source = TestData.readTaskFromResources(3).getSource();
        codeCompiler.compile(new SourceCode("com.github.someone.Solution3", source));
    }

    @Test
    public void badSourceCode() {
        thrown.expectCause(Matchers.any(org.joor.ReflectException.class));
        codeCompiler.compile(new SourceCode("SomeClass", "bullshit"));
    }

    @Test
    public void classLoadingPressureTesting() {
        final String source = TestData.readTaskFromResources(1).getSource();

        for (int i = 0; i < 100000; i++) {
            final SourceCode sourceCode = new SourceCode("Solution1", source);
            final Function<String, String> function = codeCompiler.compile(sourceCode).get();
            function.apply("1324");
        }
    }

    @Test
    public void sourceCodeWithWrongType(){
        final String source = new StringBuilder()
                .append("public class BadType implements java.util.function.Supplier<String>{")
                .append("  public String get(){")
                .append("    return \"someValue\";")
                .append("  }")
                .append("}").toString();
        thrown.expectCause(Matchers.any(IllegalArgumentException.class));
        codeCompiler.compile(new SourceCode("BadType", source));
    }

    @Test
    public void classMustBePublic(){ //class.newInstance() is called
        final String source = new StringBuilder()
                .append("class NonPublicClazz implements java.util.function.Function<String, String>{")
                .append("public String apply(String input){return input;}")
                .append("}").toString();
        thrown.expectCause(new BaseMatcher<Throwable>() {

            @Override public void describeTo(final Description description) {}
            @Override public boolean matches(final Object item) {
                final IllegalArgumentException exception = (IllegalArgumentException) item;
                return exception.getMessage().contains("is not public");
            }
        });
        codeCompiler.compile(new SourceCode("NonPublicClazz", source));
    }
}
