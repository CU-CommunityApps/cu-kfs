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

import org.kuali.kfs.vnd.VendorConstants;


/**
 * Holds constants for Vendor.
 */
public class CUVendorConstants extends VendorConstants {

    public static final String EXPIRED_DATE_QUESTION_ID = "confirmVendorDateExpiration";

    public static class EXPIRABLE_COVERAGES {
	    public static final String GENERAL_LIABILITY_COVERAGE = "General Liability Coverage";
	    public static final String AUTOMOBILE_LIABILITY_COVERAGE = "Automobile Liability Coverage";
	    public static final String WORKMAN_COMP_COVERAGE = "Workman's Compensation Coverage";
	    public static final String EXCESS_LIABILITY_UMBRELLA_COVERAGE = "Excess Liability Umbrella Policy";
	    public static final String HEALTH_DEPARTMENT_LICENSE ="Health Department License";
	    public static final String SUPPLIER_DIVERSITY_CERTIFICATION = "Supplier Diversity Certification";
    }

}
