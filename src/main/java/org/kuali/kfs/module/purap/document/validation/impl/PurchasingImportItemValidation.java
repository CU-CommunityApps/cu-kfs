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
import org.kuali.kfs.module.purap.PurapConstants;
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

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

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
            itemDescriptionValidation.setItemForValidation(getItemForValidation());
            valid &= itemDescriptionValidation.validate(event);
            
            commodityCodeValidation.setItemForValidation(getItemForValidation());
            valid &= commodityCodeValidation.validate(event);
        }
        itemUnitPriceValidation.setItemForValidation(getItemForValidation());
        valid &= itemUnitPriceValidation.validate(event);
        unitOfMeasureValidation.setItemForValidation(getItemForValidation());
        valid &= unitOfMeasureValidation.validate(event);     
        
        
        GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.ITEM_TAB_ERROR_PROPERTY);
        return valid;
    }

    protected boolean validateItemTypeForEinvoiceVendors(PurApItem item, PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = true;
        // Check that item isn't a non-qty item on an e-invoice vendor order
        VendorDetail vendor = purapDocument.getVendorDetail();
        if(ObjectUtils.isNotNull(vendor) && ((VendorDetailExtension)vendor.getExtension()).isEinvoiceVendorIndicator()) {
            // Don't allow non-quantity for einvoice vendors.
            if(PurapConstants.ItemTypeCodes.ITEM_TYPE_SERVICE_CODE.equalsIgnoreCase(item.getItemTypeCode())) {
                // Throw error that the non-qty items are not allowed if the vendor is an einvoice vendor
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_TYPE, CUPurapKeyConstants.PURAP_ITEM_NEW_NONQTY, vendor.getVendorName()); // Add error message
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
