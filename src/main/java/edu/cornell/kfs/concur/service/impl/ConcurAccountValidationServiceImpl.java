package edu.cornell.kfs.concur.service.impl;

import java.text.MessageFormat;
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
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.mo.common.active.Inactivatable;

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
    protected ConfigurationService configurationService; 

    @Override
    public ValidationResult validateConcurAccountInfo(ConcurAccountInfo concurAccountInfo) {
        return checkAccountingString(
                concurAccountInfo.getChart(),
                concurAccountInfo.getAccountNumber(),
                concurAccountInfo.getSubAccountNumber(),
                concurAccountInfo.getObjectCode(),
                concurAccountInfo.getSubObjectCode(),
                concurAccountInfo.getProjectCode());
    }

    public ValidationResult checkAccountingString(String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode) {
        ValidationResult requiredFieldsValidationResult = checkRequiredAccountInfo(chartOfAccountsCode, accountNumber, objectCode);

        if (requiredFieldsValidationResult.isNotValid()) {
            return requiredFieldsValidationResult;
        } else {
            return checkValuesAreValid(chartOfAccountsCode, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode);       
        }
    }
    
    private ValidationResult checkValuesAreValid(String chartOfAccountsCode, String accountNumber, String subAccountNumber, String objectCode, String subObjectCode, String projectCode){
        ValidationResult accountValidationResult = checkAccount(chartOfAccountsCode, accountNumber);
        if (accountValidationResult.isNotValid()) {
            return accountValidationResult;
        } else {           
            ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());
            updateValidationResultAndAddErrorMessages(validationResult, checkObjectCode(chartOfAccountsCode, objectCode));
            updateValidationResultAndAddErrorMessages(validationResult, checkSubAccount(chartOfAccountsCode, accountNumber, subAccountNumber));
            updateValidationResultAndAddErrorMessages(validationResult, checkSubObjectCode(chartOfAccountsCode, accountNumber, objectCode, subObjectCode));
            updateValidationResultAndAddErrorMessages(validationResult, checkProjectCode(projectCode));            
            return validationResult;
        }
    }
    
    private void updateValidationResultAndAddErrorMessages(ValidationResult validationResult, ValidationResult specificCheckResult){
        if (specificCheckResult.isNotValid()) {
            validationResult.setValid(false);
            validationResult.addMessages(specificCheckResult.getMessages());
        }
    }

    public ValidationResult checkRequiredAccountInfo(String chartOfAccountsCode, String accountNumber, String objectCode) {
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());
        if (chartOfAccountsCode == null || chartOfAccountsCode.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.CHART));           
        } 
        if (accountNumber == null || accountNumber.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER));
        }
        if (objectCode == null || objectCode.isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE));
        }

        return validationResult;
    }

    public ValidationResult checkAccount(String chartOfAccountsCode, String accountNumber) {
        Account account = accountService.getByPrimaryId(chartOfAccountsCode, accountNumber);
        return checkMissingOrInactive(account, MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER), MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER));
    }
    
    private ValidationResult checkMissingOrInactive(Inactivatable inactivatableObject, String missingMessage, String inactiveMessage){
        ValidationResult validationResult = new ValidationResult(true, new ArrayList<String>());
        if (inactivatableObject == null || inactivatableObject.toString().isEmpty()) {
            validationResult.setValid(false);
            validationResult.addMessage(missingMessage);
        } else if (!inactivatableObject.isActive()) {
            validationResult.setValid(false);
            validationResult.addMessage(inactiveMessage);
        }       
        return validationResult;
    }

    public ValidationResult checkObjectCode(String chartOfAccountsCode, String objectCodeParm) {
        ObjectCode objectCode = objectCodeService.getByPrimaryIdForCurrentYear(chartOfAccountsCode, objectCodeParm);   
        return checkMissingOrInactive(objectCode, MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE), MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.OBJECT_CODE));       
    }

    public ValidationResult checkSubAccount(String chartOfAccountsCode, String accountNumber, String subAccountNumber) {
        SubAccount subAccount = subAccountService.getByPrimaryId(chartOfAccountsCode, accountNumber, subAccountNumber);
        return checkMissingOrInactive(subAccount, MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER), MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER));
    }

    public ValidationResult checkSubObjectCode(String chartOfAccountsCode, String accountNumber, String objectCode, String subObjectCodeParm) {
        SubObjectCode subObjectCode = subObjectCodeService.getByPrimaryIdForCurrentYear(chartOfAccountsCode, accountNumber, objectCode, subObjectCodeParm);
        return checkMissingOrInactive(subObjectCode, MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE), MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE));
    }

    public ValidationResult checkProjectCode(String projectCodeParm) {
        ProjectCode projectCode = projectCodeService.getByPrimaryId(projectCodeParm);
        return checkMissingOrInactive(projectCode, MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE), MessageFormat.format(configurationService.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE), ConcurConstants.AccountingStringFieldNames.PROJECT_CODE)); 
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
