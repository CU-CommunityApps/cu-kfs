package edu.cornell.kfs.sys.rest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.rest.CuJaxrsTestConstants.TestAppConstants;
import edu.cornell.kfs.sys.rest.application.TestJaxrsApplication;
import edu.cornell.kfs.sys.rest.resource.TestJaxrsResource;

@Execution(ExecutionMode.SAME_THREAD)
public class ApacheHttpJerseyExampleTest {

    private static ApacheHttpJerseyTestExtension jerseyExtension;

    @BeforeAll
    static void startUpTestApplication() throws Exception {
        jerseyExtension = new ApacheHttpJerseyTestExtension(new TestJaxrsApplication());
        jerseyExtension.startUp();
    }

    @AfterAll
    static void shutDownTestApplication() throws Exception {
        try {
            if (jerseyExtension != null) {
                jerseyExtension.close();
            }
        } finally {
            jerseyExtension = null;
        }
    }

    @Test
    void testRetrievalOfSingletonsFromApplication() throws Exception {
        TestJaxrsResource singleton = jerseyExtension.getSingletonFromApplication(TestJaxrsResource.class);
        assertNotNull(singleton, "The singleton retrieved from the JAX-RS Application should not have been null");
        assertThrows(NoSuchElementException.class,
                () -> jerseyExtension.getSingletonFromApplication(String.class),
                "An exception should have been thrown when the desired singleton could not be found");
    }

    static Stream<Arguments> uriSegmentGroupings() {
        String[][] uriSegmentGroupings = {
                {},
                {"root"},
                {"items", "1234"},
                {"admin", "users", "555", "update"},
                {"items/1234"}
        };
        return Arrays.stream(uriSegmentGroupings)
                .map(Object.class::cast)
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("uriSegmentGroupings")
    void testUriConcatenationHelperMethods(String[] uriSegments) throws Exception {
        String baseUri = jerseyExtension.getBaseUri().toString();
        String baseRelativeUri = TestAppConstants.RESOURCE_RELATIVE_PATH;
        
        String expectedRelativeUri = baseRelativeUri;
        if (uriSegments.length > 0) {
            expectedRelativeUri += CUKFSConstants.SLASH + StringUtils.join(uriSegments, CUKFSConstants.SLASH);
        }
        String expectedAbsoluteUri = baseUri + CUKFSConstants.SLASH + expectedRelativeUri;
        
        assertEquals(expectedRelativeUri,
                jerseyExtension.buildRelativeUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, uriSegments),
                "Wrong relative URI");
        assertEquals(expectedAbsoluteUri,
                jerseyExtension.buildAbsoluteUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, uriSegments),
                "Wrong absolute URI");
    }

    @Test
    void testHttpGetRequestReturningEmptyResponse() throws Exception {
        String path = buildRelativePath();
        Response response = jerseyExtension.target(path).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Wrong HTTP response");
        assertEquals(0, response.getLength(), "Response content should have been empty (zero length)");
    }

    @Test
    void testHttpGetRequestReturningTextResponse() throws Exception {
        String path = buildRelativePath(TestAppConstants.DESCRIPTION_SUB_PATH);
        Response response = jerseyExtension.target(path).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Wrong HTTP response");
        
        String responseContent = response.readEntity(String.class);
        assertEquals(TestAppConstants.DESCRIPTION_RESPONSE, responseContent, "Wrong response content");
    }

    @Test
    void testHttpGetRequestReturningTextResponseAsString() throws Exception {
        String path = buildRelativePath(TestAppConstants.DESCRIPTION_SUB_PATH);
        String responseContent = jerseyExtension.target(path).request().get(String.class);
        assertEquals(TestAppConstants.DESCRIPTION_RESPONSE, responseContent, "Wrong response content");
    }

    @Test
    void testHttpPostRequestReturningEmptyResponse() throws Exception {
        String path = buildRelativePath(TestAppConstants.POST_PATH);
        Entity<String> emptyEntity = Entity.text(StringUtils.EMPTY);
        Response response = jerseyExtension.target(path).request().post(emptyEntity);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Wrong HTTP response");
        assertEquals(0, response.getLength(), "Response content should have been empty (zero length)");
    }

    @Test
    void testHttpGetRequestReturningBadRequestError() throws Exception {
        String path = buildRelativePath(TestAppConstants.BAD_REQUEST_PATH);
        Response response = jerseyExtension.target(path).request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), "Wrong HTTP response");
    }

    @Test
    void testHttpGetRequestAgainstUnmappedPath() throws Exception {
        String path = buildRelativePath(TestAppConstants.UNMAPPED_PATH);
        Response response = jerseyExtension.target(path).request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), "Wrong HTTP response");
    }

    private String buildRelativePath(String... segments) {
        return jerseyExtension.buildRelativeUriPath(TestAppConstants.RESOURCE_RELATIVE_PATH, segments);
    }

}
