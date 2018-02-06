package edu.cornell.kfs.fp.batch.service.impl;

import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.batch.CreateAccounntingDocumentReportItem;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportSerivce;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CreateAccountingDocumentReportSerivceImpl implements CreateAccountingDocumentReportSerivce {
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void generateReport(CreateAccounntingDocumentReportItem reportItem) {
        // TODO Auto-generated method stub

    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

}
