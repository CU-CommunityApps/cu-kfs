package edu.cornell.kfs.sys.web.filter;

import org.kuali.kfs.core.api.resourceloader.GlobalResourceLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * Custom subclass of DelegatingFilterProxy that always uses the GlobalResourceLoader's application context.
 */
public class CuDelegatingFilterProxy extends DelegatingFilterProxy {

    @Override
    protected WebApplicationContext findWebApplicationContext() {
        return (WebApplicationContext) GlobalResourceLoader.getContext();
    }

}
