package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardScheduleFileAwardScheduleTabRowBo;
import edu.cornell.kfs.cemi.module.cg.batch.service.impl.CemiAwardScheduleFileAwardScheduleTabRowBoFactory;
import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.dto.CemiSupplier;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierFileExtractDataBuilder;
import edu.cornell.kfs.cemi.vnd.util.VendorAccountFinder;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;

public class CemiSupplierFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase implements CemiSupplierFileExtractDataBuilder{

    private static final Logger LOG = LogManager.getLogger();
    
    protected DateTimeService dateTimeService;
    protected final boolean maskSensitiveData;
    protected final DecimalFormat supplierIdFormatter;
    protected int vendorCount;
    
    protected CemiSupplierFileExtractDataBuilderDefaultImpl(final BusinessObjectService businessObjectService, final String jobRunDateString,
            final DateTimeService dateTimeService,
            final boolean maskSensitiveData) { 
        super(businessObjectService, jobRunDateString);
        Validate.notNull(dateTimeService, "dateTimeService cannot be null");
        this.supplierIdFormatter = new DecimalFormat(CemiSupplierConstants.SUPPLIER_ID_FORMAT);
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
    }
    
//    public void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors,
//            final VendorAccountFinder accountFinder, final LocalDateTime jobRunDate) throws IOException {
//    for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
//        vendorCount++;
//        if (vendorCount % 1000 == 0) {
//            LOG.info("writeSupplierDataToIntermediateStorage, Writing {} Vendors and counting...", vendorCount);
//        }
//        final Collection<PayeeACHAccount> vendorAccounts = accountFinder.findAllActiveAccountsForVendor(
//                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
//
//        //Suppliers Tab
//        final String supplierId = supplierIdFormatter.format(vendorCount);
//        final CemiSupplier supplier = new CemiSupplier(vendor, supplierId, maskSensitiveData);
//        writeSupplierFileSupplierTabExtractDataToIntermediateStorage(supplier);
//        
//        //Record identifier associations for Supplier extract file based upon batch job run date
//        recordSupplierIdentifiersInLegacyAssociationTable(supplierId, vendor.getVendorHeaderGeneratedIdentifier(),
//                        vendor.getVendorDetailAssignedIdentifier(), jobRunDate);
//        
//        //Addresses Tab
//        writeAllSupplierAddressRowsFor(vendor, supplierId);
//        
//        // Emails Tab
//        writeSupplierEmailsAsSingleRow(vendor, supplierId);
//        
//        //Phones Tab
//        //These should be the phone numbers tied to the actual vendor
//        //These are NOT the phone numbers associated with the vendor contact list on the vendor record.
//        writeAllSupplierPhoneRowsFor(vendor, supplierId);
//
//        // Bank_Accounts Tab
//        writeSupplierBankAccountsAsSingleRow(supplierId, vendor, vendorAccounts);
//
//        //Children Tab
//        writeSupplierChildrenRowWhenVendorIsChild(vendor, supplierId, getParentSupplierReference());
//    }
//    LOG.info("writeSupplierDataToIntermediateStorage, Finished writing {} Vendors", vendorCount);
//}

    @Override
    public void writeSupplierFileSupplierTabExtractDataToIntermediateStorage(Iterator<VendorDetail> vendors) {
        for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
            vendorCount++;
            if (vendorCount % 1000 == 0) {
                LOG.info("writeSupplierDataToIntermediateStorage, Writing {} Vendors and counting...", vendorCount);
            }
            final Collection<PayeeACHAccount> vendorAccounts = findAllActiveAccountsForVendor(
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());

            //Suppliers Tab
            final String supplierId = supplierIdFormatter.format(vendorCount);
            final CemiSupplier supplier = new CemiSupplier(vendor, supplierId, maskSensitiveData);
        }
        
    }
    
    protected void createAndStoreSupplierFileSupplierTabRow(final Award award, 
            final AwardExtendedAttribute awardExtendedAttribute, final String jobRunDateString) {

        CemiAwardScheduleFileAwardScheduleTabRowBoFactory factoryForBo = 
                new CemiAwardScheduleFileAwardScheduleTabRowBoFactory(award, awardExtendedAttribute, jobRunDateString,
                        dateTimeService, maskSensitiveData);
        
        CemiAwardScheduleFileAwardScheduleTabRowBo awardScheduleTabRow = factoryForBo.createCemiAwardScheduleFileAwardScheduleTabRowBo();
        storeSheetRow(awardScheduleTabRow);
    }
    
    private Collection<PayeeACHAccount> findAllActiveAccountsForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier) {
        final String vendorId = StringUtils.join(
                vendorHeaderGeneratedIdentifier, KFSConstants.DASH, vendorDetailAssignedIdentifier);
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(PdpPropertyConstants.PAYEE_ID_NUMBER, vendorId),
                Map.entry(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.VENDOR_ID),
                Map.entry(KFSPropertyConstants.ACTIVE, KFSConstants.ACTIVE_INDICATOR)
        );
        return businessObjectService.findMatchingOrderBy(
                PayeeACHAccount.class, criteria, PdpPropertyConstants.ACH_ACCOUNT_GENERATED_IDENTIFIER, true);
    }
    
    @Override
    public void writeSupplierFileSupplierAddressTabExtractDataToIntermediateStorage(Iterator<VendorAddress> vendorAddresses) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void writeSupplierFileSupplierEmailTabExtractDataToIntermediateStorage(Iterator<VendorAddress> vendorAddresses) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void writeSupplierFileSupplierPhoneTabExtractDataToIntermediateStorage(Iterator<VendorAddress> vendorAddresses) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void writeSupplierFileSupplierBankAccountsTabExtractDataToIntermediateStorage(Iterator<PayeeACHAccount> payeeACHAccounts) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void writeSupplierFileSupplierChildrenTabExtractDataToIntermediateStorage(Iterator<VendorDetail> vendors) {
        // TODO Auto-generated method stub
        
    }
    
}
