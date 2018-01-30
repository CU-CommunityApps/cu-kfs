package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapConstants.PurchaseOrderStatuses;

public class CuPurchaseOrderForm extends PurchaseOrderForm {
	private static final String MOVE_CXML_ERROR_PO_PERM = "Move CXML Error PO"; // ==== CU Customization (KFSPTS-1457) ====
	// ==== CU Customization (KFSPTS-1457) ====
	
	// KFSPTS-794
	protected List<Note> copiedNotes;
	
	public CuPurchaseOrderForm() {
		super();
		copiedNotes = new ArrayList<Note>();
	}
 
    @Override
    public boolean shouldMethodToCallParameterBeUsed(String methodToCallParameterName, String methodToCallParameterValue, HttpServletRequest request) {
    	// KFSPTS-985
        if (methodToCallParameterName.contains("addFavoriteAccount")) {
            return true;
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }

	@Override
	public boolean shouldPropertyBePopulatedInForm(String requestParameterName,
			HttpServletRequest request) {
		// KFSPTS-985 : force it to populate
		if (requestParameterName.contains(".favoriteAccountLineIdentifier")) {
			return true;
		}
		return super.shouldPropertyBePopulatedInForm(requestParameterName, request);
	}

	@Override
	protected boolean canAmend() {
        //KFSUPGRADE-339
		//this should prevent B2B purchase orders from being amended
        if (getPurchaseOrderDocument().getHasB2BVendor()) {
        	return false;
        } 
        
		return super.canAmend();
	}
	
	protected boolean canOpenPoInCxmlErrorStatus() {
		return PurapConstants.PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus()) && KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM);
		}
	
	protected boolean canVoidPoInCxmlErrorStatus() {
		return  PurapConstants.PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus()) && KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM);
		}
	
	// ==== End CU Customization ====
	
	@Override
	protected Map<String, ExtraButton> createButtonsMap() {
		Map<String, ExtraButton> result = super.createButtonsMap();
		
        // ==== CU Customization (KFSPTS-1457) ====
        
        // Reopen PO button
        ExtraButton openCxerButton = new ExtraButton();
        openCxerButton.setExtraButtonProperty("methodToCall.openPoCxer");
        openCxerButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_openorder.gif");
        openCxerButton.setExtraButtonAltText("Open PO");

        // Void PO button
        ExtraButton voidCxerButton = new ExtraButton();
        voidCxerButton.setExtraButtonProperty("methodToCall.voidPoCxer");
        voidCxerButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_voidorder.gif");
        voidCxerButton.setExtraButtonAltText("Void PO");
        
        result.put(openCxerButton.getExtraButtonProperty(), openCxerButton);
        result.put(voidCxerButton.getExtraButtonProperty(), voidCxerButton);
        // ==== End CU Customization ====
        
		return result;
	}
	
	@Override
	public List<ExtraButton> getExtraButtons() {
		super.getExtraButtons();
		Map buttonsMap = createButtonsMap();
				
        // ==== CU Customization (KFSPTS-1457) ====
        
		if (!canContinuePoSplit()) {
			if (canOpenPoInCxmlErrorStatus()) {
				extraButtons.add((ExtraButton) buttonsMap.get("methodToCall.openPoCxer"));
			}

			if (canVoidPoInCxmlErrorStatus()) {
				extraButtons.add((ExtraButton) buttonsMap.get("methodToCall.voidPoCxer"));
			}
		}
        
        // ==== End CU Customization ====
        
        if (canContinuePoSplit()) {
            extraButtons.clear();
            extraButtons.add((ExtraButton) buttonsMap.get("methodToCall.continuePurchaseOrderSplit"));
            extraButtons.add((ExtraButton) buttonsMap.get("methodToCall.cancelPurchaseOrderSplit"));
        }
        
		return extraButtons;
	}
	
    /**
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase#populate(javax.servlet.http.HttpServletRequest)
     */
	@Override
	public void populate(HttpServletRequest request) {
		PurchaseOrderDocument po = (PurchaseOrderDocument) this.getDocument();

		super.populate(request);
		
		/*
		 * KFSPTS-794 : for PO & POA.  The notes will be refreshed from DB.  hence, the 'sendtovendor' flag will
		 * be lost. This is to save the notes from from document, and the restore it before doing comparison later in action.
		 */
		if (ObjectUtils.isNotNull(po.getPurapDocumentIdentifier())) {
			if (CollectionUtils.isNotEmpty(po.getNotes())) {
				for (Note note : (List<Note>)po.getNotes()) {
					copiedNotes.add((Note)ObjectUtils.deepCopy(note));
					//po.refreshDocumentBusinessObject();
			}
				for (Note note : (List<Note>) po.getNotes()) {
					note.refreshReferenceObject("attachment");
				}
				}       
			}
	}
	
	// KFSUPGRADE-411
    /**
     * Determines whether to display the void button for the purchase order document. Conditions:
     * PO is in Pending Print status, or is in Open status and has no PREQs against it;
     * PO's current indicator is true and pending indicator is false;
     * and the user is a member of the purchasing group).
     *
     * @return boolean true if the void button can be displayed.
     */
    protected boolean canVoid() {
        // check PO status etc
        boolean can = getPurchaseOrderDocument().isPurchaseOrderCurrentIndicator() && !getPurchaseOrderDocument().isPendingActionIndicator();

        if (can) {
            boolean pendingPrint = PurapConstants.PurchaseOrderStatuses.APPDOC_PENDING_PRINT.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());
            boolean open = PurapConstants.PurchaseOrderStatuses.APPDOC_OPEN.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());
            boolean errorFax = PurapConstants.PurchaseOrderStatuses.APPDOC_FAX_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());

            List<PaymentRequestView> preqViews = SpringContext.getBean(PurapService.class)
                    .getRelatedViews(PaymentRequestView.class, getPurchaseOrderDocument().getAccountsPayablePurchasingDocumentLinkIdentifier());
            boolean hasPaymentRequest = preqViews != null && preqViews.size() > 0;

            can = pendingPrint || (open && !hasPaymentRequest) || errorFax;
        }

        // check user authorization
        if (can) {
            DocumentAuthorizer documentAuthorizer = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(getPurchaseOrderDocument());
            can = documentAuthorizer.canInitiate(KFSConstants.FinancialDocumentTypeCodes.PURCHASE_ORDER_VOID, GlobalVariables.getUserSession().getPerson());
        }

        return can;
    }
    // end KFSUPGRADE-411

	public List<Note> getCopiedNotes() {
		return copiedNotes;
	}

	public void setCopiedNotes(List<Note> copiedNotes) {
		this.copiedNotes = copiedNotes;
	}


}
