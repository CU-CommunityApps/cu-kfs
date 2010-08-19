package edu.cornell.kfs.vnd.document.authorization;

import java.util.Set;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.authorization.VendorDocumentPresentationController;
import org.kuali.rice.kns.document.MaintenanceDocument;

public class CUVendorDocumentPresentationController extends VendorDocumentPresentationController {

	@Override
	public Set<String> getConditionallyReadOnlyPropertyNames(MaintenanceDocument document) {
		Set<String> conditionallyReadOnlyPropertyNames = super.getConditionallyReadOnlyPropertyNames(document);
		
		VendorDetail vendor = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
		
		if (!vendor.isVendorParentIndicator()) {
			conditionallyReadOnlyPropertyNames.add("vendorHeader.extension.w9ReceivedDate");
		}
		
		return conditionallyReadOnlyPropertyNames;
	}
	
}
