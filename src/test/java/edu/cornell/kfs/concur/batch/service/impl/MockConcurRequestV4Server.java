package edu.cornell.kfs.concur.batch.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.concur.ConcurConstants.ConcurApiParameters;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.batch.service.impl.fixture.RequestV4DetailFixture;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4ReportDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;

public class MockConcurRequestV4Server {

    private ConcurrentMap<String, RequestEntry> travelRequests;
    private ObjectMapper objectMapper;

    public MockConcurRequestV4Server() {
        this.travelRequests = new ConcurrentHashMap<>();
        this.objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    public void addTravelRequests(RequestV4DetailFixture... requestsToAdd) {
        for (RequestV4DetailFixture travelRequest : requestsToAdd) {
            travelRequests.put(travelRequest.id, new RequestEntry(travelRequest));
        }
    }

    public ConcurRequestV4ListingDTO findRequests(String queryUrl) {
        return null;
    }

    public ConcurRequestV4ListingDTO findRequests(Map<String, String> queryParameters) {
        String view = getExistingParameter(queryParameters, ConcurApiParameters.VIEW);
        int start = getExistingIntParameter(queryParameters, ConcurApiParameters.START);
        int limit = getExistingIntParameter(queryParameters, ConcurApiParameters.LIMIT);
        Date modifiedAfter = getExistingDateParameter(queryParameters, ConcurApiParameters.MODIFIED_AFTER);
        Date modifiedBefore = getExistingDateParameter(queryParameters, ConcurApiParameters.MODIFIED_BEFORE);
        Optional<String> userId = getOptionalParameter(queryParameters, ConcurApiParameters.USER_ID);
        
        //Stream<ConcurV4RequestFixture> results = travelRequests.stream()
                //;
        
        return null;
    }

    private Date getExistingDateParameter(Map<String, String> queryParameters, String key) {
        String value = getExistingParameter(queryParameters, key);
        try {
            return ConcurUtils.parseUTCDateToDateTime(value).toDate();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Query has badly formatted date parameter " + key, e);
        }
    }

    private int getExistingIntParameter(Map<String, String> queryParameters, String key) {
        String value = getExistingParameter(queryParameters, key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Query has badly formatted int parameter " + key, e);
        }
    }

    private String getExistingParameter(Map<String, String> queryParameters, String key) {
        String value = queryParameters.get(key);
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Query has blank or missing parameter: " + key);
        }
        return value;
    }

    private Optional<String> getOptionalParameter(Map<String, String> queryParameters, String key) {
        return Optional.ofNullable(queryParameters.get(key))
                .filter(StringUtils::isNotBlank);
    }

    private <T> T copyJsonDTO(T jsonObject, Class<T> objectClass) {
        try {
            String jsonString = objectMapper.writeValueAsString(jsonObject);
            return objectMapper.readValue(jsonString, objectClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unexpected exception encountered while copying JSON DTO", e);
        }
    }

    private static class RequestEntry {
        private final RequestV4DetailFixture requestFixture;
        private ConcurRequestV4ListItemDTO requestAsListItem;
        private ConcurRequestV4ReportDTO requestDetail;
        
        public RequestEntry(RequestV4DetailFixture requestFixture) {
            this.requestFixture = requestFixture;
            this.requestAsListItem = requestFixture.toConcurRequestV4ListItemDTO();
            this.requestDetail = requestFixture.toConcurRequestV4ReportDTO();
        }
    }

}
