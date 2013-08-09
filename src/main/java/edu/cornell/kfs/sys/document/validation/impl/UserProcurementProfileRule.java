package edu.cornell.kfs.sys.document.validation.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.krad.util.KRADConstants;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileRule extends MaintenanceDocumentRuleBase {
	private UserProcurementProfileValidationService userProcurementProfileValidationService;

	@Override
	protected boolean processCustomSaveDocumentBusinessRules(
			MaintenanceDocument document) {
		// TODO Auto-generated method stub

		return super.processCustomSaveDocumentBusinessRules(document);
	}

	@Override
	protected boolean processCustomRouteDocumentBusinessRules(
			MaintenanceDocument document) {
		// TODO Auto-generated method stub
		boolean valid =  super.processCustomRouteDocumentBusinessRules(document);
		UserProcurementProfile userProcurementProfile = (UserProcurementProfile)document.getNewMaintainableObject().getBusinessObject();
    	if (CollectionUtils.isNotEmpty(userProcurementProfile.getFavoriteAccounts())) {
			valid &= getUserProcurementProfileValidationService().validateAccounts(userProcurementProfile.getFavoriteAccounts());
     	}
    	if (StringUtils.equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION,document.getNewMaintainableObject().getMaintenanceAction()) ||
    			StringUtils.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION,document.getNewMaintainableObject().getMaintenanceAction())) {
    		valid &= !getUserProcurementProfileValidationService().validateUserProfileExist(((UserProcurementProfile)document.getNewMaintainableObject().getBusinessObject()).getPrincipalId());
    	}
    	if (valid && StringUtils.equalsIgnoreCase(KRADConstants.MAINTENANCE_COPY_ACTION,document.getNewMaintainableObject().getMaintenanceAction())) {
    		resetAccountId((UserProcurementProfile)document.getNewMaintainableObject().getBusinessObject());
    	}
    	return valid;
	}
	
	/* if this is a copy
	 * reset this pk.  otherwise, it will wipe out the copied from accounts
	 */
	private void resetAccountId(UserProcurementProfile userProcurementProfile) {
		if (CollectionUtils.isNotEmpty(userProcurementProfile.getFavoriteAccounts())) {
		for (FavoriteAccount account : userProcurementProfile.getFavoriteAccounts()) {
			account.setAccountLineIdentifier(null);
		}
		}
	}
	
	public UserProcurementProfileValidationService getUserProcurementProfileValidationService() {
		if (userProcurementProfileValidationService == null) {
			setUserProcurementProfileValidationService(SpringContext.getBean(UserProcurementProfileValidationService.class));
		}
		return userProcurementProfileValidationService;
	}

	public void setUserProcurementProfileValidationService(
			UserProcurementProfileValidationService userProcurementProfileValidationService) {
		this.userProcurementProfileValidationService = userProcurementProfileValidationService;
	}

}
