package edu.cornell.kfs.sys.web.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cornell.kfs.sys.web.service.CuApiJsonWebRequestReader;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CuApiJsonWebRequestReaderImpl implements CuApiJsonWebRequestReader {

	@Override
    public JsonNode getJsonContentFromServletRequest(HttpServletRequest request) throws BadRequestException, IOException {
        try (var requestInputStream = request.getInputStream();
             var streamReader = new InputStreamReader(requestInputStream, StandardCharsets.UTF_8)) {
            var objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(streamReader);
            if (jsonNode == null) {
                throw new BadRequestException("The request has no content in its JSON payload");
            } else if (!jsonNode.isObject()) {
                throw new BadRequestException("The request does not have a JSON object as the root node");
            }
            return jsonNode;
        } catch (JsonProcessingException e) {
            throw new BadRequestException("The request has malformed JSON content");
        }
    }
}
