package edu.cornell.kfs.module.purap.document.web.struts;

import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestForm;

import edu.cornell.kfs.module.purap.CUPurapAuthorizationConstants.CUPaymentRequestEditMode;

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

}
