package edu.cornell.kfs.module.purap.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.batch.service.JaggaerUploadFileService;

public class JaggaerUploadSupplierXmlStep extends AbstractStep {
    protected JaggaerUploadFileService jaggaerUploadFileService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        jaggaerUploadFileService.uploadSupplierXMLFiles();
        return true;
    }

    public void setJaggaerUploadFileService(JaggaerUploadFileService jaggaerUploadFileService) {
        this.jaggaerUploadFileService = jaggaerUploadFileService;
    }

}
