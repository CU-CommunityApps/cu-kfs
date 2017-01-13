package edu.cornell.kfs.concur.service.impl;

import java.util.ArrayList;

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

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;
import edu.cornell.kfs.concur.businessobjects.ValidationResult;
import edu.cornell.kfs.concur.service.ConcurAccountValidationService;

public class ConcurAccountValidationServiceImpl implements ConcurAccountValidationService {
    protected AccountService accountService;
    protected ObjectCodeService objectCodeService;
    protected SubAccountService subAccountService;
    protected SubObjectCodeService subObjectCodeService;
    protected ProjectCodeService projectCodeService;

    @Override
    public ValidationResult validateConcurAccountInfo(ConcurAccountInfo concurAccountInfo) {
        ValidationResult validationResult = checkAccountingString(
                concurAccountInfo.getChart(),
                concurAccountInfo.getAccountNumber(),
                concurAccountInfo.getSubAccountNumber(),
                concurAccountInfo.getObjectCode(),
                concurAccountInfo.getSubObjectCode(),
                concurAccountInfo.getProjectCode());
        return validationResult;
    }

    public ValidationResult checkAccountingString(String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode) {
        ValidationResult requiredFieldsValidationResult = checkRequiredAccountInfo(chartOfAccountsCode, accountNumber, objectCode);

        if (requiredFieldsValidationResult.isNotValid()) {
            return requiredFieldsValidationResult;
        } else {
            ValidationResult accountValidationResult = checkAccount(chartOfAccountsCode, accountNumber);
            if (accountValidationResult.isNotValid()) {
                return accountValidationResult;
            } else {
                ValidationResult objectCodeValidationResult = checkObjectCode(chartOfAccountsCode, objectCode);
                ValidationResult subAccountValidationResult = checkSubAccount(chartOfAccountsCode, accountNumber, subAccountNumber);
                ValidationResult subObjectCodeValidationResult = checkSubObjectCode(chartOfAccountsCode, accountNumber, objectCode, subObjectCode);
                ValidationResult projectCodeValidationResult = checkProjectCode(projectCode);               
                ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());

                if (objectCodeValidationResult.isNotValid()) {
                    validationResult.setValid(false);
                    validationResult.addMessages(objectCodeValidationResult.getMessages());
                }

                if (subAccountValidationResult.isNotValid()) {
                    validationResult.setValid(false);
                    validationResult.addMessages(subAccountValidationResult.getMessages());
                }

                if (subObjectCodeValidationResult.isNotValid()) {
                    validationResult.setValid(false);
                    validationResult.addMessages(subObjectCodeValidationResult.getMessages());
                }

                if (projectCodeValidationResult.isNotValid()) {
                    validationResult.setValid(false);
                    validationResult.addMessages(projectCodeValidationResult.getMessages());
                }

                return validationResult;
            }
        }
    }

    public ValidationResult checkRequiredAccountInfo(String chartOfAccountsCode, String accountNumber, String objectCode) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());
        if (chartOfAccountsCode == null || chartOfAccountsCode.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_CHART_OF_ACCTS_REQUIRED);
            return validationResult;
        } else if (accountNumber == null || accountNumber.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_NBR_REQUIRED);
            return validationResult;
        } else if (objectCode == null || objectCode.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_REQUIRED);
            return validationResult;
        }

        return validationResult;
    }

    public ValidationResult checkAccount(String chartOfAccountsCode, String accountNumber) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());

        Account account = accountService.getByPrimaryId(chartOfAccountsCode, accountNumber);

        if ( account == null || account.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_DOES_NOT_EXIST);
        } else if (!account.isActive()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_INACTIVE);
        } else if (account.isClosed()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_ACCT_CLOSED);
        }
        return validationResult;
    }

    public ValidationResult checkObjectCode(String chartOfAccountsCode, String objectCodeParm) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());
        
        ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(chartOfAccountsCode, objectCodeParm);
    
        if (objectCode == null || objectCode.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_DOES_NOT_EXIST);
        } else if (!objectCode.isActive()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_OBJ_CD_INACTIVE);
        }
    
        return validationResult;
    }

    public ValidationResult checkSubAccount(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());

        SubAccount subAccount = subAccountService.getByPrimaryId(chartOfAccountsCode, accountNumber, subAccountNumber);

        if (subAccount == null || subAccount.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_DOES_NOT_EXIST);
        } else if (!subAccount.isActive()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_ACCT_INACTIVE);
        }
        return validationResult;
    }

    public ValidationResult checkSubObjectCode(String chartOfAccountsCode, String accountNumber, String objectCode, String subObjectCodeParm) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());

        SubObjectCode subObjectCode = subObjectCodeService.getByPrimaryIdForCurrentYear(chartOfAccountsCode, accountNumber, objectCode, subObjectCodeParm);

        if (subObjectCode == null || subObjectCode.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_DOES_NOT_EXIST);
        } else if (!subObjectCode.isActive()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_SUB_OBJ_CD_INACTIVE);
        }
        return validationResult;
    }

    public ValidationResult checkProjectCode(String projectCodeParm) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());

        ProjectCode projectCode = projectCodeService.getByPrimaryId(projectCodeParm);

        if (projectCode == null || projectCode.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_DOES_NOT_EXIST);
        } else if ((!projectCode.isActive())) {
            validationResult.setValid(false);
            validationResult.addMessage(ConcurConstants.AccountingStringValidationErrorMessages.ERROR_PRJ_CD_INACTIVE);
        }
        return validationResult;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public ObjectCodeService getObjectCodeService() {
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

    public SubAccountService getSubAccountService() {
        return subAccountService;
    }

    public void setSubAccountService(SubAccountService subAccountService) {
        this.subAccountService = subAccountService;
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
