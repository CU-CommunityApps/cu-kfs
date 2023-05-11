package edu.cornell.kfs.sys.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.context.SpringContext;

public class PaymentMethodExtendedAttribute extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    
    private String paymentMethodCode;
    private boolean assessedFees;
    private boolean processedUsingPdp;
    private boolean displayOnVendorDocument;
    private boolean displayOnRecurringDVDocument;
    
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
