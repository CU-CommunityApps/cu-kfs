package edu.cornell.kfs.vnd.document.validation.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.VendorUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.vnd.document.validation.impl.VendorPreRules;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.vnd.CUVendorConstants;
import edu.cornell.kfs.vnd.CUVendorKeyConstants;
import edu.cornell.kfs.vnd.CUVendorPropertyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class CuVendorPreRules extends VendorPreRules {

    @Override
    protected boolean doCustomPreRules(final MaintenanceDocument document) {
        setupConvenienceObjects(document);
        setVendorNamesAndIndicator(document);
        setVendorRestriction(document);
        if (StringUtils.isBlank(question) || question.equals(VendorConstants.CHANGE_TO_PARENT_QUESTION_ID)) {
            detectAndConfirmChangeToParent(document);
        }
        if(!document.isNew()) {
            if (StringUtils.isBlank(question) || question.equals(CUVendorConstants.EXPIRED_DATE_QUESTION_ID)) {
            	detectAndConfirmExpirationDates(document);
            }
        }
        
        //check to page review ONLY if it is a new vendor
        if (newVendorDetail.getVendorHeaderGeneratedIdentifier() == null &&
                newVendorDetail.getVendorDetailAssignedIdentifier() == null) {
            displayReview(document);
        }
        return true;
    }

    protected void detectAndConfirmExpirationDates(final MaintenanceDocument document) {
        boolean proceed = true;
        
        final ArrayList<String> expired = new ArrayList<String>();
        
        final VendorDetail vendorDetail = (VendorDetail) document.getNewMaintainableObject().getBusinessObject();
        final VendorDetailExtension vendorDetailext = (VendorDetailExtension)vendorDetail.getExtension();
		if (vendorDetailext.getGeneralLiabilityExpiration()!= null && vendorDetailext.getGeneralLiabilityExpiration().before(new Date())) {
			expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.GENERAL_LIABILITY_COVERAGE);
	        GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+CUVendorPropertyConstants.GENERAL_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_IN_PAST);
		}

		if (vendorDetailext.getAutomobileLiabilityExpiration()!= null && vendorDetailext.getAutomobileLiabilityExpiration().before(new Date())) {
			expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.AUTOMOBILE_LIABILITY_COVERAGE);
	        GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+CUVendorPropertyConstants.AUTOMOBILE_LIABILITY_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_IN_PAST);
		}		

		if (vendorDetailext.getWorkmansCompExpiration()!= null && vendorDetailext.getWorkmansCompExpiration().before(new Date())) {
			expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.WORKMAN_COMP_COVERAGE);
	        GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+CUVendorPropertyConstants.WORKMANS_COMP_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_WC_EXPR_IN_PAST);
		}		
		
		if (vendorDetailext.getExcessLiabilityUmbExpiration()!= null && vendorDetailext.getExcessLiabilityUmbExpiration().before(new Date())) {
			expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.EXCESS_LIABILITY_UMBRELLA_COVERAGE);
	        GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+CUVendorPropertyConstants.EXCESS_LIABILITY_UMBRELLA_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_IN_PAST);
		}
        
		if (vendorDetailext.getHealthOffSiteLicenseExpirationDate()!= null && vendorDetailext.getHealthOffSiteLicenseExpirationDate().before(new Date())) {
	        expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.HEALTH_DEPARTMENT_LICENSE);
	        GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+CUVendorPropertyConstants.HEALTH_OFFSITE_LICENSE_EXPIRATION, CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_IN_PAST);
		}
		
		final VendorHeader vendorHeader = vendorDetail.getVendorHeader();
		final List<VendorSupplierDiversity> vendorSupplierDiversities = vendorHeader.getVendorSupplierDiversities();
                
        if (vendorSupplierDiversities.size() > 0) {
            int i = 0;
            for(final VendorSupplierDiversity vendor : vendorSupplierDiversities) {
                if (vendor.getCertificationExpirationDate().before( new Date() ) ) {
                	expired.add(CUVendorConstants.EXPIRABLE_COVERAGES.SUPPLIER_DIVERSITY_CERTIFICATION);
                    GlobalVariables.getMessageMap().putError(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE+VendorConstants.VENDOR_HEADER_ATTR+"."+
                    										 VendorPropertyConstants.VENDOR_SUPPLIER_DIVERSITIES+"[" + i + "]."+
                    										 CUVendorPropertyConstants.CERTIFICATION_EXPRIATION_DATE, 
                    										 CUVendorKeyConstants.ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST);
                    break;
                }
                i++;
            }
        }
		
		if(!expired.isEmpty()) {
			String expiredNames = "";
			for(final String name : expired) {
				expiredNames = expiredNames + "[br] - " + name;
			}
	        proceed &= askOrAnalyzeYesNoQuestion(CUVendorConstants.EXPIRED_DATE_QUESTION_ID, VendorUtils.buildMessageText(CUVendorKeyConstants.CONFIRM_VENDOR_DATE_EXPIRED, expiredNames));
		}
        
		
        if (!proceed) {
            abortRulesCheck();
        }
        else {
        	GlobalVariables.getMessageMap().clearErrorMessages();
        }
    }

}
