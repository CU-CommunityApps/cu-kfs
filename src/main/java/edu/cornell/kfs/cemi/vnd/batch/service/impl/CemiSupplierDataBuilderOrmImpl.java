package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableLong;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.impl.CemiWorkbookOrmHandler;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;

public class CemiSupplierDataBuilderOrmImpl extends CemiSupplierDataBuilderBase {

    private final CemiWorkbookOrmHandler workbookHandler;
    private final String jobRunDateString;
    private final Map<String, MutableLong> sheetRowCounts;

    public CemiSupplierDataBuilderOrmImpl(final CemiOutputDefinition outputDefinition, final CemiVendorDao cemiVendorDao,
            final LocalDateTime jobRunDate, final boolean maskSensitiveData,
            final BusinessObjectService businessObjectService, final CemiSheetDao cemiSheetDao) {
        super(outputDefinition, cemiVendorDao, jobRunDate, maskSensitiveData);
        this.workbookHandler = new CemiWorkbookOrmHandler(outputDefinition, businessObjectService, cemiSheetDao);
        this.jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        this.sheetRowCounts = outputDefinition.getSheets().stream()
                .collect(Collectors.toUnmodifiableMap(
                        CemiSheetDefinition::getName, sheetDefinition -> new MutableLong(0L)));
    }

    @Override
    protected void writeDataToIntermediateStorage(final String sheetName, final Object rowObject) throws IOException {
        final MutableLong rowCount = sheetRowCounts.get(sheetName);
        final long nextCountValue = rowCount.incrementAndGet();
        workbookHandler.storeSheetRow(rowObject, jobRunDateString, nextCountValue);
    }

    @Override
    public void close() throws IOException {
        
    }

}
