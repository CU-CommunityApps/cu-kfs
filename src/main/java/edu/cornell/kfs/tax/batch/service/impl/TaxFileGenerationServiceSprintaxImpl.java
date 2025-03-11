package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFileSections;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayee;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayment;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
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
        SprintaxPayee currentPayee = null;

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



    private SprintaxPayee createNewPayee(final TransactionDetail currentRow, final SprintaxHelper helper)
            throws SQLException {
        final SprintaxPayee nextPayee = new SprintaxPayee();
        initializePayeeIdInformation(nextPayee, currentRow.getPayeeId());
        nextPayee.setPayerEIN(payerEIN);
        nextPayee.setTaxId(currentRow.getVendorTaxNumber());
        nextPayee.setCurrentPayment(createNewPayment(currentRow, helper));
        initializeVendorData(nextPayee, helper);
        return nextPayee;
    }

    private SprintaxPayment createNewPayment(final TransactionDetail currentRow, final SprintaxHelper helper) {
        final SprintaxPayment nextPayment = new SprintaxPayment();
        nextPayment.setUniqueFormId(generateUniqueFormId(currentRow));
        nextPayment.setIncomeCode(currentRow.getIncomeCode());
        nextPayment.setIncomeCodeSubType(currentRow.getIncomeCodeSubType());
        nextPayment.setGrossAmount(KualiDecimal.ZERO);
        nextPayment.setFederalTaxWithheldAmount(KualiDecimal.ZERO);
        nextPayment.setStateIncomeTaxWithheldAmount(KualiDecimal.ZERO);
        return nextPayment;
    }

    private String generateUniqueFormId(final TransactionDetail currentRow) {
        final String transactionDetailId = currentRow.getTransactionDetailId();
        return StringUtils.length(transactionDetailId) > 10
                ? StringUtils.right(transactionDetailId, 10)
                : transactionDetailId;
    }

    private void initializePayeeIdInformation(final SprintaxPayee nextPayee, final String payeeId) {
        final String vendorHeaderId = StringUtils.substringBefore(payeeId, KFSConstants.DASH);
        final String vendorDetailId = StringUtils.substringAfter(payeeId, KFSConstants.DASH);
        nextPayee.setPayeeId(payeeId);
        nextPayee.setVendorHeaderId(Integer.valueOf(vendorHeaderId));
        nextPayee.setVendorDetailId(Integer.valueOf(vendorDetailId));
    }

    private void initializeVendorData(final SprintaxPayee nextPayee, final SprintaxHelper helper)
            throws SQLException {
        final VendorQueryResults vendorResults = transactionDetailProcessorDao.getVendor(
                nextPayee.getVendorHeaderId(), nextPayee.getVendorDetailId());
        final VendorDetailLite matchingVendor = vendorResults.getVendor();
        final VendorDetailLite vendorToProcess;

        if (ObjectUtils.isNull(matchingVendor)) {
            helper.increment(TaxStatType.NUM_NO_VENDOR);
            nextPayee.setVendorName(createMessage(CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_1042S_VENDOR_NOT_FOUND,
                    nextPayee.getVendorHeaderId(), nextPayee.getVendorDetailId()));
            vendorToProcess = null;
        } else if (matchingVendor.isVendorParentIndicator()) {
            vendorToProcess = matchingVendor;
        } else if (ObjectUtils.isNull(vendorResults.getParentVendor())) {
            initializeSoleProprietorParentVendorNameIfNecessary(nextPayee, matchingVendor, helper);
            helper.increment(TaxStatType.NUM_NO_PARENT_VENDOR);
            nextPayee.setVendorName(createMessage(
                    CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_1042S_VENDOR_PARENT_NOT_FOUND,
                    nextPayee.getVendorHeaderId(), nextPayee.getVendorDetailId()));
            vendorToProcess = null;
        } else {
            initializeSoleProprietorParentVendorNameIfNecessary(nextPayee, matchingVendor, helper);
            vendorToProcess = vendorResults.getParentVendor();
        }

        if (ObjectUtils.isNotNull(vendorToProcess)) {
            nextPayee.setVendorTypeCode(vendorToProcess.getVendorTypeCode());
            nextPayee.setVendorOwnershipCode(vendorToProcess.getVendorOwnershipCode());
            nextPayee.setVendorGIIN(vendorToProcess.getVendorGIIN());
            nextPayee.setChapter4StatusCode(vendorToProcess.getVendorChapter4StatusCode());
            initializeVendorName(nextPayee, vendorToProcess, helper);
            initializeChapter4ExemptionCode(nextPayee, vendorToProcess);
            initializeVendorAddressData(nextPayee, vendorToProcess, helper);
        } else {
            initializeWithDefaultChapter4ExemptionCode(nextPayee);
            if (ObjectUtils.isNotNull(matchingVendor)) {
                initializeVendorAddressData(nextPayee, matchingVendor, helper);
            }
        }

        if (StringUtils.isBlank(nextPayee.getVendorEmailAddress())) {
            nextPayee.setVendorEmailAddress(generatePlaceholderEmailAddress(nextPayee.getPayeeId()));
        }
    }

    /*
     * TODO: We need to revisit the parent-vendor-name setup and determine whether
     * it should be renamed to sub-vendor-name or something similar.
     */
    private void initializeSoleProprietorParentVendorNameIfNecessary(final SprintaxPayee nextPayee,
            final VendorDetailLite vendorDetail, final SprintaxHelper helper) {
        final String soleProprietorOwnershipCode = getSuffixedParameter(
                TaxCommonParameterNames.SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX);
        if (StringUtils.equals(vendorDetail.getVendorOwnershipCode(), soleProprietorOwnershipCode)) {
            nextPayee.setParentVendorName(vendorDetail.getVendorName());
        }
    }

    private void initializeVendorName(final SprintaxPayee nextPayee,
            final VendorDetailLite vendorDetail, final SprintaxHelper helper) {
        final String vendorName = vendorDetail.getVendorName();
        nextPayee.setVendorName(vendorName);
        if (StringUtils.isNotBlank(vendorName)) {
            helper.increment(TaxStatType.NUM_VENDOR_NAMES_PARSED);
            if (vendorDetail.isVendorFirstLastNameIndicator()
                    && StringUtils.contains(vendorName, KFSConstants.COMMA)) {
                nextPayee.setVendorLastName(StringUtils.trim(
                        StringUtils.substringBefore(vendorName, KFSConstants.COMMA)));
                nextPayee.setVendorFirstName(StringUtils.trim(
                        StringUtils.substringAfter(vendorName, KFSConstants.COMMA)));
            } else {
                nextPayee.setVendorLastName(StringUtils.trim(vendorName));
            }
        } else {
            helper.increment(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED);
        }
    }

    private void initializeChapter4ExemptionCode(final SprintaxPayee nextPayee, final VendorDetailLite vendor) {
        final String chapter4ExemptionCode = getSubParameter(
                Tax1042SParameterNames.CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES,
                vendor.getVendorChapter4StatusCode());
        if (StringUtils.isNotBlank(chapter4ExemptionCode)) {
            nextPayee.setChapter4ExemptionCode(chapter4ExemptionCode);
        } else {
            initializeWithDefaultChapter4ExemptionCode(nextPayee);
        }
    }

    private void initializeWithDefaultChapter4ExemptionCode(final SprintaxPayee nextPayee) {
        nextPayee.setChapter4ExemptionCode(getParameter(Tax1042SParameterNames.CHAPTER4_DEFAULT_EXEMPTION_CODE));
    }

    private void initializeVendorAddressData(final SprintaxPayee nextPayee,
            final VendorDetailLite vendor, final SprintaxHelper helper) throws SQLException {
        final VendorAddressLite usAddress = transactionDetailProcessorDao.getHighestPriorityUSVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
        final VendorAddressLite foreignAddress = transactionDetailProcessorDao.getHighestPriorityForeignVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());

        if (ObjectUtils.isNotNull(usAddress)) {
            nextPayee.setVendorUSAddressLine1(usAddress.getVendorLine1Address());
            nextPayee.setVendorUSAddressLine2(usAddress.getVendorLine2Address());
            nextPayee.setVendorUSCityName(usAddress.getVendorCityName());
            nextPayee.setVendorUSStateCode(usAddress.getVendorStateCode());
            nextPayee.setVendorUSZipCode(usAddress.getVendorZipCode());
            if (ObjectUtils.isNull(foreignAddress)) {
                nextPayee.setVendorEmailAddress(usAddress.getVendorAddressEmailAddress());
            }
        } else {
            nextPayee.setVendorUSAddressLine1(CUTaxConstants.NO_US_VENDOR_ADDRESS);
            helper.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_US);
        }

        if (ObjectUtils.isNotNull(foreignAddress)) {
            nextPayee.setVendorForeignAddressLine1(foreignAddress.getVendorLine1Address());
            nextPayee.setVendorForeignAddressLine2(foreignAddress.getVendorLine2Address());
            nextPayee.setVendorForeignCityName(foreignAddress.getVendorCityName());
            nextPayee.setVendorForeignProvinceName(foreignAddress.getVendorAddressInternationalProvinceName());
            nextPayee.setVendorForeignZipCode(foreignAddress.getVendorZipCode());
            nextPayee.setVendorForeignCountryCode(foreignAddress.getVendorCountryCode());
            nextPayee.setVendorEmailAddress(foreignAddress.getVendorAddressEmailAddress());
        } else {
            nextPayee.setVendorForeignAddressLine1(CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS);
            helper.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN);
        }
    }

    private String generatePlaceholderEmailAddress(final String payeeId) {
        final String emailFormat = configurationService.getPropertyValueAsString(
                CUTaxKeyConstants.SPRINTAX_PLACEHOLDER_EMAIL_FORMAT);
        return MessageFormat.format(emailFormat, payeeId);
    }



    private String getSuffixedParameter(final String parameterNameSuffix) {
        return getParameter(CUTaxConstants.TAX_TYPE_1042S + parameterNameSuffix);
    }

    private String getParameter(final String parameterName) {
        return taxParameterService.getParameterValueAsString(CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
    }

    private boolean multiValueParameterContains(final String parameterName, final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return getMultiValueParameter(parameterName).contains(value);
    }

    private Set<String> getMultiValueParameter(final String parameterName) {
        return taxParameterService.getParameterValuesSetAsString(CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
    }

    private String getSubParameter(final String parameterName, final String subParameterName) {
        if (StringUtils.isBlank(subParameterName)) {
            return null;
        }
        final Map<String, String> subParameters = taxParameterService.getSubParameters(
                CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
        return subParameters.get(subParameterName);
    }

    private String createMessage(final String key, final Object... arguments) {
        final String messagePattern = configurationService.getPropertyValueAsString(key);
        return MessageFormat.format(messagePattern, arguments);
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
