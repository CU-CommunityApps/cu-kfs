package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.web.ui.ExtraButton;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

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
		return PurchaseOrderStatuses.CXML_ERROR.equals(getPurchaseOrderDocument().getStatusCode()) && KimApiServiceLocator.getPermissionService().hasPermission(
				GlobalVariables.getUserSession().getPrincipalId(), PurapConstants.PURAP_NAMESPACE, MOVE_CXML_ERROR_PO_PERM);
		}
	
	protected boolean canVoidPoInCxmlErrorStatus() {
		return PurchaseOrderStatuses.CXML_ERROR.equals(getPurchaseOrderDocument().getStatusCode()) && KimApiServiceLocator.getPermissionService().hasPermission(
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
		
        if (canCreateReceiving() && getPurchaseOrderDocument().isNoQtyOrder()) {
        	// in PDForm, the 'canCreateReceiving' will add this button.  if 'isNoQtyOrder', then we don't need this, so remove it.
 //           extraButtons.remove((ExtraButton) buttonsMap.get("methodToCall.createReceivingLine"));
            ExtraButton recButton = null;
            for (ExtraButton extBtn : extraButtons) {
            	if (StringUtils.equals(extBtn.getExtraButtonAltText(),CUPurapConstants.RECEIVING_BUTTON_ALT_TEXT)) {
            		recButton = extBtn;
            		break;
            	}
            }
            if (recButton != null) {
            	extraButtons.remove(recButton);
            }
        }
		
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

	public List<Note> getCopiedNotes() {
		return copiedNotes;
	}

	public void setCopiedNotes(List<Note> copiedNotes) {
		this.copiedNotes = copiedNotes;
	}


}
