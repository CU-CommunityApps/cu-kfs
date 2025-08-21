/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchasingCommodityCodeValidation;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;


/**
 * @author dwf5
 *
 */
public class PurchaseOrderCommodityCodeValidation extends PurchasingCommodityCodeValidation {

    /**
     * This method overrides the parent method to implement some parameter checking that is unique
     * to the Requisition.
     * 
     * @return
     */
	@Override
    protected boolean commodityCodeIsRequired() {
    	// commodity code is not an attribute for below the line items.
    	if (!getItemForValidation().getItemType().isLineItemIndicator()) {
        	return false;
        }

        // if the ENABLE_COMMODITY_CODE_IND parameter is  N then we don't
        // need to check for the ITEMS_REQUIRE_COMMODITY_CODE_IND parameter anymore, just return false. 
    	boolean enableCommodityCode = getParameterService().getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.COMMODITY_CODE_IND);
        if (!enableCommodityCode) {
            return false;
        }
        else {        
            return getParameterService().getParameterValueAsBoolean(PurchaseOrderDocument.class, PurapRuleConstants.COMMODITY_CODE_REQUIRED_IND);
        }
    }
	
    /**
     * This method overrides the parent method because business rules do not require commodity codes to be active on Purchase Orders.
     * KFSPTS-1154
     * 
     * @param item - PurApItem object that contains the commodity code to be checked.
     * @return
     */
    protected boolean validateThatCommodityCodeIsActive(PurApItem item) {
    	return true;
    }

}
