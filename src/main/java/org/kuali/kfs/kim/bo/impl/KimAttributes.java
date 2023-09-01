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
package org.kuali.kfs.kim.bo.impl;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.FundGroup;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectConsolidation;
import org.kuali.kfs.coa.businessobject.ObjectLevel;
import org.kuali.kfs.coa.businessobject.ObjectSubType;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.impl.component.Component;
import org.kuali.kfs.coreservice.impl.namespace.Namespace;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.ContractManager;
import org.kuali.kfs.vnd.businessobject.VendorType;

public class KimAttributes extends TransientBusinessObjectBase {

    public static final String CHART_OF_ACCOUNTS_CODE = "chartOfAccountsCode";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String FINANCIAL_SYSTEM_DOCUMENT_TYPE_CODE = "financialSystemDocumentTypeCode";
    public static final String ORGANIZATION_CODE = "organizationCode";
    public static final String DESCEND_HIERARCHY = "descendHierarchy";
    public static final String FROM_AMOUNT = "fromAmount";
    public static final String TO_AMOUNT = "toAmount";
    public static final String FINANCIAL_DOCUMENT_TOTAL_AMOUNT = "financialDocumentTotalAmount";
    public static final String ACCOUNTING_LINE_OVERRIDE_CODE = "accountingLineOverrideCode";
    public static final String SUB_FUND_GROUP_CODE = "subFundGroupCode";
    public static final String PURCHASING_COMMODITY_CODE = "purchasingCommodityCode";
    public static final String CONTRACT_MANAGER_CODE = "contractManagerCode";
    public static final String ACH_TRANSACTION_TYPE_CODE = "achTransactionTypeCode";
    public static final String CONTRACTS_AND_GRANTS_ACCOUNT_RESPONSIBILITY_ID = "contractsAndGrantsAccountResponsibilityId";
    public static final String SUB_ACCOUNT_NUMBER = "subAccountNumber";
    public static final String FILE_PATH = "filePath";
    public static final String ROUTE_NODE_NAME = "routeNodeName";
    public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
    public static final String FINANCIAL_OBJECT_LEVEL_CODE = "financialObjectLevelCode";
    public static final String FINANCIAL_OBJECT_SUB_TYPE_CODE = "financialObjectSubTypeCode";
    public static final String FIN_CONSOLIDATION_OBJECT_CODE = "finConsolidationObjectCode";
    public static final String FUND_GROUP_CODE = "fundGroupCode";
    
    private static final long serialVersionUID = 8976113842166331719L;

    protected String methodToCall;
    protected String beanName;
    protected String buttonName;
    protected String actionClass;
    protected String namespaceCode;
    protected String componentName;
    protected String propertyName;
    protected Boolean existingRecordsOnly;
    protected Boolean createdBySelfOnly;
    protected String attachmentTypeCode;
    protected String collectionItemTypeCode;
    protected String editMode;
    protected String parameterName;
    protected String campusCode;
    protected String documentTypeName;
    protected String actionRequestCd;
    protected String routeStatusCode;
    protected String routeNodeName;
    protected String appDocStatus;
    protected String roleName;
    protected String permissionName;
    protected String responsibilityName;
    protected String groupName;
    protected Boolean required;
    protected Boolean actionDetailsAtRoleMemberLevel;
    protected String documentNumber;
    protected String sectionId;
    protected String kimTypeId;
    protected String qualifierResolverProvidedIdentifier;
    protected String viewId;
    protected String actionEvent;
    protected String collectionPropertyName;
    protected String fieldId;
    protected String groupId;
    protected String widgetId;
    protected String actionId;
    protected String financialObjectCode;
    protected String financialObjectLevelCode;
    protected String financialObjectSubTypeCode;
    protected String finConsolidationObjectCode;

    protected Namespace namespace;
    protected Component component;
    protected Parameter parameter;
    protected DocumentType documentType;
    protected String chartOfAccountsCode;
    protected String accountNumber;
    protected String organizationCode;
    protected String descendHierarchy;
    protected String fromAmount;
    protected String toAmount;
    protected String accountingLineOverrideCode;
    protected String subFundGroupCode;
    protected String purchasingCommodityCode;
    protected Integer contractManagerCode;
    protected KualiInteger customerProfileId;
    protected String achTransactionTypeCode;
    protected String vendorTypeCode;
    protected String contractsAndGrantsAccountResponsibilityId;
    protected String paymentMethodCode;
    protected String subAccountNumber;
    protected String filePath;
    protected String fundGroupCode;
    protected Integer profilePrincipalId;
    protected Chart chart;
    protected Organization organization;
    protected Account account;
    protected SubFundGroup subFundGroup;
    protected ContractManager contractManager;
    protected CommodityCode commodityCode;
    protected CustomerProfile customerProfile;
    protected SubAccount subAccount;
    protected VendorType vendorType;
    protected ObjectCode objectCode;
    protected ObjectLevel objectLevel;
    protected ObjectSubType objectSubType;
    protected ObjectConsolidation objectConsolidation;
    protected FundGroup fundGroup;

    public String getMethodToCall() {
        return methodToCall;
    }

    public void setMethodToCall(final String methodToCall) {
        this.methodToCall = methodToCall;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(final String beanName) {
        this.beanName = beanName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public void setButtonName(final String buttonName) {
        this.buttonName = buttonName;
    }

    public String getActionClass() {
        return actionClass;
    }

    public void setActionClass(final String actionClass) {
        this.actionClass = actionClass;
    }

    public String getNamespaceCode() {
        return namespaceCode;
    }

    public void setNamespaceCode(final String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(final String propertyName) {
        this.propertyName = propertyName;
    }

    public String getCollectionItemTypeCode() {
        return collectionItemTypeCode;
    }

    public void setCollectionItemTypeCode(final String collectionItemTypeCode) {
        this.collectionItemTypeCode = collectionItemTypeCode;
    }

    public String getEditMode() {
        return editMode;
    }

    public void setEditMode(final String editMode) {
        this.editMode = editMode;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(final String parameterName) {
        this.parameterName = parameterName;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(final String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String isDescendHierarchy() {
        return descendHierarchy;
    }

    public void setDescendHierarchy(final String descendHierarchy) {
        this.descendHierarchy = descendHierarchy;
    }

    public String getFromAmount() {
        return fromAmount;
    }

    public void setFromAmount(final String fromAmount) {
        this.fromAmount = fromAmount;
    }

    public String getToAmount() {
        return toAmount;
    }

    public void setToAmount(final String toAmount) {
        this.toAmount = toAmount;
    }

    public String getAccountingLineOverrideCode() {
        return accountingLineOverrideCode;
    }

    public void setAccountingLineOverrideCode(final String accountingLineOverrideCode) {
        this.accountingLineOverrideCode = accountingLineOverrideCode;
    }

    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    public void setSubFundGroupCode(final String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(final String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public Integer getContractManagerCode() {
        return contractManagerCode;
    }

    public void setContractManagerCode(final Integer contractManagerCode) {
        this.contractManagerCode = contractManagerCode;
    }

    public KualiInteger getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(final KualiInteger customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public String getAchTransactionTypeCode() {
        return achTransactionTypeCode;
    }

    public void setAchTransactionTypeCode(final String achTransactionTypeCode) {
        this.achTransactionTypeCode = achTransactionTypeCode;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(final String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getContractsAndGrantsAccountResponsibilityId() {
        return contractsAndGrantsAccountResponsibilityId;
    }

    public void setContractsAndGrantsAccountResponsibilityId(final String contractsAndGrantsAccountResponsibilityId) {
        this.contractsAndGrantsAccountResponsibilityId = contractsAndGrantsAccountResponsibilityId;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(final String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(final String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public Integer getProfilePrincipalId() {
        return profilePrincipalId;
    }

    public void setProfilePrincipalId(final Integer profilePrincipalId) {
        this.profilePrincipalId = profilePrincipalId;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(final Chart chart) {
        this.chart = chart;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(final Account account) {
        this.account = account;
    }

    public SubFundGroup getSubFundGroup() {
        return subFundGroup;
    }

    public void setSubFundGroup(final SubFundGroup subFundGroup) {
        this.subFundGroup = subFundGroup;
    }

    public ContractManager getContractManager() {
        return contractManager;
    }

    public void setContractManager(final ContractManager contractManager) {
        this.contractManager = contractManager;
    }

    public CommodityCode getCommodityCode() {
        return commodityCode;
    }

    public void setCommodityCode(final CommodityCode commodityCode) {
        this.commodityCode = commodityCode;
    }

    public CustomerProfile getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(final CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }

    public SubAccount getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(final SubAccount subAccount) {
        this.subAccount = subAccount;
    }

    public VendorType getVendorType() {
        return vendorType;
    }

    public void setVendorType(final VendorType vendorType) {
        this.vendorType = vendorType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(final String campusCode) {
        this.campusCode = campusCode;
    }

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(final String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public String getActionRequestCd() {
        return actionRequestCd;
    }

    public void setActionRequestCd(final String actionRequestCd) {
        this.actionRequestCd = actionRequestCd;
    }

    public String getRouteStatusCode() {
        return routeStatusCode;
    }

    public void setRouteStatusCode(final String routeStatusCode) {
        this.routeStatusCode = routeStatusCode;
    }

    public String getRouteNodeName() {
        return routeNodeName;
    }

    public void setRouteNodeName(final String routeNodeName) {
        this.routeNodeName = routeNodeName;
    }

    public String getAppDocStatus() {
        return appDocStatus;
    }

    public void setAppDocStatus(final String appDocStatus) {
        this.appDocStatus = appDocStatus;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(final String permissionName) {
        this.permissionName = permissionName;
    }

    public String getResponsibilityName() {
        return responsibilityName;
    }

    public void setResponsibilityName(final String responsibilityName) {
        this.responsibilityName = responsibilityName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean isExistingRecordsOnly() {
        return existingRecordsOnly;
    }

    public void setExistingRecordsOnly(final Boolean existingRecordsOnly) {
        this.existingRecordsOnly = existingRecordsOnly;
    }

    public Boolean isCreatedBySelfOnly() {
        return createdBySelfOnly;
    }

    public void setCreatedBySelfOnly(final Boolean createdBySelfOnly) {
        this.createdBySelfOnly = createdBySelfOnly;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    public Boolean isActionDetailsAtRoleMemberLevel() {
        return actionDetailsAtRoleMemberLevel;
    }

    public void setActionDetailsAtRoleMemberLevel(final Boolean actionDetailsAtRoleMemberLevel) {
        this.actionDetailsAtRoleMemberLevel = actionDetailsAtRoleMemberLevel;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(final String sectionId) {
        this.sectionId = sectionId;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(final Namespace namespace) {
        this.namespace = namespace;
    }

    public Component getComponent() {
        return component;
    }

    public void setComponent(final Component component) {
        this.component = component;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(final Parameter parameter) {
        this.parameter = parameter;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(final DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getKimTypeId() {
        return kimTypeId;
    }

    public void setKimTypeId(final String kimTypeId) {
        this.kimTypeId = kimTypeId;
    }

    public String getQualifierResolverProvidedIdentifier() {
        return qualifierResolverProvidedIdentifier;
    }

    public void setQualifierResolverProvidedIdentifier(final String qualifierResolverProvidedIdentifier) {
        this.qualifierResolverProvidedIdentifier = qualifierResolverProvidedIdentifier;
    }

    public String getAttachmentTypeCode() {
        return attachmentTypeCode;
    }

    public void setAttachmentTypeCode(final String attachmentTypeCode) {
        this.attachmentTypeCode = attachmentTypeCode;
    }

    public String getActionEvent() {
        return actionEvent;
    }

    public void setActionEvent(final String actionEvent) {
        this.actionEvent = actionEvent;
    }

    public String getCollectionPropertyName() {
        return collectionPropertyName;
    }

    public void setCollectionPropertyName(final String collectionPropertyName) {
        this.collectionPropertyName = collectionPropertyName;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(final String fieldId) {
        this.fieldId = fieldId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(final String viewId) {
        this.viewId = viewId;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(final String widgetId) {
        this.widgetId = widgetId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(final String actionId) {
        this.actionId = actionId;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(final String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getFinancialObjectLevelCode() {
        return financialObjectLevelCode;
    }

    public void setFinancialObjectLevelCode(final String financialObjectLevelCode) {
        this.financialObjectLevelCode = financialObjectLevelCode;
    }

    public String getFinancialObjectSubTypeCode() {
        return financialObjectSubTypeCode;
    }

    public void setFinancialObjectSubTypeCode(final String financialObjectSubTypeCode) {
        this.financialObjectSubTypeCode = financialObjectSubTypeCode;
    }

    public String getFinConsolidationObjectCode() {
        return finConsolidationObjectCode;
    }

    public void setFinConsolidationObjectCode(final String finConsolidationObjectCode) {
        this.finConsolidationObjectCode = finConsolidationObjectCode;
    }

    public ObjectCode getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(final ObjectCode objectCode) {
        this.objectCode = objectCode;
    }

    public ObjectLevel getObjectLevel() {
        return objectLevel;
    }

    public void setObjectLevel(final ObjectLevel objectLevel) {
        this.objectLevel = objectLevel;
    }

    public ObjectSubType getObjectSubType() {
        return objectSubType;
    }

    public void setObjectSubType(final ObjectSubType objectSubType) {
        this.objectSubType = objectSubType;
    }

    public ObjectConsolidation getObjectConsolidation() {
        return objectConsolidation;
    }

    public void setObjectConsolidation(final ObjectConsolidation objectConsolidation) {
        this.objectConsolidation = objectConsolidation;
    }

    public String getFundGroupCode() {
        return fundGroupCode;
    }

    public void setFundGroupCode(String fundGroupCode) {
        this.fundGroupCode = fundGroupCode;
    }

    public FundGroup getFundGroup() {
        return fundGroup;
    }

    public void setFundGroup(FundGroup fundGroup) {
        this.fundGroup = fundGroup;
    }
}
