package com.github.someone;

import java.util.function.Function;

public class Solution3 implements Function<String, String> {

    @Override
    public String apply(String s) {
        return "with-package";
    }
}