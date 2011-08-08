/**
 * 
 */
package edu.cornell.kfs.vnd.businessobject.authorization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.IdentityManagementService;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.inquiry.InquiryPresentationControllerBase;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KNSConstants;

/**
 * @author kwk43
 *
 */
public class VendorDetailInquiryPresentationController extends InquiryPresentationControllerBase {

	/**
	 * Implement this method to hide fields based on specific data in the record being inquired into
	 * 
	 * @return Set of property names that should be hidden
	 */
	@Override
	public Set<String> getConditionallyHiddenPropertyNames(BusinessObject businessObject) {
		Set<String> retVal = new HashSet<String>();
		
		IdentityManagementService idService = SpringContext.getBean(IdentityManagementService.class);
		UserSession uSession = GlobalVariables.getUserSession();
		
		AttributeSet permissionDetails = new AttributeSet();
        permissionDetails.put(KEWConstants.DOCUMENT_TYPE_NAME_DETAIL, "PVEN");
		
		boolean canViewAttachments = idService.isAuthorizedByTemplateName(uSession.getPrincipalId(), KNSConstants.KNS_NAMESPACE, KimConstants.PermissionTemplateNames.VIEW_NOTE_ATTACHMENT, permissionDetails, null);
		if (!canViewAttachments) {
			
			VendorDetail detail = (VendorDetail)businessObject;

			List boNotes = detail.getBoNotes();
			for (int i = 0; i < boNotes.size(); i++)
				retVal.add("boNotes["+i+"].attachmentLink");
			
		}
		return retVal;
	}
	
}
