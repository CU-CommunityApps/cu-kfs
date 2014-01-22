package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.document.web.struts.PurchaseOrderForm;
import org.kuali.rice.kns.web.ui.ExtraButton;

import edu.cornell.kfs.module.purap.CUPurapConstants;

public class CuPurchaseOrderForm extends PurchaseOrderForm {

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
        
	
    	return extraButtons;
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

}
