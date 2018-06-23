package com.epam.coderunner.runners;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        sourceCodeGuard.check(source);
    }

    @Test
    public void classNameDoesNotConform(){
        final String source = "public class BadName{}";
        thrown.expectMessage("Class name");
        sourceCodeGuard.check(source);
    }

    @Test
    public void classNameShouldBeReplaced(){
        final String source = new StringBuilder()
                .append("class Solution1{").append(NEW_LINE)
                .append("  void test(int n){").append(NEW_LINE)
                .append("    System.out.println(n);").append(NEW_LINE)
                .append("  }").append(NEW_LINE)
                .append("}").append(NEW_LINE)
                .toString();
        final String indexedSource = sourceCodeGuard.renameClass(source, "SomeName");
        System.out.println(indexedSource);
    }
}