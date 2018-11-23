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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.kuali.kfs.vnd.VendorConstants;


/**
 * Holds constants for Vendor.
 */
public class CUVendorConstants extends VendorConstants {
	public static final String VENDOR_DOCUMENT_TYPE_NAME = "PVEN";
    public static final String EXPIRED_DATE_QUESTION_ID = "confirmVendorDateExpiration";

    public static class EXPIRABLE_COVERAGES {
	    public static final String GENERAL_LIABILITY_COVERAGE = "General Liability Coverage";
	    public static final String AUTOMOBILE_LIABILITY_COVERAGE = "Automobile Liability Coverage";
	    public static final String WORKMAN_COMP_COVERAGE = "Workman's Compensation Coverage";
	    public static final String EXCESS_LIABILITY_UMBRELLA_COVERAGE = "Excess Liability Umbrella Policy";
	    public static final String HEALTH_DEPARTMENT_LICENSE ="Health Department License";
	    public static final String SUPPLIER_DIVERSITY_CERTIFICATION = "Supplier Diversity Certification";
    }
    
        
    public static class CUAddressTypes extends AddressTypes{
        public static final String TAX = "TX";
    }

    public static final String LOCALE_LOCAL = "LOCAL";
    public static final String LOCALE_REGIONAL = "REGIONAL";
    public static final String LOCALE_LOCAL_LABEL = "Local";
    public static final String LOCALE_REGIONAL_LABEL = "Regional";

    public static final String PROC_METHOD_PO = "PO";
    public static final String PROC_METHOD_PCARD = "PCARD";
    public static final String PROC_METHOD_ESHOP = "ESHOP";
    public static final String PROC_METHOD_DV = "DV";
    public static final String PROC_METHOD_FTC_BSC_PCARD = "FTC_BSC_PCARD";
    public static final String PROC_METHOD_PO_LABEL = "Purchase Order";
    public static final String PROC_METHOD_PCARD_LABEL = "Pcard";
    public static final String PROC_METHOD_ESHOP_LABEL = "e-SHOP";
    public static final String PROC_METHOD_DV_LABEL = "Disbursement Voucher";
    public static final String PROC_METHOD_FTC_BSC_PCARD_LABEL = "FTC/BSC Pcard";

    public static final Map<String, String> PROC_METHODS_LABEL_MAP;
    public static final Map<String, String> LOCALES_LABEL_MAP;



    static {
        // Build the label maps as "linked" ones to preserve insertion order, to help with using these maps in values finders.
        Map<String, String> labelsMap = new LinkedHashMap<String, String>();
        labelsMap.put(PROC_METHOD_PO, PROC_METHOD_PO_LABEL);
        labelsMap.put(PROC_METHOD_PCARD, PROC_METHOD_PCARD_LABEL);
        labelsMap.put(PROC_METHOD_ESHOP, PROC_METHOD_ESHOP_LABEL);
        labelsMap.put(PROC_METHOD_DV, PROC_METHOD_DV_LABEL);
        labelsMap.put(PROC_METHOD_FTC_BSC_PCARD, PROC_METHOD_FTC_BSC_PCARD_LABEL);
        PROC_METHODS_LABEL_MAP = Collections.unmodifiableMap(labelsMap);
        
        labelsMap = new LinkedHashMap<String, String>();
        labelsMap.put(LOCALE_LOCAL, LOCALE_LOCAL_LABEL);
        labelsMap.put(LOCALE_REGIONAL, LOCALE_REGIONAL_LABEL);
        LOCALES_LABEL_MAP = Collections.unmodifiableMap(labelsMap);
    }
    
    public static final int MAX_VENDOR_CONTACT_NAME_LENGTH = 45;
}
