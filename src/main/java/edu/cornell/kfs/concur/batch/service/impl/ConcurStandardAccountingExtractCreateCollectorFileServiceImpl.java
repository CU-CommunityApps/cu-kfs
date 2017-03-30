package edu.cornell.kfs.concur.batch.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCreateCollectorFileService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.batch.service.BusinessObjectFlatFileSerializerService;

@SuppressWarnings("deprecation")
public class ConcurStandardAccountingExtractCreateCollectorFileServiceImpl
        implements ConcurStandardAccountingExtractCreateCollectorFileService {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            ConcurStandardAccountingExtractCreateCollectorFileServiceImpl.class);

    protected static final String DATE_RANGE_FORMAT = "%s..%s";
    protected static final int DEFAULT_BUILDER_SIZE = 100;

    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected BusinessObjectFlatFileSerializerService collectorFlatFileSerializerService;
    protected LookupableHelperService batchFileLookupableHelperService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected String collectorDirectoryPath;

    @Override
    public boolean buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents) {
        ConcurStandardAccountingExtractCollectorBatchBuilder builder = new ConcurStandardAccountingExtractCollectorBatchBuilder(
                universityDateService, dateTimeService, concurSAEValidationService, this::getConcurParameterValueAsString);
        
        int sequenceNumber = calculateBatchSequenceNumber();
        CollectorBatch collectorBatch = builder.buildCollectorBatchFromStandardAccountingExtract(sequenceNumber, saeFileContents);
        if (collectorBatch == null) {
            LOG.error("There was a problem preparing the data for the Collector file; will not create a file. See earlier logs for details.");
            return false;
        }
        
        String newFileName = buildCollectorFileName(saeFileContents.getOriginalFileName());
        boolean success = collectorFlatFileSerializerService.serializeToFlatFile(newFileName, collectorBatch);
        
        if (!success) {
            LOG.error("An error occurred while writing the data to the Collector file; see earlier logs for details.");
        }
        return success;
    }

    protected String getConcurParameterValueAsString(String parameterName) {
        return parameterService.getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR,
                CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
    }

    /**
     * Collector files have a Batch Sequence Number field, which allows for processing multiple
     * related Collector files on the same day. The first file loaded on the current day
     * should have a sequence number of 1 as per the SAE-to-Collector specifications,
     * and each subsequent file for the same day needs to increment the sequence accordingly.
     */
    protected int calculateBatchSequenceNumber() {
        return 1 + countCollectorFilesGeneratedFromSAEFilesToday();
    }

    protected int countCollectorFilesGeneratedFromSAEFilesToday() {
        String wildcardFileName = ConcurConstants.COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX
                + KFSConstants.WILDCARD_CHARACTER + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
        java.sql.Date currentDate = dateTimeService.getCurrentSqlDateMidnight();
        String currentDateAsString = dateTimeService.toDateString(currentDate);
        String rangeForCurrentDate = String.format(DATE_RANGE_FORMAT, currentDateAsString, currentDateAsString);
        
        Map<String,String> criteria = new HashMap<>();
        criteria.put(CUKFSPropertyConstants.PATH, collectorDirectoryPath);
        criteria.put(KFSPropertyConstants.FILE_NAME, wildcardFileName);
        criteria.put(CUKFSPropertyConstants.LAST_MODIFIED_DATE, rangeForCurrentDate);
        
        List<? extends BusinessObject> searchResults = batchFileLookupableHelperService.getSearchResults(criteria);
        return searchResults.size();
    }

    protected String buildCollectorFileName(String saeFileName) {
        String saeFileNameWithoutExtension = StringUtils.removeEndIgnoreCase(
                saeFileName, GeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION);
        return new StringBuilder(DEFAULT_BUILDER_SIZE)
                .append(collectorDirectoryPath)
                .append(ConcurConstants.COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX)
                .append(saeFileNameWithoutExtension)
                .append(GeneralLedgerConstants.BatchFileSystem.EXTENSION)
                .toString();
    }

    public void setConcurSAEValidationService(ConcurStandardAccountingExtractValidationService concurSAEValidationService) {
        this.concurSAEValidationService = concurSAEValidationService;
    }

    public void setCollectorFlatFileSerializerService(BusinessObjectFlatFileSerializerService collectorFlatFileSerializerService) {
        this.collectorFlatFileSerializerService = collectorFlatFileSerializerService;
    }

    public void setBatchFileLookupableHelperService(LookupableHelperService batchFileLookupableHelperService) {
        this.batchFileLookupableHelperService = batchFileLookupableHelperService;
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

    public void setCollectorDirectoryPath(String collectorDirectoryPath) {
        this.collectorDirectoryPath = collectorDirectoryPath;
    }

}
