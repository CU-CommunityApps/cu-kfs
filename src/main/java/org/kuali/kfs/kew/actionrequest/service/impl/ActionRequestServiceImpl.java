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
package org.kuali.kfs.kew.actionrequest.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
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
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.routemodule.RouteModule;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.util.FutureRequestDocumentStateManager;
import org.kuali.kfs.kew.util.ResponsibleParty;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.identity.IdentityService;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.annotation.CacheEvict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can be removed when we upgrade to the 2023-02-08 financials patch.
 * ============
 * 
 * Default implementation of the {@link ActionRequestService}.
 */
public class ActionRequestServiceImpl implements ActionRequestService {

    private static final Logger LOG = LogManager.getLogger();

    private ActionRequestDAO actionRequestDAO;
    private GroupService groupService;
    private IdentityService identityService;
    private ParameterService parameterService;

    @Override
    public ActionRequest findByActionRequestId(final String actionRequestId) {
        return getActionRequestDAO().getActionRequestByActionRequestId(actionRequestId);
    }

    @Override
    public Map<String, String> getActionsRequested(
            final DocumentRouteHeaderValue routeHeader, final String principalId,
            final boolean completeAndApproveTheSame) {
        return getActionsRequested(principalId, routeHeader.getActionRequests(), completeAndApproveTheSame);
    }

    /**
     * @param principalId
     * @param actionRequests
     * @param completeAndApproveTheSame
     * @return a Map of actions that are requested for the given principalId in the given list of action requests.
     */
    protected Map<String, String> getActionsRequested(
            final String principalId, final List<ActionRequest> actionRequests,
            final boolean completeAndApproveTheSame) {
        final Map<String, String> actionsRequested = new HashMap<>();
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_FYI_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ, "false");
        actionsRequested.put(KewApiConstants.ACTION_REQUEST_COMPLETE_REQ, "false");
        String topActionRequested = KewApiConstants.ACTION_REQUEST_FYI_REQ;
        for (final ActionRequest actionRequest : actionRequests) {
            // we are getting the full list of requests here, so no need to look at role requests, if we did this then
            // we could get a "false positive" for "all approve" roles where only part of the request graph is marked
            // as "done"
            if (!RecipientType.ROLE.getCode().equals(actionRequest.getRecipientTypeCd())
                    && actionRequest.isRecipientRoutedRequest(principalId) && actionRequest.isActive()) {
                final int actionRequestComparison = ActionRequest
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

    @Override
    public ActionRequest initializeActionRequestGraph(ActionRequest actionRequest,
            final DocumentRouteHeaderValue document, final RouteNodeInstance nodeInstance) {
        if (actionRequest.getParentActionRequest() != null) {
            LOG.warn("-->A non parent action request from doc {}", document::getDocumentId);
            actionRequest = KEWServiceLocator.getActionRequestService().getRoot(actionRequest);
        }
        propagatePropertiesToRequestGraph(actionRequest, document, nodeInstance);
        return actionRequest;
    }

    private void propagatePropertiesToRequestGraph(
            final ActionRequest actionRequest,
            final DocumentRouteHeaderValue document, final RouteNodeInstance nodeInstance) {
        setPropertiesToRequest(actionRequest, document, nodeInstance);
        for (final ActionRequest actionRequestValue : actionRequest.getChildrenRequests()) {
            propagatePropertiesToRequestGraph(actionRequestValue, document, nodeInstance);
        }
    }

    private void setPropertiesToRequest(
            final ActionRequest actionRequest, final DocumentRouteHeaderValue document,
            final RouteNodeInstance nodeInstance) {
        actionRequest.setDocumentId(document.getDocumentId());
        actionRequest.setDocVersion(document.getDocVersion());
        actionRequest.setRouteLevel(document.getDocRouteLevel());
        actionRequest.setNodeInstance(nodeInstance);
        actionRequest.setStatus(ActionRequestStatus.INITIALIZED.getCode());
    }

    @Override
    public void activateRequests(final Collection actionRequests) {
        activateRequests(actionRequests, new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    @Override
    public void activateRequests(final Collection actionRequests, final ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        activationContext.setGeneratedActionItems(new ArrayList<>());
        activateRequestsInternal(actionRequests, activationContext);
        if (!activationContext.isSimulation()) {
            KEWServiceLocator.getNotificationService().notify(activationContext.getGeneratedActionItems());
        }
        LOG.info(
                "{} {} action requests.",
                () -> activationContext.isSimulation() ? "Simulated activation of" : "Activated",
                actionRequests::size
        );
        LOG.debug("Generated {} action items.", () -> activationContext.getGeneratedActionItems().size());
    }

    @Override
    public void activateRequest(final ActionRequest actionRequest) {
        activateRequests(Collections.singletonList(actionRequest),
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    @Override
    public void activateRequestNoNotification(final ActionRequest actionRequest, final ActivationContext activationContext) {
        activationContext.setGeneratedActionItems(new ArrayList<>());
        activateRequestInternal(actionRequest, activationContext);
    }

    /**
     * Internal helper method for activating a Collection of action requests and their children. Maintains an
     * accumulator for generated action items.
     *
     * @param actionRequests
     * @param activationContext
     */
    private void activateRequestsInternal(final Collection actionRequests, final ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        final List<?> actionRequestList = new ArrayList<Object>(actionRequests);
        for (int i = 0; i < actionRequestList.size(); i++) {
            activateRequestInternal((ActionRequest) actionRequestList.get(i), activationContext);
        }
    }

    /**
     * Internal helper method for activating a single action requests and it's children. Maintains an accumulator for
     * generated action items.
     */
    private void activateRequestInternal(final ActionRequest actionRequest, final ActivationContext activationContext) {
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

        if (activationContext.isSimulation()) {
            LOG.info("activateRequestInternal(...) - Simulated activation of request");
        } else {
            LOG.info(
                    "activateRequestInternal(...) - Activated action request : actionRequestId={}",
                    actionRequest::getActionRequestId
            );
        }
    }

    /**
     * Generates ActionItems for the given ActionRequest and returns the List of generated Action Items.
     *
     * @param actionRequest
     * @param activationContext
     * @return the List of generated ActionItems
     */
    private List<ActionItem> generateActionItems(
            final ActionRequest actionRequest,
            final ActivationContext activationContext) {
        LOG.debug("generating the action items for request {}", actionRequest::getActionRequestId);
        final List<ActionItem> actionItems = new ArrayList<>();
        if (!actionRequest.isPrimaryDelegator()) {
            if (actionRequest.isGroupRequest()) {
                final List<String> principalIds =
                        groupService.getMemberPrincipalIds(actionRequest.getGroupId());
                actionItems.addAll(createActionItemsForPrincipals(actionRequest, principalIds));
            } else if (actionRequest.isUserRequest()) {
                final ActionItem actionItem = getActionListService().createActionItemForActionRequest(actionRequest);
                actionItems.add(actionItem);
            }
        }
        if (!activationContext.isSimulation()) {
            for (final ActionItem actionItem : actionItems) {
                LOG.debug("Saving action item: {}", actionItems);
                getActionListService().saveActionItem(actionItem);
            }
        } else {
            actionRequest.getSimulatedActionItems().addAll(actionItems);
        }
        return actionItems;
    }

    private List<ActionItem> createActionItemsForPrincipals(
            final ActionRequest actionRequest,
            final List<String> principalIds) {
        final List<ActionItem> actionItems = new ArrayList<>();
        for (final String principalId : principalIds) {

            final ActionItem actionItem = getActionListService().createActionItemForActionRequest(actionRequest);
            actionItem.setPrincipalId(principalId);
            actionItem.setRoleName(actionRequest.getQualifiedRoleName());

            if (principalId == null) {
                final IllegalArgumentException e = new IllegalArgumentException(
                        "Exception thrown when trying to add action item with null principalId");
                LOG.error(e);
                throw e;
            } else {
                actionItems.add(actionItem);
            }
        }
        return actionItems;
    }

    private void processResponsibilityId(final ActionRequest actionRequest) {
        if (actionRequest.getResolveResponsibility()) {
            final String responsibilityId = actionRequest.getResponsibilityId();
            try {
                final RouteModule routeModule = KEWServiceLocator.getRouteModuleService().findRouteModule(actionRequest);
                if (responsibilityId != null && actionRequest.isRouteModuleRequest()) {
                    LOG.debug(
                            "Resolving responsibility id for action request id={} and responsibility id={}",
                            actionRequest::getActionRequestId,
                            actionRequest::getResponsibilityId
                    );
                    final ResponsibleParty responsibleParty =
                            routeModule.resolveResponsibilityId(actionRequest.getResponsibilityId());
                    if (responsibleParty == null) {
                        return;
                    }
                    if (responsibleParty.getPrincipalId() != null) {
                        final Principal user = identityService.getPrincipal(responsibleParty.getPrincipalId());
                        actionRequest.setPrincipalId(user.getPrincipalId());
                    } else if (responsibleParty.getGroupId() != null) {
                        actionRequest.setGroupId(responsibleParty.getGroupId());
                    } else if (responsibleParty.getRoleName() != null) {
                        actionRequest.setRoleName(responsibleParty.getRoleName());
                    }
                }
            } catch (final Exception e) {
                LOG.error("Exception thrown when trying to resolve responsibility id {}", responsibilityId, e);
                throw new RuntimeException(e);
            }
        }
    }

    protected boolean deactivateOnActionAlreadyTaken(
            final ActionRequest actionRequestToActivate,
            final ActivationContext activationContext) {
        final FutureRequestDocumentStateManager futureRequestStateMngr;

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
            final ActionTaken previousActionTaken;
            if (!activationContext.isSimulation()) {
                previousActionTaken = getActionTakenService().getPreviousAction(actionRequestToActivate);
            } else {
                previousActionTaken = getActionTakenService().getPreviousAction(actionRequestToActivate,
                        activationContext.getSimulatedActionsTaken());
            }
            if (previousActionTaken != null) {
                LOG.debug(
                        "found a satisfying action taken so setting this request done.  Action Request Id {}",
                        actionRequestToActivate::getActionRequestId
                );
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
        LOG.debug("Forcing action for action request {}", actionRequestToActivate::getActionRequestId);
        return false;
    }

    /**
     * Checks if the action request which is being activated has a group with no members. If this is the case then it
     * will immediately initiate de-activation on the request since a group with no members will result in no action
     * items being generated so should be effectively skipped.
     */
    protected boolean deactivateOnEmptyGroup(
            final ActionRequest actionRequestToActivate,
            final ActivationContext activationContext) {
        if (actionRequestToActivate.isGroupRequest()) {
            if (groupService.getMemberPrincipalIds(actionRequestToActivate.getGroup().getId()).isEmpty()) {
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
            final ActionRequest actionRequestToActivate, final ActivationContext activationContext) {
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

    @Override
    public void deactivateRequest(final ActionTaken actionTaken, final ActionRequest actionRequest) {
        deactivateRequest(actionTaken, actionRequest, null,
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    @Override
    public void deactivateRequest(
            final ActionTaken actionTaken, final ActionRequest actionRequest,
            final ActivationContext activationContext) {
        deactivateRequest(actionTaken, actionRequest, null, activationContext);
    }

    private void deactivateRequest(
            final ActionTaken actionTaken, final ActionRequest actionRequest,
            final ActionRequest deactivationRequester, final ActivationContext activationContext) {
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

    @Override
    public void deactivateRequests(final ActionTaken actionTaken, final List actionRequests) {
        deactivateRequests(actionTaken, actionRequests, null,
                new ActivationContext(!ActivationContext.CONTEXT_IS_SIMULATION));
    }

    @Override
    public void deactivateRequests(
            final ActionTaken actionTaken, final List actionRequests, final ActivationContext activationContext) {
        deactivateRequests(actionTaken, actionRequests, null, activationContext);
    }

    private void deactivateRequests(
            final ActionTaken actionTaken, final Collection actionRequests,
            final ActionRequest deactivationRequester, final ActivationContext activationContext) {
        if (actionRequests == null) {
            return;
        }
        for (final Iterator iterator = actionRequests.iterator(); iterator.hasNext(); ) {
            final ActionRequest actionRequest = (ActionRequest) iterator.next();
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
    private boolean haltForAllApprove(final ActionRequest actionRequest, final ActionRequest deactivationRequester) {
        if (ActionRequestPolicy.ALL.getCode().equals(actionRequest.getApprovePolicy())
                && actionRequest.hasChild(deactivationRequester)) {
            for (final ActionRequest childRequest : actionRequest.getChildrenRequests()) {
                if (!childRequest.isDeactivated()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<ActionRequest> getRootRequests(final Collection<ActionRequest> actionRequests) {
        final Set<ActionRequest> unsavedRequests = new HashSet<>();
        final Map<String, ActionRequest> requestMap = new HashMap<>();
        for (final ActionRequest actionRequest : actionRequests) {
            final ActionRequest rootRequest = getRoot(actionRequest);
            if (rootRequest.getActionRequestId() != null) {
                requestMap.put(rootRequest.getActionRequestId(), rootRequest);
            } else {
                unsavedRequests.add(rootRequest);
            }
        }
        final List<ActionRequest> requests = new ArrayList<>();
        requests.addAll(requestMap.values());
        requests.addAll(unsavedRequests);
        return requests;
    }

    @Override
    public ActionRequest getRoot(final ActionRequest actionRequest) {
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
    @Override
    public List<ActionRequest> findAllPendingRequests(final String documentId) {
        final ActionRequestDAO arDAO = getActionRequestDAO();
        return arDAO.findByStatusAndDocId(ActionRequestStatus.ACTIVATED.getCode(), documentId);
    }

    @Override
    public List findAllValidRequests(final String principalId, final String documentId, final String requestCode) {
        final ActionRequestDAO arDAO = getActionRequestDAO();
        final Collection pendingArs = arDAO.findByStatusAndDocId(ActionRequestStatus.ACTIVATED.getCode(), documentId);
        final List<String> arGroups = groupService.getGroupIdsByPrincipalId(principalId);
        return filterActionRequestsByCode((List<ActionRequest>) pendingArs, principalId, arGroups,
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
    @Override
    public List<ActionRequest> filterActionRequestsByCode(
            final List<ActionRequest> actionRequests,
            final String principalId, final List<String> principalGroupIds, final String requestCode) {
        final List<ActionRequest> filteredActionRequests = new ArrayList<>();

        for (final ActionRequest ar : actionRequests) {
            if (ActionRequest.compareActionCode(ar.getActionRequested(), requestCode, true) > 0) {
                continue;
            }
            if (ar.isUserRequest() && principalId.equals(ar.getPrincipalId())) {
                filteredActionRequests.add(ar);
            } else if (ar.isGroupRequest() && principalGroupIds != null && !principalGroupIds.isEmpty()) {
                for (final String groupId : principalGroupIds) {
                    if (groupId.equals(ar.getGroupId())) {
                        filteredActionRequests.add(ar);
                    }
                }
            }
        }

        return filteredActionRequests;
    }

    @Override
    public void updateActionRequestsForResponsibilityChange(final Set<String> responsibilityIds) {
        final Collection<String> documentsAffected =
                getRouteHeaderService().findPendingByResponsibilityIds(responsibilityIds);
        updateActionRequestsForResponsibilityChange(documentsAffected, responsibilityIds.size());
    }

    private void updateActionRequestsForResponsibilityChange(
            final Collection documentsAffected, final int responsibilityChangeCount
    ) {
        final String cacheWaitValue = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.RULE_DETAIL_TYPE,
                KewApiConstants.RULE_CACHE_REQUEUE_DELAY);
        Long cacheWait = KewApiConstants.DEFAULT_CACHE_REQUEUE_WAIT_TIME;
        if (StringUtils.isNotEmpty(cacheWaitValue)) {
            try {
                cacheWait = Long.valueOf(cacheWaitValue);
            } catch (final NumberFormatException e) {
                LOG.warn("Cache wait time is not a valid number: {}", cacheWaitValue);
            }
        }
        final Long loggableCacheWait = cacheWait;
        LOG.info(
                "Scheduling requeue of {} documents, affected by {} responsibility changes.  Installing a "
                + "processing wait time of {} milliseconds to avoid stale rule cache.",
                documentsAffected::size,
                () -> responsibilityChangeCount,
                () -> loggableCacheWait
        );
        for (final Object aDocumentsAffected : documentsAffected) {
            final String documentId = (String) aDocumentsAffected;

            final DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId);

            if (documentType.getRegenerateActionRequestsOnChange().getPolicyValue()) {
                final DocumentRefreshQueue documentRequeuer = KewApiServiceLocator.getDocumentRequeuerService(
                        documentId, cacheWait);
                documentRequeuer.refreshDocument(
                        documentId,
                        "Document was requeued because of a responsibility change."
                );
            }
        }
        LOG.info("updateActionRequestsForResponsibilityChange(...) - Exit");
    }

    @CacheEvict(
            allEntries = true,
            value = {
                Role.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleMember.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                DelegateType.CACHE_NAME
            }
    )
    @Override
    public void updateActionRequestsForResponsibilityChange(
            final Set<String> responsibilityIds,
            final String docIdToIgnore,
            final Set<String> accountNumbers,
            final Set<String> documentTypes
    ) {
        final Collection<String> documentsAffected = getRouteHeaderService()
                .findPendingByResponsibilityIds(responsibilityIds, accountNumbers, documentTypes);
        documentsAffected.remove(docIdToIgnore);
        updateActionRequestsForResponsibilityChange(documentsAffected, responsibilityIds.size());
    }

    /**
     * Deletes an action request and all of its action items following the graph down through the action request's
     * children. This method should be invoked on a top-level action request.
     */
    @Override
    public void deleteActionRequestGraph(final ActionRequest actionRequest) {
        deleteActionItems(actionRequest);
        if (actionRequest.getActionTakenId() != null) {
            final ActionTaken actionTaken =
                    getActionTakenService().findByActionTakenId(actionRequest.getActionTakenId());

            if (actionTaken != null) {
                getActionTakenService().delete(actionTaken);
            }
        }
        getActionRequestDAO().delete(actionRequest.getActionRequestId());
        for (final ActionRequest child : actionRequest.getChildrenRequests()) {
            deleteActionRequestGraph(child);
        }
    }

    /**
     * Deletes the action items for the action request
     *
     * @param actionRequest the action request whose action items to delete
     */
    private void deleteActionItems(final ActionRequest actionRequest) {
        final List<ActionItem> actionItems = actionRequest.getActionItems();
        LOG.debug("deleting {} action items for action request: {}", actionItems::size, () -> actionRequest);
        for (final ActionItem actionItem : actionItems) {
            LOG.debug("deleting action item: {}", actionItem);
            getActionListService().deleteActionItem(actionItem);
        }
    }

    @Override
    public List<ActionRequest> findByDocumentIdIgnoreCurrentInd(final String documentId) {
        return getActionRequestDAO().findByDocumentIdIgnoreCurrentInd(documentId);
    }

    @Override
    public List<ActionRequest> findAllActionRequestsByDocumentId(final String documentId) {
        return getActionRequestDAO().findAllByDocId(documentId);
    }

    @Override
    public List<ActionRequest> findAllRootActionRequestsByDocumentId(final String documentId) {
        return getActionRequestDAO().findAllRootByDocId(documentId);
    }

    @Override
    public List<ActionRequest> findPendingByActionRequestedAndDocId(
            final String actionRequestedCd,
            final String documentId) {
        return getActionRequestDAO().findPendingByActionRequestedAndDocId(actionRequestedCd, documentId);
    }

    @Override
    public List<String> getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(
            final String actionRequestedCd,
            final String documentId) {
        final List<String> principalIds = new ArrayList<>();
        final List<ActionRequest> actionRequests = findPendingByActionRequestedAndDocId(actionRequestedCd, documentId);
        for (final ActionRequest actionRequest : actionRequests) {
            if (actionRequest.isUserRequest()) {
                principalIds.add(actionRequest.getPrincipalId());
            } else if (actionRequest.isGroupRequest()) {
                principalIds.addAll(
                        groupService.getMemberPrincipalIds(actionRequest.getGroupId()));
            }
        }
        return principalIds;
    }

    @Override
    public List<ActionRequest> findPendingRootRequestsByDocId(final String documentId) {
        return getRootRequests(findPendingByDoc(documentId));
    }

    @Override
    public List<ActionRequest> findPendingRootRequestsByDocIdAtRouteNode(
            final String documentId,
            final String nodeInstanceId) {
        return getActionRequestDAO().findPendingRootRequestsByDocIdAtRouteNode(documentId, nodeInstanceId);
    }

    @Override
    public List<ActionRequest> findRootRequestsByDocIdAtRouteNode(final String documentId, final String nodeInstanceId) {
        return getActionRequestDAO().findRootRequestsByDocIdAtRouteNode(documentId, nodeInstanceId);
    }

    @Override
    public void saveActionRequest(final ActionRequest actionRequest) {
        if (actionRequest.isGroupRequest()) {
            final Group group = actionRequest.getGroup();
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

    @Override
    public List<ActionRequest> findPendingByDoc(final String documentId) {
        return getActionRequestDAO().findAllPendingByDocId(documentId);
    }

    @Override
    public List findActivatedByGroup(final String groupId) {
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

    public void setActionRequestDAO(final ActionRequestDAO actionRequestDAO) {
        this.actionRequestDAO = actionRequestDAO;
    }

    private RouteHeaderService getRouteHeaderService() {
        return KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
    }

    @Override
    public Recipient findDelegator(final List actionRequests) {
        Recipient delegator = null;
        String requestCode = KewApiConstants.ACTION_REQUEST_FYI_REQ;
        for (final Object actionRequest1 : actionRequests) {
            final ActionRequest actionRequest = (ActionRequest) actionRequest1;
            final ActionRequest delegatorRequest = findDelegatorRequest(actionRequest);
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

    @Override
    public ActionRequest findDelegatorRequest(final ActionRequest actionRequest) {
        ActionRequest parentRequest = actionRequest.getParentActionRequest();
        if (parentRequest != null && !(parentRequest.isUserRequest() || parentRequest.isGroupRequest())) {
            parentRequest = findDelegatorRequest(parentRequest);
        }
        return parentRequest;
    }

    @Override
    public boolean doesPrincipalHaveRequest(final String principalId, final String documentId) {
        if (getActionRequestDAO().doesDocumentHaveUserRequest(principalId, documentId)) {
            return true;
        }
        // TODO since we only store the workgroup id for workgroup requests, if the user is in a workgroup that has a
        // request than we need get all the requests with workgroup ids and see if our user is in that group
        final List<String> groupIds = getActionRequestDAO().getRequestGroupIds(documentId);
        for (final String groupId : groupIds) {
            if (groupService.isMemberOfGroup(principalId, groupId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionRequest getActionRequestForRole(final String actionTakenId) {
        return getActionRequestDAO().getRoleActionRequestByActionTakenId(actionTakenId);
    }

    public void setGroupService(final GroupService groupService) {
        this.groupService = groupService;
    }

    public void setIdentityService(final IdentityService identityService) {
        this.identityService = identityService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
