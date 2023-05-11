package org.kuali.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.businessobject.PaymentMethodChart;

public class PaymentMethodExtendedAttribute extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    
    private String paymentMethodCode;
    private boolean assessedFees;
    private boolean processedUsingPdp;
    private boolean displayOnVendorDocument;
    private boolean displayOnRecurringDVDocument;
    
    protected List<PaymentMethodChart> paymentMethodCharts = new ArrayList<PaymentMethodChart>();
    
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
    
    public List<PaymentMethodChart> getPaymentMethodCharts() {
        return paymentMethodCharts;
    }

    public void setPaymentMethodCharts(List<PaymentMethodChart> paymentMethodCharts) {
        this.paymentMethodCharts = paymentMethodCharts;
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

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public boolean isDisplayOnVendorDocument() {
        return displayOnVendorDocument;
    }

    public void setDisplayOnVendorDocument(boolean displayOnVendorDocument) {
        this.displayOnVendorDocument = displayOnVendorDocument;
    }

    public boolean isDisplayOnRecurringDVDocument() {
        return displayOnRecurringDVDocument;
    }

    public void setDisplayOnRecurringDVDocument(boolean displayOnRecurringDVDocument) {
        this.displayOnRecurringDVDocument = displayOnRecurringDVDocument;
    }

}
