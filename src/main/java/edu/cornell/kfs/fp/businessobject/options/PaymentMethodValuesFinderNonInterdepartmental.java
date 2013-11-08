package edu.cornell.kfs.fp.businessobject.options;

import edu.cornell.kfs.fp.CuFPConstants;


public class PaymentMethodValuesFinderNonInterdepartmental extends PaymentMethodValuesFinder {
    static {
        filterCriteria.put(CuFPConstants.INTERDEPARTMENTAL_PAYMENT, CuFPConstants.NO);
    }
}
