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
package org.kuali.kfs.kew.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.ActionRequestFactory;
import org.kuali.kfs.kew.actionrequest.KimPrincipalRecipient;
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

    BlanketApproveEngine(RouteNodeService routeNodeService, RouteHeaderService routeHeaderService,
            ParameterService parameterService, OrchestrationConfig config) {
        super(routeNodeService, routeHeaderService, parameterService, config);
    }

    /**
     * Orchestrates the document through the blanket approval process. The termination of the process is keyed off of
     * the Set of node names. If there are no node names, then the document will be blanket approved past the terminal
     * node(s) in the document.
     */
    @Override
    public void process(String documentId, String nodeInstanceId) throws Exception {
        if (documentId == null) {
            throw new IllegalArgumentException("Cannot process a null document id.");
        }
        ThreadContext.put("docId", documentId);

        try {
            RouteContext context = RouteContext.createNewRouteContext();
            if (config.isSupressRequestsNodePolicyErrors()) {
                RequestsNode.setSuppressPolicyErrors(RouteContext.getCurrentRouteContext());
            }

            KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
            LOG.info("Processing document for Blanket Approval: {} : {}", documentId, nodeInstanceId);
            DocumentRouteHeaderValue document = getRouteHeaderService().getRouteHeader(documentId, true);
            if (!document.isRoutable()) {
                //KULRICE-12283: Modified this message so it appears at a WARN level so we get better feedback if this
                //action is skipped
                LOG.warn("Document not routable so returning with doing no action");
                return;
            }
            List<RouteNodeInstance> activeNodeInstances = new ArrayList<>();
            if (nodeInstanceId == null) {
                activeNodeInstances.addAll(getRouteNodeService().getActiveNodeInstances(documentId));
            } else {
                RouteNodeInstance instanceNode = getRouteNodeService().findRouteNodeInstanceById(nodeInstanceId);
                if (instanceNode == null) {
                    throw new IllegalArgumentException("Invalid node instance id: " + nodeInstanceId);
                }
                activeNodeInstances.add(instanceNode);
            }
            List<RouteNodeInstance> nodeInstancesToProcess =
                    determineNodeInstancesToProcess(activeNodeInstances, config.getDestinationNodeNames());

            context.setDoNotSendApproveNotificationEmails(true);
            context.setDocument(document);
            context.setEngineState(new EngineState());
            NotificationContext notifyContext = null;
            if (config.isSendNotifications()) {
                // ==== CU Customization (KFSPTS-324, KFSUPGRADE-504, KFSPTS-24745)): Updated the line below to send FYIs instead of ACKs. ====
                notifyContext = new NotificationContext(
                        KewApiConstants.ACTION_REQUEST_FYI_REQ, config.getCause().getPrincipal(),
                        config.getCause().getActionTaken());
            }
            lockAdditionalDocuments(document);
            try {
                List<ProcessEntry> processingQueue = new LinkedList<>();
                for (RouteNodeInstance nodeInstancesToProcesses : nodeInstancesToProcess) {
                    processingQueue.add(new ProcessEntry(nodeInstancesToProcesses));
                }
                Set<String> nodesCompleted = new HashSet<>();
                // check the processingQueue for cases where there are no dest. nodes otherwise check if we've reached
                // the dest. nodes
                while (!processingQueue.isEmpty() &&
                        !isReachedDestinationNodes(config.getDestinationNodeNames(), nodesCompleted)) {
                    ProcessEntry entry = processingQueue.remove(0);
                    // TODO document magical join node workage (ask Eric)
                    // TODO this has been set arbitrarily high because the implemented processing model here will
                    // probably not work for large parallel object graphs. This needs to be re-evaluated, see KULWF-459.
                    if (entry.getTimesProcessed() > 20) {
                        throw new WorkflowException("Could not process document through to blanket approval." +
                                "  Document failed to progress past node " +
                                entry.getNodeInstance().getRouteNode().getRouteNodeName());
                    }
                    RouteNodeInstance nodeInstance = entry.getNodeInstance();
                    context.setNodeInstance(nodeInstance);
                    if (config.getDestinationNodeNames().contains(nodeInstance.getName())) {
                        nodesCompleted.add(nodeInstance.getName());
                        continue;
                    }
                    ProcessContext resultProcessContext = processNodeInstance(context, helper);
                    invokeBlanketApproval(config.getCause(), nodeInstance, notifyContext);
                    if (!resultProcessContext.getNextNodeInstances().isEmpty() || resultProcessContext.isComplete()) {
                        for (Iterator nodeIt = resultProcessContext.getNextNodeInstances().iterator(); nodeIt
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
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                } else {
                    throw new WorkflowRuntimeException(e.toString(), e);
                }
            }
        } finally {
            RouteContext.releaseCurrentRouteContext();
            ThreadContext.remove("docId");
        }
    }

    /**
     * @return true if all destination node are active but not yet complete - ready for the standard engine to take
     *         over the activation process for requests
     */
    private boolean isReachedDestinationNodes(Set destinationNodesNames, Set<String> nodeNamesCompleted) {
        return !destinationNodesNames.isEmpty() && nodeNamesCompleted.equals(destinationNodesNames);
    }

    private void addToProcessingQueue(List<ProcessEntry> processingQueue, RouteNodeInstance nodeInstance) {
        // first, detect if it's already there
        for (ProcessEntry entry : processingQueue) {
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
            List<RouteNodeInstance> activeNodeInstances, Set nodeNames) throws Exception {
        if (nodeNames.isEmpty()) {
            return activeNodeInstances;
        }
        List<RouteNodeInstance> nodeInstancesToProcess = new ArrayList<>();
        for (Iterator<RouteNodeInstance> iterator = activeNodeInstances.iterator(); iterator.hasNext(); ) {
            RouteNodeInstance nodeInstance = iterator.next();
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

    private boolean isNodeNameInPath(Set nodeNames, RouteNodeInstance nodeInstance) throws Exception {
        boolean isInPath = false;
        for (Object nodeName1 : nodeNames) {
            String nodeName = (String) nodeName1;
            for (RouteNode nextNode : nodeInstance.getRouteNode().getNextNodes()) {
                isInPath = isInPath || isNodeNameInPath(nodeName, nextNode, new HashSet<>());
            }
        }
        return isInPath;
    }

    private boolean isNodeNameInPath(String nodeName, RouteNode node, Set<String> inspected) {
        boolean isInPath = !inspected.contains(node.getRouteNodeId()) && node.getRouteNodeName().equals(nodeName);
        inspected.add(node.getRouteNodeId());
        for (RouteNode nextNode : node.getNextNodes()) {
            isInPath = isInPath || isNodeNameInPath(nodeName, nextNode, inspected);
        }
        return isInPath;
    }

    private String printNodeNames(Set nodesNames) {
        StringBuffer buffer = new StringBuffer();
        for (Iterator iterator = nodesNames.iterator(); iterator.hasNext(); ) {
            String nodeName = (String) iterator.next();
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
    private void invokeBlanketApproval(ActionTaken actionTaken, RouteNodeInstance nodeInstance,
            NotificationContext notifyContext) {
        List actionRequests = getActionRequestService()
                .findPendingRootRequestsByDocIdAtRouteNode(nodeInstance.getDocumentId(),
                        nodeInstance.getRouteNodeInstanceId());
        actionRequests = getActionRequestService().getRootRequests(actionRequests);
        List<ActionRequest> requestsToNotify = new ArrayList<>();
        for (Iterator iterator = actionRequests.iterator(); iterator.hasNext(); ) {
            ActionRequest request = (ActionRequest) iterator.next();
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
            ActionRequestFactory arFactory =
                    new ActionRequestFactory(RouteContext.getCurrentRouteContext().getDocument(), nodeInstance);
            KimPrincipalRecipient delegatorRecipient = null;
            if (actionTaken.getDelegatorPrincipal() != null) {
                delegatorRecipient = new KimPrincipalRecipient(actionTaken.getDelegatorPrincipal());
            }
            List<ActionRequest> notificationRequests = arFactory
                    .generateNotifications(requestsToNotify, notifyContext.getPrincipalTakingAction(),
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
        private int timesProcessed = 0;

        ProcessEntry(RouteNodeInstance nodeInstance) {
            this.nodeInstance = nodeInstance;
        }

        public RouteNodeInstance getNodeInstance() {
            return nodeInstance;
        }

        public void setNodeInstance(RouteNodeInstance nodeInstance) {
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
