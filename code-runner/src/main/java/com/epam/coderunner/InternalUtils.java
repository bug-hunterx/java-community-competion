package com.epam.coderunner;

import com.google.gson.Gson;

public final class InternalUtils {
    private static final Gson gson = new Gson();

    private InternalUtils(){}

    public static <T> String toJson(final T obj){
        return gson.toJson(obj);
    }

    public static <T> T fromJson(final String json, final Class<T> type){
        return gson.fromJson(json, type);
    }
}
