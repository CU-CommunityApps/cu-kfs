package edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiImportBudgetConstants;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiImportBudgetExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiImportBudgetExtractService;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiImportBudgetExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;

public class CemiImportBudgetExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiImportBudgetExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiImportBudgetExtractOrmDao cemiImportBudgetExtractOrmDao;
    private CemiImportBudgetExtractDao cemiImportBudgetExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;

    public CemiImportBudgetExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable business objects from"
                + " the previous run (if present)...");
        cemiImportBudgetExtractDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable business objects...");
        cemiImportBudgetExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiImportBudgetConstants.IMPORT_BUDGET_EXTRACT_PLAIN_FILENAME);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateDataConversionExtractFile, Starting creation of CEMI Import Budget Extract file {}",
                CemiImportBudgetConstants.IMPORT_BUDGET_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiImportBudgetConstants.IMPORT_BUDGET_EXTRACT_PLAIN_FILENAME,
                CemiImportBudgetConstants.IMPORT_BUDGET_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiImportBudgetExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiImportBudgetConstants.IMPORT_BUDGET_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiImportBudgetConstants.IMPORT_BUDGET_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiImportBudgetExtractOrmDao(CemiImportBudgetExtractOrmDao cemiImportBudgetExtractOrmDao) {
        this.cemiImportBudgetExtractOrmDao = cemiImportBudgetExtractOrmDao;
    }

    public void setCemiImportBudgetExtractDao(CemiImportBudgetExtractDao cemiImportBudgetExtractDao) {
        this.cemiImportBudgetExtractDao = cemiImportBudgetExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
