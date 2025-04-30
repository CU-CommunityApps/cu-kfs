package edu.cornell.kfs.module.purap.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.module.purap.rest.resource.PurchaseOrderResource;

@ApplicationPath("ws/purapp/po")
public class PurchasOrderApplication extends Application {
    
    private Set<Object> singletons = new HashSet<>();
    
    public PurchasOrderApplication() {
        singletons.add(new PurchaseOrderResource());
    }
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
