package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Map;
import java.util.Set;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;

@SuppressWarnings("deprecation")
public class IWantDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

    private static final long serialVersionUID = 1L;
    
    /*
     * Customization to ensure the canSendNoteFyi action is available when the canSendNoteFyi is true. 
     * This action might have been be removed in the super call if the canSendAdHocRequests returns false.
     */
    @Override
    public Set<String> getDocumentActions(Document document, Person user,
            Set<String> documentActionsFromPresentationController) {
        Set<String> documentActionsToReturn = super.getDocumentActions(document, user,
                documentActionsFromPresentationController);

        if (!documentActionsToReturn.contains(KRADConstants.KUALI_ACTION_CAN_SEND_NOTE_FYI) && canSendNoteFyi(document, user)) {
            documentActionsToReturn.add(KRADConstants.KUALI_ACTION_CAN_SEND_NOTE_FYI);
        }
        return documentActionsToReturn;
    }

    @Override
    protected void addPermissionDetails(Object dataObject, Map<String, String> attributes) {
        super.addPermissionDetails(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    @Override
    protected void addRoleQualification(Object dataObject, Map<String, String> attributes) {
        super.addRoleQualification(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    /*
     * Only approvers should be able to ad hoc route for approval.
     */
    @Override
    public boolean canSendAnyTypeAdHocRequests(Document document, Person user) {
        return canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_APPROVE_REQ, user);
    }
    

    /*
     * CU Customization (KFSPTS-2270): Updated authorizer to allow editing of document overview/description
     * by more users than just the initiator.
     */
    @Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document,
                KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT,
                user.getPrincipalId());
    }
    
    @Override
    public boolean canSave(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canSave(document, user);
        }
    }
    
    @Override
    public boolean canReload(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canReload(document, user);
        }
    }
    
    @Override
    public boolean canClose(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canClose(document, user);
        }
    }
    
    @Override
    public boolean canCopy(Document document, Person user) {
        if (isDocumentAtConfirmationStep(document)) {
            return false;
        } else {
            return super.canCopy(document, user);
        }
    }
    
    private boolean isDocumentAtConfirmationStep(Document document) {
        IWantDocument iWantDocument = (IWantDocument) document;
        return CUPurapConstants.IWantDocumentSteps.CONFIRM_STEP.equalsIgnoreCase(iWantDocument.getStep());
    }
}
