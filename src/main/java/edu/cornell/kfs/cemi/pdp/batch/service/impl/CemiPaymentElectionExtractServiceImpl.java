package  edu.cornell.kfs.cemi.pdp.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionConstants;
import edu.cornell.kfs.cemi.pdp.batch.CreateCemiPaymentElectionExtractStep;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionExtractService;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiPaymentElectionExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiPaymentElectionExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao;
    private CemiPaymentElectionDao cemiPaymentElectionDao;
    private AchBankService achBankService;
    private BusinessObjectService businessObjectService;

    public CemiPaymentElectionExtractServiceImpl(final Environment environment) {
        super(environment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable PayeeACHAccount KFS generated identifiers for "
                + "Payment Election from the previous run (if present)...");
        cemiPaymentElectionDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of extractable "
                + "PayeeACHAccount generated identifiers for Payment Election...");
        cemiPaymentElectionDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME);

        try (
                final Stream<PayeeACHAccount> payeeAchAccounts =
                        cemiPaymentElectionOrmDao.getPayeeAchAccountsForCemiPaymentElectionExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiPaymentElectionFileExtractDataBuilderDefaultImpl dataBuilder =
                    new CemiPaymentElectionFileExtractDataBuilderDefaultImpl(
                            businessObjectService, jobRunDateString, achBankService, cemiPaymentElectionOrmDao,
                            cemiPaymentElectionDao, shouldMaskCemiSensitiveData());
            final Iterator<PayeeACHAccount> payeeAchAccountsIterator = payeeAchAccounts.iterator();
            dataBuilder.writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage(payeeAchAccountsIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateDataConversionExtractFile, Starting creation of CEMI Extract file {}",
                CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME,
                CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_FILENAME_PREFIX);
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiPaymentElectionExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiPaymentElectionConstants.PAYMENT_ELECTION_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiPaymentElectionConstants.PAYMENT_ELECTION_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiPaymentElectionOrmDao(CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao) {
        this.cemiPaymentElectionOrmDao = cemiPaymentElectionOrmDao;
    }

    public void setCemiPaymentElectionDao(CemiPaymentElectionDao cemiPaymentElectionDao) {
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
