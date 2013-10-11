package edu.cornell.kfs.vnd.document.authorization;

import java.util.Set;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.authorization.VendorDocumentPresentationController;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.vnd.CUVendorPropertyConstants;

public class CuVendorDocumentPresentationController extends VendorDocumentPresentationController {

    @Override
    public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
        Set<String> conditionallyReadonlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);
        VendorDetail vendor = (VendorDetail)document.getNewMaintainableObject().getBusinessObject();

        if (!vendor.isVendorParentIndicator()) {
            conditionallyReadonlyPropertyNames.add(CUVendorPropertyConstants.VENDOR_W9_RECEIVED_DATE);
        }

        return conditionallyReadonlyPropertyNames;
    }

}
