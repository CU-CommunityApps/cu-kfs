/**
 * 
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;

/**
 * @author dwf5
 *
 */
public class PurchaseOrderAmendmentCommodityCodeValidation extends PurchaseOrderCommodityCodeValidation {

	
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

    /**
     * Overrides the method in PurchasingDocumentRuleBase so that we'll return true
     * if the item has been previously saved to the database and we'll only check for
     * the commodity code active flag if the item has not been previously saved to
     * the database. 
     * 
     * @param item
     * @param commodityCodeRequired
     * @return
     */
    @Override
    protected boolean validateThatCommodityCodeIsActive(PurApItem item) {
        if (item.getVersionNumber() != null) {
            return true;
        }
        else {
            if (!((PurchasingItemBase)item).getCommodityCode().isActive()) {
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INACTIVE, " in " + item.getItemIdentifierString());
                return false;
            }
            return true;
        }
    }
	
    @Override
    protected boolean commodityCodeIsRequired(PurApItem item) {
    	// commodity code is not an attribute for below the line items.
    	if (!item.getItemType().isLineItemIndicator()) {
        	return false;
        }

        //if the ENABLE_COMMODITY_CODE_IND parameter is  N then we don't
        //need to check for the ITEMS_REQUIRE_COMMODITY_CODE_IND parameter anymore, just return false. 
        boolean enableCommodityCode = SpringContext.getBean(ParameterService.class).getIndicatorParameter(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapParameterConstants.ENABLE_COMMODITY_CODE_IND);
        if (!enableCommodityCode) {
            return false;
        }
        else {        
            return getParameterService().getIndicatorParameter(PurchaseOrderDocument.class, PurapRuleConstants.ITEMS_REQUIRE_COMMODITY_CODE_IND);
        }
    }
}
