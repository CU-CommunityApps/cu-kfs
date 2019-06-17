package edu.cornell.kfs.module.purap.rest.application;

import edu.cornell.kfs.module.purap.rest.resource.EinvoiceApiResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("einvoice")
public class PurapApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();

    public PurapApiApplication() {
        singletons.add(new EinvoiceApiResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
