package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierOrderFromBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierOrderFromDataBuilder;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiSupplierOrderFromDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiSupplierOrderFromDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final String supplierJobRunDate;
    private final CemiVendorOrmDao cemiVendorOrmDao;

    public CemiSupplierOrderFromDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final String supplierJobRunDate, final CemiVendorOrmDao cemiVendorOrmDao) {
        super(businessObjectService, jobRunDate, CemiSupplierOrderFromBo.class);
        Validate.notBlank(supplierJobRunDate, "supplierJobRunDate cannot be blank");
        Validate.notNull(cemiVendorOrmDao, "cemiVendorOrmDao cannot be null");
        this.supplierJobRunDate = supplierJobRunDate;
        this.cemiVendorOrmDao = cemiVendorOrmDao;
    }

    @Override
    public void writeSupplierOrderFromDataToIntermediateStorage(
            final Iterator<CemiSupplierAddressBo> supplierAddresses) {
        int supplierAddressCount = 0;
        CemiSupplierBo currentSupplier = new CemiSupplierBo();
        List<CemiSupplierAddressBo> currentSupplierAddresses = new ArrayList<>();
        currentSupplier.setSupplierId(CUKFSConstants.NULL);
        int currentSpreadsheetKey = 0;

        for (final CemiSupplierAddressBo supplierAddress : IteratorUtils.asIterable(supplierAddresses)) {
            supplierAddressCount++;
            if (supplierAddressCount % 1000 == 0) {
                LOG.info("writeSupplierOrderFromDataToIntermediateStorage, Processing {} supplier addresses and counting...",
                        supplierAddressCount);
            }
            final String supplierId = supplierAddress.getSupplierId();
            if (!Strings.CS.equals(supplierId, currentSupplier.getSupplierId())) {
                createAndStoreSupplierOrderFromRows(currentSupplier, currentSupplierAddresses,
                        Integer.toString(currentSpreadsheetKey));
                currentSupplier = getSupplier(supplierId);
                currentSupplierAddresses = new ArrayList<>();
                currentSpreadsheetKey++;
            }
            currentSupplierAddresses.add(supplierAddress);
        }

        createAndStoreSupplierOrderFromRows(currentSupplier, currentSupplierAddresses,
                Integer.toString(currentSpreadsheetKey));
        LOG.info("writeSupplierOrderFromDataToIntermediateStorage, Finished processing {} supplier addresses",
                supplierAddressCount);
    }

    private CemiSupplierBo getSupplier(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE, supplierJobRunDate)
        );
        final Collection<CemiSupplierBo> results = businessObjectService.findMatching(CemiSupplierBo.class, criteria);
        Validate.validState(!results.isEmpty(), "Could not find data row for supplier: %s", supplierId);
        return results.iterator().next();
    }

    private void createAndStoreSupplierOrderFromRows(final CemiSupplierBo supplier,
            final List<CemiSupplierAddressBo> supplierAddresses, final String spreadsheetKey) {
        if (supplierAddresses.isEmpty()) {
            return;
        }
        final Map<String, List<VendorAddress>> kfsVendorAddresses = getKfsVendorAddresses(supplier.getSupplierId());
        final CemiSupplierEmailBo emailRow = getSupplierEmailRow(supplier.getSupplierId());
        final List<Pair<String, CemiSupplierAddressBo>> addressesWithUniqueEmails = getUniqueEmailsForAddresses(
                supplierAddresses, kfsVendorAddresses, emailRow);

        final List<CemiSupplierAddressBo> addressesForOutput = new ArrayList<>(supplierAddresses.size());

        int connectionRowId = 1;

        for (final CemiSupplierAddressBo supplierAddress : supplierAddresses) {
            final String kfsEmailAddress = getKfsVendorEmailAddress(supplierAddress, kfsVendorAddresses);
            final CemiSupplierOrderFromBo supplierOrderFromRow = new CemiSupplierOrderFromBoFactory()
                    .withSupplier(supplier)
                    //.withSupplierAddress(supplierAddress)
                    .withSpreadsheetKey(spreadsheetKey)
                    .withSupplierConnectionRowId(Integer.toString(connectionRowId))
                    .withFirstRowForSupplierFlag(connectionRowId == 1)
                    .createCemiSupplierOrderFromBo();

            storeSheetRow(supplierOrderFromRow);
            connectionRowId++;
        }
    }

    private CemiSupplierEmailBo getSupplierEmailRow(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE, supplierJobRunDate)
        );
        final Collection<CemiSupplierEmailBo> results = businessObjectService.findMatching(
                CemiSupplierEmailBo.class, criteria);
        Validate.validState(!results.isEmpty(), "Could not find email data row for PO supplier: %s", supplierId);
        return results.iterator().next();
    }

    private List<Pair<String, CemiSupplierAddressBo>> getUniqueEmailsForAddresses(
            final List<CemiSupplierAddressBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses,
            final CemiSupplierEmailBo emailRow) {
        final List<Pair<String, CemiSupplierAddressBo>> addressesWithEmails = new ArrayList<>(supplierAddresses.size());
        final Set<String> encounteredEmails = new HashSet<>();

        for (final CemiSupplierAddressBo supplierAddress : supplierAddresses) {
            final String kfsEmailAddress = getKfsVendorEmailAddress(supplierAddress, kfsVendorAddresses);
            if (StringUtils.isBlank(kfsEmailAddress)
                    || !encounteredEmails.add(StringUtils.lowerCase(kfsEmailAddress, Locale.US))) {
                continue;
            } else if (Strings.CI.equalsAny(kfsEmailAddress, emailRow.getEmailAddress1(), emailRow.getEmailAddress2(),
                    emailRow.getEmailAddress3())) {
                addressesWithEmails.add(Pair.of(kfsEmailAddress, supplierAddress));
            } else {
                LOG.warn("getUniqueEmailsForAddresses, Supplier Address {} is associated with email {} that is not "
                        + "among the emails in the related Supplier Email record. This email-and-address pair will "
                        + "be ignored.", supplierAddress.getAddressId(), kfsEmailAddress);
            }
        }

        return addressesWithEmails;
    }

    private String getKfsVendorEmailAddress(final CemiSupplierAddressBo supplierAddress,
            final Map<String, List<VendorAddress>> groupedVendorAddresses) {
        final String addressKey = CemiVendorUtils.generateAddressKey(supplierAddress);
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

    private Map<String, List<VendorAddress>> getKfsVendorAddresses(final String supplierId) {
        final List<VendorAddress> vendorAddresses = cemiVendorOrmDao.getKfsVendorAddresses(
                supplierId, supplierJobRunDate);
        return CemiVendorUtils.groupKfsVendorAddressesByLineDataThenPrioritizeByType(
                vendorAddresses, AddressTypes.PURCHASE_ORDER);
    }

}
