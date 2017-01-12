package edu.cornell.kfs.concur.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.concur.rest.resource.ConcurTestConnectionResource;

@ApplicationPath("/ws/systems/v1.0")
public class ConcurTestConnectionApplication extends Application {

    protected Set<Object> singletons = new HashSet<>();
    protected Set<Class<?>> clazzes = new HashSet<>();

    public ConcurTestConnectionApplication() {
        singletons.add(new ConcurTestConnectionResource());
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
