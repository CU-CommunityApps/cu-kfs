package edu.cornell.kfs.sys.rest.application;

import javax.ws.rs.ApplicationPath;

import edu.cornell.kfs.sys.rest.CuJaxrsTestConstants.TestAppConstants;
import edu.cornell.kfs.sys.rest.resource.TestJaxrsResource;

@ApplicationPath(TestAppConstants.APPLICATION_PATH)
public class TestJaxrsApplication extends ApplicationWithSingletons {

    public TestJaxrsApplication() {
        super(new TestJaxrsResource());
    }

}
