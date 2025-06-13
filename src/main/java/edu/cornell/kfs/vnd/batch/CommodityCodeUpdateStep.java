package edu.cornell.kfs.vnd.batch;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.vnd.batch.service.CommodityCodeUpdateService;

public class CommodityCodeUpdateStep extends AbstractStep {
	private static final Logger LOG = LogManager.getLogger(CommodityCodeUpdateStep.class);
    private CommodityCodeUpdateService commodityCodeUpdateService;
    private BatchInputFileService batchInputFileService;
    private BatchInputFileType commodityCodeInputFileType;

    /**
     * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
     */
    public boolean execute(String jobName, LocalDateTime jobRunDate) {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(commodityCodeInputFileType);

        boolean processSuccess = true;
        List<String> processedFiles = new ArrayList<String>();
        for (String inputFileName : fileNamesToLoad) {
            commodityCodeUpdateService.loadCommodityCodeFile(inputFileName);
            if (processSuccess) {
                processedFiles.add(inputFileName);
            }
        }

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
     * Sets the commodityCodeInputFileType attribute value.
     */
    public void setCommodityCodeInputFileType(BatchInputFileType commodityCodeInputFileType) {
        this.commodityCodeInputFileType = commodityCodeInputFileType;
    }

    /**
     * @param commodityCodeUpdateService The commodityCodeUpdateService to set.
     */
    public void setCommodityCodeUpdateService(CommodityCodeUpdateService commodityCodeUpdateService) {
        this.commodityCodeUpdateService = commodityCodeUpdateService;
    }
}
