package com.epam.coderunner.runners;

import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

final class SourceCodeGuard {

    private static final List<String> keywordBlacklist = readBlacklist();
    private static final Pattern classNamePattern = Pattern.compile("(?s)(.*class\\s+)Solution[0-9]+(\\b.*)");

    static String checkAndRename(final String source, final String className) {
        for (final String s : keywordBlacklist) {
            checkArgument(!source.contains(s), "Source code should not contain keyword: '%s'", s);
        }
        final Matcher classNameMatcher = classNamePattern.matcher(source);
        checkArgument(classNameMatcher.matches(),
                "Class name must be 'SolutionN', where 'N' is a one or more digits number");
        return classNameMatcher.replaceAll("$1" + className + "$2");
    }

    private static List<String> readBlacklist() {
        final Path path = Paths.get(Resources.getResource("source-keywords-blacklist").getPath());
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
