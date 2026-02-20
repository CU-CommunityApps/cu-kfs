package edu.cornell.kfs.vnd.batch.service.impl;

import java.text.DecimalFormat;
import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.sys.service.impl.CemiFileWriter;
import edu.cornell.kfs.vnd.CemiVendorConstants;
import edu.cornell.kfs.vnd.CemiVendorConstants.SupplierExtractSheets;
import edu.cornell.kfs.vnd.batch.dto.CemiSupplier;

public class CemiSupplierFileAppender {

    private static final Logger LOG = LogManager.getLogger();

    private final CemiFileWriter fileWriter;
    private final Iterator<VendorDetail> vendors;
    private final DecimalFormat supplierIdFormatter;
    private int vendorCount;
    private int supplierRowCount;
    private int supplierAddressRowCount;
    private int supplierChildMappingRowCount;

    public CemiSupplierFileAppender(final CemiFileWriter fileWriter, final Iterator<VendorDetail> vendors) {
        this.fileWriter = fileWriter;
        this.vendors = vendors;
        this.supplierIdFormatter = new DecimalFormat(CemiVendorConstants.SUPPLIER_ID_FORMAT);
    }

    public void writeVendorsToFile() {
        for (final VendorDetail vendor : IteratorUtils.asIterable(vendors)) {
            vendorCount++;
            if (vendorCount % 1000 == 0) {
                LOG.info("writeVendorsToFile, Writing {} Vendors and counting...", vendorCount);
            }
            final String supplierId = supplierIdFormatter.format(vendorCount);
            final CemiSupplier supplier = new CemiSupplier(vendor, supplierId);
            writeSupplierRow(supplier);
        }
    }

    private void writeSupplierRow(final CemiSupplier supplier) {
        fileWriter.writeRow(SupplierExtractSheets.SUPPLIER, supplier);
        supplierRowCount++;
    }

    public int getVendorCount() {
        return vendorCount;
    }

}
