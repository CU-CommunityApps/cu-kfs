package edu.cornell.kfs.sys.rest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.rest.CuJaxrsTestConstants.TestAppConstants;
import edu.cornell.kfs.sys.rest.application.TestJaxrsApplication;
import liquibase.util.StringUtils;

public class ApacheHttpJerseyJUnit5Test extends ApacheHttpJerseyTestBase.ForJUnit5 {

    @Override
    protected Application configure() {
        return new TestJaxrsApplication();
    }

    @Test
    void testAutoPopulateOfApplicationVariable() throws Exception {
        assertTrue(application != null,
                "The variable storing the Application instance should have been auto-populated");
        
        Application actualApplication = application;
        if (application instanceof ResourceConfig) {
            actualApplication = ((ResourceConfig) application).getApplication();
        }
        assertTrue(TestJaxrsApplication.class.isInstance(actualApplication),
                "The potentially-wrapped Application instance did not have the expected type");
    }

    @Test
    void testUriConcatenationHelperMethods() throws Exception {
        String[][] uriSegmentArrays = {
                {},
                {"root"},
                {"items", "1234"},
                {"admin", "users", "555", "update"},
                {"items/1234"}
        };
        String baseUri = getBaseUri().toString();
        String baseRelativeUri = TestAppConstants.RESOURCE_RELATIVE_PATH;
        for (String[] uriSegments : uriSegmentArrays) {
            String expectedRelativeUri = baseRelativeUri;
            if (uriSegments.length > 0) {
                expectedRelativeUri += CUKFSConstants.SLASH + StringUtils.join(uriSegments, CUKFSConstants.SLASH);
            }
            String expectedAbsoluteUri = baseUri + CUKFSConstants.SLASH + expectedRelativeUri;
            assertEquals(expectedRelativeUri,
                    buildRelativeUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, uriSegments), "Wrong relative URI");
            assertEquals(expectedAbsoluteUri,
                    buildAbsoluteUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, uriSegments), "Wrong absolute URI");
        }
    }

    @Test
    void testHttpGetReturningEmptyResponse() throws Exception {
        String path = buildRelativePath();
        Response response = target(path).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Wrong HTTP response");
        assertEquals(0, response.getLength(), "Response content should have been empty (zero length)");
    }

    @Test
    void testHttpGetReturningTextResponse() throws Exception {
        String path = buildRelativePath(TestAppConstants.DESCRIPTION_SUB_PATH);
        Response response = target(path).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Wrong HTTP response");
        
        String responseContent = response.readEntity(String.class);
        assertEquals(TestAppConstants.DESCRIPTION_RESPONSE, responseContent, "Wrong response content");
    }

    @Test
    void testHttpGetReturningTextResponseAsString() throws Exception {
        String path = buildRelativePath(TestAppConstants.DESCRIPTION_SUB_PATH);
        String responseContent = target(path).request().get(String.class);
        assertEquals(TestAppConstants.DESCRIPTION_RESPONSE, responseContent, "Wrong response content");
    }

    private String buildRelativePath(String... segments) {
        return buildRelativeUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, segments);
    }

}
