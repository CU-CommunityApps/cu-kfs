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

import org.kuali.kfs.vnd.VendorKeyConstants;

/**
 * Holds error key constants for Vendor.
 */
public class CUVendorKeyConstants extends VendorKeyConstants {

    // Vendor Maintenance
    public static final String CONFIRM_VENDOR_DATE_EXPIRED = "message.vendorMaint.confirm.expired.date";
    
    //Vendor Maintenance W9 Received Date
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_NOT_POPULATED = "error.document.vendor.w9ReceivedNotPopulatedButIndicatorIsTrue";
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_POPULATED_W_O_IND = "error.document.vendor.w9ReceivedPopulatedButIndicatorIsNull";
    
    //Vendor Maintenance W9 Received Date
    public static final String ERROR_DOCUMENT_VNDMAINT_W8BENRECEIVED_NOT_POPULATED = "error.document.vendor.w8BENReceivedNotPopulatedButIndicatorIsTrue";
    public static final String ERROR_DOCUMENT_VNDMAINT_W8BENRECEIVED_POPULATED_W_O_IND = "error.document.vendor.w8BENReceivedPopulatedButIndicatorIsNull";
    
    //Vendor Maintenance Supplier Diversity Expiration Date
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST = "error.document.vendor.supplierDiversityExpirationDateIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK = "error.document.vendor.supplierDiversityExpirationDateCannotBeBlank";
    
    //Vendor Maintenance Insurance Tracking
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_NEEDED = "error.document.vendor.generalLiabilityExpirationDateNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_IN_PAST = "error.document.vendor.generalLiabilityExpirationDateIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_COVERAGE_NEEDED = "error.document.vendor.generalLiabilityCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_NEEDED = "error.document.vendor.automobileExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_IN_PAST = "error.document.vendor.automobileExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_COVERAGE_NEEDED = "error.document.vendor.automobileCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_EXPR_NEEDED = "error.document.vendor.workmansCompExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_EXPR_IN_PAST = "error.document.vendor.workmansCompExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_COVERAGE_NEEDED = "error.document.vendor.workmansCompCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_NEEDED = "error.document.vendor.umbrellaExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_IN_PAST = "error.document.vendor.umbrellaExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_COVERAGE_NEEDED = "error.document.vendor.umbrellaCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_NEEDED = "error.document.vendor.healthLicenseExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_IN_PAST = "error.document.vendor.healthLicenseExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_NEEDED = "error.document.vendor.healthLicenseNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_INSURANCE_REQUIRED_USED_WO_DATA = "error.document.vendor.insuranceReqIndicatedWOData";
    
    //Vendor Maintenance Credit Merchant
    
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_DUPLICATE = "error.document.vendor.creditMerchantNameDuplicate";
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_BLANK = "error.document.vendor.creditMerchantNameBlank";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST = "error.document.vendor.dateInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_FUTURE = "error.document.vendor.dateInFuture";
    // Vendor Maintenance Commodity Code
    public static final String ERROR_VENDOR_COMMODITY_CODE_REQUIRE_ONE_DEFAULT_IND = "error.vendorCommodityCode.require.one.defaultIndicator";
    public static final String ERROR_VENDOR_COMMODITY_CODE_IS_REQUIRED_FOR_THIS_VENDOR_TYPE = "error.vendorCommodityCode.is.required.for.vendorType";
    public static final String ERROR_VENDOR_COMMODITY_CODE_DEFAULT_IS_REQUIRED_FOR_B2B = "error.vendorCommodityCode.default.required.for.b2b";
    public static final String ERROR_VENDOR_COMMODITY_CODE_DOES_NOT_EXIST = "error.vendorCommodityCode.nonExistance";
    public static final String ERROR_DEFAULT_VENDOR_COMMODITY_CODE_ALREADY_EXISTS = "error.vendorCommodityCode.defaultAlreadySelected";
    public static final String ERROR_VENDOR_COMMODITY_CODE_ALREADY_ASSIGNED_TO_VENDOR = "error.vendorCommodityCode.duplicateCommodityCode";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_PO_ADDRESS = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.missing";
    public static final String ERROR_PO_TRANMSISSION_NOT_ALLOWED_FOR_VENDOR_TYPE = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.notAllowed";
    public static final String ERROR_NO_PO_TRANSMISSION_WITH_NON_PO_ADDRESS = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.specified";
    public static final String ERROR_PO_TRANSMISSION_METHOD_UNKNOWN = "error.vendorMaint.poTransmissionMethodActionUnknown";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_FAX_NUMBER = "error.vendorMaint.vendorAddress.faxNumberMissing";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_EMAIL = "error.vendorMaint.vendorAddress.emailAddressMissing";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL = "error.vendorMaint.vendorAddress.USPostalAddressMissing";

    public static final String ERROR_DOCUMENT_VENDOR_TYPE_IS_REQUIRED_FOR_ADD_VENDORADRESS = "error.document.vendortype.isrequired.for.add.vendoraddress";
}
