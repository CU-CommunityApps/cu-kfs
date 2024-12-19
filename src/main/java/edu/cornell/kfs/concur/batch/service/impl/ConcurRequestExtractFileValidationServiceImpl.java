package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEmployeeInfoValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurRequestExtractFileValidationServiceImpl implements ConcurRequestExtractFileValidationService {
	private static final Logger LOG = LogManager.getLogger(ConcurRequestExtractFileValidationServiceImpl.class);
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEmployeeInfoValidationService concurEmployeeInfoValidationService;

    public boolean requestExtractHeaderRowValidatesToFileContents(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        boolean headerValidationPassed;
        LOG.info("requestExtractHeaderRowValidatesToFileContents:  batchDate=" + requestExtractFile.getBatchDate() + "=   recordCount=" + requestExtractFile.getRecordCount() + "=     totalApprovedAmount=" + requestExtractFile.getTotalApprovedAmount() + "=");
        headerValidationPassed = (fileRowCountMatchesHeaderRowCount(requestExtractFile, reportData) &&
                                  fileOnlyContainsOurEmployeeCustomerIndicator(requestExtractFile, reportData) &&
                                  fileTotalApprovedAmountsAggregatedByRequestIdMatchHeaderTotalApprovedAmount(requestExtractFile, reportData));
        LOG.info("requestExtractHeaderRowValidatesToFileContents:  Header validation : " + ((headerValidationPassed) ? "PASSED" : "FAILED"));
        return headerValidationPassed;
    }

    public void performRequestDetailLineValidation(ConcurRequestExtractRequestDetailFileLine detailFileLine, List<String> uniqueRequestIdsInFile) {
        if (requestDetailFileLineIsCashAdvance(detailFileLine)) {
            boolean lineValidationPassed = true;
            lineValidationPassed =  requestedCashAdvanceHasNotBeenClonedInFile(detailFileLine, uniqueRequestIdsInFile);
            LOG.info("performRequestDetailLineValidation: requestedCashAdvanceHasNotBeenClonedInFile = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceHasNotBeenUsedInExpenseReport(detailFileLine);
            LOG.info("performRequestDetailLineValidation: requestedCashAdvanceHasNotBeenUsedInExpenseReport = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceIsNotBeingDuplicated(detailFileLine);
            LOG.info("performRequestDetailLineValidation: requestedCashAdvanceIsNotBeingDuplicated = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceAccountingInformationIsValid(detailFileLine);
            LOG.info("performRequestDetailLineValidation: requestedCashAdvanceAccountingInformationIsValid = " + lineValidationPassed);

            detailFileLine.getValidationResult().setValidCashAdvanceLine(lineValidationPassed);
            LOG.info("performRequestDetailLineValidation: Detail File Line validation : " + ((lineValidationPassed) ? "PASSED" : ("FAILED" + KFSConstants.NEWLINE + detailFileLine.toString())));
        }
    }

    /**
     * A request detail line is considered a cash advance when request entry detail type is set to the cash advance indicator.
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestDetailFileLineIsCashAdvance(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isNotEmpty(detailFileLine.getCashAdvancePaymentCodeName()) &&
            StringUtils.equalsIgnoreCase(detailFileLine.getCashAdvancePaymentCodeName(), ConcurConstants.REQUEST_EXTRACT_CASH_ADVANCE_INDICATOR)) {
            detailFileLine.getValidationResult().setCashAdvanceLine(true);
        }
        else {
            detailFileLine.getValidationResult().setCashAdvanceLine(false);
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_NOT_CASH_ADVANCE_DATA_LINE));
        }
        return detailFileLine.getValidationResult().isCashAdvanceLine();
    }

    /**
     * Multiple lines representing the same cash advance can occur in a single request extract file
     * due to the way Concur constructs the file for request extract request entry detail file lines.
     * This function determines whether the detailFileLine being processed matches any of the unique
     * requestId values already in a list of cash advances previously identified.
     *
     * false is returned when a match occurs to indicate the line sent in for validation IS a clone
     * of a detailFileLine already encountered.
     *
     * true is returned when a match does NOT occur to indicate the line sent in for validation IS NOT
     * a clone of a detailFileLine already encountered AND the requestId for that detailFileLine
     * is added to the uniqueRequestIdsInFile list.
     *
     *
     * @param detailFileLine
     * @param uniqueRequestIdsInFile
     * @return boolean
     */
    private boolean requestedCashAdvanceHasNotBeenClonedInFile(ConcurRequestExtractRequestDetailFileLine detailFileLine, List<String> uniqueRequestIdsInFile) {
        if (requestIdIsNotInListOfUniqueRequestIds(detailFileLine.getRequestId(), uniqueRequestIdsInFile)) {
            detailFileLine.getValidationResult().setClonedCashAdvance(false);
            uniqueRequestIdsInFile.add(detailFileLine.getRequestId());
            LOG.info("requestedCashAdvanceHasNotBeenClonedInFile: UNIQUE CASH ADVANCE DETECTED and added to uniqueRequestIdsInFile=" + uniqueRequestIdsInFile + "=");
        }
        else {
            detailFileLine.getValidationResult().setClonedCashAdvance(true);
            LOG.info("requestedCashAdvanceHasNotBeenClonedInFile: CLONED CASH ADVANCE DETECTED cloned requestId=" + detailFileLine.getRequestId() + "=  uniqueRequestIdsInFile=" + uniqueRequestIdsInFile + "=");
        }
        return detailFileLine.getValidationResult().isNotClonedCashAdvance();
    }

    /**
     * If a cash advance has been used in an expense report, the expense report id will have information in it.
     * Bypass that transaction.
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestedCashAdvanceHasNotBeenUsedInExpenseReport(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getExpenseReportId())) {
            return true;
        }
        else {
            detailFileLine.getValidationResult().setCashAdvanceUsedInExpenseReport(true);
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_USED_IN_EXPENSE_REPORT));
            LOG.debug("requestedCashAdvanceHasNotBeenUsedInExpenseReport: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_USED_IN_EXPENSE_REPORT));
            return false;
        }
    }

    /**
     * If a cash advance is duplicated, it will have already been paid and there will be an entry in our tracking database table
     * Bypass that transaction.
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestedCashAdvanceIsNotBeingDuplicated(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        boolean haveDataForLookup = true;
        haveDataForLookup &= cashAdvanceKeyIsValid(detailFileLine);
        haveDataForLookup &= employeeIdIsValid(detailFileLine);
        haveDataForLookup &= validateAddressIfCheckPayment(detailFileLine);
        haveDataForLookup &= payeeIdTypeIsValid(detailFileLine);
        haveDataForLookup &= requestAmountIsValid(detailFileLine);

        if (haveDataForLookup) {
            ConcurRequestedCashAdvance cashAdvanceSearchKeys = new ConcurRequestedCashAdvance();
            cashAdvanceSearchKeys.setEmployeeId(detailFileLine.getEmployeeId());
            cashAdvanceSearchKeys.setCashAdvanceKey(detailFileLine.getCashAdvanceKey());
            cashAdvanceSearchKeys.setPaymentAmount(detailFileLine.getRequestAmount());

            if (getConcurRequestedCashAdvanceService().isDuplicateConcurRequestCashAdvance(cashAdvanceSearchKeys)) {
                detailFileLine.getValidationResult().setDuplicatedCashAdvanceLine(true);
                detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED));
                LOG.info("requestedCashAdvanceIsNotBeingDuplicated: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED));
                return false;
            }
            else {
                LOG.info("requestedCashAdvanceIsNotBeingDuplicated: No entry in table. Not a duplicate.");
                return true;
            }
        }
        else {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_INVALID_KEYS_FOR_DUPLICATE_CHECK));
            LOG.info("requestedCashAdvanceIsNotBeingDuplicated: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_INVALID_KEYS_FOR_DUPLICATE_CHECK));
            return false;
        }
    }

    private boolean cashAdvanceKeyIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getCashAdvanceKey())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_INVALID_UNIQUE_IDENTIFIER));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean employeeIdIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getEmployeeId())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_EMPLOYEE_ID_NULL_OR_BLANK));
            return false;
        }
        else {
            boolean validPerson = getConcurEmployeeInfoValidationService().validPerson(detailFileLine.getEmployeeId());
            if(validPerson) {
                return true;
            } else {
                detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS));
                return false;
            }
        }
    }
    private boolean validateAddressIfCheckPayment(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        boolean valid = true;
        String validationMessage = getConcurEmployeeInfoValidationService().getAddressValidationMessageIfCheckPayment(detailFileLine.getEmployeeId());
        if (StringUtils.isNotBlank(validationMessage)) {
            valid = false;
            detailFileLine.getValidationResult().addErrorMessage(validationMessage);
        }
        return valid;
    }

    private boolean payeeIdTypeIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isNotEmpty(detailFileLine.getPayeeIdType())
                && getConcurBatchUtilityService().isValidTravelerStatusForProcessingAsPDPEmployeeType(detailFileLine.getPayeeIdType())) {
            return true;
        } else {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_PAYEE_ID_TYPE_INVALID));
            return false;
        }
    }

    private boolean requestAmountIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if ((detailFileLine.getRequestAmount() == null) || (detailFileLine.getRequestAmount().isNegative()) || (detailFileLine.getRequestAmount().isZero())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_REQUEST_AMOUNT_INVALID));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean fileRowCountMatchesHeaderRowCount(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        int fileLineCount = getTotalRequestFileRowCount(requestExtractFile);
        if (fileLineCount == requestExtractFile.getRecordCount()) {
            return true;
        }
        else {
            String headerValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HEADER_ROW_COUNT_FAILED), requestExtractFile.getRecordCount().toString(), String.valueOf(fileLineCount));
            reportData.getHeaderValidationErrors().add(headerValidationError);
            LOG.error("fileRowCountMatchesHeaderRowCount: " + headerValidationError);
            return false;
        }
    }

    private boolean fileOnlyContainsOurEmployeeCustomerIndicator(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        if ( CollectionUtils.isEmpty(requestExtractFile.getRequestDetails()) ) {
            reportData.getHeaderValidationErrors().add(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HAS_NO_REQUEST_DETAIL_LINES));
            LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HAS_NO_REQUEST_DETAIL_LINES));
            return false;
        }
        else {
            if (customerProfileIsValidOnAllRequestDetailLines(requestExtractFile)) {
                return true;
            }
            else {
                String fileValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CONTAINS_BAD_CUSTOMER_PROFILE_GROUP), getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID));
                reportData.getHeaderValidationErrors().add(fileValidationError);
                LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: " + fileValidationError);
                return false;
            }
        }
    }

    private boolean customerProfileIsValidOnAllRequestDetailLines(ConcurRequestExtractFile requestExtractFile) {
        if ( CollectionUtils.isEmpty(requestExtractFile.getRequestDetails()) ) {
            return false;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            boolean foundOnAllLines = true;
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                foundOnAllLines &= concurEmployeeInfoValidationService.isEmployeeGroupIdValid(detailLine.getEmployeeGroupId());
           }
            return foundOnAllLines;
        }
    }

    private boolean fileTotalApprovedAmountsAggregatedByRequestIdMatchHeaderTotalApprovedAmount(ConcurRequestExtractFile requestExtractFile, ConcurRequestExtractBatchReportData reportData) {
        KualiDecimal detailLinesAmountSumKualiDecimal = calculateFileTotalApprovedAmountAggregatedByRequestId(requestExtractFile);
        if (detailLinesAmountSumKualiDecimal.equals(requestExtractFile.getTotalApprovedAmount())) {
            return true;
        }
        else {
            String fileValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_HEADER_AMOUNT_FILE_AMOUNT_MISMATCH), requestExtractFile.getTotalApprovedAmount().toString(), detailLinesAmountSumKualiDecimal.toString());
            reportData.getHeaderValidationErrors().add(fileValidationError);
            LOG.error("fileTotalApprovedAmountsAggregatedByRequestIdMatchHeaderTotalApprovedAmount: " + fileValidationError);
            return false;
        }
    }

    private int getTotalRequestFileRowCount(ConcurRequestExtractFile requestExtractFile) {
        int rowCount = 0;
        if ( CollectionUtils.isEmpty(requestExtractFile.getRequestDetails()) ) {
            return rowCount;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                //adding 1 ensures detailLine is also included in rowCount
                rowCount = rowCount + computedTotalRequestEntryDetailLinesForRequestDetail(detailLine) + 1;
            }
            return rowCount;
        }
    }

    private int computedTotalRequestEntryDetailLinesForRequestDetail(ConcurRequestExtractRequestDetailFileLine requestDetailLine) {
        if ( CollectionUtils.isEmpty(requestDetailLine.getRequestEntryDetails()) ) {
            return 0;
        }
        else {
            return requestDetailLine.getRequestEntryDetails().size();
        }
    }

    private KualiDecimal calculateFileTotalApprovedAmountAggregatedByRequestId(ConcurRequestExtractFile requestExtractFile) {
        KualiDecimal detailLinesTotalApprovedAmountSum = KualiDecimal.ZERO;
        if ( CollectionUtils.isEmpty(requestExtractFile.getRequestDetails()) ) {
            return detailLinesTotalApprovedAmountSum;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            List<String> uniqueRequestIds = findAllUniqueRequestIds(requestDetailLines);
            for (String uniqueRequestId : uniqueRequestIds) {
                boolean approvedAmountForRequestIdNotFound = true;
                ListIterator<ConcurRequestExtractRequestDetailFileLine> requestDetailFileLineIterator = requestDetailLines.listIterator();
                while (requestDetailFileLineIterator.hasNext() && approvedAmountForRequestIdNotFound) {
                    ConcurRequestExtractRequestDetailFileLine detailLine = requestDetailFileLineIterator.next();
                    if (StringUtils.equals(detailLine.getRequestId(), uniqueRequestId)) {
                        approvedAmountForRequestIdNotFound = false;
                        detailLinesTotalApprovedAmountSum = detailLinesTotalApprovedAmountSum.add(detailLine.getTotalApprovedAmount());
                    }
                }
            }
            return detailLinesTotalApprovedAmountSum;
        }
    }

    private List<String> findAllUniqueRequestIds(List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines) {
        List<String> uniqueRequestIds = new ArrayList<String>();
        for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
            if (requestIdIsNotInListOfUniqueRequestIds(detailLine.getRequestId(), uniqueRequestIds)) {
                uniqueRequestIds.add(detailLine.getRequestId());
            }
        }
        return uniqueRequestIds;
    }

    private boolean requestIdIsNotInListOfUniqueRequestIds(String requestIdInQuestion, List<String> uniqueRequestIds) {
        boolean requestIdNotFound = true;
        ListIterator<String> uniqueRequestIdsIterator = uniqueRequestIds.listIterator();
        while (uniqueRequestIdsIterator.hasNext() && requestIdNotFound) {
            if (StringUtils.equals(uniqueRequestIdsIterator.next(), requestIdInQuestion)) {
                requestIdNotFound = false;
            }
        }
        return requestIdNotFound;
    }

    private boolean requestedCashAdvanceAccountingInformationIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        ConcurAccountInfo concurAccountInfo =
            new ConcurAccountInfo(detailFileLine.getChart(), detailFileLine.getAccountNumber(), detailFileLine.getSubAccountNumber(),
                                  getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE),
                                  detailFileLine.getSubObjectCode(), detailFileLine.getProjectCode());
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(concurAccountInfo);
        if (validationResults.isNotValid()) {
            detailFileLine.getValidationResult().addErrorMessages(validationResults.getErrorMessages());
        }
        return validationResults.isValid();
    }


    public ConcurRequestedCashAdvanceService getConcurRequestedCashAdvanceService() {
        return concurRequestedCashAdvanceService;
    }

    public void setConcurRequestedCashAdvanceService(ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
    }

    public ConcurAccountValidationService getConcurAccountValidationService() {
        return concurAccountValidationService;
    }

    public void setConcurAccountValidationService(ConcurAccountValidationService concurAccountValidationService) {
        this.concurAccountValidationService = concurAccountValidationService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public ConcurEmployeeInfoValidationService getConcurEmployeeInfoValidationService() {
        return concurEmployeeInfoValidationService;
    }

    public void setConcurEmployeeInfoValidationService(ConcurEmployeeInfoValidationService concurEmployeeInfoValidationService) {
        this.concurEmployeeInfoValidationService = concurEmployeeInfoValidationService;
    }

}
