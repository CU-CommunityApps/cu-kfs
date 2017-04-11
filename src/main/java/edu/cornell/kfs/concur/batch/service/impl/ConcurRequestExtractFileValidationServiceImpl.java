package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.kuali.kfs.sys.KFSConstants;

import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurRequestExtractFileValidationServiceImpl implements ConcurRequestExtractFileValidationService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class);
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected PersonService personService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    public boolean requestExtractHeaderRowValidatesToFileContents(ConcurRequestExtractFile requestExtractFile) {
        boolean headerValidationPassed;
        LOG.info("requestExtractHeaderRowValidatesToFileContents:  batchDate=" + requestExtractFile.getBatchDate() + "=   recordCount=" + requestExtractFile.getRecordCount() + "=     totalApprovedAmount=" + requestExtractFile.getTotalApprovedAmount() + "=");
        headerValidationPassed = (fileRowCountMatchesHeaderRowCount(requestExtractFile) &&
                                  fileOnlyContainsOurEmployeeCustomerIndicator(requestExtractFile) &&
                                  fileTotalApprovedAmountsMatchHeaderTotalApprovedAmount(requestExtractFile));
        LOG.info("requestExtractHeaderRowValidatesToFileContents:  Header validation : " + ((headerValidationPassed) ? "PASSED" : "FAILED"));
        return headerValidationPassed;
    }

    public void performRequestDetailLineValidation(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (requestDetailFileLineIsCashAdvance(detailFileLine)) {
            boolean lineValidationPassed;
            lineValidationPassed = (requestedCashAdvanceHasNotBeenUsedInExpenseReport(detailFileLine) &&
                                    requestedCashAdvanceIsNotBeingDuplicated(detailFileLine) &&
                                    requestedCashAdvanceAccountingInformationIsValid(detailFileLine));
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
        if (StringUtils.isNotEmpty(detailFileLine.getRequestEntryExpenseType()) &&
            StringUtils.equalsIgnoreCase(detailFileLine.getRequestEntryExpenseType(), ConcurConstants.REQUEST_EXTRACT_CASH_ADVANCE_INDICATOR)) {
            detailFileLine.getValidationResult().setCashAdvanceLine(true);
        }
        else {
            detailFileLine.getValidationResult().setCashAdvanceLine(false);
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_NOT_CASH_ADVANCE_DATA_LINE));
        }
        return detailFileLine.getValidationResult().isCashAdvanceLine();
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
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_USED_IN_EXPENSE_REPORT));
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
        boolean haveDataForLookup = (requestIdIsValid(detailFileLine) && employeeIdIsValid(detailFileLine) && payeeIdTypeIsValid(detailFileLine) && requestAmountIsValid(detailFileLine));

        ConcurRequestedCashAdvance cashAdvanceSearchKeys = new ConcurRequestedCashAdvance();
        cashAdvanceSearchKeys.setEmployeeId(detailFileLine.getEmployeeId());
        cashAdvanceSearchKeys.setRequestId(detailFileLine.getRequestId());
        cashAdvanceSearchKeys.setPaymentAmount(detailFileLine.getRequestAmount());

        if (getConcurRequestedCashAdvanceService().isDuplicateConcurRequestCashAdvance(cashAdvanceSearchKeys)) {
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED));
            LOG.debug("requestedCashAdvanceIsNotBeingDuplicated: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean requestIdIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getRequestId())) {
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_REQUEST_ID_INVALID));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean employeeIdIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getEmployeeId())) {
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_EMPLOYEE_ID_NULL_OR_BLANK));
            return false;
        }
        else {
            Person employee = getPersonService().getPersonByEmployeeId(detailFileLine.getEmployeeId());
            if (ObjectUtils.isNotNull(employee)) {
                return true;
            }
            else {
                detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_EMPLOYEE_ID_NOT_FOUND_IN_KFS));
                return false;
            }
        }
    }

    private boolean payeeIdTypeIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if ( StringUtils.isNotEmpty(detailFileLine.getPayeeIdType()) &&
             (detailFileLine.getPayeeIdType().equalsIgnoreCase(ConcurConstants.EMPLOYEE_STATUS_CODE) ||
              detailFileLine.getPayeeIdType().equalsIgnoreCase(ConcurConstants.NON_EMPLOYEE_STATUS_CODE)) ){
            return true;
        }
        else {
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_PAYEE_ID_TYPE_INVALID));
            return false;
        }
    }

    private boolean requestAmountIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        if ((detailFileLine.getRequestAmount() == null) || (detailFileLine.getRequestAmount().isNegative()) || (detailFileLine.getRequestAmount().isZero())) {
            detailFileLine.getValidationResult().addMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_REQUEST_AMOUNT_INVALID));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean fileRowCountMatchesHeaderRowCount(ConcurRequestExtractFile requestExtractFile) {
        int fileLineCount = getTotalRequestFileRowCount(requestExtractFile);
        if (fileLineCount == requestExtractFile.getRecordCount()) {
            return true;
        }
        else {
            LOG.error("fileRowCountMatchesHeaderRowCount: Header row count validation failed. Header record count was =" + requestExtractFile.getRecordCount() + "= while file line count was =" + fileLineCount + "=");
            return false;
        }
    }

    private boolean fileOnlyContainsOurEmployeeCustomerIndicator(ConcurRequestExtractFile requestExtractFile) {
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
            LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: There are no Request Detail lines in the file.");
            return false;
        }
        else {
            String ourCustomerProfile = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID);
            if (StringUtils.isNotEmpty(ourCustomerProfile) && ourCustomerProfileIsOnAllRequestDetailLines(requestExtractFile, ourCustomerProfile)) {
                return true;
            }
            else {
                LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: File contains Request Detail lines that do not match the employee customer profile group of: " + getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID));
                return false;
            }
        }
    }

    private boolean ourCustomerProfileIsOnAllRequestDetailLines(ConcurRequestExtractFile requestExtractFile, String ourCustomerProfile) {
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
            return false;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            boolean foundOnAllLines = true;
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                foundOnAllLines &= (StringUtils.isNotEmpty(detailLine.getEmployeeGroupId()) &&
                                   detailLine.getEmployeeGroupId().equalsIgnoreCase(ourCustomerProfile));
            }
            return foundOnAllLines;
        }
    }

    private boolean fileTotalApprovedAmountsMatchHeaderTotalApprovedAmount(ConcurRequestExtractFile requestExtractFile) {
        KualiDecimal detailLinesAmountSumKualiDecimal = getTotalRequestFileTotalApprovedAmount(requestExtractFile);
        if (detailLinesAmountSumKualiDecimal.equals(requestExtractFile.getTotalApprovedAmount())) {
            return true;
        }
        else {
            LOG.error("fileTotalApprovedAmountsMatchHeaderTotalApprovedAmount: Header amount validation failed. Header amount was =" + requestExtractFile.getTotalApprovedAmount().toString() + "= while file calculated amount was =" + detailLinesAmountSumKualiDecimal.toString() + "=");
            return false;
        }
    }

    private int getTotalRequestFileRowCount(ConcurRequestExtractFile requestExtractFile) {
        int rowCount = 0;
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
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
        if ( (requestDetailLine.getRequestEntryDetails() == null) || (requestDetailLine.getRequestEntryDetails().isEmpty()) ) {
            return 0;
        }
        else {
            return requestDetailLine.getRequestEntryDetails().size();
        }
    }

    private KualiDecimal getTotalRequestFileTotalApprovedAmount(ConcurRequestExtractFile requestExtractFile) {
        KualiDecimal detailLinesTotalApprovedAmountSum = KualiDecimal.ZERO;
        if ( (requestExtractFile.getRequestDetails() == null) || (requestExtractFile.getRequestDetails().isEmpty()) ) {
            return detailLinesTotalApprovedAmountSum;
        }
        else {
            List<ConcurRequestExtractRequestDetailFileLine> requestDetailLines = requestExtractFile.getRequestDetails();
            for (ConcurRequestExtractRequestDetailFileLine detailLine : requestDetailLines) {
                detailLinesTotalApprovedAmountSum = detailLinesTotalApprovedAmountSum.add(detailLine.getTotalApprovedAmount());
            }
            return detailLinesTotalApprovedAmountSum;
        }
    }

    private boolean requestedCashAdvanceAccountingInformationIsValid(ConcurRequestExtractRequestDetailFileLine detailFileLine) {
        ConcurAccountInfo concurAccountInfo =
            new ConcurAccountInfo(detailFileLine.getChart(), detailFileLine.getAccountNumber(), detailFileLine.getSubAccountNumber(),
                                  getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE),
                                  detailFileLine.getSubObjectCode(), detailFileLine.getProjectCode());
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(concurAccountInfo);
        if (validationResults.isNotValid()) {
            detailFileLine.getValidationResult().addMessages(validationResults.getMessages());
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

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
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

}
