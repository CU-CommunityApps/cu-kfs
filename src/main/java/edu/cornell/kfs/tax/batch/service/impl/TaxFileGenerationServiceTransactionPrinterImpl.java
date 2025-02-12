package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFileSections;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.impl.TransactionDetailMapperForPrintingRowContents;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public class TaxFileGenerationServiceTransactionPrinterImpl implements TaxFileGenerationService {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;
    private String outputDefinitionFilePath;
    private String fileOutputDirectory;
    private boolean maskSensitiveData;

    @Override
    public Object generateFiles(final TaxBatchConfig config) throws IOException, SQLException {
        Validate.notNull(config, "config cannot be null");
        Validate.isTrue(config.getMode() == TaxBatchConfig.Mode.PRINT_TRANSACTION_ROWS,
                "config should have specified PRINT_TRANSACTION_ROWS mode");

        return transactionDetailProcessorDao.processTransactionDetails(
                config, TransactionDetailMapperForPrintingRowContents::new, this::generateTransactionDetailFile);
    }

    private TaxStatistics generateTransactionDetailFile(final TaxBatchConfig config,
            final TransactionDetailRowMapper<TransactionDetail> rowMapper) throws Exception {
        final String fileName = generateTransactionDetailFileName(config);
        final TaxOutputDefinitionV2 outputDefinition = parseOutputDefinitionForPrintingTransactionRows();

        try (
                final WrappedCsvWriter csvWriter = new WrappedCsvWriter(fileName, outputDefinition);
                final TaxFileRowWriterTransactionPrinterImpl rowWriter = new TaxFileRowWriterTransactionPrinterImpl(
                        outputDefinition, csvWriter, maskSensitiveData);
        ) {
            return generateTransactionDetailFile(config, rowMapper, rowWriter);
        }
    }

    private TaxStatistics generateTransactionDetailFile(final TaxBatchConfig config,
            final TransactionDetailRowMapper<TransactionDetail> rowMapper,
            final TaxFileRowWriterTransactionPrinterImpl rowWriter) throws Exception {
        rowWriter.writeHeaderRow(TaxFileSections.PLAIN_TRANSACTION_DETAIL_ROW);

        while (rowMapper.moveToNextRow()) {
            final TransactionDetail transactionDetail = rowMapper.readCurrentRow();
            rowWriter.writeDataRow(TaxFileSections.PLAIN_TRANSACTION_DETAIL_ROW, transactionDetail);
        }

        // TODO: Implement more statistics handling (if necessary) in subsequent user stories.
        return new TaxStatistics();
    }

    private String generateTransactionDetailFileName(final TaxBatchConfig config) {
        final DateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        final String filePrefix = getFilePrefix(config);
        final String dateSuffix = dateFormat.format(config.getProcessingStartDate());

        return StringUtils.join(fileOutputDirectory, CUKFSConstants.SLASH,
                filePrefix, config.getReportYear(), dateSuffix, CUKFSConstants.TEXT_FILE_EXTENSION);
    }

    private String getFilePrefix(final TaxBatchConfig config) {
        switch (config.getTaxType()) {
            case CUTaxConstants.TAX_TYPE_1099:
                return CUTaxConstants.TAX_1099_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX;

            case CUTaxConstants.TAX_TYPE_1042S:
                return CUTaxConstants.TAX_1042S_TRANSACTION_DETAILS_OUTPUT_FILE_PREFIX;

            default:
                throw new IllegalStateException("Unrecognized tax type: " + config.getTaxType());
        }
    }

    private TaxOutputDefinitionV2 parseOutputDefinitionForPrintingTransactionRows() throws IOException {
        try (final InputStream inputStream = CuCoreUtilities.getResourceAsStream(outputDefinitionFilePath)) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return taxOutputDefinitionV2FileType.parse(fileContents);
        }
    }

    public void setTransactionDetailProcessorDao(final TransactionDetailProcessorDao transactionDetailProcessorDao) {
        this.transactionDetailProcessorDao = transactionDetailProcessorDao;
    }

    public void setTaxOutputDefinitionV2FileType(final TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType) {
        this.taxOutputDefinitionV2FileType = taxOutputDefinitionV2FileType;
    }

    public void setOutputDefinitionFilePath(final String outputDefinitionFilePath) {
        this.outputDefinitionFilePath = outputDefinitionFilePath;
    }

    public void setFileOutputDirectory(final String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public void setMaskSensitiveData(final boolean maskSensitiveData) {
        this.maskSensitiveData = maskSensitiveData;
    }

}
