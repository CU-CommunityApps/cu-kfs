/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.batch;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.batch.service.ProcurementCardLoadTransactionsService;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.service.ReportWriterService;

/**
 * This step will call a service method to load the kuali pcard xml file into the transaction table. Validates the data before the
 * load. Functions performed by this step: 1) Lookup path and filename from APC for the pcard input file 2) Load the pcard xml file
 * 3) Parse each transaction and validate against the data dictionary 4) Clean fp_prcrmnt_card_trn_t from the previous run 5) Load
 * new transactions into fp_prcrmnt_card_trn_t 6) Rename input file using the current date (backup) RESTART: All functions performed
 * withing a single transaction. Step can be restarted as needed.
 */
public class ProcurementCardLoadFlatFileStep extends AbstractStep {
    private static final Logger LOG = LogManager.getLogger();
    private ProcurementCardLoadTransactionsService procurementCardLoadTransactionsService;
    private BatchInputFileService batchInputFileService;
    private BatchInputFileType procurementCardInputFileType;
    private ReportWriterService reportWriterService;
    
    /**
     * Controls the procurement card process.
     */
    public boolean execute(String jobName, LocalDateTime jobRunDate) {
        procurementCardLoadTransactionsService.cleanTransactionsTable();

        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(procurementCardInputFileType);
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

    /**
     * Clears out associated .done files for the processed data files.
     */
    private void removeDoneFiles(List<String> dataFileNames) {
        for (String dataFileName : dataFileNames) {
            File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
            if (doneFile.exists()) {
                doneFile.delete();
            }
        }
    }

    /**
     * Sets the batchInputFileService attribute value.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * Sets the procurementCardInputFileType attribute value.
     */
    public void setProcurementCardInputFileType(BatchInputFileType procurementCardInputFileType) {
        this.procurementCardInputFileType = procurementCardInputFileType;
    }

    /**
     * Sets the procurementCardLoadTransactionsService attribute value.
     */
    public void setProcurementCardLoadTransactionsService(ProcurementCardLoadTransactionsService procurementCardLoadTransactionsService) {
        this.procurementCardLoadTransactionsService = procurementCardLoadTransactionsService;
    }
    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }
}
