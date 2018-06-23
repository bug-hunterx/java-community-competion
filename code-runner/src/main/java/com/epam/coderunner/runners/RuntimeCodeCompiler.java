package com.epam.coderunner.runners;

import com.epam.coderunner.model.SourceCode;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Unloading class seems unnecessary, see related pressure test. */
@Component
final class RuntimeCodeCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);

    @SuppressWarnings("unchecked")
    <T> T compile(final SourceCode source) {
        final Object obj = Reflect.compile(source.getClassName(), source.getCode()).create().get();
        LOG.trace("SourceCode code has type of {}", obj.getClass());
        return (T) obj;
    }
}
