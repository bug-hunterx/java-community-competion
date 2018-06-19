package com.epam.coderunner.runners;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;

public final class SourceCodeGuardTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void check() {
        final String source = new StringBuilder()
                .append("void test(){")
                .append("System.exit(0);")
                .append("}").toString();
        thrown.expectMessage("keyword");
        SourceCodeGuard.check(source);
    }
}