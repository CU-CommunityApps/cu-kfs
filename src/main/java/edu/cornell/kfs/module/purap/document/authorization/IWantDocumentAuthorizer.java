package edu.cornell.kfs.module.purap.document.authorization;

import java.util.Map;

import org.kuali.kfs.sys.document.authorization.FinancialSystemTransactionalDocumentAuthorizerBase;
import org.kuali.kfs.sys.identity.KfsKimAttributes;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.util.KimConstants;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.KNSConstants;

import edu.cornell.kfs.module.purap.document.IWantDocument;

public class IWantDocumentAuthorizer extends FinancialSystemTransactionalDocumentAuthorizerBase {

	@Override
    protected void addPermissionDetails(BusinessObject businessObject, Map<String, String> attributes) {
        
        super.addPermissionDetails(businessObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) businessObject;
        attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KfsKimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }
    
    @Override
    protected void addRoleQualification(BusinessObject businessObject, Map<String, String> attributes) {
        
        super.addRoleQualification(businessObject, attributes);
        IWantDocument iWantDocument = (IWantDocument) businessObject;
        attributes.put(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE, iWantDocument.getRoutingChart());
        attributes.put(KfsKimAttributes.ORGANIZATION_CODE, iWantDocument.getRoutingOrganization());
    }
	
	/*
     * CU Customization (KFSPTS-2270): Updated authorizer to allow editing of document overview/description
     * by more users than just the initiator.
     */
    @Override
    public boolean canEditDocumentOverview(Document document, Person user){
		return isAuthorizedByTemplate(document,
				KNSConstants.KNS_NAMESPACE,
				KimConstants.PermissionTemplateNames.EDIT_DOCUMENT,
				user.getPrincipalId());
    }
}
