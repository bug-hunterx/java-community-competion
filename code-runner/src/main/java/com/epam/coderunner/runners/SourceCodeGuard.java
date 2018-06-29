package com.epam.coderunner.runners;

import com.epam.coderunner.model.SourceCode;
import com.google.common.io.Resources;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

@Component
class SourceCodeGuard {
    private static final List<String> keywordBlacklist = readBlacklist();
    private static final Pattern classNamePattern = Pattern.compile("(?s)(.*class\\s+)Solution[\\d]+(\\b.*)");
    private static final Pattern packageNamePattern = Pattern.compile("(?s)\\s*package\\s+[\\w.]+;(.*)");
    private static final AtomicLong classId = new AtomicLong(1000000);

    SourceCode proguard(final String source) {
        for (final String s : keywordBlacklist) {
            checkArgument(!source.contains(s), "SourceCode code should not contain keyword: '%s'", s);
        }
        final Matcher classNameMatcher = classNamePattern.matcher(source);
        checkArgument(classNameMatcher.matches(),
                "Class name must be 'SolutionN', where 'N' is a one or more digits number");
        final String className = "LoadedClass" + classId.getAndIncrement();
        final String renamedSource = classNameMatcher.replaceAll("$1" + className + "$2");
        final String packageRemovedSource = packageNamePattern.matcher(renamedSource).replaceAll("$1");
        return new SourceCode(className, packageRemovedSource);
    }

    private static List<String> readBlacklist() {
        try {
            final Path path = Paths.get(Resources.getResource("source-keywords-blacklist").toURI());
            return Files.readAllLines(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
