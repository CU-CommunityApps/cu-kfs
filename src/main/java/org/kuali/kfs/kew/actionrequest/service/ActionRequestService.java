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
public interface ActionRequestService {

    ActionRequest initializeActionRequestGraph(ActionRequest actionRequest,
            DocumentRouteHeaderValue document, RouteNodeInstance nodeInstance);

    void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest);

    void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest, boolean simulate);

    void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest,
            ActivationContext activationContext);

    void deactivateRequests(ActionTaken actionTaken, List actionRequests);

    void deactivateRequests(ActionTaken actionTaken, List actionRequests, boolean simulate);

    void deactivateRequests(ActionTaken actionTaken, List actionRequests, ActivationContext activationContext);

    void deleteActionRequestGraph(ActionRequest actionRequest);

    List findAllValidRequests(String principalId, String documentId, String requestCode);

    List findAllValidRequests(String principalId, Collection actionRequests, String requestCode);

    List<ActionRequest> findPendingByDoc(String documentId);

    void saveActionRequest(ActionRequest actionRequest);

    void activateRequest(ActionRequest actionRequest);

    void activateRequest(ActionRequest actionRequest, boolean simulate);

    void activateRequest(ActionRequest actionRequest, ActivationContext activationContext);

    void activateRequests(Collection actionRequests);

    void activateRequests(Collection actionRequests, boolean simulate);

    void activateRequests(Collection actionRequests, ActivationContext activationContext);

    List activateRequestNoNotification(ActionRequest actionRequest, boolean simulate);

    List activateRequestNoNotification(ActionRequest actionRequest, ActivationContext activationContext);

    ActionRequest findByActionRequestId(String actionRequestId);

    List<ActionRequest> findPendingRootRequestsByDocId(String documentId);

    List<ActionRequest> findPendingRootRequestsByDocIdAtRouteLevel(String documentId, Integer routeLevel);

    List<ActionRequest> findPendingByDocIdAtOrBelowRouteLevel(String documentId, Integer routeLevel);

    List<ActionRequest> findPendingRootRequestsByDocIdAtOrBelowRouteLevel(String documentId, Integer routeLevel);

    List<ActionRequest> findPendingRootRequestsByDocumentType(String documentTypeId);

    List<ActionRequest> findAllActionRequestsByDocumentId(String documentId);

    List<ActionRequest> findAllRootActionRequestsByDocumentId(String documentId);

    List<ActionRequest> findPendingByActionRequestedAndDocId(String actionRequestedCdCd, String documentId);

    /**
     * This method gets a list of ids of all principals who have a pending action request for a document.
     */
    List<String> getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(String actionRequestedCd,
            String documentId);

    List<ActionRequest> findByStatusAndDocId(String statusCd, String documentId);

    void alterActionRequested(List actionRequests, String actionRequestCd);

    List<ActionRequest> findByDocumentIdIgnoreCurrentInd(String documentId);

    List findActivatedByGroup(String groupId);

    /**
     * Schedule requeue of documents, affected by responsibility changes.
     *
     * @param responsibilityIds used to find documents to requeue.
     */
    void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds);
    
    /*
     * FINP-8322 changes from KualiCo patch release 2022-03-23 applied to
     * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
     */
    /**
     * Schedule requeue of documents, affected by responsibility changes.
     *
     * @param responsibilityIds used to find documents to requeue.
     * @param docIdToIgnore doc id to not requeue (used to avoid requeueing the doc that spawned this request)
     */
    void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds, String docIdToIgnore);

    ActionRequest getRoot(ActionRequest actionRequest);

    List<ActionRequest> getRootRequests(Collection<ActionRequest> actionRequests);

    boolean isDuplicateRequest(ActionRequest actionRequest);

    List<ActionRequest> findPendingByDocRequestCdRouteLevel(String documentId, String requestCode,
            Integer routeLevel);

    List<ActionRequest> findPendingByDocRequestCdNodeName(String documentId, String requestCode, String nodeName);

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

    /**
     * @return the closest delegator for the given ActionRequest
     */
    Recipient findDelegator(ActionRequest actionRequest);

    ActionRequest findDelegatorRequest(ActionRequest actionRequest);

    void deleteByDocumentId(String documentId);

    void deleteByActionRequestId(String actionRequestId);

    void validateActionRequest(ActionRequest actionRequest);

    List<ActionRequest> findPendingRootRequestsByDocIdAtRouteNode(String documentId, String nodeInstanceId);

    List<ActionRequest> findRootRequestsByDocIdAtRouteNode(String documentId, String nodeInstanceId);

    List getDelegateRequests(ActionRequest actionRequest);

    /**
     * If this is a role request, then this method returns a List of the action request for each recipient within the
     * role.  Otherwise, it will return a List with just the original action request.
     */
    List getTopLevelRequests(ActionRequest actionRequest);

    boolean isValidActionRequestCode(String actionRequestCode);

    /**
     * Checks if the given user has any Action Requests on the given document.
     */
    boolean doesPrincipalHaveRequest(String principalId, String documentId);

    Map<String, String> getActionsRequested(DocumentRouteHeaderValue routeHeader, String principalId,
            boolean completeAndApproveTheSame);

    ActionRequest getActionRequestForRole(String actionTakenId);
}
