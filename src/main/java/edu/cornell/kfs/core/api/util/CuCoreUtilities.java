package edu.cornell.kfs.core.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.kuali.kfs.core.api.util.ClassLoaderUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

/*
 * CU customization: This class preserves the getResourceAsStream method from base code CoreUtilities. 
 * CoreUtilities has been updated with FINP-8446 from the 5/18/22 release, to remove unused methods but we are still using the getResourceAsStream method.
 */
public final class CuCoreUtilities {
    
    private CuCoreUtilities() {
        throw new UnsupportedOperationException("do not call");
    }
    
    /**
     * @param resourceLoc resource location; syntax supported by {@link DefaultResourceLoader}
     * @return a handle to the Resource
     */
    private static Resource getResource(String resourceLoc) {
        AbsoluteFileSystemResourceLoader resourceLoader = new AbsoluteFileSystemResourceLoader();
        resourceLoader.setClassLoader(ClassLoaderUtils.getDefaultClassLoader());
        return resourceLoader.getResource(resourceLoc);
    }
    
    /**
     * Attempts to retrieve the resource stream.
     *
     * @param resourceLoc resource location; syntax supported by {@link DefaultResourceLoader}
     * @return the resource stream or null if the resource could not be obtained
     * @throws MalformedURLException
     * @throws IOException
     * @see DefaultResourceLoader
     */
    public static InputStream getResourceAsStream(String resourceLoc) throws MalformedURLException, IOException {
        Resource resource = getResource(resourceLoc);
        if (resource.exists()) {
            return resource.getInputStream();
        }
        return null;
    }
    
    /**
     * The standard Spring FileSystemResourceLoader does not support normal absolute file paths for historical
     * backwards-compatibility reasons. This class simply circumvents that behavior to allow proper interpretation of
     * absolute paths (i.e. not stripping a leading slash)
     */
    private static class AbsoluteFileSystemResourceLoader extends FileSystemResourceLoader {
        @Override
        protected Resource getResourceByPath(String path) {
            return new FileSystemResource(path);
        }
    }

}
