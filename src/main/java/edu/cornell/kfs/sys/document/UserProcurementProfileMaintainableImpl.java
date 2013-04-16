package edu.cornell.kfs.sys.document;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;
import org.kuali.rice.kns.web.ui.Field;
import org.kuali.rice.kns.web.ui.Row;
import org.kuali.rice.kns.web.ui.Section;

import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileMaintainableImpl extends FinancialSystemMaintainable {

	@Override
	public void setGenerateDefaultValues(String docTypeName) {
		// TODO Auto-generated method stub
        super.setGenerateDefaultValues(docTypeName);
        if (getMaintenanceAction() == null || StringUtils.equals(KNSConstants.MAINTENANCE_NEW_ACTION,getMaintenanceAction())) {
        	((UserProcurementProfile)getBusinessObject()).setPrincipalId( GlobalVariables.getUserSession().getPrincipalId());
        }
    	
	}

 
    @Override
    public List<Section> getCoreSections(MaintenanceDocument document, Maintainable oldMaintainable) {
		boolean hasRole = SpringContext.getBean(UserProcurementProfileValidationService.class).canMaintainUserProcurementProfile();
    
    	List<Section> sections = super.getCoreSections(document, oldMaintainable);
    	// if it is not 'super user', then default to user him/herself and disable person lookup
      if ( StringUtils.equals(document.getNewMaintainableObject().getMaintenanceAction(),KNSConstants.MAINTENANCE_EDIT_ACTION)
    		  || (!hasRole && StringUtils.equals(KNSConstants.MAINTENANCE_NEW_ACTION, document.getNewMaintainableObject().getMaintenanceAction()))) {
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


}
