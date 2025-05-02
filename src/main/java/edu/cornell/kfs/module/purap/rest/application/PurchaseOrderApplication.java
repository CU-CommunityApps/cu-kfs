package edu.cornell.kfs.module.purap.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.module.purap.rest.resource.PurchaseOrderResource;

@ApplicationPath("ws/purap/po")
public class PurchaseOrderApplication extends Application {
    
    private Set<Object> singletons = new HashSet<>();
    
    public PurchaseOrderApplication() {
        singletons.add(new PurchaseOrderResource());
    }
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
