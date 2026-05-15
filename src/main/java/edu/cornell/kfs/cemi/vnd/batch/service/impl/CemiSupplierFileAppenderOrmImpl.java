package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.impl.CemiWorkbookOrmHandler;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiSupplierFileAppenderOrmImpl extends CemiSupplierFileAppenderBase {

    private final CemiWorkbookOrmHandler workbookHandler;
    private final String jobRunDateString;

    public CemiSupplierFileAppenderOrmImpl(final CemiOutputDefinition outputDefinition, final LocalDateTime jobRunDate,
            final BusinessObjectService businessObjectService, final CemiSheetDao cemiSheetDao) {
        super(outputDefinition, jobRunDate);
        this.workbookHandler = new CemiWorkbookOrmHandler(outputDefinition, businessObjectService, cemiSheetDao);
        this.jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
    }

    @Override
    protected void populateSheetFromIntermediateDataStorage(
            final CemiSheetDefinition sheetDefinition, final CemiExcelWriter fileWriter) throws IOException {
        final String sheetName = sheetDefinition.getName();
        workbookHandler.getAndHandleSheetRowDataForPrinting(jobRunDateString, jobRunDateString, sheetDataRow -> {
            fileWriter.writeRow(sheetName, sheetDataRow);
        });
    }

    @Override
    protected Stream<String[]> getCloseableSheetDataStreamFromIntermediateStorage(
            final CemiSheetDefinition sheetDefinition) throws IOException {
        throw new UnsupportedOperationException("This method is not used by this implementation");
    }

    @Override
    public void cleanUpIntermediateStorage() throws IOException {
        
    }

}
