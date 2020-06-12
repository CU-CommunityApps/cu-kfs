package edu.cornell.kfs.module.cam.rest.application;

import edu.cornell.kfs.module.cam.rest.resource.CuCapAssetApiResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("ws/capasset")
public class CuCapAssetApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();

    public CuCapAssetApiApplication() {
        singletons.add(new CuCapAssetApiResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
