package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiAwardBudgetConstants;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiAwardBudgetExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardBudgetExtractService;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardBudgetExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiAwardBudgetExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiAwardBudgetExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiAwardBudgetExtractOrmDao cemiAwardBudgetExtractOrmDao;
    private CemiAwardBudgetExtractDao cemiAwardBudgetExtractDao;
    private BusinessObjectService businessObjectService;

    public CemiAwardBudgetExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable business objects from"
                + " the previous run (if present)...");
        cemiAwardBudgetExtractDao.clearExistingListOfExtractableProposalNumbers();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable business objects...");
        cemiAwardBudgetExtractDao.queryAndStoreAwardProposalNumbersForAwardBudgetExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiAwardBudgetConstants.AWARD_BUDGET_EXTRACT_PLAIN_FILENAME);

        try (
                final Stream<Award> awards =
                        cemiAwardBudgetExtractOrmDao.getAwardsForCemiAwardBudgetExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiAwardBudgetFileExtractDataBuilderDefaultImpl dataBuilder =
                    new CemiAwardBudgetFileExtractDataBuilderDefaultImpl(businessObjectService, jobRunDateString);
            final Iterator<Award> awardsIterator = awards.iterator();
            dataBuilder.writeAwardBudgetFileBudgetTemplateTabExtractDataToIntermediateStorage(awardsIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateDataConversionExtractFile, Starting creation of CEMI Extract file {}",
                CemiAwardBudgetConstants.AWARD_BUDGET_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiAwardBudgetConstants.AWARD_BUDGET_EXTRACT_PLAIN_FILENAME,
                CemiAwardBudgetConstants.AWARD_BUDGET_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiAwardBudgetExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiAwardBudgetConstants.AWARD_BUDGET_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiAwardBudgetConstants.AWARD_BUDGET_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiAwardBudgetExtractOrmDao(CemiAwardBudgetExtractOrmDao cemiAwardBudgetExtractOrmDao) {
        this.cemiAwardBudgetExtractOrmDao = cemiAwardBudgetExtractOrmDao;
    }

    public void setCemiAwardBudgetExtractDao(CemiAwardBudgetExtractDao cemiAwardBudgetExtractDao) {
        this.cemiAwardBudgetExtractDao = cemiAwardBudgetExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
