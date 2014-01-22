package edu.cornell.kfs.module.purap.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderAction;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;

public class CuPurchaseOrderAction extends PurchaseOrderAction {

	@Override
	public ActionForward save(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ActionForward forward =  super.save(mapping, form, request, response);
        //reindex the document to pick up the changes.
        PurchaseOrderDocument document = (PurchaseOrderDocument) ((PurchaseOrderForm)form).getDocument();
        this.reIndexDocument(document);
        return forward;
	}

    /**
     * This method is being added to handle calls to perform re-indexing of documents following change events performed on the documents.  This is necessary to correct problems
     * with searches not returning accurate results due to changes being made to documents, but those changes not be indexed.
     * 
     * @param document - The document to be re-indexed.
     */
    private void reIndexDocument(PurchaseOrderDocument document) {
        //force reindexing
//        DocumentRouteHeaderValue routeHeader = KEWServiceLocator.getRouteHeaderService().getRouteHeader(document.getDocumentNumber());
//		SearchableAttributeProcessingService searchableAttributeService = MessageServiceNames.getSearchableAttributeService(routeHeader);
//		searchableAttributeService.indexDocument(Long.valueOf(document.getDocumentNumber()));
        //RICE20 replaced searchableAttributeProcessingService.indexDocument with DocumentAttributeIndexingQueue.indexDocument
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();

        documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());

    }

}
