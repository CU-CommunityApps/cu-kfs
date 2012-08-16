package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.PostalCodeValidationService; 
import org.kuali.kfs.vnd.VendorKeyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.MessageMap;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;


/**
 * This class contains all of the data checks needed to ensure that the data
 * exists for the Method of PO Transmission specified.
 * 
 *
 */
public class PurchaseOrderTransmissionMethodDataRulesServiceImpl implements PurchaseOrderTransmissionMethodDataRulesService{
	
	
	/**
	 * Returns false when faxNumber is null OR blank OR not is not in the PhoneNumberService.isValidPhoneNumber format;
	 * Otherwise, returns true.
	 */
	public boolean isFaxNumberValid(String faxNumber) {
		boolean dataIsValid = true; 
		if ( (faxNumber == null) || (StringUtils.isEmpty(faxNumber)) || (!SpringContext.getBean(PhoneNumberService.class).isValidPhoneNumber(faxNumber)) ) {
			dataIsValid &= false;
	    }
		return dataIsValid;		
	}
	
	/**
	 * Returns false when emailAddress is null OR blank OR does not contain an @ character;
	 * Otherwise, returns true.
	 */
	public boolean isEmailAddressValid(String emailAddress) {
		boolean dataIsValid = true;	
		if ( (emailAddress == null) || (StringUtils.isEmpty(emailAddress)) || (!StringUtils.contains(emailAddress, "@")) ) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	}        	
	
	
	/**
	 * Returns false when country code is null OR blank
	 * Otherwise, returns true.
	 */
	public boolean isCountryCodeValid(String countryCode) {
		boolean dataIsValid = true;	
		if ( (countryCode == null) || (StringUtils.isEmpty(countryCode))) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	} 
	
	/**
	 * Returns false when state code is null OR blank
	 * Otherwise, returns true.
	 */
	public boolean isStateCodeValid(String stateCode) {
		boolean dataIsValid = true;	
		if ( (stateCode == null) || (StringUtils.isEmpty(stateCode))) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	} 
	
	/**
	 * Returns false when zip/postal code is null OR blank
	 * Otherwise, returns true.
	 */
	public boolean isZipCodeValid(String zipCode) {
		boolean dataIsValid = true;	
		if ( (zipCode == null) || (StringUtils.isEmpty(zipCode))) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	} 

	/**
	 * Returns false when address 1 is null OR blank
	 * Otherwise, returns true.
	 */
	public boolean isAddress1Valid(String address1) {
		boolean dataIsValid = true;	
		if ( (address1 == null) || (StringUtils.isEmpty(address1))) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	}
	
	/**
	 * Returns false when city is null OR blank
	 * Otherwise, returns true.
	 */
	public boolean isCityValid(String cityName) {
		boolean dataIsValid = true;	
		if ( (cityName == null) || (StringUtils.isEmpty(cityName))) {
			dataIsValid &= false;
	    }
		return dataIsValid;	 
	}
	
	/**
	 * Returns false when any of the data items required for a US postal address (address 1, city, state, postal code, and country) 
	 * are null or blank OR PostalCodeValidationService fails; Otherwise, returns true. 
	 */
	public boolean isPostalAddressValid(String address1, String cityName, String stateCode, String zipCode, String countryCode) {
		boolean dataIsValid = true;
		
		if (!this.isCountryCodeValid(countryCode) || !this.isStateCodeValid(stateCode) || !this.isZipCodeValid(zipCode) || !this.isAddress1Valid(address1) || !this.isCityValid(cityName) || 
			!SpringContext.getBean(PostalCodeValidationService.class).validateAddress(countryCode, stateCode, zipCode, "", "")) {
			dataIsValid &= false;
		}
		return dataIsValid;		
	}
	
	
	
	/**
	 * This routine verifies that the address type selected by the user is PO as well as ensuring that the
	 * data necessary for the Method of PO Transmission chosen on the REQ, PO, or POA document exists on the 
	 * document's VendorAddress record for the chosen Vendor.    
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
	 */ 
	public boolean validateDataForMethodOfPOTransmissionExistsOnVendorAddress(Document document){
		boolean dataExists = true;		
		MessageMap errorMap = GlobalVariables.getMessageMap();
		errorMap.clearErrorPath(); 
		errorMap.addToErrorPath(PurapConstants.VENDOR_ERRORS);
	
		//for REQ verify that vendor address type chosen is a PO address type
		if (document instanceof RequisitionDocument) {
			PurchasingDocumentBase purapDocument = (PurchasingDocumentBase) document;
			dataExists &= this.isVendorAddressPOAddressType(purapDocument.getVendorAddressGeneratedIdentifier());
			if (!dataExists) {
				//address chosen is not a PO address type
				errorMap.putError(VendorPropertyConstants.VENDOR_ADDRESS_LINE_1, CUPurapKeyConstants.PURAP_VENDOR_ADDRES_TYPE_NOT_PO);	
			}
		}
		
		//for REQ, PO, and POA verify that data exists on form for method of PO transmission value selected
		if ((document instanceof RequisitionDocument) || (document instanceof PurchaseOrderDocument) || (document instanceof PurchaseOrderAmendmentDocument)) {
			PurchasingDocumentBase purapDocument = (PurchasingDocumentBase) document;
			dataExists &= this.isVendorAddressPOAddressType(purapDocument.getVendorAddressGeneratedIdentifier());
			String poTransMethodCode = purapDocument.getPurchaseOrderTransmissionMethodCode();
			if (poTransMethodCode != null && !StringUtils.isBlank(poTransMethodCode) ) {
				if (poTransMethodCode.equals(PurapConstants.POTransmissionMethods.FAX)) {
					dataExists = isFaxNumberValid(purapDocument.getVendorFaxNumber());
					if (!dataExists) {
						errorMap.putError(VendorPropertyConstants.VENDOR_FAX_NUMBER, CUPurapKeyConstants.PURAP_VENDOR_ADDRESS_FAX_NUMBER_MISSING);						
					}
				}
				else if (poTransMethodCode.equals(PurapConstants.POTransmissionMethods.EMAIL)) {
					//lookup vendor address in database to get email address and then validate email address
					dataExists = isEmailAddressValid(this.getVendorAddressEmailAddressForVendorAddressId(purapDocument.getVendorAddressGeneratedIdentifier()));					
                    if (!dataExists) {
						errorMap.putError(VendorPropertyConstants.VENDOR_ADDRESS_EMAIL_ADDRESS, CUPurapKeyConstants.PURAP_VENDOR_ADDRESS_EMAIL_ADDRESS_MISSING);					
                    }
				}	
				else if (poTransMethodCode.equals(PurapConstants.POTransmissionMethods.MANUAL)) {
					dataExists = isPostalAddressValid(purapDocument.getVendorLine1Address(), purapDocument.getVendorCityName(), purapDocument.getVendorStateCode(), purapDocument.getVendorPostalCode(), purapDocument.getVendorCountryCode());
                    if (!dataExists) {
						errorMap.putError(VendorPropertyConstants.VENDOR_ADDRESS_LINE_1, VendorKeyConstants.ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL);
                    }
				}					
			}
		}			
		errorMap.clearErrorPath();
		return dataExists;
	}
	
	/**
	 * Return the vendor address email address value for the vendor address represented by the input parameter. 
	 * Vendor address email address is not an attribute on the Vendor Address of the purchasing documents.
	 */
	private String getVendorAddressEmailAddressForVendorAddressId(Integer vendorAddressGeneratedIdentifier){
		String emailAddress = null;
		if (vendorAddressGeneratedIdentifier != null) {
			VendorAddress vendorAddress = null;
			Map criteria = new HashMap();
			criteria.put(VendorPropertyConstants.VENDOR_ADDRESS_GENERATED_IDENTIFIER, vendorAddressGeneratedIdentifier);
			BusinessObjectService boSevice = SpringContext.getBean(BusinessObjectService.class);
			vendorAddress = (VendorAddress) boSevice.findByPrimaryKey(VendorAddress.class, criteria);
			if (vendorAddress != null){
				emailAddress = vendorAddress.getVendorAddressEmailAddress();
			}				
		}
		return emailAddress;
	}
	
	/**
	 * Return true when input parameter represents a PO vendor address type; 
	 * otherwise return false
	 */
	private boolean isVendorAddressPOAddressType(Integer vendorAddressGeneratedIdentifier){
		boolean idIsPOAddress = false;
		if (vendorAddressGeneratedIdentifier != null) {
			VendorAddress vendorAddress = null;
			Map criteria = new HashMap();
			criteria.put(VendorPropertyConstants.VENDOR_ADDRESS_GENERATED_IDENTIFIER, vendorAddressGeneratedIdentifier);
			BusinessObjectService boSevice = SpringContext.getBean(BusinessObjectService.class);
			vendorAddress = (VendorAddress) boSevice.findByPrimaryKey(VendorAddress.class, criteria);
			String addressTypeCode = vendorAddress.getVendorAddressType().getVendorAddressTypeCode();
			if ( (addressTypeCode != null) && (!addressTypeCode.isEmpty()) && (addressTypeCode.equals(KFSConstants.FinancialDocumentTypeCodes.PURCHASE_ORDER)) ){
				idIsPOAddress= true;
			}
		}		
		return idIsPOAddress;
	} 
}
