package edu.cornell.kfs.module.purap;

import org.kuali.kfs.module.purap.PurapAuthorizationConstants;

public class CUPurapAuthorizationConstants extends PurapAuthorizationConstants {

    
    public static class CURequisitionEditMode  {
        //KFSPTS-1792
        public static final String ENABLE_CAPITAL_ASSET = "enableCapitalAsset";
    }
    
    public static class CUPaymentRequestEditMode  {
        // KFSPTS-1891
        public static final String EDIT_AMOUNT = "editAmount";
        public static final String WAIVE_WIRE_FEE_EDITABLE = "waiveWireFeeEditable";
        //KFSPTS-2968
        public static final String ADDITONAL_CHARGE_AMOUNT_EDITABLE= "addtnlChargeAmountEditable";
    }
}