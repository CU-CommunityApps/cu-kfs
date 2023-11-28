/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.kew.impl.document;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actionrequest.service.impl.NotificationSuppression;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.WorkflowDocumentActionsService;
import org.kuali.kfs.kew.api.document.DocumentProcessingQueue;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.kew.engine.RouteHelper;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sys.KFSConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can be removed when we upgrade to the 2023-02-08 financials patch.
 * ============
 * 
 * A service which effectively "refreshes" and requeues a document.  It first deletes any
 * pending action requests on the documents and then requeues the document for standard routing.
 * Additionally, it adds duplicate notification suppression state to RouteNodeInstanceS for
 * which ActionRequestS will be regenerated.
 *
 * <p>Intended to be called async and wired that way in server/client spring beans.</p>
 */
public class DocumentRefreshQueueImpl implements DocumentRefreshQueue {

    private static final Logger LOG = LogManager.getLogger();

    private ActionRequestService actionRequestService;
    private DocumentProcessingQueue documentProcessingQueue;
    private RouteNodeService routeNodeService;

    private final RouteHelper helper = new RouteHelper();

    private PersonService personService;
    private WorkflowDocumentActionsService workflowDocumentActionsService;

    /**
     * Requeues a document, and sets notification suppression data
     *
     * @see org.kuali.kfs.kew.api.document.DocumentRefreshQueue#refreshDocument(java.lang.String)
     */
    @Override
    public void refreshDocument(final String documentId) {
        Validate.isTrue(StringUtils.isNotBlank(documentId), "documentId must be supplied");

        KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
        final Collection<RouteNodeInstance> activeNodes = routeNodeService.getActiveNodeInstances(documentId);
        final List<ActionRequest> requestsToDelete = new ArrayList<>();

        final NotificationSuppression notificationSuppression = new NotificationSuppression();

        for (final RouteNodeInstance nodeInstance : activeNodes) {
            // only "requeue" if we're dealing with a request activation node
            if (helper.isRequestActivationNode(nodeInstance.getRouteNode())) {
                final List<ActionRequest> deletesForThisNode =
                        actionRequestService.findPendingRootRequestsByDocIdAtRouteNode(documentId,
                                nodeInstance.getRouteNodeInstanceId());

                for (final ActionRequest deleteForThisNode : deletesForThisNode) {
                    // check either the request or its first present child request to see if it is system generated
                    boolean containsRoleOrRuleRequests = deleteForThisNode.isRouteModuleRequest();
                    if (!containsRoleOrRuleRequests) {
                        if (CollectionUtils.isNotEmpty(deleteForThisNode.getChildrenRequests())) {
                            containsRoleOrRuleRequests =
                                    deleteForThisNode.getChildrenRequests().get(0).isRouteModuleRequest();
                        }
                    }

                    if (containsRoleOrRuleRequests) {
                        // remove all route or rule system generated requests
                        requestsToDelete.add(deleteForThisNode);

                        // suppress duplicate notifications
                        notificationSuppression.addNotificationSuppression(nodeInstance, deleteForThisNode);
                    }
                }

                // this will trigger a regeneration of requests
                nodeInstance.setInitial(true);
                routeNodeService.save(nodeInstance);
            }
        }
        for (final ActionRequest requestToDelete : requestsToDelete) {
            actionRequestService.deleteActionRequestGraph(requestToDelete);
        }
        try {
            documentProcessingQueue.process(documentId);
        } catch (final Exception e) {
            throw new WorkflowRuntimeException(e);
        }
        LOG.info("refreshDocument(...) - Ran DocumentRequeuer : documentId={}", documentId);
    }

    @Override
    public void refreshDocument(final String documentId, final String annotation) {
        Validate.isTrue(StringUtils.isNotBlank(documentId), "documentId must be supplied");
        final Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
        workflowDocumentActionsService.logAnnotation(
                documentId,
                systemUser.getPrincipalId(),
                annotation
        );

        refreshDocument(documentId);
    }

    public void setActionRequestService(final ActionRequestService actionRequestService) {
        this.actionRequestService = actionRequestService;
    }

    public void setDocumentProcessingQueue(final DocumentProcessingQueue documentProcessingQueue) {
        this.documentProcessingQueue = documentProcessingQueue;
    }

    public void setRouteNodeService(final RouteNodeService routeNodeService) {
        this.routeNodeService = routeNodeService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setWorkflowDocumentActionsService(final WorkflowDocumentActionsService workflowDocumentActionsService) {
        this.workflowDocumentActionsService = workflowDocumentActionsService;
    }
}
