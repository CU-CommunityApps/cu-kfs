/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.vnd;

/**
 * Property name constants.
 */
public class CUVendorPropertyConstants {

	public static final String INSURANCE_REQUIRED = "extension.insuranceRequiredIndicator";
	
    public static final String GENERAL_LIABILITY_EXPIRATION = "extension.generalLiabilityExpiration";
    public static final String GENERAL_LIABILITY_AMOUNT = "extension.generalLiabilityCoverageAmount";
    
    public static final String AUTOMOBILE_LIABILITY_EXPIRATION = "extension.automobileLiabilityExpiration";
    public static final String AUTOMOBILE_LIABILITY_AMOUNT = "extension.automobileLiabilityCoverageAmount";
    
    public static final String WORKMANS_COMP_EXPIRATION = "extension.workmansCompExpiration";
    public static final String WORKMANS_COMP_AMOUNT = "extension.workmansCompCoverageAmount";
    
    public static final String EXCESS_LIABILITY_UMBRELLA_EXPIRATION = "extension.excessLiabilityUmbExpiration";
    public static final String EXCESS_LIABILITY_UMBRELLA_AMOUNT = "extension.excessLiabilityUmbrellaAmount";

    public static final String HEALTH_OFFSITE_LICENSE_EXPIRATION = "extension.healthOffSiteLicenseExpirationDate";
    public static final String HEALTH_OFFSITE_LICENSE_REQUIRED = "extension.healthOffSiteCateringLicenseReq";


    public static final String SUPPLIER_DIVERSITY_EXPRIATION = "extension.vendorSupplierDiversityExpirationDate";
 
    public static final String VENDOR_W9_RECEIVED_INDICATOR = "vendorHeader.vendorW9ReceivedIndicator";
    
    public static final String VENDOR_ADDRESS_METHOD_OF_PO_TRANSMISSION = "extension.purchaseOrderTransmissionMethodCode";
    public static final String VENDOR_ADDRESS_EMAIL_ADDRESS = "vendorAddressEmailAddress";
    public static final String VENDOR_ADDRESS_GENERATED_IDENTIFIER = "vendorAddressGeneratedIdentifier";

    public static final String VENDOR_HEADER_SUPPLIER_DIVERSITY_CODE = "vendorHeader.vendorSupplierDiversities.vendorSupplierDiversityCode";	

    public static final String VENDOR_PAYMENT_TERMS = "vendorPaymentTermsCode";

    public static final String VENDOR_CONTACT_PHONE_NUMBERS = "vendorContactPhoneNumbers";
}
