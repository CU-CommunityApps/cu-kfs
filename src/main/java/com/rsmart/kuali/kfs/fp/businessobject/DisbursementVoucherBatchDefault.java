/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.fp.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.Campus;
import org.kuali.kfs.sys.businessobject.PaymentDocumentationLocation;

import com.rsmart.kuali.kfs.fp.FPPropertyConstants;

/**
 * Contains default values to use when loading disbursement voucher documents in batch
 */
public class DisbursementVoucherBatchDefault extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = 1L;
	private String unitCode;
    private String unitName;
    private String disbVchrContactPersonName;
    private String disbVchrContactPhoneNumber;
    private String disbVchrContactEmailId;
    private String campusCode;
    private String disbVchrPaymentMethodCode;
    private String disbursementVoucherDocumentationLocationCode;
    private String disbVchrBankCode;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String financialDocumentLineDescription;

    private Campus campus;
    private PaymentDocumentationLocation documentationLocation;
    private Bank bank;
    private Chart chart;
    private Account account;
    private ObjectCode objectCode;

    public DisbursementVoucherBatchDefault() {
        super();
    }


    /**
     * Gets the unitCode attribute.
     * 
     * @return Returns the unitCode.
     */
    public String getUnitCode() {
        return unitCode;
    }


    /**
     * Sets the unitCode attribute value.
     * 
     * @param unitCode The unitCode to set.
     */
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }


    /**
     * Gets the unitName attribute.
     * 
     * @return Returns the unitName.
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * Sets the unitName attribute value.
     * 
     * @param unitName The unitName to set.
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * Gets the disbVchrContactPersonName attribute.
     * 
     * @return Returns the disbVchrContactPersonName.
     */
    public String getDisbVchrContactPersonName() {
        return disbVchrContactPersonName;
    }

    /**
     * Sets the disbVchrContactPersonName attribute value.
     * 
     * @param disbVchrContactPersonName The disbVchrContactPersonName to set.
     */
    public void setDisbVchrContactPersonName(String disbVchrContactPersonName) {
        this.disbVchrContactPersonName = disbVchrContactPersonName;
    }

    /**
     * Gets the disbVchrContactPhoneNumber attribute.
     * 
     * @return Returns the disbVchrContactPhoneNumber.
     */
    public String getDisbVchrContactPhoneNumber() {
        return disbVchrContactPhoneNumber;
    }

    /**
     * Sets the disbVchrContactPhoneNumber attribute value.
     * 
     * @param disbVchrContactPhoneNumber The disbVchrContactPhoneNumber to set.
     */
    public void setDisbVchrContactPhoneNumber(String disbVchrContactPhoneNumber) {
        this.disbVchrContactPhoneNumber = disbVchrContactPhoneNumber;
    }

    /**
     * Gets the disbVchrContactEmailId attribute.
     * 
     * @return Returns the disbVchrContactEmailId.
     */
    public String getDisbVchrContactEmailId() {
        return disbVchrContactEmailId;
    }

    /**
     * Sets the disbVchrContactEmailId attribute value.
     * 
     * @param disbVchrContactEmailId The disbVchrContactEmailId to set.
     */
    public void setDisbVchrContactEmailId(String disbVchrContactEmailId) {
        this.disbVchrContactEmailId = disbVchrContactEmailId;
    }

    /**
     * Gets the campusCode attribute.
     * 
     * @return Returns the campusCode.
     */
    public String getCampusCode() {
        return campusCode;
    }

    /**
     * Sets the campusCode attribute value.
     * 
     * @param campusCode The campusCode to set.
     */
    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    /**
     * Gets the disbVchrPaymentMethodCode attribute.
     * 
     * @return Returns the disbVchrPaymentMethodCode.
     */
    public String getDisbVchrPaymentMethodCode() {
        return disbVchrPaymentMethodCode;
    }

    /**
     * Sets the disbVchrPaymentMethodCode attribute value.
     * 
     * @param disbVchrPaymentMethodCode The disbVchrPaymentMethodCode to set.
     */
    public void setDisbVchrPaymentMethodCode(String disbVchrPaymentMethodCode) {
        this.disbVchrPaymentMethodCode = disbVchrPaymentMethodCode;
    }

    /**
     * Gets the disbursementVoucherDocumentationLocationCode attribute.
     * 
     * @return Returns the disbursementVoucherDocumentationLocationCode.
     */
    public String getDisbursementVoucherDocumentationLocationCode() {
        return disbursementVoucherDocumentationLocationCode;
    }

    /**
     * Sets the disbursementVoucherDocumentationLocationCode attribute value.
     * 
     * @param disbursementVoucherDocumentationLocationCode The disbursementVoucherDocumentationLocationCode to set.
     */
    public void setDisbursementVoucherDocumentationLocationCode(String disbursementVoucherDocumentationLocationCode) {
        this.disbursementVoucherDocumentationLocationCode = disbursementVoucherDocumentationLocationCode;
    }

    /**
     * Gets the disbVchrBankCode attribute.
     * 
     * @return Returns the disbVchrBankCode.
     */
    public String getDisbVchrBankCode() {
        return disbVchrBankCode;
    }

    /**
     * Sets the disbVchrBankCode attribute value.
     * 
     * @param disbVchrBankCode The disbVchrBankCode to set.
     */
    public void setDisbVchrBankCode(String disbVchrBankCode) {
        this.disbVchrBankCode = disbVchrBankCode;
    }

    /**
     * Gets the chartOfAccountsCode attribute.
     * 
     * @return Returns the chartOfAccountsCode.
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    /**
     * Sets the chartOfAccountsCode attribute value.
     * 
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    /**
     * Gets the accountNumber attribute.
     * 
     * @return Returns the accountNumber.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Sets the accountNumber attribute value.
     * 
     * @param accountNumber The accountNumber to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     * Gets the financialObjectCode attribute.
     * 
     * @return Returns the financialObjectCode.
     */
    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    /**
     * Sets the financialObjectCode attribute value.
     * 
     * @param financialObjectCode The financialObjectCode to set.
     */
    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    /**
     * Gets the financialDocumentLineDescription attribute.
     * 
     * @return Returns the financialDocumentLineDescription.
     */
    public String getFinancialDocumentLineDescription() {
        return financialDocumentLineDescription;
    }

    /**
     * Sets the financialDocumentLineDescription attribute value.
     * 
     * @param financialDocumentLineDescription The financialDocumentLineDescription to set.
     */
    public void setFinancialDocumentLineDescription(String financialDocumentLineDescription) {
        this.financialDocumentLineDescription = financialDocumentLineDescription;
    }

    /**
     * Gets the campus attribute.
     * 
     * @return Returns the campus.
     */
    public Campus getCampus() {
        if ( StringUtils.isBlank(campusCode) ) {
            campus = null;
        } else {
            if ( campus == null || !StringUtils.equals( campus.getCode(), campusCode) ) {
                campus = KRADServiceLocator.getBusinessObjectService().findBySinglePrimaryKey(Campus.class, campusCode);
            }
        }
        return campus;
    }

    /**
     * Sets the campus attribute value.
     * 
     * @param campus The campus to set.
     */
    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    /**
     * Gets the documentationLocation attribute.
     * 
     * @return Returns the documentationLocation.
     */
    public PaymentDocumentationLocation getDocumentationLocation() {
        return documentationLocation;
    }

    /**
     * Sets the documentationLocation attribute value.
     * 
     * @param documentationLocation The documentationLocation to set.
     */
    public void setDocumentationLocation(PaymentDocumentationLocation documentationLocation) {
        this.documentationLocation = documentationLocation;
    }

    /**
     * Gets the bank attribute.
     * 
     * @return Returns the bank.
     */
    public Bank getBank() {
        return bank;
    }

    /**
     * Sets the bank attribute value.
     * 
     * @param bank The bank to set.
     */
    public void setBank(Bank bank) {
        this.bank = bank;
    }

    /**
     * Gets the chart attribute.
     * 
     * @return Returns the chart.
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Sets the chart attribute value.
     * 
     * @param chart The chart to set.
     */
    public void setChart(Chart chart) {
        this.chart = chart;
    }

    /**
     * Gets the account attribute.
     * 
     * @return Returns the account.
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the account attribute value.
     * 
     * @param account The account to set.
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Gets the objectCode attribute.
     * 
     * @return Returns the objectCode.
     */
    public ObjectCode getObjectCode() {
        return objectCode;
    }

    /**
     * Sets the objectCode attribute value.
     * 
     * @param objectCode The objectCode to set.
     */
    public void setObjectCode(ObjectCode objectCode) {
        this.objectCode = objectCode;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put(FPPropertyConstants.UNIT_CODE, this.unitCode);

        return m;
    }

}
