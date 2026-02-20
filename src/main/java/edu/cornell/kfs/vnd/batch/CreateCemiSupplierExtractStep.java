package edu.cornell.kfs.vnd.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.vnd.batch.service.CemiSupplierExtractService;

public class CreateCemiSupplierExtractStep extends AbstractStep {

    private CemiSupplierExtractService cemiSupplierExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        cemiSupplierExtractService.resetState();
        cemiSupplierExtractService.initializeVendorActivityDateRangeSettings();
        cemiSupplierExtractService.populateListOfInScopeVendors();
        cemiSupplierExtractService.generateSupplierExtractFile();
        return true;
    }

    public void setCemiSupplierExtractService(final CemiSupplierExtractService cemiSupplierExtractService) {
        this.cemiSupplierExtractService = cemiSupplierExtractService;
    }

}
