/*
 * Copyright 2010 The Kuali Foundation.
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
package edu.cornell.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;


public class PaymentMethod extends PersistableBusinessObjectBase implements MutableInactivatable {

	public static final String PM_CODE_WIRE = "W";
	public static final String PM_CODE_FOREIGN_DRAFT = "F";
	public static final String PM_CODE_INTERNAL_BILLING = "B";
    protected String paymentMethodCode;
    protected String paymentMethodName;
    protected String bankCode;
    protected boolean assessedFees;
    protected boolean processedUsingPdp;
    protected boolean interdepartmentalPayment;
    protected boolean offsetUsingClearingAccount;
    protected boolean displayOnDisbursementVoucherDocument;
    protected boolean displayOnVendorDocument;
    protected boolean active = true;
    
    protected List<PaymentMethodChart> paymentMethodCharts = new ArrayList<PaymentMethodChart>();
    
    protected Bank bank;
    
    @SuppressWarnings("unchecked")
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap<String, String> lhm = new LinkedHashMap<String, String>();
        lhm.put("paymentMethodCode", paymentMethodCode);
        lhm.put("paymentMethodName", paymentMethodName);
        return lhm;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public boolean isAssessedFees() {
        return assessedFees;
    }

    public void setAssessedFees(boolean assesedFees) {
        this.assessedFees = assesedFees;
    }

    public boolean isProcessedUsingPdp() {
        return processedUsingPdp;
    }

    public void setProcessedUsingPdp(boolean processedUsingPdp) {
        this.processedUsingPdp = processedUsingPdp;
    }

    public boolean isInterdepartmentalPayment() {
        return interdepartmentalPayment;
    }

    public void setInterdepartmentalPayment(boolean interdepartmentalPayment) {
        this.interdepartmentalPayment = interdepartmentalPayment;
    }

    public boolean isOffsetUsingClearingAccount() {
        return offsetUsingClearingAccount;
    }

    public void setOffsetUsingClearingAccount(boolean offsetUsingClearingAccount) {
        this.offsetUsingClearingAccount = offsetUsingClearingAccount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<PaymentMethodChart> getPaymentMethodCharts() {
        return paymentMethodCharts;
    }

    public void setPaymentMethodCharts(List<PaymentMethodChart> paymentMethodCharts) {
        this.paymentMethodCharts = paymentMethodCharts;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    /**
     * Returns the PaymentMethodChart record applicable for the given chart and transaction date.
     * 
     * @param chartOfAccountsCode
     * @param transDate The date of the transaction.  Used for comparing to the effective date on the records.  If null, the current date will be pulled from the DateTimeService.
     * @return Applicable PaymentMethodChart object.  <b>null</b> if none is found.
     */
    public PaymentMethodChart getPaymentMethodChartInfo( String chartOfAccountsCode, java.sql.Date transDate ) {
        if ( transDate == null ) {
            transDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
        }
        if ( ObjectUtils.isNotNull(getPaymentMethodCharts()) && chartOfAccountsCode != null ) {
            // pull the first one matching the chart where the date is before/equal to today
            // the ORM mapping ensures that these are retrieved in reverse date order,
            // so the first one found will be the effective entry
            for ( PaymentMethodChart pmc : getPaymentMethodCharts() ) {
                if ( StringUtils.equals(pmc.getChartOfAccountsCode(), chartOfAccountsCode)
                        && transDate.after(pmc.getEffectiveDate()) ) {
                    return pmc;
                }
            }
        }
        return null;
    }

    public boolean isDisplayOnDisbursementVoucherDocument() {
        return displayOnDisbursementVoucherDocument;
    }

    public void setDisplayOnDisbursementVoucherDocument(boolean displayOnDisbursementVoucherDocument) {
        this.displayOnDisbursementVoucherDocument = displayOnDisbursementVoucherDocument;
    }

    public boolean isDisplayOnVendorDocument() {
        return displayOnVendorDocument;
    }

    public void setDisplayOnVendorDocument(boolean displayOnVendorDocument) {
        this.displayOnVendorDocument = displayOnVendorDocument;
    }
    
}
