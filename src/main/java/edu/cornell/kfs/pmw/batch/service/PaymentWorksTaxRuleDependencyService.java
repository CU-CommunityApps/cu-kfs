package edu.cornell.kfs.pmw.batch.service;

import java.util.List;
import java.util.Map;

import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

public interface PaymentWorksTaxRuleDependencyService {
    KfsVendorDataWrapper populateTaxRuleDependentAttributes(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap);

}
