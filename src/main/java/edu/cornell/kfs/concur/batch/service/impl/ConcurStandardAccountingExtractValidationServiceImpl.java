package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurEmployeeInfoValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurStandardAccountingExtractValidationServiceImpl implements ConcurStandardAccountingExtractValidationService {
	private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class);
    
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurEmployeeInfoValidationService concurEmployeeInfoValidationService;
    protected ConfigurationService configurationService;
    
    @Override
    public boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile,
                                                            ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateDetailCount(concurStandardAccountingExtractFile, reportData);
        valid = validateAmountsAndDebitCreditCode(concurStandardAccountingExtractFile, reportData) && valid;
        valid = validateHeaderDate(concurStandardAccountingExtractFile.getBatchDate(), reportData) && valid;
        if (LOG.isInfoEnabled() && valid) {
            LOG.info("validateConcurStandardAccountExtractFile, passed file level validation, the record counts, batch date, and journal totals are all correct.");
        }
        return valid;
    }

    @Override
    public boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile,
                                       ConcurStandardAccountingExtractBatchReportData reportData) {
        Integer numberOfDetailsInHeader = concurStandardAccountingExtractFile.getRecordCount();
        int actualNumberOfDetails = concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().size();
        
        boolean valid = numberOfDetailsInHeader.intValue() == actualNumberOfDetails;
        
        if (valid) {
            LOG.debug("validateDetailCount, Number of detail lines is what we expected: " + actualNumberOfDetails);
        } else {
            String validationError = "The header said there were (" + numberOfDetailsInHeader +
                    ") detail lines expected, but the actual number of details were (" + actualNumberOfDetails + ")";
            reportData.addHeaderValidationError(validationError);
            LOG.error("validateDetailCount, " + validationError);
        }
        
        return valid;
    }

    @Override
    public boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile,
                                                     ConcurStandardAccountingExtractBatchReportData reportData) {
        KualiDecimal journalTotal = concurStandardAccountingExtractFile.getJournalAmountTotal();
        KualiDecimal detailTotal = KualiDecimal.ZERO;
        boolean debitCreditValid = true;
        boolean employeeGroupIdValid = true;
        boolean journalTotalValidation = true;
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
            if (line.getJournalAmount() != null) {
                detailTotal = detailTotal.add(line.getJournalAmount());
            } else {
                String validationError = "Parsed a null KualiDecimal from the original value of " + line.getJournalAmountString();
                reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationError));
                LOG.error("validateAmountsAndDebitCreditCode, " + validationError);
                journalTotalValidation = false;
            }
            debitCreditValid &= validateDebitCreditField(line, reportData);
            employeeGroupIdValid &= validateEmployeeGroupId(line, reportData);
        }
        
        journalTotalValidation = journalTotalValidation && detailTotal.equals(journalTotal);
        if (journalTotalValidation) {
            LOG.debug("validateAmounts, journal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotal.doubleValue() + " do match.");
        } else {
            String validationError = "The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")";
            reportData.addHeaderValidationError(validationError);
            LOG.error("validateAmounts, " + validationError);
        }
        return journalTotalValidation && debitCreditValid && employeeGroupIdValid;
    }

    @Override
    public boolean validateDebitCreditField(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = StringUtils.equalsIgnoreCase(line.getJournalDebitCredit(), ConcurConstants.CREDIT) || 
                        StringUtils.equalsIgnoreCase(line.getJournalDebitCredit(), ConcurConstants.DEBIT);
        if (valid) {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        } else {
            String validationError = "Invalid debit or credit: " + line.getJournalDebitCredit();
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationError));
            LOG.error("validateDebitCreditField, " + validationError);
        }
        return valid;
    }
    
    @Override
    public boolean validateEmployeeGroupId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = concurEmployeeInfoValidationService.isEmployeeGroupIdValid(line.getEmployeeGroupId());      
        if (valid) {
            LOG.debug("Found a valid employee group id.");
        } else {
            String validationError = ("Found an invalid employee group id: " + line.getEmployeeGroupId() + ".  We expected the group ID to be in this list:" + concurBatchUtilityService.getConcurParameterValue(ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID));
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationError));
            LOG.error("validateEmployeeGroupId, " + validationError);
        }
        return valid;
    }

    @Override
    public boolean validateDate(Date date) {
        boolean valid = date != null;
        if (valid) {
            LOG.debug("validateDate, found a valid date: " + date);
        } else {
            LOG.error("validateDate, found a a null date.");
        }
        return valid;
    }

    protected boolean validateHeaderDate(Date date, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateDate(date);
        if (!valid) {
            reportData.addHeaderValidationError("Header row contains an invalid date.");
        }
        return valid;
    }
    
    @Override
    public boolean validateEmployeeId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = getConcurEmployeeInfoValidationService().validPerson(line.getEmployeeId());
        if (!valid) {
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, 
                    getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS)));
            LOG.error("validateEmployeeId, Found a an invalid employee ID: " + line.getEmployeeId());
        }
        return valid;
    }
    
    @Override
    public boolean validateConcurStandardAccountingExtractDetailLineForCollector(ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateConcurStandardAccountingExtractDetailLineBase(line, reportData, false);
        valid = validateAccountingLine(line, reportData) && valid;
        return valid;
    }

    @Override
    public boolean validateConcurStandardAccountingExtractDetailLineWithObjectCodeOverrideForPdp(ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractBatchReportData reportData, String overriddenObjectCode, String overriddenSubObjectCode) {
        boolean valid = validateConcurStandardAccountingExtractDetailLineBase(line, reportData, true);
        valid = validateAccountingLineWithObjectCodeOverrides(line, reportData, overriddenObjectCode, overriddenSubObjectCode) && valid;
        return valid;
    }
    
    private boolean validateConcurStandardAccountingExtractDetailLineBase(ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractBatchReportData reportData, boolean validateAddress) {
        boolean valid = validateReportId(line, reportData);
        valid = validateEmployeeId(line, reportData) && valid;
        if (validateAddress) {
            valid &= validateAddressIfCheckPayment(line, reportData);
        }
        return valid;
    }
    
    private boolean validateAddressIfCheckPayment(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = true;
        String validationMessage = getConcurEmployeeInfoValidationService().getAddressValidationMessageIfCheckPayment(line.getEmployeeId());
        if (StringUtils.isNotBlank(validationMessage)) {
            valid = false;
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationMessage));
        }
        return valid;
    }
    
    private boolean validateReportId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = StringUtils.isNotBlank(line.getReportId());
        if (valid) {
            LOG.debug("validateReportId, found a valid report ID: " + line.getReportId());
        } else {
            String validationError = "No valid report ID.";
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationError));
            LOG.error("validateReportId, " + validationError);
        }
        return valid;
    }
    
    private boolean validateAccountingLine(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        if (!getConcurStandardAccountingExtractCashAdvanceService().isCashAdvanceToBeAppliedToReimbursement(line)) {
            ConcurAccountInfo accountingInformation = buildConcurAccountingInformation(line);
            ValidationResult validationResults = buildValidationResult(accountingInformation, false);
            if (validationResults.isNotValid()) {
                reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line, validationResults.getErrorMessages()));
            }
            return validationResults.isValid();
        } else {
            LOG.debug("validateAccountingLine, found a cash advance line, no need to validate");
            return true;
        }
    }

    private boolean validateAccountingLineWithObjectCodeOverrides(ConcurStandardAccountingExtractDetailLine line, 
            ConcurStandardAccountingExtractBatchReportData reportData, String overriddenObjectCode, String overriddenSubObjectCode) {
        if (!getConcurStandardAccountingExtractCashAdvanceService().isCashAdvanceToBeAppliedToReimbursement(line)) {
            logErrorsWithOriginalAccountingDetails(line);
    
            ValidationResult overriddenValidationResults = buildValidationResult(
                    buildConcurAccountingInformation(line, overriddenObjectCode, overriddenSubObjectCode), true);
    
            if (overriddenValidationResults.isNotValid()) {
                reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(
                        line, overriddenValidationResults.getErrorMessages()));
            }

            return overriddenValidationResults.isValid();
        } else {
            LOG.debug("validateAccountingLineWithObjectCodeOverrides, found a cash advance line, no need to validate");
            return true;
        }
    }

    private void logErrorsWithOriginalAccountingDetails(ConcurStandardAccountingExtractDetailLine line) {
        ConcurAccountInfo accountingInformation = buildConcurAccountingInformation(line);
        buildValidationResult(accountingInformation, false);
    }
    
    private ConcurAccountInfo buildConcurAccountingInformation(ConcurStandardAccountingExtractDetailLine line) {
        String objectCode = line.getJournalAccountCode();
        String subObjectCode;
        if (getConcurBatchUtilityService().lineRepresentsPersonalExpenseChargedToCorporateCard(line)) {
            subObjectCode = StringUtils.EMPTY;
        } else {
            subObjectCode = line.getSubObjectCode();
        }
        ConcurAccountInfo accountingInformation = buildConcurAccountingInformation(line, objectCode, subObjectCode);
        return accountingInformation;
    }
    
    private ConcurAccountInfo buildConcurAccountingInformation(ConcurStandardAccountingExtractDetailLine line, String objectCode, String subObjectCode) {
        if (getConcurBatchUtilityService().lineRepresentsPersonalExpenseChargedToCorporateCard(line)) {
            return new ConcurAccountInfo(line.getReportChartOfAccountsCode(), line.getReportAccountNumber(), 
                    line.getReportSubAccountNumber(), objectCode, subObjectCode, line.getReportProjectCode());
        } else {
            return new ConcurAccountInfo(line.getChartOfAccountsCode(), line.getAccountNumber(), 
                    line.getSubAccountNumber(), objectCode, subObjectCode, line.getProjectCode());
        }
    }
    
    private ValidationResult buildValidationResult(ConcurAccountInfo accountingInfo, boolean isOverriddenInfo) {
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(accountingInfo);
        if (validationResults.isNotValid()) {
            String overriddenOrOriginal = isOverriddenInfo ? "overridden" : "original";
            String messageStarter = "buildValidationResult, the " + overriddenOrOriginal + " acounting validation results: "; 
            String formattedString = validationResults.isValid() ? validationResults.getAccountDetailMessagesAsOneFormattedString() : validationResults.getErrorMessagesAsOneFormattedString();
            LOG.info(messageStarter + formattedString);
        }
        return validationResults;
    }

    public ConcurAccountValidationService getConcurAccountValidationService() {
        return concurAccountValidationService;
    }

    public void setConcurAccountValidationService(ConcurAccountValidationService concurAccountValidationService) {
        this.concurAccountValidationService = concurAccountValidationService;
    }

    public ConcurStandardAccountingExtractCashAdvanceService getConcurStandardAccountingExtractCashAdvanceService() {
        return concurStandardAccountingExtractCashAdvanceService;
    }

    public void setConcurStandardAccountingExtractCashAdvanceService(ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService) {
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
