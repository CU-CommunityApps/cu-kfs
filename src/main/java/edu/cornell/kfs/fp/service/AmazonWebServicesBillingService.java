package edu.cornell.kfs.fp.service;

public interface AmazonWebServicesBillingService {
    
    /**
     * Generates Distribution of Income documents based on the results retrieved from an Amazon web service that provides details
     * on monthly charges and what KFS accounts to charge.
     */
    void generateDistributionOfIncomeDocumentsFromAWSService();

}
