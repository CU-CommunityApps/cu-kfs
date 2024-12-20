package edu.cornell.kfs.concur.rest.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.concur.rest.resource.ConcurAIResource;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("ws/concur/ai")
public class ConcurAIApplication extends Application {
    
    private Set<Object> singletons = new HashSet<>();
    
    public ConcurAIApplication() {
        singletons.add(new ConcurAIResource());
    }
    
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
