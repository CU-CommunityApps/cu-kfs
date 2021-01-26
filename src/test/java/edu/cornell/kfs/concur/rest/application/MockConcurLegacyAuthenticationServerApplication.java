package edu.cornell.kfs.concur.rest.application;

import javax.ws.rs.ApplicationPath;

import edu.cornell.kfs.concur.rest.resource.MockConcurLegacyAuthenticationServerResource;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.rest.application.ApplicationWithSingletons;

@ApplicationPath(CUKFSConstants.SLASH)
public class MockConcurLegacyAuthenticationServerApplication extends ApplicationWithSingletons {

    public MockConcurLegacyAuthenticationServerApplication() {
        super(new MockConcurLegacyAuthenticationServerResource());
    }

}
