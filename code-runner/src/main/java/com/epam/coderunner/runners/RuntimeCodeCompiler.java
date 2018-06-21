package com.epam.coderunner.runners;

import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class RuntimeCodeCompiler {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);

    @SuppressWarnings("unchecked")
    static <T> T compile(final String className, final String source){
        final Object obj = Reflect.compile(className, SourceCodeGuard.checkAndRename(source, className)).create().get();
        LOG.debug("Source code has type of {}", obj.getClass());
        return (T) obj;
    }

    static void disposeClass(final String className){
        //todo: unload class
    }
}
