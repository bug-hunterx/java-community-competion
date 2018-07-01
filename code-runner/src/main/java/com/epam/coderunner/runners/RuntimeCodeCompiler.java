package com.epam.coderunner.runners;

import com.epam.coderunner.model.SourceCode;
import com.google.common.reflect.TypeToken;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/** Unloading class seems unnecessary, see related pressure test. */
@Component
final class RuntimeCodeCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);
    private final TypeToken<Function<String, String>> type = new TypeToken<Function<String, String>>() {};

    @SuppressWarnings("unchecked")
    Supplier<Function<String, String>> compile(final SourceCode source) {
        try {
            final Class<?> clazz = Reflect.compile(source.getClassName(), source.getCode())
                    .create()
                    .type();
            checkArgument(type.isSupertypeOf(clazz),
                    "Source code type[%s] is not a sub type of %s", clazz, type);
            LOG.debug("Source code[{}] has type of {}", source.getClassName(), Arrays.toString(clazz.getInterfaces()));
            checkArgument(Modifier.isPublic(clazz.getModifiers()),
                    "Source code class[%s] is not public.", clazz);
            return () -> {
                try {
                    return (Function<String, String>) clazz.newInstance();
                } catch (final Exception e) {
                    LOG.error("Cannot create instance of function of type:{}, source:{}", type, source.getCode(), e);
                    throw new RuntimeException("Source type instance creation failed.", e);
                }
            };
        } catch (final Exception e) {
            LOG.error("Compile source code failed, error:", e);
            throw new RuntimeException("Compile source code failed.", e);
        }

    }
}
