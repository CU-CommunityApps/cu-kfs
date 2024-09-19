package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class PaymentMethodExtendedAttribute extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    
    private String paymentMethodCode;
    private boolean processedUsingPdp;
    private boolean displayOnRecurringDVDocument;
    private String additionalCreditMemoDataCode;

    public boolean isProcessedUsingPdp() {
        return processedUsingPdp;
    }

    public void setProcessedUsingPdp(boolean processedUsingPdp) {
        this.processedUsingPdp = processedUsingPdp;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public boolean isDisplayOnRecurringDVDocument() {
        return displayOnRecurringDVDocument;
    }

    public void setDisplayOnRecurringDVDocument(boolean displayOnRecurringDVDocument) {
        this.displayOnRecurringDVDocument = displayOnRecurringDVDocument;
    }

    public String getAdditionalCreditMemoDataCode() {
        return additionalCreditMemoDataCode;
    }

    public void setAdditionalCreditMemoDataCode(String additionalCreditMemoDataCode) {
        this.additionalCreditMemoDataCode = additionalCreditMemoDataCode;
    }

}
