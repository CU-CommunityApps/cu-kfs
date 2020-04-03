package edu.cornell.kfs.module.purap.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.identity.PurapKimAttributes;
import org.kuali.kfs.module.purap.identity.RelatedDocumentDerivedRoleTypeServiceImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.role.service.impl.RouteLogDerivedRoleTypeServiceImpl;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleMembership.Builder;
import org.kuali.kfs.krad.util.KRADConstants;

public class CuRelatedDocumentDerivedRoleTypeServiceImpl extends RelatedDocumentDerivedRoleTypeServiceImpl {

    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(String namespaceCode, String roleName, Map<String,String> qualification) {
        List<RoleMembership> members = new ArrayList<RoleMembership>();
        if(qualification!=null && !qualification.isEmpty()){
            if (SOURCE_DOCUMENT_ROUTER_ROLE_NAME.equals(roleName)) {
                try {
                    PurchasingAccountsPayableDocument document = (PurchasingAccountsPayableDocument) documentService.getByDocumentHeaderId(qualification.get(KFSPropertyConstants.DOCUMENT_NUMBER));
                    if (document != null) {
                        PurchasingAccountsPayableDocument sourceDocument = document.getPurApSourceDocumentIfPossible();
                        if (sourceDocument != null && StringUtils.isNotBlank(sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId()) ) {
                            Map<String,String> roleQualifier = new HashMap<String,String>(1);
                            roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER, sourceDocument.getDocumentNumber() );
                            Builder roleMember = RoleMembership.Builder.create(null,null,sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId(),KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,roleQualifier);
                            members.add( roleMember.build());
                        }
                    }
                }
                catch (WorkflowException e) {
                    throw new RuntimeException("Unable to load document in getPrincipalIdsFromApplicationRole", e);
                }
            }
            else if (SENSITIVE_RELATED_DOCUMENT_INITATOR_OR_REVIEWER_ROLE_NAME.equals(roleName)) {
            	// KFSUPGRADE-346
                if (!qualification.containsKey(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)) {
                    Map<String,String> tempQualification = new HashMap<String,String>(1);
                    tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, qualification.get("documentNumber"));
                    for ( String principalId : roleService.getRoleMemberPrincipalIds(KRADConstants.KUALI_RICE_WORKFLOW_NAMESPACE, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
                        Builder roleMember = RoleMembership.Builder.create(null,null,principalId,KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,tempQualification);
                        members.add( roleMember.build());

                    }
                } else { 
                    for (String documentId : purapService.getRelatedDocumentIds(new Integer(qualification.get(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)))) {
                        Map<String,String> tempQualification = new HashMap<String,String>(1);
                        tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentId);
                        for ( String principalId : roleService.getRoleMemberPrincipalIds(KRADConstants.KUALI_RICE_WORKFLOW_NAMESPACE, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
                            Builder roleMember = RoleMembership.Builder.create(null,null,principalId,KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,tempQualification);
                            members.add( roleMember.build());

                        }
                    }
                }           
            }
        }
        return members;
    }

}
