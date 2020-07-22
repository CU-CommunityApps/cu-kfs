/**
 * 
 */
package edu.cornell.kfs.vnd.businessobject.authorization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.services.IdentityManagementService;
import org.kuali.kfs.kns.inquiry.InquiryPresentationControllerBase;
import org.kuali.kfs.krad.UserSession;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

/**
 * @author kwk43
 *
 */
@SuppressWarnings("deprecation")
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
		
		Map<String,String> permissionDetails = new HashMap<String,String>();
        permissionDetails.put(KewApiConstants.DOCUMENT_TYPE_NAME_DETAIL, "PVEN");
		
		boolean canViewAttachments = idService.isAuthorizedByTemplateName(uSession.getPrincipalId(), KRADConstants.KNS_NAMESPACE, KimConstants.PermissionTemplateNames.VIEW_NOTE_ATTACHMENT, permissionDetails, null);
		if (!canViewAttachments) {
			
			VendorDetail detail = (VendorDetail) businessObject;
		    VendorService vendorService = SpringContext.getBean(VendorService.class);
		    List<Note> boNotes = vendorService.getVendorNotes(detail);
			
		    for (int i = 0; i < boNotes.size(); i++)
				retVal.add("boNotes["+i+"].attachmentLink");
			
		}
		return retVal;
	}
	
}
