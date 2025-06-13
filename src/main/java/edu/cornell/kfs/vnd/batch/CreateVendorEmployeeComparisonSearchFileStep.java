package edu.cornell.kfs.vnd.batch;

import java.time.LocalDateTime;
import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonService;

public class CreateVendorEmployeeComparisonSearchFileStep extends AbstractStep {

    private VendorEmployeeComparisonService vendorEmployeeComparisonService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        vendorEmployeeComparisonService.generateFileContainingPotentialVendorEmployees();
        return true;
    }

    public void setVendorEmployeeComparisonService(
            final VendorEmployeeComparisonService vendorEmployeeComparisonService) {
        this.vendorEmployeeComparisonService = vendorEmployeeComparisonService;
    }

}
