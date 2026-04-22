package edu.cornell.kfs.cemi.vnd.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.cemi.vnd.batch.service.CemiSupplierExtractService;

public class CreateCemiSupplierExtractStep extends AbstractStep {

    private CemiSupplierExtractService cemiSupplierExtractService;

    @Override
    public boolean execute(final String jobName, final LocalDateTime jobRunDate) throws InterruptedException {
        //Phase1: Obtain the dataset
        cemiSupplierExtractService.resetState();
        cemiSupplierExtractService.initializeVendorActivityDateRangeSettings();
        cemiSupplierExtractService.populateListOfBaseVendorData();
        cemiSupplierExtractService.populateListOfInScopeVendors();
        //Phase 2: Loop through result set to create all the csv files
        cemiSupplierExtractService.generateIntermediateSupplierExtractData(jobRunDate);
        //Phase 3: Create single multi-tabbed file.
        cemiSupplierExtractService.generateSupplierExtractFile(jobRunDate);
        return true;
    }

    public void setCemiSupplierExtractService(final CemiSupplierExtractService cemiSupplierExtractService) {
        this.cemiSupplierExtractService = cemiSupplierExtractService;
    }

}
