package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.sys.CemiPropertyConstants.CemiColumnNames;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiSheetDao;
import edu.cornell.kfs.cemi.sys.batch.dataaccess.CemiTableMetadata;
import edu.cornell.kfs.cemi.sys.batch.service.CemiTableMetadataService;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiSheetDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiSupplierFileAppenderTableImpl extends CemiSupplierFileAppenderBase {

    private final CemiSheetDao cemiSheetDao;
    private final CemiTableMetadataService cemiTableMetadataService;
    private final String jobRunDateString;

    public CemiSupplierFileAppenderTableImpl(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate, final CemiSheetDao cemiSheetDao,
            final CemiTableMetadataService cemiTableMetadataService) {
        super(outputDefinition, jobRunDate);
        Validate.notNull(cemiSheetDao, "cemiSheetDao cannot be null");
        Validate.notNull(cemiTableMetadataService, "cemiTableMetadataService cannot be null");
        this.cemiSheetDao = cemiSheetDao;
        this.cemiTableMetadataService = cemiTableMetadataService;
        this.jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
    }

    @Override
    protected void populateSheetFromIntermediateDataStorage(
            final CemiSheetDefinition sheetDefinition, final CemiExcelWriter fileWriter) throws IOException {
        final String sheetName = sheetDefinition.getName();
        final CemiTableMetadata tableMetadata = cemiTableMetadataService.getCemiTableMetadata(
                outputDefinition, sheetName);
        final Map<String, Object> criteria = Collections.singletonMap(
                CemiColumnNames.EXTR_FILE_RUNDATE, jobRunDateString);
        final List<String> orderByFields = List.of(CemiColumnNames.ROW_INDEX);
        cemiSheetDao.processSheetTableRows(tableMetadata, criteria, orderByFields,
                sheetRow -> fileWriter.writeRow(sheetName, sheetRow));
    }

    @Override
    protected Stream<String[]> getCloseableSheetDataStreamFromIntermediateStorage(
            final CemiSheetDefinition sheetDefinition) throws IOException {
        throw new UnsupportedOperationException("not used");
        /*final CemiTableMetadata tableMetadata = cemiTableMetadataService.getCemiTableMetadata(
                outputDefinition, sheetDefinition.getName());
        final Map<String, Object> criteria = Collections.singletonMap(
                CemiColumnNames.EXTR_FILE_RUNDATE, jobRunDateString);
        final List<String> orderByFields = List.of(CemiColumnNames.ROW_INDEX);
        return cemiSheetDao.getSheetTableRowsFormattedForFileOutput(tableMetadata, criteria, orderByFields);*/
    }

    @Override
    public void cleanUpIntermediateStorage() throws IOException {

    }

}
