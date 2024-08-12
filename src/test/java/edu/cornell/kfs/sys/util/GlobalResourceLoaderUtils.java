package edu.cornell.kfs.sys.util;

import java.util.function.Supplier;

import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.krad.util.ResourceLoaderUtil;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public final class GlobalResourceLoaderUtils {

    /**
     * Convenience method that temporarily forces any calls to the GlobalResourceLoader.getResource(String) method
     * to instead call the ResourceLoaderUtil.getFileResource(String) method. In order for the static mocking to work,
     * the logic that calls the getResource() method must be wrapped by the given Supplier and must not branch off
     * into a separate thread.
     */
    public static <T> T handleTaskCallingGetResource(final Supplier<T> task) {
        try (
                final MockedStatic<GlobalResourceLoader> mockLoader = Mockito.mockStatic(GlobalResourceLoader.class)
        ) {
            mockLoader.when(() -> GlobalResourceLoader.getResource(Mockito.anyString()))
                    .then(invocation -> ResourceLoaderUtil.getFileResource(invocation.getArgument(0)));
            return task.get();
        }
    }

}
