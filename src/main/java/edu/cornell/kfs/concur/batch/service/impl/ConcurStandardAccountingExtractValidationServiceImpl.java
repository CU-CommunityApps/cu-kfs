package edu.cornell.kfs.concur.batch.service.impl;

import java.sql.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import com.ctc.wstx.util.StringUtil;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractValidationServiceImpl implements ConcurStandardAccountingExtractValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractValidationServiceImpl.class);
    
    protected AccountService accountService;
    protected SubAccountService subAccountService;
    protected ObjectCodeService objectCodeService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;
    
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
        boolean debbitCreditValid = true;
        
        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile
                .getConcurStandardAccountingExtractDetailLines()) {
            detailTotal = detailTotal.add(line.getJournalAmount());
            debbitCreditValid &= validateDebitCreditField(line.getJounalDebitCredit());
        }
        
        boolean journalTotalValidation = journalTotal.equals(detailTotal);
        if (journalTotalValidation) {
            LOG.debug("validateAmounts, journal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotal.doubleValue() + " do match.");
        } else {
            LOG.error("validateAmounts, The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")");
        }
        return journalTotalValidation && debbitCreditValid;
    }

    @Override
    public boolean validateDebitCreditField(String debitCredit) {
        boolean valid = StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.CREDIT) || 
                StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.DEBIT);
        
        if (valid) {
            LOG.debug("validateDebitCreditField, found a valid debit/credit.");
        } else {
            LOG.error("validateDebitCreditField, invalid debit or credit: " + debitCredit);
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
    
    @Override
    public boolean validateConcurStandardAccountingExtractDetailLine(ConcurStandardAccountingExtractDetailLine line) {
        boolean valid = validateReportId(line.getReportId());
        valid = validateAccountingInformation(line) && valid;
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
    
    private boolean validateAccountingInformation(ConcurStandardAccountingExtractDetailLine line) {
        boolean valid = validateRequiredElementsExists(line);
        valid &= validdateChartAndAccountAndSubAccount(line.getChartOfAccountsCode(), line.getAccountNumber(), line.getSubAccountNumber());
        valid &= validObjectCodeAndSubObjectCode(line.getChartOfAccountsCode(), line.getAccountNumber(), line.getJournalAccountCode(), line.getSubObjectCode());
        valid &= validateProjectCode(line.getProjectCode());
        return valid;
    }
    
    private boolean validateRequiredElementsExists(ConcurStandardAccountingExtractDetailLine line) {
        boolean valid = StringUtils.isNotBlank(line.getAccountNumber()) && 
                StringUtils.isNotBlank(line.getChartOfAccountsCode()) &&
                StringUtils.isNotBlank(line.getJournalAccountCode());
        if (valid) {
            LOG.debug("validateRequiredElementsExists, found a chart, account, and object code");
        } else {
            LOG.error("validateRequiredElementsExists, chart, account, or object code was empty.");
        }
        return valid;
    }
    
    private boolean validdateChartAndAccountAndSubAccount(String chartCode, String accountNumber, String subAccountNumber) {
        boolean valid = true;
        Account account = getAccountService().getByPrimaryId(chartCode, accountNumber);
        if (account == null || !account.isActive()) {
            LOG.error("validdateChartAndAccountAndSubAccount, found an invalid chart '" + chartCode + "' and account number '" + accountNumber + "'");
            valid = false;
        } else {
            LOG.debug("validdateChartAndAccountAndSubAccount, valid chart and account number");
            if (StringUtils.isNotBlank(subAccountNumber)) {
                SubAccount subAccount = getSubAccountService().getByPrimaryId(chartCode, accountNumber, subAccountNumber);
                if (subAccount == null || !subAccount.isActive()) {
                    LOG.error("validdateChartAndAccountAndSubAccount, found an invalid chart '" + chartCode + "' and account number '" + 
                            accountNumber + "' and sub account number '" + subAccountNumber + "'");
                    valid = false;
                } else {
                    LOG.debug("validdateChartAndAccountAndSubAccount, found a valid sub account number");
                }
            }
        }
        return valid;
    }
    
    private boolean validObjectCodeAndSubObjectCode(String chartCode, String accountNumber, String financialObjectCode, String subObjectCode) {
        boolean valid = true;
        ObjectCode objectCode = getObjectCodeService().getByPrimaryIdForCurrentYear(chartCode, financialObjectCode);
        if (objectCode == null || !objectCode.isActive()) {
            LOG.error("validObjectCodeAndSubObjectCode, found an invalid chart '" + chartCode + "' and objectCode '" + financialObjectCode + "'");
            valid = false;
        } else {
            LOG.debug("validObjectCodeAndSubObjectCode, Found a valid sub object");
            if (StringUtils.isNotBlank(subObjectCode)) {
                SubObjectCode subObject = getSubObjectCodeService().getByPrimaryIdForCurrentYear(chartCode, accountNumber, financialObjectCode, subObjectCode);
                if (subObject == null || !subObject.isActive()) {
                    LOG.error("validObjectCodeAndSubObjectCode, found an invalid chart '" + chartCode + "' and objectCode '" + financialObjectCode + 
                            "' and account number '" + accountNumber + "' and sub object code '" + subObjectCode + "'");
                    valid = false;
                } else {
                    LOG.debug("validObjectCodeAndSubObjectCode, found a valid sub object code");
                }
            }
        }
        return valid;
    }
    
    private boolean validateProjectCode(String projectCode) {
        boolean valid = true;
        if (StringUtils.isNotBlank(projectCode)) {
            ProjectCode proj = getProjectCodeService().getByPrimaryId(projectCode);
            if (proj == null || !proj.isActive()) {
                LOG.error("validateProjectCode, found an invalid project code: " + projectCode);
                valid = false;
            } else {
                LOG.debug("validateProjectCode, found a valid project code");
            }
        }
        return valid;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public SubAccountService getSubAccountService() {
        return subAccountService;
    }

    public void setSubAccountService(SubAccountService subAccountService) {
        this.subAccountService = subAccountService;
    }

    public ObjectCodeService getObjectCodeService() {
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public SubObjectCodeService getSubObjectCodeService() {
        return subObjectCodeService;
    }

    public void setSubObjectCodeService(SubObjectCodeService subObjectCodeService) {
        this.subObjectCodeService = subObjectCodeService;
    }

    public ProjectCodeService getProjectCodeService() {
        return projectCodeService;
    }

    public void setProjectCodeService(ProjectCodeService projectCodeService) {
        this.projectCodeService = projectCodeService;
    }

}
