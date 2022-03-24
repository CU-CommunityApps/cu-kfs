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
package org.kuali.kfs.kew.actionrequest.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionlist.service.ActionListService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.Recipient;
import org.kuali.kfs.kew.actionrequest.dao.ActionRequestDAO;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.actiontaken.service.ActionTakenService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.action.ActionRequestPolicy;
import org.kuali.kfs.kew.api.action.ActionRequestStatus;
import org.kuali.kfs.kew.api.action.RecipientType;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.engine.ActivationContext;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.routemodule.RouteModule;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.util.FutureRequestDocumentStateManager;
import org.kuali.kfs.kew.util.PerformanceLogger;
import org.kuali.kfs.kew.util.ResponsibleParty;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of the {@link ActionRequestService}.
 */
public class ActionRequestServiceImpl implements ActionRequestService {

    private static final Logger LOG = LogManager.getLogger();

    private ActionRequestDAO actionRequestDAO;

    public ActionRequest findByActionRequestId(String actionRequestId) {
        return getActionRequestDAO().getActionRequestByActionRequestId(actionRequestId);
    }

    public Map<String, String> getActionsRequested(DocumentRouteHeaderValue routeHeader, String principalId,
            boolean completeAndApproveTheSame) {
        return getActionsRequested(principalId, routeHeader.getActionRequests(), completeAndApproveTheSame);
    }

    /**
     * @param principalId
     * @param actionRequests
     * @param completeAndApproveTheSame
     * @return a Map of actions that are requested for the given principalId in the given list of action requests.
     */
    protected Map<String, String> getActionsRequested(String principalId, List<ActionRequest> actionRequests,
            boolean completeAndApproveTheSame) {
        Map<String, String> actionsRequested = new HashMap<>();
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_COMPLETE_REQ, "false");
        String topActionRequested = KewApiConstants.ACTION_REQUEST_FYI_REQ;
        for (ActionRequest actionRequest : actionRequests) {
            // we are getting the full list of requests here, so no need to look at role requests, if we did this then
            // we could get a "false positive" for "all approve" roles where only part of the request graph is marked
            // as "done"
            if (!RecipientType.ROLE.getCode().equals(actionRequest.getRecipientTypeCd())
                    && actionRequest.isRecipientRoutedRequest(principalId) && actionRequest.isActive()) {
                int actionRequestComparison = ActionRequest
                        .compareActionCode(actionRequest.getActionRequested(), topActionRequested,
                                completeAndApproveTheSame);
                if (actionRequest.isFYIRequest() && actionRequestComparison >= 0) {
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "true");
                } else if (actionRequest.isAcknowledgeRequest() && actionRequestComparison >= 0) {
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, "true");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "false");
                    topActionRequested = actionRequest.getActionRequested();
                } else if (actionRequest.isApproveRequest() && actionRequestComparison >= 0) {
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ, "true");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, "false");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "false");
                    topActionRequested = actionRequest.getActionRequested();
                } else if (actionRequest.isCompleteRequest() && actionRequestComparison >= 0) {
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_COMPLETE_REQ, "true");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ, "false");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, "false");
                    actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "false");
                    if (completeAndApproveTheSame) {
                        actionsRequested.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ, "true");
                    }
                    topActionRequested = actionRequest.getActionRequested();
                }
            }
        }
        return actionsRequested;
    }

    public ActionRequest initializeActionRequestGraph(ActionRequest actionRequest,
            DocumentRouteHeaderValue document, RouteNodeInstance nodeInstance) {
        if (actionRequest.getParentActionRequest() != null) {
            LOG.warn("-->A non parent action request from doc " + document.getDocumentId());
            actionRequest = KEWServiceLocator.getActionRequestService().getRoot(actionRequest);
        }
        propagatePropertiesToRequestGraph(actionRequest, document, nodeInstance);
        return actionRequest;
    }

    private void propagatePropertiesToRequestGraph(ActionRequest actionRequest,
            DocumentRouteHeaderValue document, RouteNodeInstance nodeInstance) {
        setPropertiesToRequest(actionRequest, document, nodeInstance);
        for (ActionRequest actionRequestValue : actionRequest.getChildrenRequests()) {
            propagatePropertiesToRequestGraph(actionRequestValue, document, nodeInstance);
        }
    }

    private void setPropertiesToRequest(ActionRequest actionRequest, DocumentRouteHeaderValue document,
            RouteNodeInstance nodeInstance) {
        actionRequest.setDocumentId(document.getDocumentId());
        actionRequest.setDocVersion(document.getDocVersion());
        actionRequest.setRouteLevel(document.getDocRouteLevel());
        actionRequest.setNodeInstance(nodeInstance);
        actionRequest.setStatus(ActionRequestStatus.INITIALIZED.getCode());
    }

    public void activateRequests(Collection actionRequests) {
        activateRequests(actionRequests, new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    public void activateRequests(Collection actionRequests, boolean simulate) {
        activateRequests(actionRequests, new ActivationContext(simulate));
    }

    public void activateRequests(Collection actionRequests, ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        PerformanceLogger performanceLogger = null;
        if (LOG.isInfoEnabled()) {
            performanceLogger = new PerformanceLogger();
        }
        activationContext.setGeneratedActionItems(new ArrayList<>());
        activateRequestsInternal(actionRequests, activationContext);
        if (!activationContext.isSimulation()) {
            KEWServiceLocator.getNotificationService().notify(activationContext.getGeneratedActionItems());
        }
        if (LOG.isInfoEnabled()) {
            performanceLogger.log("Time to " +
                    (activationContext.isSimulation() ? "simulate activation of " : "activate ") +
                    actionRequests.size() + " action requests.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated " + activationContext.getGeneratedActionItems().size() + " action items.");
        }
    }

    public void activateRequest(ActionRequest actionRequest) {
        activateRequests(Collections.singletonList(actionRequest),
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    public void activateRequest(ActionRequest actionRequest, boolean simulate) {
        activateRequests(Collections.singletonList(actionRequest), new ActivationContext(simulate));
    }

    public void activateRequest(ActionRequest actionRequest, ActivationContext activationContext) {
        activateRequests(Collections.singletonList(actionRequest), activationContext);
    }

    public List activateRequestNoNotification(ActionRequest actionRequest, boolean simulate) {
        return activateRequestNoNotification(actionRequest, new ActivationContext(simulate));
    }

    public List activateRequestNoNotification(ActionRequest actionRequest, ActivationContext activationContext) {
        activationContext.setGeneratedActionItems(new ArrayList<>());
        activateRequestInternal(actionRequest, activationContext);
        return activationContext.getGeneratedActionItems();
    }

    /**
     * Internal helper method for activating a Collection of action requests and their children. Maintains an
     * accumulator for generated action items.
     *
     * @param actionRequests
     * @param activationContext
     */
    private void activateRequestsInternal(Collection actionRequests, ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        List<?> actionRequestList = new ArrayList<Object>(actionRequests);
        for (int i = 0; i < actionRequestList.size(); i++) {
            activateRequestInternal((ActionRequest) actionRequestList.get(i), activationContext);
        }
    }

    /**
     * Internal helper method for activating a single action requests and it's children. Maintains an accumulator for
     * generated action items.
     */
    private void activateRequestInternal(ActionRequest actionRequest, ActivationContext activationContext) {
        PerformanceLogger performanceLogger = null;
        if (LOG.isInfoEnabled()) {
            performanceLogger = new PerformanceLogger();
        }
        if (actionRequest == null || actionRequest.isActive() || actionRequest.isDeactivated()) {
            return;
        }
        processResponsibilityId(actionRequest);
        if (deactivateOnActionAlreadyTaken(actionRequest, activationContext)) {
            return;
        }
        if (deactivateOnInactiveGroup(actionRequest, activationContext)) {
            return;
        }
        if (deactivateOnEmptyGroup(actionRequest, activationContext)) {
            return;
        }
        actionRequest.setStatus(ActionRequestStatus.ACTIVATED.getCode());
        if (!activationContext.isSimulation()) {
            saveActionRequest(actionRequest);
            activationContext.getGeneratedActionItems().addAll(generateActionItems(actionRequest, activationContext));
        }
        activateRequestsInternal(actionRequest.getChildrenRequests(), activationContext);
        activateRequestInternal(actionRequest.getParentActionRequest(), activationContext);
        if (LOG.isInfoEnabled()) {
            if (activationContext.isSimulation()) {
                performanceLogger.log("Time to simulate activation of request.");
            } else {
                performanceLogger.log("Time to activate action request with id " + actionRequest.getActionRequestId());
            }
        }
    }

    /**
     * Generates ActionItems for the given ActionRequest and returns the List of generated Action Items.
     *
     * @param actionRequest
     * @param activationContext
     * @return the List of generated ActionItems
     */
    private List<ActionItem> generateActionItems(ActionRequest actionRequest,
            ActivationContext activationContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generating the action items for request " + actionRequest.getActionRequestId());
        }
        List<ActionItem> actionItems = new ArrayList<>();
        if (!actionRequest.isPrimaryDelegator()) {
            if (actionRequest.isGroupRequest()) {
                List<String> principalIds =
                        KimApiServiceLocator.getGroupService().getMemberPrincipalIds(actionRequest.getGroupId());
                actionItems.addAll(createActionItemsForPrincipals(actionRequest, principalIds));
            } else if (actionRequest.isUserRequest()) {
                ActionItem actionItem = getActionListService().createActionItemForActionRequest(actionRequest);
                actionItems.add(actionItem);
            }
        }
        if (!activationContext.isSimulation()) {
            for (ActionItem actionItem : actionItems) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Saving action item: " + actionItems);
                }
                getActionListService().saveActionItem(actionItem);
            }
        } else {
            actionRequest.getSimulatedActionItems().addAll(actionItems);
        }
        return actionItems;
    }

    private List<ActionItem> createActionItemsForPrincipals(ActionRequest actionRequest,
            List<String> principalIds) {
        List<ActionItem> actionItems = new ArrayList<>();
        for (String principalId : principalIds) {

            ActionItem actionItem = getActionListService().createActionItemForActionRequest(actionRequest);
            actionItem.setPrincipalId(principalId);
            actionItem.setRoleName(actionRequest.getQualifiedRoleName());

            if (principalId == null) {
                IllegalArgumentException e = new IllegalArgumentException(
                        "Exception thrown when trying to add action item with null principalId");
                LOG.error(e);
                throw e;
            } else {
                actionItems.add(actionItem);
            }
        }
        return actionItems;
    }

    private void processResponsibilityId(ActionRequest actionRequest) {
        if (actionRequest.getResolveResponsibility()) {
            String responsibilityId = actionRequest.getResponsibilityId();
            try {
                RouteModule routeModule = KEWServiceLocator.getRouteModuleService().findRouteModule(actionRequest);
                if (responsibilityId != null && actionRequest.isRouteModuleRequest()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Resolving responsibility id for action request id=" +
                                actionRequest.getActionRequestId()
                                + " and responsibility id=" + actionRequest.getResponsibilityId());
                    }
                    ResponsibleParty responsibleParty =
                            routeModule.resolveResponsibilityId(actionRequest.getResponsibilityId());
                    if (responsibleParty == null) {
                        return;
                    }
                    if (responsibleParty.getPrincipalId() != null) {
                        Principal user = KimApiServiceLocator.getIdentityService()
                                .getPrincipal(responsibleParty.getPrincipalId());
                        actionRequest.setPrincipalId(user.getPrincipalId());
                    } else if (responsibleParty.getGroupId() != null) {
                        actionRequest.setGroupId(responsibleParty.getGroupId());
                    } else if (responsibleParty.getRoleName() != null) {
                        actionRequest.setRoleName(responsibleParty.getRoleName());
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception thrown when trying to resolve responsibility id " + responsibilityId, e);
                throw new RuntimeException(e);
            }
        }
    }

    protected boolean deactivateOnActionAlreadyTaken(ActionRequest actionRequestToActivate,
            ActivationContext activationContext) {
        FutureRequestDocumentStateManager futureRequestStateMngr = null;

        if (actionRequestToActivate.isGroupRequest()) {
            futureRequestStateMngr = new FutureRequestDocumentStateManager(actionRequestToActivate.getRouteHeader(),
                    actionRequestToActivate.getGroup());
        } else if (actionRequestToActivate.isUserRequest()) {
            futureRequestStateMngr = new FutureRequestDocumentStateManager(actionRequestToActivate.getRouteHeader(),
                    actionRequestToActivate.getPrincipalId());
        } else {
            return false;
        }

        if (futureRequestStateMngr.isReceiveFutureRequests()) {
            return false;
        }
        if (!actionRequestToActivate.getForceAction() || futureRequestStateMngr.isDoNotReceiveFutureRequests()) {
            ActionTaken previousActionTaken = null;
            if (!activationContext.isSimulation()) {
                previousActionTaken = getActionTakenService().getPreviousAction(actionRequestToActivate);
            } else {
                previousActionTaken = getActionTakenService().getPreviousAction(actionRequestToActivate,
                        activationContext.getSimulatedActionsTaken());
            }
            if (previousActionTaken != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("found a satisfying action taken so setting this request done.  Action Request Id "
                            + actionRequestToActivate.getActionRequestId());
                }
                // set up the delegation for an action taken if this is a delegate request and the delegate has
                // already taken action.
                if (!previousActionTaken.isForDelegator()
                        && actionRequestToActivate.getParentActionRequest() != null) {
                    previousActionTaken.setDelegator(actionRequestToActivate.getParentActionRequest().getRecipient());
                    if (!activationContext.isSimulation()) {
                        getActionTakenService().saveActionTaken(previousActionTaken);
                    }
                }
                deactivateRequest(previousActionTaken, actionRequestToActivate, null, activationContext);
                return true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Forcing action for action request " + actionRequestToActivate.getActionRequestId());
        }
        return false;
    }

    /**
     * Checks if the action request which is being activated has a group with no members. If this is the case then it
     * will immediately initiate de-activation on the request since a group with no members will result in no action
     * items being generated so should be effectively skipped.
     */
    protected boolean deactivateOnEmptyGroup(ActionRequest actionRequestToActivate,
            ActivationContext activationContext) {
        if (actionRequestToActivate.isGroupRequest()) {
            if (KimApiServiceLocator.getGroupService()
                    .getMemberPrincipalIds(actionRequestToActivate.getGroup().getId()).isEmpty()) {
                deactivateRequest(null, actionRequestToActivate, null, activationContext);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the action request which is being activated is being assigned to an inactive group.  If this is the
     * case and if the FailOnInactiveGroup policy is set to false then it will immediately initiate de-activation on
     * the request
     */
    protected boolean deactivateOnInactiveGroup(
            ActionRequest actionRequestToActivate, ActivationContext activationContext) {
        if (actionRequestToActivate.isGroupRequest()) {
            if (!actionRequestToActivate.getGroup().isActive() &&
                    !actionRequestToActivate.getRouteHeader().getDocumentType().getFailOnInactiveGroup()
                            .getPolicyValue()) {
                deactivateRequest(null, actionRequestToActivate, null, activationContext);
                return true;
            }
        }
        return false;
    }

    public void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest) {
        deactivateRequest(actionTaken, actionRequest, null,
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    public void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest, boolean simulate) {
        deactivateRequest(actionTaken, actionRequest, null, new ActivationContext(simulate));
    }

    public void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest,
            ActivationContext activationContext) {
        deactivateRequest(actionTaken, actionRequest, null, activationContext);
    }

    private void deactivateRequest(ActionTaken actionTaken, ActionRequest actionRequest,
            ActionRequest deactivationRequester, ActivationContext activationContext) {
        if (actionRequest == null || actionRequest.isDeactivated()
                || haltForAllApprove(actionRequest, deactivationRequester)) {
            return;
        }
        actionRequest.setStatus(ActionRequestStatus.DONE.getCode());
        actionRequest.setActionTaken(actionTaken);
        if (actionTaken != null) {
            actionTaken.getActionRequests().add(actionRequest);
        }
        if (!activationContext.isSimulation()) {
            getActionRequestDAO().saveActionRequest(actionRequest);
            deleteActionItems(actionRequest);
        }
        deactivateRequests(actionTaken, actionRequest.getChildrenRequests(), actionRequest, activationContext);
        deactivateRequest(actionTaken, actionRequest.getParentActionRequest(), actionRequest, activationContext);
    }

    public void deactivateRequests(ActionTaken actionTaken, List actionRequests) {
        deactivateRequests(actionTaken, actionRequests, null,
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    public void deactivateRequests(ActionTaken actionTaken, List actionRequests, boolean simulate) {
        deactivateRequests(actionTaken, actionRequests, null, new ActivationContext(simulate));
    }

    public void deactivateRequests(
            ActionTaken actionTaken, List actionRequests, ActivationContext activationContext) {
        deactivateRequests(actionTaken, actionRequests, null, activationContext);
    }

    private void deactivateRequests(ActionTaken actionTaken, Collection actionRequests,
            ActionRequest deactivationRequester, ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        for (Iterator iterator = actionRequests.iterator(); iterator.hasNext(); ) {
            ActionRequest actionRequest = (ActionRequest) iterator.next();
            deactivateRequest(actionTaken, actionRequest, deactivationRequester, activationContext);
        }
    }

    /**
     * Returns true if we are dealing with an 'All Approve' request, the requester of the deactivation is a child of the
     * 'All Approve' request, and all of the children have not been deactivated. If all of the children are already
     * deactivated or a non-child request initiated deactivation, then this method returns false. false otherwise.
     *
     * @param actionRequest
     * @param deactivationRequester
     * @return
     */
    private boolean haltForAllApprove(ActionRequest actionRequest, ActionRequest deactivationRequester) {
        if (ActionRequestPolicy.ALL.getCode().equals(actionRequest.getApprovePolicy())
                && actionRequest.hasChild(deactivationRequester)) {
            for (ActionRequest childRequest : actionRequest.getChildrenRequests()) {
                if (!childRequest.isDeactivated()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ActionRequest> getRootRequests(Collection<ActionRequest> actionRequests) {
        Set<ActionRequest> unsavedRequests = new HashSet<>();
        Map<String, ActionRequest> requestMap = new HashMap<>();
        for (ActionRequest actionRequest : actionRequests) {
            ActionRequest rootRequest = getRoot(actionRequest);
            if (rootRequest.getActionRequestId() != null) {
                requestMap.put(rootRequest.getActionRequestId(), rootRequest);
            } else {
                unsavedRequests.add(rootRequest);
            }
        }
        List<ActionRequest> requests = new ArrayList<>();
        requests.addAll(requestMap.values());
        requests.addAll(unsavedRequests);
        return requests;
    }

    public ActionRequest getRoot(ActionRequest actionRequest) {
        if (actionRequest == null) {
            return null;
        }
        if (actionRequest.getParentActionRequest() != null) {
            return getRoot(actionRequest.getParentActionRequest());
        }
        return actionRequest;
    }

    /**
     * Returns all pending requests for a given routing identity
     *
     * @param documentId the id of the document header being routed
     * @return a List of all pending ActionRequestValues for the document
     */
    public List<ActionRequest> findAllPendingRequests(String documentId) {
        ActionRequestDAO arDAO = getActionRequestDAO();
        return arDAO.findByStatusAndDocId(ActionRequestStatus.ACTIVATED.getCode(), documentId);
    }

    public List findAllValidRequests(String principalId, String documentId, String requestCode) {
        ActionRequestDAO arDAO = getActionRequestDAO();
        Collection pendingArs = arDAO.findByStatusAndDocId(ActionRequestStatus.ACTIVATED.getCode(), documentId);
        return findAllValidRequests(principalId, pendingArs, requestCode);
    }

    public List findAllValidRequests(String principalId, Collection actionRequests, String requestCode) {
        List<String> arGroups = KimApiServiceLocator.getGroupService().getGroupIdsByPrincipalId(principalId);
        return filterActionRequestsByCode((List<ActionRequest>) actionRequests, principalId, arGroups,
                requestCode);
    }

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
    public List<ActionRequest> filterActionRequestsByCode(List<ActionRequest> actionRequests,
            String principalId, List<String> principalGroupIds, String requestCode) {
        List<ActionRequest> filteredActionRequests = new ArrayList<>();

        for (ActionRequest ar : actionRequests) {
            if (ActionRequest.compareActionCode(ar.getActionRequested(), requestCode, true) > 0) {
                continue;
            }
            if (ar.isUserRequest() && principalId.equals(ar.getPrincipalId())) {
                filteredActionRequests.add(ar);
            } else if (ar.isGroupRequest() && principalGroupIds != null && !principalGroupIds.isEmpty()) {
                for (String groupId : principalGroupIds) {
                    if (groupId.equals(ar.getGroupId())) {
                        filteredActionRequests.add(ar);
                    }
                }
            }
        }

        return filteredActionRequests;
    }

    /*
     * FINP-8322 changes from KualiCo patch release 2022-03-23 applied to
     * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
     */
    public void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds) {
        PerformanceLogger performanceLogger = null;
        if (LOG.isInfoEnabled()) {
            performanceLogger = new PerformanceLogger();
        }
        Collection documentsAffected = getRouteHeaderService().findPendingByResponsibilityIds(responsibilityIds);
        updateActionRequestsForResponsibilityChange(documentsAffected, responsibilityIds.size());
    }
    
    /*
     * FINP-8322 changes from KualiCo patch release 2022-03-23 applied to
     * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
     */
    private void updateActionRequestsForResponsibilityChange(
            Collection documentsAffected, int responsibilityChangeCount
    ) {
        String cacheWaitValue = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.RULE_DETAIL_TYPE,
                KewApiConstants.RULE_CACHE_REQUEUE_DELAY);
        Long cacheWait = KewApiConstants.DEFAULT_CACHE_REQUEUE_WAIT_TIME;
        if (StringUtils.isNotEmpty(cacheWaitValue)) {
            try {
                cacheWait = Long.valueOf(cacheWaitValue);
            } catch (NumberFormatException e) {
                LOG.warn("Cache wait time is not a valid number: " + cacheWaitValue);
            }
        }
        final Long loggableCacheWait = cacheWait;
        LOG.info("Scheduling requeue of " + documentsAffected.size() + " documents, affected by " +
                responsibilityChangeCount
                + " responsibility changes.  Installing a processing wait time of " + cacheWait
                + " milliseconds to avoid stale rule cache.");
        for (Object aDocumentsAffected : documentsAffected) {
            String documentId = (String) aDocumentsAffected;

            DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId);

            if (documentType.getRegenerateActionRequestsOnChange().getPolicyValue()) {
                DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(
                        documentId, cacheWait);
                documentRequeuer.refreshDocument(documentId);
            }
        }
        LOG.info("updateActionRequestsForResponsibilityChange(...) - Exit");
    }
    
    /*
     * FINP-8322 changes from KualiCo patch release 2022-03-23 applied to
     * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
     */
    @Override
    public void updateActionRequestsForResponsibilityChange(Set<String> responsibilityIds, String docIdToIgnore) {
        Collection documentsAffected = getRouteHeaderService().findPendingByResponsibilityIds(responsibilityIds);
        documentsAffected.remove(docIdToIgnore);
        updateActionRequestsForResponsibilityChange(documentsAffected, responsibilityIds.size());
    }

    /**
     * Deletes an action request and all of its action items following the graph down through the action request's
     * children. This method should be invoked on a top-level action request.
     */
    public void deleteActionRequestGraph(ActionRequest actionRequest) {
        deleteActionItems(actionRequest);
        if (actionRequest.getActionTakenId() != null) {
            ActionTaken actionTaken =
                    getActionTakenService().findByActionTakenId(actionRequest.getActionTakenId());

            if (actionTaken != null) {
                getActionTakenService().delete(actionTaken);
            }
        }
        getActionRequestDAO().delete(actionRequest.getActionRequestId());
        for (ActionRequest child : actionRequest.getChildrenRequests()) {
            deleteActionRequestGraph(child);
        }
    }

    /**
     * Deletes the action items for the action request
     *
     * @param actionRequest the action request whose action items to delete
     */
    private void deleteActionItems(ActionRequest actionRequest) {
        List<ActionItem> actionItems = actionRequest.getActionItems();
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleting " + actionItems.size() + " action items for action request: " + actionRequest);
        }
        for (ActionItem actionItem : actionItems) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("deleting action item: " + actionItem);
            }
            getActionListService().deleteActionItem(actionItem);
        }
    }

    public List<ActionRequest> findByDocumentIdIgnoreCurrentInd(String documentId) {
        return getActionRequestDAO().findByDocumentIdIgnoreCurrentInd(documentId);
    }

    public List<ActionRequest> findAllActionRequestsByDocumentId(String documentId) {
        return getActionRequestDAO().findAllByDocId(documentId);
    }

    public List<ActionRequest> findAllRootActionRequestsByDocumentId(String documentId) {
        return getActionRequestDAO().findAllRootByDocId(documentId);
    }

    public List<ActionRequest> findPendingByActionRequestedAndDocId(String actionRequestedCd,
            String documentId) {
        return getActionRequestDAO().findPendingByActionRequestedAndDocId(actionRequestedCd, documentId);
    }

    public List<String> getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(String actionRequestedCd,
            String documentId) {
        List<String> principalIds = new ArrayList<>();
        List<ActionRequest> actionRequests = findPendingByActionRequestedAndDocId(actionRequestedCd, documentId);
        for (ActionRequest actionRequest : actionRequests) {
            if (actionRequest.isUserRequest()) {
                principalIds.add(actionRequest.getPrincipalId());
            } else if (actionRequest.isGroupRequest()) {
                principalIds.addAll(
                        KimApiServiceLocator.getGroupService().getMemberPrincipalIds(actionRequest.getGroupId()));
            }
        }
        return principalIds;
    }

    public List<ActionRequest> findPendingByDocIdAtOrBelowRouteLevel(String documentId, Integer routeLevel) {
        return getActionRequestDAO().findPendingByDocIdAtOrBelowRouteLevel(documentId, routeLevel);
    }

    public List<ActionRequest> findPendingRootRequestsByDocId(String documentId) {
        return getRootRequests(findPendingByDoc(documentId));
    }

    public List<ActionRequest> findPendingRootRequestsByDocIdAtRouteNode(String documentId,
            String nodeInstanceId) {
        return getActionRequestDAO().findPendingRootRequestsByDocIdAtRouteNode(documentId, nodeInstanceId);
    }

    public List<ActionRequest> findRootRequestsByDocIdAtRouteNode(String documentId, String nodeInstanceId) {
        return getActionRequestDAO().findRootRequestsByDocIdAtRouteNode(documentId, nodeInstanceId);
    }

    public List<ActionRequest> findPendingRootRequestsByDocIdAtOrBelowRouteLevel(
            String documentId, Integer routeLevel) {
        return getActionRequestDAO().findPendingRootRequestsByDocIdAtOrBelowRouteLevel(documentId, routeLevel);
    }

    public List<ActionRequest> findPendingRootRequestsByDocIdAtRouteLevel(String documentId,
            Integer routeLevel) {
        return getActionRequestDAO().findPendingRootRequestsByDocIdAtRouteLevel(documentId, routeLevel);
    }

    public List<ActionRequest> findPendingRootRequestsByDocumentType(String documentTypeId) {
        return getActionRequestDAO().findPendingRootRequestsByDocumentType(documentTypeId);
    }

    public void saveActionRequest(ActionRequest actionRequest) {
        if (actionRequest.isGroupRequest()) {
            Group group = actionRequest.getGroup();
            if (group == null) {
                throw new RuntimeException("Attempted to save an action request with a non-existent group.");
            }
            if (!group.isActive()
                    && actionRequest.getRouteHeader().getDocumentType().getFailOnInactiveGroup().getPolicyValue()) {
                throw new RuntimeException("Attempted to save an action request with an inactive group.");
            }
        }
        getActionRequestDAO().saveActionRequest(actionRequest);
    }

    public List<ActionRequest> findPendingByDoc(String documentId) {
        return getActionRequestDAO().findAllPendingByDocId(documentId);
    }

    public List<ActionRequest> findPendingByDocRequestCdRouteLevel(String documentId, String requestCode,
            Integer routeLevel) {
        List<ActionRequest> requests = new ArrayList<>();
        for (Object object : getActionRequestDAO().findAllPendingByDocId(documentId)) {
            ActionRequest actionRequest = (ActionRequest) object;
            if (ActionRequest.compareActionCode(actionRequest.getActionRequested(), requestCode, true) > 0) {
                continue;
            }
            if (actionRequest.getRouteLevel().intValue() == routeLevel.intValue()) {
                requests.add(actionRequest);
            }
        }
        return requests;
    }

    public List<ActionRequest> findPendingByDocRequestCdNodeName(String documentId, String requestCode,
            String nodeName) {
        List<ActionRequest> requests = new ArrayList<>();
        for (Object object : getActionRequestDAO().findAllPendingByDocId(documentId)) {
            ActionRequest actionRequest = (ActionRequest) object;
            if (ActionRequest.compareActionCode(actionRequest.getActionRequested(), requestCode, true) > 0) {
                continue;
            }
            if (actionRequest.getNodeInstance() != null
                    && actionRequest.getNodeInstance().getName().equals(nodeName)) {
                requests.add(actionRequest);
            }
        }
        return requests;
    }

    public List findActivatedByGroup(String groupId) {
        return getActionRequestDAO().findActivatedByGroup(groupId);
    }

    private ActionListService getActionListService() {
        return KEWServiceLocator.getActionListService();
    }

    private ActionTakenService getActionTakenService() {
        return KEWServiceLocator.getActionTakenService();
    }

    public ActionRequestDAO getActionRequestDAO() {
        return actionRequestDAO;
    }

    public void setActionRequestDAO(ActionRequestDAO actionRequestDAO) {
        this.actionRequestDAO = actionRequestDAO;
    }

    private RouteHeaderService getRouteHeaderService() {
        return (RouteHeaderService) KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
    }

    public List<ActionRequest> findByStatusAndDocId(String statusCd, String documentId) {
        return getActionRequestDAO().findByStatusAndDocId(statusCd, documentId);
    }

    public void alterActionRequested(List actionRequests, String actionRequestCd) {
        for (Object actionRequest1 : actionRequests) {
            ActionRequest actionRequest = (ActionRequest) actionRequest1;

            actionRequest.setActionRequested(actionRequestCd);
            for (ActionItem item : actionRequest.getActionItems()) {
                item.setActionRequestCd(actionRequestCd);
            }

            saveActionRequest(actionRequest);
        }
    }

    // TODO this still won't work in certain cases when checking from the root
    public boolean isDuplicateRequest(ActionRequest actionRequest) {
        List<ActionRequest> requests = findAllRootActionRequestsByDocumentId(actionRequest.getDocumentId());
        for (ActionRequest existingRequest : requests) {
            if (existingRequest.isDone()
                    && existingRequest.getRouteLevel().equals(actionRequest.getRouteLevel())
                    && Objects.equals(existingRequest.getPrincipalId(), actionRequest.getPrincipalId())
                    && Objects.equals(existingRequest.getGroupId(), actionRequest.getGroupId())
                    && Objects.equals(existingRequest.getRoleName(), actionRequest.getRoleName())
                    && Objects.equals(existingRequest.getQualifiedRoleName(), actionRequest.getQualifiedRoleName())
                    && existingRequest.getActionRequested().equals(actionRequest.getActionRequested())) {
                return true;
            }
        }
        return false;
    }

    public Recipient findDelegator(List actionRequests) {
        Recipient delegator = null;
        String requestCode = KewApiConstants.ACTION_REQUEST_FYI_REQ;
        for (Object actionRequest1 : actionRequests) {
            ActionRequest actionRequest = (ActionRequest) actionRequest1;
            ActionRequest delegatorRequest = findDelegatorRequest(actionRequest);
            if (delegatorRequest != null) {
                if (ActionRequest.compareActionCode(delegatorRequest.getActionRequested(), requestCode, true) >=
                        0) {
                    delegator = delegatorRequest.getRecipient();
                    requestCode = delegatorRequest.getActionRequested();
                }
            }
        }
        return delegator;
    }

    public Recipient findDelegator(ActionRequest actionRequest) {
        ActionRequest delegatorRequest = findDelegatorRequest(actionRequest);
        Recipient delegator = null;
        if (delegatorRequest != null) {
            delegator = delegatorRequest.getRecipient();
        }
        return delegator;
    }

    public ActionRequest findDelegatorRequest(ActionRequest actionRequest) {
        ActionRequest parentRequest = actionRequest.getParentActionRequest();
        if (parentRequest != null && !(parentRequest.isUserRequest() || parentRequest.isGroupRequest())) {
            parentRequest = findDelegatorRequest(parentRequest);
        }
        return parentRequest;
    }

    public void deleteByDocumentId(String documentId) {
        actionRequestDAO.deleteByDocumentId(documentId);
    }

    public void deleteByActionRequestId(String actionRequestId) {
        actionRequestDAO.delete(actionRequestId);
    }

    public void validateActionRequest(ActionRequest actionRequest) {
        LOG.debug("Enter validateActionRequest(..)");
        List<WorkflowServiceErrorImpl> errors = new ArrayList<>();

        String actionRequestCd = actionRequest.getActionRequested();
        if (actionRequestCd == null || actionRequestCd.trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest cd null.", "actionrequest.actionrequestcd.empty",
                    actionRequest.getActionRequestId().toString()));
        } else if (!KewApiConstants.ACTION_REQUEST_CD.containsKey(actionRequestCd)) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest cd invalid.",
                    "actionrequest.actionrequestcd.invalid", actionRequest.getActionRequestId()));
        }

        String documentId = actionRequest.getDocumentId();
        if (documentId == null || StringUtils.isEmpty(documentId)) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest Document id empty.",
                    "actionrequest.documentid.empty", actionRequest.getActionRequestId()));
        } else if (getRouteHeaderService().getRouteHeader(documentId) == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest Document id invalid.",
                    "actionrequest.documentid.invalid", actionRequest.getActionRequestId()));
        }

        String actionRequestStatus = actionRequest.getStatus();
        if (actionRequestStatus == null || actionRequestStatus.trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest status null.",
                    "actionrequest.actionrequeststatus.empty", actionRequest.getActionRequestId()));
        } else if (ActionRequestStatus.fromCode(actionRequestStatus) == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest status invalid.",
                    "actionrequest.actionrequeststatus.invalid", actionRequest.getActionRequestId()));
        }

        if (actionRequest.getResponsibilityId() == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest responsibility id null.",
                    "actionrequest.responsibilityid.empty", actionRequest.getActionRequestId()));
        }

        Integer priority = actionRequest.getPriority();
        if (priority == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest priority null.", "actionrequest.priority.empty",
                    actionRequest.getActionRequestId()));
        }

        // if(actionRequest.getRouteMethodName() == null || actionRequest.getRouteMethodName().trim().equals("")){
        // errors.add(new WorkflowServiceErrorImpl("ActionRequest route method name null.",
        // "actionrequest.routemethodname.empty", actionRequest.getActionRequestId().toString()));
        // }

        Integer routeLevel = actionRequest.getRouteLevel();
        if (routeLevel == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest route level null.",
                    "actionrequest.routelevel.empty", actionRequest.getActionRequestId().toString()));
        } else if (routeLevel < -1) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest route level invalid.",
                    "actionrequest.routelevel.invalid", actionRequest.getActionRequestId()));
        }

        Integer version = actionRequest.getDocVersion();
        if (version == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest doc version null.",
                    "actionrequest.docversion.empty", actionRequest.getActionRequestId()));
        }

        if (actionRequest.getCreateDate() == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionRequest create date null.",
                    "actionrequest.createdate.empty", actionRequest.getActionRequestId()));
        }

        String recipientType = actionRequest.getRecipientTypeCd();
        if (recipientType != null && !recipientType.trim().equals("")) {
            if (recipientType.equals(KewApiConstants.WORKGROUP)) {
                String workgroupId = actionRequest.getGroupId();
                if (workgroupId == null) {
                    errors.add(new WorkflowServiceErrorImpl("ActionRequest workgroup null.",
                            "actionrequest.workgroup.empty", actionRequest.getActionRequestId()));
                } else if (KimApiServiceLocator.getGroupService().getGroup(workgroupId) == null) {
                    errors.add(new WorkflowServiceErrorImpl("ActionRequest workgroup invalid.",
                            "actionrequest.workgroup.invalid", actionRequest.getActionRequestId()));
                }

            }
            if (recipientType.equals(KewApiConstants.PERSON)) {
                String principalId = actionRequest.getPrincipalId();
                if (principalId == null || principalId.trim().equals("")) {
                    errors.add(new WorkflowServiceErrorImpl("ActionRequest person id null.",
                            "actionrequest.persosn.empty", actionRequest.getActionRequestId()));
                } else {
                    Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
                    if (principal == null) {
                        errors.add(new WorkflowServiceErrorImpl("ActionRequest person id invalid.",
                                "actionrequest.personid.invalid", actionRequest.getActionRequestId()));
                    }
                }

                if (recipientType.equals(KewApiConstants.ROLE)
                        && (actionRequest.getRoleName() == null || actionRequest.getRoleName().trim().equals(""))) {
                    errors.add(new WorkflowServiceErrorImpl("ActionRequest role name null.",
                            "actionrequest.rolename.null", actionRequest.getActionRequestId()));
                }
            }
            LOG.debug("Exit validateActionRequest(..) ");
            if (!errors.isEmpty()) {
                throw new WorkflowServiceErrorException("ActionRequest Validation Error", errors);
            }
        }
    }

    public List getDelegateRequests(ActionRequest actionRequest) {
        List<ActionRequest> delegateRequests = new ArrayList<>();
        List requests = getTopLevelRequests(actionRequest);
        for (Object request : requests) {
            ActionRequest parentActionRequest = (ActionRequest) request;
            delegateRequests.addAll(parentActionRequest.getChildrenRequests());
        }
        return delegateRequests;
    }

    public List getTopLevelRequests(ActionRequest actionRequest) {
        List<ActionRequest> topLevelRequests = new ArrayList<>();
        if (actionRequest.isRoleRequest()) {
            topLevelRequests.addAll(actionRequest.getChildrenRequests());
        } else {
            topLevelRequests.add(actionRequest);
        }
        return topLevelRequests;
    }

    public boolean isValidActionRequestCode(String actionRequestCode) {
        return actionRequestCode != null && KewApiConstants.ACTION_REQUEST_CODES.containsKey(actionRequestCode);
    }

    public boolean doesPrincipalHaveRequest(String principalId, String documentId) {
        if (getActionRequestDAO().doesDocumentHaveUserRequest(principalId, documentId)) {
            return true;
        }
        // TODO since we only store the workgroup id for workgroup requests, if the user is in a workgroup that has a
        // request than we need get all the requests with workgroup ids and see if our user is in that group
        List<String> groupIds = getActionRequestDAO().getRequestGroupIds(documentId);
        for (String groupId : groupIds) {
            if (KimApiServiceLocator.getGroupService().isMemberOfGroup(principalId, groupId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionRequest getActionRequestForRole(String actionTakenId) {
        return getActionRequestDAO().getRoleActionRequestByActionTakenId(actionTakenId);
    }
}
