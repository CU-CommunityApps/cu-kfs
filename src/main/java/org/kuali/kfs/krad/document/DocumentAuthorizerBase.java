/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.krad.document;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.action.ActionType;
import org.kuali.kfs.kew.api.doctype.RoutePath;
import org.kuali.kfs.kew.engine.node.ProcessDefinition;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.bo.DataObjectAuthorizerBase;
import org.kuali.kfs.sys.KFSConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for {@link DocumentAuthorizer} that perform KIM permission checks to authorize the actions
 */
public class DocumentAuthorizerBase extends DataObjectAuthorizerBase implements DocumentAuthorizer {

    private static final long serialVersionUID = -5354518767379472681L;

    public static final String PRE_ROUTING_ROUTE_NAME = "PreRoute";

    public final boolean canInitiate(String documentTypeName, Person user) {
        String nameSpaceCode = KFSConstants.CoreModuleNamespaces.KFS;
        Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME, documentTypeName);

        return getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), nameSpaceCode,
            KimConstants.PermissionTemplateNames.INITIATE_DOCUMENT, permissionDetails,
            Collections.emptyMap());
    }

    public boolean canOpen(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.OPEN_DOCUMENT, user.getPrincipalId());
    }

    public boolean canEdit(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId());
    }

    public boolean canAnnotate(Document document, Person user) {
        return canEdit(document, user);
    }

    public boolean canReload(Document document, Person user) {
        return true;
    }

    public boolean canClose(Document document, Person user) {
        return true;
    }

    public boolean canSave(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.WORKFLOW,
            KimConstants.PermissionTemplateNames.SAVE_DOCUMENT, user.getPrincipalId());
    }

    public boolean canRoute(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.WORKFLOW,
            KimConstants.PermissionTemplateNames.ROUTE_DOCUMENT, user.getPrincipalId());
    }

    public boolean canCancel(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.WORKFLOW,
            KimConstants.PermissionTemplateNames.CANCEL_DOCUMENT, user.getPrincipalId());
    }

    public boolean canRecall(Document document, Person user) {
        return KewApiServiceLocator.getWorkflowDocumentActionsService().determineValidActions(
                document.getDocumentNumber(), user.getPrincipalId()).getValidActions().contains(ActionType.RECALL);
    }

    public boolean canCopy(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.COPY_DOCUMENT, user.getPrincipalId());
    }

    public boolean canPerformRouteReport(Document document, Person user) {
        return true;
    }

    public boolean canBlanketApprove(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.WORKFLOW,
            KimConstants.PermissionTemplateNames.BLANKET_APPROVE_DOCUMENT, user.getPrincipalId());
    }

    public boolean canApprove(Document document, Person user) {
        return canTakeRequestedAction(document, KewApiConstants.ACTION_REQUEST_APPROVE_REQ, user);
    }

    public boolean canDisapprove(Document document, Person user) {
        return canApprove(document, user);
    }

    public boolean canSendNoteFyi(Document document, Person user) {
        return canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_FYI_REQ, user);
    }

    public boolean canFyi(Document document, Person user) {
        return canTakeRequestedAction(document, KewApiConstants.ACTION_REQUEST_FYI_REQ, user);
    }

    public boolean canAcknowledge(Document document, Person user) {
        return canTakeRequestedAction(document, KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, user);
    }

    public boolean canReceiveAdHoc(Document document, Person user, String actionRequestCode) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        additionalPermissionDetails.put(KimConstants.AttributeConstants.ACTION_REQUEST_CD, actionRequestCode);

        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.WORKFLOW,
            KimConstants.PermissionTemplateNames.AD_HOC_REVIEW_DOCUMENT, user.getPrincipalId(),
            additionalPermissionDetails, null);
    }

    public boolean canAddNoteAttachment(Document document, String attachmentTypeCode, Person user) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        if (attachmentTypeCode != null) {
            additionalPermissionDetails.put(KimConstants.AttributeConstants.ATTACHMENT_TYPE_CODE, attachmentTypeCode);
        }

        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.ADD_NOTE_ATTACHMENT, user.getPrincipalId(),
            additionalPermissionDetails, null);
    }

    public boolean canDeleteNoteAttachment(Document document, String attachmentTypeCode,
            String authorUniversalIdentifier, Person user) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        if (attachmentTypeCode != null) {
            additionalPermissionDetails.put(KimConstants.AttributeConstants.ATTACHMENT_TYPE_CODE, attachmentTypeCode);
        }

        // first check permissions that does not restrict on the author
        additionalPermissionDetails.put(KimConstants.AttributeConstants.CREATED_BY_SELF, "false");
        boolean canDeleteNoteAttachment = isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.DELETE_NOTE_ATTACHMENT, user.getPrincipalId(),
                additionalPermissionDetails, null);

        if (!canDeleteNoteAttachment) {
            // check for permissions restricted by author
            additionalPermissionDetails.put(KimConstants.AttributeConstants.CREATED_BY_SELF, "true");
            canDeleteNoteAttachment = isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
                    KimConstants.PermissionTemplateNames.DELETE_NOTE_ATTACHMENT, user.getPrincipalId(),
                    additionalPermissionDetails, null);

            // if permission has been granted user must be the author
            if (canDeleteNoteAttachment && !authorUniversalIdentifier.equals(user.getPrincipalId())) {
                canDeleteNoteAttachment = false;
            }
        }

        return canDeleteNoteAttachment;
    }

    public boolean canViewNoteAttachment(Document document, String attachmentTypeCode, String authorUniversalIdentifier,
            Person user) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        if (attachmentTypeCode != null) {
            additionalPermissionDetails.put(KimConstants.AttributeConstants.ATTACHMENT_TYPE_CODE, attachmentTypeCode);
        }

        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.VIEW_NOTE_ATTACHMENT, user.getPrincipalId(),
            additionalPermissionDetails, null);
    }

    public boolean canSendAdHocRequests(Document document, String actionRequestCd, Person user) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        if (actionRequestCd != null) {
            additionalPermissionDetails.put(KimConstants.AttributeConstants.ACTION_REQUEST_CD, actionRequestCd);
        }

        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.SEND_AD_HOC_REQUEST, user.getPrincipalId(),
            additionalPermissionDetails, null);
    }

    public boolean canEditDocumentOverview(Document document, Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId()) && this.isDocumentInitiator(
            document, user);
    }

    public boolean canSendAnyTypeAdHocRequests(Document document, Person user) {
        if (canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_FYI_REQ, user)) {
            RoutePath routePath = KEWServiceLocator.getDocumentTypeService().getRoutePathForDocumentTypeName(
                document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
            ProcessDefinition processDefinition = routePath.getPrimaryProcess();
            if (processDefinition != null) {
                return processDefinition.getInitialRouteNode() != null;
            } else {
                return false;
            }
        } else if (canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, user)) {
            return true;
        }

        return canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_APPROVE_REQ, user);
    }

    public boolean canTakeRequestedAction(Document document, String actionRequestCode, Person user) {
        Map<String, String> additionalPermissionDetails = new HashMap<>();
        additionalPermissionDetails.put(KimConstants.AttributeConstants.ACTION_REQUEST_CD, actionRequestCode);

        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.TAKE_REQUESTED_ACTION, user.getPrincipalId(),
            additionalPermissionDetails, null);
    }

    @Override
    protected void addPermissionDetails(Object dataObject, Map<String, String> attributes) {
        super.addPermissionDetails(dataObject, attributes);

        if (dataObject instanceof Document) {
            addStandardAttributes((Document) dataObject, attributes);
        }
    }

    @Override
    protected void addRoleQualification(Object dataObject, Map<String, String> attributes) {
        super.addRoleQualification(dataObject, attributes);

        if (dataObject instanceof Document) {
            addStandardAttributes((Document) dataObject, attributes);
        }
    }

    protected void addStandardAttributes(Document document, Map<String, String> attributes) {
        WorkflowDocument wd = document.getDocumentHeader().getWorkflowDocument();
        attributes.put(KimConstants.AttributeConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        attributes.put(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME, wd.getDocumentTypeName());

        if (wd.isInitiated() || wd.isSaved()) {
            attributes.put(KimConstants.AttributeConstants.ROUTE_NODE_NAME, PRE_ROUTING_ROUTE_NAME);
        } else {
            attributes.put(KimConstants.AttributeConstants.ROUTE_NODE_NAME,
                KewApiServiceLocator.getWorkflowDocumentService().getCurrentRouteNodeNames(wd));
        }

        attributes.put(KimConstants.AttributeConstants.ROUTE_STATUS_CODE, wd.getStatus().getCode());
    }

    protected boolean isDocumentInitiator(Document document, Person user) {
        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return workflowDocument.getInitiatorPrincipalId().equalsIgnoreCase(user.getPrincipalId());
    }
}
