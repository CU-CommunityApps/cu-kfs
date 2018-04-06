package edu.cornell.kfs.module.purap.document.validation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.PurapParameterConstants;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.PurapRuleConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.validation.impl.PurchasingProcessVendorValidation;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.FinancialSystemWorkflowHelperService;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;
import edu.cornell.kfs.module.purap.document.service.impl.PurchaseOrderTransmissionMethodDataRulesServiceImpl;

public class CuPurchasingProcessVendorValidation extends PurchasingProcessVendorValidation {

    private VendorService vendorService;
    private ParameterService parameterService;
    private PostalCodeValidationService postalCodeValidationService;
    private PurchaseOrderTransmissionMethodDataRulesService purchaseOrderTransmissionMethodDataRulesService;
    private FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        boolean valid = true;
        PurchasingDocument purDocument = (PurchasingDocument) event.getDocument();
        MessageMap errorMap = GlobalVariables.getMessageMap();
        errorMap.clearErrorPath();
        errorMap.addToErrorPath(PurapConstants.VENDOR_ERRORS);

//        valid &= super.validate(event);

        if (!purDocument.getRequisitionSourceCode().equals(PurapConstants.RequisitionSources.B2B)) {

            // CU enhancement here. Replaced foundation implementation block
            if ( (purDocument.getVendorHeaderGeneratedIdentifier() != null) &&  
               	 ( !(isDocumentInNodeWhereMopotDataValidationIsBypassed(purDocument))) &&             	             	 
                    (purDocument.getPurchaseOrderTransmissionMethodCode().equals(CUPurapConstants.POTransmissionMethods.EMAIL) ||
                     purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.FAX) || 
                     purDocument.getPurchaseOrderTransmissionMethodCode().equals(CUPurapConstants.POTransmissionMethods.MANUAL)) ) {            	
               	valid &= this.validateDataForMethodOfPOTransmissionExistsOnVendorAddress(purDocument);
               	//called routine took care of presenting error message to user
               }            
        }

        VendorDetail vendorDetail = getVendorService().getVendorDetail(purDocument.getVendorHeaderGeneratedIdentifier(), purDocument.getVendorDetailAssignedIdentifier());
        if (ObjectUtils.isNull(vendorDetail)) {
            return valid;
        }
        VendorHeader vendorHeader = vendorDetail.getVendorHeader();

        // make sure that the vendor is not debarred
        if (vendorDetail.isVendorDebarred()) {
            if (getParameterService().getParameterValueAsBoolean(KFSConstants.OptionalModuleNamespaces.PURCHASING_ACCOUNTS_PAYABLE, "Requisition", PurapParameterConstants.SHOW_DEBARRED_VENDOR_WARNING_IND)) {
                if (StringUtils.isEmpty(((PurchasingDocumentBase)purDocument).getJustification())) {
                    errorMap.putWarning(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.WARNING_DEBARRED_VENDOR, vendorDetail.getVendorName());
                    valid &= false;
                }
            } else {
                errorMap.putError(VendorPropertyConstants.VENDOR_NAME, PurapKeyConstants.ERROR_DEBARRED_VENDOR);
                valid &= false;
            }
        }

        // make sure that the vendor is of allowed type
        List<String> allowedVendorTypes = new ArrayList<String>( getParameterService().getParameterValuesAsString(KfsParameterConstants.PURCHASING_DOCUMENT.class, PurapRuleConstants.PURAP_VENDOR_TYPE_ALLOWED_ON_REQ_AND_PO) );
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

        //make sure that the vendor contract is active and not expired.
        // KFSUPGRADE-266 remove kfsmi-8690/kfscntrb-929
//        if (ObjectUtils.isNotNull(purDocument.getVendorContractGeneratedIdentifier())) {
//            VendorContract vendorContract = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(VendorContract.class, purDocument.getVendorContractGeneratedIdentifier());
//            Date currentDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
//
//            if (currentDate.compareTo(vendorContract.getVendorContractEndDate()) > 0 || !vendorContract.isActive()) {
//                valid &= false;
//                errorMap.putError(VendorPropertyConstants.VENDOR_CONTRACT_NAME, PurapKeyConstants.ERROR_INACTIVE_OR_EXPIRED_VENDOR_CONTRACT);
//            }
//        }
        
        // validate vendor address
        getPostalCodeValidationService().validateAddress(purDocument.getVendorCountryCode(), purDocument.getVendorStateCode(), purDocument.getVendorPostalCode(), PurapPropertyConstants.VENDOR_STATE_CODE, PurapPropertyConstants.VENDOR_POSTAL_CODE);

        errorMap.clearErrorPath();
        return valid;

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
        WorkflowDocument workflowDoc = purDocument.getDocumentHeader().getWorkflowDocument();
     //   List<String>  currentRouteLevels = Arrays.asList(Strings.split(purDocument.getDocumentHeader().getWorkflowDocument().getCurrentNodeNames(), ","));
        Set<String> currentRouteLevels = workflowDoc.getCurrentNodeNames();
        
        if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.ACCOUNT_NODE) && workflowDoc.isApprovalRequested() && !workflowDoc.getDocumentTypeName().equals(PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_AMENDMENT_DOCUMENT)) {
        	//added document not being a POA to conditional check due to FO being able to change data during account node approval on the POA and needed validation being bypassed 
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
        else if (currentRouteLevels.contains(CUPurapConstants.MethodOfPOTransmissionByPassValidationNodes.AD_HOC_NODE) && 
        		financialSystemWorkflowHelperService.isAdhocApprovalRequestedForPrincipal(workflowDoc, GlobalVariables.getUserSession().getPrincipalId())) {
        	//Doc could be editable OR NON-editable for a user when it is stopped on an AdHoc Route Node  
        	//bypass validation check when document is in adhoc route node and adhoc was requested
        	
        	return_value = true;
        }
        return (return_value);
    }  
        
	/**
	 * This routine verifies that the data necessary for the Method of PO Transmission chosen on the REQ, 
	 * PO, or POA document exists on the document's VendorAddress record for the chosen Vendor.    
	 * If the required checks pass, true is returned.
	 * If the required checks fail, false is returned.
	 * 
	 * NOTE: This routine could not be used for the VendorAddress validation checks on the Vendor maintenance 
	 * document because the Method of PO Transmission value selectable on that document pertains to the specific
	 * VendorAddress being maintained.  The method of PO transmission being used for this routine's validation
	 * checks is the one that is present on the input parameter purchasing document (REQ, PO, or POA) and could 
	 * be different from the value of the same name that is on the VendorAddress.  It is ok if these two values 
	 * are different because the user could have changed it after the default was obtained via the lookup and 
	 * used to populate the REQ, PO, or POA value as long as the data required for the method of PO transmission
	 * selected in that document exists on the VendorAddress record chosen on the REQ, PO, or POA. 
	 * 
	 * 	For KFSPTS-1458: This method was changed extensively to address new business rules.
	 */ 
	public boolean validateDataForMethodOfPOTransmissionExistsOnVendorAddress(Document document){
		boolean dataExists = true;		
		MessageMap errorMap = GlobalVariables.getMessageMap();
		errorMap.clearErrorPath(); 
		errorMap.addToErrorPath(PurapConstants.VENDOR_ERRORS);
		
		//for REQ, PO, and POA verify that data exists on form for method of PO transmission value selected
		if ((document instanceof RequisitionDocument) || (document instanceof PurchaseOrderDocument) || (document instanceof PurchaseOrderAmendmentDocument)) {
			PurchaseOrderTransmissionMethodDataRulesServiceImpl purchaseOrderTransmissionMethodDataRulesServiceImpl = SpringContext.getBean(PurchaseOrderTransmissionMethodDataRulesServiceImpl.class);
			PurchasingDocumentBase purapDocument = (PurchasingDocumentBase) document;
			String poTransMethodCode = purapDocument.getPurchaseOrderTransmissionMethodCode();
			if (poTransMethodCode != null && !StringUtils.isBlank(poTransMethodCode) ) {
				if (poTransMethodCode.equals(PurapConstants.POTransmissionMethods.FAX)) {
					dataExists = purchaseOrderTransmissionMethodDataRulesServiceImpl.isFaxNumberValid(purapDocument.getVendorFaxNumber());
					if (!dataExists) {
						errorMap.putError(VendorPropertyConstants.VENDOR_FAX_NUMBER, CUPurapKeyConstants.PURAP_MOPOT_REQUIRED_DATA_MISSING);						
					}
				}
				else if (poTransMethodCode.equals(CUPurapConstants.POTransmissionMethods.EMAIL)) {					
					dataExists = purchaseOrderTransmissionMethodDataRulesServiceImpl.isEmailAddressValid(purapDocument.getVendorEmailAddress());
					if (!dataExists) {
						errorMap.putError("vendorEmailAddress", CUPurapKeyConstants.PURAP_MOPOT_REQUIRED_DATA_MISSING);						
					}				
				}
				else if (poTransMethodCode.equals(CUPurapConstants.POTransmissionMethods.MANUAL)) {
					dataExists = purchaseOrderTransmissionMethodDataRulesServiceImpl.isPostalAddressValid(purapDocument.getVendorLine1Address(), purapDocument.getVendorCityName(), purapDocument.getVendorStateCode(), purapDocument.getVendorPostalCode(), purapDocument.getVendorCountryCode());
                    if (!dataExists) {
						errorMap.putError(VendorPropertyConstants.VENDOR_ADDRESS_LINE_1, CUPurapKeyConstants.PURAP_MOPOT_REQUIRED_DATA_MISSING);
                    }
				}					
			}
		}			
		return dataExists;
	}


	public FinancialSystemWorkflowHelperService getFinancialSystemWorkflowHelperService() {
		return financialSystemWorkflowHelperService;
	}


	public void setFinancialSystemWorkflowHelperService(
			FinancialSystemWorkflowHelperService financialSystemWorkflowHelperService) {
		this.financialSystemWorkflowHelperService = financialSystemWorkflowHelperService;
	}

	public void setVendorService(VendorService vendorService) {
		super.setVendorService(vendorService);
		this.vendorService = vendorService;
	}

	public void setParameterService(ParameterService parameterService) {
		super.setParameterService(parameterService);
		this.parameterService = parameterService;
	}

	public void setPostalCodeValidationService(PostalCodeValidationService postalCodeValidationService) {
		super.setPostalCodeValidationService(postalCodeValidationService);
		this.postalCodeValidationService = postalCodeValidationService;
	}

}
