package edu.cornell.kfs.vnd.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonService;

public class ProcessVendorEmployeeComparisonResultFileStep extends AbstractStep {

    private static final Logger LOG = LogManager.getLogger();

    private VendorEmployeeComparisonService vendorEmployeeComparisonService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        final boolean result = vendorEmployeeComparisonService.processResultsOfVendorEmployeeComparison();
        if (!result) {
            LOG.error("execute, Unexpected errors were encountered while processing one or more result files; "
                    + "see prior logs for details.");
            throw new RuntimeException("Failed to process results of vendor employee comparison");
        }
        return true;
    }

    public void setVendorEmployeeComparisonService(
            final VendorEmployeeComparisonService vendorEmployeeComparisonService) {
        this.vendorEmployeeComparisonService = vendorEmployeeComparisonService;
    }

}
