package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.batch.businessobject.CemiSupplierParentIdentifiersReference;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplier;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierAddress;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierBankAccount;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierBankAccountSubEntry;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierChildren;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierEmail;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplierPhone;
import edu.cornell.kfs.vnd.batch.service.CemiSupplierDataBuilder;
import edu.cornell.kfs.vnd.util.VendorAccountFinder;

public abstract class CemiSupplierDataBuilderBase implements CemiSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected final CemiOutputDefinition outputDefinition;
    protected final LocalDateTime jobRunDate;
    protected final boolean maskSensitiveData;
    protected final DecimalFormat supplierIdFormatter;
    protected int vendorCount;
    
    protected CemiSupplierParentIdentifiersReference parentSupplierReference = null;
    

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
    public void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors,
                final VendorAccountFinder accountFinder) throws IOException {
        for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
            vendorCount++;
            if (vendorCount % 1000 == 0) {
                LOG.info("writeSupplierDataToIntermediateStorage, Writing {} Vendors and counting...", vendorCount);
            }
            final Collection<PayeeACHAccount> vendorAccounts = accountFinder.findAllActiveAccountsForVendor(
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());

            //Suppliers Tab
            final String supplierId = supplierIdFormatter.format(vendorCount);
            final CemiSupplier supplier = new CemiSupplier(vendor, supplierId, maskSensitiveData);
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
                    LOG.debug("writeSupplierDataToIntermediateStorage, vendorAddressGeneratedIdentifier {} for vendor {}-{} was NOT written to conversion file.", 
                            vendorAddress.getVendorAddressGeneratedIdentifier(),
                            vendor.getVendorHeaderGeneratedIdentifier(),
                            vendor.getVendorDetailAssignedIdentifier());
                }
            }
            
            // Emails Tab
            List<VendorAddress> vendorAddressesWithEmail = vendor.getVendorAddresses().stream()
                            .filter(VendorAddress::isActive)
                            .filter(a -> StringUtils.isNotBlank(a.getVendorAddressEmailAddress()))
                            .collect(Collectors.toList());
            if(vendorAddressesWithEmail.size() > 0) {
                final CemiSupplierEmail supplierEmail = new CemiSupplierEmail(vendor, supplierId);
                writeSupplierEmailRow(supplierEmail);
            }
            
            //Phones Tab
            //These should be the phone numbers tied to the actual vendor
            //These are NOT the phone numbers associated with the vendor contact list on the vendor record.
            writeAllSupplierPhoneRowsFor(vendor, supplierId);

            // Bank_Accounts Tab
            writeSupplierBankAccountsAsSingleRow(supplierId, vendor, vendorAccounts);

            //Children Tab
            writeSupplierChildrenRowWhenVendorIsChild(vendor, supplierId, getParentSupplierReference());
        }
        LOG.info("writeSupplierDataToIntermediateStorage, Finished writing {} Vendors", vendorCount);
    }

    protected void writeSupplierRow(final CemiSupplier supplier) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.SUPPLIER, supplier);
    }
    
    protected void writeSupplierAddressRow(final CemiSupplierAddress supplierAddress) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.ADDRESSES, supplierAddress);
    }
    
    protected void writeSupplierEmailRow(final CemiSupplierEmail supplierEmail) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.EMAILS, supplierEmail);
    }
    
    protected void writeSupplierPhoneRow(final CemiSupplierPhone supplierPhone) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.PHONES, supplierPhone);
    }
    
    protected void writeAllSupplierPhoneRowsFor(VendorDetail vendor, String supplierId) throws IOException {
        int phoneNumberCount = 0;
        for (final VendorPhoneNumber vendorPhoneNumber : vendor.getVendorPhoneNumbers()) {
            //Presuming phone numbers are US and NOT restricting by country
            if (vendorPhoneNumber.isActive()) {
                phoneNumberCount++;
                final CemiSupplierPhone supplierPhone = new CemiSupplierPhone(vendorPhoneNumber, supplierId, phoneNumberCount);
                writeSupplierPhoneRow(supplierPhone);
            } else {
                LOG.debug("writeAllSupplierPhoneRowsFor, vendorPhoneGeneratedIdentifier {} for vendor {}-{} was NOT written to conversion file.",
                        vendorPhoneNumber.getVendorPhoneGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
            }
        }
    }
    
    protected boolean currentVendorShouldBecomeParentVendor(VendorDetail currentVendor) {
        if (currentVendor.isVendorParentIndicator()) {
            return true;
        }
        return false;
    }
    
    protected void writeSupplierChildrenRow(final CemiSupplierChildren supplierChildren) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.CHILDREN, supplierChildren);
    }
    
    protected void writeSupplierChildrenRowWhenVendorIsChild(VendorDetail currentVendor, String currentSupplierId,
            CemiSupplierParentIdentifiersReference parentSupplierReference) throws IOException {
        
        if (ObjectUtils.isNotNull(parentSupplierReference)
                && !currentVendor.isVendorParentIndicator()
                && currentVendor.getVendorHeaderGeneratedIdentifier().equals(parentSupplierReference.getParentVendorHeaderGeneratedIdentifier())
                && !currentVendor.getVendorDetailAssignedIdentifier().equals(parentSupplierReference.getParentVendorDetailAssignedIdentifier())) {
            //child vendor has been detected
            final CemiSupplierChildren supplierChildren = new CemiSupplierChildren(currentSupplierId, parentSupplierReference.getParentSupplierId());
            writeSupplierChildrenRow(supplierChildren);
        } else {
            if (ObjectUtils.isNotNull(parentSupplierReference) ) {
                LOG.debug("writeSupplierChildrenRowWhenVendorIsChild, child vendor was not detected for "
                    + "currentVendor.getVendorHeaderGeneratedIdentifier {}  "
                    + "currentVendor.getVendorDetailAssignedIdentifier() {}  "
                    + "parentSupplierReference.getParentVendorHeaderGeneratedIdentifier() {}   "
                    + "parentSupplierReference.getParentVendorDetailAssignedIdentifier() {}",
                    currentVendor.getVendorHeaderGeneratedIdentifier(),
                    currentVendor.getVendorDetailAssignedIdentifier(),
                    parentSupplierReference.getParentVendorHeaderGeneratedIdentifier(),
                    parentSupplierReference.getParentVendorDetailAssignedIdentifier());
            } else {
                LOG.debug("writeSupplierChildrenRowWhenVendorIsChild, child vendor was not detected parentSupplierReference is NULL");
            }
        }
        
        if (currentVendorShouldBecomeParentVendor(currentVendor)) {
            setParentSupplierReference(new CemiSupplierParentIdentifiersReference(currentSupplierId, currentVendor.getVendorHeaderGeneratedIdentifier(), currentVendor.getVendorDetailAssignedIdentifier()));
        } else {
            LOG.debug("writeSupplierChildrenRowWhenVendorIsChild, currentVendorShouldBecomeParentVendor return false for "
                    + "currentVendor.getVendorHeaderGeneratedIdentifier {}  "
                    + "currentVendor.getVendorDetailAssignedIdentifier() {}  "
                    + "currentVendor.isVendorParentIndicator() {}"
                    + "parentSupplierReference.getParentVendorHeaderGeneratedIdentifier() {}   "
                    + "parentSupplierReference.getParentVendorDetailAssignedIdentifier() {}",
                    currentVendor.getVendorHeaderGeneratedIdentifier(),
                    currentVendor.getVendorDetailAssignedIdentifier(),
                    currentVendor.isVendorParentIndicator(),
                    parentSupplierReference.getParentVendorHeaderGeneratedIdentifier(),
                    parentSupplierReference.getParentVendorDetailAssignedIdentifier());
        }
    }

    protected void writeSupplierBankAccountsAsSingleRow(final String supplierId, final VendorDetail vendor,
            final Collection<PayeeACHAccount> vendorAccounts) throws IOException {
        final Stream.Builder<CemiSupplierBankAccountSubEntry> accountEntries = Stream.builder();
        int numAccounts = 0;

        for (final PayeeACHAccount vendorAccount : vendorAccounts) {
            if (!vendorAccount.isActive()) {
                LOG.warn("writeSupplierBankAccountsAsSingleRow, Payee ACH Account with ID {} for KFS Vendor {}-{} is "
                        + "inactive and will NOT be included; it should have been filtered out by the upstream query",
                        vendorAccount.getAchAccountGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
                continue;
            }
            numAccounts++;
            final CemiSupplierBankAccountSubEntry accountEntry = new CemiSupplierBankAccountSubEntry(
                    vendorAccount, supplierId, numAccounts);
            accountEntries.add(accountEntry);
        }

        if (numAccounts == 0) {
            LOG.debug("writeSupplierBankAccountsAsSingleRow, No active Payee ACH Accounts exist for KFS Vendor {}-{}; "
                    + "a corresponding Supplier Bank Account row will NOT be written",
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
            return;
        }

        while (numAccounts < CemiVendorConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES) {
            numAccounts++;
            accountEntries.add(CemiSupplierBankAccountSubEntry.EMPTY);
        }

        if (numAccounts > CemiVendorConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES) {
            LOG.warn("writeSupplierBankAccountsAsSingleRow, Found {} active Payee ACH Accounts for KFS Vendor {}-{}; "
                    + "only the first {} will be written",
                    numAccounts, vendor.getVendorHeaderGeneratedIdentifier(),
                    vendor.getVendorDetailAssignedIdentifier(), CemiVendorConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES);
        }

        final CemiSupplierBankAccount supplierAccount = new CemiSupplierBankAccount(supplierId, accountEntries.build());
        writeSupplierBankAccountRow(supplierAccount);
    }

    protected void writeSupplierBankAccountRow(final CemiSupplierBankAccount supplierAccount) throws IOException {
        writeDataToIntermediateStorage(CemiVendorConstants.SupplierExtractSheets.BANK_ACCOUNTS, supplierAccount);
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

    public CemiSupplierParentIdentifiersReference getParentSupplierReference() {
        return parentSupplierReference;
    }

    public void setParentSupplierReference(CemiSupplierParentIdentifiersReference parentSupplierReference) {
        this.parentSupplierReference = parentSupplierReference;
    }

}
