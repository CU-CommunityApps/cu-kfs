package edu.cornell.kfs.pdp.document.validation.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.validation.impl.PayeeAchAccountRule;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;

public class CUPayeeAchAccountRule extends PayeeAchAccountRule {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CUPayeeAchAccountRule.class);
	
	protected BusinessObjectService businessObjectService;
	
	
	/**
	 * Override to only find active records for duplication check.
	 */
	@Override
	protected boolean checkForDuplicateRecord() {
		LOG.info("checkForDuplicateRecord, entering");
        boolean valid = true;

        if (areACHAccountGeneratedIdentifiersNotNull() && areACHAccountGeneratedIdentifiersTheSame() && 
        	arePayeeIdentifierTypeCodesTheSame() && areAchTransactionTypesTheSame() && arePayeeIdNumbersTheSame()) {
        	LOG.info("checkForDuplicateRecord, attributes are the same, returning " + valid);
        	return valid;
        }

        // check for a duplicate record if creating a new record or editing an old one and above mentioned conditions are not true
        Map<String, Object> criteria = new HashMap<String, Object>();

        criteria.put(PdpPropertyConstants.ACH_TRANSACTION_TYPE, newPayeeAchAccount.getAchTransactionType());
        criteria.put(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, newPayeeAchAccount.getPayeeIdentifierTypeCode());
        criteria.put(PdpPropertyConstants.PAYEE_ID_NUMBER, newPayeeAchAccount.getPayeeIdNumber());
        criteria.put("active", true);

        int matches = getBusinessObjectService().countMatching(PayeeACHAccount.class, criteria);
        if (matches > 0) {
        	LOG.info("checkForDuplicateRecord, Found a duplicate record, setting VALID to false");
            putFieldError(PdpPropertyConstants.PAYEE_ID_NUMBER, KFSKeyConstants.ERROR_DOCUMENT_PAYEEACHACCOUNTMAINT_DUPLICATE_RECORD);
            valid = false;
        }

        return valid;
    }

	protected boolean arePayeeIdNumbersTheSame() {
		return StringUtils.equals(newPayeeAchAccount.getPayeeIdNumber(), oldPayeeAchAccount.getPayeeIdNumber());
	}

	protected boolean areAchTransactionTypesTheSame() {
		return StringUtils.equals(newPayeeAchAccount.getAchTransactionType(), oldPayeeAchAccount.getAchTransactionType());
	}

	protected boolean arePayeeIdentifierTypeCodesTheSame() {
		return StringUtils.equals(newPayeeAchAccount.getPayeeIdentifierTypeCode(), oldPayeeAchAccount.getPayeeIdentifierTypeCode());
	}

	protected boolean areACHAccountGeneratedIdentifiersTheSame() {
		return newPayeeAchAccount.getAchAccountGeneratedIdentifier().equals(oldPayeeAchAccount.getAchAccountGeneratedIdentifier());
	}

	protected boolean areACHAccountGeneratedIdentifiersNotNull() {
		return newPayeeAchAccount.getAchAccountGeneratedIdentifier() != null && oldPayeeAchAccount.getAchAccountGeneratedIdentifier() != null;
	}

	public BusinessObjectService getBusinessObjectService() {
		if (businessObjectService == null) {
			businessObjectService = SpringContext.getBean(BusinessObjectService.class);
		}
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}
	
	

}
