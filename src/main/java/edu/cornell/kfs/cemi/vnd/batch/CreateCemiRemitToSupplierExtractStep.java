package edu.cornell.kfs.cemi.vnd.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierExtractService;

/**
 * Batch step for creating the CEMI Remit To Supplier Connection extract file.
 * 
 * This step generates an Excel file containing remit-to supplier connection data
 * for loading into Workday.
 */
public class CreateCemiRemitToSupplierExtractStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private CemiRemitToSupplierExtractService cemiRemitToSupplierExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        LOG.info("execute, Starting CEMI Remit To Supplier Extract Step");

        try {
            cemiRemitToSupplierExtractService.resetState();
            cemiRemitToSupplierExtractService.initializeExtractDateSettings();
            cemiRemitToSupplierExtractService.populateListOfInScopeAddresses();
            cemiRemitToSupplierExtractService.generateIntermediateRemitToSupplierExtractData(jobRunDate);
            cemiRemitToSupplierExtractService.generateRemitToSupplierExtractFile(jobRunDate);
            LOG.info("execute, CEMI Remit To Supplier Extract Step completed successfully");
            return true;
        } catch (final Exception e) {
            LOG.error("execute, Error during CEMI Remit To Supplier Extract Step", e);
            throw e;
        }
    }

    public void setCemiRemitToSupplierExtractService(
            final CemiRemitToSupplierExtractService cemiRemitToSupplierExtractService) {
        this.cemiRemitToSupplierExtractService = cemiRemitToSupplierExtractService;
    }


}