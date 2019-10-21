/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.identity;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.ContractManager;
import org.kuali.kfs.vnd.businessobject.VendorType;
import org.kuali.rice.core.api.util.type.KualiInteger;

public class KfsKimAttributes extends org.kuali.rice.kim.bo.impl.KimAttributes {

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
    public static final String CUSTOMER_PROFILE_ID = "customerProfileId";
    public static final String PROFILE_ID = "profileId";
    public static final String ACH_TRANSACTION_TYPE_CODE = "achTransactionTypeCode";
    public static final String VENDOR_TYPE_CODE = "vendorTypeCode";
    public static final String CONTRACTS_AND_GRANTS_ACCOUNT_RESPONSIBILITY_ID = "contractsAndGrantsAccountResponsibilityId";
    public static final String PAYMENT_METHOD_CODE = "paymentMethodCode";
    public static final String SUB_ACCOUNT_NUMBER = "subAccountNumber";
    public static final String FILE_PATH = "filePath";
    public static final String ROUTE_NODE_NAME = "routeNodeName";
    public static final String REPORT_CODE = "reportCode";
    public static final String CAMPUS_CODE = "campusCode";

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
    protected Integer profilePrincipalId;
    protected String reportCode;
    protected String campusCode;

    protected Chart chart;
    protected Organization organization;
    protected Account account;
    protected SubFundGroup subFundGroup;
    protected ContractManager contractManager;
    protected CommodityCode commodityCode;
    protected CustomerProfile customerProfile;
    protected SubAccount subAccount;
    protected VendorType vendorType;

    @SuppressWarnings("unchecked")

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String isDescendHierarchy() {
        return descendHierarchy;
    }

    public void setDescendHierarchy(String descendHierarchy) {
        this.descendHierarchy = descendHierarchy;
    }

    public String getFromAmount() {
        return fromAmount;
    }

    public void setFromAmount(String fromAmount) {
        this.fromAmount = fromAmount;
    }

    public String getToAmount() {
        return toAmount;
    }

    public void setToAmount(String toAmount) {
        this.toAmount = toAmount;
    }

    public String getAccountingLineOverrideCode() {
        return accountingLineOverrideCode;
    }

    public void setAccountingLineOverrideCode(String accountingLineOverrideCode) {
        this.accountingLineOverrideCode = accountingLineOverrideCode;
    }

    public String getSubFundGroupCode() {
        return subFundGroupCode;
    }

    public void setSubFundGroupCode(String subFundGroupCode) {
        this.subFundGroupCode = subFundGroupCode;
    }

    public String getPurchasingCommodityCode() {
        return purchasingCommodityCode;
    }

    public void setPurchasingCommodityCode(String purchasingCommodityCode) {
        this.purchasingCommodityCode = purchasingCommodityCode;
    }

    public Integer getContractManagerCode() {
        return contractManagerCode;
    }

    public void setContractManagerCode(Integer contractManagerCode) {
        this.contractManagerCode = contractManagerCode;
    }

    public KualiInteger getCustomerProfileId() {
        return customerProfileId;
    }

    public void setCustomerProfileId(KualiInteger customerProfileId) {
        this.customerProfileId = customerProfileId;
    }

    public String getAchTransactionTypeCode() {
        return achTransactionTypeCode;
    }

    public void setAchTransactionTypeCode(String achTransactionTypeCode) {
        this.achTransactionTypeCode = achTransactionTypeCode;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getContractsAndGrantsAccountResponsibilityId() {
        return contractsAndGrantsAccountResponsibilityId;
    }

    public void setContractsAndGrantsAccountResponsibilityId(String contractsAndGrantsAccountResponsibilityId) {
        this.contractsAndGrantsAccountResponsibilityId = contractsAndGrantsAccountResponsibilityId;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public Integer getProfilePrincipalId() {
        return profilePrincipalId;
    }

    public void setProfilePrincipalId(Integer profilePrincipalId) {
        this.profilePrincipalId = profilePrincipalId;
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public SubFundGroup getSubFundGroup() {
        return subFundGroup;
    }

    public void setSubFundGroup(SubFundGroup subFundGroup) {
        this.subFundGroup = subFundGroup;
    }

    public ContractManager getContractManager() {
        return contractManager;
    }

    public void setContractManager(ContractManager contractManager) {
        this.contractManager = contractManager;
    }

    public CommodityCode getCommodityCode() {
        return commodityCode;
    }

    public void setCommodityCode(CommodityCode commodityCode) {
        this.commodityCode = commodityCode;
    }

    public CustomerProfile getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(CustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }

    public SubAccount getSubAccount() {
        return subAccount;
    }

    public void setSubAccount(SubAccount subAccount) {
        this.subAccount = subAccount;
    }

    public VendorType getVendorType() {
        return vendorType;
    }

    public void setVendorType(VendorType vendorType) {
        this.vendorType = vendorType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getReportCode() {
        return reportCode;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    @Override
    public String getCampusCode() {
        return campusCode;
    }

    @Override
    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }
}
