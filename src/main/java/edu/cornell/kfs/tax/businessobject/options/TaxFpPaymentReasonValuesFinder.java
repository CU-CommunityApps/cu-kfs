package edu.cornell.kfs.tax.businessobject.options;

import java.util.List;

import org.kuali.kfs.fp.businessobject.options.PaymentReasonValuesFinder;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;

import edu.cornell.kfs.tax.CUTaxConstants;

/**
 * Subclass of PaymentReasonValuesFinder that
 * changes the first key/value pair to map it
 * to "*" instead of a blank value.
 */
public class TaxFpPaymentReasonValuesFinder extends PaymentReasonValuesFinder {
    private static final long serialVersionUID = 6374289567230286527L;

    @SuppressWarnings("unchecked")
    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = super.getKeyValues();
        keyValues.set(0, new ConcreteKeyValue(CUTaxConstants.ANY_OR_NONE_PAYMENT_REASON, "Any/None"));
        return keyValues;
    }

}
