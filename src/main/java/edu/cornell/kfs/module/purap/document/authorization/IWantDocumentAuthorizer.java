package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Map;

import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.module.purap.document.IWantDocument;

@SuppressWarnings("deprecation")
public class IWantDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

    private static final long serialVersionUID = 1L;

    @Override
    protected void addPermissionDetails(Object dataObject, Map<String, String> attributes) {
        super.addPermissionDetails(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KfsKimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    @Override
    protected void addRoleQualification(Object dataObject, Map<String, String> attributes) {
        super.addRoleQualification(dataObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) dataObject;
        attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KfsKimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }

    /*
     * Only allow ad hoc approvals, and only to authorized users.
     */
    @Override
    public boolean canSendAdHocRequests(Document document, String actionRequestCd, Person user) {
        if (!KewApiConstants.ACTION_REQUEST_APPROVE_REQ.equals(actionRequestCd)) {
            return false;
        }
        return super.canSendAdHocRequests(document, actionRequestCd, user);
    }

    /*
     * CU Customization (KFSPTS-2270): Updated authorizer to allow editing of document overview/description
     * by more users than just the initiator.
     */
    @Override
    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document,
                KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.EDIT_DOCUMENT,
                user.getPrincipalId());
    }
}
