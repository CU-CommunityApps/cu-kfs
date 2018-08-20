package edu.cornell.kfs.concur.batch.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.BusinessObjectFlatFileSerializerService;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCashAdvanceService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCreateCollectorFileService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

@SuppressWarnings("deprecation")
public class ConcurStandardAccountingExtractCreateCollectorFileServiceImpl
        implements ConcurStandardAccountingExtractCreateCollectorFileService {

	private static final Logger LOG = LogManager.getLogger(
            ConcurStandardAccountingExtractCreateCollectorFileServiceImpl.class);

    protected static final String DATE_RANGE_FORMAT = "%s..%s";

    protected ConcurStandardAccountingExtractValidationService concurSAEValidationService;
    protected ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService;
    protected ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService;
    protected BusinessObjectFlatFileSerializerService collectorFlatFileSerializerService;
    protected LookupableHelperService batchFileLookupableHelperService;
    protected ConfigurationService configurationService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected OptionsService optionsService;
    protected UniversityDateService universityDateService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected String collectorDirectoryPath;

    @Override
    public String buildCollectorFile(ConcurStandardAccountingExtractFile saeFileContents,
            ConcurStandardAccountingExtractBatchReportData reportData) {
        if (saeFileContents == null) {
            throw new IllegalArgumentException("saeFileContents cannot be null");
        } else if (reportData == null) {
            throw new IllegalArgumentException("reportData cannot be null");
        }
        
        CollectorBatch collectorBatch = buildCollectorBatch(saeFileContents, reportData);
        if (collectorBatch == null) {
            LOG.error("buildCollectorFile(): There was a problem preparing the data for the Collector file;"
                    + " will not create a file. See earlier logs for details.");
            return StringUtils.EMPTY;
        }
        return writeToCollectorFile(saeFileContents.getOriginalFileName(), collectorBatch);
    }

    protected CollectorBatch buildCollectorBatch(ConcurStandardAccountingExtractFile saeFileContents,
            ConcurStandardAccountingExtractBatchReportData reportData) {
        try {
            ConcurStandardAccountingExtractCollectorBatchBuilder builder = createBatchBuilder();
            int sequenceNumber = calculateBatchSequenceNumber();
            return builder.buildCollectorBatchFromStandardAccountingExtract(sequenceNumber, saeFileContents, reportData);
        } catch (Exception e) {
            LOG.error("buildCollectorBatch(): Unexpected unhandled error encountered while generating the CollectorBatch object", e);
            reportData.addHeaderValidationError("Encountered an unexpected error while generating Collector data from the SAE file");
            return null;
        }
    }

    protected ConcurStandardAccountingExtractCollectorBatchBuilder createBatchBuilder() {
        return new ConcurStandardAccountingExtractCollectorBatchBuilder(
                concurRequestedCashAdvanceService, concurStandardAccountingExtractCashAdvanceService, configurationService, concurBatchUtilityService,
                optionsService, universityDateService, dateTimeService, concurSAEValidationService, this::getConcurParameterValueAsString);
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

    protected String writeToCollectorFile(String originalFileName, CollectorBatch collectorBatch) {
        String collectorFileName = buildCollectorFileName(originalFileName);
        String collectorFilePath = buildFullyQualifiedCollectorFileName(collectorFileName);
        boolean fileCreatedSuccessfully = collectorFlatFileSerializerService.serializeToFlatFile(collectorFilePath, collectorBatch);
        if (!fileCreatedSuccessfully) {
            LOG.error("writeToCollectorFile(): An error occurred while writing the data to the Collector file; see earlier logs for details.");
            return StringUtils.EMPTY;
        }
        return collectorFileName;
    }

    protected String buildCollectorFileName(String saeFileName) {
        String saeFileNameWithoutExtension = StringUtils.substringBeforeLast(saeFileName, KFSConstants.DELIMITER);
        return ConcurConstants.COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX + saeFileNameWithoutExtension
                + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
    }

    protected String buildFullyQualifiedCollectorFileName(String collectorFileName) {
        return collectorDirectoryPath + collectorFileName;
    }

    public void setConcurSAEValidationService(ConcurStandardAccountingExtractValidationService concurSAEValidationService) {
        this.concurSAEValidationService = concurSAEValidationService;
    }

    public void setConcurRequestedCashAdvanceService(ConcurRequestedCashAdvanceService concurRequestedCashAdvanceService) {
        this.concurRequestedCashAdvanceService = concurRequestedCashAdvanceService;
    }

    public void setConcurStandardAccountingExtractCashAdvanceService(
            ConcurStandardAccountingExtractCashAdvanceService concurStandardAccountingExtractCashAdvanceService) {
        this.concurStandardAccountingExtractCashAdvanceService = concurStandardAccountingExtractCashAdvanceService;
    }

    public void setCollectorFlatFileSerializerService(BusinessObjectFlatFileSerializerService collectorFlatFileSerializerService) {
        this.collectorFlatFileSerializerService = collectorFlatFileSerializerService;
    }

    public void setBatchFileLookupableHelperService(LookupableHelperService batchFileLookupableHelperService) {
        this.batchFileLookupableHelperService = batchFileLookupableHelperService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public void setOptionsService(OptionsService optionsService) {
        this.optionsService = optionsService;
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
