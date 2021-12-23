package edu.cornell.kfs.concur.batch.service.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.kuali.rice.core.api.config.property.ConfigContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurConstants.ConcurOAuth2;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurExpenseV3Service;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ReportItemDTO;
import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.sys.util.CURestClientUtils;

public class ConcurExpenseV3ServiceImpl implements ConcurExpenseV3Service {
    private static final Logger LOG = LogManager.getLogger();
    
    private ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void validateExpenseReports(String accessToken, List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        ConcurExpenseV3ListingDTO expenseList = getConcurStartingExpenseListing(accessToken);
        processExpenseListing(accessToken, expenseList, processingResults);

    }
    
    protected ConcurExpenseV3ListingDTO getConcurStartingExpenseListing(String accessToken) {
        return getConcurExpenseListing(accessToken, findDefaultExpenseListingEndPoint());
    }
    
    protected String findDefaultExpenseListingEndPoint() {
        /*
         * @todo pull this value from a parameter
         */
        String baseUrl = "https://us.api.concursolutions.com/api/v3.0/expense/reports?approvalStatusCode=A_EXTV&limit=2&user=ALL&isTestUser=";
        baseUrl = baseUrl + !isProduction();
        return baseUrl;
    }
    
    protected ConcurExpenseV3ListingDTO getConcurExpenseListing(String accessToken, String expenseListEndpoint) {
        LOG.info("getConcurExpenseListing, calling endpoint " + expenseListEndpoint);
        return buildConcurDTOFromEndpoint(accessToken, expenseListEndpoint, ConcurExpenseV3ListingDTO.class);
        
    }
    
    protected <T> T buildConcurDTOFromEndpoint(String accessToken, String concurEndPoint, Class<T> dtoType) {
        String tokenResponseString = null;
        int maxRetryCount = findMaxRetries();
        int retryCount = 0;
        while (retryCount < maxRetryCount && tokenResponseString == null) {
            LOG.info("buildConcurDTOFromEndpoint, trying to build " + dtoType + " from concur endpoint, attempt number " + retryCount);
            tokenResponseString = callConcurEndpoint(accessToken, concurEndPoint);
            retryCount++;
        }
        if (StringUtils.isBlank(tokenResponseString)) {
            throw new RuntimeException("buildConcurClientRequest, Unable to call concur endpoint " + concurEndPoint);
        }
        
        return convertJsonToConcurDTO(tokenResponseString, dtoType);
    }
    
    protected int findMaxRetries() {
        String retryCountString = concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.WEBSERVICE_MAX_RETRIES);
        if (LOG.isDebugEnabled()) {
            LOG.debug("findMaxRetries, the maximum number of retries is " + retryCountString);
        }
        return Integer.valueOf(retryCountString);
    }
    
    protected String callConcurEndpoint(String accessToken, String concurEndPoint) {
        Client client = null;
        Response response = null;
        try {
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.register(new JacksonJsonProvider());
            client = ClientBuilder.newClient(clientConfig);
            response = buildConcurClientRequest(client, accessToken, concurEndPoint);
            if (Family.SUCCESSFUL == response.getStatusInfo().getFamily()) {
                LOG.debug("callConcurEndpoint, successfully got a response");
                String responseString = response.readEntity(String.class);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("callConcurEndpoint, responseString: " + responseString);
                }
                return responseString;
            } else {
                LOG.error("callConcurEndpoint, unsuccessful response code returned when trying to call endpoint: " 
                        + response.getStatus());
                LOG.debug("callConcurEndpoint, the response: " + response.toString());
            }
        } catch (Exception e) {
            LOG.error("callConcurEndpoint, had an error trying to call concur end point", e);
        } finally {
            CURestClientUtils.closeQuietly(response);
            CURestClientUtils.closeQuietly(client);
        }
        return null;
    }
    
    protected Response buildConcurClientRequest(Client client, String accessToken, String concurEndPoint) {
        URI uri;
        try {
            uri = new URI(concurEndPoint);
        } catch (URISyntaxException e) {
            LOG.error("buildConcurClientRequest, there was a problem building client request.", e);
            throw new RuntimeException("buildConcurClientRequest, An error occured while building URI: " + concurEndPoint, e);
        }
        return client.target(uri)
                .request()
                .header(ConcurOAuth2.REQUEST_HEADER_CONTENT_TYPE_KEY_NAME, MediaType.TEXT_PLAIN)
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();
    }
    
    private <T> T convertJsonToConcurDTO(String tokenResponse,  Class<T> dtoType) {
        ObjectMapper objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
        T dto;
        try {
            dto = objectMapper.readValue(tokenResponse, dtoType);
        } catch (JsonProcessingException e) {
            LOG.error("convertJsonToConcurDTO, unable to convert json to " + dtoType, e);
            throw new RuntimeException("convertJsonToConcurDTO, unable to convert roken response to a java dto ", e);
        }
        return dto;
    }
    
    protected void processExpenseListing(String accessToken, ConcurExpenseV3ListingDTO expenseList, List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        for (ConcurExpenseV3ListItemDTO partialExpenseReportFromListing : expenseList.getItems()) {
            ConcurExpenseV3ListItemDTO fullExpenseReport = getConcurExpenseReport(accessToken, partialExpenseReportFromListing.getId(), partialExpenseReportFromListing.getOwnerLoginID());
            LOG.debug("processExpenseListing, expenseItem: " + partialExpenseReportFromListing.toString());
            ArrayList<String> validationMessages = new ArrayList<>();
            validationMessages.add("Validation not implemented yet");
            processingResults.add(new ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType.ExpenseReport, 
                    ConcurEventNotificationVersion2ProcessingResults.processingError, partialExpenseReportFromListing.getId(), validationMessages));
            /*
             * @todo implement validation here
             */
        }
        if (StringUtils.isNotBlank(expenseList.getNextPage())) {
            ConcurExpenseV3ListingDTO nextConcurExpenseV3ListingDTO = getConcurExpenseListing(accessToken, expenseList.getNextPage());
            processExpenseListing(accessToken, nextConcurExpenseV3ListingDTO, processingResults);
        }
    }
    
    protected ConcurExpenseV3ListItemDTO getConcurExpenseReport(String accessToken, String reportId, String userName) {
        //Https://us.api.concursolutions.com/api/v3.0/expense/reports/04CF0D6919AA4B2CB0F4?user=clk3@cornell.edu
        
        String expenseReportEndpoint = findBaseExpenseReportEndPoint() + reportId + "?user=" + userName;
        LOG.info("getConcurExpenseReport, calling endpoint " + expenseReportEndpoint);
        return buildConcurDTOFromEndpoint(accessToken, expenseReportEndpoint, ConcurExpenseV3ListItemDTO.class);
    }
    
    protected String findBaseExpenseReportEndPoint() {
        /*
         * @todo pull this value from a parameter
         */
        return "Https://us.api.concursolutions.com/api/v3.0/expense/reports/";
    }
    
    protected boolean isProduction() {
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
