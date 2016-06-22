package edu.cornell.kfs.fp.document.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.CuFPConstants;

public class RecurringDisbursementVoucherDocumentPresentationController
		extends CuDisbursementVoucherDocumentPresentationController {
	
	private static final long serialVersionUID = 7036832064326279968L;
	protected static Log LOG = LogFactory.getLog(RecurringDisbursementVoucherDocumentPresentationController.class);
	protected PermissionService permissionService;

	@Override
	public boolean canEdit(Document document) {
		boolean canEdit = super.canEdit(document) && hasRecurringDVPerm();
		return canEdit;
	}
	
	@Override
	public boolean canInitiate(String documentTypeName) {
		boolean canInit = super.canInitiate(documentTypeName) && hasRecurringDVPerm() ;
		return canInit;
	}
	
	protected boolean hasRecurringDVPerm() {
		return getPermissionService().hasPermission(GlobalVariables.getUserSession().getPrincipalId(), 
				KFSConstants.CoreModuleNamespaces.FINANCIAL, CuFPConstants.PermissionNames.RECURRING_DV_PERMISSION.name);
	}
	
	public PermissionService getPermissionService() {
		if (permissionService == null) {
			permissionService = SpringContext.getBean(PermissionService.class);
		}
		return permissionService;
	}
	
	public void setpermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

}
