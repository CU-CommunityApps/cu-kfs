package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.sys.PaymentMethodAdditionalDocumentData;

public class PaymentMethodExtendedAttribute extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    
    private String paymentMethodCode;
    private boolean processedUsingPdp;
    private boolean displayOnRecurringDVDocument;
    private String additionalCreditMemoDataCode;
    private String additionalCreditMemoDataLabel;

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
    
    /**
     * Get a displayable version of the {@code additionalCreditMemoDataCode}.  This value is determined by
     * using the label for the corresponding {@link PaymentMethodAdditionalDocumentData} enum.
     * @return a displayable value for the {@code additionalCreditMemoDataCode}
     * @see #getAdditionalCreditMemoDataCode()
     */
    public String getAdditionalCreditMemoDataLabel() {
        return PaymentMethodAdditionalDocumentData.forCode(additionalCreditMemoDataCode)
                .map(PaymentMethodAdditionalDocumentData::getLabel)
                .orElse("");
    }

}
