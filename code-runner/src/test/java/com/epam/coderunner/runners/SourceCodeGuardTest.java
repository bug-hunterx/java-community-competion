package com.epam.coderunner.runners;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public final class SourceCodeGuardTest {

    private static final String NEW_LINE = System.lineSeparator();
    private final SourceCodeGuard sourceCodeGuard = new SourceCodeGuard();
    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void sourceContainsBlacklistedKeyword() {
        final String source = new StringBuilder()
                .append("void test(){")
                .append("System.exit(0);")
                .append("}").toString();
        thrown.expectMessage("keyword");
        sourceCodeGuard.proguard(source);
    }

    @Test
    public void classNameDoesNotConform(){
        final String source = "public class BadName{}";
        thrown.expectMessage("Class name");
        sourceCodeGuard.proguard(source);
    }

    @Test
    public void contentProgarded(){
        final String source = new StringBuilder()
                .append("package com.github.someone;").append(NEW_LINE)
                .append("class Solution1{").append(NEW_LINE)
                .append("  void test(int n){").append(NEW_LINE)
                .append("    System.out.println(n);").append(NEW_LINE)
                .append("  }").append(NEW_LINE)
                .append("}").append(NEW_LINE)
                .toString();
        final String indexedSource = sourceCodeGuard.proguard(source).getCode();
        assertThat(indexedSource).contains("LoadedClass");
        assertThat(indexedSource).doesNotContain("package");
    }
}
