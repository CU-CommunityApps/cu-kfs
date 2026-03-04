package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplier;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierAddress;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierPhone;
import edu.cornell.kfs.vnd.batch.service.CemiSupplierDataBuilder;

public abstract class CemiSupplierDataBuilderBase implements CemiSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;
    protected final boolean maskSensitiveData;
    protected final DecimalFormat supplierIdFormatter;
    protected int vendorCount;

    protected CemiSupplierDataBuilderBase(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate, final boolean maskSensitiveData) {
        Validate.notNull(outputDefinition, "outputDefinition cannot be null");
        Validate.notNull(jobRunDate, "jobRunDate cannot be null");
        this.outputDefinition = outputDefinition;
        this.jobRunDate = jobRunDate;
        this.maskSensitiveData = maskSensitiveData;
        this.supplierIdFormatter = new DecimalFormat(CemiVendorConstants.SUPPLIER_ID_FORMAT);
    }

    /*
     * NOTE: It is assumed that when the iterator returns a parent vendor, the subsequent iterations
     * will return ALL of its child vendors (if any) BEFORE returning the next unrelated parent vendor.
     */
    @Override
    public void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors) throws IOException {
        for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
            vendorCount++;
            if (vendorCount % 1000 == 0) {
                LOG.info("writeSupplierDataToIntermediateStorage, Writing {} Vendors and counting...", vendorCount);
            }
            //Suppliers Tab
            final String supplierId = supplierIdFormatter.format(vendorCount);
            final CemiSupplier supplier = new CemiSupplier(vendor, supplierId);
            writeSupplierRow(supplier);
            
            //Addresses Tab
            int addressCount = 0;
            for (final VendorAddress vendorAddress : vendor.getVendorAddresses()) {
                //Restricting addresses by country = US
                if (vendorAddress.isActive() && 
                        vendorAddress.getVendorCountryCode().equalsIgnoreCase(CemiVendorConstants.COUNTRY_CODE_UNITED_STATES)) {
                    addressCount++;
                    final CemiSupplierAddress supplierAddress = new CemiSupplierAddress(vendor.getVendorHeader().getVendorTypeCode(), vendorAddress, supplierId, addressCount);
                    writeSupplierAddressRow(supplierAddress);
                } else {
                    LOG.info("writeSupplierDataToIntermediateStorage, vendorAddressGeneratedIdentifier {} for vendor {}-{} was NOT written to conversion file.", 
                            vendorAddress.getVendorAddressGeneratedIdentifier(),
                            vendor.getVendorHeaderGeneratedIdentifier(),
                            vendor.getVendorDetailAssignedIdentifier());
                }
            }
            
            //Phones Tab
            //These should be the phone numbers tied to the actual vendor
            //These are NOT the phone numbers associated with the vendor contact list on the vendor record.
            writeAllSupplierPhoneRows(vendor, supplierId);
        }
        LOG.info("writeSupplierDataToIntermediateStorage, Finished writing {} Vendors", vendorCount);
    }

    protected void writeSupplierRow(final CemiSupplier supplier) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.SUPPLIER, supplier);
    }
    
    protected void writeSupplierAddressRow(final CemiSupplierAddress supplierAddress) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.ADDRESSES, supplierAddress);
    }
    
    protected void writeSupplierPhoneRow(final CemiSupplierPhone supplierPhone) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.PHONES, supplierPhone);
    }
    
    protected void writeAllSupplierPhoneRows(VendorDetail vendor, String supplierId) throws IOException {
        int phoneNumberCount = 0;
        for (final VendorPhoneNumber vendorPhoneNumber : vendor.getVendorPhoneNumbers()) {
            //Presuming phone numbers are US and NOT restricting by country
            if (vendorPhoneNumber.isActive()) {
                phoneNumberCount++;
                final CemiSupplierPhone supplierPhone = new CemiSupplierPhone(vendorPhoneNumber, supplierId, phoneNumberCount);
                writeSupplierPhoneRow(supplierPhone);
            } else {
                LOG.info("writeSupplierDataToIntermediateStorage, vendorPhoneGeneratedIdentifier {} for vendor {}-{} was NOT written to conversion file.",
                        vendorPhoneNumber.getVendorPhoneGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
            }
        }
    }
    

    /*
     * The subclass that writes the vendor data to the temp tables needs to implement this method.
     * If desired, the implementation can keep connections/files/etc. open until close() is called.
     * See the CSV implementation for an example.
     */
    protected abstract void writeDataToIntermediateStorage(
            final String sheetName, final Object rowObject) throws IOException;

    // The temp table implementation can use (or override) this method to retrieve the column value to be inserted.
    protected String getFieldValue(final CemiFieldDefinition field, final Object rowObject) {
        switch (field.getType()) {
            case STATIC:
                return field.getValue();

            case STRING:
                return (String) ObjectUtils.getPropertyValue(rowObject, field.getKey());

            default:
                throw new IllegalStateException("Unknown field type: " + field.getType());
        }
    }

}
