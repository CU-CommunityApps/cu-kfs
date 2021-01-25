package edu.cornell.kfs.sys.rest;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class CuJaxrsTestConstants {

    public static final class TestAppConstants {
        public static final String APPLICATION_PATH = "testserver";
        public static final String RESOURCE_RELATIVE_PATH = "testapi";
        public static final String RESOURCE_FULL_PATH =
                APPLICATION_PATH + CUKFSConstants.SLASH + RESOURCE_RELATIVE_PATH;
        public static final String DESCRIPTION_SUB_PATH = "description";
        public static final String DESCRIPTION_RESPONSE = "This is a resource for testing a JAX-RS test server";
    }

}
