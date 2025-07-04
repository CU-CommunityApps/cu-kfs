package edu.cornell.kfs.tax.batch;

import java.time.LocalDateTime;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.service.TaxProcessingService;

/**
 * Batch step that performs 1099 or 1042S tax processing
 * for a given year (or date range within a year).
 */
public class TaxProcessingStep extends AbstractStep {

    private TaxProcessingService taxProcessingService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        String taxType = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                CUTaxConstants.TAX_NAMESPACE, CUTaxConstants.TAX_PARM_DETAIL, TaxCommonParameterNames.TAX_TYPE);
        taxProcessingService.doTaxProcessing(taxType, jobRunDate);
        return true;
    }

    public void setTaxProcessingService(TaxProcessingService taxProcessingService) {
        this.taxProcessingService = taxProcessingService;
    }
}
