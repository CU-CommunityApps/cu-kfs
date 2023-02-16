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
package org.kuali.kfs.kew.documentoperation.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.document.DocumentProcessingQueue;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.web.KewKualiAction;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSKeyConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can be removed when we upgrade to the 2023-02-08 financials patch.
 * ============
 * 
 * Struts Action for queueing operations of workflow documents.
 */
public class DocumentQueueOperationAction extends KewKualiAction {

    private BusinessObjectService businessObjectService;

    public ActionForward getDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentQueueOperationForm docForm = (DocumentQueueOperationForm) form;
        String docId = null;

        // check if we have a plausible docId first
        if (StringUtils.isEmpty(docForm.getDocumentId())) {
            GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_REQUIRED, "Document ID");
        } else {
            try {
                docId = docForm.getDocumentId().trim();
            } catch (NumberFormatException nfe) {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_NUMERIC, "Document ID");
            }
        }

        if (docId != null) {
            DocumentRouteHeaderValue routeHeader = getRouteHeaderService().getRouteHeader(docId);
            if (routeHeader == null) {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_EXISTENCE, "document");
            } else {
                docForm.setRouteHeader(routeHeader);
                docForm.setDocumentId(docForm.getDocumentId().trim());
            }
        }

        return mapping.findForward("basic");
    }

    public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentQueueOperationForm docForm = (DocumentQueueOperationForm) form;
        docForm.setRouteHeader(new DocumentRouteHeaderValue());
        docForm.setDocumentId(null);
        return mapping.findForward("basic");
    }

    public ActionForward queueDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        try {
            DocumentQueueOperationForm docForm = (DocumentQueueOperationForm) form;
            final DocumentRouteHeaderValue routeHeader = docForm.getRouteHeader();
            if (routeHeader != null) {
                final String documentId = routeHeader.getDocumentId();
                if (StringUtils.isNotBlank(documentId)) {
                    DocumentProcessingQueue documentProcessingQueue =
                            KewApiServiceLocator.getDocumentProcessingQueue(documentId);
                    documentProcessingQueue.process(docForm.getDocumentId());
                    ActionMessages messages = new ActionMessages();
                    messages.add(ActionMessages.GLOBAL_MESSAGE,
                            new ActionMessage("general.message", "Document was successfully queued"));
                    saveMessages(request, messages);
                } else {
                    GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_REQUIRED,
                            "Document ID");
                }
            }
            return mapping.findForward("basic");
        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    public ActionForward indexSearchableAttributes(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentQueueOperationForm docForm = (DocumentQueueOperationForm) form;
        final DocumentRouteHeaderValue routeHeader = docForm.getRouteHeader();
        if (routeHeader != null) {
            final String documentId = routeHeader.getDocumentId();
            if (StringUtils.isNotBlank(documentId)) {
                DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
                queue.indexDocument(documentId);
                ActionMessages messages = new ActionMessages();
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("general.message",
                                "Searchable Attribute Indexing was successfully scheduled"));
                saveMessages(request, messages);
            } else {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_REQUIRED, "Document ID");
            }
        }
        return mapping.findForward("basic");
    }

    public ActionForward queueDocumentRefresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentQueueOperationForm docForm = (DocumentQueueOperationForm) form;
        final DocumentRouteHeaderValue routeHeader = docForm.getRouteHeader();
        if (routeHeader != null) {
            final String documentId = routeHeader.getDocumentId();
            if (StringUtils.isNotBlank(documentId)) {
                DocumentRefreshQueue docRequeue = KewApiServiceLocator
                        .getDocumentRequeuerService(documentId, 0L);
                docRequeue.refreshDocument(documentId, "Document was requeued from Document Queue Operation.");
                ActionMessages messages = new ActionMessages();
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("general.message", "Document Requeuer was successfully scheduled"));
                saveMessages(request, messages);
            } else {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_REQUIRED, "Document ID");
            }
        }
        return mapping.findForward("basic");
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    private RouteHeaderService getRouteHeaderService() {
        return KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
    }
}
