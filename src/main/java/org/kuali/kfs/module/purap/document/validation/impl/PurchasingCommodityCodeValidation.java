/*
 * Copyright 2008-2009 The Kuali Foundation
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

public class PurchasingCommodityCodeValidation extends GenericValidation {
    
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DataDictionaryService dataDictionaryService;
    private PurApItem itemForValidation;
    
    public static final String UNORDERED_ITEM_DEFAULT_COMMODITY_CODE = "UNORDERED_ITEM_DEFAULT_COMMODITY_CODE";

    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        GlobalVariables.getMessageMap().clearErrorPath();                
        GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        
        if(ObjectUtils.isNotNull(itemForValidation)) {
	        itemForValidation.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
	        valid &= validateCommodityCodes(itemForValidation, commodityCodeIsRequired(itemForValidation));
	        
	        GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        }
        return valid;

    }
    
    /**
     * Validates whether the commodity code existed on the item, and if existed, whether the
     * commodity code on the item existed in the database, and if so, whether the commodity 
     * code is active. Display error if any of these 3 conditions are not met.
     * 
     * @param item  The PurApItem containing the commodity code to be validated.
     * @return boolean false if the validation fails and true otherwise.
     */
    protected boolean validateCommodityCodes(PurApItem item, boolean commodityCodeRequired) {
        boolean valid = true;
        String identifierString = item.getItemIdentifierString();
        PurchasingItemBase purItem = (PurchasingItemBase) item;
        
        // check to see if item is unordered and commodity code is required, if so, assign a default commodity code and return true
        if (commodityCodeRequired && purItem.getItemTypeCode().equals("UNOR") ) {
            ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            String unorderedItemDefaultCommodityCode = parameterService.getParameterValue(PurapConstants.PURAP_NAMESPACE, "LineItemReceiving", UNORDERED_ITEM_DEFAULT_COMMODITY_CODE);
            purItem.setPurchasingCommodityCode(unorderedItemDefaultCommodityCode);
            valid = true;
        }
        
        //This validation is only needed if the commodityCodeRequired system parameter is true
        if (commodityCodeRequired && StringUtils.isBlank(purItem.getPurchasingCommodityCode()) ) {
            //This is the case where the commodity code is required but the item does not currently contain the commodity code.
            valid = false;
            String attributeLabel = dataDictionaryService.
                                    getDataDictionary().getBusinessObjectEntry(CommodityCode.class.getName()).
                                    getAttributeDefinition(PurapPropertyConstants.ITEM_COMMODITY_CODE).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + identifierString);
        }
        else if (StringUtils.isNotBlank(purItem.getPurchasingCommodityCode())) {
            //Find out whether the commodity code has existed in the database
            Map<String,String> fieldValues = new HashMap<String, String>();
            fieldValues.put(PurapPropertyConstants.ITEM_COMMODITY_CODE, purItem.getPurchasingCommodityCode());
            if (businessObjectService.countMatching(CommodityCode.class, fieldValues) != 1) {
                //This is the case where the commodity code on the item does not exist in the database.
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INVALID,  " in " + identifierString);
            }
            else {
                valid &= validateThatCommodityCodeIsActive(item);
            }
        }
        
        return valid;
    }

    /**
     * This method retrieves the commodity code from the provided PurApItem object and checks if the commodity code is active.
     * This method will automatically return true if the document has already been submitted (ie. not in initiated or saved status).  
     * This is because we do not care if the commodity code continues to be active after the document has been submitted.  KFSPTS-1154
     * 
     * @param item - PurApItem object that contains the commodity code to be checked.
     * @return
     */
    protected boolean validateThatCommodityCodeIsActive(PurApItem item) {
        if (item.getVersionNumber() != null) {
            return true;
        }
        else {
            // Only check for active commodity codes if the doc is in initiated or saved status.  
            PurchasingAccountsPayableDocument purapDoc = item.getPurapDocument();
            if(ObjectUtils.isNotNull(purapDoc)) {
		        KualiWorkflowDocument kwd = purapDoc.getDocumentHeader().getWorkflowDocument();
		        if(!(kwd.stateIsInitiated() || kwd.stateIsSaved())) {
		            return true;
		        }
            }
            
            if (!((PurchasingItemBase)item).getCommodityCode().isActive()) {
                //This is the case where the commodity code on the item is not active.
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE, PurapKeyConstants.PUR_COMMODITY_CODE_INACTIVE, " in " + item.getItemIdentifierString());
                return false;
            }
            return true;
        }
    }

    /**
     * 
     * @return
     */
    protected boolean commodityCodeIsRequired(PurApItem item) {
    	return false;
    }

    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Gets the parameterService attribute. 
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Sets the parameterService attribute value.
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

}
