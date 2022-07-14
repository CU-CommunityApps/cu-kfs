package edu.cornell.kfs.module.purap.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.role.service.impl.RouteLogDerivedRoleTypeServiceImpl;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.api.role.RoleMembership.Builder;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.identity.PurapKimAttributes;
import org.kuali.kfs.module.purap.identity.RelatedDocumentDerivedRoleTypeServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

public class CuRelatedDocumentDerivedRoleTypeServiceImpl extends RelatedDocumentDerivedRoleTypeServiceImpl {

    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(String namespaceCode, String roleName, Map<String,String> qualification) {
        List<RoleMembership> members = new ArrayList<RoleMembership>();
        if(qualification!=null && !qualification.isEmpty()){
            if (SOURCE_DOCUMENT_ROUTER_ROLE_NAME.equals(roleName)) {
                PurchasingAccountsPayableDocument document = (PurchasingAccountsPayableDocument) getDocumentService().getByDocumentHeaderId(qualification.get(KFSPropertyConstants.DOCUMENT_NUMBER));
                if (document != null) {
                    PurchasingAccountsPayableDocument sourceDocument = document.getPurApSourceDocumentIfPossible();
                    if (sourceDocument != null && StringUtils.isNotBlank(sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId()) ) {
                        Map<String,String> roleQualifier = new HashMap<String,String>(1);
                        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER, sourceDocument.getDocumentNumber() );
                        Builder roleMember = Builder.create(null,null,sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId(),KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,roleQualifier);
                        members.add( roleMember.build());
                    }
                }
            }
            else if (SENSITIVE_RELATED_DOCUMENT_INITATOR_OR_REVIEWER_ROLE_NAME.equals(roleName)) {
            	// KFSUPGRADE-346
                if (!qualification.containsKey(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)) {
                    Map<String,String> tempQualification = new HashMap<String,String>(1);
                    tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, qualification.get("documentNumber"));
                    for ( String principalId : getRoleService().getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.WORKFLOW, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
                        Builder roleMember = Builder.create(null,null,principalId,KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,tempQualification);
                        members.add( roleMember.build());

                    }
                } else { 
                    for (String documentId : getPurapService().getRelatedDocumentIds(new Integer(qualification.get(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)))) {
                        Map<String,String> tempQualification = new HashMap<String,String>(1);
                        tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentId);
                        for ( String principalId : getRoleService().getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.WORKFLOW, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
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
