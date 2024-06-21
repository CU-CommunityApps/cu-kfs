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
package org.kuali.kfs.kew.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.ActionRequestFactory;
import org.kuali.kfs.kew.actionrequest.Recipient;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.ActionRequestType;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kew.api.doctype.DocumentTypePolicy;
import org.kuali.kfs.kew.api.exception.InvalidActionTakenException;
import org.kuali.kfs.kew.engine.RouteHelper;
import org.kuali.kfs.kew.engine.node.NodeGraphSearchCriteria;
import org.kuali.kfs.kew.engine.node.NodeGraphSearchResult;
import org.kuali.kfs.kew.engine.node.RouteNode;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteLevelChange;
import org.kuali.kfs.kew.framework.postprocessor.PostProcessor;
import org.kuali.kfs.kew.framework.postprocessor.ProcessDocReport;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Returns a document to a previous node in the route.
 * <p>
 * Current implementation only supports returning to a node on the main branch of the
 * document.
 */
public class ReturnToPreviousNodeAction extends ActionBase {

    private static final Logger LOG = LogManager.getLogger();

    // ReturnToPrevious returns to initial node when sent a null node name
    protected static final String INITIAL_NODE_NAME = null;
    protected static final boolean DEFAULT_SEND_NOTIFICATIONS = true;

    private final RouteHelper helper = new RouteHelper();
    protected final String nodeName;
    private boolean superUserUsage;
    private final boolean sendNotifications;
    private final boolean sendNotificationsForPreviousRequests;

    public ReturnToPreviousNodeAction(final DocumentRouteHeaderValue routeHeader, final Person person) {
        this(routeHeader, person, DEFAULT_ANNOTATION, INITIAL_NODE_NAME, DEFAULT_SEND_NOTIFICATIONS);
    }

    public ReturnToPreviousNodeAction(
            final DocumentRouteHeaderValue routeHeader, final Person person, final String annotation,
            final String nodeName, final boolean sendNotifications, final boolean runPostProcessorLogic) {
        this(KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD, routeHeader, person, annotation, nodeName,
                sendNotifications, runPostProcessorLogic);
    }

    /**
     * Constructor used to override the action taken code...e.g. when being performed as part of a Move action
     */
    public ReturnToPreviousNodeAction(
            final DocumentRouteHeaderValue routeHeader,
            final Person person, final String annotation, final String nodeName, final boolean sendNotifications) {
        this(KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD, routeHeader, person, annotation, nodeName,
                sendNotifications, DEFAULT_RUN_POSTPROCESSOR_LOGIC);
    }

    /**
     * Constructor used to override the action taken code...e.g. when being performed as part of a Move action
     */
    protected ReturnToPreviousNodeAction(
            final String overrideActionTakenCode, final DocumentRouteHeaderValue routeHeader,
                                         final Person person, final String annotation, final String nodeName,
                                         final boolean sendNotifications, final boolean runPostProcessorLogic) {
        super(overrideActionTakenCode, routeHeader, person, annotation, runPostProcessorLogic);
        this.nodeName = nodeName;
        this.sendNotifications =
                isPolicySet(routeHeader.getDocumentType(), DocumentTypePolicy.NOTIFY_PENDING_ON_RETURN,
                        sendNotifications);
        sendNotificationsForPreviousRequests =
                isPolicySet(routeHeader.getDocumentType(), DocumentTypePolicy.NOTIFY_COMPLETED_ON_RETURN);
    }

    /**
     * Revokes requests, deactivating them with the specified ActionTaken.  Sends FYI notifications if sendNotifications
     * is true.
     * TODO will this work properly in the case of an ALL APPROVE role requests with some of the requests already
     * completed?
     */
    private void revokePendingRequests(
            final List<ActionRequest> pendingRequests, final ActionTaken actionTaken,
                                       final Person person, final Recipient delegator) {
        revokeRequests(pendingRequests);
        getActionRequestService().deactivateRequests(actionTaken, pendingRequests);
        if (sendNotifications) {
            generateNotificationsForRevokedRequests(pendingRequests, person, delegator);
        }
    }

    /**
     * Revokes requests (not deactivating them).  Sends FYI notifications if sendNotifications is true.
     */
    private void revokePreviousRequests(
            final List<ActionRequest> actionRequests, final Person person, final Recipient delegator) {
        revokeRequests(actionRequests);
        if (sendNotificationsForPreviousRequests) {
            generateNotificationsForRevokedRequests(actionRequests, person, delegator);
        }
    }

    /**
     * Generates FYIs for revoked ActionRequests
     *
     * @param revokedRequests the revoked actionrequests
     * @param person          principal taking action, omitted from notifications
     * @param delegator       delegator to omit from notifications
     */
    private void generateNotificationsForRevokedRequests(
            final List<ActionRequest> revokedRequests, final Person person, final Recipient delegator) {
        final ActionRequestFactory arFactory = new ActionRequestFactory(getRouteHeader());
        final List<ActionRequest> notificationRequests = arFactory
                .generateNotifications(revokedRequests, person, delegator, KewApiConstants.ACTION_REQUEST_FYI_REQ,
                        getActionTakenCode());
        getActionRequestService().activateRequests(notificationRequests);
    }

    /**
     * Takes a list of root action requests and marks them and all of their children as "non-current".
     */
    private void revokeRequests(final List<ActionRequest> actionRequests) {
        for (final ActionRequest actionRequest : actionRequests) {
            actionRequest.setCurrentIndicator(Boolean.FALSE);
            if (actionRequest.getActionTaken() != null) {
                actionRequest.getActionTaken().setCurrentIndicator(Boolean.FALSE);
                KEWServiceLocator.getActionTakenService().saveActionTaken(actionRequest.getActionTaken());
            }
            revokeRequests(actionRequest.getChildrenRequests());
            KEWServiceLocator.getActionRequestService().saveActionRequest(actionRequest);
        }
    }

    /**
     * Template method that determines what action request to generate when returning to initiator
     *
     * @return the ActionRequestType
     */
    protected ActionRequestType getReturnToInitiatorActionRequestType() {
        return ActionRequestType.APPROVE;
    }

    private void processReturnToInitiator(final RouteNodeInstance newNodeInstance) {
        // important to pull this from the RouteNode's DocumentType so we get the proper version
        final RouteNode initialNode =
                newNodeInstance.getRouteNode().getDocumentType().getPrimaryProcess().getInitialRouteNode();
        if (initialNode != null) {
            if (newNodeInstance.getRouteNode().getRouteNodeId().equals(initialNode.getRouteNodeId())) {
                LOG.debug("Document was returned to initiator");
                final ActionRequestFactory arFactory = new ActionRequestFactory(getRouteHeader(), newNodeInstance);
                final ActionRequest notificationRequest = arFactory
                        .createNotificationRequest(getReturnToInitiatorActionRequestType().getCode(),
                                determineInitialNodePerson(getRouteHeader()), getActionTakenCode(), getPerson(),
                                "Document initiator");
                getActionRequestService().activateRequest(notificationRequest);
            }
        }
    }

    /**
     * Determines which principal to generate an actionqrequest when the document is returned to the initial node By
     * default this is the document initiator.
     *
     * @param routeHeader the document route header
     * @return a Principal
     */
    protected Person determineInitialNodePerson(final DocumentRouteHeaderValue routeHeader) {
        return routeHeader.getInitiatorPerson();
    }

    /* (non-Javadoc)
     * @see org.kuali.kfs.kew.actions.ActionBase#isActionCompatibleRequest(java.util.List)
     */
    @Override
    public String validateActionRules() {
        return validateActionRules(getActionRequestService().findAllPendingRequests(routeHeader.getDocumentId()));
    }

    @Override
    public String validateActionRules(final List<ActionRequest> actionRequests) {
        if (!getRouteHeader().isValidActionToTake(getActionPerformedCode())) {
            final String docStatus = getRouteHeader().getDocRouteStatus();
            return "Document of status '" + docStatus + "' cannot taken action '" +
                    KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS + "' to node name " + nodeName;
        }
        final List<ActionRequest> filteredActionRequests = findApplicableActionRequests(actionRequests);
        if (!isActionCompatibleRequest(filteredActionRequests) && !isSuperUserUsage()) {
            return "No request for the user is compatible with the " +
                   WorkflowAction.fromCode(getActionTakenCode()).getLabel() + " action";
        }
        return "";
    }

    /**
     * Allows subclasses to determine which actionrequests to inspect for purposes of action validation
     *
     * @param actionRequests all actionrequests for this document
     * @return a (possibly) filtered list of actionrequests
     */
    protected List<ActionRequest> findApplicableActionRequests(final List<ActionRequest> actionRequests) {
        return filterActionRequestsByCode(actionRequests, KewApiConstants.ACTION_REQUEST_COMPLETE_REQ);
    }

    /* (non-Javadoc)
     * @see org.kuali.kfs.kew.actions.ActionBase#isActionCompatibleRequest(java.util.List)
     */
    @Override
    public boolean isActionCompatibleRequest(final List<ActionRequest> requests) {
        final String actionTakenCode = getActionPerformedCode();

        // can always cancel saved or initiated document
        if (routeHeader.isStateInitiated() || routeHeader.isStateSaved()) {
            return true;
        }

        boolean actionCompatible = false;
        final Iterator<ActionRequest> ars = requests.iterator();
        ActionRequest actionRequest;

        while (ars.hasNext()) {
            actionRequest = ars.next();

            if (actionRequest.isFYIRequest() || actionRequest.isAcknowledgeRequest() || actionRequest.isApproveRequest()
                    || actionRequest.isCompleteRequest()) {
                actionCompatible = true;
                break;
            }

            // RETURN_TO_PREVIOUS_ROUTE_LEVEL action available only if you've been routed a complete or approve request
            if (KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD.equals(actionTakenCode) &&
                    (actionRequest.isCompleteRequest() || actionRequest.isApproveRequest())) {
                actionCompatible = true;
            }
        }

        return actionCompatible;
    }

    @Override
    public void recordAction() throws InvalidActionTakenException {
        try (CloseableThreadContext.Instance ignored = CloseableThreadContext.put(
                KewApiConstants.DOCUMENT_ID_PARAMETER,
                getRouteHeader().getDocumentId())
        ) {
            doRecordAction();
        }
    }

    private void doRecordAction() throws InvalidActionTakenException {
        updateSearchableAttributesIfPossible();
        LOG.debug(
                "Returning document {} to previous node: {}, annotation: {}",
                () -> getRouteHeader().getDocumentId(),
                () -> nodeName,
                () -> annotation
        );

        final List actionRequests = getActionRequestService()
                .findAllValidRequests(
                        getPerson().getPrincipalId(), getDocumentId(),
                        KewApiConstants.ACTION_REQUEST_COMPLETE_REQ);
        final String errorMessage = validateActionRules(actionRequests);
        if (StringUtils.isNotEmpty(errorMessage)) {
            //throw new InvalidActionTakenException(errorMessage);
        }

        final Collection<RouteNodeInstance> activeNodeInstances =
                KEWServiceLocator.getRouteNodeService().getActiveNodeInstances(getRouteHeader().getDocumentId());
        final NodeGraphSearchCriteria criteria =
                new NodeGraphSearchCriteria(NodeGraphSearchCriteria.SEARCH_DIRECTION_BACKWARD, activeNodeInstances,
                        nodeName);
        final NodeGraphSearchResult result = KEWServiceLocator.getRouteNodeService().searchNodeGraph(criteria);
        validateReturnPoint(nodeName, activeNodeInstances, result);
        
        for (final RouteNodeInstance nodeInstance : activeNodeInstances) {
            // mark the node instance as having been revoked
            KEWServiceLocator.getRouteNodeService().revokeNodeInstance(getRouteHeader(), nodeInstance);
//            final List<ActionRequest> nodeRequests = getActionRequestService()
//                    .findRootRequestsByDocIdAtRouteNode(getRouteHeader().getDocumentId(),
//                            nodeInstance.getRouteNodeInstanceId());
//            for (final ActionRequest request : nodeRequests) {
//                if (request.isDone()) {
//                    doneRequests.add(request);
//                } else {
//                    pendingRequests.add(request);
//                }
//            }
        }

        LOG.debug("Record the returnToPreviousNode action");
        // determines the highest priority delegator in the list of action requests
        // this delegator will be used to save the action taken, and omitted from notification request generation
        final Recipient delegator = findDelegatorForActionRequests(actionRequests);
        final ActionTaken actionTaken = saveActionTaken(Boolean.FALSE, delegator);

        LOG.debug("Finding requests in return path and setting current indicator to FALSE");
        final List<ActionRequest> doneRequests = new ArrayList<>();
        final List<ActionRequest> pendingRequests = new ArrayList<>();
        for (final RouteNodeInstance nodeInstance : (List<RouteNodeInstance>) result.getPath()) {
            // mark the node instance as having been revoked
            KEWServiceLocator.getRouteNodeService().revokeNodeInstance(getRouteHeader(), nodeInstance);
            final List<ActionRequest> nodeRequests = getActionRequestService()
                    .findRootRequestsByDocIdAtRouteNode(getRouteHeader().getDocumentId(),
                            nodeInstance.getRouteNodeInstanceId());
            for (final ActionRequest request : nodeRequests) {
                if (request.isDone()) {
                    doneRequests.add(request);
                } else {
                    pendingRequests.add(request);
                }
            }
        }
        revokePreviousRequests(doneRequests, getPerson(), delegator);
        LOG.debug(
                "Change pending requests to FYI and activate for docId {}",
                () -> getRouteHeader().getDocumentId()
        );
        revokePendingRequests(pendingRequests, actionTaken, getPerson(), delegator);
        notifyActionTaken(actionTaken);
        executeNodeChange(activeNodeInstances, result);
        sendAdditionalNotifications();
    }

    /**
     * Template method subclasses can use to send addition notification upon a return to previous action.
     * This occurs after the postprocessors have been called and the node has been changed
     */
    protected void sendAdditionalNotifications() {
        // no implementation
    }

    /**
     * This method runs various validation checks on the nodes we ended up at so as to make sure we don't
     * invoke strange return scenarios.
     */
    private void validateReturnPoint(final String nodeName, final Collection activeNodeInstances, final NodeGraphSearchResult result)
            throws
            InvalidActionTakenException {
        final RouteNodeInstance resultNodeInstance = result.getResultNodeInstance();
        if (result.getResultNodeInstance() == null) {
            throw new InvalidActionTakenException("Could not locate return point for node name '" + nodeName + "'.");
        }
        assertValidNodeType(resultNodeInstance);
        assertValidBranch(resultNodeInstance, activeNodeInstances);
        assertValidProcess(resultNodeInstance, activeNodeInstances);
        assertFinalApprovalNodeNotInPath(result.getPath());
    }

    private void assertValidNodeType(final RouteNodeInstance resultNodeInstance) throws InvalidActionTakenException {
        // the return point can only be a simple or a split node
        if (!helper.isSimpleNode(resultNodeInstance.getRouteNode()) &&
                !helper.isSplitNode(resultNodeInstance.getRouteNode())) {
            throw new InvalidActionTakenException(
                    "Can only return to a simple or a split node, attempting to return to " +
                            resultNodeInstance.getRouteNode().getNodeType());
        }
    }

    private void assertValidBranch(final RouteNodeInstance resultNodeInstance, final Collection activeNodeInstances) throws
            InvalidActionTakenException {
        // the branch of the return point needs to be the same as one of the branches of the active nodes or the same as the root branch
        boolean inValidBranch = false;
        if (resultNodeInstance.getBranch().getParentBranch() == null) {
            inValidBranch = true;
        } else {
            for (final Iterator iterator = activeNodeInstances.iterator(); iterator.hasNext(); ) {
                final RouteNodeInstance nodeInstance = (RouteNodeInstance) iterator.next();
                if (nodeInstance.getBranch().getBranchId().equals(resultNodeInstance.getBranch().getBranchId())) {
                    inValidBranch = true;
                    break;
                }
            }
        }
//        if (!inValidBranch) {
//            throw new InvalidActionTakenException(
//                    "Returning to an illegal branch, can only return to node within the same branch as an active node or to the primary branch.");
//        }
    }

    private void assertValidProcess(final RouteNodeInstance resultNodeInstance, final Collection activeNodeInstances) throws
            InvalidActionTakenException {
        // if we are in a process, we need to return within the same process
        if (resultNodeInstance.isInProcess()) {
            boolean inValidProcess = false;
            for (final Iterator iterator = activeNodeInstances.iterator(); iterator.hasNext(); ) {
                final RouteNodeInstance nodeInstance = (RouteNodeInstance) iterator.next();
                if (nodeInstance.isInProcess() && nodeInstance.getProcess().getRouteNodeInstanceId()
                        .equals(nodeInstance.getProcess().getRouteNodeInstanceId())) {
                    inValidProcess = true;
                    break;
                }
            }
            if (!inValidProcess) {
                throw new InvalidActionTakenException(
                        "Returning into an illegal process, cannot return to node within a previously executing process.");
            }
        }
    }

    /**
     * Cannot return past a COMPLETE final approval node.  This means that you can return from an active and incomplete final approval node.
     *
     * @param path
     * @throws InvalidActionTakenException
     */
    private void assertFinalApprovalNodeNotInPath(final List path) throws InvalidActionTakenException {
        for (final Iterator iterator = path.iterator(); iterator.hasNext(); ) {
            final RouteNodeInstance nodeInstance = (RouteNodeInstance) iterator.next();
            // if we have a complete final approval node in our path, we cannot return past it
            if (nodeInstance.isComplete() && Boolean.TRUE.equals(nodeInstance.getRouteNode().getFinalApprovalInd())) {
                throw new InvalidActionTakenException(
                        "Cannot return past or through the final approval node '" + nodeInstance.getName() + "'.");
            }
        }
    }

    private void executeNodeChange(final Collection activeNodes, final NodeGraphSearchResult result) {
        final List<RouteNodeInstance> startingNodes = determineStartingNodes(result.getPath(), activeNodes);
        final RouteNodeInstance newNodeInstance = materializeReturnPoint(startingNodes, result);
        for (final RouteNodeInstance activeNode : startingNodes) {
            notifyNodeChange(activeNode, newNodeInstance);
        }
        processReturnToInitiator(newNodeInstance);
    }

    private void notifyNodeChange(
            final RouteNodeInstance oldNodeInstance,
            final RouteNodeInstance newNodeInstance
    ) {
        try {
            LOG.debug(
                    "Notifying post processor of route node change '{}'->'{}",
                    oldNodeInstance::getName,
                    newNodeInstance::getName
            );
            final PostProcessor postProcessor = routeHeader.getDocumentType().getPostProcessor();
            KEWServiceLocator.getRouteHeaderService().saveRouteHeader(getRouteHeader());
            final DocumentRouteLevelChange routeNodeChange = new DocumentRouteLevelChange(
                    routeHeader.getDocumentId(),
                    routeHeader.getAppDocId(),
                    oldNodeInstance.getName(),
                    newNodeInstance.getName()
            );
            final ProcessDocReport report = postProcessor.doRouteLevelChange(routeNodeChange);
            setRouteHeader(KEWServiceLocator.getRouteHeaderService().getRouteHeader(getDocumentId()));
            if (!report.isSuccess()) {
                LOG.warn("{}", report::getMessage, report::getProcessException);
                throw new InvalidActionTakenException(report.getMessage());
            }
        } catch (final Exception ex) {
            throw new WorkflowRuntimeException(ex.getMessage());
        }
    }

    private List<RouteNodeInstance> determineStartingNodes(final List path, final Collection<RouteNodeInstance> activeNodes) {
        final List<RouteNodeInstance> startingNodes = new ArrayList<>();
        for (final RouteNodeInstance activeNodeInstance : activeNodes) {
            if (isInPath(activeNodeInstance, path)) {
                startingNodes.add(activeNodeInstance);
            }
        }
        return startingNodes;
    }

    private boolean isInPath(final RouteNodeInstance nodeInstance, final List<RouteNodeInstance> path) {
        for (final RouteNodeInstance pathNodeInstance : path) {
            if (pathNodeInstance.getRouteNodeInstanceId().equals(nodeInstance.getRouteNodeInstanceId())) {
                return true;
            }
        }
        return false;
    }

    private RouteNodeInstance materializeReturnPoint(
            final Collection<RouteNodeInstance> startingNodes, final NodeGraphSearchResult result) {
        final RouteNodeService nodeService = KEWServiceLocator.getRouteNodeService();
        final RouteNodeInstance returnInstance = result.getResultNodeInstance();
        final RouteNodeInstance newNodeInstance =
                helper.getNodeFactory().createRouteNodeInstance(getDocumentId(), returnInstance.getRouteNode());
        newNodeInstance.setBranch(returnInstance.getBranch());
        newNodeInstance.setProcess(returnInstance.getProcess());
        newNodeInstance.setComplete(false);
        newNodeInstance.setActive(true);
        nodeService.save(newNodeInstance);
        for (final RouteNodeInstance activeNodeInstance : startingNodes) {
            // TODO what if the activeNodeInstance already has next nodes?
            activeNodeInstance.setComplete(true);
            activeNodeInstance.setActive(false);
            activeNodeInstance.setInitial(false);
            activeNodeInstance.addNextNodeInstance(newNodeInstance);
        }
        for (final RouteNodeInstance activeNodeInstance : startingNodes) {
            nodeService.save(activeNodeInstance);
        }
        // TODO really we need to call transitionTo on this node, how can we do that?
        // this isn't an issue yet because we only allow simple nodes and split nodes at the moment which do no real
        // work on transitionTo but we may need to enhance that in the future
        return newNodeInstance;
    }

    public boolean isSuperUserUsage() {
        return superUserUsage;
    }

    public void setSuperUserUsage(final boolean superUserUsage) {
        this.superUserUsage = superUserUsage;
    }

}
