package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxAmountType;
import edu.cornell.kfs.tax.batch.TaxOutputConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailExtractor;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.SprintaxInfo1042S;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayment1042S;
import edu.cornell.kfs.tax.batch.dto.VendorQueryResults;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinition;
import edu.cornell.kfs.tax.businessobject.NoteLite;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.VendorAddressLite;
import edu.cornell.kfs.tax.businessobject.VendorDetailLite;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.util.TaxUtils;

public class TaxFileGenerationServiceSprintaxImpl implements TaxFileGenerationService {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;
    private ConfigurationService configurationService;
    private ParameterService parameterService;
    private String sprintaxBioFileDefinitionFilePath;
    private String sprintaxPaymentsFileDefinitionFilePath;
    private String fileOutputDirectory;

    @Override
    public TaxStatistics generateFiles(final TaxOutputConfig config) throws IOException, SQLException {
        return transactionDetailProcessorDao.processTransactionDetails(config, this::generateFilesFromTransactions);
    }

    private TaxStatistics generateFilesFromTransactions(final TaxOutputConfig config,
            final TransactionDetailExtractor rowExtractor) throws Exception {
        final TaxOutputDefinition bioFileDefinition = parseTaxOutputDefinition(
                sprintaxBioFileDefinitionFilePath);
        final TaxOutputDefinition paymentsFileDefinition = parseTaxOutputDefinition(
                sprintaxPaymentsFileDefinitionFilePath);
        final String bioFilePath = createOutputFilePath(CUTaxConstants.Sprintax.BIO_OUTPUT_FILE_PREFIX, config);
        final String paymentsFilePath = createOutputFilePath(
                CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX, config);

        try (
                final TaxFileRowWriterSprintaxBioFileImpl bioFileWriter = 
                        new TaxFileRowWriterSprintaxBioFileImpl(bioFilePath, "", bioFileDefinition);
                final TaxFileRowWriterSprintaxPaymentsFileImpl paymentsFileWriter = 
                        new TaxFileRowWriterSprintaxPaymentsFileImpl(paymentsFilePath, "", paymentsFileDefinition);
        ) {
            final Map<String, String> objectCodeToIncomeClassCodeMappings =
                    prepareObjectCodeToIncomeClassCodeMappings();
            final SprintaxHelper helper = new SprintaxHelper(config, rowExtractor, bioFileWriter, paymentsFileWriter,
                    objectCodeToIncomeClassCodeMappings);
            return generateSprintaxFiles(helper);
        }
    }

    private TaxOutputDefinition parseTaxOutputDefinition(final String filePath) throws IOException {
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(filePath)) {
            final byte[] fileContents = IOUtils.toByteArray(fileStream);
            return taxOutputDefinitionV2FileType.parse(fileContents);
        }
    }

    private String createOutputFilePath(final String fileNamePrefix, final TaxOutputConfig config) {
        final DateFormat dateFormat = new SimpleDateFormat(CUTaxConstants.FILENAME_SUFFIX_DATE_FORMAT, Locale.US);
        return StringUtils.join(fileOutputDirectory, CUKFSConstants.SLASH,
                fileNamePrefix, dateFormat.format(config.getProcessingStartDate()), FileExtensions.CSV);
    }

    private Map<String, String> prepareObjectCodeToIncomeClassCodeMappings() {
        final Collection<String> parameterValuesToParse = getMultiValueParameter(
                Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES);
        return TaxUtils.buildValueToKeyMapFromParameterContainingMultiValueEntries(parameterValuesToParse);
    }



    private TaxStatistics generateSprintaxFiles(final SprintaxHelper helper) throws IOException, SQLException {
        SprintaxInfo1042S currentInfo = null;

        while (helper.rowExtractor.moveToNextRow()) {
            final TransactionDetail currentRow = helper.rowExtractor.getCurrentRow();
            Validate.validState(StringUtils.isNotBlank(currentRow.getVendorTaxNumber()),
                    "A blank tax ID was detected on a 1042-S transaction for payee: %s", currentRow.getPayeeId());
            helper.increment(TaxStatType.NUM_TRANSACTION_ROWS);

            if (currentInfo == null) {
                currentInfo = createNewInfo(currentRow, helper);
            } else if (!StringUtils.equals(currentInfo.getTaxId(), currentRow.getVendorTaxNumber())) {
                finalizeCurrentInfo(currentInfo, helper);
                currentInfo = createNewInfo(currentRow, helper);
            }
            processCurrentRow(currentInfo, currentRow, helper);
        }

        if (currentInfo != null) {
            finalizeCurrentInfo(currentInfo, helper);
        }

        return helper.statistics;
    }



    private SprintaxInfo1042S createNewInfo(final TransactionDetail currentRow, final SprintaxHelper helper)
            throws SQLException {
        final SprintaxInfo1042S nextInfo = new SprintaxInfo1042S();
        initializePayeeIdInformation(nextInfo, currentRow.getPayeeId());
        nextInfo.setTaxId(currentRow.getVendorTaxNumber());
        nextInfo.setCurrentPayment(createNewPayment(currentRow, helper));
        initializeVendorData(nextInfo, helper);
        return nextInfo;
    }

    private SprintaxPayment1042S createNewPayment(final TransactionDetail currentRow, final SprintaxHelper helper) {
        final SprintaxPayment1042S nextPayment = new SprintaxPayment1042S();
        nextPayment.setIncomeCode(currentRow.getIncomeCode());
        nextPayment.setIncomeCodeSubType(currentRow.getIncomeCodeSubType());
        nextPayment.setGrossAmount(KualiDecimal.ZERO);
        nextPayment.setFederalTaxWithheldAmount(KualiDecimal.ZERO);
        nextPayment.setStateIncomeTaxWithheldAmount(KualiDecimal.ZERO);
        return nextPayment;
    }

    private void initializePayeeIdInformation(final SprintaxInfo1042S nextInfo, final String payeeId) {
        final String vendorHeaderId = StringUtils.substringBefore(payeeId, KFSConstants.DASH);
        final String vendorDetailId = StringUtils.substringAfter(payeeId, KFSConstants.DASH);
        nextInfo.setPayeeId(payeeId);
        nextInfo.setVendorHeaderId(Integer.valueOf(vendorHeaderId));
        nextInfo.setVendorDetailId(Integer.valueOf(vendorDetailId));
    }

    private void initializeVendorData(final SprintaxInfo1042S nextInfo, final SprintaxHelper helper)
            throws SQLException {
        final VendorQueryResults vendorResults = transactionDetailProcessorDao.getVendor(
                nextInfo.getVendorHeaderId(), nextInfo.getVendorDetailId());
        final VendorDetailLite matchingVendor = vendorResults.getVendor();
        final VendorDetailLite vendorToProcess;
        
        if (ObjectUtils.isNull(matchingVendor)) {
            helper.increment(TaxStatType.NUM_NO_VENDOR);
            nextInfo.setVendorNameForOutput(createMessage(CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_1042S_VENDOR_NOT_FOUND,
                    nextInfo.getVendorHeaderId(), nextInfo.getVendorDetailId()));
            vendorToProcess = null;
        } else if (matchingVendor.isVendorParentIndicator()) {
            vendorToProcess = matchingVendor;
        } else if (ObjectUtils.isNull(vendorResults.getParentVendor())) {
            initializeSoleProprietorParentVendorNameIfNecessary(nextInfo, matchingVendor, helper);
            helper.increment(TaxStatType.NUM_NO_PARENT_VENDOR);
            nextInfo.setVendorNameForOutput(createMessage(
                    CUTaxKeyConstants.MESSAGE_TAX_OUTPUT_1042S_VENDOR_PARENT_NOT_FOUND,
                    nextInfo.getVendorHeaderId(), nextInfo.getVendorDetailId()));
            vendorToProcess = null;
        } else {
            initializeSoleProprietorParentVendorNameIfNecessary(nextInfo, matchingVendor, helper);
            vendorToProcess = vendorResults.getParentVendor();
        }

        if (ObjectUtils.isNotNull(vendorToProcess)) {
            nextInfo.setVendorTypeCode(vendorToProcess.getVendorTypeCode());
            nextInfo.setVendorOwnershipCode(vendorToProcess.getVendorOwnershipCode());
            initializeVendorName(nextInfo, vendorToProcess, helper);
            initializeChapter4ExemptionCode(nextInfo, vendorToProcess);
            initializeVendorAddressData(nextInfo, vendorToProcess, helper);
        } else {
            initializeWithDefaultChapter4ExemptionCode(nextInfo);
            if (ObjectUtils.isNotNull(matchingVendor)) {
                initializeVendorAddressData(nextInfo, matchingVendor, helper);
            }
        }
    }

    /*
     * TODO: We need to revisit the parent-vendor-name setup and determine whether
     * it should be renamed to sub-vendor-name or something similar.
     */
    private void initializeSoleProprietorParentVendorNameIfNecessary(final SprintaxInfo1042S nextInfo,
            final VendorDetailLite vendorDetail, final SprintaxHelper helper) {
        final String soleProprietorOwnershipCode = getSuffixedParameter(
                TaxCommonParameterNames.SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX);
        if (StringUtils.equals(vendorDetail.getVendorOwnershipCode(), soleProprietorOwnershipCode)) {
            nextInfo.setParentVendorNameForOutput(vendorDetail.getVendorName());
        }
    }

    private void initializeVendorName(final SprintaxInfo1042S nextInfo,
            final VendorDetailLite vendorDetail, final SprintaxHelper helper) {
        final String vendorName = vendorDetail.getVendorName();
        nextInfo.setVendorNameForOutput(vendorName);
        if (StringUtils.isNotBlank(vendorName)) {
            helper.increment(TaxStatType.NUM_VENDOR_NAMES_PARSED);
            if (vendorDetail.isVendorFirstLastNameIndicator()
                    && StringUtils.contains(vendorName, KFSConstants.COMMA)) {
                nextInfo.setVendorLastName(StringUtils.trim(
                        StringUtils.substringBefore(vendorName, KFSConstants.COMMA)));
                nextInfo.setVendorFirstName(StringUtils.trim(
                        StringUtils.substringAfter(vendorName, KFSConstants.COMMA)));
            } else {
                nextInfo.setVendorLastName(StringUtils.trim(vendorName));
            }
        } else {
            helper.increment(TaxStatType.NUM_VENDOR_NAMES_NOT_PARSED);
        }
    }

    private void initializeChapter4ExemptionCode(final SprintaxInfo1042S nextInfo, final VendorDetailLite vendor) {
        final String chapter4ExemptionCode = getSubParameter(
                Tax1042SParameterNames.CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES,
                vendor.getVendorChapter4StatusCode());
        if (StringUtils.isNotBlank(chapter4ExemptionCode)) {
            nextInfo.setChapter4ExemptionCode(chapter4ExemptionCode);
        } else {
            initializeWithDefaultChapter4ExemptionCode(nextInfo);
        }
    }

    private void initializeWithDefaultChapter4ExemptionCode(final SprintaxInfo1042S nextInfo) {
        nextInfo.setChapter4ExemptionCode(getParameter(Tax1042SParameterNames.CHAPTER4_DEFAULT_EXEMPTION_CODE));
    }

    private void initializeVendorAddressData(final SprintaxInfo1042S nextInfo,
            final VendorDetailLite vendor, final SprintaxHelper helper) throws SQLException {
        final VendorAddressLite usAddress = transactionDetailProcessorDao.getHighestPriorityUSVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
        final VendorAddressLite foreignAddress = transactionDetailProcessorDao.getHighestPriorityForeignVendorAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());

        if (ObjectUtils.isNotNull(usAddress)) {
            nextInfo.setVendorUSAddressLine1(usAddress.getVendorLine1Address());
            if (ObjectUtils.isNull(foreignAddress)) {
                nextInfo.setVendorEmailAddress(usAddress.getVendorAddressEmailAddress());
            }
        } else {
            nextInfo.setVendorUSAddressLine1(CUTaxConstants.NO_US_VENDOR_ADDRESS);
            helper.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_US);
        }

        if (ObjectUtils.isNotNull(foreignAddress)) {
            nextInfo.setVendorForeignAddressLine1(foreignAddress.getVendorLine1Address());
            nextInfo.setVendorForeignAddressLine2(foreignAddress.getVendorLine2Address());
            nextInfo.setVendorForeignCityName(foreignAddress.getVendorCityName());
            nextInfo.setVendorForeignProvinceName(foreignAddress.getVendorAddressInternationalProvinceName());
            nextInfo.setVendorForeignZipCode(foreignAddress.getVendorZipCode());
            nextInfo.setVendorForeignCountryCode(foreignAddress.getVendorCountryCode());
            nextInfo.setVendorEmailAddress(foreignAddress.getVendorAddressEmailAddress());
        } else {
            nextInfo.setVendorForeignAddressLine1(CUTaxConstants.NO_FOREIGN_VENDOR_ADDRESS);
            helper.increment(TaxStatType.NUM_NO_VENDOR_ADDRESS_FOREIGN);
        }
    }



    private void processCurrentRow(final SprintaxInfo1042S currentInfo, final TransactionDetail currentRow,
            final SprintaxHelper helper) throws SQLException {
        final TaxRowClusionBuilderSprintaxImpl clusionBuilder = new TaxRowClusionBuilderSprintaxImpl(
                helper, parameterService);
        final String incomeClassCode = helper.objectCodeToIncomeClassCodeMappings.get(
                StringUtils.defaultString(currentRow.getFinObjectCode()));
        final TaxAmountType amountType = determineAmountType(currentRow, incomeClassCode, helper);
        Validate.validState(amountType != null, "Amount Type cannot be null; it should be UNKNOWN if undetermined");

        if (amountType != TaxAmountType.UNKNOWN) {
            clusionBuilder.appendCheckForVendorType(currentInfo.getVendorTypeCode())
                    .appendCheckForVendorOwnershipType(currentInfo.getVendorOwnershipCode())
                    .appendCheckForDocumentType(currentRow.getDocumentType());
        }

        boolean paymentReasonIsBlankOrProcessable = true;
        if (StringUtils.isNotBlank(currentRow.getPaymentReasonCode())) {
            clusionBuilder.appendCheckForPaymentReason(currentRow.getPaymentReasonCode());
            paymentReasonIsBlankOrProcessable = clusionBuilder.getResultOfPreviousCheck();
        }

        if (paymentReasonIsBlankOrProcessable && amountType != TaxAmountType.UNKNOWN) {
            final List<NoteLite> documentNotes = transactionDetailProcessorDao.getNotesByDocumentNumber(
                    currentRow.getDocumentNumber());
            if (CollectionUtils.isNotEmpty(documentNotes)) {
                helper.increment(TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED);
            } else {
                helper.increment(TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED);
            }
        }
    }

    private TaxAmountType determineAmountType(final TransactionDetail currentRow, final String incomeClassCode,
            final SprintaxHelper helper) {
        if (StringUtils.isNotBlank(incomeClassCode)) {
            helper.increment(TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED);
            return TaxAmountType.GROSS_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES);
            return TaxAmountType.FEDERAL_TAX_WITHHELD_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES);
            return TaxAmountType.STATE_INCOME_TAX_WITHHELD_AMOUNT;
        } else {
            return TaxAmountType.UNKNOWN;
        }
    }



    private void finalizeCurrentInfo(final SprintaxInfo1042S currentInfo, final SprintaxHelper helper) {
        
    }



    private String getSuffixedParameter(final String parameterNameSuffix) {
        return parameterService.getParameterValueAsString(CUTaxConstants.TAX_NAMESPACE,
                CUTaxConstants.TAX_1042S_PARM_DETAIL, CUTaxConstants.TAX_TYPE_1042S + parameterNameSuffix);
    }

    private String getParameter(final String parameterName) {
        return parameterService.getParameterValueAsString(CUTaxConstants.TAX_NAMESPACE,
                CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
    }

    private boolean suffixedMultiValueParameterContains(final String parameterNameSuffix, final String value) {
        return multiValueParameterContains(CUTaxConstants.TAX_TYPE_1042S + parameterNameSuffix, value);
    }

    private boolean multiValueParameterContains(final String parameterName, final String value) {
        return getMultiValueParameter(parameterName).contains(value);
    }

    private Collection<String> getMultiValueParameter(final String parameterName) {
        return parameterService.getParameterValuesAsString(CUTaxConstants.TAX_NAMESPACE,
                CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
    }

    private String getSubParameter(final String parameterName, final String subParameterName) {
        return parameterService.getSubParameterValueAsString(CUTaxConstants.TAX_NAMESPACE,
                CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName, subParameterName);
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

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
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



    private static final class SprintaxHelper implements TaxStatisticsHandler {
        private final TaxOutputConfig config;
        private final TransactionDetailExtractor rowExtractor;
        private final TaxFileRowWriterSprintaxBioFileImpl bioFileWriter;
        private final TaxFileRowWriterSprintaxPaymentsFileImpl paymentsFileWriter;
        private final TaxStatistics statistics;
        private final Map<String, String> objectCodeToIncomeClassCodeMappings;

        private SprintaxHelper(final TaxOutputConfig config, final TransactionDetailExtractor rowExtractor,
                final TaxFileRowWriterSprintaxBioFileImpl bioFileWriter,
                final TaxFileRowWriterSprintaxPaymentsFileImpl paymentsFileWriter,
                final Map<String, String> objectCodeToIncomeClassCodeMappings) {
            this.config = config;
            this.rowExtractor = rowExtractor;
            this.bioFileWriter = bioFileWriter;
            this.paymentsFileWriter = paymentsFileWriter;
            this.statistics = new TaxStatistics();
            this.objectCodeToIncomeClassCodeMappings = objectCodeToIncomeClassCodeMappings;
        }

        @Override
        public void increment(final TaxStatType entryType) {
            statistics.increment(entryType);
        }
    }

}
