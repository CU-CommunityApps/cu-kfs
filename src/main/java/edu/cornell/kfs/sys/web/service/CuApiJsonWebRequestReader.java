package edu.cornell.kfs.sys.web.service;

import com.fasterxml.jackson.databind.JsonNode;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import java.io.IOException;

public interface CuApiJsonWebRequestReader {

    public JsonNode getJsonContentFromServletRequest(HttpServletRequest request) throws BadRequestException, IOException;

}
