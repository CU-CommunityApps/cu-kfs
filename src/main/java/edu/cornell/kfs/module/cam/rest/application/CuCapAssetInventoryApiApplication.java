package edu.cornell.kfs.module.cam.rest.application;

import edu.cornell.kfs.module.cam.rest.resource.CuCapAssetInventoryApiResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("ws/capasset/inventory")
public class CuCapAssetInventoryApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();

    public CuCapAssetInventoryApiApplication() {
        singletons.add(new CuCapAssetInventoryApiResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
