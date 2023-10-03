package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.List;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants.CreditMemoEditMode;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants.PaymentRequestEditMode;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.module.purap.document.service.PurapService;
import org.kuali.kfs.module.purap.document.web.struts.VendorCreditMemoForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;

public class CuVendorCreditMemoForm extends VendorCreditMemoForm {
    // KFSPTS-1891.  TODO : move this up to share with preqform
    protected String wireChargeMessage;
    
    @Override
    public List<ExtraButton> getExtraButtons() {
        final List<ExtraButton> extraButtons = super.getExtraButtons();
        final VendorCreditMemoDocument cmDocument = (VendorCreditMemoDocument) getDocument();
        final String appExternalImageURL = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY);
        
        if (!getEditingMode().containsKey(CreditMemoEditMode.DISPLAY_INIT_TAB)){
            
            if (!(SpringContext.getBean(PurapService.class).isFullDocumentEntryCompleted(cmDocument) == false && documentActions.containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT))) {
                if (getEditingMode().containsKey(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE)) {
                    addExtraButton("methodToCall.calculate", appExternalImageURL + "buttonsmall_calculate.gif", "Calculate");
                }
            
            }
            
        }
        
        return extraButtons;
    }
    
    public String getWireChargeMessage() {
        return wireChargeMessage;
    }

    public void setWireChargeMessage(final String wireChargeMessage) {
        this.wireChargeMessage = wireChargeMessage;
    }


}
