package org.kuali.kfs.kew.impl.document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.ActionRequestFactory;
import org.kuali.kfs.kew.actionrequest.KimPrincipalRecipient;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.document.DocumentProcessingOptions;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.kew.engine.OrchestrationConfig;
import org.kuali.kfs.kew.engine.WorkflowEngine;
import org.kuali.kfs.kew.engine.WorkflowEngineFactory;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.krad.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class DocumentProcessingHelper {
    private static final Logger LOG = LogManager.getLogger();
    private static final String COMPLETE_ACTION_REQUEST_DESCRIPTION = "Initiator needs to complete document.";

    DocumentProcessingHelper() {
    }

    OrchestrationConfig configFor(DocumentProcessingOptions options) {
        return new OrchestrationConfig(OrchestrationConfig.EngineCapability.STANDARD, Collections.emptySet(), (ActionTaken)null, options.isSendNotifications(), options.isRunPostProcessor());
    }

    void processDocument(String documentId, OrchestrationConfig config, WorkflowEngineFactory workflowEngineFactory) {
        WorkflowEngine engine = workflowEngineFactory.newEngine(config);

        try {
            engine.process(documentId, (String)null);
        } catch (Exception var6) {
            LOG.error("Failed to process document through the workflow engine", var6);
            if (var6 instanceof RuntimeException) {
                throw (RuntimeException)var6;
            } else {
                throw new WorkflowRuntimeException(var6);
            }
        }
    }

    void queueForIndexing(String documentId, DocumentProcessingOptions options, DocumentAttributeIndexingQueue documentAttributeIndexingQueue) {
        if (options.isIndexSearchAttributes()) {
            documentAttributeIndexingQueue.indexDocument(documentId);
        }

    }

    boolean handleNotYetRoutedDocument(String documentId, ActionRequestService actionRequestService, RouteHeaderService routeHeaderService, RouteNodeService routeNodeService) {
        DocumentRouteHeaderValue routeHeader = routeHeaderService.getRouteHeader(documentId);
        if (ObjectUtils.isNull(routeHeader)) { // CU Customization to avoid NullPointerException (See KFSPTS-29330 and linked Jira)
            return false;
        } else if (routeHeader.isRouted()) {
            return true;
        } else {
            Optional<ActionRequest> existingCompleteRequest = actionRequestService.findPendingByDoc(documentId).stream().filter(ActionRequest::isCompleteRequest).findFirst();
            if (existingCompleteRequest.isPresent()) {
                return false;
            } else {
                RouteNodeInstance initialNode = (RouteNodeInstance)routeNodeService.getInitialNodeInstances(documentId).stream().findFirst().orElse((Object)null);
                ActionRequestFactory actionRequestFactory = new ActionRequestFactory(routeHeader, initialNode);
                ActionRequest actionRequest = actionRequestFactory.createActionRequest("C", 0, new KimPrincipalRecipient(routeHeader.getInitiatorPrincipal()), "Initiator needs to complete document.", "-3", Boolean.TRUE, "");
                actionRequestService.activateRequest(actionRequest);
                return false;
            }
        }
    }
}

