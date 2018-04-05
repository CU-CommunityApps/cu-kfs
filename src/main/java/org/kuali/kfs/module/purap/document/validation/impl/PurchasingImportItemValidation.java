/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2018 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.service.DataDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapConstants.ItemFields;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PurchasingImportItemValidation extends PurchasingAccountsPayableImportItemValidation {

    private BusinessObjectService businessObjectService;
    private DataDictionaryService dataDictionaryService;
    private PurchasingUnitOfMeasureValidation unitOfMeasureValidation;
    private PurchasingItemUnitPriceValidation itemUnitPriceValidation;
    private PurchasingItemDescriptionValidation itemDescriptionValidation;
    private PurchasingCommodityCodeValidation commodityCodeValidation;
    
    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;  
        
        PurApItem refreshedItem = getItemForValidation();
        refreshedItem.refreshReferenceObject("itemType");
        super.setItemForValidation(refreshedItem);
        
        valid &= super.validate(event);
        GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.ITEM_TAB_ERROR_PROPERTY);
        
        if (getItemForValidation().getItemType().isLineItemIndicator()) {
            valid &= validateItemDescription(getItemForValidation());
            
            commodityCodeValidation.setItemForValidation(getItemForValidation());
            valid &= commodityCodeValidation.validate(event);
        }
        valid &= validateItemUnitPrice(getItemForValidation());
        valid &= validateUnitOfMeasureCodeExists(getItemForValidation());     
        
        
        GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.ITEM_TAB_ERROR_PROPERTY);
        return valid;
    }
    
    /**
     * Checks that a description was entered for the item.
     * 
     * @param item
     * @return
     */
    public boolean validateItemDescription(PurApItem item) {
        boolean valid = true;      
        if (StringUtils.isEmpty(item.getItemDescription())) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                    getAttributeDefinition(PurapPropertyConstants.ITEM_DESCRIPTION).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_DESCRIPTION, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
        }
        return valid;
    }
    
    /**
     * Validates the unit price for all applicable item types. It validates that the unit price field was
     * entered on the item, and that the price is in the right range for the item type.
     * 
     * @param purDocument the purchasing document to be validated
     * @return boolean false if there is any validation that fails.
     */
    public boolean validateItemUnitPrice(PurApItem item) {
        boolean valid = true;
        if (item.getItemType().isLineItemIndicator()) {
            if (ObjectUtils.isNull(item.getItemUnitPrice())) {
                valid = false;
                String attributeLabel = dataDictionaryService.
                                        getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                        getAttributeDefinition(PurapPropertyConstants.ITEM_UNIT_PRICE).getLabel();
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
            }
        }    

        if (ObjectUtils.isNotNull(item.getItemUnitPrice())) {
            if ((BigDecimal.ZERO.compareTo(item.getItemUnitPrice()) > 0) && ((!item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) && (!item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)))) {
                // If the item type is not full order discount or trade in items, don't allow negative unit price.
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_ITEM_AMOUNT_BELOW_ZERO, ItemFields.UNIT_COST, item.getItemIdentifierString());
                valid = false;
            }
            else if ((BigDecimal.ZERO.compareTo(item.getItemUnitPrice()) < 0) && ((item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) || (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)))) {
                // If the item type is full order discount or trade in items, its unit price must be negative.
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_PRICE, PurapKeyConstants.ERROR_ITEM_AMOUNT_NOT_BELOW_ZERO, ItemFields.UNIT_COST, item.getItemIdentifierString());
                valid = false;
            }
        }

        return valid;
    }
    
    /**
     * Validates that if the item type is quantity based, that the unit of measure code is valid.
     * Looks for the UOM Code in the table. If it is not there, the code is invalid. 
     * This checking is needed only for imported items, since items added from new line could only 
     * choose an existing UOM from the drop-down list.
     * 
     * @param item the item to be validated
     * @return boolean false if the item type is quantity based and the unit of measure code is invalid.
     */
    protected boolean validateUnitOfMeasureCodeExists(PurApItem item) {
        boolean valid = true;
        
        if (item.getItemType().isQuantityBasedGeneralLedgerIndicator()) {            
            String uomCode = item.getItemUnitOfMeasureCode();
            Map<String,String> fieldValues = new HashMap<String,String>();
            fieldValues.put(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, uomCode);
            if (businessObjectService.countMatching(UnitOfMeasure.class, fieldValues) != 1) {
                String[] errorParams = { uomCode, "" + item.getItemLineNumber() };
                GlobalVariables.getMessageMap().putError(PurapConstants.ITEM_TAB_ERRORS, PurapKeyConstants.ERROR_ITEMPARSER_INVALID_UOM_CODE, errorParams);
                valid = false;
            }  
        }

        return valid;
    }
   

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
    
    public PurchasingUnitOfMeasureValidation getUnitOfMeasureValidation() {
        return unitOfMeasureValidation;
    }

    public void setUnitOfMeasureValidation(PurchasingUnitOfMeasureValidation unitOfMeasureValidation) {
        this.unitOfMeasureValidation = unitOfMeasureValidation;
    }

    public PurchasingItemUnitPriceValidation getItemUnitPriceValidation() {
        return itemUnitPriceValidation;
    }

    public void setItemUnitPriceValidation(PurchasingItemUnitPriceValidation itemUnitPriceValidation) {
        this.itemUnitPriceValidation = itemUnitPriceValidation;
    }

    public PurchasingItemDescriptionValidation getItemDescriptionValidation() {
        return itemDescriptionValidation;
    }

    public void setItemDescriptionValidation(PurchasingItemDescriptionValidation itemDescriptionValidation) {
        this.itemDescriptionValidation = itemDescriptionValidation;
    }
    public PurchasingCommodityCodeValidation getCommodityCodeValidation() {
        return commodityCodeValidation;
    }

    public void setCommodityCodeValidation(PurchasingCommodityCodeValidation commodityCodeValidation) {
        this.commodityCodeValidation = commodityCodeValidation;
    }

}
