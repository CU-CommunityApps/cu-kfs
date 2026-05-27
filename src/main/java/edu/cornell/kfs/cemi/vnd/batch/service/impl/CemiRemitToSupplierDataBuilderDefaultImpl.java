package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiRemitToSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierDataBuilder;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierOrmDao;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiRemitToSupplierDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiRemitToSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao;
    private final String supplierJobRunDate;
    private final boolean maskSensitiveData;

    public CemiRemitToSupplierDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao,
            final String supplierJobRunDate, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDate, CemiRemitToSupplierBo.class);
        this.cemiRemitToSupplierOrmDao = cemiRemitToSupplierOrmDao;
        this.supplierJobRunDate = supplierJobRunDate;
        this.maskSensitiveData = maskSensitiveData;
    }

    /*
     * NOTE: It is assumed that, for all addresses associated with a specific supplier, the iterator will return
     * all such addresses BEFORE returning an address associated with a different supplier.
     */
    @Override
    public void writeRemitToSupplierDataToIntermediateStorage(final Iterator<CemiSupplierAddressBo> addresses) {
        CemiSupplierBo currentSupplier = new CemiSupplierBo();
        List<CemiSupplierAddressBo> currentSupplierAddresses = new ArrayList<>();
        currentSupplier.setSupplierId(CUKFSConstants.NULL);

        for (final CemiSupplierAddressBo address : IteratorUtils.asIterable(addresses)) {
            final String supplierId = address.getSupplierId();
            if (!Strings.CS.equals(supplierId, currentSupplier.getSupplierId())) {
                createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses);
                currentSupplier = getSupplier(supplierId);
                currentSupplierAddresses = new ArrayList<>();
            }
            currentSupplierAddresses.add(address);
        }

        createAndStoreRemitToSupplierRows(currentSupplier, currentSupplierAddresses);
    }

    private CemiSupplierBo getSupplier(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE, jobRunDate)
        );
        final Collection<CemiSupplierBo> results = businessObjectService.findMatching(CemiSupplierBo.class, criteria);
        Validate.validState(!results.isEmpty(), "Could not find data row for supplier: %s", supplierId);
        return results.iterator().next();
    }

    private void createAndStoreRemitToSupplierRows(final CemiSupplierBo supplier,
            final List<CemiSupplierAddressBo> supplierAddresses) {
        if (supplierAddresses.isEmpty()) {
            return;
        }
        final Map<String, List<VendorAddress>> kfsVendorAddresses = getKfsVendorAddresses(supplier.getSupplierId());
        ensureExplicitDefaultRemitAddressIsListedFirstIfPresent(supplierAddresses, kfsVendorAddresses);

        int remitIndexForSupplier = 1;
        for (final CemiSupplierAddressBo supplierAddress : supplierAddresses) {
            final String emailAddress = getEmailAddress(supplierAddress, kfsVendorAddresses);
            final boolean defaultConnection = (remitIndexForSupplier == 1);
            final CemiRemitToSupplierBo remitToSupplierRow = new CemiRemitToSupplierBoFactory()
                    .withSupplierAddress(supplierAddress)
                    .withSupplier(supplier)
                    .withOptionalEmailAddress(emailAddress)
                    .withRemitIndex(remitIndexForSupplier)
                    .withDefaultConnectionFlag(defaultConnection)
                    .withMaskingFlag(maskSensitiveData)
                    .createCemiRemitToSupplierBo();

            storeSheetRow(remitToSupplierRow);
            remitIndexForSupplier++;
        }
    }

    private Map<String, List<VendorAddress>> getKfsVendorAddresses(final String supplierId) {
        final List<VendorAddress> vendorAddresses = cemiRemitToSupplierOrmDao.getKfsVendorAddresses(
                supplierId, supplierJobRunDate);
        final Map<String, List<VendorAddress>> groupedVendorAddresses = new HashMap<>();

        for (final VendorAddress vendorAddress : vendorAddresses) {
            final String addressKey = generateAddressKey(vendorAddress);
            final List<VendorAddress> subGroup = groupedVendorAddresses.computeIfAbsent(
                    addressKey, key -> new ArrayList<>());
            subGroup.add(vendorAddress);
        }

        final Comparator<VendorAddress> vendorAddressComparator = getVendorAddressEmailPrecedenceComparator();
        for (final List<VendorAddress> subGroup : groupedVendorAddresses.values()) {
            Collections.sort(subGroup, vendorAddressComparator);
        }

        return groupedVendorAddresses;
    }

    private void ensureExplicitDefaultRemitAddressIsListedFirstIfPresent(
            final List<CemiSupplierAddressBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses) {
        final int defaultRemitAddressListIndex = getListIndexOfExplicitDefaultRemitAddressIfPresent(
                supplierAddresses, kfsVendorAddresses);
        if (defaultRemitAddressListIndex > 0) {
            final CemiSupplierAddressBo defaultRemitAddress = supplierAddresses
                    .remove(defaultRemitAddressListIndex);
            supplierAddresses.add(0, defaultRemitAddress);
        }
    }

    private int getListIndexOfExplicitDefaultRemitAddressIfPresent(
            final List<CemiSupplierAddressBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses) {
        final String defaultRemitAddressKey = kfsVendorAddresses.values().stream()
                .flatMap(List::stream)
                .filter(kfsAddress -> CemiVendorUtils
                        .addressTypeIsActiveAndIsDefaultAndMatches(AddressTypes.REMIT, kfsAddress))
                .map(this::generateAddressKey)
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);

        if (StringUtils.isBlank(defaultRemitAddressKey)) {
            return -1;
        } else {
            return IntStream.range(0, supplierAddresses.size())
                    .filter(index -> Strings.CS.equals(
                            defaultRemitAddressKey, generateAddressKey(supplierAddresses.get(index))))
                    .findFirst()
                    .orElse(-1);
        }
    }

    private String getEmailAddress(final CemiSupplierAddressBo supplierAddress,
            final Map<String, List<VendorAddress>> groupedVendorAddresses) {
        final String addressKey = generateAddressKey(supplierAddress);
        final List<VendorAddress> addressGroup = groupedVendorAddresses.get(addressKey);
        if (addressGroup == null) {
            LOG.warn("getEmailAddress, UNEXPECTED: No KFS Vendor Addresses found for Supplier Address: {}", addressKey);
            return KFSConstants.EMPTY_STRING;
        }

        return addressGroup.stream()
                .map(VendorAddress::getVendorAddressEmailAddress)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(KFSConstants.EMPTY_STRING);
    }

    private String generateAddressKey(final VendorAddress vendorAddress) {
        return CemiUtils.generateConcatenatedKey(
                    vendorAddress.getVendorLine1Address(), vendorAddress.getVendorLine2Address(),
                    vendorAddress.getVendorCityName(), vendorAddress.getVendorStateCode(),
                    vendorAddress.getVendorZipCode(), vendorAddress.getVendorCountryCode());
    }

    private String generateAddressKey(final CemiSupplierAddressBo supplierAddress) {
        return CemiUtils.generateConcatenatedKey(
                    supplierAddress.getAddressLine1(), supplierAddress.getAddressLine2(),
                    supplierAddress.getCity(), supplierAddress.getState(),
                    supplierAddress.getZipCode(), supplierAddress.getCountryForAddress());
    }

    private Comparator<VendorAddress> getVendorAddressEmailPrecedenceComparator() {
        return Comparator.comparing(VendorAddress::getVendorAddressTypeCode, this::compareAddressTypesForEmailPrecedence)
                .thenComparing(VendorAddress::getVendorAddressGeneratedIdentifier);
    }

    private int compareAddressTypesForEmailPrecedence(final String addressType1, final String addressType2) {
        if (Strings.CS.equals(addressType1, AddressTypes.REMIT)) {
            return Strings.CS.equals(addressType2, AddressTypes.REMIT) ? 0 : -1;
        } else if (Strings.CS.equals(addressType2, AddressTypes.REMIT)) {
            return 1;
        } else {
            return 0;
        }
    }

}
