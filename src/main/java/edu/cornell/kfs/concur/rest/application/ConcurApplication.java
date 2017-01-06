package edu.cornell.kfs.concur.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.concur.rest.resource.ConcurAccountValidationResource;

@ApplicationPath("/concur/v1.0")
public class ConcurApplication extends Application {

    protected Set<Object> singletons = new HashSet<>();
    protected Set<Class<?>> clazzes = new HashSet<>();

    public ConcurApplication() {
        singletons.add(new ConcurAccountValidationResource());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return clazzes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
