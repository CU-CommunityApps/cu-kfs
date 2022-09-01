/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.businessobject.PurchasingItemBase;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.WorkflowDocumentFactory;

import java.util.HashMap;
import java.util.Map;

public class PurchasingCommodityCodeValidation extends GenericValidation {
    // TODO : CU has quite a few change in this class.  Just try to make this work for POAmendmentValidation. need further check on this class.
    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private BusinessObjectService businessObjectService;
    private PurApItem itemForValidation;
    private ParameterService parameterService;
    public static final String UNORDERED_ITEM_DEFAULT_COMMODITY_CODE = "UNORDERED_ITEM_DEFAULT_COMMODITY_CODE";
  
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        GlobalVariables.getMessageMap().clearErrorPath();                
        GlobalVariables.getMessageMap().addToErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        
        if(ObjectUtils.isNotNull(itemForValidation)) {
        	// CU fix for NPE KFSPTS-1154
	        itemForValidation.refreshReferenceObject(PurapPropertyConstants.COMMODITY_CODE);
	        // kfs 5.1 remove the item argument in commodityCodeIsRequired
	        valid &= validateCommodityCodes(itemForValidation, commodityCodeIsRequired());
	        
	        GlobalVariables.getMessageMap().removeFromErrorPath(PurapConstants.ITEM_TAB_ERRORS);
        }
        return valid;

    }
    
    public PurApItem getItemForValidation() {
        return itemForValidation;
    }

    public void setItemForValidation(PurApItem itemForValidation) {
        this.itemForValidation = itemForValidation;
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
            String unorderedItemDefaultCommodityCode = parameterService.getParameterValueAsString(PurapConstants.PURAP_NAMESPACE, "LineItemReceiving", UNORDERED_ITEM_DEFAULT_COMMODITY_CODE);
            purItem.setPurchasingCommodityCode(unorderedItemDefaultCommodityCode);
            valid = true;
        }
        
        //This validation is only needed if the commodityCodeRequired system parameter is true
        if (commodityCodeRequired && StringUtils.isBlank(purItem.getPurchasingCommodityCode()) ) {
            //This is the case where the commodity code is required but the item does not currently contain the commodity code.
            valid = false;
            String attributeLabel = businessObjectDictionaryService
                    .getBusinessObjectEntry(CommodityCode.class.getName()).getAttributeDefinition(
                            PurapPropertyConstants.ITEM_COMMODITY_CODE).getLabel();
            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE,
                    KFSKeyConstants.ERROR_REQUIRED, attributeLabel + " in " + identifierString);
        }
        else if (StringUtils.isNotBlank(purItem.getPurchasingCommodityCode())) {
            //Find out whether the commodity code has existed in the database
            Map<String,String> fieldValues = new HashMap<String, String>();
            fieldValues.put(PurapPropertyConstants.ITEM_COMMODITY_CODE, purItem.getPurchasingCommodityCode());
            if (businessObjectService.countMatching(CommodityCode.class, fieldValues) != 1) {
                //This is the case where the commodity code on the item does not exist in the database.
                valid = false;
                GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE,
                        PurapKeyConstants.PUR_COMMODITY_CODE_INVALID, " in " + identifierString);
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
        if (shouldCheckCommodityCodeIsActive(item)) {
	        if (!((PurchasingItemBase)item).getCommodityCode().isActive()) {
	            //This is the case where the commodity code on the item is not active.
	            GlobalVariables.getMessageMap().putError(PurapPropertyConstants.ITEM_COMMODITY_CODE,
	                    PurapKeyConstants.PUR_COMMODITY_CODE_INACTIVE, " in " + item.getItemIdentifierString());
	            return false;
	        }
        }
        return true;
    }

	/**
	 * This method analyzes the document status and determines if the commodity codes associated with this item should be verified as active.
	 * The current implementation only checks that a commodity code is active if the document associated is in either INITIATED or SAVED status.
	 * For all other statuses the document may be in, the commodity code will not be checked for active status and the method will simply return 
	 * true unconditionally.
	 * 
	 * @param item
	 * @return 
	 */
	private boolean shouldCheckCommodityCodeIsActive(PurApItem item) {
	    if(ObjectUtils.isNotNull(item.getPurapDocument())) {
	    String docNum = item.getPurapDocument().getDocumentNumber();
		PurchasingAccountsPayableDocument purapDoc = item.getPurapDocument();		
        	// Ran into issues with workflow doc not being populated in doc header for some PURAP docs, so needed to add check and retrieval.
        	DocumentHeader docHdr = (DocumentHeader)purapDoc.getDocumentHeader();
        	WorkflowDocument kwd = null;
 			
		    kwd = WorkflowDocumentFactory.loadDocument(GlobalVariables.getUserSession().getPrincipalId(), docNum);
		    docHdr.setWorkflowDocument(kwd);

            // Only check for active commodity codes if the doc is in initiated or saved status.  
        	if(ObjectUtils.isNull(kwd)) {
        		kwd = docHdr.getWorkflowDocument();
        	}
	        if(!(kwd.isInitiated() || kwd.isSaved())) { 
	            return false;
	        }
        }
        return true;
	}
	
    /**
     * Predicate to do a parameter lookup and tell us whether a commodity code is required. Override in child classes.
     *
     * @return True if a commodity code is required.
     */
    protected boolean commodityCodeIsRequired() {
        return false;
    }
    
    public void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

	public ParameterService getParameterService() {
		return parameterService;
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

    

}
