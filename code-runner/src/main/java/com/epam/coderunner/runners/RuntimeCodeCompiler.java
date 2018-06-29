package com.epam.coderunner.runners;

import com.epam.coderunner.model.SourceCode;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Supplier;

/** Unloading class seems unnecessary, see related pressure test. */
@Component
final class RuntimeCodeCompiler<T> {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);



    @SuppressWarnings("unchecked")
    Supplier<T> compile(final SourceCode source) {
        try {
            final Class<?> type = Reflect.compile(source.getClassName(), source.getCode())
                    .create()
                    .type();
            //checkArgument(type.equals(), "Source code super type[{}] is not a Function", type.getSuperclass()); todo
            LOG.debug("SourceCode code has type of {}", Arrays.toString(type.getInterfaces()));
            return () -> {
                try {
                    return (T) type.newInstance();
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
