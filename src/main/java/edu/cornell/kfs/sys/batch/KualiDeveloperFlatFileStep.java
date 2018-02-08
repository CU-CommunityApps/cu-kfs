package edu.cornell.kfs.sys.batch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.sys.batch.service.KualiDeveloperFeedService;

public class KualiDeveloperFlatFileStep extends AbstractStep {

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType kualiDeveloperFlatInputFileType;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected KualiDeveloperFeedService kualiDeveloperFeedService;

    public boolean execute(String arg0, Date arg1) throws InterruptedException {
        List<String> fileNamesToLoad = batchInputFileService.listInputFileNamesWithDoneFile(kualiDeveloperFlatInputFileType);

        boolean processSuccess = false;
        List<String> doneFiles = new ArrayList<>();

        for (String fileNameToProcess : fileNamesToLoad) {
            if (fileNameToProcess != null) {
                kualiDeveloperFeedService.loadKualiDeveloperDataFromBatchFile(fileNameToProcess);
                doneFiles.add(fileNameToProcess);
            }
        }
        CuBatchFileUtils.removeDoneFiles(doneFiles);

        return processSuccess;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public BatchInputFileType getKualiDeveloperFlatInputFileType() {
        return kualiDeveloperFlatInputFileType;
    }

    public void setKualiDeveloperFlatInputFileType(BatchInputFileType kualiDeveloperFlatInputFileType) {
        this.kualiDeveloperFlatInputFileType = kualiDeveloperFlatInputFileType;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public KualiDeveloperFeedService getKualiDeveloperFeedService() {
        return kualiDeveloperFeedService;
    }

    public void setKualiDeveloperFeedService(
            KualiDeveloperFeedService kualiDeveloperFeedService) {
        this.kualiDeveloperFeedService = kualiDeveloperFeedService;
    }

}
