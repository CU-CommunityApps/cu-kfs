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

import org.kuali.kfs.vnd.VendorPropertyConstants;

/**
 * Property name constants.
 */
public class CUVendorPropertyConstants extends VendorPropertyConstants {

	public static final String INSURANCE_REQUIRED = "insuranceRequiredIndicator";
	
    public static final String GENERAL_LIABILITY_EXPIRATION = "generalLiabilityExpiration";
    public static final String GENERAL_LIABILITY_AMOUNT = "generalLiabilityCoverageAmount";
    
    public static final String AUTOMOBILE_LIABILITY_EXPIRATION = "automobileLiabilityExpiration";
    public static final String AUTOMOBILE_LIABILITY_AMOUNT = "automobileLiabilityCoverageAmount";
    
    public static final String WORKMANS_COMP_EXPIRATION = "workmansCompExpiration";
    public static final String WORKMANS_COMP_AMOUNT = "workmansCompCoverageAmount";
    
    public static final String EXCESS_LIABILITY_UMBRELLA_EXPIRATION = "excessLiabilityUmbExpiration";
    public static final String EXCESS_LIABILITY_UMBRELLA_AMOUNT = "excessLiabilityUmbrellaAmount";

    public static final String HEALTH_OFFSITE_LICENSE_EXPIRATION = "healthOffSiteLicenseExpirationDate";
    public static final String HEALTH_OFFSITE_LICENSE_REQUIRED = "healthOffSiteCateringLicenseReq";


    public static final String SUPPLIER_DIVERSITY_EXPRIATION = "vendorSupplierDiversityExpirationDate";
    public static final String VENDOR_W9_RECEIVED_DATE = "vendorHeader.extension.vendorW9ReceivedDate";
 
    public static final String VENDOR_W9_RECEIVED_INDICATOR = "vendorHeader.vendorW9ReceivedIndicator";
    
    public static final String VENDOR_ADDRESS_METHOD_OF_PO_TRANSMISSION = "purchaseOrderTransmissionMethodCode";
    public static final String VENDOR_ADDRESS_EMAIL_ADDRESS = "vendorAddressEmailAddress";
    public static final String VENDOR_ADDRESS_GENERATED_IDENTIFIER = "vendorAddressGeneratedIdentifier";

    public static final String VENDOR_HEADER_SUPPLIER_DIVERSITY_CODE = "vendorHeader.vendorSupplierDiversities.vendorSupplierDiversityCode";	
        
	
}
