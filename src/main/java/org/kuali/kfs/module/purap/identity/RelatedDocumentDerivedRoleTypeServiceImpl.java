/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.identity;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.kim.role.DerivedRoleTypeServiceBase;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.role.service.impl.RouteLogDerivedRoleTypeServiceImpl;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleMembership.Builder;
import org.kuali.rice.kim.api.role.RoleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelatedDocumentDerivedRoleTypeServiceImpl extends DerivedRoleTypeServiceBase {

    protected static final String SOURCE_DOCUMENT_ROUTER_ROLE_NAME = "Source Document Router";
    protected static final String SENSITIVE_RELATED_DOCUMENT_INITATOR_OR_REVIEWER_ROLE_NAME =
            "Sensitive Related Document Initiator Or Reviewer";

    // CU customizations change access from private to protected
    protected DocumentService documentService;
    protected PurapService purapService;
    protected RoleService roleService;

    /**
     * This service will accept the following attributes: Document Number Context: An fyi to the initiator - in the
     * case of Automatic Purchase Orders (apo), the fyi is supposed to go to the requisition router. Otherwise, it
     * should go to the PO router. Requirements: - KFS-PURAP Source Document Router - for Automated Purchase Order,
     * Requisition router according to KR-WKFLW Router role / for normal Purchase Order, Purchase Order router
     * according to KR-WKFLW Router
     */
    @Override
    public List<RoleMembership> getRoleMembersFromDerivedRole(String namespaceCode, String roleName,
            Map<String, String> qualification) {
        List<RoleMembership> members = new ArrayList<>();
        if (qualification != null && !qualification.isEmpty()) {
            if (SOURCE_DOCUMENT_ROUTER_ROLE_NAME.equals(roleName)) {
                try {
                    PurchasingAccountsPayableDocument document =
                            (PurchasingAccountsPayableDocument) documentService
                                    .getByDocumentHeaderId(qualification.get(KFSPropertyConstants.DOCUMENT_NUMBER));
                    if (document != null) {
                        PurchasingAccountsPayableDocument sourceDocument = document.getPurApSourceDocumentIfPossible();
                        if (sourceDocument != null && StringUtils.isNotBlank(sourceDocument.getDocumentHeader()
                                .getWorkflowDocument().getRoutedByPrincipalId())) {
                            Map<String, String> roleQualifier = new HashMap<>(1);
                            roleQualifier.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER,
                                    sourceDocument.getDocumentNumber());
                            Builder roleMember = RoleMembership.Builder.create(null, null,
                                    sourceDocument.getDocumentHeader().getWorkflowDocument().getRoutedByPrincipalId(),
                                    KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE, roleQualifier);
                            members.add(roleMember.build());
                        }
                    }
                } catch (WorkflowException e) {
                    throw new RuntimeException("Unable to load document in getPrincipalIdsFromApplicationRole", e);
                }
            } else if (SENSITIVE_RELATED_DOCUMENT_INITATOR_OR_REVIEWER_ROLE_NAME.equals(roleName)) {
                for (String documentId : purapService.getRelatedDocumentIds(new Integer(qualification.get(
                        PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER)))) {
                    Map<String, String> tempQualification = new HashMap<>(1);
                    tempQualification.put(KFSPropertyConstants.DOCUMENT_NUMBER, documentId);
                    for (String principalId : roleService.getRoleMemberPrincipalIds(
                            KRADConstants.KUALI_RICE_WORKFLOW_NAMESPACE,
                            RouteLogDerivedRoleTypeServiceImpl.INITIATOR_OR_REVIEWER_ROLE_NAME, tempQualification)) {
                        Builder roleMember = RoleMembership.Builder.create(null, null, principalId,
                                KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE, tempQualification);
                        members.add(roleMember.build());
                    }
                }
            }
        }
        return members;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setPurapService(PurapService purapService) {
        this.purapService = purapService;
    }

    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }
}
