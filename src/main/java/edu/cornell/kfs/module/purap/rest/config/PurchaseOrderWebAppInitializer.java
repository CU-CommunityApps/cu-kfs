package edu.cornell.kfs.module.purap.rest.config;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import edu.cornell.kfs.module.purap.rest.application.PurchaseOrderApplication;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

public class PurchaseOrderWebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        // Create the Spring application context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(PurchaseOrderApplication.class);
        
        // Register the context loader listener
        servletContext.addListener(new ContextLoaderListener(rootContext));

        // Create and register the DispatcherServlet
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(PurchaseOrderApplication.class);
        DispatcherServlet servlet = new DispatcherServlet(dispatcherContext);
        ServletRegistration.Dynamic registration = servletContext.addServlet("purchaseOrderDispatcher", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/ws/purap/po/*");
        
        // Register the authentication filter using DelegatingFilterProxy
        FilterRegistration.Dynamic authFilter = servletContext.addFilter("purchaseOrderAuthFilter", 
                new DelegatingFilterProxy("purchaseOrderAuthFilter"));
        authFilter.addMappingForUrlPatterns(null, false, "/ws/purap/po/api/*");
    }
}
