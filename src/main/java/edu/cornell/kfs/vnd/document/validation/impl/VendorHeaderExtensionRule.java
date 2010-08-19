package edu.cornell.kfs.vnd.document.validation.impl;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.validation.impl.VendorRule;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorHeaderExtendedAttribute;

public class VendorHeaderExtensionRule extends VendorRule {

	protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorHeaderExtensionRule.class);
	
	@Override
	protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
		boolean success = super.processCustomRouteDocumentBusinessRules(document);
		
		success &= checkW9ReceivedIndicatorAndDate(document);
		
		return success;
	}
	
	protected boolean checkW9ReceivedIndicatorAndDate(MaintenanceDocument document) {
		boolean success = true;
		
		VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();		
		VendorHeader vendorHeader = vendorDetail.getVendorHeader();
		
		boolean vendorW9ReceivedIndicator = false;
		
		if (vendorHeader.getVendorW9ReceivedIndicator()!= null) {
			vendorW9ReceivedIndicator =	vendorHeader.getVendorW9ReceivedIndicator();
		}

		VendorHeaderExtendedAttribute vhea = (VendorHeaderExtendedAttribute) vendorHeader.getExtension();
		Date w9ReceivedDate = vhea.getW9ReceivedDate();
		
		if (vendorW9ReceivedIndicator && (w9ReceivedDate == null) ) {
			success = false;
			putFieldError("vendorHeader.extension.w9ReceivedDate", CUKFSKeyConstants.ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_NOT_POPULATED);
		}
		
		if ( (!vendorW9ReceivedIndicator) && (w9ReceivedDate!=null)) {
			success = false;
			putFieldError("vendorHeader.vendorW9ReceivedIndicator", CUKFSKeyConstants.ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_POPULATED_W_O_IND);
		}			
		
		return success;
	}
}
