/*
 * Copyright 2008 The Kuali Foundation
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
package edu.cornell.kfs.vnd.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.vnd.businessobject.VendorBatchDetail;

/**
 * 
 * Used to convert parsed csv data to List of Vendor Batch data
 *
 */
public class VendorBatchCsvBuilder {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(VendorBatchCsvBuilder.class);

    private static final String DOUBLE_QUOTE = "\"";
    /**
     * Convert the parseData object into VendorBatch BO 
     * 
     * @param parseDataList
     * @return
     */
    public static List<VendorBatchDetail> buildVendorBatch(List<Map<String, String>> parseDataList) {
        List<VendorBatchDetail> vendors = new ArrayList<VendorBatchDetail>();
        for (Map<String, String> rowDataMap : parseDataList) {
            vendors.add(buildVendorsFromDataMap(rowDataMap));
        }
        return vendors;
    }

     
    /**
     * build the VendorBatch BO from the data row
     * 
     * @param rowDataMap
     * @return
     */
    private static VendorBatchDetail buildVendorsFromDataMap(Map<String, String> rowDataMap) {

    	VendorBatchDetail vendor = new VendorBatchDetail();
        vendor.setVendorNumber(rowDataMap.get(VendorBatchCsv.vendorNumber.name()));        
        vendor.setVendorName((rowDataMap.get(VendorBatchCsv.vendorName.name())));        
        vendor.setLegalFirstName(rowDataMap.get(VendorBatchCsv.legalFirstName.name()));        
        vendor.setLegalLastName(rowDataMap.get(VendorBatchCsv.legalLastName.name()));        
        vendor.setVendorTypeCode(rowDataMap.get(VendorBatchCsv.vendorTypeCode.name()));        
        vendor.setForeignVendor(rowDataMap.get(VendorBatchCsv.foreignVendor.name()));        
        vendor.setTaxNumber(rowDataMap.get(VendorBatchCsv.taxNumber.name()));        
        vendor.setTaxNumberType(rowDataMap.get(VendorBatchCsv.taxNumberType.name()));        
        vendor.setOwnershipTypeCode(rowDataMap.get(VendorBatchCsv.ownershipTypeCode.name()));        
        vendor.setDefaultB2BPaymentMethodCode(rowDataMap.get(VendorBatchCsv.defaultB2BPaymentMethodCode.name()));        
        vendor.setTaxable(rowDataMap.get(VendorBatchCsv.taxable.name()));        
        vendor.seteInvoice(rowDataMap.get(VendorBatchCsv.eInvoice.name()));        
        vendor.setAddresses(filterQuote(rowDataMap.get(VendorBatchCsv.addresses.name())));        
        vendor.setContacts(filterQuote(rowDataMap.get(VendorBatchCsv.contacts.name())));        
        vendor.setPhoneNumbers(filterQuote(rowDataMap.get(VendorBatchCsv.phoneNumbers.name())));        
        vendor.setSupplierDiversities(filterQuote(rowDataMap.get(VendorBatchCsv.supplierDiversities.name())));        
        vendor.setInsuranceTracking(filterQuote(rowDataMap.get(VendorBatchCsv.insuranceTracking.name())));        
        vendor.setNotes(filterQuote(rowDataMap.get(VendorBatchCsv.notes.name())));        
        vendor.setAttachmentFiles(filterQuote(rowDataMap.get(VendorBatchCsv.attachmentFiles.name())));        
        return vendor;
    }


    private static String filterQuote(String fieldData) {
    	if (fieldData.startsWith(DOUBLE_QUOTE)) {
    	    fieldData = fieldData.replaceAll(DOUBLE_QUOTE, KFSConstants.EMPTY_STRING);
    	} 
    	return fieldData;

    }
}
