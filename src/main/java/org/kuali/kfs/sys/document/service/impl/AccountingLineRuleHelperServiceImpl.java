/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
    public boolean hasRequiredOverrides(final AccountingLine line, final String overrideCode) {
        return hasAccountRequiredOverrides(line, overrideCode) && hasObjectBudgetRequiredOverrides(line, overrideCode);
    }

    public boolean hasAccountRequiredOverrides(final AccountingLine line, final String overrideCode) {
        boolean retVal = true;
        final AccountingLineOverride override = AccountingLineOverride.valueOf(overrideCode);
        final Account account = line.getAccount();
        if (AccountingLineOverride.needsExpiredAccountOverride(account)
                && !override.hasComponent(AccountingLineOverride.COMPONENT.EXPIRED_ACCOUNT)) {
            final Account continuation = accountService.getUnexpiredContinuationAccountOrNull(account);
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

    public boolean hasObjectBudgetRequiredOverrides(final AccountingLine line, final String overrideCode) {
        boolean retVal = true;
        final ObjectCode objectCode = line.getObjectCode();
        final AccountingLineOverride override = AccountingLineOverride.valueOf(overrideCode);
        final Account account = line.getAccount();
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
    protected Account getUnexpiredContinuationAccountOrNull(final Account account) {
        return accountService.getUnexpiredContinuationAccountOrNull(account);
    }

    private List<AccountingLineValidationError> getExistsActiveValidationErrors(final PersistableBusinessObject bo, final boolean isActive, final boolean useShortMessages, final String errorPropertyName, final String errorPropertyIdentifyingName, final String label) {
        final List<AccountingLineValidationError> validationErrors = new ArrayList<>();

        if (ObjectUtils.isNull(bo)) {
            final AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        } else if (!isActive) {
            // make sure it's active for usage
            final AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
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

    protected void setGlobalErrorMessages(final List<AccountingLineValidationError> validationErrors) {
        validationErrors.forEach(error -> {
            GlobalVariables.getMessageMap().putError(error.getPropertyName(), error.getErrorKey(), error.getMessageParameters());
        });
    }

    @Override
    public boolean isValidAccount(final String accountIdentifyingPropertyName, final Account account) {
        return isValidAccount(account, KFSPropertyConstants.ACCOUNT_NUMBER, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidAccount(final Account account, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getAccountValidationErrors(account, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    public List<AccountingLineValidationError> getAccountValidationErrors(final Account account, final String errorPropertyName, final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final String label = getAccountLabel();

        final List<AccountingLineValidationError> validationErrors = new ArrayList<>();

        if (ObjectUtils.isNull(account)) {
            final AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
            if (useShortMessages) {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_SHORT);
            } else {
                error.setErrorKey(KFSKeyConstants.ERROR_EXISTING_WITH_IDENTIFYING_ACCOUNTING_LINE);
                error.setMessageParameters(errorPropertyIdentifyingName, label);
            }
            validationErrors.add(error);
        } else if (!account.isActive() || account.isClosed()) {
            // make sure it's active for usage
            final AccountingLineValidationError error = new AccountingLineValidationError(errorPropertyName);
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
    public boolean isValidChart(final String accountIdentifyingPropertyName, final Chart chart) {
        return isValidChart(chart, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidChart(final Chart chart, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getChartValidationErrors(chart, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getChartValidationErrors(final Chart chart, final String errorPropertyName, final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final String label = getChartLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(chart) && chart.isActive();
        return getExistsActiveValidationErrors(chart, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidObjectCode(final String accountIdentifyingPropertyName, final ObjectCode objectCode) {
        return isValidObjectCode(objectCode, KFSPropertyConstants.FINANCIAL_OBJECT_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidObjectCode(final ObjectCode objectCode, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getObjectCodeValidationErrors(objectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getObjectCodeValidationErrors(
            final ObjectCode objectCode, final String errorPropertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final String label = getObjectCodeLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(objectCode) && StringUtils.isNotBlank(objectCode.getCode()) && objectCode.isFinancialObjectActiveCode();
        return getExistsActiveValidationErrors(objectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidObjectTypeCode(final String accountIdentifyingPropertyName, final ObjectType objectTypeCode) {
        return isValidObjectTypeCode(objectTypeCode, KFSPropertyConstants.OBJECT_TYPE_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidObjectTypeCode(final ObjectType objectTypeCode, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getObjectTypeCodeValidationErrors(objectTypeCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getObjectTypeCodeValidationErrors(
            final ObjectType objectTypeCode, final String errorPropertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        // note that the errorPropertyName does not match the actual attribute name
        final String label = getObjectTypeCodeLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(objectTypeCode) && objectTypeCode.isActive();
        return getExistsActiveValidationErrors(objectTypeCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidProjectCode(final String errorPropertyIdentifyingName, final ProjectCode projectCode) {
        return isValidProjectCode(projectCode, KFSConstants.PROJECT_CODE_PROPERTY_NAME, errorPropertyIdentifyingName);
    }

    @Override
    public boolean isValidProjectCode(final ProjectCode projectCode, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getProjectCodeValidationErrors(projectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getProjectCodeValidationErrors(
            final ProjectCode projectCode, final String errorPropertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        // note that the errorPropertyName does not match the actual attribute name
        final String label = getProjectCodeLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(projectCode) && projectCode.isActive();
        return getExistsActiveValidationErrors(projectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidSubAccount(final String accountIdentifyingPropertyName, final SubAccount subAccount) {
        return isValidSubAccount(subAccount, KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidSubAccount(final SubAccount subAccount, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getSubAccountValidationErrors(subAccount, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getSubAccountValidationErrors(
            final SubAccount subAccount, final String errorPropertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final String label = getSubAccountLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(subAccount) && subAccount.isActive();
        return getExistsActiveValidationErrors(subAccount, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    @Override
    public boolean isValidSubObjectCode(final String accountIdentifyingPropertyName, final SubObjectCode subObjectCode) {
        return isValidSubObjectCode(subObjectCode, KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, accountIdentifyingPropertyName);
    }

    @Override
    public boolean isValidSubObjectCode(final SubObjectCode subObjectCode, final String errorPropertyName, final String errorPropertyIdentifyingName) {
        final List<AccountingLineValidationError> errors = getSubObjectCodeValidationErrors(subObjectCode, errorPropertyName, errorPropertyIdentifyingName, false);
        setGlobalErrorMessages(errors);
        return errors.isEmpty();
    }

    protected List<AccountingLineValidationError> getSubObjectCodeValidationErrors(
            final SubObjectCode subObjectCode, final String errorPropertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final String label = getSubObjectCodeLabel();

        // Check exists & active
        final boolean isActive = ObjectUtils.isNotNull(subObjectCode) && subObjectCode.isActive();
        return getExistsActiveValidationErrors(subObjectCode, isActive, useShortMessages, errorPropertyName, errorPropertyIdentifyingName, label);
    }

    /**
     * Validates an accounting line. Side effects: refreshes referenced BOs and sets global error messages
     *
     * @param accountingLine
     * @return boolean whether the accounting line is valid
     */
    @Override
    public boolean validateAccountingLine(final AccountingLine accountingLine) {
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

        final List<AccountingLineValidationError> validationErrors = getAccountingLineValidationErrors(accountingLine, false);
        setGlobalErrorMessages(validationErrors);

        boolean valid = validationErrors.isEmpty();

        valid &= hasRequiredOverrides(accountingLine, accountingLine.getOverrideCode());
        return valid;
    }

    @Override
    public List<AccountingLineValidationError> getAccountingLineValidationErrors(final AccountingLine accountingLine, final boolean useShortMessages) {
        final List<AccountingLineValidationError> validationErrors = new ArrayList<>();
        
        // CORNELL FIX:
        if (ObjectUtils.isNull(accountingLine)) {
            return validationErrors;
        }

        //get the accounting line sequence string to identify which line has error.
        final String accountIdentifyingPropertyName = getAccountIdentifyingPropertyName(accountingLine);

        // retrieve accounting line objects to validate
        final Chart chart = accountingLine.getChart();
        final Account account = accountingLine.getAccount();
        final ObjectCode objectCode = accountingLine.getObjectCode();

        validationErrors.addAll(
                getChartValidationErrors(chart, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, accountIdentifyingPropertyName, useShortMessages)
        );
        validationErrors.addAll(
                getAccountValidationErrors(account, KFSPropertyConstants.ACCOUNT_NUMBER, accountIdentifyingPropertyName, useShortMessages)
        );

        // sub account is not required
        if (StringUtils.isNotBlank(accountingLine.getSubAccountNumber())
                && !accountingLine.getSubAccountNumber().equals(getDashSubAccountNumber())) {
            final SubAccount subAccount = accountingLine.getSubAccount();

            validationErrors.addAll(
                    getSubAccountValidationErrors(subAccount, KFSPropertyConstants.SUB_ACCOUNT_NUMBER, accountIdentifyingPropertyName, useShortMessages)
            );
        }

        validationErrors.addAll(
                getObjectCodeValidationErrors(objectCode, KFSPropertyConstants.FINANCIAL_OBJECT_CODE, accountIdentifyingPropertyName, useShortMessages)
        );
        // sub object is not required
        if (StringUtils.isNotBlank(accountingLine.getFinancialSubObjectCode())) {
            final SubObjectCode subObjectCode = accountingLine.getSubObjectCode();

            validationErrors.addAll(
                    getSubObjectCodeValidationErrors(subObjectCode, KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, accountIdentifyingPropertyName, useShortMessages)
            );
        }
        // project code is not required
        if (StringUtils.isNotBlank(accountingLine.getProjectCode())) {
            final ProjectCode projectCode = accountingLine.getProject();

            validationErrors.addAll(
                    getProjectCodeValidationErrors(projectCode, KFSConstants.PROJECT_CODE_PROPERTY_NAME, accountIdentifyingPropertyName, useShortMessages)
            );
        }
        if (StringUtils.isNotBlank(accountingLine.getReferenceOriginCode())) {
            final OriginationCode referenceOrigin = accountingLine.getReferenceOrigin();
            validationErrors.addAll(
                    getReferenceOriginCodeValidationErrors(accountIdentifyingPropertyName, referenceOrigin, useShortMessages)
            );
        }
        if (StringUtils.isNotBlank(accountingLine.getReferenceTypeCode())) {
            final DocumentType referenceType = accountingLine.getReferenceFinancialSystemDocumentType();
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
    protected List<AccountingLineValidationError> getReferenceOriginCodeValidationErrors(
            final String accountIdentifyingPropertyName,
                                                                                         final OriginationCode referenceOriginCode, final boolean useShortMessages) {
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
    protected List<AccountingLineValidationError> getReferenceTypeCodeValidationErrors(
            final String documentTypeCode, final DocumentType referenceType,
                                                                                       final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        if (StringUtils.isNotBlank(documentTypeCode)
                && !financialSystemDocumentTypeService.isCurrentActiveAccountingDocumentType(documentTypeCode)) {
            final AccountingLineValidationError validationError = new AccountingLineValidationError(KFSPropertyConstants.REFERENCE_TYPE_CODE);
            if (useShortMessages) {
                validationError.setErrorKey(KFSKeyConstants.ERROR_INACTIVE_SHORT);
            } else {
                validationError.setErrorKey(KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_NON_ACTIVE_CURRENT_ACCOUNTING_DOCUMENT_TYPE);
                validationError.setMessageParameters(documentTypeCode);
            }
            final List<AccountingLineValidationError> validationErrors = new ArrayList<>();
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
    protected List<AccountingLineValidationError> getExistenceValidationErrors(
            final Object toCheck, final String attributeName, final String propertyName,
            final String errorPropertyIdentifyingName, final boolean useShortMessages) {
        final List<AccountingLineValidationError> validationErrors = new ArrayList<>();
        final String label = dataDictionaryService.getAttributeShortLabel(SourceAccountingLine.class, attributeName);
        if (ObjectUtils.isNull(toCheck)) {
            final AccountingLineValidationError validationError = new AccountingLineValidationError(propertyName);
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

    protected String getAccountIdentifyingPropertyName(final AccountingLine accountingLine) {
        String errorProperty = "";

        // CORNELL FIX: if (accountingLine.getSequenceNumber() != null) {
        if (ObjectUtils.isNotNull(accountingLine) && ObjectUtils.isNotNull(accountingLine.getSequenceNumber())) {
            errorProperty = "Accounting Line: " + accountingLine.getSequenceNumber() + ", Chart: " +
                    accountingLine.getChartOfAccountsCode() + ", Account: " + accountingLine.getAccountNumber() + " - ";
        }

        return errorProperty;
    }

    public void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setFinancialSystemDocumentTypeService(
            final FinancialSystemDocumentTypeService financialSystemDocumentTypeService) {
        this.financialSystemDocumentTypeService = financialSystemDocumentTypeService;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    // This exists so tests can avoid static mocking
    String getDashSubAccountNumber() {
        return KFSConstants.getDashSubAccountNumber();
    }
}
