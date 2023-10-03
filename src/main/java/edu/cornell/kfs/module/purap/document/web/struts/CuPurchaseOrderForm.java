package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;

import edu.cornell.kfs.sys.CUKFSConstants;

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
    public boolean shouldMethodToCallParameterBeUsed(
            final String methodToCallParameterName, 
            final String methodToCallParameterValue, final HttpServletRequest request) {
    	// KFSPTS-985
        if (methodToCallParameterName.contains("addFavoriteAccount")) {
            return true;
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName, methodToCallParameterValue, request);
    }

	@Override
	public boolean shouldPropertyBePopulatedInForm(
	        final String requestParameterName, final HttpServletRequest request) {
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
		return PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus()) && KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM);
		}
	
	protected boolean canVoidPoInCxmlErrorStatus() {
		return  PurchaseOrderStatuses.APPDOC_CXML_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus()) && KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM);
		}
	
	// ==== End CU Customization ====
	
	@Override
	protected Map<String, ExtraButton> createButtonsMap() {
		final Map<String, ExtraButton> result = super.createButtonsMap();
		
        // ==== CU Customization (KFSPTS-1457) ====
        
        // Reopen PO button
        final ExtraButton openCxerButton = new ExtraButton();
        openCxerButton.setExtraButtonProperty("methodToCall.openPoCxer");
        openCxerButton.setExtraButtonSource("${" + KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY + "}buttonsmall_openorder.gif");
        openCxerButton.setExtraButtonAltText("Open PO");

        // Void PO button
        final ExtraButton voidCxerButton = new ExtraButton();
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
		final Map buttonsMap = createButtonsMap();
				
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
	public void populate(final HttpServletRequest request) {
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
            final boolean pendingPrint = PurchaseOrderStatuses.APPDOC_PENDING_PRINT.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());
            final boolean open = PurchaseOrderStatuses.APPDOC_OPEN.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());
            final boolean errorFax = PurchaseOrderStatuses.APPDOC_FAX_ERROR.equals(getPurchaseOrderDocument().getApplicationDocumentStatus());

            final List<PaymentRequestView> preqViews = SpringContext.getBean(PurapService.class)
                    .getRelatedViews(PaymentRequestView.class, getPurchaseOrderDocument().getAccountsPayablePurchasingDocumentLinkIdentifier());
            boolean hasPaymentRequest = preqViews != null && preqViews.size() > 0;

            can = pendingPrint || open && !hasPaymentRequest || errorFax;
        }

        // check user authorization
        if (can) {
            final DocumentAuthorizer documentAuthorizer = SpringContext.getBean(DocumentHelperService.class).getDocumentAuthorizer(getPurchaseOrderDocument());
            can = documentAuthorizer.canInitiate(PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_VOID_DOCUMENT, GlobalVariables.getUserSession().getPerson());
        }

        return can;
    }
    // end KFSUPGRADE-411

    /**
     * Overridden to properly increment the local "itemCount" loop counter when processing POA documents,
     * but otherwise has the same code as in the superclass.
     * 
     * @see org.kuali.kfs.module.purap.document.web.struts.PurchasingAccountsPayableFormBase#populateItemAccountingLines(java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void populateItemAccountingLines(final Map parameterMap) {
        if (!(getDocument() instanceof PurchaseOrderAmendmentDocument)) {
            super.populateItemAccountingLines(parameterMap);
            return;
        }
        
        int itemCount = 0;
        for (final PurApItem item : ((PurchasingAccountsPayableDocument) getDocument()).getItems()) {
            populateAccountingLine(item.getNewSourceLine(), KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.ITEM + "[" + itemCount + "]."
                    + KFSPropertyConstants.NEW_SOURCE_LINE, parameterMap);

            int sourceLineCount = 0;
            for (final PurApAccountingLine purApLine : item.getSourceAccountingLines()) {
                populateAccountingLine(purApLine, KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.ITEM + "[" + itemCount + "]."
                        + KFSPropertyConstants.SOURCE_ACCOUNTING_LINE + "[" + sourceLineCount + "]", parameterMap);
                sourceLineCount += 1;
            }
            itemCount += 1;
        }
    }

    /**
     * Overridden to allow expired-account overrides to be repopulated on POA documents,
     * using logic derived from the KualiAccountingDocumentFormBase ancestor class.
     * 
     * @see org.kuali.kfs.module.purap.document.web.struts.PurchasingFormBase#repopulateOverrides(
     * org.kuali.kfs.sys.businessobject.AccountingLine, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void repopulateOverrides(
            final AccountingLine line, 
            final String accountingLinePropertyName, final Map parameterMap) {
        super.repopulateOverrides(line, accountingLinePropertyName, parameterMap);
        
        if (!(getPurchaseOrderDocument() instanceof PurchaseOrderAmendmentDocument)) {
            return;
        }
        
        if (line.getAccountExpiredOverrideNeeded()) {
            if (parameterMap.containsKey(accountingLinePropertyName + CUKFSConstants.ACCOUNT_EXPIRED_OVERRIDE_PRESENT_PARAMETER_SUFFIX)) {
                line.setAccountExpiredOverride(
                        parameterMap.containsKey(accountingLinePropertyName + CUKFSConstants.ACCOUNT_EXPIRED_OVERRIDE_PARAMETER_SUFFIX));
            }
        } else {
            line.setAccountExpiredOverride(false);
        }
    }

	public List<Note> getCopiedNotes() {
		return copiedNotes;
	}

	public void setCopiedNotes(final List<Note> copiedNotes) {
		this.copiedNotes = copiedNotes;
	}


}
