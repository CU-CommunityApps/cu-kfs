package edu.cornell.kfs.tax.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.tax.service.TaxProcessingV2Service;

public class SprintaxProcessingStep extends AbstractStep {

    private TaxProcessingV2Service taxProcessingV2Service;

    @Override
    public boolean execute(final String jobName, final Date jobRunDate) throws InterruptedException {
        taxProcessingV2Service.performTaxProcessingFor1042S(jobRunDate);
        return true;
    }

    public void setTaxProcessingV2Service(final TaxProcessingV2Service taxProcessingV2Service) {
        this.taxProcessingV2Service = taxProcessingV2Service;
    }

}
