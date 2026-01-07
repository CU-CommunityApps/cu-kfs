package edu.cornell.kfs.core.framework.resourceloader;

import java.util.List;

import javax.servlet.ServletContext;

import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.kfs.core.framework.resourceloader.SpringResourceLoader;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * CU-specific SpringResourceLoader instance that is only meant for initializing Spring contexts
 * that are children of the main KFS Spring context.
 */
public class CuSpringResourceLoader extends SpringResourceLoader {

    public CuSpringResourceLoader(final List<String> fileLocs, final ServletContext servletContext) {
        super(fileLocs, servletContext);
    }

    @Override
    public void start() {
        if (!GlobalResourceLoader.isInitialized()) {
            throw new IllegalStateException("The main KFS Spring context has not been initialized yet");
        }
        super.start();
    }

    @Override
    protected void customizeNewContextPriorToLoading(final ConfigurableWebApplicationContext newContext) {
        newContext.setParent(GlobalResourceLoader.getContext());
    }

}
