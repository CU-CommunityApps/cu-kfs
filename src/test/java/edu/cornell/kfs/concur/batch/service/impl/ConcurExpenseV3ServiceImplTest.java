package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.IllegalStateException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationStatus;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationWebApiService;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationResponse;
import edu.cornell.kfs.concur.exception.ConcurWebserviceException;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;

class ConcurExpenseV3ServiceImplTest {
    private static final String ACCESS_TOKEN = "testing access token";
    private static final String INIT_EXPENSE_LISTING = "Initial expense listing";
    
    private static final String CONCUR_URL = "https://test.cncr.kuali.cornell.edu/dummyApi/";
    private static final String CONCUR_EXPENSE_ENDPOINT = CONCUR_URL + "expenses/";
    private static final String CONCUR_ALLOCATION_ENDPOINT = CONCUR_URL + "allocations/";
    private static final String CONCUR_EXPENSE_ENDOINT_FORMAT = CONCUR_EXPENSE_ENDPOINT + "{0}?user={1}";
    private static final String CONCUR_ALLOCATION_ENDPOINT_FORMAT = CONCUR_ALLOCATION_ENDPOINT + "{0}";
    
    private static final String EXPENSE_REPORT_ID_MESSAGE_FORMAT = "Expense report for report id {0}";
    private static final String ALLOCATION_EXPENSE_REPORT_ID_MESSSAGE_FORMAT = "Allocation listing for expense report id {0}";
    private static final String PROCESSING_ERROR_MESSAGE_FORMAT = "There was an error processing {0}: {1}";

    private static final String GOOD_ID = "123";
    private static final String GOOD_OWNER_ID = "goodowner";

    private static final String BAD_ID = "987";
    private static final String BAD_OWNER_ID = "badowner";

    private ConcurExpenseV3ServiceImpl expenseService;
    private List<ConcurEventNotificationResponse> processingResults;

    @BeforeEach
    public void setUp() throws Exception {
        Configurator.setLevel(ConcurExpenseV3ServiceImpl.class.getName(), Level.DEBUG);
        expenseService = new TestableConcurExpenseV3ServiceImpl();
        expenseService.setConfigurationService(buildMockConfigurationService());
        expenseService.setConcurBatchUtilityService(buildMockConcurBatchUtilityService());
        expenseService.setConcurEventNotificationWebApiService(buildMockConcurEventNotificationWebApiService());
        processingResults = new ArrayList<ConcurEventNotificationResponse>();
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(
                service.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_INTIAL_EXPENSE_LISTING))
                .thenReturn(INIT_EXPENSE_LISTING);
        Mockito.when(service.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_REPORT))
                .thenReturn(EXPENSE_REPORT_ID_MESSAGE_FORMAT);
        Mockito.when(service
                .getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EXPENSEV3_EXPENSE_ALLOCATION_LISTING))
                .thenReturn(ALLOCATION_EXPENSE_REPORT_ID_MESSSAGE_FORMAT);
        Mockito.when(service.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_PROCESSING_ERROR))
                .thenReturn(PROCESSING_ERROR_MESSAGE_FORMAT);
        return service;
    }

    private ConcurBatchUtilityService buildMockConcurBatchUtilityService() {
        ConcurBatchUtilityService service = Mockito.mock(ConcurBatchUtilityService.class);
        Mockito.when(service.getConcurParameterValue(ConcurParameterConstants.CONCUR_GEOLOCATION_URL))
                .thenReturn(CONCUR_URL);
        return service;
    }

    private ConcurEventNotificationWebApiService buildMockConcurEventNotificationWebApiService() {
        ConcurEventNotificationWebApiService service = Mockito.mock(ConcurEventNotificationWebApiService.class);
        Mockito.when(service.buildConcurDTOFromEndpoint(ACCESS_TOKEN, CONCUR_EXPENSE_ENDPOINT,
                ConcurExpenseV3ListingDTO.class, INIT_EXPENSE_LISTING))
                .thenReturn(buildTestingConcurExpenseV3ListingDTO());

        String goodExpenseEndpoint = MessageFormat.format(CONCUR_EXPENSE_ENDOINT_FORMAT, GOOD_ID, GOOD_OWNER_ID);
        String goodExpenseMessage = MessageFormat.format(EXPENSE_REPORT_ID_MESSAGE_FORMAT, GOOD_ID);
        Mockito.when(service.buildConcurDTOFromEndpoint(ACCESS_TOKEN, goodExpenseEndpoint,
                ConcurExpenseV3ListItemDTO.class, goodExpenseMessage)).thenReturn(buildItem(GOOD_ID, GOOD_OWNER_ID));

        String goodAllocationEndpoint = MessageFormat.format(CONCUR_ALLOCATION_ENDPOINT_FORMAT, GOOD_ID);
        String goodALlocationMessage = MessageFormat.format(ALLOCATION_EXPENSE_REPORT_ID_MESSSAGE_FORMAT, GOOD_ID);
        Mockito.when(service.buildConcurDTOFromEndpoint(ACCESS_TOKEN, goodAllocationEndpoint,
                ConcurExpenseAllocationV3ListingDTO.class, goodALlocationMessage))
                .thenReturn(buildGoodAllocationListingDTO());

        String badExpenseEndpoint = MessageFormat.format(CONCUR_EXPENSE_ENDOINT_FORMAT, BAD_ID, BAD_OWNER_ID);
        String badExpenseMessage = MessageFormat.format(EXPENSE_REPORT_ID_MESSAGE_FORMAT, BAD_ID);
        Mockito.when(service.buildConcurDTOFromEndpoint(ACCESS_TOKEN, badExpenseEndpoint,
                ConcurExpenseV3ListItemDTO.class, badExpenseMessage))
                .thenThrow(new ConcurWebserviceException("Testing Concur exception"));

        return service;
    }

    private ConcurExpenseV3ListingDTO buildTestingConcurExpenseV3ListingDTO() {
        ConcurExpenseV3ListingDTO dto = new ConcurExpenseV3ListingDTO();
        List<ConcurExpenseV3ListItemDTO> items = new ArrayList<ConcurExpenseV3ListItemDTO>();
        items.add(buildItem(GOOD_ID, GOOD_OWNER_ID));
        items.add(buildItem(BAD_ID, BAD_OWNER_ID));
        dto.setItems(items);
        ;
        return dto;
    }
    
    private ConcurExpenseV3ListItemDTO buildItem(String id, String ownerLoginId) {
        ConcurExpenseV3ListItemDTO item = new ConcurExpenseV3ListItemDTO();
        item.setId(id);
        item.setOwnerLoginID(ownerLoginId);
        return item;
    }

    private ConcurExpenseAllocationV3ListingDTO buildGoodAllocationListingDTO() {
        ConcurExpenseAllocationV3ListingDTO dto = new ConcurExpenseAllocationV3ListingDTO();
        List<ConcurExpenseAllocationV3ListItemDTO> items = new ArrayList<ConcurExpenseAllocationV3ListItemDTO>();
        dto.setItems(items);
        return dto;
    }

    @AfterEach
    public void tearDown() throws Exception {
        expenseService = null;
        processingResults = null;
    }

    @Test
    public void test() {
        assertEquals(0, processingResults.size());
        expenseService.processExpenseReports(ACCESS_TOKEN, processingResults);
        assertEquals(2, processingResults.size());
        ConcurEventNotificationResponse goodReport = findConcurEventNotificationResponse(GOOD_ID);
        assertEquals(ConcurEventNotificationStatus.validAccounts, goodReport.getEventNotificationStatus());
        
        ConcurEventNotificationResponse badReport = findConcurEventNotificationResponse(BAD_ID);
        assertEquals(ConcurEventNotificationStatus.processingError, badReport.getEventNotificationStatus());
    }
    
    private ConcurEventNotificationResponse findConcurEventNotificationResponse(String reportNumber) {
        for (ConcurEventNotificationResponse response : processingResults) {
            if (StringUtils.equals(reportNumber, response.getReportNumber())) {
                return response;
            }
        }
        throw new RuntimeException("Unable to find report number " + reportNumber);
    }

    private class TestableConcurExpenseV3ServiceImpl extends ConcurExpenseV3ServiceImpl {
        @Override
        protected boolean isProduction() {
            return false;
        }

        @Override
        protected String findDefaultExpenseListingEndPoint() {
            return CONCUR_EXPENSE_ENDPOINT;
        }

        @Override
        protected String findBaseExpenseReportEndPoint() {
            return CONCUR_EXPENSE_ENDPOINT;
        }

        @Override
        protected String findBaseAllocationEndPoint() {
            return CONCUR_ALLOCATION_ENDPOINT;
        }
    }

}
