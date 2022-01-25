package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;

import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNoticationVersion2EventType;
import edu.cornell.kfs.concur.ConcurConstants.ConcurEventNotificationVersion2ProcessingResults;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationV2WebserviceService;
import edu.cornell.kfs.concur.batch.service.ConcurExpenseV3Service;
import edu.cornell.kfs.concur.businessobjects.ConcurEventNotificationProcessingResultsDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseAllocationV3ListingDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListItemDTO;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurExpenseV3ListingDTO;

public class ConcurExpenseV3ServiceImpl implements ConcurExpenseV3Service {
    private static final Logger LOG = LogManager.getLogger();

    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService;

    @Override
    public void processExpenseReports(String accessToken,
            List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        ConcurExpenseV3ListingDTO expenseList = getConcurStartingExpenseListing(accessToken);
        processExpenseListing(accessToken, expenseList, processingResults);
    }

    protected ConcurExpenseV3ListingDTO getConcurStartingExpenseListing(String accessToken) {
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                findDefaultExpenseListingEndPoint(), ConcurExpenseV3ListingDTO.class, "Initial expense listing");
    }

    protected String findDefaultExpenseListingEndPoint() {
        String baseUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V3_LISTING_ENDPOINT);
        baseUrl = baseUrl + !isProduction();
        return baseUrl;
    }

    protected void processExpenseListing(String accessToken, ConcurExpenseV3ListingDTO expenseList,
            List<ConcurEventNotificationProcessingResultsDTO> processingResults) {
        for (ConcurExpenseV3ListItemDTO partialExpenseReportFromListing : expenseList.getItems()) {
            /*
             * @todo maybe remove fullExpenseReport
             */
            ConcurExpenseV3ListItemDTO fullExpenseReport = getConcurExpenseReport(accessToken,
                    partialExpenseReportFromListing.getId(), partialExpenseReportFromListing.getOwnerLoginID());
            
            List<ConcurExpenseAllocationV3ListItemDTO> allocationItems = getConcurExpenseAllocationV3ListItemsForReport(accessToken, fullExpenseReport.getId());
            
            validateExepsneALlocations(processingResults, allocationItems, fullExpenseReport.getId());
        }
        if (StringUtils.isNotBlank(expenseList.getNextPage())) {
            ConcurExpenseV3ListingDTO nextConcurExpenseV3ListingDTO = concurEventNotificationV2WebserviceService
                    .buildConcurDTOFromEndpoint(accessToken, expenseList.getNextPage(), ConcurExpenseV3ListingDTO.class, "Expense listing next page");
            processExpenseListing(accessToken, nextConcurExpenseV3ListingDTO, processingResults);
        }
    }
    
    protected ConcurExpenseV3ListItemDTO getConcurExpenseReport(String accessToken, String reportId, String userName) {
        String expenseReportEndpoint = findBaseExpenseReportEndPoint() + reportId + "?user=" + userName;
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                expenseReportEndpoint, ConcurExpenseV3ListItemDTO.class, "expense report for " + reportId);
    }
    
    protected List<ConcurExpenseAllocationV3ListItemDTO> getConcurExpenseAllocationV3ListItemsForReport(String accessToken, String reportId) {
        String baseAllocationEndpoint = findAllocationEndPoint() + reportId;
        ConcurExpenseAllocationV3ListingDTO allocationList = getConcurExpenseAllocationV3ListingDTO(accessToken, reportId, baseAllocationEndpoint);
        
        List<ConcurExpenseAllocationV3ListItemDTO> allocationItems = allocationList.getItems();
        
        while(StringUtils.isNotBlank(allocationList.getNextPage())) {
            allocationList = getConcurExpenseAllocationV3ListingDTO(accessToken, reportId, allocationList.getNextPage());
            allocationItems.addAll(allocationList.getItems());
        }
        
        return allocationItems;
    }
    
    protected String findAllocationEndPoint() {
        /*
         * @todo pull this from a parameter
         */
        return "https://www.concursolutions.com/api/v3.0/expense/allocations?reportID=";
    }
    
    protected ConcurExpenseAllocationV3ListingDTO getConcurExpenseAllocationV3ListingDTO(String accessToken, String reportId, String allocationEndpoint) {
        return concurEventNotificationV2WebserviceService.buildConcurDTOFromEndpoint(accessToken,
                allocationEndpoint, ConcurExpenseAllocationV3ListingDTO.class, "expense report for " + reportId);
    }


    protected void validateExepsneALlocations(List<ConcurEventNotificationProcessingResultsDTO> processingResults,
            List<ConcurExpenseAllocationV3ListItemDTO> allocationItems, String reportId) {
        LOG.info("validateExepsneALlocations, validation step not implemented yet.");
        ArrayList<String> validationMessages = new ArrayList<>();
        validationMessages.add("Validation not implemented yet");
        processingResults.add(
                new ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType.ExpenseReport,
                        ConcurEventNotificationVersion2ProcessingResults.processingError, reportId,
                        validationMessages));
    }

    protected String findBaseExpenseReportEndPoint() {
        String reportUrl = concurBatchUtilityService
                .getConcurParameterValue(ConcurParameterConstants.EXPENSE_V3_REPORT_ENDPOINT);
        return reportUrl;
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

    public void setConcurEventNotificationV2WebserviceService(
            ConcurEventNotificationV2WebserviceService concurEventNotificationV2WebserviceService) {
        this.concurEventNotificationV2WebserviceService = concurEventNotificationV2WebserviceService;
    }

}
