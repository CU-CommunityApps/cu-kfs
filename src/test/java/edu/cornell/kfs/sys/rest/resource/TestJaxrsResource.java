package edu.cornell.kfs.sys.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import edu.cornell.kfs.sys.rest.CuJaxrsTestConstants.TestAppConstants;

@Path(TestAppConstants.RESOURCE_RELATIVE_PATH)
public class TestJaxrsResource {

    @GET
    public Response ping() {
        return Response.ok().build();
    }

    @GET
    @Path(TestAppConstants.DESCRIPTION_SUB_PATH)
    public Response describeTestResource() {
        return Response.ok(TestAppConstants.DESCRIPTION_RESPONSE).build();
    }

}
