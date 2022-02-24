package edu.cornell.kfs.sys.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.businessobject.FavoriteAccount;
import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileMaintainableImpl extends FinancialSystemMaintainable {

	@Override
	public void setGenerateDefaultValues(String docTypeName) {
        super.setGenerateDefaultValues(docTypeName);
        if (getMaintenanceAction() == null || StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION,getMaintenanceAction())) {
        	((UserProcurementProfile)getBusinessObject()).setPrincipalId( GlobalVariables.getUserSession().getPrincipalId());
        }
    	
	}

 
    @Override
    public List<Section> getCoreSections(MaintenanceDocument document, Maintainable oldMaintainable) {
		boolean hasRole = SpringContext.getBean(UserProcurementProfileValidationService.class).canMaintainUserProcurementProfile();
    
    	List<Section> sections = super.getCoreSections(document, oldMaintainable);
    	// if it is not 'super user', then default to user him/herself and disable person lookup
      if ( StringUtils.equals(document.getNewMaintainableObject().getMaintenanceAction(),KRADConstants.MAINTENANCE_EDIT_ACTION)
    		  || (!hasRole && StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION, document.getNewMaintainableObject().getMaintenanceAction()))) {
    	  for (Section section : sections) {
    		  for (Row row : section.getRows()) {
    			  for (Field field : row.getFields()) {
                      if (StringUtils.equalsIgnoreCase(field.getPropertyName(),"profileUser.principalName")) {
                    	  field.setReadOnly(true);
                    	 field.setQuickFinderClassNameImpl("");
                      }
    			  }
    		  }
    	  }
      }
    	return sections;
    }


	@Override
	public void prepareForSave() {
		// clean account, otherwise it will be saved in maintdoc.  this will cause some issue.
		for (FavoriteAccount account : ((UserProcurementProfile)getBusinessObject()).getFavoriteAccounts()) {
			account.setAccount(null);
		}
		super.prepareForSave();
	}


	@Override
	public void saveBusinessObject() {
		// TODO Auto-generated method stub
		if (StringUtils.equals(getMaintenanceAction(),
				KRADConstants.MAINTENANCE_EDIT_ACTION)) {
			Map<String, Object> pkMap = new HashMap<String, Object>();
			pkMap.put("userProfileId", ((UserProcurementProfile) getBusinessObject()).getUserProfileId());
			UserProcurementProfile userProfile = (UserProcurementProfile) getBusinessObjectService()
					.findByPrimaryKey(UserProcurementProfile.class, pkMap);
			List<FavoriteAccount> deletedAccounts = new ArrayList<FavoriteAccount>();
			if (ObjectUtils.isNotNull(userProfile) && CollectionUtils.isNotEmpty(userProfile.getFavoriteAccounts())) {
				for (FavoriteAccount account : userProfile.getFavoriteAccounts()) {
					boolean accountFound = false;
					if (CollectionUtils.isNotEmpty(((UserProcurementProfile) getBusinessObject()).getFavoriteAccounts())) {
						for (FavoriteAccount account1 : ((UserProcurementProfile) getBusinessObject()).getFavoriteAccounts()) {
							if (account1.getAccountLineIdentifier() != null&& account.getAccountLineIdentifier().equals(account1.getAccountLineIdentifier())) {
								accountFound = true;
								break;
							}
						}
					}
					if (!accountFound) {
						deletedAccounts.add(account);
					}
				}
				if (CollectionUtils.isNotEmpty(deletedAccounts)) {
					getBusinessObjectService().delete(deletedAccounts);
				}
			}

		}
		super.saveBusinessObject();
	}


}
