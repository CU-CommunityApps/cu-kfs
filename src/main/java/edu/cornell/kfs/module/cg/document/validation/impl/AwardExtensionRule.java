package edu.cornell.kfs.module.cg.document.validation.impl;

import org.kuali.kfs.module.cg.document.validation.impl.AwardRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class AwardExtensionRule extends AwardRule {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
    	boolean success = true;
    	success &= super.processCustomRouteDocumentBusinessRules(document);
    	
    	success &= checkFinalFinancialReportRequired();
    	
    	return success;
    }
    
    protected boolean checkFinalFinancialReportRequired() {
    	boolean success = true;
    	
    	AwardExtendedAttribute awardExtendedAttributeNew = (AwardExtendedAttribute) super.getNewBo().getExtension();
    	AwardExtendedAttribute awardExtendedAttributeOld = (AwardExtendedAttribute) super.getOldBo().getExtension();

    	if (awardExtendedAttributeNew.isFinalFinancialReportRequired() && null==awardExtendedAttributeNew.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", KFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	if (awardExtendedAttributeOld.isFinalFinancialReportRequired() && null==awardExtendedAttributeOld.getFinalFiscalReportDate()) {
    		success = false;
    		putFieldError("extension.finalFiscalReportDate", KFSKeyConstants.ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED);
    	}

    	
    	return success;
    }

}
