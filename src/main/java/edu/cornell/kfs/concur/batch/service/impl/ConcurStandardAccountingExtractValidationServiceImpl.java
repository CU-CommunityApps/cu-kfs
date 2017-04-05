package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
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
    protected PersonService personService;
    
    @Override
    public boolean validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile,
                                                            ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateDetailCount(concurStandardAccountingExtractFile, reportData);
        valid = validateAmountsAndDebitCreditCode(concurStandardAccountingExtractFile, reportData) && valid;
        valid = validateHeaderDate(concurStandardAccountingExtractFile.getBatchDate(), reportData) && valid;
        if (LOG.isDebugEnabled() && valid) {
            LOG.debug("validateConcurStandardAccountExtractFile, passed file level validation, the record counts, batch date, and journal totals are all correct.");
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
                reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), 
                        line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), validationError));
                LOG.error("validateAmountsAndDebitCreditCode, " + validationError);
                journalTotalValidation = false;
            }
            debitCreditValid &= validateDebitCreditField(line, reportData);
            employeeGroupIdValid &= validateEmployeeGroupId(line, reportData);
        }
        
        journalTotalValidation = journalTotalValidation && journalTotal.equals(detailTotal);
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
        boolean valid = StringUtils.equalsIgnoreCase(line.getJounalDebitCredit(), ConcurConstants.CREDIT) || 
                        StringUtils.equalsIgnoreCase(line.getJounalDebitCredit(), ConcurConstants.DEBIT);
        if (valid) {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        } else {
            String validationError = "Invalid debit or credit: " + line.getJounalDebitCredit();
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), validationError));
            LOG.error("validateDebitCreditField, " + validationError);
        }
        return valid;
    }
    
    @Override
    public boolean validateEmployeeGroupId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        String expectedGroupId = findEmployeeGroupId();
        boolean valid = StringUtils.equalsIgnoreCase(line.getEmployeeGroupId(), expectedGroupId);
        if (valid) {
            LOG.debug("Found a valid employee group id.");
        } else {
            String validationError = ("Found an invalid employee group id: " + line.getEmployeeGroupId() + ".  We expected the group ID to be " + expectedGroupId);
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), validationError));
            LOG.error("validateEmployeeGroupId, " + validationError);
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

    protected boolean validateHeaderDate(Date date, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateDate(date);
        if (!valid) {
            reportData.addHeaderValidationError("Header row contains an invalid date.");
        }
        return valid;
    }
    
    @Override
    public boolean validateEmployeeId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        Person employee = findPerson(line.getEmployeeId());
        boolean valid = ObjectUtils.isNotNull(employee);
        if (valid) {
            LOG.debug("validateEmployeeId, found a valid employee: " + employee.getName());
        } else {
            String validationError = "Found a an invalid employee ID: " + line.getEmployeeId();
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), validationError));
            LOG.error("validateEmployeeId, " + validationError);
        }
        return valid;
    }
    
    private Person findPerson(String employeeId) {
        if (StringUtils.isNotBlank(employeeId)) {
            try {
                Person employee = getPersonService().getPersonByEmployeeId(employeeId);
                return employee;
            } catch (Exception e) {
                LOG.error("findPerson, Unable to create a person from employee ID: " + employeeId, e);
            }
        }
        return null;
    }
    
    @Deprecated
    public boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line) {
        ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
        return validateConcurStandardAccountingExtractDetailLine(line, reportData);
    }

    @Override
    public boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = validateReportId(line, reportData);
        valid = validateAccountingLine(line, reportData) && valid;
        valid = validateEmployeeId(line, reportData) && valid;
        return valid;
    }
    
    private boolean validateReportId(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean valid = StringUtils.isNotBlank(line.getReportId());
        if (valid) {
            LOG.debug("validateReportId, found a valid report ID: " + line.getReportId());
        } else {
            String validationError = "No valid report ID.";
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), validationError));
            LOG.error("validateReportId, " + validationError);
        }
        return valid;
    }

    private boolean validateAccountingLine(ConcurStandardAccountingExtractDetailLine line, ConcurStandardAccountingExtractBatchReportData reportData) {
        reportErrorsWithOriginalAccountingDetails(line);
        String overriddenObjectCode = getParameterService().getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.CONCUR_SAE_PDP_DEFAULT_OBJECT_CODE);
        ConcurAccountInfo overriddenConcurAccountingInformation = new ConcurAccountInfo(line.getChartOfAccountsCode(), line.getAccountNumber(), 
                line.getSubAccountNumber(), overriddenObjectCode, StringUtils.EMPTY, line.getProjectCode());
        ValidationResult overriddenValidationResults = buildValidationResult(overriddenConcurAccountingInformation, true);

        if (overriddenValidationResults.isNotValid()) {
            reportData.addValidationErrorFileLine(new ConcurBatchReportLineValidationErrorItem(line.getReportId(), line.getEmployeeId(), line.getEmployeeLastName(), line.getEmployeeFirstName(), line.getEmployeeMiddleInitital(), overriddenValidationResults.getMessages()));
        }

        return overriddenValidationResults.isValid();
    }

    private void reportErrorsWithOriginalAccountingDetails(ConcurStandardAccountingExtractDetailLine line) {
        ConcurAccountInfo accountingInformation = new ConcurAccountInfo(line.getChartOfAccountsCode(), line.getAccountNumber(), 
                line.getSubAccountNumber(), line.getJournalAccountCode(), line.getSubObjectCode(), line.getProjectCode());
        buildValidationResult(accountingInformation, false);
    }
    
    private ValidationResult buildValidationResult(ConcurAccountInfo accountingInfo, boolean isOverriddenInfo) {
        ValidationResult validationResults = getConcurAccountValidationService().validateConcurAccountInfo(accountingInfo);
        if (validationResults.isNotValid()) {
            String overriddenOrOriginal = isOverriddenInfo ? "overridden" : "original";
            String messageStarter = "buildValidationResult, the " + overriddenOrOriginal + " acounting validation results: "; 
            LOG.info(messageStarter + validationResults.getErrorMessagesAsOneFormattedString());
        }
        return validationResults;
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

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
