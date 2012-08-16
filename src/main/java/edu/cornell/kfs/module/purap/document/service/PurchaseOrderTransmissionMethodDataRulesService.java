package edu.cornell.kfs.module.purap.document.service;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.rice.kns.document.Document;


public interface PurchaseOrderTransmissionMethodDataRulesService { 

	public boolean isFaxNumberValid(String faxNumber);
	
	public boolean isEmailAddressValid(String emailAddress);
	
	public boolean isCountryCodeValid(String countryCode);
	
	public boolean isStateCodeValid(String stateCode);
	
	public boolean isZipCodeValid(String zipCode);
	
	public boolean isAddress1Valid(String address1);
	
	public boolean isCityValid(String cityName);
	
	public boolean isPostalAddressValid(String address1, String cityName, String stateCode, String zipCode, String countryCode);
	
	public boolean validateDataForMethodOfPOTransmissionExistsOnVendorAddress(Document document);

}
