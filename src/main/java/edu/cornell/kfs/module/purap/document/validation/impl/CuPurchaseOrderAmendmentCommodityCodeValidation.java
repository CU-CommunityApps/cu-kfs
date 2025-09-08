/**
 * 
 */
package edu.cornell.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchasingCommodityCodeValidation;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;


/**
 * @author dwf5
 *
 * NOTE : This class extends the parent class PurchasingCommodityCodeValidation because the logic that the commodity code validation is based on
 * for POAs should not inherit from the PO commodity code validation.  POA validation is more closely in line with the Requisition's requirements 
 * than the Purchase Order's.
 *
 */
public class CuPurchaseOrderAmendmentCommodityCodeValidation extends PurchasingCommodityCodeValidation {

	
    /**
     * Overrides to provide validation for PurchaseOrderAmendmentDocument. 
     * @see org.kuali.kfs.module.purap.document.validation.impl.PurchasingDocumentRuleBase#validateCommodityCodes(org.kuali.kfs.module.purap.businessobject.PurApItem, boolean)
     */
    @Override
    protected boolean validateCommodityCodes(PurApItem item, boolean commodityCodeRequired) {
        //If the item is inactive then don't need any of the following validations.
        if (!((PurchaseOrderItem)item).isItemActiveIndicator()) {
            return true;
        }
        else {
            return super.validateCommodityCodes(item, commodityCodeRequired);
        }
    }

    @Override
    protected boolean commodityCodeIsRequired() {
	        // kfs 5.1 remove the item argument in commodityCodeIsRequired
    	// commodity code is not an attribute for below the line items.
    	if (!getItemForValidation().getItemType().isLineItemIndicator()) {
        	return false;
        }

        //if the ENABLE_COMMODITY_CODE_IND parameter is  N then we don't
        //need to check for the ITEMS_REQUIRE_COMMODITY_CODE_IND parameter anymore, just return false. 
        boolean enableCommodityCode = getParameterService().getParameterValueAsBoolean(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.COMMODITY_CODE_IND);
        if (!enableCommodityCode) {
            return false;
        }
        else {        
            return getParameterService().getParameterValueAsBoolean(PurchaseOrderDocument.class, PurapRuleConstants.COMMODITY_CODE_REQUIRED_IND);
        }
    }
}
