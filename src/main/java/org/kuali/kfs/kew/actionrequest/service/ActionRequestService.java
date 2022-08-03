/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.kew.actionrequest.service;

import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.Recipient;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.engine.ActivationContext;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service to handle the building, sorting, saving, activating and deactivating of action request graphs. These lists
 * are what determine role and delegation behaviors in graphs of action requests.
 * <p>
 * Fetching that is being done is also taking into account the 'weight' of action request codes.
 */

/* Cornell customization: backport FINP-8341 */
public interface ActionRequestService {

    ActionRequest initializeActionRequestGraph(ActionRequest actionRequest,
            DocumentRouteHeaderValue document, RouteNodeInstance nodeInstance);

    void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest);

    void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest,
            ActivationContext activationContext);

    void deactivateRequests(ActionTaken actionTaken, List actionRequests);

    void deactivateRequests(ActionTaken actionTaken, List actionRequests, ActivationContext activationContext);

    void deleteActionRequestGraph(ActionRequest actionRequest);

    List findAllValidRequests(String principalId, String documentId, String requestCode);

    List<ActionRequest> findPendingByDoc(String documentId);

    void saveActionRequest(ActionRequest actionRequest);

    void activateRequest(ActionRequest actionRequest);

    void activateRequests(Collection actionRequests);

    void activateRequests(Collection actionRequests, ActivationContext activationContext);

    void activateRequestNoNotification(ActionRequest actionRequest, ActivationContext activationContext);

    ActionRequest findByActionRequestId(String actionRequestId);

    List<ActionRequest> findPendingRootRequestsByDocId(String documentId);

    List<ActionRequest> findAllActionRequestsByDocumentId(String documentId);

    List<ActionRequest> findAllRootActionRequestsByDocumentId(String documentId);

    List<ActionRequest> findPendingByActionRequestedAndDocId(String actionRequestedCdCd, String documentId);

    /**
     * This method gets a list of ids of all principals who have a pending action request for a document.
     */
    List<String> getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(String actionRequestedCd,
            String documentId);

    List<ActionRequest> findByDocumentIdIgnoreCurrentInd(String documentId);

    List findActivatedByGroup(String groupId);

    /**
     * Schedule requeue of documents, affected by responsibility changes.
     *
     * @param responsibilityIds used to find documents to requeue.
     */
    void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds);

    /**
     * Schedule requeue of documents, affected by responsibility changes.
     *
     * @param responsibilityIds used to find documents to requeue.
     * @param docIdToIgnore doc id to not requeue (used to avoid requeueing the doc that spawned this request)
     * @param accountNumbers account numbers used to find docs to requeue
     * @param documentTypes document types used to find docs to requeue
     */
    void updateActionRequestsForResponsibilityChange(
            Set<String> responsibilityIds,
            String docIdToIgnore,
            Set<String> accountNumbers,
            Set<String> documentTypes
    );

    ActionRequest getRoot(ActionRequest actionRequest);

    List<ActionRequest> getRootRequests(Collection<ActionRequest> actionRequests);

    /**
     * Returns all pending requests for a given routing entity
     *
     * @param documentId the id of the document header being routed
     * @return a List of all pending ActionRequestValues for the document
     */
    List<ActionRequest> findAllPendingRequests(String documentId);

    /**
     * Filters action requests based on if they occur after the given requestCode, and if they relate to
     * the given principal
     *
     * @param actionRequests    the List of ActionRequestValues to filter
     * @param principalId       the id of the principal to find active requests for
     * @param principalGroupIds List of group ids that the principal belongs to
     * @param requestCode       the request code for all ActionRequestValues to be after
     * @return the filtered List of ActionRequestValues
     */
    List<ActionRequest> filterActionRequestsByCode(List<ActionRequest> actionRequests, String principalId,
            List<String> principalGroupIds, String requestCode);

    /**
     * @return the highest priority delegator in the list of action requests.
     */
    Recipient findDelegator(List actionRequests);

    ActionRequest findDelegatorRequest(ActionRequest actionRequest);

    List<ActionRequest> findPendingRootRequestsByDocIdAtRouteNode(String documentId, String nodeInstanceId);

    List<ActionRequest> findRootRequestsByDocIdAtRouteNode(String documentId, String nodeInstanceId);

    /**
     * Checks if the given user has any Action Requests on the given document.
     */
    boolean doesPrincipalHaveRequest(String principalId, String documentId);

    Map<String, String> getActionsRequested(DocumentRouteHeaderValue routeHeader, String principalId,
            boolean completeAndApproveTheSame);

    ActionRequest getActionRequestForRole(String actionTakenId);
}
