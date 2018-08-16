package edu.cornell.kfs.fp.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.service.AmazonWebServicesBillingService;

public class LoadAWSBillsStep extends AbstractStep {
    
	private static final Logger LOG = LogManager.getLogger(LoadAWSBillsStep.class);
    
    private AmazonWebServicesBillingService amazonWebServicesBillingService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        SimpleDateFormat dateFormater = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
        LOG.info("execute(); jobName: " + jobName + " run date: " + dateFormater.format(jobRunDate));
        getAmazonWebServicesBillingService().generateDistributionOfIncomeDocumentsFromAWSService();
        LOG.info("execute(); finished");
        return true;
    }

    public AmazonWebServicesBillingService getAmazonWebServicesBillingService() {
        return amazonWebServicesBillingService;
    }

    public void setAmazonWebServicesBillingService(AmazonWebServicesBillingService amazonWebServicesBillingService) {
        this.amazonWebServicesBillingService = amazonWebServicesBillingService;
    }

}
