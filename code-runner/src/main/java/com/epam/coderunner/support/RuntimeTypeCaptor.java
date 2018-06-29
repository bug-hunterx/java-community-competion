package com.epam.coderunner.support;

import com.google.common.reflect.TypeToken;

/**
 * Extends this class with reified type parameter to capture the type for subclass to use.<br>
 * Note: It will not work, if one extends this class still with generic type.
 *
 * @param <T> type parameter to capture
 */
public abstract class RuntimeTypeCaptor<T> {
    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) {};

    protected final TypeToken<T> getGenericType() {
        return typeToken;
    }
}
