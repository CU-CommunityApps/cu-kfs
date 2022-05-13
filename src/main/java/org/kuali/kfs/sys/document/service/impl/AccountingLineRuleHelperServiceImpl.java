/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.FundGroup;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectSubType;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineOverride;
import org.kuali.kfs.sys.businessobject.OriginationCode;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.document.service.AccountingLineRuleHelperService;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentTypeService;

import java.util.ArrayList;
import java.util.List;

public class AccountingLineRuleHelperServiceImpl implements AccountingLineRuleHelperService {
    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private DataDictionaryService dataDictionaryService;
    private FinancialSystemDocumentTypeService financialSystemDocumentTypeService;
    protected AccountService accountService;
    private ConfigurationService configurationService;

    @Override
    public String getAccountLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(Account.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.ACCOUNT_NUMBER).getShortLabel();
    }

    @Override
    public String getChartLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(Chart.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE).getShortLabel();
    }

    @Override
    public String getFundGroupCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(FundGroup.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.CODE).getShortLabel();
    }

    @Override
    public String getObjectCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(ObjectCode.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.FINANCIAL_OBJECT_CODE).getShortLabel();
    }

    @Override
    public String getObjectSubTypeCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(ObjectSubType.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.CODE).getShortLabel();
    }

    @Override
    public String getObjectTypeCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(ObjectType.class.getName())
                .getAttributeDefinition(KFSConstants.GENERIC_CODE_PROPERTY_NAME).getShortLabel();
    }

    @Override
    public String getOrganizationCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(Organization.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.ORGANIZATION_CODE).getShortLabel();
    }

    @Override
    public String getProjectCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(ProjectCode.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.CODE).getShortLabel();
    }

    @Override
    public String getSubAccountLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(SubAccount.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.SUB_ACCOUNT_NUMBER).getShortLabel();
    }

    @Override
    public String getSubFundGroupCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(SubFundGroup.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.SUB_FUND_GROUP_CODE).getShortLabel();
    }

    @Override
    public String getSubObjectCodeLabel() {
        return businessObjectDictionaryService.getBusinessObjectEntry(SubObjectCode.class.getName())
                .getAttributeDefinition(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE).getShortLabel();
    }

    @Override
    public boolean hasRequiredOverrides(AccountingLine line, String overrideCode) {
        return hasAccountRequiredOverrides(line, overrideCode) && hasObjectBudgetRequiredOverrides(line, overrideCode);
    }

    public boolean hasAccountRequiredOverrides(AccountingLine line, String overrideCode) {
        boolean retVal = true;
        AccountingLineOverride override = AccountingLineOverride.valueOf(overrideCode);
        Account account = line.getAccount();
        if (AccountingLineOverride.needsExpiredAccountOverride(account)
                && !override.hasComponent(AccountingLineOverride.COMPONENT.EXPIRED_ACCOUNT)) {
            Account continuation = accountService.getUnexpiredContinuationAccountOrNull(account);
            // CORNELL FIX: if (continuation == null) {
            if (ObjectUtils.isNull(continuation)) {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER,
                        KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_EXPIRED_NO_CONTINUATION, account.getAccountNumber());
            } else {
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ACCOUNT_NUMBER,
                        KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_EXPIRED, account.getAccountNumber(),
                        continuation.getChartOfAccountsCode(), continuation.getAccountNumber());
            }
            retVal = false;
        }
        return retVal;
    }

    public boolean hasObjectBudgetRequiredOverrides(AccountingLine line, String overrideCode) {
        boolean retVal = true;
        ObjectCode objectCode = line.getObjectCode();
        AccountingLineOverride override = AccountingLineOverride.valueOf(overrideCode);
        Account account = line.getAccount();
        if (AccountingLineOverride.needsObjectBudgetOverride(account, objectCode)
                && !override.hasComponent(AccountingLineOverride.COMPONENT.NON_BUDGETED_OBJECT)) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.FINANCIAL_OBJECT_CODE,
                    KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_PRESENCE_NON_BUDGETED_OBJECT_CODE, account.getAccountNumber(),
                    objectCode.getFinancialObjectCode());
            retVal = false;
        }
        return retVal;
    }

    /**
     * Method moved to AccountService, use method there instead. Same logic.
     */
    @Deprecated
    protected Account getUnexpiredContinuationAccountOrNull(Account account) {
        return accountService.getUnexpiredContinuationAccountOrNull(account);
    }

    private List<AccountingLineValidationError> getExistsActiveValidationErrors(PersistableBusinessObject bo, boolean isActive, boolean useShortMessages, String errorPropertyName, String errorPropertyIdentifyingName, String label) {
        List<AccountingLineValidationError> validationErrors = new ArrayList<>();

        if (ObjectUtils.isNull(bo)) {
            AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        } else if (!isActive) {
            // make sure it's active for usage
            AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_INACTIVE_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_INACTIVE_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        }

        return validationErrors;
    }

    protected void setGlobalErrorMessages(List<AccountingLineValidationError> validationErrors) {
        validationErrors.forEach(error -> {
            GlobalVariables.getMessageMap().putError(error.getPropertyName(), error.getErrorKey(), error.getMessageParameters());
        });
    }

    @Override
    public boolean isValidAccount(String accountIdentifyingPropertyName, Account account) {
        return isValidAccount(account, KFSPropertyConstants.ACCOUNT_NUMBER, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidAccount(Account account, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getAccountValidationErrors(account, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    public List<AccountingLineValidationError> getAccountValidationErrors(Account account, String errorPropertyName, String errorPropertyIdentifyingName, boolean useShortMessages) {
        String label = getAccountLabel();

        List<AccountingLineValidationError> validationErrors = new ArrayList<>();

        if (ObjectUtils.isNull(account)) {
            AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        } else if (!account.isActive() || account.isClosed()) {
            // make sure it's active for usage
            AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_ACCOUNT_CLOSED_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNT_CLOSED_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        }

        return validationErrors;
    }

    @Override
    public boolean isValidChart(String accountIdentifyingPropertyName, Chart chart) {
        return isValidChart(chart, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidChart(Chart chart, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getChartValidationErrors(chart, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getChartValidationErrors(Chart chart, String errorPropertyName, String errorPropertyIdentifyingName, boolean useShortMessages) {
        String label = getChartLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(chart) && chart.isActive();
        return getExistsActiveValidationErrors(chart, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidObjectCode(String accountIdentifyingPropertyName, ObjectCode objectCode) {
        return isValidObjectCode(objectCode, KFSPropertyConstants.FINANCIAL_OBJECT_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidObjectCode(ObjectCode objectCode, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getObjectCodeValidationErrors(objectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getObjectCodeValidationErrors(ObjectCode objectCode, String errorPropertyName,
            String errorPropertyIdentifyingName, boolean useShortMessages) {
        String label = getObjectCodeLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(objectCode) && StringUtils.isNotBlank(objectCode.getCode()) && objectCode.isFinancialObjectActiveCode();
        return getExistsActiveValidationErrors(objectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidObjectTypeCode(String accountIdentifyingPropertyName, ObjectType objectTypeCode) {
        return isValidObjectTypeCode(objectTypeCode, KFSPropertyConstants.OBJECT_TYPE_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidObjectTypeCode(ObjectType objectTypeCode, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getObjectTypeCodeValidationErrors(objectTypeCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getObjectTypeCodeValidationErrors(ObjectType objectTypeCode, String errorPropertyName,
            String errorPropertyIdentifyingName, boolean useShortMessages) {
        // note that the errorPropertyName does not match the actual attribute name
        String label = getObjectTypeCodeLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(objectTypeCode) && objectTypeCode.isActive();
        return getExistsActiveValidationErrors(objectTypeCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidProjectCode(String errorPropertyIdentifyingName, ProjectCode projectCode) {
        return isValidProjectCode(projectCode, KFSConstants.PROJECT_CODE_PROPERTY_NAME, errorPropertyIdentifyingName);
    }

    @Override
    public boolean isValidProjectCode(ProjectCode projectCode, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getProjectCodeValidationErrors(projectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getProjectCodeValidationErrors(ProjectCode projectCode, String errorPropertyName,
            String errorPropertyIdentifyingName, boolean useShortMessages) {
        // note that the errorPropertyName does not match the actual attribute name
        String label = getProjectCodeLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(projectCode) && projectCode.isActive();
        return getExistsActiveValidationErrors(projectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidSubAccount(String accountIdentifyingPropertyName, SubAccount subAccount) {
        return isValidSubAccount(subAccount, KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidSubAccount(SubAccount subAccount, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getSubAccountValidationErrors(subAccount, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getSubAccountValidationErrors(SubAccount subAccount, String errorPropertyName,
                                                                             String errorPropertyIdentifyingName, boolean useShortMessages) {
        String label = getSubAccountLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(subAccount) && subAccount.isActive();
        return getExistsActiveValidationErrors(subAccount, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidSubObjectCode(String accountIdentifyingPropertyName, SubObjectCode subObjectCode) {
        return isValidSubObjectCode(subObjectCode, KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidSubObjectCode(SubObjectCode subObjectCode, String errorPropertyName, String errorPropertyIdentifyingName) {
        List<AccountingLineValidationError> errors = getSubObjectCodeValidationErrors(subObjectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getSubObjectCodeValidationErrors(SubObjectCode subObjectCode, String errorPropertyName,
            String errorPropertyIdentifyingName, boolean useShortMessages) {
        String label = getSubObjectCodeLabel();

        // Check exists & active
        boolean isActive = ObjectUtils.isNotNull(subObjectCode) && subObjectCode.isActive();
        return getExistsActiveValidationErrors(subObjectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    /**
     * Validates an accounting line. Side effects: refreshes referenced BOs and sets global error messages
     *
     * @param accountingLine
     * @return boolean whether the accounting line is valid
     */
    @Override
    public boolean validateAccountingLine(AccountingLine accountingLine) {
        // CORNELL FIX: if (accountingLine == null) {
        if (ObjectUtils.isNull(accountingLine)) {
            throw new IllegalStateException(configurationService.getPropertyValueAsString(
                    KFSKeyConstants.ERROR_DOCUMENT_NULL_ACCOUNTING_LINE));
        }

        // Refresh reference objects
        accountingLine.refreshReferenceObject("chart");
        accountingLine.refreshReferenceObject("account");
        accountingLine.refreshReferenceObject("objectCode");
        if (StringUtils.isNotBlank(accountingLine.getSubAccountNumber())
                && !accountingLine.getSubAccountNumber().equals(getDashSubAccountNumber())) {
            accountingLine.refreshReferenceObject("subAccount");
        }
        if (StringUtils.isNotBlank(accountingLine.getFinancialSubObjectCode())) {
            accountingLine.refreshReferenceObject("subObjectCode");
        }
        if (StringUtils.isNotBlank(accountingLine.getProjectCode())) {
            accountingLine.refreshReferenceObject("project");
        }
        if (StringUtils.isNotBlank(accountingLine.getReferenceOriginCode())) {
            accountingLine.refreshReferenceObject("referenceOrigin");
        }

        List<AccountingLineValidationError> validationErrors = getAccountingLineValidationErrors(accountingLine, false);
        setGlobalErrorMessages(validationErrors);

        boolean valid = validationErrors.isEmpty();

        valid &= hasRequiredOverrides(accountingLine, accountingLine.getOverrideCode());
        return valid;
    }

    @Override
    public List<AccountingLineValidationError> getAccountingLineValidationErrors(AccountingLine accountingLine, boolean useShortMessages) {
        List<AccountingLineValidationError> validationErrors = new ArrayList<>();
        
        // CORNELL FIX:
        if (ObjectUtils.isNull(accountingLine)) {
            return validationErrors;
        }

        //get the accounting line sequence string to identify which line has error.
        String accountIdentifyingPropertyName = getAccountIdentifyingPropertyName(accountingLine);

        // retrieve accounting line objects to validate
        Chart chart = accountingLine.getChart();
        Account account = accountingLine.getAccount();
        ObjectCode objectCode = accountingLine.getObjectCode();

        validationErrors.addAll(
                getChartValidationErrors(chart, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountIdentifyingPropertyName, useShortMessages)
        );
        validationErrors.addAll(
                getAccountValidationErrors(account, KFSPropertyConstants.ACCOUNT_NUMBER, accountIdentifyingPropertyName, useShortMessages)
        );

        // sub account is not required
        if (StringUtils.isNotBlank(accountingLine.getSubAccountNumber())
                && !accountingLine.getSubAccountNumber().equals(getDashSubAccountNumber())) {
            SubAccount subAccount = accountingLine.getSubAccount();

            validationErrors.addAll(
                    getSubAccountValidationErrors(subAccount, KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountIdentifyingPropertyName, useShortMessages)
            );
        }

        validationErrors.addAll(
                getObjectCodeValidationErrors(objectCode, KFSPropertyConstants.FINANCIAL_OBJECT_CODE, accountIdentifyingPropertyName, useShortMessages)
        );
        // sub object is not required
        if (StringUtils.isNotBlank(accountingLine.getFinancialSubObjectCode())) {
            SubObjectCode subObjectCode = accountingLine.getSubObjectCode();

            validationErrors.addAll(
                    getSubObjectCodeValidationErrors(subObjectCode, KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, accountIdentifyingPropertyName, useShortMessages)
            );
        }
        // project code is not required
        if (StringUtils.isNotBlank(accountingLine.getProjectCode())) {
            ProjectCode projectCode = accountingLine.getProject();

            validationErrors.addAll(
                    getProjectCodeValidationErrors(projectCode, KFSConstants.PROJECT_CODE_PROPERTY_NAME, accountIdentifyingPropertyName, useShortMessages)
            );
        }
        if (StringUtils.isNotBlank(accountingLine.getReferenceOriginCode())) {
            OriginationCode referenceOrigin = accountingLine.getReferenceOrigin();
            validationErrors.addAll(
                    getReferenceOriginCodeValidationErrors(accountIdentifyingPropertyName, referenceOrigin, useShortMessages)
            );
        }
        if (StringUtils.isNotBlank(accountingLine.getReferenceTypeCode())) {
            DocumentType referenceType = accountingLine.getReferenceFinancialSystemDocumentType();
            validationErrors.addAll(
                    getReferenceTypeCodeValidationErrors(accountingLine.getReferenceTypeCode(), referenceType, accountIdentifyingPropertyName, useShortMessages)
            );
        }

        return validationErrors;
    }

    /**
     * This method will check the reference origin code for existence in the system and whether it can actively be used.
     *
     * @param referenceOriginCode
     * @return List The validation errors
     */
    protected List<AccountingLineValidationError> getReferenceOriginCodeValidationErrors(String accountIdentifyingPropertyName,
                                                                                         OriginationCode referenceOriginCode, boolean useShortMessages) {
        return getExistenceValidationErrors(referenceOriginCode, KFSPropertyConstants.REFERENCE_ORIGIN_CODE,
                KFSPropertyConstants.REFERENCE_ORIGIN_CODE, accountIdentifyingPropertyName, useShortMessages);
    }

    /**
     * This method will check the reference type code for existence in the system and whether it can actively be used.
     *
     * @param documentTypeCode    the document type name of the reference document type
     * @param referenceType
     * @return List the validation errors
     */
    protected List<AccountingLineValidationError> getReferenceTypeCodeValidationErrors(String documentTypeCode, DocumentType referenceType,
                                                                                       String errorPropertyIdentifyingName, boolean useShortMessages) {
        if (!StringUtils.isBlank(documentTypeCode)
                && !financialSystemDocumentTypeService.isCurrentActiveAccountingDocumentType(documentTypeCode)) {
            AccountingLineValidationError validationError = new AccountingLineValidationError(KFSPropertyConstants.REFERENCE_TYPE_CODE);
            if (useShortMessages) {
                validationError.setErrorKey(KFSKeyConstants.ERROR_INACTIVE_SHORT);
            } else {
                validationError.setErrorKey(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_NON_ACTIVE_CURRENT_ACCOUNTING_DOCUMENT_TYPE);
                validationError.setMessageParameters(documentTypeCode);
            }
            List<AccountingLineValidationError> validationErrors = new ArrayList<>();
            validationErrors.add(validationError);
            return validationErrors;
        }
        return getExistenceValidationErrors(referenceType, KFSPropertyConstants.REFERENCE_TYPE_CODE,
                KFSPropertyConstants.REFERENCE_TYPE_CODE, errorPropertyIdentifyingName, useShortMessages);
    }

    /**
     * Checks for the existence of the given Object. This is doing an OJB-proxy-smart check, so assuming the given
     * Object is not in need of a refresh(), this method adds an existence error to the global error map if the given
     * Object is not in the database.
     *
     * @param toCheck             the Object to check for existence
     * @param attributeName       the name of the SourceAccountingLine attribute in the accountingLineEntry
     * @param propertyName        the name of the property within the global error path.
     * @return list of the validation errors based on existence of object
     */
    protected List<AccountingLineValidationError> getExistenceValidationErrors(Object toCheck, String attributeName, String propertyName,
            String errorPropertyIdentifyingName, boolean useShortMessages) {
        List<AccountingLineValidationError> validationErrors = new ArrayList<>();
        String label = dataDictionaryService.getAttributeShortLabel(SourceAccountingLine.class, attributeName);
        if (ObjectUtils.isNull(toCheck)) {
            AccountingLineValidationError validationError = new AccountingLineValidationError(propertyName);
            if (useShortMessages) {
                validationError.setErrorKey(KFSKeyConstants.ERROR_EXISTING_SHORT);
            } else {
                validationError.setErrorKey(KFSKeyConstants.ERROR_EXISTING_WITH_IDENTIFYING_ACCOUNTING_LINE);
                validationError.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    protected String getAccountIdentifyingPropertyName(AccountingLine accountingLine) {
        String errorProperty = "";

        // CORNELL FIX: if (accountingLine.getSequenceNumber() != null) {
        if (ObjectUtils.isNotNull(accountingLine) && ObjectUtils.isNotNull(accountingLine.getSequenceNumber())) {
            errorProperty = "Accounting Line: " + accountingLine.getSequenceNumber() + ", Chart: " +
                    accountingLine.getChartOfAccountsCode() + ", Account: " + accountingLine.getAccountNumber() + " - ";
        }

        return errorProperty;
    }

    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setFinancialSystemDocumentTypeService(
            FinancialSystemDocumentTypeService financialSystemDocumentTypeService) {
        this.financialSystemDocumentTypeService = financialSystemDocumentTypeService;
    }

    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // This exists so tests can avoid static mocking
    String getDashSubAccountNumber() {
        return KFSConstants.getDashSubAccountNumber();
    }
}
