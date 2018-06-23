package com.epam.coderunner.runners;

import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Unloading class seems unnecessary, see related pressure test. */
@Component
final class RuntimeCodeCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);

    private final SourceCodeGuard codeGuard;

    @Autowired
    RuntimeCodeCompiler(final SourceCodeGuard codeGuard) {
        this.codeGuard = codeGuard;
    }

    @SuppressWarnings("unchecked")
    <T> T compile(final String className, final String source){
        final String checkedSource = codeGuard.check(source);
        final Object obj = Reflect.compile(className, codeGuard.renameClass(checkedSource, className)).create().get();
        LOG.trace("Source code has type of {}", obj.getClass());
        return (T) obj;
    }
}
