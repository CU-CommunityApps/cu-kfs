package edu.cornell.kfs.module.purap.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.cornell.kfs.module.purap.rest.resource.PaymentRequestResource;

@ApplicationPath("ws/purap/paymentrequest")
public class PaymentRequestApplication extends Application {
    private Set<Object> singletons = new HashSet<>();

    public PaymentRequestApplication() {
        singletons.add(new PaymentRequestResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
