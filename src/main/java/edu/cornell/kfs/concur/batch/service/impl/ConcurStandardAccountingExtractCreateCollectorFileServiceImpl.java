package edu.cornell.kfs.concur.batch.service.impl;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCreateCollectorFileService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractCreateCollectorFileServiceImpl
        implements ConcurStandardAccountingExtractCreateCollectorFileService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            ConcurStandardAccountingExtractCreateCollectorFileServiceImpl.class);

    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;

    @Override
    public boolean buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents) {
        // TODO: Once Jay's changes are merged, use a method reference for the new validation service method!
        ConcurStandardAccountingExtractCollectorBatchBuilder builder = new ConcurStandardAccountingExtractCollectorBatchBuilder(
                universityDateService::getFiscalYear, dateTimeService::toString,
                (saeLine) -> true, this::getConcurParameterValueAsString);
        
        CollectorBatch collectorBatch = builder.buildCollectorBatchFromStandardAccountingExtract(0, saeFileContents);
        if (collectorBatch == null) {
            LOG.error("There was a problem preparing the data for the Collector file; will not create a file.");
            return false;
        }
        
        return true;
    }

    protected String getConcurParameterValueAsString(String parameterName) {
        return parameterService.getParameterValueAsString("KFS-???", "????", parameterName);
    }

    public void setConcurSAEValidationService(ConcurStandardAccountingExtractValidationService concurSAEValidationService) {
        this.concurSAEValidationService = concurSAEValidationService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
