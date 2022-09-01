package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestForm;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;
import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;

public class CuPaymentRequestForm extends PaymentRequestForm {
	
    // KFSPTS-1891
    protected String wireChargeMessage;
    
    public CuPaymentRequestForm() {
		super();
	}
    
    @Override
    public boolean canCalculate() {
    	boolean can = super.canCalculate();
    	// KFSUPGRADE-788
        can = can || editingMode.containsKey(CUPaymentRequestEditMode.WAIVE_WIRE_FEE_EDITABLE);
    	return can;
    }
    

	public String getWireChargeMessage() {
		return wireChargeMessage;
	}

	public void setWireChargeMessage(String wireChargeMessage) {
		this.wireChargeMessage = wireChargeMessage;
	}

    @Override
    public List<ExtraButton> getExtraButtons() {
        super.getExtraButtons();
        PaymentRequestDocument paymentRequestDocument = this.getPaymentRequestDocument();
        if (StringUtils.equalsIgnoreCase(paymentRequestDocument.getDocumentHeader().getApplicationDocumentStatus(),
                PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED) && paymentRequestDocument.getDocumentHeader().getWorkflowDocument().isFinal()
                        && !SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class).isPaymentMethodProcessedUsingPdp(
                                ((CuPaymentRequestDocument) paymentRequestDocument).getPaymentMethodCode())) {
            ExtraButton cancelButton = null;
            for (ExtraButton extraButton : extraButtons) {
                if (StringUtils.equals("methodToCall.cancel", extraButton.getExtraButtonProperty())) {
                    cancelButton = extraButton;
                }                
            }
            if (ObjectUtils.isNotNull(cancelButton)) {
                extraButtons.remove(cancelButton);
            }
            
        }
        return extraButtons;
    }

}
