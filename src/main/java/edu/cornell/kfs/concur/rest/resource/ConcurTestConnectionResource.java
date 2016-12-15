package edu.cornell.kfs.concur.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/testconnection")
public class ConcurTestConnectionResource {

    @GET
    public Response testConnection() {
        return Response.ok().build();
    }
}
