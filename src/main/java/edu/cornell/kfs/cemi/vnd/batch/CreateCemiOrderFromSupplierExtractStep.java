package edu.cornell.kfs.cemi.vnd.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.vnd.batch.service.CemiOrderFromSupplierExtractService;

/**
 * Batch step for creating the CEMI Order From Supplier Connection extract file.
 * 
 * This step generates an Excel file containing order-from supplier connection data
 * for loading into Workday.
 */
public class CreateCemiOrderFromSupplierExtractStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private CemiOrderFromSupplierExtractService cemiOrderFromSupplierExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        LOG.info("execute, Starting CEMI Order From Supplier Extract Step");

        try {
            cemiOrderFromSupplierExtractService.resetState();
            cemiOrderFromSupplierExtractService.initializeExtractDateSettings();
            cemiOrderFromSupplierExtractService.populateListOfKfsVendorAddressMappings();
            cemiOrderFromSupplierExtractService.populateListOfSupplierAddressMappings();
            cemiOrderFromSupplierExtractService.populateListOfInScopeAddresses();
            cemiOrderFromSupplierExtractService.generateIntermediateOrderFromSupplierExtractData(jobRunDate);
            cemiOrderFromSupplierExtractService.generateOrderFromSupplierExtractFile(jobRunDate);
            LOG.info("execute, CEMI Order From Supplier Extract Step completed successfully");
            return true;
        } catch (final Exception e) {
            LOG.error("execute, Error during CEMI Order From Supplier Extract Step", e);
            throw e;
        }
    }

    public void setCemiOrderFromSupplierExtractService(
            final CemiOrderFromSupplierExtractService cemiOrderFromSupplierExtractService) {
        this.cemiOrderFromSupplierExtractService = cemiOrderFromSupplierExtractService;
    }

}
