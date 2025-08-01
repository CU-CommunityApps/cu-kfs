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
    public List<RoleMembership> getRoleMembersFromDerivedRole(
            final String namespaceCode, final String roleName, final Map<String,String> qualification) {
        final List<RoleMembership> members = new ArrayList<RoleMembership>();
        if(qualification!=null && !qualification.isEmpty()){
            if (SOURCE_DOCUMENT_ROUTER_ROLE_NAME.equals(roleName)) {
                final PurchasingAccountsPayableDocument document = (PurchasingAccountsPayableDocument) getDocumentService().getByDocumentHeaderId(qualification.get(KFSPropertyConstants.DOCUMENT_NUMBER));
                if (document != null) {
                    final PurchasingAccountsPayableDocument sourceDocument = document.getPurApSourceDocumentIfPossible();
                    if (sourceDocument != null && StringUtils.isNotBlank(sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId()) ) {
                        final Map<String,String> roleQualifier = new HashMap<String,String>(1);
                        roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER, sourceDocument.getDocumentNumber() );
                        final Builder roleMember = Builder.create(null,null,sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId(),KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,roleQualifier);
                        members.add( roleMember.build());
                    }
                }
            }
            else if (SENSITIVE_RELATED_DOCUMENT_INITATOR_OR_REVIEWER_ROLE_NAME.equals(roleName)) {
            	// KFSUPGRADE-346
                if (!qualification.containsKey(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)) {
                    final Map<String,String> tempQualification = new HashMap<String,String>(1);
                    tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, qualification.get("documentNumber"));
                    for ( final String principalId : getRoleService().getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.WORKFLOW, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
                        final Builder roleMember = Builder.create(null,null,principalId,KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,tempQualification);
                        members.add( roleMember.build());

                    }
                } else { 
                    for (final String documentId : getPurapService().getRelatedDocumentIds(Integer.valueOf(qualification.get(
                            PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)))) {
                        final Map<String,String> tempQualification = new HashMap<String,String>(1);
                        tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentId);
                        for ( final String principalId : getRoleService().getRoleMemberPrincipalIds(KFSConstants.CoreModuleNamespaces.WORKFLOW, RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification) ) {
                            final Builder roleMember = RoleMembership.Builder.create(null,null,principalId,KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE,tempQualification);
                            members.add( roleMember.build());

                        }
                    }
                }           
            }
        }
        return members;
    }

}
