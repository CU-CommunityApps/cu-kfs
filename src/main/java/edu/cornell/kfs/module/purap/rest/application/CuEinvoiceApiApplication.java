package edu.cornell.kfs.module.purap.rest.application;

import edu.cornell.kfs.module.purap.rest.resource.CuEinvoiceApiResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("einvoice")
public class CuEinvoiceApiApplication extends Application {

    private Set<Object> singletons = new HashSet<>();

    public CuEinvoiceApiApplication() {
        singletons.add(new CuEinvoiceApiResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
