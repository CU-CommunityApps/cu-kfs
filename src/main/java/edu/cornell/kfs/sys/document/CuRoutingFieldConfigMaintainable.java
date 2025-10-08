package edu.cornell.kfs.sys.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.sys.document.RoutingFieldConfigMaintainable;
import org.kuali.kfs.vnd.VendorConstants;

public class CuRoutingFieldConfigMaintainable extends RoutingFieldConfigMaintainable {
    
    protected static final List<String> CU_VENDOR_DOC_TYPE_FIELD_NAMES = List.of(
            "extension.insuranceRequiredIndicator",
            "extension.insuranceRequirementsCompleteIndicator",
            "extension.cornellAdditionalInsuredIndicator",
            "extension.generalLiabilityCoverageAmount",
            "extension.generalLiabilityExpiration",
            "extension.automobileLiabilityCoverageAmount",
            "extension.automobileLiabilityExpiration",
            "extension.workmansCompCoverageAmount",
            "extension.workmansCompExpiration",
            "extension.excessLiabilityUmbExpiration",
            "extension.excessLiabilityUmbrellaAmount",
            "extension.healthOffSiteCateringLicenseReq",
            "extension.healthOffSiteLicenseExpirationDate",
            "extension.insuranceNotes",
            "extension.merchantNotes",
            "defaultPaymentMethodCode"

            );
    
    @Override
    public Map<String, List<String>> fieldNames() {
        Map<String, List<String>> fieldNames = super.fieldNames();
        List<String> vendorFields = new ArrayList<>(fieldNames.get(VendorConstants.VENDOR_DOC_TYPE));
        vendorFields.addAll(CU_VENDOR_DOC_TYPE_FIELD_NAMES);
        
        // Create mutable map to preserve all parent fields
        Map<String, List<String>> newFieldNames = new HashMap<>(fieldNames);
        newFieldNames.put(VendorConstants.VENDOR_DOC_TYPE, vendorFields);
        
        return newFieldNames;
    }

}
