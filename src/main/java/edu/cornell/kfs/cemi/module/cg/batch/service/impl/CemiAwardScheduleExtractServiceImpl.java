package  edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiAwardScheduleConstants;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiAwardScheduleExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardScheduleExtractService;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardScheduleExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiAwardScheduleExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiAwardScheduleExtractService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao;
    private CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    
    public CemiAwardScheduleExtractServiceImpl(final Environment environment) {
        super(environment);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable Awards for Award Schedule from the previous run (if present)...");
        cemiAwardScheduleExtractDao.clearExistingListOfExtractableProposalNumbers();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of extractable Awards for Award Schedule...");
        cemiAwardScheduleExtractDao.queryAndStoreAwardProposalNumbersForAwardScheduleExtract();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for Award Schedule spreadsheet "
                + "and placing in intermediate storage...");
        final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        final Stream<Award> awards = cemiAwardScheduleExtractOrmDao.getAwardsForCemiAwardScheduleExtractAsCloseableStream();
        final CemiAwardScheduleFileExtractDataBuilderDefaultImpl dataBuilder = new CemiAwardScheduleFileExtractDataBuilderDefaultImpl(
                businessObjectService, jobRunDateString, dateTimeService, cemiAwardScheduleExtractOrmDao,
                cemiAwardScheduleExtractDao, shouldMaskCemiSensitiveData());
        final Iterator<Award> awardsIterator = awards.iterator();
        dataBuilder.writeAwardScheduleFileAwardScheduleTabExtractDataToIntermediateStorage(awardsIterator);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateAwardSchedulerExtractFile, Starting creation of CEMI AwardSchedule Extract file...");
        generateFileForDataExtract(jobRunDate, CemiAwardScheduleConstants.AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME,
                CemiAwardScheduleConstants.AWARD_SCHEDULE_EXTRACT_FILENAME_PREFIX);
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataMaskingParameter() {
        return CreateCemiAwardScheduleExtractStep.class;
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForCopyFileToOutboundFolderParameter() {
        return CreateCemiAwardScheduleExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiAwardScheduleConstants.AWARD_SCHEDULE_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiAwardScheduleConstants.AWARD_SCHEDULE_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiAwardScheduleExtractOrmDao(CemiAwardScheduleExtractOrmDao cemiAwardScheduleExtractOrmDao) {
        this.cemiAwardScheduleExtractOrmDao = cemiAwardScheduleExtractOrmDao;
    }

    public void setCemiAwardScheduleExtractDao(CemiAwardScheduleExtractDao cemiAwardScheduleExtractDao) {
        this.cemiAwardScheduleExtractDao = cemiAwardScheduleExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
