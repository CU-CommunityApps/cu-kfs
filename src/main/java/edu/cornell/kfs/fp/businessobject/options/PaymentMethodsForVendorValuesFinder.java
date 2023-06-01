package edu.cornell.kfs.fp.businessobject.options;

import org.kuali.kfs.sys.businessobject.options.PaymentMethodValuesFinderBase;

public class PaymentMethodsForVendorValuesFinder extends PaymentMethodValuesFinderBase {
    private static final String DISPLAY_ON_VENDOR_DOCUMENT = "extension.displayOnVendorDocument";

    public PaymentMethodsForVendorValuesFinder() {
        super(DISPLAY_ON_VENDOR_DOCUMENT);
    }  
}
