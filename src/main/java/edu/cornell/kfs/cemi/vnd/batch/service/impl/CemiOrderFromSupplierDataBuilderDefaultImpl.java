package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
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
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants.AddressTypes;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiOrderFromSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiOrderFromSupplierDataBuilder;
import edu.cornell.kfs.cemi.vnd.batch.service.impl.CemiOrmDataBuilderBase;  //CHANGE THIS TO USE VERSION edu.cornell.kfs.cemi.sys.batch.service.impl
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiOrderFromSupplierDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.cemi.vnd.util.CemiVendorUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiOrderFromSupplierDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiOrderFromSupplierDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final String supplierJobRunDate;
    private final CemiVendorOrmDao cemiVendorOrmDao;
    private final CemiOrderFromSupplierDao cemiOrderFromSupplierDao;
    private final Writer skippedSuppliersWriter;
    private final boolean maskSensitiveData;

    public CemiOrderFromSupplierDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final String supplierJobRunDate, final CemiVendorOrmDao cemiVendorOrmDao,
            final CemiOrderFromSupplierDao cemiOrderFromSupplierDao, final Writer skippedSuppliersWriter,
            final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDate, CemiOrderFromSupplierBo.class);
        Validate.notBlank(supplierJobRunDate, "supplierJobRunDate cannot be blank");
        Validate.notNull(cemiVendorOrmDao, "cemiVendorOrmDao cannot be null");
        Validate.notNull(cemiOrderFromSupplierDao, "cemiOrderFromSupplierDao cannot be null");
        Validate.notNull(skippedSuppliersWriter, "skippedSuppliersWriter cannot be null");
        this.supplierJobRunDate = supplierJobRunDate;
        this.cemiVendorOrmDao = cemiVendorOrmDao;
        this.cemiOrderFromSupplierDao = cemiOrderFromSupplierDao;
        this.skippedSuppliersWriter = skippedSuppliersWriter;
        this.maskSensitiveData = maskSensitiveData;
    }

    /*
     * NOTE: It is assumed that the iterator will return all of a given Supplier's eligible addresses
     *       BEFORE returning any addresses pertaining to the next Supplier.
     */
    @Override
    public void writeOrderFromSupplierDataToIntermediateStorage(
            final Iterator<CemiSupplierAddressBo> supplierAddresses) {
        int supplierAddressCount = 0;
        CemiSupplierBo currentSupplier = new CemiSupplierBo();
        List<CemiSupplierAddressBo> currentSupplierAddresses = new ArrayList<>();
        currentSupplier.setSupplierId(CUKFSConstants.NULL);
        int currentSpreadsheetKey = 1;
        int orderFromConnectionCount = 0;

        for (final CemiSupplierAddressBo supplierAddress : IteratorUtils.asIterable(supplierAddresses)) {
            supplierAddressCount++;
            if (supplierAddressCount % 1000 == 0) {
                LOG.info("writeOrderFromSupplierDataToIntermediateStorage, Processing {} supplier addresses and counting...",
                        supplierAddressCount);
            }
            final String supplierId = supplierAddress.getSupplierId();
            if (!Strings.CS.equals(supplierId, currentSupplier.getSupplierId())) {
                final int numNewConnectionsCreated = createAndStoreOrderFromSupplierRows(currentSupplier,
                        currentSupplierAddresses, Integer.toString(currentSpreadsheetKey));
                if (numNewConnectionsCreated > 0) {
                    currentSpreadsheetKey++;
                    orderFromConnectionCount += numNewConnectionsCreated;
                }
                currentSupplier = getSupplier(supplierId);
                currentSupplierAddresses = new ArrayList<>();
            }
            currentSupplierAddresses.add(supplierAddress);
        }

        orderFromConnectionCount += createAndStoreOrderFromSupplierRows(currentSupplier, currentSupplierAddresses,
                Integer.toString(currentSpreadsheetKey));
        LOG.info("writeOrderFromSupplierDataToIntermediateStorage, Finished processing and filtering {} "
                + "supplier addresses, to create a total of {} Order From Supplier Connections",
                supplierAddressCount, orderFromConnectionCount);
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

    /*
     * NOTE: This current implementation assumes that punchout/CXML Suppliers should have exactly 2 connections
     *       (one for punchouts and one for non-punchouts). It also assumes that each email-based connection
     *       should have no more than 1 email address, meaning adjustments will be needed if we have to add
     *       multiple emails per connection.
     */
    private int createAndStoreOrderFromSupplierRows(final CemiSupplierBo supplier,
            final List<CemiSupplierAddressBo> supplierAddresses, final String spreadsheetKey) {
        if (supplierAddresses.isEmpty()) {
            return 0;
        }
        final boolean isPunchoutSupplier = cemiOrderFromSupplierDao.determineIfSupplierIsUsedForPunchouts(
                supplier.getSupplierId(), supplierJobRunDate);
        final Map<String, List<VendorAddress>> kfsVendorAddresses = getKfsVendorAddresses(supplier.getSupplierId());
        final CemiSupplierEmailBo emailRow = getSupplierEmailRowIfPresent(supplier.getSupplierId());
        if (ObjectUtils.isNull(emailRow)) {
            LOG.warn("createAndStoreOrderFromSupplierRows, Supplier {} does not have an associated Supplier Email "
                    + "record. This Supplier will be excluded from the extract altogether.", supplier.getSupplierId());
            writeSkippedSupplierToReportFile(supplier.getSupplierId());
            return 0;
        }

        final List<Pair<String, CemiSupplierAddressBo>> addressesWithUniqueEmails = getUniqueEmailsForAddresses(
                supplierAddresses, kfsVendorAddresses, emailRow);

        final List<Pair<String, CemiSupplierAddressBo>> addressesForOutput;
        if (isPunchoutSupplier) {
            if (addressesWithUniqueEmails.isEmpty()) {
                LOG.warn("createAndStoreOrderFromSupplierRows, Punchout Supplier {} has no email addresses that are "
                        + "eligible to be included in the Order From Supplier extract. This Supplier will be "
                        + "excluded from the extract altogether.", supplier.getSupplierId());
                writeSkippedSupplierToReportFile(supplier.getSupplierId());
                addressesForOutput = List.of();
            } else {
                // Just duplicate the first address; the loop below will handle the punchout logic accordingly.
                final Pair<String, CemiSupplierAddressBo> firstAddressWithUniqueEmail = addressesWithUniqueEmails.get(0);
                addressesForOutput = List.of(firstAddressWithUniqueEmail, firstAddressWithUniqueEmail);
            }
        } else if (addressesWithUniqueEmails.size() <= 1) {
            LOG.debug("createAndStoreOrderFromSupplierRows, Supplier {} only has one unique email across its "
                    + "PO-related addresses. This Supplier will be excluded from the extract altogether.",
                    supplier.getSupplierId());
            writeSkippedSupplierToReportFile(supplier.getSupplierId());
            addressesForOutput = List.of();
        } else {
            addressesForOutput = addressesWithUniqueEmails;
        }

        int connectionRowId = 1;

        for (final Pair<String, CemiSupplierAddressBo> emailAndAddressPair : addressesForOutput) {
            final boolean isFirstRowForSupplier = (connectionRowId == 1);
            final boolean isPunchoutConnection = isPunchoutSupplier && isFirstRowForSupplier;
            final CemiOrderFromSupplierBo orderFromSupplierRow = new CemiOrderFromSupplierBoFactory()
                    .withSupplier(supplier)
                    .withSupplierEmailRow(emailRow)
                    .withEmailFromKfsVendorAddress(emailAndAddressPair.getLeft())
                    .withSpreadsheetKey(spreadsheetKey)
                    .withSupplierConnectionRowId(Integer.toString(connectionRowId))
                    .withFirstRowForSupplierFlag(isFirstRowForSupplier)
                    .withPunchoutSupplierFlag(isPunchoutSupplier)
                    .withPunchoutConnectionFlag(isPunchoutConnection)
                    .withMaskingFlag(maskSensitiveData)
                    .createCemiOrderFromSupplierBo();

            storeSheetRow(orderFromSupplierRow);
            connectionRowId++;
        }

        return addressesForOutput.size();
    }

    private void writeSkippedSupplierToReportFile(final String supplierId) {
        try {
            skippedSuppliersWriter.write(supplierId);
            skippedSuppliersWriter.write(KFSConstants.NEWLINE);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, List<VendorAddress>> getKfsVendorAddresses(final String supplierId) {
        final List<VendorAddress> vendorAddresses = cemiVendorOrmDao.getKfsVendorAddresses(
                supplierId, supplierJobRunDate);
        return CemiVendorUtils.groupKfsVendorAddressesByLineDataThenPrioritizeByType(
                vendorAddresses, AddressTypes.PURCHASE_ORDER);
    }

    private CemiSupplierEmailBo getSupplierEmailRowIfPresent(final String supplierId) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(CemiVendorPropertyConstants.SUPPLIER_ID, supplierId),
                Map.entry(CemiBasePropertyConstants.JOB_RUN_DATE, supplierJobRunDate)
        );
        final Collection<CemiSupplierEmailBo> results = businessObjectService.findMatching(
                CemiSupplierEmailBo.class, criteria);
        if (!results.isEmpty()) {
            return results.iterator().next();
        } else {
            return null;
        }
    }

    private List<Pair<String, CemiSupplierAddressBo>> getUniqueEmailsForAddresses(
            final List<CemiSupplierAddressBo> supplierAddresses,
            final Map<String, List<VendorAddress>> kfsVendorAddresses,
            final CemiSupplierEmailBo emailRow) {
        final List<Pair<String, CemiSupplierAddressBo>> addressesWithEmails = new ArrayList<>(supplierAddresses.size());
        final Set<String> encounteredEmails = new HashSet<>();

        for (final CemiSupplierAddressBo supplierAddress : supplierAddresses) {
            final String kfsEmailAddress = getKfsVendorEmailAddress(supplierAddress, kfsVendorAddresses);
            if (StringUtils.isNotBlank(kfsEmailAddress)
                    && encounteredEmails.add(StringUtils.lowerCase(kfsEmailAddress, Locale.US))) {
                if (Strings.CI.equalsAny(kfsEmailAddress, emailRow.getEmailAddress1(), emailRow.getEmailAddress2(),
                        emailRow.getEmailAddress3())) {
                    addressesWithEmails.add(Pair.of(kfsEmailAddress, supplierAddress));
                } else {
                    LOG.warn("getUniqueEmailsForAddresses, Supplier Address {} is associated with email {} that is not "
                            + "among the emails in the related Supplier Email record. This email-and-address pair will "
                            + "be ignored.", supplierAddress.getAddressId(), kfsEmailAddress);
                }
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

}
