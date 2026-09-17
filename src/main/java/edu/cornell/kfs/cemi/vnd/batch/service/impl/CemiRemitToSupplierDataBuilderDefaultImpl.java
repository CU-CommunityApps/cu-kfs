package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants.SupplierExtractSheets;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiRemitToSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileBankAccountsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileEmailsTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierDataBuilder;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiRemitToSupplierDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiRemitToSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final String supplierJobRunDate;
    private final boolean maskSensitiveData;

    /*
     * Using reflection to read the emails from the Supplier Emails Tab Row BO, to provide better flexibility in case
     * the Supplier Extract adds more emails (due to such BOs storing all of a Supplier's emails in one record).
     */
    private final List<Method> supplierEmailAddressGetterMethods;

    public CemiRemitToSupplierDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final String supplierJobRunDate, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDate, CemiRemitToSupplierBo.class);
        this.supplierJobRunDate = supplierJobRunDate;
        this.maskSensitiveData = maskSensitiveData;
        this.supplierEmailAddressGetterMethods = findAllGetterMethodsForRetrievingSupplierEmailAddresses();
        Validate.validState(!supplierEmailAddressGetterMethods.isEmpty(), "Could not find any email address "
                + "getter methods on the Supplier Emails Tab Row BO; if they have been renamed, then please "
                + "modify the Remit To Supplier Extract's builder class accordingly");
    }

    private List<Method> findAllGetterMethodsForRetrievingSupplierEmailAddresses() {
        final Stream.Builder<Method> getterMethods = Stream.builder();
        final String baseGetterName = "getEmailAddress";
        int nextIndex = 0;
        boolean moreEmailGetterMethodsAvailable = true;

        do {
            try {
                nextIndex++;
                final Method emailGetter = CemiSupplierFileEmailsTabRowBo.class.getMethod(baseGetterName + nextIndex);
                getterMethods.add(emailGetter);
            } catch (final NoSuchMethodException e) {
                moreEmailGetterMethodsAvailable = false;
            }
        } while (moreEmailGetterMethodsAvailable);

        return getterMethods.build().collect(Collectors.toUnmodifiableList());
    }

    /*
     * NOTE: It is assumed that, for all addresses associated with a specific supplier, the iterator will return
     * all such addresses BEFORE returning an address associated with a different supplier.
     */
    @Override
    public void writeRemitToSupplierDataToIntermediateStorage(final Iterator<CemiSupplierFileAddressesTabRowBo> addresses) {
        int supplierAddressCount = 0;
        CemiSupplierFileSupplierTabRowBo currentSupplier = new CemiSupplierFileSupplierTabRowBo();
        List<CemiSupplierFileAddressesTabRowBo> currentSupplierAddresses = new ArrayList<>();
        String currentSettlementBankAccountId = CemiBaseConstants.EMPTY_STRING;
        currentSupplier.setSupplierId(CUKFSConstants.NULL);

        for (final CemiSupplierFileAddressesTabRowBo address : IteratorUtils.asIterable(addresses)) {
            supplierAddressCount++;
            if (supplierAddressCount % 1000 == 0) {
                LOG.info("writeRemitToSupplierDataToIntermediateStorage, Processing {} supplier addresses and counting...",
                        supplierAddressCount);
            }
            final String supplierId = address.getSupplierId();
            if (!Strings.CS.equals(supplierId, currentSupplier.getSupplierId())) {
                createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses,
                        currentSettlementBankAccountId);
                currentSupplier = getSupplier(supplierId);
                currentSupplierAddresses = new ArrayList<>();
                currentSettlementBankAccountId = getFirstSettlementBankAccountIdForSupplier(supplierId);
            }
            currentSupplierAddresses.add(address);
        }

        createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses, currentSettlementBankAccountId);
        LOG.info("writeRemitToSupplierDataToIntermediateStorage, Finished processing {} supplier addresses",
                supplierAddressCount);
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

    private String getFirstSettlementBankAccountIdForSupplier(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE_STRING, supplierJobRunDate)
        );
        final Collection<CemiSupplierFileBankAccountsTabRowBo> results = businessObjectService.findMatching(
                CemiSupplierFileBankAccountsTabRowBo.class, criteria);
        if (!results.isEmpty()) {
            final CemiSupplierFileBankAccountsTabRowBo supplierBankAccounts = results.iterator().next();
            return supplierBankAccounts.getSettlementBankAccountId1();
        } else {
            return CemiBaseConstants.EMPTY_STRING;
        }
    }

    @SuppressWarnings("deprecation")
    private void createAndStoreRemitToSupplierRows(final CemiSupplierFileSupplierTabRowBo supplier,
            final List<CemiSupplierFileAddressesTabRowBo> supplierAddresses, final String settlementBankAccountId) {
        if (supplierAddresses.isEmpty()) {
            return;
        } else if (StringUtils.equals(supplier.getDefaultPaymentType(), CemiSupplierConstants.PAYMENT_TYPE_ACH_MANUAL)
                && StringUtils.isBlank(settlementBankAccountId)) {
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
            final String emailAddress = getEmailAddress(supplierAddress, kfsVendorAddresses, supplierEmailsTab);
            final boolean defaultConnection = (remitIndexForSupplier == 1);
            final CemiRemitToSupplierBo remitToSupplierRow = new CemiRemitToSupplierBoFactory()
                    .withSupplierAddress(supplierAddress)
                    .withSupplier(supplier)
                    .withOptionalSettlementBankAccountId(settlementBankAccountId)
                    .withOptionalEmailAddress(emailAddress)
                    .withRemitIndex(remitIndexForSupplier)
                    .withDefaultConnectionFlag(defaultConnection)
                    .withMaskingFlag(maskSensitiveData)
                    .createCemiRemitToSupplierBo();

            storeSheetRow(remitToSupplierRow);
            remitIndexForSupplier++;
        }
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
    private String getEmailAddress(final CemiSupplierFileAddressesTabRowBo supplierAddress,
            final Map<String, List<VendorAddress>> groupedVendorAddresses,
            final Optional<CemiSupplierFileEmailsTabRowBo> supplierEmailsTabWrapper) {
        if (supplierEmailsTabWrapper.isEmpty()) {
            return CemiBaseConstants.EMPTY_STRING;
        }

        final CemiSupplierFileEmailsTabRowBo supplierEmailsTab = supplierEmailsTabWrapper.get();
        final String addressKey = CemiVendorUtils.generateAddressKey(supplierAddress);
        final List<VendorAddress> addressGroup = groupedVendorAddresses.get(addressKey);
        if (addressGroup == null) {
            LOG.debug("getEmailAddress, No email-bearing KFS Vendor Addresses found for Supplier Address: {}", addressKey);
            return CemiBaseConstants.EMPTY_STRING;
        }

        final CharSequence[] supplierEmails = getSupplierEmails(supplierEmailsTab);
        Validate.validState(supplierEmails.length > 0, "Unable to read email addresses from the Supplier "
                + "Emails Tab Row business object; code changes might be needed if the BO's getter methods "
                + "have been renamed");

        return addressGroup.stream()
                .map(VendorAddress::getVendorAddressEmailAddress)
                .filter(emailAddress -> StringUtils.equalsAnyIgnoreCase(emailAddress, supplierEmails))
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);
    }

    private CharSequence[] getSupplierEmails(final CemiSupplierFileEmailsTabRowBo supplierEmailsTab) {
        return supplierEmailAddressGetterMethods.stream()
                .map(emailGetterMethod -> getEmailAddress(supplierEmailsTab, emailGetterMethod))
                .filter(StringUtils::isNotBlank)
                .toArray(CharSequence[]::new);
    }

    private String getEmailAddress(final CemiSupplierFileEmailsTabRowBo supplierEmailsTab,
            final Method emailGetterMethod) {
        try {
            return (String) emailGetterMethod.invoke(supplierEmailsTab);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
