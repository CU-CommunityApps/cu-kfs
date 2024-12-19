package edu.cornell.kfs.concur.batch.service.impl;

import java.text.MessageFormat;
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
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEmployeeInfoValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceFileValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurSaeCreateRequestedCashAdvanceFileValidationServiceImpl implements ConcurSaeCreateRequestedCashAdvanceFileValidationService {
    private static final Logger LOG = LogManager.getLogger(ConcurSaeCreateRequestedCashAdvanceFileValidationServiceImpl.class);
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEmployeeInfoValidationService concurEmployeeInfoValidationService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;

    @Override
    public boolean saeHeaderRowValidatesToFileContentsForRequestedCashAdvances(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        boolean headerValidationPassed;
        LOG.info("saeHeaderRowValidatesToFileContentsForRequestedCashAdvances:  batchDate=" + standardAccountingExtractFile.getBatchDate() + "=   recordCount=" + standardAccountingExtractFile.getRecordCount() + "=     journalAmountTotal=" + standardAccountingExtractFile.getJournalAmountTotal() + "=");
        headerValidationPassed = (fileRowCountMatchesHeaderRowCount(standardAccountingExtractFile, reportData) &&
                                  fileOnlyContainsOurEmployeeCustomerIndicator(standardAccountingExtractFile, reportData) &&
                                  fileHeaderJournalTotalMatchesFileDetailsCalculatedJournalAmountTotal(standardAccountingExtractFile, reportData));
        LOG.info("saeHeaderRowValidatesToFileContentsForRequestedCashAdvances:  Header validation : " + ((headerValidationPassed) ? "PASSED" : "FAILED"));
        return headerValidationPassed;
    }

    @Override
    public void performSaeDetailLineValidationForRequestedCashAdvances(ConcurStandardAccountingExtractDetailLine detailFileLine, List<String> uniqueRequestedCashAdvanceKeysInFile) {
        if (requestDetailFileLineIsCashAdvance(detailFileLine)) {
            boolean lineValidationPassed = true;
            lineValidationPassed = requestedCashAdvanceApprovedOrApplied(detailFileLine);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceApprovedByConcurAdministrator = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceHasNotBeenClonedInFile(detailFileLine, uniqueRequestedCashAdvanceKeysInFile);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceHasNotBeenClonedInFile = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceHasNotBeenUsedInExpenseReport(detailFileLine);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceHasNotBeenUsedInExpenseReport = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceIsNotBeingDuplicated(detailFileLine);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceIsNotBeingDuplicated = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceHasValidAddressWhenPaymentIsByCheck(detailFileLine);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceHasValidAddressWhenPaymentIsByCheck = " + lineValidationPassed);
            
            lineValidationPassed &= requestedCashAdvanceAccountingInformationIsValid(detailFileLine);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: requestedCashAdvanceAccountingInformationIsValid = " + lineValidationPassed);

            detailFileLine.getValidationResult().setValidCashAdvanceLine(lineValidationPassed);
            LOG.info("performSaeDetailLineValidationForRequestedCashAdvances: Detail File Line validation : " + ((lineValidationPassed) ? "PASSED" : ("FAILED" + KFSConstants.NEWLINE + detailFileLine.toString())));
        }
    }

    /**
     * Cash advances should have the cash advance indicator set.
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestDetailFileLineIsCashAdvance(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        if (getConcurStandardAccountingExtractCashAdvanceService().isPreTripCashAdvanceRequestLine(detailFileLine)) {
            detailFileLine.getValidationResult().setCashAdvanceLine(true);
        } else {
            detailFileLine.getValidationResult().setCashAdvanceLine(false);
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_NOT_REQUESTED_CASH_ADVANCE_DATA_LINE));
        }
        return detailFileLine.getValidationResult().isCashAdvanceLine();
    }

    /**
     * Requested cash advances are valid if they are either approved by administrator (1) or applied to trip reimbursement (2)
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestedCashAdvanceApprovedOrApplied(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        boolean validCashAdvance = getConcurStandardAccountingExtractCashAdvanceService().isPreTripCashAdvanceIssuedByCashAdmin(detailFileLine) ||
                getConcurStandardAccountingExtractCashAdvanceService().isCashAdvanceToBeAppliedToReimbursement(detailFileLine);

        if (validCashAdvance) {
            detailFileLine.getValidationResult().setCashAdvanceApprovedOrApplied(true);
        } else {
            detailFileLine.getValidationResult().setCashAdvanceApprovedOrApplied(false);

            String errorMessage = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_DATA_LINE_NOT_APPROVED_OR_APPLIED);
            String validationError = MessageFormat.format(errorMessage, detailFileLine.getCashAdvanceTransactionType());
            detailFileLine.getValidationResult().addErrorMessage(validationError);
        }

        return detailFileLine.getValidationResult().isCashAdvanceApprovedOrApplied();
    }

    /**
     * Multiple lines representing the same cash advance could occur in a single standard accounting extract file 
     * due to the way Concur constructs the file.  This function determines whether the detailFileLine being 
     * processed matches any of the unique requestId values already in a list of cash advances previously identified.
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
    private boolean requestedCashAdvanceHasNotBeenClonedInFile(ConcurStandardAccountingExtractDetailLine detailFileLine, List<String> uniqueRequestedCashAdvanceKeysInFile) {
        if (cashAdvanceKeyIsNotInListOfUniqueCashAdvanceKeys(detailFileLine.getCashAdvanceKey(), uniqueRequestedCashAdvanceKeysInFile)) {
            detailFileLine.getValidationResult().setClonedCashAdvance(false);
            uniqueRequestedCashAdvanceKeysInFile.add(detailFileLine.getCashAdvanceKey());
            LOG.info("requestedCashAdvanceHasNotBeenClonedInFile: UNIQUE CASH ADVANCE REQUEST DETECTED and added to uniqueRequestedCashAdvanceKeysInFile=" + uniqueRequestedCashAdvanceKeysInFile + "=");
        }
        else {
            detailFileLine.getValidationResult().setClonedCashAdvance(true);
            LOG.info("requestedCashAdvanceHasNotBeenClonedInFile: CLONED CASH ADVANCE REQUEST DETECTED cloned cashAdvancekey=" + detailFileLine.getCashAdvanceKey() + "=  uniqueRequestedCashAdvanceKeysInFile=" + uniqueRequestedCashAdvanceKeysInFile + "=");
        }
        return detailFileLine.getValidationResult().isNotClonedCashAdvance();
    }

    /**
     * If a cash advance is being used as part of the trip reimbursement processing, meaning the cash advance is
     * being applied to an expense report, cash advance transaction type will be set to a value indicating
     * that type of processing for the data. Bypass that transaction for the requested cash advance processing.
     * 
     * returns false: when cash transaction type equals value indicating cash advance is being applied to trip reimbursement
     * 
     * returns true: when cash transaction type indicates data is NOT being applied to a trip reimbursement
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestedCashAdvanceHasNotBeenUsedInExpenseReport(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        if (StringUtils.isNotEmpty(detailFileLine.getCashAdvanceTransactionType()) &&
            StringUtils.equalsIgnoreCase(detailFileLine.getCashAdvanceTransactionType(), ConcurConstants.SAE_CASH_ADVANCE_BEING_APPLIED_TO_TRIP_REIMBURSEMENT)) {
            detailFileLine.getValidationResult().setCashAdvanceUsedInExpenseReport(true);
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_NOT_REQUESTED_CASH_ADVANCE_DATA_LINE));
            LOG.info("requestedCashAdvanceHasNotBeenUsedInExpenseReport: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_USED_IN_EXPENSE_REPORT));
        } else {
            detailFileLine.getValidationResult().setCashAdvanceUsedInExpenseReport(false);
        }
        return detailFileLine.getValidationResult().isCashAdvanceNotUsedInExpenseReport();
    }

    /**
     * If a requested cash advance initiated and approved by the Concur Cash Advance Administrator is duplicated, it will have already been paid.
     * There will be an entry in our tracking database table.
     * Bypass that transaction.
     *
     * @param detailFileLine
     * @return boolean
     */
    private boolean requestedCashAdvanceIsNotBeingDuplicated(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        boolean haveDataForLookup = true;
        haveDataForLookup &= cashAdvanceKeyIsValid(detailFileLine);
        haveDataForLookup &= employeeIdIsValid(detailFileLine);
        haveDataForLookup &= requestAmountIsValid(detailFileLine);

        if (haveDataForLookup) {
            ConcurRequestedCashAdvance cashAdvanceSearchKeys = new ConcurRequestedCashAdvance();
            cashAdvanceSearchKeys.setEmployeeId(detailFileLine.getEmployeeId());
            cashAdvanceSearchKeys.setCashAdvanceKey(detailFileLine.getCashAdvanceKey());
            cashAdvanceSearchKeys.setPaymentAmount(detailFileLine.getCashAdvanceAmount());

            if (getConcurRequestedCashAdvanceService().isDuplicateConcurRequestCashAdvance(cashAdvanceSearchKeys)) {
                detailFileLine.getValidationResult().setDuplicatedCashAdvanceLine(true);
                detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_DUPLICATE_CASH_ADVANCE_DETECTED));
                LOG.info("requestedCashAdvanceIsNotBeingDuplicated: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_DUPLICATE_CASH_ADVANCE_DETECTED));
                return false;
            } else {
                LOG.info("requestedCashAdvanceIsNotBeingDuplicated: No entry in table. Not a duplicate.");
                detailFileLine.getValidationResult().setDuplicatedCashAdvanceLine(false);
                return true;
            }
        } else {
            detailFileLine.getValidationResult().setDuplicatedCashAdvanceLine(true);
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_INVALID_KEYS_FOR_DUPLICATE_CHECK));
            LOG.info("requestedCashAdvanceIsNotBeingDuplicated: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_INVALID_KEYS_FOR_DUPLICATE_CHECK));
            return false;
        }
    }

    private boolean cashAdvanceKeyIsValid(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getCashAdvanceKey())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_INVALID_UNIQUE_IDENTIFIER));
            return false;
        }
        else {
            return true;
        }
    }

    private boolean employeeIdIsValid(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        if (StringUtils.isEmpty(detailFileLine.getEmployeeId())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_EMPLOYEE_ID_NULL_OR_BLANK));
            return false;
        } else {
            boolean validPerson = getConcurEmployeeInfoValidationService().validPerson(detailFileLine.getEmployeeId());
            if(validPerson) {
                return true;
            } else {
                detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS));
                return false;
            }
        }
    }

    private boolean requestedCashAdvanceHasValidAddressWhenPaymentIsByCheck(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        boolean valid = true;
        String validationMessage = getConcurEmployeeInfoValidationService().getAddressValidationMessageIfCheckPayment(detailFileLine.getEmployeeId());
        if (StringUtils.isNotBlank(validationMessage)) {
            valid = false;
            detailFileLine.getValidationResult().addErrorMessage(validationMessage);
        }
        detailFileLine.getValidationResult().setValidAddressWhenCheckPaymentForCashAdvance(valid);
        return valid;
    }

    private boolean requestAmountIsValid(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        if ((detailFileLine.getCashAdvanceAmount() == null) || (detailFileLine.getCashAdvanceAmount().isNegative()) || (detailFileLine.getCashAdvanceAmount().isZero())) {
            detailFileLine.getValidationResult().addErrorMessage(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCE_AMOUNT_INVALID));
            return false;
        } else {
            return true;
        }
    }

    private boolean fileRowCountMatchesHeaderRowCount(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        int fileLineCount = standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().size();
        if (fileLineCount == standardAccountingExtractFile.getRecordCount()) {
            return true;
        } else {
            String headerValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_HEADER_ROW_COUNT_FAILED), standardAccountingExtractFile.getRecordCount().toString(), String.valueOf(fileLineCount));
            reportData.getHeaderValidationErrors().add(headerValidationError);
            LOG.error("fileRowCountMatchesHeaderRowCount: " + headerValidationError);
            return false;
        }
    }

    private boolean fileOnlyContainsOurEmployeeCustomerIndicator(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        if ( CollectionUtils.isEmpty(standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) ) {
            reportData.getHeaderValidationErrors().add(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_HAS_NO_REQUEST_DETAIL_LINES));
            LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: " + getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_HAS_NO_REQUEST_DETAIL_LINES));
            return false;
        } else {
            if (customerProfileIsValidOnAllRequestDetailLines(standardAccountingExtractFile)) {
                return true;
            } else {
                String fileValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_CONTAINS_BAD_CUSTOMER_PROFILE_GROUP), getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID));
                reportData.getHeaderValidationErrors().add(fileValidationError);
                LOG.error("fileOnlyContainsOurEmployeeCustomerIndicator: " + fileValidationError);
                return false;
            }
        }
    }

    private boolean customerProfileIsValidOnAllRequestDetailLines(ConcurStandardAccountingExtractFile standardAccountingExtractFile) {
        if ( CollectionUtils.isEmpty(standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) ) {
            return false;
        } else {
            List<ConcurStandardAccountingExtractDetailLine> saeDetailLines = standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines();
            boolean foundOnAllLines = true;
            for (ConcurStandardAccountingExtractDetailLine detailLine : saeDetailLines) {
                foundOnAllLines &= concurEmployeeInfoValidationService.isEmployeeGroupIdValid(detailLine.getEmployeeGroupId());
           }
            return foundOnAllLines;
        }
    }
    
    private boolean fileHeaderJournalTotalMatchesFileDetailsCalculatedJournalAmountTotal(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        KualiDecimal detailLinesAmountSumKualiDecimal = calculateFileDetailsJournalAmountTotal(standardAccountingExtractFile, reportData);
        if (detailLinesAmountSumKualiDecimal.equals(standardAccountingExtractFile.getJournalAmountTotal())) {
            return true;
        } else {
            String fileValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_HEADER_JOURNAL_AMOUNT_FILE_AMOUNT_MISMATCH), standardAccountingExtractFile.getJournalAmountTotal().toString(), detailLinesAmountSumKualiDecimal.toString());
            reportData.getHeaderValidationErrors().add(fileValidationError);
            LOG.error("fileHeaderJournalTotalMatchesFileDetailsCalculatedJournalAmountTotal: " + fileValidationError);
            return false;
        }
    }

    private KualiDecimal calculateFileDetailsJournalAmountTotal(ConcurStandardAccountingExtractFile standardAccountingExtractFile, ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        KualiDecimal detailLinesTotalJournalAmountSum = KualiDecimal.ZERO;
        if ( CollectionUtils.isEmpty(standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) ) {
            return detailLinesTotalJournalAmountSum;
        } else {
            List<ConcurStandardAccountingExtractDetailLine> standardAccountingDetailLines = standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines();
            for (ConcurStandardAccountingExtractDetailLine standardAccountingDetailLine : standardAccountingDetailLines) {
                if (standardAccountingDetailLine.getJournalAmount() != null) {
                    detailLinesTotalJournalAmountSum = detailLinesTotalJournalAmountSum.add(standardAccountingDetailLine.getJournalAmount());
                } else {
                    String fileValidationError = MessageFormat.format(getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_DETAIL_JOURNAL_AMOUNT_NULL_DETECTED), standardAccountingDetailLine.getEmployeeId(), standardAccountingDetailLine.getCashAdvanceKey());
                    reportData.getHeaderValidationErrors().add(fileValidationError);
                    LOG.error("calculateFileDetailsJournalAmountTotal: " + fileValidationError);
                }
            }
            return detailLinesTotalJournalAmountSum;
        }
    }

    private boolean cashAdvanceKeyIsNotInListOfUniqueCashAdvanceKeys(String cashAdvanceIdInQuestion, List<String> uniqueCashAdvanceKeys) {
        boolean cashAdvanceIdNotFound = true;
        ListIterator<String> uniqueCashAdvanceIdsIterator = uniqueCashAdvanceKeys.listIterator();
        while (uniqueCashAdvanceIdsIterator.hasNext() && cashAdvanceIdNotFound) {
            if (StringUtils.equals(uniqueCashAdvanceIdsIterator.next(), cashAdvanceIdInQuestion)) {
                cashAdvanceIdNotFound = false;
            }
        }
        return cashAdvanceIdNotFound;
    }

    private boolean requestedCashAdvanceAccountingInformationIsValid(ConcurStandardAccountingExtractDetailLine detailFileLine) {
        ConcurAccountInfo concurAccountInfo =
            new ConcurAccountInfo(detailFileLine.getEmployeeChart(), detailFileLine.getEmployeeAccountNumber(), StringUtils.EMPTY,
                                  getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.DEFAULT_TRAVEL_REQUEST_OBJECT_CODE),
                                  StringUtils.EMPTY, StringUtils.EMPTY);
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(concurAccountInfo);
        if (validationResults.isNotValid()) {
            detailFileLine.getValidationResult().addErrorMessages(validationResults.getErrorMessages());
        }
        detailFileLine.getValidationResult().setCashAdvanceAccountingDataValid(validationResults.isValid());
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

    public ConcurStandardAccountingExtractCashAdvanceService getConcurStandardAccountingExtractCashAdvanceService() {
        return concurStandardAccountingExtractCashAdvanceService;
    }

    public void setConcurStandardAccountingExtractCashAdvanceService(
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService) {
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
    }

}
