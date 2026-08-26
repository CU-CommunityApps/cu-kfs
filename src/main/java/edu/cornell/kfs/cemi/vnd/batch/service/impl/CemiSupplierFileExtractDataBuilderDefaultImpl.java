package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBankAccountBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileBankAccountsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileChildrenTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileEmailsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFilePhonesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierParentIdentifiersReference;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierFileExtractDataBuilder;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierBankAccountBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierEmailBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFileAddressesTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFileBankAccountsTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFileChildrenTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFileEmailsTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFilePhonesTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiSupplierFileSupplierTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.service.ISOFIPSConversionService;

public class CemiSupplierFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase implements CemiSupplierFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private ISOFIPSConversionService isoFipsConversionService;
    private boolean maskSensitiveData;
    private DecimalFormat supplierIdFormatter;
    private CemiSupplierBankAccountBo emptyBankAccountBo;
    private CemiSupplierEmailBo emptyEmailBo;

    public CemiSupplierFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final ISOFIPSConversionService isoFipsConversionService, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString,
                CemiSupplierFileSupplierTabRowBo.class,
                CemiSupplierFileAddressesTabRowBo.class,
                CemiSupplierFilePhonesTabRowBo.class,
                CemiSupplierFileBankAccountsTabRowBo.class,
                CemiSupplierFileChildrenTabRowBo.class,
                CemiSupplierFileEmailsTabRowBo.class);
        Validate.notNull(isoFipsConversionService, "isoFipsConversionService cannot be null");

        this.isoFipsConversionService = isoFipsConversionService;
        this.maskSensitiveData = maskSensitiveData;
        this.supplierIdFormatter = new DecimalFormat(CemiSupplierConstants.SUPPLIER_ID_FORMAT);
        this.emptyBankAccountBo = CemiSupplierBankAccountBoFactory.createBankAccountBoFrom(
                Optional.empty(), CemiBaseConstants.EMPTY_STRING, 0, maskSensitiveData);
        this.emptyEmailBo = CemiSupplierEmailBoFactory.createEmailBoFrom(
                List.of(), CemiBaseConstants.EMPTY_STRING, false, 0);
    }

    @Override
    public void writeSupplierFileExtractDataForAllMappedTabsToIntermediateStorage(Iterator<VendorDetail> vendors) {
        CemiSupplierParentIdentifiersReference parentSupplierReference = new CemiSupplierParentIdentifiersReference(
                CemiBaseConstants.EMPTY_STRING, Integer.valueOf(0), Integer.valueOf(0));
        int vendorCount = 0;

        for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
            vendorCount++;
            if (vendorCount % 1000 == 0) {
                LOG.info("writeSupplierDataToIntermediateStorage, Writing {} Vendors and counting...", vendorCount);
            }

            final Collection<PayeeACHAccount> vendorAccounts = findAllActiveAccountsForVendor(
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
            final String supplierId = supplierIdFormatter.format(vendorCount);
            if (vendor.isVendorParentIndicator()) {
                parentSupplierReference = createParentSupplierReference(supplierId, vendor);
            }

            createAndStoreSupplierFileSupplierTabRow(vendor, supplierId);
            createAndStoreAllSupplierAddressesFor(vendor, supplierId);
            createAndStoreAllSupplierPhonesFor(vendor, supplierId);
            createAndStoreFlattenedBankAccountsRowIfNecessary(vendor, supplierId, vendorAccounts);
            createAndStoreSupplierChildMappingRowIfNecessary(vendor, supplierId, parentSupplierReference);
            createAndStoreFlattenedEmailsRowIfNecessary(vendor, supplierId);
        }
    }

    private CemiSupplierParentIdentifiersReference createParentSupplierReference(
            final String newParentSupplierId, final VendorDetail newParentVendor) {
        return new CemiSupplierParentIdentifiersReference(newParentSupplierId,
                newParentVendor.getVendorHeaderGeneratedIdentifier(),
                newParentVendor.getVendorDetailAssignedIdentifier());
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

    private void createAndStoreSupplierFileSupplierTabRow(final VendorDetail vendor, final String supplierId) {
        final CemiSupplierFileSupplierTabRowBo supplierRowBo = CemiSupplierFileSupplierTabRowBoFactory
                .createTabRowBoFrom(vendor, supplierId, isoFipsConversionService, maskSensitiveData);
        storeSheetRow(supplierRowBo);
    }

    private void createAndStoreAllSupplierAddressesFor(final VendorDetail vendor, final String supplierId) {
        final Map<String, List<VendorAddress>> orderedAddressGroups = new LinkedHashMap<>();
        final String vendorTypeCode = vendor.getVendorHeader().getVendorTypeCode();
        
        for (final VendorAddress vendorAddress : vendor.getVendorAddresses()) {
            // Restricting addresses by country = US
            if (!vendorAddress.isActive() ||
                    !vendorAddress.getVendorCountryCode().equalsIgnoreCase(CemiSupplierConstants.COUNTRY_CODE_UNITED_STATES)) {
                LOG.debug("createAndStoreAllSupplierAddressesFor, Vendor Address {} for Vendor {}-{} "
                         + "was NOT written to conversion file.",
                        vendorAddress.getVendorAddressGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
                continue;
            }
            final String addressKey = CemiUtils.generateKeyForGroupingDuplicates(
                    vendorAddress.getVendorLine1Address(), vendorAddress.getVendorLine2Address(),
                    vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                    vendorAddress.getVendorZipCode(), vendorAddress.getVendorAddressInternationalProvinceName(),
                    vendorAddress.getVendorCountryCode());
            final List<VendorAddress> addressGroup = orderedAddressGroups.computeIfAbsent(
                    addressKey, key -> new ArrayList<>());
            addressGroup.add(vendorAddress);
        }

        int addressCount = 0;
        for (final List<VendorAddress> addressGroup : orderedAddressGroups.values()) {
            addressCount++;
            createAndStoreSupplierFileAddressesTabRow(addressGroup, supplierId, vendorTypeCode, addressCount);
        }
    }

    private void createAndStoreSupplierFileAddressesTabRow(final List<VendorAddress> addressGroup,
            final String supplierId, final String vendorTypeCode, final int addressIndex) {
        final CemiSupplierFileAddressesTabRowBo addressesRowBo = CemiSupplierFileAddressesTabRowBoFactory
                .createTabRowBoFrom(addressGroup, supplierId, vendorTypeCode, addressIndex);
        storeSheetRow(addressesRowBo);
    }

    private void createAndStoreAllSupplierPhonesFor(final VendorDetail vendor, final String supplierId) {
        final Map<String, List<VendorPhoneNumber>> orderedPhoneGroups = new LinkedHashMap<>();

        for (final VendorPhoneNumber vendorPhoneNumber : vendor.getVendorPhoneNumbers()) {
            // Presuming phone numbers are US and NOT restricting by country
            if (!vendorPhoneNumber.isActive()) {
                LOG.debug("writeAllSupplierPhoneRowsFor, Vendor Phone {} for Vendor {}-{} was NOT written to conversion file.",
                        vendorPhoneNumber.getVendorPhoneGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
                continue;
            }
            final String phoneKey = CemiUtils.generateKeyForGroupingDuplicates(
                    vendorPhoneNumber.getVendorPhoneNumber(), vendorPhoneNumber.getVendorPhoneExtensionNumber());
            final List<VendorPhoneNumber> phoneGroup = orderedPhoneGroups.computeIfAbsent(
                    phoneKey, key -> new ArrayList<>());
            phoneGroup.add(vendorPhoneNumber);
        }

        int phoneNumberCount = 0;
        for (final List<VendorPhoneNumber> phoneGroup : orderedPhoneGroups.values()) {
            phoneNumberCount++;
            createAndStoreSupplierFilePhonesTabRow(phoneGroup, supplierId, phoneNumberCount);
        }
    }

    private void createAndStoreSupplierFilePhonesTabRow(final List<VendorPhoneNumber> phoneGroup,
            final String supplierId, final int phoneIndex) {
        final CemiSupplierFilePhonesTabRowBo phoneRowBo = CemiSupplierFilePhonesTabRowBoFactory.createTabRowBoFrom(
                phoneGroup, supplierId, phoneIndex);
        storeSheetRow(phoneRowBo);
    }

    private void createAndStoreFlattenedBankAccountsRowIfNecessary(final VendorDetail vendor, final String supplierId,
            final Collection<PayeeACHAccount> vendorAccounts) {
        final PayeeACHAccount[] activeVendorAccounts = vendorAccounts.stream()
                .filter(vendorAccount -> isVendorAccountActive(vendorAccount, vendor))
                .toArray(PayeeACHAccount[]::new);

        if (activeVendorAccounts.length == 0) {
            LOG.debug("createAndStoreFlattenedBankAccountsRowIfNecessary, No active Payee ACH Accounts exist for "
                    + "KFS Vendor {}-{}; a corresponding Supplier Bank Accounts row will NOT be written",
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
            return;
        }

        final CemiSupplierBankAccountBo[] bankAccountBos = new CemiSupplierBankAccountBo[activeVendorAccounts.length];
        for (int i = 0; i < activeVendorAccounts.length; i++) {
            final PayeeACHAccount vendorAccount = activeVendorAccounts[i];
            bankAccountBos[i] = CemiSupplierBankAccountBoFactory.createBankAccountBoFrom(
                    Optional.of(vendorAccount), supplierId, i + 1, maskSensitiveData);
        }

        final List<CemiSupplierBankAccountBo> paddedBankAccountsList = CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                emptyBankAccountBo, CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES, bankAccountBos);
        if (paddedBankAccountsList.size() > CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES) {
            LOG.warn("createAndStoreFlattenedBankAccountsRowIfNecessary, Found {} active Payee ACH Accounts for Vendor "
                    + "{}-{}; only the first {} will be written to the file",
                    paddedBankAccountsList.size(), vendor.getVendorHeaderGeneratedIdentifier(),
                    vendor.getVendorDetailAssignedIdentifier(), CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES);
        }

        createAndStoreSupplierFileBankAccountsTabRow(supplierId, paddedBankAccountsList);
    }

    private void createAndStoreSupplierFileBankAccountsTabRow(
            final String supplierId, final List<CemiSupplierBankAccountBo> bankAccounts) {
        final CemiSupplierFileBankAccountsTabRowBo bankAccountsRowBo = CemiSupplierFileBankAccountsTabRowBoFactory
                .creatTabRowBoFrom(supplierId, bankAccounts);
        storeSheetRow(bankAccountsRowBo);
    }

    private boolean isVendorAccountActive(final PayeeACHAccount vendorAccount, final VendorDetail vendor) {
        if (vendorAccount.isActive()) {
            return true;
        } else {
            LOG.warn("isVendorAccountActive, Payee ACH Account with ID {} for KFS Vendor {}-{} is inactive "
                    + "and will NOT be included; it should have been filtered out by the upstream query",
                    vendorAccount.getAchAccountGeneratedIdentifier(),
                    vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
            return false;
        }
    }

    protected void createAndStoreSupplierChildMappingRowIfNecessary(VendorDetail currentVendor, String currentSupplierId,
            CemiSupplierParentIdentifiersReference parentSupplierReference) {
        if (currentVendor.isVendorParentIndicator()) {
            LOG.debug("createAndStoreSupplierChildMappingRowIfNecessary, Current Vendor {}-{} is marked as a parent; "
                    + "a corresponding Supplier Children row will NOT be written",
                    currentVendor.getVendorHeaderGeneratedIdentifier(),
                    currentVendor.getVendorDetailAssignedIdentifier());
        } else if (
                currentVendor.getVendorHeaderGeneratedIdentifier().equals(
                        parentSupplierReference.getParentVendorHeaderGeneratedIdentifier())
                && !currentVendor.getVendorDetailAssignedIdentifier().equals(
                        parentSupplierReference.getParentVendorDetailAssignedIdentifier())) {
            createAndStoreSupplierFileChildrenTabRow(parentSupplierReference.getParentSupplierId(), currentSupplierId);
        } else {
            LOG.warn("createAndStoreSupplierChildMappingRowIfNecessary, Current Vendor {}-{} is not marked as a parent, "
                    + "yet it's not a child of Vendor {}-{}. Either the Vendors are being iterated over in the wrong "
                    + "order, or the data query incorrectly excluded the matching parent Vendor, or some other logic "
                    + "error occurred. A corresponding Supplier Children row will NOT be written.",
                    currentVendor.getVendorHeaderGeneratedIdentifier(),
                    currentVendor.getVendorDetailAssignedIdentifier(),
                    parentSupplierReference.getParentVendorHeaderGeneratedIdentifier(),
                    parentSupplierReference.getParentVendorDetailAssignedIdentifier());
        }
    }

    private void createAndStoreSupplierFileChildrenTabRow(final String parentSupplierId, final String childSupplierId) {
        final CemiSupplierFileChildrenTabRowBo childrenRowBo = CemiSupplierFileChildrenTabRowBoFactory
                .createTabRowBoFrom(parentSupplierId, childSupplierId);
        storeSheetRow(childrenRowBo);
    }

    private void createAndStoreFlattenedEmailsRowIfNecessary(VendorDetail vendor, String supplierId) {
        final Map<String, List<VendorAddress>> orderedAddressGroups = groupAndOrderVendorAddressesContainingEmails(vendor);
        if (orderedAddressGroups.isEmpty()) {
            LOG.debug("createAndStoreFlattenedEmailsRowIfNecessary, Did not find any active Vendor Addresses containing "
                        + "email info for Vendor {}-{}; a corresponding Supplier Email row will NOT be written",
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
            return;
        }

        final List<List<VendorAddress>> reorderedGroups = CemiVendorUtils.reorderAddressGroupsToPutPrimaryGroupFirst(
                vendor.getVendorHeader().getVendorTypeCode(), orderedAddressGroups.values());
        final CemiSupplierEmailBo[] emailBos = new CemiSupplierEmailBo[reorderedGroups.size()];
        for (int i = 0; i < emailBos.length; i++) {
            final List<VendorAddress> addressGroup = reorderedGroups.get(i);
            final boolean isPrimary = (i == 0);
            emailBos[i] = CemiSupplierEmailBoFactory.createEmailBoFrom(addressGroup, supplierId, isPrimary, i + 1);
        }

        final List<CemiSupplierEmailBo> paddedEmailsList = CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                emptyEmailBo, CemiSupplierConstants.MAX_SUPPLIER_EMAIL_ENTRIES, emailBos);
        if (paddedEmailsList.size() > CemiSupplierConstants.MAX_SUPPLIER_EMAIL_ENTRIES) {
            LOG.warn("createAndStoreFlattenedEmailsRowIfNecessary, Found {} distinct active emails for Vendor {}-{}; "
                    + "only the first {} will be written to the file",
                    paddedEmailsList.size(), vendor.getVendorHeaderGeneratedIdentifier(),
                    vendor.getVendorDetailAssignedIdentifier(), CemiSupplierConstants.MAX_SUPPLIER_EMAIL_ENTRIES);
        }

        createAndStoreSupplierFileEmailsTabRow(supplierId, paddedEmailsList);
    }

    private void createAndStoreSupplierFileEmailsTabRow(
            final String supplierId, final List<CemiSupplierEmailBo> emailAddresses) {
        final CemiSupplierFileEmailsTabRowBo emailsRowBo = CemiSupplierFileEmailsTabRowBoFactory
                .createTabRowBoFrom(supplierId, emailAddresses);
        storeSheetRow(emailsRowBo);
    }

    private Map<String, List<VendorAddress>> groupAndOrderVendorAddressesContainingEmails(final VendorDetail vendor) {
        final Map<String, List<VendorAddress>> orderedAddressGroups = new LinkedHashMap<>();

        for (final VendorAddress vendorAddress : vendor.getVendorAddresses()) {
            if (StringUtils.isBlank(vendorAddress.getVendorAddressEmailAddress())) {
                continue;
            } else if (!vendorAddress.isActive()) {
                LOG.debug("groupAndOrderVendorAddressesContainingEmails, Vendor Address {} containing email info "
                        + "for Vendor {}-{} was NOT written to conversion file.",
                        vendorAddress.getVendorAddressGeneratedIdentifier(),
                        vendor.getVendorHeaderGeneratedIdentifier(),
                        vendor.getVendorDetailAssignedIdentifier());
                continue;
            }
            final String addressKey = CemiUtils.generateKeyForGroupingDuplicates(
                    vendorAddress.getVendorAddressEmailAddress());
            final List<VendorAddress> addressGroup = orderedAddressGroups.computeIfAbsent(
                    addressKey, key -> new ArrayList<>());
            addressGroup.add(vendorAddress);
        }

        return orderedAddressGroups;
    }

}
