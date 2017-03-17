package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurStandardAccountingExtractValidationServiceImpl implements ConcurStandardAccountingExtractValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class);
    
    protected ConcurAccountValidationService concurAccountValidationService;
    protected ParameterService parameterService;
    
    @Override
    public boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        boolean valid = validateDetailCount(concurStandardAccountingExtractFile);
        valid = validateAmountsAndDebitCreditCode(concurStandardAccountingExtractFile) && valid;
        valid = validateDate(concurStandardAccountingExtractFile.getBatchDate()) && valid;
        if (LOG.isDebugEnabled() && valid) {
            LOG.debug("validateConcurStandardAccountExtractFile, passed file level validation, the record counts, batch date, and journal totals are all correct.");
        }
        return valid;
    }

    @Override
    public boolean validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        Integer numberOfDetailsInHeader = concurStandardAccountingExtractFile.getRecordCount();
        int actualNumberOfDetails = concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines().size();
        
        boolean valid = numberOfDetailsInHeader.intValue() == actualNumberOfDetails;
        
        if (valid) {
            LOG.debug("validateDetailCount, Number of detail lines is what we expected: " + actualNumberOfDetails);
        } else {
            LOG.error("validateDetailCount, The header said there were (" + numberOfDetailsInHeader + 
                    ") detail lines expected, but the actual number of details were (" + actualNumberOfDetails + ")");
        }
        
        return valid;
    }

    @Override
    public boolean validateAmountsAndDebitCreditCode(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        KualiDecimal journalTotal = concurStandardAccountingExtractFile.getJournalAmountTotal();
        KualiDecimal detailTotal = KualiDecimal.ZERO;
        boolean debitCreditValid = true;
        boolean employeeGroupIdValid = true;
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile
                .getConcurStandardAccountingExtractDetailLines()) {
            detailTotal = detailTotal.add(line.getJournalAmount());
            debitCreditValid &= validateDebitCreditField(line.getJounalDebitCredit());
            employeeGroupIdValid &= validateEmployeeGroupId(line.getEmployeeGroupId());
        }
        
        boolean journalTotalValidation = journalTotal.equals(detailTotal);
        if (journalTotalValidation) {
            LOG.debug("validateAmounts, journal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotal.doubleValue() + " do match.");
        } else {
            LOG.error("validateAmounts, The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")");
        }
        return journalTotalValidation && debitCreditValid && employeeGroupIdValid;
    }

    @Override
    public boolean validateDebitCreditField(String debitCredit) {
        boolean valid = StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.CREDIT) || 
                StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.DEBIT);
        if (valid) {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        } else {
            LOG.error("validateDebitCreditField, invalid debit or credit: " + debitCredit);
        }
        return valid;
    }
    
    @Override
    public boolean validateEmployeeGroupId(String employeeGroupId) {
        String expectedGroupId = findEmployeeGroupId();
        boolean valid = StringUtils.equalsIgnoreCase(employeeGroupId, expectedGroupId);
        if (valid) {
            LOG.debug("Found a valid employee group id.");
        } else {
            LOG.error("Found an invalid employee group id: " + employeeGroupId + ".  We expected the group ID to be " + expectedGroupId);
        }
        return valid;
    }
    
    protected String findEmployeeGroupId() {
        String expectedGroupId = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.CONCUR_CUSTOMER_PROFILE_GROUP_ID);
        return expectedGroupId;
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
    
    @Override
    public boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line) {
        boolean valid = validateReportId(line.getReportId());
        valid = validateAccountingLine(line) && valid;
        return valid;
    }
    
    private boolean validateReportId(String reportId) {
        boolean valid = StringUtils.isNotEmpty(reportId);
        if (valid) {
            LOG.debug("validateReportId, found a valid report ID: " + reportId);
        } else {
            LOG.error("validateReportId, no valid report ID");
        }
        return valid;
    }

    private boolean validateAccountingLine(ConcurStandardAccountingExtractDetailLine line) {
        ConcurAccountInfo concurAccountInfo = new ConcurAccountInfo(line.getChartOfAccountsCode(), line.getAccountNumber(), 
                line.getSubAccountNumber(), line.getJournalAccountCode(), line.getSubObjectCode(), line.getProjectCode(), line.getOrgRefId());
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(concurAccountInfo);
        LOG.info("validateAccountingInformation, the accounting validation results: " + validationResults.getErrorMessagesAsOneFormattedString());
        return validationResults.isValid();
    }

    public ConcurAccountValidationService getConcurAccountValidationService() {
        return concurAccountValidationService;
    }

    public void setConcurAccountValidationService(ConcurAccountValidationService concurAccountValidationService) {
        this.concurAccountValidationService = concurAccountValidationService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
