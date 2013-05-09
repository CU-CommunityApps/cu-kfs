package edu.cornell.kfs.sys.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.document.MaintenanceDocumentBase;
import org.kuali.rice.kns.exception.AuthorizationException;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;

import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

public class UserProcurementProfileAuthorizer  extends FinancialSystemMaintenanceDocumentAuthorizerBase {

	@Override
	public Set<String> getDocumentActions(Document document, Person user,
			Set<String> documentActions) {
		// TODO Auto-generated method stub
		Set<String> documentActions1 = super.getDocumentActions(document, user, documentActions);
		if (documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_EDIT)
				&& !documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_SAVE)) {
			documentActions1.add(KNSConstants.KUALI_ACTION_CAN_SAVE);
		}
		if (documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_BLANKET_APPROVE)) {
			documentActions1.remove(KNSConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
		}

		if (documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_EDIT)) {
			MaintenanceDocumentBase maintDoc = (MaintenanceDocumentBase)document;
			if (StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KNSConstants.MAINTENANCE_EDIT_ACTION)
					|| StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KNSConstants.MAINTENANCE_COPY_ACTION)) {
				boolean hasRole = SpringContext.getBean(UserProcurementProfileValidationService.class).canMaintainUserProcurementProfile();
				UserProcurementProfile userProfile = (UserProcurementProfile) maintDoc.getNewMaintainableObject().getBusinessObject();
				if (!hasRole
						&& (!StringUtils.equals(userProfile.getPrincipalId(),GlobalVariables.getUserSession().getPrincipalId())
								|| StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KNSConstants.MAINTENANCE_COPY_ACTION))) {

					throw new AuthorizationException(user.getName(), maintDoc.getNewMaintainableObject().getMaintenanceAction() ," UserProcurementProfile");
				}
			}
		}
		return documentActions1;
	}

}
