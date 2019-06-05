package edu.cornell.kfs.fp.batch.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AmazonBillResultsDTO {
    public String masterAccountNumber;
    public String masterAccountName;
    public boolean successfullyProcessed;
    public int numberOfAwsAccountInCloudCheckr;
    public int xmlCreationCount;
    public List<String> awsAccountWithoutDefaultAccount;
    public List<String> awsAccountWithExistingDI;
    public List<String> awsAccountGeneratedDIxml;
    public List<AmazonKfsAccountDTO> defaultAccountErrors;
    public List<AmazonKfsAccountDTO> costCenterErrors;
    
    private static final Logger LOG = LogManager.getLogger(AmazonBillResultsDTO.class);
    
    public AmazonBillResultsDTO() {
        awsAccountWithoutDefaultAccount = new ArrayList<String>();
        awsAccountWithExistingDI = new ArrayList<String>();
        awsAccountGeneratedDIxml = new ArrayList<String>();
        defaultAccountErrors = new ArrayList<AmazonKfsAccountDTO>();
        costCenterErrors = new ArrayList<AmazonKfsAccountDTO>();
    }
    
    public void logResults() {
        String headerFooter = "*****************************";
        LOG.info(headerFooter);
        LOG.info("logResults, masterAccountNumber: " + masterAccountNumber + " masterAccountName: " + masterAccountName);
        LOG.info("logResults, successfullyProcessed: " + successfullyProcessed);
        LOG.info("logResults, numberOfAwsAccountInCloudCheckr: " + numberOfAwsAccountInCloudCheckr);
        LOG.info("logResults, xmlCreationCount: " + xmlCreationCount);
        LOG.info("logResults, awsAccountWithoutDefaultAccount: " + awsAccountWithoutDefaultAccount);
        LOG.info("logResults, awsAccountWithExistingDI: " + awsAccountWithExistingDI);
        LOG.info("logResults, awsAccountGeneratedDIxml: " + awsAccountGeneratedDIxml);
        if (!defaultAccountErrors.isEmpty()) {
            LOG.info("logResults, default KFS accounts with errors: " + defaultAccountErrors);
        }
        if (!costCenterErrors.isEmpty()) {
            LOG.info("logResults, cost centers with errors: " + costCenterErrors);
        }
        LOG.info(headerFooter);
    }
}
