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
package org.kuali.kfs.kew.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.api.document.DocumentProcessingOptions;
import org.kuali.kfs.kew.api.document.DocumentProcessingQueue;
import org.kuali.kfs.kew.engine.OrchestrationConfig;
import org.kuali.kfs.kew.engine.WorkflowEngineFactory;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.ksb.util.KSBConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * ====
 * CU Customization: Added a separate method for handling the FINP-8541 functionality (which checks whether
 * a document has been routed and, if not, will add a Complete request if one doesn't exist). This is needed
 * to fix a DB transaction issue, where merely retrieving the route header can cause it to remain stale
 * when subsequently invoking the workflow engine.
 * ====
 * 
 * Reference implementation of the {@code DocumentProcessingQueue}.
 */
public class DocumentProcessingQueueImpl implements DocumentProcessingQueue {

    private WorkflowEngineFactory workflowEngineFactory;
    private ActionRequestService actionRequestService;
    private RouteHeaderService routeHeaderService;
    private RouteNodeService routeNodeService;

    private final DocumentProcessingHelper helper;

    public DocumentProcessingQueueImpl() {
        this(new DocumentProcessingHelper());
    }

    DocumentProcessingQueueImpl(final DocumentProcessingHelper helper) {
        this.helper = helper;
    }

    @Override
    public void process(final String documentId) {
        processWithOptions(documentId, null);
    }

    @Override
    public void processWithOptions(final String documentId, DocumentProcessingOptions options) {
        if (StringUtils.isBlank(documentId)) {
            throw new IllegalArgumentException("documentId was a null or blank value");
        }
        if (options == null) {
            options = DocumentProcessingOptions.createDefault();
        }
        /*
         * CU Customization: Moved the handleNotYetRoutedDocument() check to a different method,
         * and added invocation of it via the bean version of this service for transactional reasons.
         * (However, the new method will instead be invoked directly when async services are actually
         * running in synchronous mode, since such a setup causes the document's action/operation and
         * workflow engine processing to occur in the same transaction.)
         */
        final boolean isRouted;
        if (shouldUseSeparateTransactionToCheckIfDocumentProcessingShouldProceed()) {
            isRouted = getDocumentProcessingQueueAsBean().shouldProceedWithDocumentProcessing(documentId);
        } else {
            isRouted = shouldProceedWithDocumentProcessing(documentId);
        }

        if (isRouted) {
            final OrchestrationConfig config = helper.configFor(options);
            helper.processDocument(documentId, config, workflowEngineFactory);
        }
        helper.queueForIndexing(documentId, options);
    }

    /*
     * ====
     * CU Customization: Added several helper methods for implementing the ability to perform
     * the handleNotYetRoutedDocument() logic in a separate transaction.
     * ====
     */

    private boolean shouldUseSeparateTransactionToCheckIfDocumentProcessingShouldProceed() {
        return !StringUtils.equalsIgnoreCase(getMessageDeliverySetting(), KSBConstants.MESSAGING_SYNCHRONOUS);
    }

    private DocumentProcessingQueue getDocumentProcessingQueueAsBean() {
        return SpringContext.getBean(DocumentProcessingQueue.class);
    }

    /*
     * NOTE: We should not inject "message.delivery" as a bean property placeholder nor read it
     *       from the ConfigurationService. Newer financials versions adjusted the property
     *       resolution process in a way that no longer respects certain programmatic ConfigContext
     *       property changes, especially the override of "message.delivery" by integration tests.
     *       That's why this code still needs to read it from the current ConfigContext for now.
     */
    private String getMessageDeliverySetting() {
        return ConfigContext.getCurrentContextConfig().getProperty(KSBConstants.Config.MESSAGE_DELIVERY);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public boolean shouldProceedWithDocumentProcessing(String documentId) {
        return helper.handleNotYetRoutedDocument(
                documentId,
                actionRequestService,
                routeHeaderService,
                routeNodeService);
    }

    /*
     * ====
     * End CU Customization Block
     * ====
     */

    public void setWorkflowEngineFactory(final WorkflowEngineFactory workflowEngineFactory) {
        this.workflowEngineFactory = workflowEngineFactory;
    }

    public void setActionRequestService(final ActionRequestService actionRequestService) {
        this.actionRequestService = actionRequestService;
    }

    public void setRouteHeaderService(final RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    }

    public void setRouteNodeService(final RouteNodeService routeNodeService) {
        this.routeNodeService = routeNodeService;
    }

}
