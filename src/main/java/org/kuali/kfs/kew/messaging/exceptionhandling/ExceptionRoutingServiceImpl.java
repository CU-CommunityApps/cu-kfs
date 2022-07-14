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
package org.kuali.kfs.kew.messaging.exceptionhandling;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.ActionRequestFactory;
import org.kuali.kfs.kew.actionrequest.KimGroupRecipient;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.ActionRequestStatus;
import org.kuali.kfs.kew.api.exception.InvalidActionTakenException;
import org.kuali.kfs.kew.engine.RouteContext;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.exception.RouteManagerException;
import org.kuali.kfs.kew.exception.WorkflowDocumentExceptionRoutingService;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kew.framework.postprocessor.PostProcessor;
import org.kuali.kfs.kew.framework.postprocessor.ProcessDocReport;
import org.kuali.kfs.kew.role.RoleRouteModule;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.util.PerformanceLogger;
import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.kuali.kfs.sys.KFSConstants;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * ====
 * CU Customization:
 * Class added in support of the FINP-7647 backport from the 2021-09-30 release.
 * This overlay should be removed once we upgrade to the 2021-09-30 release or later.
 * ====
 */
public class ExceptionRoutingServiceImpl implements WorkflowDocumentExceptionRoutingService {

    private static final Logger LOG = LogManager.getLogger();

    public void placeInExceptionRouting(String errorMessage, PersistedMessage persistedMessage,
            String documentId) throws Exception {
        RouteNodeInstance nodeInstance = null;
        KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
        DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
        RouteContext routeContext = establishRouteContext(document, null);
        List<RouteNodeInstance> activeNodeInstances =
                KEWServiceLocator.getRouteNodeService().getActiveNodeInstances(documentId);
        if (!activeNodeInstances.isEmpty()) {
            // take the first active nodeInstance found.
            nodeInstance = activeNodeInstances.get(0);
        }
        placeInExceptionRouting(errorMessage, nodeInstance, persistedMessage, routeContext, document, true);
    }

    @Override
    public DocumentRouteHeaderValue placeInExceptionRouting(Throwable throwable, PersistedMessage persistedMessage,
            String documentId) throws Exception {
        return placeInExceptionRouting(throwable, persistedMessage, documentId, true);
    }

    protected DocumentRouteHeaderValue placeInExceptionRouting(Throwable throwable,
            PersistedMessage persistedMessage, String documentId, boolean invokePostProcessor) throws Exception {
        KEWServiceLocator.getRouteHeaderService().lockRouteHeader(documentId, true);
        DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
        throwable = unwrapRouteManagerExceptionIfPossible(throwable);
        RouteContext routeContext = establishRouteContext(document, throwable);
        RouteNodeInstance nodeInstance = routeContext.getNodeInstance();
        Throwable cause = determineActualCause(throwable, 0);
        String errorMessage = cause != null && cause.getMessage() != null ? cause.getMessage() : "";
        return placeInExceptionRouting(errorMessage, nodeInstance, persistedMessage, routeContext, document,
                invokePostProcessor);
    }

    protected DocumentRouteHeaderValue placeInExceptionRouting(String errorMessage, RouteNodeInstance nodeInstance,
            PersistedMessage persistedMessage, RouteContext routeContext, DocumentRouteHeaderValue document,
            boolean invokePostProcessor) throws Exception {
        String documentId = document.getDocumentId();
        ThreadContext.put("docId", documentId);
        PerformanceLogger performanceLogger = new PerformanceLogger(documentId);
        try {
            // mark all active requests to initialized and delete the action items
            List<ActionRequest> actionRequests =
                    KEWServiceLocator.getActionRequestService().findPendingByDoc(documentId);
            for (ActionRequest actionRequest : actionRequests) {
                if (actionRequest.isActive()) {
                    actionRequest.setStatus(ActionRequestStatus.INITIALIZED.getCode());
                    for (ActionItem actionItem : actionRequest.getActionItems()) {
                        KEWServiceLocator.getActionListService().deleteActionItem(actionItem);
                    }
                    KEWServiceLocator.getActionRequestService().saveActionRequest(actionRequest);
                }
            }

            LOG.debug("Generating exception request for doc : " + documentId);
            if (errorMessage == null) {
                errorMessage = "";
            }
            if (errorMessage.length() > KewApiConstants.MAX_ANNOTATION_LENGTH) {
                errorMessage = errorMessage.substring(0, KewApiConstants.MAX_ANNOTATION_LENGTH);
            }
            List<ActionRequest> exceptionRequests;
            if (nodeInstance.getRouteNode().isExceptionGroupDefined()) {
                exceptionRequests = generateExceptionGroupRequests(routeContext);
            } else {
                exceptionRequests = generateKimExceptionRequests(routeContext);
            }
            if (exceptionRequests.isEmpty()) {
                LOG.warn("Failed to generate exception requests for exception routing!");
            }
            activateExceptionRequests(routeContext, exceptionRequests, errorMessage, invokePostProcessor);

            if (persistedMessage == null) {
                LOG.warn("Attempting to delete null persisted message.");
            } else {
                KSBServiceLocator.getMessageQueueService().delete(persistedMessage);
            }
        } finally {
            performanceLogger.log("Time to generate exception request.");
            ThreadContext.remove("docId");
        }

        return document;
    }

    @Override
    public void placeInExceptionRoutingLastDitchEffort(Throwable throwable, PersistedMessage persistedMessage,
            String documentId) throws Exception {
        placeInExceptionRouting(throwable, persistedMessage, documentId, false);
    }

    protected void notifyStatusChange(DocumentRouteHeaderValue routeHeader, String oldStatusCode)
            throws InvalidActionTakenException {
        DocumentRouteStatusChange statusChangeEvent =
                new DocumentRouteStatusChange(routeHeader.getDocumentId(), routeHeader.getAppDocId(), oldStatusCode,
                        KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        try {
            LOG.debug("Notifying post processor of status change " + oldStatusCode + "->" +
                    KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
            PostProcessor postProcessor = routeHeader.getDocumentType().getPostProcessor();
            ProcessDocReport report = postProcessor.doRouteStatusChange(statusChangeEvent);
            if (!report.isSuccess()) {
                LOG.warn(report.getMessage(), report.getProcessException());
                throw new InvalidActionTakenException(report.getMessage());
            }
        } catch (Exception ex) {
            LOG.warn(ex, ex);
            throw new WorkflowRuntimeException(ex);
        }
    }

    protected List<ActionRequest> generateExceptionGroupRequests(RouteContext routeContext) {
        RouteNodeInstance nodeInstance = routeContext.getNodeInstance();
        ActionRequestFactory arFactory = new ActionRequestFactory(routeContext.getDocument(), null);
        ActionRequest exceptionRequest = arFactory
                .createActionRequest(KewApiConstants.ACTION_REQUEST_COMPLETE_REQ, 0,
                        new KimGroupRecipient(nodeInstance.getRouteNode().getExceptionWorkgroup()),
                        "Exception Workgroup for route node " + nodeInstance.getName(),
                        KewApiConstants.EXCEPTION_REQUEST_RESPONSIBILITY_ID, Boolean.TRUE, "");
        return Collections.singletonList(exceptionRequest);
    }

    protected List<ActionRequest> generateKimExceptionRequests(RouteContext routeContext) throws Exception {
        RoleRouteModule roleRouteModule = new RoleRouteModule();
        roleRouteModule.setNamespace(KFSConstants.CoreModuleNamespaces.WORKFLOW);
        roleRouteModule.setResponsibilityTemplateName(KewApiConstants.EXCEPTION_ROUTING_RESPONSIBILITY_TEMPLATE_NAME);
        List<ActionRequest> requests = roleRouteModule.findActionRequests(routeContext);
        // let's ensure we are only dealing with root requests
        requests = KEWServiceLocator.getActionRequestService().getRootRequests(requests);
        processExceptionRequests(requests);
        return requests;
    }

    /**
     * Takes the given list of Action Requests and ensures their attributes are set properly for exception
     * routing requests.  Namely, this ensures that all "force action" values are set to "true".
     */
    protected void processExceptionRequests(List<ActionRequest> exceptionRequests) {
        if (exceptionRequests != null) {
            for (ActionRequest actionRequest : exceptionRequests) {
                processExceptionRequest(actionRequest);
            }
        }
    }

    /**
     * Processes a single exception request, ensuring that it's force action flag is set to true and it's node instance is set to null.
     * It then recurses through any children requests.
     */
    protected void processExceptionRequest(ActionRequest actionRequest) {
        actionRequest.setForceAction(true);
        actionRequest.setNodeInstance(null);
        processExceptionRequests(actionRequest.getChildrenRequests());
    }

    /**
     * End IU Customization
     *
     * @param routeContext
     * @param exceptionRequests
     * @param exceptionMessage
     * @throws Exception
     */

    protected void activateExceptionRequests(
            RouteContext routeContext, List<ActionRequest> exceptionRequests, String exceptionMessage,
            boolean invokePostProcessor) throws
            Exception {
        setExceptionAnnotations(exceptionRequests, exceptionMessage);
        // TODO is there a reason we reload the document here?
        DocumentRouteHeaderValue rh =
                KEWServiceLocator.getRouteHeaderService().getRouteHeader(routeContext.getDocument().getDocumentId());
        String oldStatus = rh.getDocRouteStatus();
        rh.setDocRouteStatus(KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        if (invokePostProcessor) {
            notifyStatusChange(rh, oldStatus);
        }
        KEWServiceLocator.getRouteHeaderService().saveRouteHeader(rh);
        KEWServiceLocator.getActionRequestService().activateRequests(exceptionRequests);
    }

    /**
     * Sets the exception message as the annotation on the top-level Action Requests
     */
    protected void setExceptionAnnotations(List<ActionRequest> actionRequests, String exceptionMessage) {
        for (ActionRequest actionRequest : actionRequests) {
            actionRequest.setAnnotation(exceptionMessage);
        }
    }

    private Throwable unwrapRouteManagerExceptionIfPossible(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            throwable = throwable.getCause();
        }
        if (throwable != null && !(throwable instanceof RouteManagerException) &&
            throwable.getCause() instanceof RouteManagerException) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    protected Throwable determineActualCause(Throwable throwable, int depth) {
        if (depth >= 10) {
            return throwable;
        }
        if (throwable instanceof InvocationTargetException || throwable instanceof RouteManagerException) {
            if (throwable.getCause() != null) {
                return determineActualCause(throwable.getCause(), ++depth);
            }
        }
        return throwable;
    }

    protected RouteContext establishRouteContext(DocumentRouteHeaderValue document, Throwable throwable) {
        RouteContext routeContext = new RouteContext();
        if (throwable instanceof RouteManagerException) {
            RouteManagerException rmException = (RouteManagerException) throwable;
            routeContext = rmException.getRouteContext();
        } else {
            routeContext.setDocument(document);
            List<RouteNodeInstance> activeNodeInstances =
                    KEWServiceLocator.getRouteNodeService().getActiveNodeInstances(document.getDocumentId());
            if (!activeNodeInstances.isEmpty()) {
                // take the first active nodeInstance found.
                RouteNodeInstance nodeInstance = activeNodeInstances.get(0);
                routeContext.setNodeInstance(nodeInstance);
            }
        }
        if (routeContext.getNodeInstance() == null) {
            // get the initial node instance
            routeContext.setNodeInstance(document.getInitialRouteNodeInstances().get(0));
        }
        return routeContext;
    }
}