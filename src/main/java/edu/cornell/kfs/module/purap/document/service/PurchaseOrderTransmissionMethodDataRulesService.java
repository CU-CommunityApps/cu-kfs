package edu.cornell.kfs.module.purap.document.service;

public interface PurchaseOrderTransmissionMethodDataRulesService { 

	public boolean isFaxNumberValid(String faxNumber);
	
	public boolean isEmailAddressValid(String emailAddress);
	
	public boolean isCountryCodeValid(String countryCode);
	
	public boolean isStateCodeValid(String stateCode);
	
	public boolean isZipCodeValid(String zipCode);
	
	public boolean isAddress1Valid(String address1);
	
	public boolean isCityValid(String cityName);
	
	public boolean isPostalAddressValid(String address1, String cityName, String stateCode, String zipCode, String countryCode);

}
