package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.impl.CemiBufferedSheetTableWriter;
import edu.cornell.kfs.cemi.sys.batch.service.CemiTableMetadataService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.cemi.vnd.util.VendorAccountFinder;

public class CemiSupplierDataBuilderTableImpl extends CemiSupplierDataBuilderBase {

    private CemiBufferedSheetTableWriter sheetTableWriter;

    public CemiSupplierDataBuilderTableImpl(final CemiOutputDefinition outputDefinition,
            final CemiVendorDao cemiVendorDao, final LocalDateTime jobRunDate, final boolean maskSensitiveData,
            final CemiSheetDao cemiSheetDao, final CemiTableMetadataService cemiTableMetadataService) {
        super(outputDefinition, cemiVendorDao, jobRunDate, maskSensitiveData);
        Validate.notNull(cemiSheetDao, "cemiSheetDao cannot be null");
        Validate.notNull(cemiTableMetadataService, "cemiTableMetadataService cannot be null");
        this.sheetTableWriter = new CemiBufferedSheetTableWriter(
                cemiSheetDao, cemiTableMetadataService, outputDefinition);
    }

    @Override
    public void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors,
                final VendorAccountFinder accountFinder, final LocalDateTime jobRunDate) throws IOException {
        super.writeSupplierDataToIntermediateStorage(vendors, accountFinder, jobRunDate);
        sheetTableWriter.flushAllRows();
    }

    @Override
    protected void writeDataToIntermediateStorage(final String sheetName, final Object rowObject) throws IOException {
        sheetTableWriter.write(sheetName, rowObject);
    }

    @Override
    public void close() throws IOException {
        sheetTableWriter.close();
    }

}
