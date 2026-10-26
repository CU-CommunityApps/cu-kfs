package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.SupplierExtractSheets;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiRemitToSupplierFileRemitToSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileBankAccountsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileEmailsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierFileExtractDataBuilder;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.factory.CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiRemitToSupplierFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiRemitToSupplierFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final String supplierJobRunDate;
    @SuppressWarnings("unused")
    private final boolean maskSensitiveData;

    public CemiRemitToSupplierFileExtractDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final String supplierJobRunDate, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDate, CemiRemitToSupplierFileRemitToSupplierTabRowBo.class);
        this.supplierJobRunDate = supplierJobRunDate;
        this.maskSensitiveData = maskSensitiveData;
    }

    /*
     * NOTE: It is assumed that, for all addresses associated with a specific supplier, the iterator will return
     * all such addresses BEFORE returning an address associated with a different supplier.
     */
    @Override
    public void writeRemitToSupplierFileRemitToSupplierTabExtractDataToIntermediateStorage(
            final Iterator<CemiSupplierFileAddressesTabRowBo> addresses) {
        int supplierAddressCount = 0;
        int remitToSupplierRowCount = 0;
        CemiSupplierFileSupplierTabRowBo currentSupplier = new CemiSupplierFileSupplierTabRowBo();
        List<CemiSupplierFileAddressesTabRowBo> currentSupplierAddresses = new ArrayList<>();
        Optional<String> currentSettlementBankAccountId = Optional.empty();
        currentSupplier.setSupplierId(CUKFSConstants.NULL);

        for (final CemiSupplierFileAddressesTabRowBo address : IteratorUtils.asIterable(addresses)) {
            supplierAddressCount++;
            if (supplierAddressCount % 1000 == 0) {
                LOG.info("writeRemitToSupplierFileRemitToSupplierTabExtractDataToIntermediateStorage, "
                        + "Processing {} supplier addresses and counting...", supplierAddressCount);
            }
            final String supplierId = address.getSupplierId();
            if (!Strings.CS.equals(supplierId, currentSupplier.getSupplierId())) {
                remitToSupplierRowCount += createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses,
                        currentSettlementBankAccountId);
                currentSupplier = getSupplier(supplierId);
                currentSupplierAddresses = new ArrayList<>();
                currentSettlementBankAccountId = getFirstSettlementBankAccountIdForSupplier(supplierId);
            }
            currentSupplierAddresses.add(address);
        }

        remitToSupplierRowCount += createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses,
                currentSettlementBankAccountId);
        LOG.info("writeRemitToSupplierFileRemitToSupplierTabExtractDataToIntermediateStorage, "
                + "Finished generating {} Remit To Supplier rows from {} supplier addresses",
                remitToSupplierRowCount, supplierAddressCount);
    }

    private CemiSupplierFileSupplierTabRowBo getSupplier(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE_STRING, supplierJobRunDate)
        );
        final Collection<CemiSupplierFileSupplierTabRowBo> results = businessObjectService.findMatching(
                CemiSupplierFileSupplierTabRowBo.class, criteria);
        Validate.validState(!results.isEmpty(), "Could not find data row for supplier: %s", supplierId);
        return results.iterator().next();
    }

    private Optional<String> getFirstSettlementBankAccountIdForSupplier(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE_STRING, supplierJobRunDate)
        );
        final Collection<CemiSupplierFileBankAccountsTabRowBo> results = businessObjectService.findMatching(
                CemiSupplierFileBankAccountsTabRowBo.class, criteria);
        if (!results.isEmpty()) {
            final CemiSupplierFileBankAccountsTabRowBo supplierBankAccounts = results.iterator().next();
            return Optional.ofNullable(supplierBankAccounts.getSettlementBankAccountId1())
                    .filter(StringUtils::isNotBlank);
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings("deprecation")
    private int createAndStoreRemitToSupplierRows(final CemiSupplierFileSupplierTabRowBo supplier,
            final List<CemiSupplierFileAddressesTabRowBo> supplierAddresses,
            final Optional<String> settlementBankAccountId) {
        if (supplierAddresses.isEmpty()) {
            return 0;
        } else if (StringUtils.equals(supplier.getDefaultPaymentType(), CemiSupplierConstants.PAYMENT_TYPE_WIRE_MANUAL)) {
            LOG.info("createAndStoreRemitToSupplierRows, Supplier {} has a default payment type of {}, which is not "
                    + "a valid payment type for Remit To Supplier data. Will not generate any Remit To Supplier rows "
                    + "for this Supplier.",
                    supplier.getSupplierId(), CemiSupplierConstants.PAYMENT_TYPE_WIRE_MANUAL);
            return 0;
        } else if (StringUtils.equals(supplier.getDefaultPaymentType(), CemiSupplierConstants.PAYMENT_TYPE_ACH_MANUAL)
                && settlementBankAccountId.isEmpty()) {
            LOG.error("createAndStoreRemitToSupplierRows, Supplier {} does not specify any settlement bank accounts "
                    + "in its {} tab data, even though it specifies {} as the default payment type. The settlement "
                    + "bank account data will be left blank on the corresponding Remit To Supplier file row(s), but "
                    + "further manual corrections or source data corrections may be needed.",
                    supplier.getSupplierId(), SupplierExtractSheets.BANK_ACCOUNTS, supplier.getDefaultPaymentType());
        }

        final Map<String, List<VendorAddress>> kfsVendorAddresses = getKfsVendorRemitAddressesWithEmails(supplier);
        final Optional<CemiSupplierFileEmailsTabRowBo> supplierEmailsTab = getSupplierEmailsTabIfPresent(supplier);
        ensureExplicitDefaultRemitAddressIsListedFirstIfPresent(supplierAddresses, kfsVendorAddresses);

        int remitIndexForSupplier = 1;
        for (final CemiSupplierFileAddressesTabRowBo supplierAddress : supplierAddresses) {
            final Optional<String> emailAddress = getEmailAddress(supplierAddress, kfsVendorAddresses, supplierEmailsTab);
            final boolean defaultConnection = (remitIndexForSupplier == 1);
            final CemiRemitToSupplierFileRemitToSupplierTabRowBo remitToSupplierRow
                    = CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory.createTabRowBoFrom(
                            supplierAddress, supplier, settlementBankAccountId, emailAddress,
                            remitIndexForSupplier, defaultConnection);

            storeSheetRow(remitToSupplierRow);
            remitIndexForSupplier++;
        }

        return remitIndexForSupplier - 1;
    }

    private Map<String, List<VendorAddress>> getKfsVendorRemitAddressesWithEmails(
            final CemiSupplierFileSupplierTabRowBo supplier) {
        final Collection<VendorAddress> vendorRemitAddresses = getKfsVendorRemitAddresses(supplier);
        final Map<String, List<VendorAddress>> groupedVendorAddresses = new HashMap<>();

        for (final VendorAddress vendorRemitAddress : vendorRemitAddresses) {
            if (StringUtils.isBlank(vendorRemitAddress.getVendorAddressEmailAddress())) {
                continue;
            }
            final String addressKey = CemiVendorUtils.generateAddressKey(vendorRemitAddress);
            final List<VendorAddress> subGroup = groupedVendorAddresses.computeIfAbsent(
                    addressKey, key -> new ArrayList<>());
            if (CemiVendorUtils.addressTypeIsActiveAndIsDefaultAndMatches(AddressTypes.REMIT, vendorRemitAddress)) {
                subGroup.add(0, vendorRemitAddress);
            } else {
                subGroup.add(vendorRemitAddress);
            }
        }

        return groupedVendorAddresses;
    }

    private Collection<VendorAddress> getKfsVendorRemitAddresses(final CemiSupplierFileSupplierTabRowBo supplier) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(KFSPropertyConstants.VENDOR_HEADER_GENERATED_ID, supplier.getVendorHeaderGeneratedIdentifier()),
                Map.entry(KFSPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID, supplier.getVendorDetailAssignedIdentifier()),
                Map.entry(VendorPropertyConstants.VENDOR_ADDRESS_TYPE_CODE, AddressTypes.REMIT),
                Map.entry(VendorPropertyConstants.VENDOR_ADDRESS_ACTIVE_INDICATOR, KFSConstants.ACTIVE_INDICATOR)
        );
        return businessObjectService.findMatchingOrderBy(VendorAddress.class, criteria,
                KFSPropertyConstants.VENDOR_ADDRESS_GENERATED_ID, true);
    }

    private Optional<CemiSupplierFileEmailsTabRowBo> getSupplierEmailsTabIfPresent(
            final CemiSupplierFileSupplierTabRowBo supplier) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplier.getSupplierId()),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE_STRING, supplier.getJobRunDateString())
        );
        final Collection<CemiSupplierFileEmailsTabRowBo> results = businessObjectService.findMatching(
                CemiSupplierFileEmailsTabRowBo.class, criteria);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.iterator().next());
    }

    private void ensureExplicitDefaultRemitAddressIsListedFirstIfPresent(
            final List<CemiSupplierFileAddressesTabRowBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses) {
        final int defaultRemitAddressListIndex = getListIndexOfExplicitDefaultRemitAddressIfPresent(
                supplierAddresses, kfsVendorAddresses);
        if (defaultRemitAddressListIndex > 0) {
            final CemiSupplierFileAddressesTabRowBo defaultRemitAddress = supplierAddresses
                    .remove(defaultRemitAddressListIndex);
            supplierAddresses.add(0, defaultRemitAddress);
        }
    }

    private int getListIndexOfExplicitDefaultRemitAddressIfPresent(
            final List<CemiSupplierFileAddressesTabRowBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses) {
        final String defaultRemitAddressKey = kfsVendorAddresses.values().stream()
                .flatMap(List::stream)
                .filter(kfsAddress -> CemiVendorUtils
                        .addressTypeIsActiveAndIsDefaultAndMatches(AddressTypes.REMIT, kfsAddress))
                .map(CemiVendorUtils::generateAddressKey)
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);

        if (StringUtils.isBlank(defaultRemitAddressKey)) {
            return -1;
        } else {
            return IntStream.range(0, supplierAddresses.size())
                    .filter(index -> Strings.CS.equals(
                            defaultRemitAddressKey, CemiVendorUtils.generateAddressKey(supplierAddresses.get(index))))
                    .findFirst()
                    .orElse(-1);
        }
    }

    @SuppressWarnings("deprecation")
    private Optional<String> getEmailAddress(final CemiSupplierFileAddressesTabRowBo supplierAddress,
            final Map<String, List<VendorAddress>> groupedVendorAddresses,
            final Optional<CemiSupplierFileEmailsTabRowBo> supplierEmailsTabWrapper) {
        if (supplierEmailsTabWrapper.isEmpty()) {
            return Optional.empty();
        }

        final CemiSupplierFileEmailsTabRowBo supplierEmailsTab = supplierEmailsTabWrapper.get();
        final String addressKey = CemiVendorUtils.generateAddressKey(supplierAddress);
        final List<VendorAddress> addressGroup = groupedVendorAddresses.get(addressKey);
        if (addressGroup == null) {
            LOG.debug("getEmailAddress, No email-bearing KFS Vendor Addresses found for Supplier Address: {}", addressKey);
            return Optional.empty();
        }

        final CharSequence[] supplierEmails = getSupplierEmails(supplierEmailsTab);

        return addressGroup.stream()
                .map(VendorAddress::getVendorAddressEmailAddress)
                .filter(emailAddress -> StringUtils.equalsAnyIgnoreCase(emailAddress, supplierEmails))
                .findFirst();
    }

    /*
     * NOTE: If the Supplier Extract adds more email slots to the "Emails" tab/sheet,
     *       then this method will need to be updated accordingly.
     */
    private CharSequence[] getSupplierEmails(final CemiSupplierFileEmailsTabRowBo supplierEmailsTab) {
        return Stream.of(
                        supplierEmailsTab.getEmailAddress1(),
                        supplierEmailsTab.getEmailAddress2(),
                        supplierEmailsTab.getEmailAddress3(),
                        supplierEmailsTab.getEmailAddress4(),
                        supplierEmailsTab.getEmailAddress5(),
                        supplierEmailsTab.getEmailAddress6(),
                        supplierEmailsTab.getEmailAddress7()
                )
                .filter(StringUtils::isNotBlank)
                .toArray(CharSequence[]::new);
    }

}
