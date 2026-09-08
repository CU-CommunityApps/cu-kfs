package  edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardParameterConstants;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiAwardExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardExtractService;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiAwardExtractServiceImpl extends CemiDataExtractServiceBase implements CemiAwardExtractService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private String reportsDirectory;
    private CemiAwardExtractOrmDao cemiAwardExtractOrmDao;
    private CemiAwardExtractDao cemiAwardExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    
    public CemiAwardExtractServiceImpl(final Environment environment) {
        super(environment);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Removing all transient data values from previous run (...if it exists)");
        cemiAwardExtractDao.clearingAllExistingBusinessObjectKeysAndSetupDataFromPreviousExecution();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeExtractDateSettings() {
        final String awardScheduleJobRunDate = getAwardScheduleJobRunDate();
        LOG.info("initializeExtractDateSettings, Setting Award Schedule extraction date-time used by any Award Extract "
                + "dependent queries to system parameter CEMI_AWARD_EXTRACT_AWARD_SCHEDULE_DATETIME value {}",
                awardScheduleJobRunDate);
        cemiAwardExtractDao.storeAwardScheduleExtractDependentQuerySettings(awardScheduleJobRunDate);
    }
    
    private String getAwardScheduleJobRunDate() {
        final String awardScheduleJobRunDate = parameterService.getParameterValueAsString(
                CreateCemiAwardExtractStep.class,
                CemiAwardParameterConstants.CEMI_AWARD_EXTRACT_AWARD_SCHEDULE_DATETIME);
        Validate.validState(StringUtils.isNotBlank(awardScheduleJobRunDate), "Parameter %s should not have been blank",
                CemiAwardParameterConstants.CEMI_AWARD_EXTRACT_AWARD_SCHEDULE_DATETIME);
        return awardScheduleJobRunDate;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable KFS business objects...");
        cemiAwardExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for Award spreadsheet "
                + "and placing in intermediate database table storage...");
        
        final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        final String skippedAwardsFilePath = buildPathForSkippedAwardsReportFile(jobRunDateString);
        try (
                final Stream<Award> awards = 
                        cemiAwardExtractOrmDao.getAwardsForCemiAwardExtractAsCloseableStream();
                
                final FileOutputStream fileStream = new FileOutputStream(skippedAwardsFilePath);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
                final BufferedWriter skippedAwardsWriter = new BufferedWriter(streamWriter);
        ) {
            
            final CemiAwardFileExtractDataBuilderDefaultImpl dataBuilder = 
                    new CemiAwardFileExtractDataBuilderDefaultImpl(
                            businessObjectService, jobRunDateString, dateTimeService, cemiAwardExtractOrmDao,
                            cemiAwardExtractDao, skippedAwardsWriter, shouldMaskCemiSensitiveData());
            final Iterator<Award> awardsIterator = awards.iterator();
            dataBuilder.writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage(awardsIterator);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private String buildPathForSkippedAwardsReportFile(final String jobRunDate) {
        return StringUtils.join(reportsDirectory, CemiAwardConstants.AWARD_EXTRACT_SKIPPED_AWARDS_FILE_PREFIX,
                jobRunDate, CUKFSConstants.TEXT_FILE_EXTENSION);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateAwardrExtractFile, Starting creation of CEMI Extract file {}",
                CemiAwardConstants.AWARD_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiAwardConstants.AWARD_EXTRACT_PLAIN_FILENAME,
                CemiAwardConstants.AWARD_EXTRACT_FILENAME_PREFIX);
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiAwardExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiAwardConstants.AWARD_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiAwardConstants.AWARD_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public String getReportsDirectory() {
        return reportsDirectory;
    }

    public void setReportsDirectory(String reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public void setCemiAwardExtractOrmDao(CemiAwardExtractOrmDao cemiAwardExtractOrmDao) {
        this.cemiAwardExtractOrmDao = cemiAwardExtractOrmDao;
    }

    public void setCemiAwardExtractDao(CemiAwardExtractDao cemiAwardExtractDao) {
        this.cemiAwardExtractDao = cemiAwardExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public CemiAwardExtractOrmDao getCemiAwardExtractOrmDao() {
        return cemiAwardExtractOrmDao;
    }

    public CemiAwardExtractDao getCemiAwardExtractDao() {
        return cemiAwardExtractDao;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }
}
