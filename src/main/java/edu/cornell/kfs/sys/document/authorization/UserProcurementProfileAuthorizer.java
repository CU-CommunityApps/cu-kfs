package edu.cornell.kfs.sys.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.sys.businessobject.UserProcurementProfile;
import edu.cornell.kfs.sys.service.UserProcurementProfileValidationService;

@SuppressWarnings("deprecation")
public class UserProcurementProfileAuthorizer  extends MaintenanceDocumentAuthorizerBase {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Set<String> getDocumentActions(Document document, Person user,
			Set<String> documentActions) {
		Set<String> documentActions1 = super.getDocumentActions(document, user, documentActions);
		if (documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_EDIT)
				&& !documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_SAVE)) {
			documentActions1.add(KRADConstants.KUALI_ACTION_CAN_SAVE);
		}
		if (documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE)) {
			documentActions1.remove(KRADConstants.KUALI_ACTION_CAN_BLANKET_APPROVE);
		}

		if (documentActions1.contains(KRADConstants.KUALI_ACTION_CAN_EDIT)) {
			MaintenanceDocument maintDoc = (MaintenanceDocument)document;
			if (StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KRADConstants.MAINTENANCE_EDIT_ACTION)
					|| StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KRADConstants.MAINTENANCE_COPY_ACTION)) {
				boolean hasRole = SpringContext.getBean(UserProcurementProfileValidationService.class).canMaintainUserProcurementProfile();
				UserProcurementProfile userProfile = (UserProcurementProfile) maintDoc.getNewMaintainableObject().getBusinessObject();
				if (!hasRole
						&& (!StringUtils.equals(userProfile.getPrincipalId(),GlobalVariables.getUserSession().getPrincipalId())
								|| StringUtils.equals(maintDoc.getNewMaintainableObject().getMaintenanceAction(),KRADConstants.MAINTENANCE_COPY_ACTION))) {

					throw new AuthorizationException(user.getName(), maintDoc.getNewMaintainableObject().getMaintenanceAction() ," UserProcurementProfile");
				}
			}
		}
		return documentActions1;
	}

}
