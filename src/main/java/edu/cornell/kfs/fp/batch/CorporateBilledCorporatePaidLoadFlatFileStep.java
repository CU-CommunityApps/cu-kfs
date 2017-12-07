package edu.cornell.kfs.fp.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.sys.service.ReportWriterService;

public class CorporateBilledCorporatePaidLoadFlatFileStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidLoadFlatFileStep.class);
    private ProcurementCardLoadTransactionsService procurementCardLoadTransactionsService;
    private BatchInputFileService batchInputFileService;
    private BatchInputFileType corporateBilledCorporatePaidInputFileType;
    private ReportWriterService reportWriterService;
    private FileStorageService fileStorageService;
    
    public boolean execute(String jobName, Date jobRunDate) {
        procurementCardLoadTransactionsService.cleanTransactionsTable();

        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(corporateBilledCorporatePaidInputFileType);
        
        ((WrappingBatchService) reportWriterService).initialize();
        
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad) {
            LOG.info("execute, file name: " + inputFileName);
            try {
                boolean currentFileProccessed = procurementCardLoadTransactionsService.loadProcurementCardFile(inputFileName, reportWriterService);
                if (currentFileProccessed) {
                    processedFiles.add(inputFileName);
                }
                
            } catch (RuntimeException e) {
                LOG.error("execute, There was an error proccessing " + inputFileName, e);
            }
        }

        ((WrappingBatchService) reportWriterService).destroy();

        removeDoneFiles(processedFiles);

        return true;
    }

    private void removeDoneFiles(List<String> dataFileNames) {
        fileStorageService.removeDoneFiles(dataFileNames);
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setProcurementCardLoadTransactionsService(ProcurementCardLoadTransactionsService procurementCardLoadTransactionsService) {
        this.procurementCardLoadTransactionsService = procurementCardLoadTransactionsService;
    }
    
    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setCorporateBilledCorporatePaidInputFileType(BatchInputFileType corporateBilledCorporatePaidInputFileType) {
        this.corporateBilledCorporatePaidInputFileType = corporateBilledCorporatePaidInputFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
}
