/*
 * Copyright 2008 The Kuali Foundation
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.util.Strings;
import org.kuali.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kns.datadictionary.validation.fieldlevel.PhoneNumberValidationPattern;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.workflow.service.KualiWorkflowDocument;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;
import edu.cornell.kfs.module.purap.document.service.impl.PurchaseOrderTransmissionMethodDataRulesServiceImpl;

public class PurchasingProcessVendorValidation extends PurchasingAccountsPayableProcessVendorValidation {
    
    private VendorService vendorService;
    private ParameterService parameterService;
    private PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService;
    
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingDocument purDocument = (PurchasingDocument) event.getDocument();
        MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(PurapConstants.VENDOR_ERRORS);
        
        valid &= super.validate(event);        
        
        if (!purDocument.getRequisitionSourceCode().equals(PurapConstants.RequisitionSources.B2B)) {

// KFSPTS-1458 : code that existed before change below was put in place     	
//            //If there is a vendor and the transmission method is FAX and the fax number is blank, display
//            //error that the fax number is required.
//            if (purDocument.getVendorHeaderGeneratedIdentifier() != null && purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.FAX) && (purDocument.getVendorFaxNumber() == null || StringUtils.isBlank(purDocument.getVendorFaxNumber()))) {
//                valid &= false;
//                String attributeLabel = SpringContext.getBean(DataDictionaryService.class).
//                getDataDictionary().getBusinessObjectEntry(VendorAddress.class.getName()).
//                getAttributeDefinition(VendorPropertyConstants.VENDOR_FAX_NUMBER).getLabel();
//                errorMap.putError(VendorPropertyConstants.VENDOR_FAX_NUMBER, KFSKeyConstants.ERROR_REQUIRED, attributeLabel);
//            }
//            if (StringUtils.isNotBlank(purDocument.getVendorFaxNumber())) {
//                PhoneNumberValidationPattern phonePattern = new PhoneNumberValidationPattern();
//                if (!phonePattern.matches(purDocument.getVendorFaxNumber())) {
//                    valid &= false;
//                    errorMap.putError(VendorPropertyConstants.VENDOR_FAX_NUMBER, PurapKeyConstants.ERROR_FAX_NUMBER_INVALID);
//                }
//            }
        	//******************************************************************
            //KFSPTS-1458 implementation notes:
        	//Logic reworked for validation of data required by method of PO transmission value selected. 
        	//
        	//Per the functional users, the only time the data validation check should be performed is when a user  
        	//has the ability to edit the data on the eDoc (those users in roles = initiator and contract manager).
        	//
        	//Per the functional users, the data validation check should NOT be performed when a user does not have the ability to
        	//edit the data on the eDoc (those users in roles = fiscal officer, c&g approver, commodity review, base org reviewer, and separation of duties reviewer).
        	//
        	//Route nodes corresponding to the "roles" that were identified by the functional users were used
        	//to determine whether the data validation check should or should not occur to ensure proper security.
        	//
        	//if-check is:
        	//(vendor record exists) and (based on route node: role is one that allowed edoc editing) and (mopot is one needing data validation) 
        	//******************************************************************
        	PurchaseOrderTransmissionMethodDataRulesServiceImpl purchaseOrderTransmissionMethodDataRulesServiceImpl = SpringContext.getBean(PurchaseOrderTransmissionMethodDataRulesServiceImpl.class);
            if ( (purDocument.getVendorHeaderGeneratedIdentifier() != null) &&  
            	 ( !(isDocumentInNodeWhereMopotDataValidationIsBypassed(purDocument))) &&             	             	 
                 (purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.EMAIL) ||
                  purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.FAX) || 
                  purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.MANUAL)) ) {            	
            	valid &= purchaseOrderTransmissionMethodDataRulesServiceImpl.validateDataForMethodOfPOTransmissionExistsOnVendorAddress(purDocument);
            	//called routine took care of presenting error message to user
            }            
        }

        VendorDetail vendorDetail = vendorService.getVendorDetail(purDocument.getVendorHeaderGeneratedIdentifier(), purDocument.getVendorDetailAssignedIdentifier());
        if (ObjectUtils.isNull(vendorDetail))
            return valid;
        VendorHeader vendorHeader = vendorDetail.getVendorHeader();

        // make sure that the vendor is not debarred
        if (vendorDetail.isVendorDebarred()) {
            valid &= false;
            errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_DEBARRED_VENDOR);
        }

        // make sure that the vendor is of allowed type
        List<String> allowedVendorTypes = parameterService.getParameterValues(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapRuleConstants.PURAP_VENDOR_TYPE_ALLOWED_ON_REQ_AND_PO);
        if (allowedVendorTypes != null && !allowedVendorTypes.isEmpty()){
           if (ObjectUtils.isNotNull(vendorHeader) && ObjectUtils.isNotNull(vendorHeader.getVendorTypeCode()) && ! allowedVendorTypes.contains(vendorHeader.getVendorTypeCode())) {
                    valid &= false;
                    errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_INVALID_VENDOR_TYPE);
            }
        }

        // make sure that the vendor is active
        if (!vendorDetail.isActiveIndicator()) {
            valid &= false;
            errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_INACTIVE_VENDOR);
        }

        errorMap.clearErrorPath();
        return valid;

    }

    public VendorService getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
    
    public PurchaseOrderTransmissionMethodDataRulesService getPurchaseOrderTransmissionMethodDataRulesService() {
        return purchaseOrderTransmissionMethodDataRulesService;
    }

    public void setPurchaseOrderTransmissionMethodDataRulesService(PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService) {
        this.purchaseOrderTransmissionMethodDataRulesService = purchaseOrderTransmissionMethodDataRulesService;
    }
	
	
	/**
	 * KFSPTS-1458 
	 * Using Route nodes to determine when data validation should occur based on functional user's requirements.
	 * 
	 */
    private boolean isDocumentInNodeWhereMopotDataValidationIsBypassed (PurchasingDocument purDocument) {
    	boolean return_value = false;
        KualiWorkflowDocument workflowDoc = purDocument.getDocumentHeader().getWorkflowDocument();
        List<String>  currentRouteLevels = Arrays.asList(Strings.split(purDocument.getDocumentHeader().getWorkflowDocument().getCurrentRouteNodeNames(), ","));
        if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.ACCOUNT_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.SEPARATION_OF_DUTIES_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.AWARD_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.SUB_ACCOUNT_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.COMMODITY_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.COMMODITY_APO_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.ACCOUNTING_ORGANIZATION_HIERARCHY_NODE) && workflowDoc.isApprovalRequested()) {
        	return_value = true;
        }
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.AD_HOC_NODE) && workflowDoc.isApprovalRequested() &&  !workflowDoc.isAdHocRequested()) {
        	return_value = true;
        }
        return (return_value);
    }  
        
}
