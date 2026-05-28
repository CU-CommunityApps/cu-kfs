package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.cemi.sys.CemiBasePropertyConstants;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;
import edu.cornell.kfs.cemi.vnd.CemiVendorPropertyConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierOrderFromBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierOrderFromDataBuilder;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiSupplierOrderFromDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiSupplierOrderFromDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    private final String supplierJobRunDate;

    public CemiSupplierOrderFromDataBuilderDefaultImpl(final BusinessObjectService businessObjectService,
            final String jobRunDate, final String supplierJobRunDate) {
        super(businessObjectService, jobRunDate, CemiSupplierOrderFromBo.class);
        this.supplierJobRunDate = supplierJobRunDate;
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
        int connectionRowId = 1;

        for (final CemiSupplierAddressBo supplierAddress : supplierAddresses) {
            final CemiSupplierOrderFromBo supplierOrderFromRow = new CemiSupplierOrderFromBoFactory()
                    .withSupplier(supplier)
                    .withSupplierAddress(supplierAddress)
                    .withSpreadsheetKey(spreadsheetKey)
                    .withSupplierConnectionRowId(Integer.toString(connectionRowId))
                    .withFirstRowForSupplierFlag(connectionRowId == 1)
                    .createCemiSupplierOrderFromBo();

            storeSheetRow(supplierOrderFromRow);
            connectionRowId++;
        }
    }

}
