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

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

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
    
    /**
     * 
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid=true;        
        GlobalVariables.getMessageMap().addToErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);
        //refresh itemType
        PurApItem refreshedItem = getItemForValidation();
        refreshedItem.refreshReferenceObject("itemType");
        super.setItemForValidation(refreshedItem);
        
        valid &= super.validate(event);
        
        unitOfMeasureValidation.setItemForValidation(getItemForValidation());
        valid &= unitOfMeasureValidation.validate(event);
        
        itemUnitPriceValidation.setItemForValidation(getItemForValidation());
        valid &= itemUnitPriceValidation.validate(event);                     
        
        if (getItemForValidation().getItemType().isLineItemIndicator()) {

            itemDescriptionValidation.setItemForValidation(getItemForValidation());
            valid &= itemDescriptionValidation.validate(event);
                        
            itemQuantityValidation.setItemForValidation(getItemForValidation());
            valid &= itemQuantityValidation.validate(event);

            commodityCodeValidation.setItemForValidation(getItemForValidation());
            valid &= commodityCodeValidation.validate(event);
            // KFSPTS-2096
            valid &= validateMixItemType(getItemForValidation(), (PurchasingAccountsPayableDocument)event.getDocument());
        }
        
        GlobalVariables.getMessageMap().removeFromErrorPath(PurapPropertyConstants.NEW_PURCHASING_ITEM_LINE);

        return valid;
    }

    /**
     * Validates that there are no non-quantity line items on orders placed to vendors identified as einvoice vendors.
     * 
     * @param item  The PurApItem containing the item type to be validated.
     * @return boolean false if the validation fails and true otherwise.
     */
    protected boolean validateItemTypeForEinvoiceVendors(PurApItem item, PurchasingAccountsPayableDocument purapDocument) {
    	boolean valid = true;
        // Check that item isn't a non-qty item on an e-invoice vendor order
    	VendorDetail vendor = purapDocument.getVendorDetail();
        if(ObjectUtils.isNotNull(vendor) && ((VendorDetailExtension)vendor.getExtension()).isEinvoiceVendorIndicator()) {
            // Don't allow non-quantity for einvoice vendors.
            if(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode())) {
                // Throw error that the non-qty items are not allowed if the vendor is an einvoice vendor
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_TYPE, CUPurapKeyConstants.PURAP_ITEM_NEW_NONQTY, vendor.getVendorName()); 
                valid = false;
	        }        
        }    	
    	return valid;
    }

    private boolean validateMixItemType(PurApItem newItem, PurchasingAccountsPayableDocument purapDocument) {
    	boolean valid = true;
        if(StringUtils.isNotBlank(newItem.getItemTypeCode())) {
        	String itemTypeCode = newItem.getItemTypeCode();
            for(PurApItem item : purapDocument.getItems()) {
            	if (StringUtils.isNotBlank(item.getItemTypeCode()) && !itemTypeCode.equalsIgnoreCase(item.getItemTypeCode()) &&
            			 (PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode()) ||
     	            			PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE.equalsIgnoreCase(item.getItemTypeCode()))) {
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
