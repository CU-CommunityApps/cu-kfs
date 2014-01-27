/*
 * Copyright 2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.document.validation.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants.ItemFields;
import org.kuali.kfs.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UnitOfMeasure;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapPropertyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PurchasingAddItemValidation extends PurchasingAccountsPayableAddItemValidation {

    private BusinessObjectService businessObjectService;
    private DataDictionaryService dataDictionaryService;
    private PurchasingUnitOfMeasureValidation unitOfMeasureValidation;
    private PurchasingItemUnitPriceValidation itemUnitPriceValidation;
    private PurchasingItemDescriptionValidation itemDescriptionValidation;
    private PurchasingItemQuantityValidation itemQuantityValidation;
    private PurchasingCommodityCodeValidation commodityCodeValidation;
    
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid=true;        
        GlobalVariables.getMessageMap().addToErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
        //refresh itemType
        PurApItem refreshedItem = getItemForValidation();
        refreshedItem.refreshReferenceObject("itemType");
        super.setItemForValidation(refreshedItem);

        valid &= super.validate(event);
        valid &= validateItemUnitPrice(getItemForValidation());
        valid &= validateUnitOfMeasure(getItemForValidation());
        if (getItemForValidation().getItemType().isLineItemIndicator()) {

            valid &= validateItemDescription(getItemForValidation());
            valid &= validateItemQuantity(getItemForValidation());
            commodityCodeValidation.setItemForValidation(getItemForValidation());
            valid &= commodityCodeValidation.validate(event);
            // KFSPTS-2096
            valid &= validateMixItemType(getItemForValidation(), (PurchasingAccountsPayableDocument)event.getDocument());
        }
        GlobalVariables.getMessageMap().removeFromErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);

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
     * Validates that if the item type is quantity based, the item quantity is required and if the item type is amount based, the
     * quantity is not allowed.
     * 
     * @param item the item to be validated
     * @return boolean false if there's any validation that fails.
     */
    public boolean validateItemQuantity(PurApItem item) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) item;
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator() && (ObjectUtils.isNull(purItem.getItemQuantity()))) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel();            
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
        }
        else if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator() && ObjectUtils.isNotNull(purItem.getItemQuantity())) {
            valid = false;
            String attributeLabel = dataDictionaryService.
                                    getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                    getAttributeDefinition(PurapPropertyConstants.ITEM_QUANTITY).getLabel(); 
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.QUANTITY, PurapKeyConstants.ERROR_ITEM_QUANTITY_NOT_ALLOWED, attributeLabel + " in " + item.getItemIdentifierString());
        }

        return valid;
    }

    /**
     * Predicate to do a parameter lookup and tell us whether a commodity code is required.
     * Override in child classes. 
     * 
     * @return      True if a commodity code is required.
     */
    protected boolean commodityCodeIsRequired() {
        return false;
    }

    protected boolean validateThatCommodityCodeIsActive(PurApItem item) {
        if (!((PurchasingItemBase)item).getCommodityCode().isActive()) {
            //This is the case where the commodity code on the item is not active.
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INACTIVE, " in " + item.getItemIdentifierString());
            return false;
        }
        return true;
    }
    
    private boolean validateMixItemType(PurApItem newItem, PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = true;
        if(StringUtils.isNotBlank(newItem.getItemTypeCode())) {
            String itemTypeCode = newItem.getItemTypeCode();
            for(PurApItem item : purapDocument.getItems()) {
                if (StringUtils.isNotBlank(item.getItemTypeCode()) && !itemTypeCode.equalsIgnoreCase(item.getItemTypeCode()) &&
                         (CUPurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode()) ||
                                CUPurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE.equalsIgnoreCase(item.getItemTypeCode()))) {
                    GlobalVariables.getMessageMap().addToErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
                    GlobalVariables.getMessageMap().putError(CUPurapPropertyConstants.ITEM_TYPE_CODE, CUPurapKeyConstants.PURAP_MIX_ITEM_QTY_NONQTY); 
                    valid &= false;
                    GlobalVariables.getMessageMap().removeFromErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
                    break;
                }
            }
        }
        return valid;
    }
    
    protected boolean validateItemTypeForEinvoiceVendors(PurApItem item, PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = true;
        // Check that item isn't a non-qty item on an e-invoice vendor order
        VendorDetail vendor = purapDocument.getVendorDetail();
        if(ObjectUtils.isNotNull(vendor) && ((VendorDetailExtension)vendor.getExtension()).isEinvoiceVendorIndicator()) {
            // Don't allow non-quantity for einvoice vendors.
            if(CUPurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode())) {
                // Throw error that the non-qty items are not allowed if the vendor is an einvoice vendor
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_TYPE, CUPurapKeyConstants.PURAP_ITEM_NEW_NONQTY, vendor.getVendorName()); 
                valid = false;
            }        
        }       
        return valid;
    }
    
    public boolean validateUnitOfMeasure(PurApItem item) {
        boolean valid = true;
        PurchasingItemBase purItem = (PurchasingItemBase) item;
        // Validations for quantity based item type
        if (purItem.getItemType().isQuantityBasedGeneralLedgerIndicator()) {
            String uomCode = purItem.getItemUnitOfMeasureCode();
            if (StringUtils.isEmpty(uomCode)) {
                valid = false;
                String attributeLabel = dataDictionaryService.
                                        getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                        getAttributeDefinition(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).
                                        getLabel();
                GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + item.getItemIdentifierString());
            }
            else {
                //Find out whether the unit of measure code has existed in the database
                Map<String,String> fieldValues = new HashMap<String, String>();
                fieldValues.put(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, purItem.getItemUnitOfMeasureCode());
                if (businessObjectService.countMatching(UnitOfMeasure.class, fieldValues) != 1) {
                    //This is the case where the unit of measure code on the item does not exist in the database.
                    valid = false;
                    GlobalVariables.getMessageMap().putError(KFSPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, PurapKeyConstants.PUR_ITEM_UNIT_OF_MEASURE_CODE_INVALID,  " in " + item.getItemIdentifierString());
                }
            }
        }
        
        // Validations for non-quantity based item type
         if (purItem.getItemType().isAmountBasedGeneralLedgerIndicator() && StringUtils.isNotBlank(purItem.getItemUnitOfMeasureCode())) {
             valid = false;
             String attributeLabel = dataDictionaryService.
                                     getDataDictionary().getBusinessObjectEntry(item.getClass().getName()).
                                     getAttributeDefinition(PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE).getLabel(); 
             GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_UNIT_OF_MEASURE_CODE, PurapKeyConstants.ERROR_ITEM_UOM_NOT_ALLOWED, attributeLabel + " in " + item.getItemIdentifierString());
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
    
    public PurchasingItemUnitPriceValidation getItemUnitPriceValidation() {
        return itemUnitPriceValidation;
    }
    
    public void setItemUnitPriceValidation(PurchasingItemUnitPriceValidation itemUnitPriceValidation) {
        this.itemUnitPriceValidation = itemUnitPriceValidation;
    }
    public PurchasingUnitOfMeasureValidation getUnitOfMeasureValidation() {
        return unitOfMeasureValidation;
    }

    public void setUnitOfMeasureValidation(PurchasingUnitOfMeasureValidation unitOfMeasureValidation) {
        this.unitOfMeasureValidation = unitOfMeasureValidation;
    }

    public PurchasingItemDescriptionValidation getItemDescriptionValidation() {
        return itemDescriptionValidation;
    }
    
    public void setItemDescriptionValidation(PurchasingItemDescriptionValidation itemDescriptionValidation) {
        this.itemDescriptionValidation = itemDescriptionValidation;
    }

    public PurchasingItemQuantityValidation getItemQuantityValidation() {
        return itemQuantityValidation;
    }

    public void setItemQuantityValidation(PurchasingItemQuantityValidation itemQuantityValidation) {
        this.itemQuantityValidation = itemQuantityValidation;
    }

    public PurchasingCommodityCodeValidation getCommodityCodeValidation() {
        return commodityCodeValidation;
    }

    public void setCommodityCodeValidation(PurchasingCommodityCodeValidation commodityCodeValidation) {
        this.commodityCodeValidation = commodityCodeValidation;
    }
    

}
