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
    public List<AmazonKfsAccountDTO> defaultAccountsWithErrors;
    public List<AmazonKfsAccountDTO> costCentersWithErrors;
    
    private static final Logger LOG = LogManager.getLogger(AmazonBillResultsDTO.class);
    
    public AmazonBillResultsDTO() {
        awsAccountWithoutDefaultAccount = new ArrayList<String>();
        awsAccountWithExistingDI = new ArrayList<String>();
        awsAccountGeneratedDIxml = new ArrayList<String>();
        defaultAccountsWithErrors = new ArrayList<AmazonKfsAccountDTO>();
        costCentersWithErrors = new ArrayList<AmazonKfsAccountDTO>();
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
        LOG.info("logResults, number of default accounts with errors: " + defaultAccountsWithErrors.size());
        if (!defaultAccountsWithErrors.isEmpty()) {
            LOG.info("logResults, default KFS accounts with errors: " + defaultAccountsWithErrors);
        }
        LOG.info("logResults, number of cost centers with errors: " + costCentersWithErrors.size());
        if (!costCentersWithErrors.isEmpty()) {
            LOG.info("logResults, cost centers with errors: " + costCentersWithErrors);
        }
        LOG.info(headerFooter);
    }
}
