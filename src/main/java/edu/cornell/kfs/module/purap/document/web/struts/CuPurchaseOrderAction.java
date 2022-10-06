package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderAction;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;


public class CuPurchaseOrderAction extends PurchaseOrderAction {
	// ==== CU Customization (KFSPTS-1457): Added some new constants and variables. ====
	private static final String MOVE_CXML_ERROR_PO_PERM = "Move CXML Error PO";
	private static final String MOVE_CXML_ERROR_PO_SUCCESS = "moveCxmlErrorPoSuccess";
	private static final String STATUS_OVERRIDE_QUESTION = "statusOverrideQuestion";
	
	  // ==== CU Customization (KFSPTS-1457): Added the ability for certain users to move "CXER"-status POs into "OPEN" or "VOID" status. ====
	   

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
     * This method is being added to handle calls to force re-indexing of documents following change events performed on the documents.
     * This is necessary to correct problems with searches not returning accurate results due to changes being made to documents, 
     * but those changes not be indexed.
     * 
     * @param document - The document to be re-indexed.
     */
    private void reIndexDocument(PurchaseOrderDocument document) {
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        documentAttributeIndexingQueue.indexDocument(document.getDocumentNumber());
    }
    
	public ActionForward openPoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return movePoCxer(mapping, form, request, response, PurchaseOrderStatuses.APPDOC_OPEN, "Open");
	}
	   
	public ActionForward voidPoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    return movePoCxer(mapping, form, request, response, PurchaseOrderStatuses.APPDOC_VOID, "Void");
	}
	   
	protected ActionForward movePoCxer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response,
	            String newStatus, String newStatusLabel) throws Exception {
		KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
	    PurchaseOrderDocument po = (PurchaseOrderDocument) kualiDocumentFormBase.getDocument();
	    Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
	       
	    // If the user has not received the question screen yet, then perform the PO status update.
	    if (ObjectUtils.isNull(question)) {
	    	
	    	// Check authorization.
	    	checkMovePoCxerAuthorization(po);
	    	
	    	// Use logic similar to the executeManualStatusChange() method to override the document's status.
	    	try {
	    		PurapService purapService = SpringContext.getBean(PurapService.class);
	    		po.updateAndSaveAppDocStatus(newStatus);
	    		purapService.saveDocumentNoValidation(po);
	    		}
	    	catch (Exception e) {
	    		throw new RuntimeException(e);
	    		}
	    	
	    	// Add a message to the route log.
	    	po.getDocumentHeader().getWorkflowDocument().logAnnotation("Moved PO document from 'Error occurred sending cxml' status to '" + newStatusLabel + "' status.");
	    	
	    	// Present a success message to the user.
	    	String message = "PO document " + po.getDocumentNumber() + " was successfully moved to '" + newStatusLabel + "' status.";
	    	
	    	return this.performQuestionWithoutInput(mapping, form, request, response, MOVE_CXML_ERROR_PO_SUCCESS, message, STATUS_OVERRIDE_QUESTION, MOVE_CXML_ERROR_PO_SUCCESS, "");
	    	}
	    
	    // If the user already went to the success "question" (which only had a "close" button), then simply perform the "returnToSender" logic.
	    
	    return returnToSender(request, mapping, kualiDocumentFormBase);
	    }
	// A helper method for checking if the user has authorization to move CXML Error POs and if the document is indeed in CXML error status.
	
	private void checkMovePoCxerAuthorization(PurchaseOrderDocument po) throws Exception {
		
		// Check if the user is authorized to open/void the PO.
		if (!KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM)) {
			throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(), "manuallyOverridePurchaseOrderStatus",
					po.getDocumentNumber(), "You are not authorized to manually move purchase order statuses on PO documents in 'Error occurred sending cxml' status.", null);
			}
		
		// Ensure that the document is currently in "CXER" status before trying to update it.
		if (!PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(po.getApplicationDocumentStatus())) {
			throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(), "manuallyOverridePurchaseOrderStatus",
					po.getDocumentNumber(), "You are not authorized to perform this action on PO documents not in 'Error occurred sending cxml' status.", null);
			}
		}
	   
	   // ==== End CU Customization ====

    /**
     * Overridden to perform the proper account-line-override setup
     * on POA items' account addLines, as well as on all of the items'
     * existing accounting lines instead of just the filtered list
     * from the document's getSourceAccountingLines() method override.
     * 
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#processAccountingLineOverrides(
     * org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase)
     */
    @Override
    protected void processAccountingLineOverrides(KualiAccountingDocumentFormBase transForm) {
        if (transForm.hasDocumentId() && transForm.getDocument() instanceof PurchaseOrderAmendmentDocument) {
            PurchaseOrderAmendmentDocument poaDocument = (PurchaseOrderAmendmentDocument) transForm.getDocument();
            processAccountingLineOverrides(transForm.getNewSourceLine());
            processAccountingLineOverrides(transForm.getNewTargetLine());
            processAccountingLineOverrides(poaDocument, getItemSourceAccountingLines(poaDocument));
            processAccountingLineOverrides(poaDocument, poaDocument.getTargetAccountingLines());
            /*
             * Using null document as first arg, for consistency with regular source/target addLine handling
             * in KualiAccountingDocumentActionBase.
             */
            processAccountingLineOverrides(null, getItemAccountingAddLines(poaDocument));
        } else {
            super.processAccountingLineOverrides(transForm);
        }
    }

    protected List<PurApAccountingLine> getItemSourceAccountingLines(PurchaseOrderAmendmentDocument document) {
        List<PurApItem> items = (List<PurApItem>) document.getItems();
        return items.stream()
                .flatMap((item) -> item.getSourceAccountingLines().stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected List<PurApAccountingLine> getItemAccountingAddLines(PurchaseOrderAmendmentDocument document) {
        List<PurApItem> items = (List<PurApItem>) document.getItems();
        return items.stream()
                .map(PurApItem::getNewSourceLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
