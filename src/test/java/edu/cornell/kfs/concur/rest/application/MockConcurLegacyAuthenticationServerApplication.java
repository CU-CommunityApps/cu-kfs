package edu.cornell.kfs.concur.rest.application;

import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.ApplicationPath;

import edu.cornell.kfs.concur.ConcurTestConstants.MockLegacyAuthConstants;
import edu.cornell.kfs.concur.rest.resource.MockConcurLegacyAuthenticationServerResource;
import edu.cornell.kfs.sys.rest.application.ApplicationWithSingletons;

@ApplicationPath(MockLegacyAuthConstants.BASE_PATH)
public class MockConcurLegacyAuthenticationServerApplication extends ApplicationWithSingletons {

    public MockConcurLegacyAuthenticationServerApplication() {
        super(buildLegacyAuthResource());
    }

    private static MockConcurLegacyAuthenticationServerResource buildLegacyAuthResource() {
        MockConcurLegacyAuthenticationServerResource resource = new MockConcurLegacyAuthenticationServerResource();
        resource.setCurrentTokens(new ConcurrentHashMap<>());
        resource.setDateTimeFormatter(null);
        return resource;
    }

}
