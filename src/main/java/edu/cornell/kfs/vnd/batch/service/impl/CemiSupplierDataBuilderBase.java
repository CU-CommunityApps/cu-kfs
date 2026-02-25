package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.sys.batch.xml.CemiFieldDefinition;
import edu.cornell.kfs.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.CemiVendorConstants.SupplierExtractSheets;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplier;
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
            final String supplierId = supplierIdFormatter.format(vendorCount);
            final CemiSupplier supplier = new CemiSupplier(vendor, supplierId);
            writeSupplierRow(supplier);
        }
        LOG.info("writeSupplierDataToIntermediateStorage, Finished writing {} Vendors", vendorCount);
    }

    protected void writeSupplierRow(final CemiSupplier supplier) throws IOException {
        writeDataToIntermediateStorage(SupplierExtractSheets.SUPPLIER, supplier);
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
