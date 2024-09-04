package edu.cornell.kfs.fp.businessobject.options;

import org.kuali.kfs.sys.businessobject.options.PaymentMethodValuesFinderBase;

public class RecurringDisbursementVoucherPaymentMethodValuesFinder extends PaymentMethodValuesFinderBase {
    
    private static final String DISPLAY_ON_RECURRING_DV_DOCUMENT = "extension.displayOnRecurringDVDocument";

    public RecurringDisbursementVoucherPaymentMethodValuesFinder() {
        super(DISPLAY_ON_RECURRING_DV_DOCUMENT, false);
    }  

}
