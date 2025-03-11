package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFileSections;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayment;
import edu.cornell.kfs.tax.batch.dto.SprintaxRowData;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;
import edu.cornell.kfs.tax.service.TransactionOverrideService;
import edu.cornell.kfs.tax.util.TaxUtils;

/*
 * This service will be fully implemented in a follow-up user story.
 */
public class TaxFileGenerationServiceSprintaxImpl implements TaxFileGenerationService, TransactionDetailHandler {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;
    private TransactionOverrideService transactionOverrideService;
    private ConfigurationService configurationService;
    private TaxParameterService taxParameterService;
    private String sprintaxBioFileDefinitionFilePath;
    private String sprintaxPaymentsFileDefinitionFilePath;
    private String fileOutputDirectory;
    private String payerEIN;
    private boolean scrubOutput;

    @Override
    public Object generateFiles(final TaxBatchConfig config) throws IOException, SQLException {
        Validate.notNull(config, "config cannot be null");
        Validate.isTrue(config.getMode() == TaxBatchConfig.Mode.CREATE_TAX_FILES,
                "config should have specified CREATE_TAX_FILES mode");

        return transactionDetailProcessorDao.processTransactionDetails(config, this);
    }

    @Override
    public TaxStatistics performProcessing(final TaxBatchConfig config,
            final TaxDtoRowMapper<TransactionDetail> rowMapper) throws Exception {
        final Map<String, String> transactionOverrides = TaxUtils.buildTransactionOverridesMap(
                transactionOverrideService, config);
        final TaxOutputDefinitionV2 bioFileDefinition = TaxUtils.parseTaxOutputDefinition(
                taxOutputDefinitionV2FileType, sprintaxBioFileDefinitionFilePath);
        final TaxOutputDefinitionV2 paymentsFileDefinition = TaxUtils.parseTaxOutputDefinition(
                taxOutputDefinitionV2FileType, sprintaxPaymentsFileDefinitionFilePath);
        final String bioFilePath = TaxUtils.buildCsvTaxFilePath(
                CUTaxConstants.Sprintax.BIO_OUTPUT_FILE_PREFIX, fileOutputDirectory, config.getProcessingStartDate());
        final String paymentsFilePath = TaxUtils.buildCsvTaxFilePath(
                CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX, fileOutputDirectory,
                        config.getProcessingStartDate());

        try (
                final TaxFileRowWriterImpl bioFileWriter = 
                        new TaxFileRowWriterImpl(bioFileDefinition, TaxDtoFieldEnum.class, bioFilePath, scrubOutput);
                final TaxFileRowWriterImpl paymentsFileWriter = 
                        new TaxFileRowWriterImpl(paymentsFileDefinition, TaxDtoFieldEnum.class, paymentsFilePath,
                                scrubOutput);
        ) {
            final SprintaxHelper helper = new SprintaxHelper(config, rowMapper, bioFileWriter, paymentsFileWriter,
                    transactionOverrides);
            return readAndProcessTransactions(helper);
        }
    }

    private TaxStatistics readAndProcessTransactions(final SprintaxHelper helper) throws IOException, SQLException {
        SprintaxRowData currentData = null;

        helper.bioFileWriter.writeHeaderRow(TaxFileSections.SPRINTAX_BIOGRAPHIC_ROW_1042S);
        helper.paymentsFileWriter.writeHeaderRow(TaxFileSections.SPRINTAX_PAYMENT_ROW_1042S);

        while (helper.rowMapper.moveToNextRow()) {
            final TransactionDetail currentRow = helper.rowMapper.readCurrentRow();
            Validate.validState(StringUtils.isNotBlank(currentRow.getVendorTaxNumber()),
                    "A blank tax ID was detected on a 1042-S transaction for payee: %s", currentRow.getPayeeId());
            helper.increment(TaxStatType.NUM_TRANSACTION_ROWS);

            // TODO: Fix!
            
            /*if (currentData == null) {
                currentData = createNewInfo(currentRow, helper);
            } else {
                if (shouldPrintTaxFileRowBeforeProcessingNextTransaction(currentData, currentRow)) {
                    printTaxFileRow(currentData, helper);
                    if (!nextTransactionIsForDifferentVendor(currentData, currentRow)) {
                        currentData.setCurrentPayment(createNewPayment(currentRow, helper));
                    }
                }
                if (nextTransactionIsForDifferentVendor(currentData, currentRow)) {
                    currentData = createNewInfo(currentRow, helper);
                }
            }

            processCurrentRow(currentData, currentRow, helper);*/
        }

        /*if (currentData != null && shouldPrintTaxFileRow(currentData.getCurrentPayment())) {
            printTaxFileRow(currentData, helper);
        }*/

        return helper.statistics;
    }

    private SprintaxRowData createNewInfo(final TransactionDetail currentRow, final SprintaxHelper helper)
            throws SQLException {
        final SprintaxRowData nextInfo = new SprintaxRowData();
        //initializePayeeIdInformation(nextInfo, currentRow.getPayeeId());
        nextInfo.setPayerEIN(payerEIN);
        nextInfo.setTaxId(currentRow.getVendorTaxNumber());
        nextInfo.setCurrentPayment(createNewPayment(currentRow, helper));
        //initializeVendorData(nextInfo, helper);
        return nextInfo;
    }

    private SprintaxPayment createNewPayment(final TransactionDetail currentRow, final SprintaxHelper helper) {
        final SprintaxPayment nextPayment = new SprintaxPayment();
        //nextPayment.setUniqueFormId(generateUniqueFormId(currentRow));
        nextPayment.setIncomeCode(currentRow.getIncomeCode());
        nextPayment.setIncomeCodeSubType(currentRow.getIncomeCodeSubType());
        nextPayment.setGrossAmount(KualiDecimal.ZERO);
        nextPayment.setFederalTaxWithheldAmount(KualiDecimal.ZERO);
        nextPayment.setStateIncomeTaxWithheldAmount(KualiDecimal.ZERO);
        return nextPayment;
    }



    public void setTransactionDetailProcessorDao(final TransactionDetailProcessorDao transactionDetailProcessorDao) {
        this.transactionDetailProcessorDao = transactionDetailProcessorDao;
    }

    public void setTaxOutputDefinitionV2FileType(final TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType) {
        this.taxOutputDefinitionV2FileType = taxOutputDefinitionV2FileType;
    }

    public void setTransactionOverrideService(final TransactionOverrideService transactionOverrideService) {
        this.transactionOverrideService = transactionOverrideService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setTaxParameterService(final TaxParameterService taxParameterService) {
        this.taxParameterService = taxParameterService;
    }

    public void setSprintaxBioFileDefinitionFilePath(final String sprintaxBioFileDefinitionFilePath) {
        this.sprintaxBioFileDefinitionFilePath = sprintaxBioFileDefinitionFilePath;
    }

    public void setSprintaxPaymentsFileDefinitionFilePath(final String sprintaxPaymentsFileDefinitionFilePath) {
        this.sprintaxPaymentsFileDefinitionFilePath = sprintaxPaymentsFileDefinitionFilePath;
    }

    public void setFileOutputDirectory(final String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public void setPayerEIN(final String payerEIN) {
        this.payerEIN = payerEIN;
    }

    public void setScrubOutput(final boolean scrubOutput) {
        this.scrubOutput = scrubOutput;
    }



    private static final class SprintaxHelper implements TaxStatisticsHandler {
        private final TaxBatchConfig config;
        private final TaxDtoRowMapper<TransactionDetail> rowMapper;
        private final TaxFileRowWriterImpl bioFileWriter;
        private final TaxFileRowWriterImpl paymentsFileWriter;
        private final Map<String, String> transactionOverrides;
        private final TaxStatistics statistics;

        private SprintaxHelper(final TaxBatchConfig config, final TaxDtoRowMapper<TransactionDetail> rowMapper,
                final TaxFileRowWriterImpl bioFileWriter, final TaxFileRowWriterImpl paymentsFileWriter,
                final Map<String, String> transactionOverrides) {
            this.config = config;
            this.rowMapper = rowMapper;
            this.bioFileWriter = bioFileWriter;
            this.paymentsFileWriter = paymentsFileWriter;
            this.transactionOverrides = transactionOverrides;
            this.statistics = new TaxStatistics();
        }

        @Override
        public void increment(final TaxStatType entryType) {
            statistics.increment(entryType);
        }

        @Override
        public void increment(final TaxStatType baseEntryType, final String documentType) {
            statistics.increment(baseEntryType, documentType);
        }
    }

}
