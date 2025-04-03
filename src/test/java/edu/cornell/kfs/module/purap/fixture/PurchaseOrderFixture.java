package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentIntegTestUtils;


public enum PurchaseOrderFixture {
	PO_B2B(RequisitionFixture.REQ_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_PENDING_CXML),
	PO_B2B_INVALID(RequisitionFixture.REQ_B2B_INVALID, 10, true, "Description", PurchaseOrderStatuses.APPDOC_PENDING_CXML),
	PO_B2B_CXML_VALIDATION(RequisitionFixture.REQ_B2B_CXML, 10, true, "Description", PurchaseOrderStatuses.APPDOC_PENDING_CXML),
	PO_B2B_CXML_VALIDATION_INVALID(RequisitionFixture.REQ_B2B_CXML_INVALID, 10, true, "Description", PurchaseOrderStatuses.APPDOC_PENDING_CXML),
	PO_NON_B2B_OPEN(RequisitionFixture.REQ_NON_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_ITEMS(RequisitionFixture.REQ_NON_B2B_WITH_ITEMS, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_IN_PROCESS(RequisitionFixture.REQ_NON_B2B, 10, true, "Description", PurchaseOrderStatuses.APPDOC_IN_PROCESS),
	PO_NON_B2B_OPEN_TRADE_IN_ITEMS(RequisitionFixture.REQ_NON_B2B_TRADE_IN_ITEMS, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_BELOW_5K(RequisitionFixture.REQ_NON_B2B_WITH_NON_QTY_ITEM_BELOW_5K,
			10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_AT_5K(RequisitionFixture.REQ_NON_B2B_WITH_NON_QTY_ITEM_AT_5K,
			10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_NON_QTY_ITEM_ABOVE_5K(RequisitionFixture.REQ_NON_B2B_WITH_NON_QTY_ITEM_ABOVE_5K,
			10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_QTY_ITEM_BELOW_5K(RequisitionFixture.REQ_NON_B2B_WITH_QTY_ITEM_BELOW_5K, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_QTY_ITEM_AT_5K(RequisitionFixture.REQ_NON_B2B_WITH_QTY_ITEM_AT_5K, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),
	PO_NON_B2B_OPEN_WITH_QTY_ITEM_ABOVE_5K(RequisitionFixture.REQ_NON_B2B_WITH_QTY_ITEM_ABOVE_5K, 10, true, "Description", PurchaseOrderStatuses.APPDOC_OPEN),;

	public final RequisitionFixture requisitionFixture;
	public final Integer contractManagerCode;
	public final boolean purchaseOrderCurrentIndicator;
	public final String documentDescription;
	public final String applicationDocumentStatus;

	private PurchaseOrderFixture(RequisitionFixture requisitionFixture,
			Integer contractManagerCode, boolean purchaseOrderCurrentIndicator,
			String documentDescription, String applicationDocumentStatus) {
		this.requisitionFixture = requisitionFixture;
		this.contractManagerCode = contractManagerCode;
		this.purchaseOrderCurrentIndicator = purchaseOrderCurrentIndicator;
		this.documentDescription = documentDescription;
		this.applicationDocumentStatus = applicationDocumentStatus;
	}

	public PurchaseOrderDocument createPurchaseOrderdDocument(boolean savedReq)throws WorkflowException {
		PurchaseOrderDocument purchaseOrderDocument = (PurchaseOrderDocument) SpringContext.getBean(DocumentService.class).getNewDocument(PurchaseOrderDocument.class);
		
		if(savedReq){
			purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionFixture.createRequisition(SpringContext.getBean(DocumentService.class)));
		}
		else{
			purchaseOrderDocument.populatePurchaseOrderFromRequisition(requisitionFixture.createRequisition());
		}
		purchaseOrderDocument.setContractManagerCode(this.contractManagerCode);
		purchaseOrderDocument.setPurchaseOrderCurrentIndicator(this.purchaseOrderCurrentIndicator);
		purchaseOrderDocument.getDocumentHeader().setDocumentDescription(this.documentDescription);
		purchaseOrderDocument.setApplicationDocumentStatus(applicationDocumentStatus);
		
		purchaseOrderDocument.refreshNonUpdateableReferences();

		return purchaseOrderDocument;
	}
	
	public PurchaseOrderDocument createPurchaseOrderdDocument(DocumentService documentService)throws WorkflowException {
		PurchaseOrderDocument purchaseOrderDocument = this.createPurchaseOrderdDocument(true);

		//purchaseOrderDocument.refreshNonUpdateableReferences();
		purchaseOrderDocument.prepareForSave();
		AccountingDocumentIntegTestUtils.saveDocument(purchaseOrderDocument, documentService);

		return purchaseOrderDocument;
	}

}
