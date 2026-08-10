package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;


public interface CemiSupplierFileExtractDataBuilder {
    
    void writeSupplierFileSupplierTabExtractDataToIntermediateStorage(final Iterator<VendorDetail> vendors);
    
    void writeSupplierFileSupplierAddressTabExtractDataToIntermediateStorage(final Iterator<VendorAddress> vendorAddresses);
    
    void writeSupplierFileSupplierEmailTabExtractDataToIntermediateStorage(final Iterator<VendorAddress> vendorAddresses);
    
    void writeSupplierFileSupplierPhoneTabExtractDataToIntermediateStorage(final Iterator<VendorAddress> vendorAddresses);
    
    void writeSupplierFileSupplierBankAccountsTabExtractDataToIntermediateStorage(final Iterator<PayeeACHAccount> payeeACHAccounts);
    
    void writeSupplierFileSupplierChildrenTabExtractDataToIntermediateStorage(final Iterator<VendorDetail> vendors);
}
