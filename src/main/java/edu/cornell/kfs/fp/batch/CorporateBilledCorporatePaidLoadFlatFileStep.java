package edu.cornell.kfs.fp.batch;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.service.ReportWriterService;

public class CorporateBilledCorporatePaidLoadFlatFileStep extends AbstractStep {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidLoadFlatFileStep.class);
    private ProcurementCardLoadTransactionsService procurementCardLoadTransactionsService;
    private BatchInputFileService batchInputFileService;
    private BatchInputFileType corporateBilledCorporatePaidInputFileType;
    private ReportWriterService reportWriterService;
    
    public boolean execute(String jobName, Date jobRunDate) {
        procurementCardLoadTransactionsService.cleanTransactionsTable();

        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(corporateBilledCorporatePaidInputFileType);
        
        ((WrappingBatchService) reportWriterService).initialize();
        
        boolean processSuccess = true;
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad) {
            LOG.info("execute, file name: " + inputFileName);
            processSuccess = procurementCardLoadTransactionsService.loadProcurementCardFile(inputFileName,reportWriterService);
            if (processSuccess) {
                processedFiles.add(inputFileName);
            }
        }

        ((WrappingBatchService) reportWriterService).destroy();

        removeDoneFiles(processedFiles);

        return processSuccess;
    }

    private void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
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
}
