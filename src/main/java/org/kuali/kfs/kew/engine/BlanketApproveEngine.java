/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kew.engine;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.ActionRequestFactory;
import org.kuali.kfs.kew.actionrequest.PersonRecipient;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actions.NotificationContext;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.exception.InvalidActionTakenException;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.engine.node.RequestsNode;
import org.kuali.kfs.kew.engine.node.RouteNode;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * ====
 * CU Customization (KFSPTS-324, KFSUPGRADE-504, KFSPTS-24745):
 * Updated blanket approval engine to send FYI requests instead of ACK requests.
 * ====
 */

/**
 * A WorkflowEngine implementation which orchestrates the document through the blanket approval process.
 */
public class BlanketApproveEngine extends StandardWorkflowEngine {

    private static final Logger LOG = LogManager.getLogger();

    BlanketApproveEngine(
            final RouteNodeService routeNodeService, final RouteHeaderService routeHeaderService,
            final ParameterService parameterService, final OrchestrationConfig config) {
        super(routeNodeService, routeHeaderService, parameterService, config);
    }

    /**
     * Orchestrates the document through the blanket approval process. The termination of the process is keyed off of
     * the Set of node names. If there are no node names, then the document will be blanket approved past the terminal
     * node(s) in the document.
     */
    @Override
    public void process(final String documentId, final String nodeInstanceId) throws Exception {
        if (documentId == null) {
            throw new IllegalArgumentException("Cannot process a null document id.");
        }

        try (CloseableThreadContext.Instance ignored =
                     CloseableThreadContext.put(KewApiConstants.DOCUMENT_ID_PARAMETER, documentId)
        ) {
            final RouteContext context = RouteContext.createNewRouteContext();
            if (config.isSupressRequestsNodePolicyErrors()) {
                RequestsNode.setSuppressPolicyErrors(RouteContext.getCurrentRouteContext());
            }

            KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
            LOG.info("Processing document for Blanket Approval: {} : {}", documentId, nodeInstanceId);
            final DocumentRouteHeaderValue document = getRouteHeaderService().getRouteHeader(documentId, true);
            if (!document.isRoutable()) {
                //KULRICE-12283: Modified this message so it appears at a WARN level so we get better feedback if this
                //action is skipped
                LOG.warn("Document not routable so returning with doing no action");
                return;
            }
            final List<RouteNodeInstance> activeNodeInstances = new ArrayList<>();
            if (nodeInstanceId == null) {
                activeNodeInstances.addAll(getRouteNodeService().getActiveNodeInstances(documentId));
            } else {
                final RouteNodeInstance instanceNode = getRouteNodeService().findRouteNodeInstanceById(nodeInstanceId);
                if (instanceNode == null) {
                    throw new IllegalArgumentException("Invalid node instance id: " + nodeInstanceId);
                }
                activeNodeInstances.add(instanceNode);
            }
            final List<RouteNodeInstance> nodeInstancesToProcess =
                    determineNodeInstancesToProcess(activeNodeInstances, config.getDestinationNodeNames());

            context.setDoNotSendApproveNotificationEmails(true);
            context.setDocument(document);
            context.setEngineState(new EngineState());
            NotificationContext notifyContext = null;
            if (config.isSendNotifications()) {
                // ==== CU Customization (KFSPTS-324, KFSUPGRADE-504, KFSPTS-24745)): Updated the line below to send FYIs instead of ACKs. ====
                notifyContext = new NotificationContext(
                        KewApiConstants.ACTION_REQUEST_FYI_REQ, config.getCause().getPerson(),
                        config.getCause().getActionTaken());
            }
            lockAdditionalDocuments(document);
            try {
                final List<ProcessEntry> processingQueue = new LinkedList<>();
                for (final RouteNodeInstance nodeInstancesToProcesses : nodeInstancesToProcess) {
                    processingQueue.add(new ProcessEntry(nodeInstancesToProcesses));
                }
                final Set<String> nodesCompleted = new HashSet<>();
                // check the processingQueue for cases where there are no dest. nodes otherwise check if we've reached
                // the dest. nodes
                while (!processingQueue.isEmpty() &&
                        !isReachedDestinationNodes(config.getDestinationNodeNames(), nodesCompleted)) {
                    final ProcessEntry entry = processingQueue.remove(0);
                    // TODO document magical join node workage (ask Eric)
                    // TODO this has been set arbitrarily high because the implemented processing model here will
                    // probably not work for large parallel object graphs. This needs to be re-evaluated, see KULWF-459.
                    if (entry.getTimesProcessed() > 20) {
                        throw new WorkflowException("Could not process document through to blanket approval." +
                                "  Document failed to progress past node " +
                                entry.getNodeInstance().getRouteNode().getRouteNodeName());
                    }
                    final RouteNodeInstance nodeInstance = entry.getNodeInstance();
                    context.setNodeInstance(nodeInstance);
                    if (config.getDestinationNodeNames().contains(nodeInstance.getName())) {
                        nodesCompleted.add(nodeInstance.getName());
                        continue;
                    }
                    final ProcessContext resultProcessContext = processNodeInstance(context, helper);
                    invokeBlanketApproval(config.getCause(), nodeInstance, notifyContext);
                    if (!resultProcessContext.getNextNodeInstances().isEmpty() || resultProcessContext.isComplete()) {
                        for (final Iterator nodeIt = resultProcessContext.getNextNodeInstances().iterator(); nodeIt
                                .hasNext(); ) {
                            addToProcessingQueue(processingQueue, (RouteNodeInstance) nodeIt.next());
                        }
                    } else {
                        entry.increment();
                        processingQueue.add(processingQueue.size(), entry);
                    }
                }
                //clear the context so the standard engine can begin routing normally
                RouteContext.clearCurrentRouteContext();
                // continue with normal routing after blanket approve brings us to the correct place
                // if there is an active approve request this is no-op.
                super.process(documentId, null);
            } catch (final Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                } else {
                    throw new WorkflowRuntimeException(e.toString(), e);
                }
            }
        } finally {
            RouteContext.releaseCurrentRouteContext();
        }
    }

    /**
     * @return true if all destination node are active but not yet complete - ready for the standard engine to take
     *         over the activation process for requests
     */
    private boolean isReachedDestinationNodes(final Set destinationNodesNames, final Set<String> nodeNamesCompleted) {
        return !destinationNodesNames.isEmpty() && nodeNamesCompleted.equals(destinationNodesNames);
    }

    private void addToProcessingQueue(final List<ProcessEntry> processingQueue, final RouteNodeInstance nodeInstance) {
        // first, detect if it's already there
        for (final ProcessEntry entry : processingQueue) {
            if (entry.getNodeInstance().getRouteNodeInstanceId().equals(nodeInstance.getRouteNodeInstanceId())) {
                entry.setNodeInstance(nodeInstance);
                return;
            }
        }
        processingQueue.add(processingQueue.size(), new ProcessEntry(nodeInstance));
    }

    /**
     * If there are multiple paths, we need to figure out which ones we need to follow for blanket approval. This
     * method will throw an exception if a node with the given name could not be located in the routing path. This
     * method is written in such a way that it should be impossible for there to be an infinite loop, even if there
     * is extensive looping in the node graph.
     */
    private List<RouteNodeInstance> determineNodeInstancesToProcess(
            final List<RouteNodeInstance> activeNodeInstances, final Set nodeNames) throws Exception {
        if (nodeNames.isEmpty()) {
            return activeNodeInstances;
        }
        final List<RouteNodeInstance> nodeInstancesToProcess = new ArrayList<>();
        for (final Iterator<RouteNodeInstance> iterator = activeNodeInstances.iterator(); iterator.hasNext(); ) {
            final RouteNodeInstance nodeInstance = iterator.next();
            if (isNodeNameInPath(nodeNames, nodeInstance)) {
                nodeInstancesToProcess.add(nodeInstance);
            }
        }
        if (nodeInstancesToProcess.size() == 0) {
            throw new InvalidActionTakenException(
                    "Could not locate nodes with the given names in the blanket approval path '" +
                            printNodeNames(nodeNames) + "'.  " +
                            "The document is probably already passed the specified nodes or does not contain the nodes.");
        }
        return nodeInstancesToProcess;
    }

    private boolean isNodeNameInPath(final Set nodeNames, final RouteNodeInstance nodeInstance) {
        boolean isInPath = false;
        for (final Object nodeName1 : nodeNames) {
            final String nodeName = (String) nodeName1;
            for (final RouteNode nextNode : nodeInstance.getRouteNode().getNextNodes()) {
                isInPath = isInPath || isNodeNameInPath(nodeName, nextNode, new HashSet<>());
            }
        }
        return isInPath;
    }

    private boolean isNodeNameInPath(final String nodeName, final RouteNode node, final Set<String> inspected) {
        boolean isInPath = !inspected.contains(node.getRouteNodeId()) && node.getRouteNodeName().equals(nodeName);
        inspected.add(node.getRouteNodeId());
        for (final RouteNode nextNode : node.getNextNodes()) {
            isInPath = isInPath || isNodeNameInPath(nodeName, nextNode, inspected);
        }
        return isInPath;
    }

    private String printNodeNames(final Set nodesNames) {
        final StringBuffer buffer = new StringBuffer();
        for (final Iterator iterator = nodesNames.iterator(); iterator.hasNext(); ) {
            final String nodeName = (String) iterator.next();
            buffer.append(nodeName);
            buffer.append(iterator.hasNext() ? ", " : "");
        }
        return buffer.toString();
    }

    /**
     * Invokes the blanket approval for the given node instance. This deactivates all pending approve or complete
     * requests at the node and sends out notifications to the individuals who's requests were trumped by the blanket
     * approve.
     */
    private void invokeBlanketApproval(
            final ActionTaken actionTaken, final RouteNodeInstance nodeInstance,
            final NotificationContext notifyContext) {
        List actionRequests = getActionRequestService()
                .findPendingRootRequestsByDocIdAtRouteNode(nodeInstance.getDocumentId(),
                        nodeInstance.getRouteNodeInstanceId());
        actionRequests = getActionRequestService().getRootRequests(actionRequests);
        final List<ActionRequest> requestsToNotify = new ArrayList<>();
        for (final Iterator iterator = actionRequests.iterator(); iterator.hasNext(); ) {
            final ActionRequest request = (ActionRequest) iterator.next();
            if (request.isApproveOrCompleteRequest()) {
                getActionRequestService().deactivateRequest(actionTaken, request);
                requestsToNotify.add(request);
            }
            //KULRICE-12283: Added logic to deactivate acks or FYIs if a config option is provided. This will mainly
            //be used when a document is moved.
            if (request.isAcknowledgeRequest() && config.isDeactivateAcknowledgements()) {
                getActionRequestService().deactivateRequest(actionTaken, request);
            }
            if (request.isFYIRequest() && config.isDeactivateFYIs()) {
                getActionRequestService().deactivateRequest(actionTaken, request);
            }
        }
        if (notifyContext != null && !requestsToNotify.isEmpty()) {
            final ActionRequestFactory arFactory =
                    new ActionRequestFactory(RouteContext.getCurrentRouteContext().getDocument(), nodeInstance);
            PersonRecipient delegatorRecipient = null;
            if (actionTaken.getDelegatorPerson() != null) {
                delegatorRecipient = new PersonRecipient(actionTaken.getDelegatorPerson());
            }
            final List<ActionRequest> notificationRequests = arFactory
                    .generateNotifications(requestsToNotify, notifyContext.getPersonTakingAction(),
                            delegatorRecipient, notifyContext.getNotificationRequestCode(),
                            notifyContext.getActionTakenCode());
            getActionRequestService().activateRequests(notificationRequests);
        }
    }

    private ActionRequestService getActionRequestService() {
        return KEWServiceLocator.getActionRequestService();
    }

    private static class ProcessEntry {

        private RouteNodeInstance nodeInstance;
        private int timesProcessed;

        ProcessEntry(final RouteNodeInstance nodeInstance) {
            this.nodeInstance = nodeInstance;
        }

        public RouteNodeInstance getNodeInstance() {
            return nodeInstance;
        }

        public void setNodeInstance(final RouteNodeInstance nodeInstance) {
            this.nodeInstance = nodeInstance;
        }

        public void increment() {
            timesProcessed++;
        }

        public int getTimesProcessed() {
            return timesProcessed;
        }
    }
}
