package  edu.cornell.kfs.cemi.pdp.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
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
import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionParameterConstants;
import edu.cornell.kfs.cemi.pdp.batch.CreateCemiPaymentElectionExtractStep;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionExtractService;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionFileExtractDataBuilder;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
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

    @Override
    public void logSystemParameterValues() {
        LOG.info("logSystemParameterValues, Environment lane is '{}', sensitive data will be {}",
                environment.getLane(), shouldMaskCemiSensitiveData() ? "masked" : "unmasked");
        CemiUtils.logParameterValues(parameterService, CreateCemiPaymentElectionExtractStep.class, List.of(
                CemiPaymentElectionParameterConstants.COPY_CEMI_PAYMENT_ELECTION_FILE_TO_OUTBOUND_FOLDER,
                CemiBaseParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable PayeeACHAccount KFS generated identifiers for"
                + "Payment Election from the previous run (if present)...");
        cemiPaymentElectionDao.clearExistingListOfExtractablePayeeAchAccountGeneratedIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of extractable "
                + "PayeeACHAccount generated identifiers for Payment Election...");
        cemiPaymentElectionDao.queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for Payment Election spreadsheet "
                + "and placing in intermediate storage...");

        try (
                final Stream<PayeeACHAccount> payeeAchAccounts =
                        cemiPaymentElectionOrmDao.getPayeeAchAccountIdsForCemiPaymentElectionExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiPaymentElectionFileExtractDataBuilder dataBuilder =
                    new CemiPaymentElectionFileExtractDataBuilderDefaultImpl(
                            businessObjectService, jobRunDateString, achBankService, shouldMaskCemiSensitiveData());
            final Iterator<PayeeACHAccount> payeeAchAccountIterator = payeeAchAccounts.iterator();
            dataBuilder.writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage(payeeAchAccountIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateDataConversionExtractFile, Starting creation of CEMI Payment Election Extract file...");
        generateFileForDataExtract(jobRunDate, CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME,
                CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_FILENAME_PREFIX);
    }

    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiPaymentElectionExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiPaymentElectionConstants.PAYMENT_ELECTION_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiPaymentElectionConstants.PAYMENT_ELECTION_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiPaymentElectionOrmDao(final CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao) {
        this.cemiPaymentElectionOrmDao = cemiPaymentElectionOrmDao;
    }

    public void setCemiPaymentElectionDao(final CemiPaymentElectionDao cemiPaymentElectionDao) {
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
    }

    public void setAchBankService(final AchBankService achBankService) {
        this.achBankService = achBankService;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
