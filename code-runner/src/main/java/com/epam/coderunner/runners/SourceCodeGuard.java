package com.epam.coderunner.runners;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

final class SourceCodeGuard {

    private static final List<String> keywordBlacklist = readBlacklist();

    static void check(final String source) {
        for (final String s : keywordBlacklist) {
            Preconditions.checkArgument(!source.contains(s), "Source code contains foul keyword: %s", s);
        }
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
