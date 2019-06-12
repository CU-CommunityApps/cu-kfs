package edu.cornell.kfs.module.purap.rest.application;

import edu.cornell.kfs.module.purap.rest.resource.EinvoiceApiResource;
import org.kuali.kfs.sys.rest.application.BaseApiApplication;
import org.kuali.kfs.sys.rest.filter.AcceptHeaderContainerRequestFilter;
import org.kuali.kfs.sys.rest.resource.AuthenticationResource;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("purap/api/v1")
public class PurapApiApplication extends BaseApiApplication {

    public PurapApiApplication() {
        addSingleton(new AuthenticationResource());
        addSingleton(new EinvoiceApiResource());
        addClass(AcceptHeaderContainerRequestFilter.class);
    }
}
