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
        return concurEventNotificationV2WebserviceService.getConcurExpenseListing(accessToken,
                findDefaultExpenseListingEndPoint());
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
            ConcurExpenseV3ListItemDTO fullExpenseReport = getConcurExpenseReport(accessToken,
                    partialExpenseReportFromListing.getId(), partialExpenseReportFromListing.getOwnerLoginID());
            validateConcurExpenseReport(processingResults, fullExpenseReport);
        }
        if (StringUtils.isNotBlank(expenseList.getNextPage())) {
            ConcurExpenseV3ListingDTO nextConcurExpenseV3ListingDTO = concurEventNotificationV2WebserviceService
                    .getConcurExpenseListing(accessToken, expenseList.getNextPage());
            processExpenseListing(accessToken, nextConcurExpenseV3ListingDTO, processingResults);
        }
    }

    protected void validateConcurExpenseReport(List<ConcurEventNotificationProcessingResultsDTO> processingResults,
            ConcurExpenseV3ListItemDTO fullExpenseReport) {
        LOG.info("validateConcurExpenseReport, validation step not implemented yet.");
        ArrayList<String> validationMessages = new ArrayList<>();
        validationMessages.add("Validation not implemented yet");
        processingResults.add(
                new ConcurEventNotificationProcessingResultsDTO(ConcurEventNoticationVersion2EventType.ExpenseReport,
                        ConcurEventNotificationVersion2ProcessingResults.processingError, fullExpenseReport.getId(),
                        validationMessages));
    }

    protected ConcurExpenseV3ListItemDTO getConcurExpenseReport(String accessToken, String reportId, String userName) {
        String expenseReportEndpoint = findBaseExpenseReportEndPoint() + reportId + "?user=" + userName;
        return concurEventNotificationV2WebserviceService.getConcurExpenseV3ListItemDTO(accessToken,
                expenseReportEndpoint);
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
