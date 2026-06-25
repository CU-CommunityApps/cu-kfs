package edu.cornell.kfs.cemi.sys.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.sys.batch.service.CemiCsvDataImportService;

public class ImportCemiLegacyDataStep extends AbstractStep {

    private CemiCsvDataImportService cemiCsvDataImportService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        final CemiCsvBatchInputFileType inputFileType = cemiCsvDataImportService.getBatchInputFileTypeForProcessing();
        cemiCsvDataImportService.truncateDestinationTableFor(inputFileType);
        cemiCsvDataImportService.importCsvDataFor(inputFileType);
        return true;
    }

    public void setCemiCsvDataImportService(final CemiCsvDataImportService cemiCsvDataImportService) {
        this.cemiCsvDataImportService = cemiCsvDataImportService;
    }

}
