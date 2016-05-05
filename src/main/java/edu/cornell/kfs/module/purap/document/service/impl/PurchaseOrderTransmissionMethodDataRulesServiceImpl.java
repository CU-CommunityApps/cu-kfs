package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;


/**
 * This class contains all of the data checks needed to ensure that the data
 * exists for the Method of PO Transmission specified.
 * 
 *
 */
public class PurchaseOrderTransmissionMethodDataRulesServiceImpl implements PurchaseOrderTransmissionMethodDataRulesService{
	
	private PostalCodeValidationService postalCodeValidationService;
	private PhoneNumberService phoneNumberService;
	
	/**
	 * Returns false when faxNumber is null OR blank OR not is not in the PhoneNumberService.isValidPhoneNumber format;
	 * Otherwise, returns true.
	 */
	public boolean isFaxNumberValid(String faxNumber) {
		boolean dataIsValid = true; 
		if ( (faxNumber == null) || (StringUtils.isEmpty(faxNumber)) || (!getPhoneNumberService().isValidPhoneNumber(faxNumber)) ) {
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
			!getPostalCodeValidationService().validateAddress(countryCode, stateCode, zipCode, "", "")) {
			dataIsValid &= false;
		}
		return dataIsValid;		
	}

	public PostalCodeValidationService getPostalCodeValidationService() {
		return postalCodeValidationService;
	}

	public void setPostalCodeValidationService(PostalCodeValidationService postalCodeValidationService) {
		this.postalCodeValidationService = postalCodeValidationService;
	}

	public PhoneNumberService getPhoneNumberService() {
		return phoneNumberService;
	}

	public void setPhoneNumberService(PhoneNumberService phoneNumberService) {
		this.phoneNumberService = phoneNumberService;
	}
	
}
