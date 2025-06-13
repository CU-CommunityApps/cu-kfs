package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.CUTaxKeyConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxBoxType1042S;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxFileSections;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxRowClusionResult;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayee;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayee.SprintaxField;
import edu.cornell.kfs.tax.batch.dto.SprintaxPayment;
import edu.cornell.kfs.tax.batch.dto.TaxBoxUpdates;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.service.TaxPayeeHelperService;
import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;
import edu.cornell.kfs.tax.service.TransactionOverrideService;
import edu.cornell.kfs.tax.util.TaxUtils;

public class TaxFileGenerationServiceSprintaxImpl implements TaxFileGenerationService, TransactionDetailHandler {

    private static final Logger LOG = LogManager.getLogger();

    private static final int MAX_BATCH_UPDATE_SIZE = 30;

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;
    private TaxPayeeHelperService taxPayeeHelperService;
    private TransactionOverrideService transactionOverrideService;
    private ConfigurationService configurationService;
    private TaxParameterService taxParameterService;
    private String sprintaxDemographicFileDefinitionFilePath;
    private String sprintaxPaymentsFileDefinitionFilePath;
    private String fileOutputDirectory;
    private String payerEIN;
    private boolean scrubOutput;

    @Override
    public TaxStatistics generateFiles(final TaxBatchConfig config) throws IOException, SQLException {
        Validate.notNull(config, "config cannot be null");
        Validate.isTrue(config.getMode() == TaxBatchConfig.Mode.CREATE_TAX_FILES,
                "config should have specified CREATE_TAX_FILES mode");
        Validate.isTrue(StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S),
                "config should have specified the 1042S tax type");

        return transactionDetailProcessorDao.processTransactionDetails(config, this);
    }

    @Override
    public TaxStatistics performProcessing(final TaxBatchConfig config,
            final TaxDtoRowMapper<TransactionDetail> rowMapper) throws Exception {
        final Map<String, String> transactionOverrides = TaxUtils.buildTransactionOverridesMap(
                transactionOverrideService, config);
        final TaxOutputDefinitionV2 demographicFileDefinition = TaxUtils.parseTaxOutputDefinition(
                taxOutputDefinitionV2FileType, sprintaxDemographicFileDefinitionFilePath);
        final TaxOutputDefinitionV2 paymentsFileDefinition = TaxUtils.parseTaxOutputDefinition(
                taxOutputDefinitionV2FileType, sprintaxPaymentsFileDefinitionFilePath);
        final String demographicFilePath = TaxUtils.buildCsvTaxFilePath(
                CUTaxConstants.Sprintax.DEMOGRAPHIC_OUTPUT_FILE_PREFIX, fileOutputDirectory, config);
        final String paymentsFilePath = TaxUtils.buildCsvTaxFilePath(
                CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX, fileOutputDirectory, config);

        try (
                final TaxFileRowWriterImpl demographicFileWriter = 
                        new TaxFileRowWriterImpl(demographicFileDefinition, SprintaxField.class, demographicFilePath,
                                scrubOutput);
                final TaxFileRowWriterImpl paymentsFileWriter = 
                        new TaxFileRowWriterImpl(paymentsFileDefinition, SprintaxField.class, paymentsFilePath,
                                scrubOutput);
        ) {
            final SprintaxHelper helper = new SprintaxHelper(config, rowMapper, demographicFileWriter,
                    paymentsFileWriter, transactionOverrides);
            return readAndProcessTransactions(helper);
        }
    }

    private TaxStatistics readAndProcessTransactions(final SprintaxHelper helper) throws IOException, SQLException {
        SprintaxPayee currentPayee = null;

        helper.demographicFileWriter.writeHeaderRow(TaxFileSections.SPRINTAX_DEMOGRAPHIC_ROW_1042S);
        helper.paymentsFileWriter.writeHeaderRow(TaxFileSections.SPRINTAX_PAYMENT_ROW_1042S);

        while (helper.rowMapper.moveToNextRow()) {
            final TransactionDetail currentRow = helper.rowMapper.readCurrentRow();
            Validate.validState(StringUtils.isNotBlank(currentRow.getVendorTaxNumber()),
                    "A blank tax ID was detected on a 1042-S transaction for payee: %s", currentRow.getPayeeId());
            helper.increment(TaxStatType.NUM_TRANSACTION_ROWS);

            if (currentPayee == null) {
                currentPayee = createNewPayee(currentRow, helper);
            } else {
                if (nextTransactionNeedsNewPayment(currentPayee, currentRow)) {
                    if (shouldPrintTaxFileRow(currentPayee.getCurrentPayment())) {
                        printTaxFileRow(currentPayee, helper);
                    }
                    if (!nextTransactionIsForDifferentVendor(currentPayee, currentRow)) {
                        currentPayee.setCurrentPayment(createNewPayment(currentRow, helper));
                    }
                }
                if (nextTransactionIsForDifferentVendor(currentPayee, currentRow)) {
                    currentPayee = createNewPayee(currentRow, helper);
                }
            }

            processCurrentRow(currentPayee, currentRow, helper);
        }

        if (helper.pendingBatchUpdates.size() > 0) {
            transactionDetailProcessorDao.updateVendorInfoAndTaxBoxesOnTransactionDetails(
                    helper.pendingBatchUpdates, helper.config);
            helper.pendingBatchUpdates.clear();
        }

        if (currentPayee != null && shouldPrintTaxFileRow(currentPayee.getCurrentPayment())) {
            printTaxFileRow(currentPayee, helper);
        }

        return helper.statistics;
    }



    private SprintaxPayee createNewPayee(final TransactionDetail currentRow, final SprintaxHelper helper)
            throws SQLException {
        final SprintaxPayee payee = taxPayeeHelperService.createTaxPayeeWithPopulatedVendorData(
                SprintaxPayee::new, currentRow, helper);
        payee.setPayerEIN(payerEIN);
        payee.setCurrentPayment(createNewPayment(currentRow, helper));
        initializeChapter4ExemptionCode(payee);
        if (StringUtils.isBlank(payee.getVendorEmailAddress())) {
            payee.setPlaceholderEmailAddress(generatePlaceholderEmailAddress(payee.getPayeeId()));
        }
        return payee;
    }

    private void initializeChapter4ExemptionCode(final SprintaxPayee payee) {
        String chapter4ExemptionCode = StringUtils.isNotBlank(payee.getVendorChapter4StatusCode())
                ? getSubParameter(Tax1042SParameterNames.CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES,
                        payee.getVendorChapter4StatusCode())
                : null;
        if (StringUtils.isBlank(chapter4ExemptionCode)) {
            chapter4ExemptionCode = getParameter(Tax1042SParameterNames.CHAPTER4_DEFAULT_EXEMPTION_CODE);
        }
        payee.setChapter4ExemptionCode(chapter4ExemptionCode);
    }

    private String generatePlaceholderEmailAddress(final String payeeId) {
        return createMessage(CUTaxKeyConstants.SPRINTAX_PLACEHOLDER_EMAIL_FORMAT, payeeId);
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



    private boolean nextTransactionNeedsNewPayment(final SprintaxPayee currentPayee, final TransactionDetail nextRow) {
        final SprintaxPayment currentPayment = currentPayee.getCurrentPayment();
        return nextTransactionIsForDifferentVendor(currentPayee, nextRow)
                || nextTransactionHasDifferentIncomeCode(currentPayment.getIncomeCode(), nextRow.getIncomeCode())
                || nextTransactionHasDifferentIncomeCodeSubType(
                        currentPayment.getIncomeCodeSubType(), nextRow.getIncomeCodeSubType());
    }

    private boolean nextTransactionIsForDifferentVendor(final SprintaxPayee currentPayee,
            final TransactionDetail nextRow) {
        return !StringUtils.equals(currentPayee.getVendorTaxNumber(), nextRow.getVendorTaxNumber());
    }

    private boolean nextTransactionHasDifferentIncomeCode(final String currentIncomeCode,
            final String nextIncomeCode) {
        return StringUtils.isNotBlank(currentIncomeCode) && !StringUtils.equals(currentIncomeCode, nextIncomeCode);
    }

    private boolean nextTransactionHasDifferentIncomeCodeSubType(final String currentIncomeCodeSubType,
            final String nextIncomeCodeSubType) {
        return StringUtils.isNotBlank(currentIncomeCodeSubType)
                && !StringUtils.equals(currentIncomeCodeSubType, nextIncomeCodeSubType);
    }

    private boolean shouldPrintTaxFileRow(final SprintaxPayment currentPayment) {
        return currentPayment.isFoundAtLeastOneProcessableTransaction() && (
                currentPayment.isExplicitlyMarkedAsTaxTreatyExemptIncome()
                        || currentPayment.isExplicitlyMarkedAsForeignSourceIncome()
                        || (currentPayment.getFedIncomeTaxPercent() != null && !KualiDecimal.ZERO.equals(
                                currentPayment.getFedIncomeTaxPercent()))
                        || !KualiDecimal.ZERO.equals(currentPayment.getFederalTaxWithheldAmount())
        );
    }



    private void processCurrentRow(final SprintaxPayee currentPayee, final TransactionDetail currentRow,
            final SprintaxHelper helper) throws SQLException {
        final SprintaxPayment currentPayment = currentPayee.getCurrentPayment();
        currentPayment.setTaxTreatyExemptIncome(currentRow.getIncomeTaxTreatyExemptIndicator());
        currentPayment.setForeignSourceIncome(currentRow.getForeignSourceIncomeIndicator());
        currentPayment.setFedIncomeTaxPercent(currentRow.getFederalIncomeTaxPercent());

        final TaxBoxType1042S taxBoxType = determineTaxBoxType(currentRow, helper);
        Validate.validState(taxBoxType != null, "Tax Box Type cannot be null; it should be UNKNOWN if undetermined");

        final TaxRowClusionResult rowClusionResult = checkForExclusions(currentPayee, currentRow, helper, taxBoxType);
        final TaxBoxType1042S taxBoxOverride = findTaxBoxOverride(currentRow, helper);
        final TaxBoxType1042S taxBoxToUse = determineTaxBoxToUse(taxBoxType, taxBoxOverride, rowClusionResult);
        final TaxBoxUpdates taxBoxUpdates = new TaxBoxUpdates();
        taxBoxUpdates.setForm1042SBoxToUse(taxBoxToUse.toString());
        taxBoxUpdates.setForm1042SOverriddenBox((taxBoxOverride != null) ? taxBoxType.toString() : null);
        currentPayee.setCurrentTaxBoxUpdates(taxBoxUpdates);

        if (taxBoxToUse == TaxBoxType1042S.GROSS_AMOUNT) {
            currentPayment.addToGrossAmount(currentRow.getNetPaymentAmount());
            currentPayment.setFoundAtLeastOneProcessableTransaction(true);
        } else if (taxBoxToUse == TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT) {
            currentPayment.addToFederalTaxWithheldAmount(currentRow.getNetPaymentAmount());
            currentPayment.setFoundAtLeastOneProcessableTransaction(true);
        } else if (taxBoxToUse == TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT) {
            currentPayment.addToStateIncomeTaxWithheldAmount(currentRow.getNetPaymentAmount());
            currentPayment.setFoundAtLeastOneProcessableTransaction(true);
        } else {
            helper.increment(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS);
        }

        final TransactionDetail updatedRow = createTransactionDetailCopyContainingFieldUpdates(
                currentRow, currentPayee, helper);
        helper.pendingBatchUpdates.add(updatedRow);
        if (helper.pendingBatchUpdates.size() >= MAX_BATCH_UPDATE_SIZE) {
            transactionDetailProcessorDao.updateVendorInfoAndTaxBoxesOnTransactionDetails(
                    helper.pendingBatchUpdates, helper.config);
            helper.pendingBatchUpdates.clear();
        }
    }

    private TaxBoxType1042S determineTaxBoxType(final TransactionDetail currentRow, final SprintaxHelper helper) {
        final Set<String> incomeClassCodes = findIncomeClassCodesForObject(currentRow.getFinObjectCode());
        if (CollectionUtils.isNotEmpty(incomeClassCodes)) {
            helper.increment(TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED, currentRow.getDocumentType());
            return TaxBoxType1042S.GROSS_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_FTW_AMOUNTS_DETERMINED, currentRow.getDocumentType());
            return TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_SITW_AMOUNTS_DETERMINED, currentRow.getDocumentType());
            return TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT;
        } else {
            return TaxBoxType1042S.UNKNOWN;
        }
    }

    private Set<String> findIncomeClassCodesForObject(final String objectCode) {
        if (StringUtils.isBlank(objectCode)) {
            return Set.of();
        }
        final Map<String, Set<String>> objectCodeToIncomeClassCodeMappings = taxParameterService
                .getValueToKeysMapFromParameterContainingMultiValueEntries(CUTaxConstants.TAX_1042S_PARM_DETAIL,
                        Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES);
        return objectCodeToIncomeClassCodeMappings.getOrDefault(objectCode, Set.of());
    }

    private TaxRowClusionResult checkForExclusions(
            final SprintaxPayee currentPayee, final TransactionDetail currentRow,
            final SprintaxHelper helper, final TaxBoxType1042S taxBoxType) throws SQLException {
        final String objectCode = currentRow.getFinObjectCode();
        final TaxRowClusionBuilderSprintaxImpl clusionBuilder = new TaxRowClusionBuilderSprintaxImpl(
                helper, taxParameterService, currentRow.getDocumentType());

        if (taxBoxType != TaxBoxType1042S.UNKNOWN) {
            clusionBuilder.appendCheckForVendorType(currentPayee.getVendorTypeCode())
                    .appendCheckForVendorOwnershipType(currentPayee.getVendorOwnershipCode())
                    .appendCheckForDocumentType(currentRow.getDocumentType());
        }

        boolean paymentReasonIsBlankOrProcessable = true;
        if (StringUtils.isNotBlank(currentRow.getPaymentReasonCode())) {
            clusionBuilder.appendCheckForPaymentReason(currentRow.getPaymentReasonCode());
            paymentReasonIsBlankOrProcessable =
                    (clusionBuilder.getResultOfPreviousCheck() == TaxRowClusionResult.INCLUDE);
        }

        if (paymentReasonIsBlankOrProcessable && taxBoxType != TaxBoxType1042S.UNKNOWN) {
            final String chartAndAccountPair = StringUtils.join(
                    currentRow.getChartCode(), KFSConstants.DASH, currentRow.getAccountNumber());
            final boolean isRoyaltyAmount = isRoyaltyAmount(currentRow, taxBoxType);
            final List<NoteLite> documentNotes = transactionDetailProcessorDao.getNotesByDocumentNumber(
                    currentRow.getDocumentNumber());

            if (StringUtils.isNotBlank(currentRow.getPaymentLine1Address())) {
                clusionBuilder.appendCheckForPaymentLine1Address(currentRow.getPaymentLine1Address());
            }
            clusionBuilder.appendCheckForDocumentNotes(documentNotes);

            if (isRoyaltyAmount) {
                clusionBuilder.appendCheckForAccountOnRoyalties(objectCode, chartAndAccountPair);
                if (isDVDocumentTransaction(currentRow)) {
                    clusionBuilder.appendCheckForDvCheckStubTextOnRoyalties(
                            objectCode, currentRow.getDvCheckStubText());
                }
            } else if (taxBoxType == TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT) {
                clusionBuilder.appendCheckForAccountOnFederalTaxWithholding(objectCode, chartAndAccountPair);
            } else if (taxBoxType == TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT) {
                clusionBuilder.appendCheckForAccountOnStateTaxWithholding(objectCode, chartAndAccountPair);
            }
        }

        if (clusionBuilder.getCumulativeResult() != TaxRowClusionResult.EXCLUDE) {
            clusionBuilder.appendCheckForIncomeCode(currentRow.getIncomeCode(), taxBoxType)
                    .appendCheckForIncomeCodeSubType(currentRow.getIncomeCodeSubType(), taxBoxType);
        }

        return clusionBuilder.getCumulativeResult();
    }

    private boolean isRoyaltyAmount(final TransactionDetail currentRow, final TaxBoxType1042S taxBoxType) {
        return taxBoxType == TaxBoxType1042S.GROSS_AMOUNT && multiValueParameterContains(
                Tax1042SParameterNames.INCOME_CLASS_CODE_DENOTING_ROYALTIES, currentRow.getIncomeClassCode());
    }

    private boolean isDVDocumentTransaction(final TransactionDetail currentRow) {
        return StringUtils.equals(currentRow.getDocumentType(), DisbursementVoucherConstants.DOCUMENT_TYPE_CODE);
    }

    private TaxBoxType1042S findTaxBoxOverride(final TransactionDetail currentRow, final SprintaxHelper helper) {
        final String transactionOverrideKey = TaxUtils.buildTransactionOverrideKey(currentRow);
        final String transactionOverride = helper.transactionOverrides.get(transactionOverrideKey);
        if (StringUtils.isBlank(transactionOverride)) {
            return null;
        }
        switch (transactionOverride) {
            case CUTaxConstants.FORM_1042S_GROSS_BOX:
                return TaxBoxType1042S.GROSS_AMOUNT;

            case CUTaxConstants.FORM_1042S_FED_TAX_WITHHELD_BOX:
                return TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT;

            case CUTaxConstants.FORM_1042S_STATE_INC_TAX_WITHHELD_BOX:
                return TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT;

            case CUTaxConstants.TAX_1042S_UNKNOWN_BOX_KEY:
            default:
                return TaxBoxType1042S.UNKNOWN;
        }
    }

    private TaxBoxType1042S determineTaxBoxToUse(final TaxBoxType1042S taxBox, final TaxBoxType1042S taxBoxOverride,
            final TaxRowClusionResult rowClusionResult) {
        if (taxBoxOverride != null) {
            return taxBoxOverride;
        } else if (rowClusionResult == TaxRowClusionResult.EXCLUDE) {
            return TaxBoxType1042S.UNKNOWN;
        } else {
            return taxBox;
        }
    }

    private TransactionDetail createTransactionDetailCopyContainingFieldUpdates(final TransactionDetail currentRow,
            final SprintaxPayee currentPayee, final SprintaxHelper helper) {
        final TransactionDetail updatedRow = (TransactionDetail) ObjectUtils.deepCopy(currentRow);
        updatedRow.setVendorName(currentPayee.getVendorName());
        updatedRow.setParentVendorName(currentPayee.getParentVendorName());
        updatedRow.setVendorEmailAddress(currentPayee.getNonPlaceholderVendorEmailAddress());
        updatedRow.setVendorChapter4StatusCode(currentPayee.getVendorChapter4StatusCode());
        updatedRow.setVendorGIIN(currentPayee.getVendorGIIN());
        updatedRow.setVendorLine1Address(currentPayee.getVendorLine1Address());
        updatedRow.setVendorLine2Address(currentPayee.getVendorLine2Address());
        updatedRow.setVendorCityName(currentPayee.getVendorCityName());
        updatedRow.setVendorStateCode(currentPayee.getVendorStateCode());
        updatedRow.setVendorZipCode(currentPayee.getVendorZipCode());
        updatedRow.setVendorForeignLine1Address(currentPayee.getVendorForeignLine1Address());
        updatedRow.setVendorForeignLine2Address(currentPayee.getVendorForeignLine2Address());
        updatedRow.setVendorForeignCityName(currentPayee.getVendorForeignCityName());
        updatedRow.setVendorForeignZipCode(currentPayee.getVendorForeignZipCode());
        updatedRow.setVendorForeignProvinceName(currentPayee.getVendorForeignProvinceName());
        updatedRow.setVendorForeignCountryCode(currentPayee.getVendorForeignCountryCode());
        updatedRow.setForm1042SBox(currentPayee.getForm1042SBox());
        updatedRow.setForm1042SOverriddenBox(currentPayee.getForm1042SOverriddenBox());
        return updatedRow;
    }



    private void printTaxFileRow(final SprintaxPayee currentPayee, final SprintaxHelper helper) throws IOException {
        if (!currentPayee.isDemographicRowWritten()) {
            prepareForPrintingDemographicRow(currentPayee, helper);
            helper.demographicFileWriter.writeDataRow(TaxFileSections.SPRINTAX_DEMOGRAPHIC_ROW_1042S, currentPayee);
            helper.increment(TaxStatType.NUM_BIO_RECORDS_WRITTEN);
            currentPayee.setDemographicRowWritten(true);
        }
        prepareForPrintingPaymentRow(currentPayee, helper);
        helper.paymentsFileWriter.writeDataRow(TaxFileSections.SPRINTAX_PAYMENT_ROW_1042S, currentPayee);
        helper.increment(TaxStatType.NUM_DETAIL_RECORDS_WRITTEN);
    }

    private void prepareForPrintingDemographicRow(final SprintaxPayee currentPayee, final SprintaxHelper helper) {
        final String formattedTaxId = formatTaxIdForFile(currentPayee.getVendorTaxNumber());
        if (shouldTreatTaxIdAsITIN(currentPayee.getVendorTaxNumber())) {
            LOG.debug("prepareForPrintingBiographicRow, Biographic row for payee {} has an ITIN tax ID",
                    currentPayee.getPayeeId());
            currentPayee.setFormattedITINValue(formattedTaxId);
        } else {
            LOG.debug("prepareForPrintingBiographicRow, Biographic row for payee {} has an SSN tax ID",
                    currentPayee.getPayeeId());
            currentPayee.setFormattedSSNValue(formattedTaxId);
        }

        final boolean isMissingUSAddress = StringUtils.containsIgnoreCase(
                currentPayee.getVendorLine1Address(), CUTaxConstants.VENDOR_NOT_FOUND_MESSAGE);
        final boolean isMissingForeignAddress = StringUtils.containsIgnoreCase(
                currentPayee.getVendorForeignLine1Address(), CUTaxConstants.VENDOR_NOT_FOUND_MESSAGE);
        if (isMissingUSAddress) {
            helper.increment(TaxStatType.NUM_BIO_LINES_WITHOUT_US_ADDRESS);
            if (isMissingForeignAddress) {
                helper.increment(TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS);
                helper.increment(TaxStatType.NUM_BIO_LINES_WITHOUT_ANY_ADDRESS);
            }
        } else if (isMissingForeignAddress) {
            helper.increment(TaxStatType.NUM_BIO_LINES_WITHOUT_FOREIGN_ADDRESS);
        }

        final String chapter3StatusCode = getChapter3StatusCodeByVendorOwnershipCode(
                currentPayee.getVendorOwnershipCode());
        currentPayee.setChapter3StatusCode(chapter3StatusCode);
    }

    private boolean shouldTreatTaxIdAsITIN(final String taxId) {
        Validate.validState(StringUtils.length(taxId) == 9,
                "Detected a tax ID that was only %s characters long", StringUtils.length(taxId));
        return taxId.charAt(0) == '0' && (taxId.charAt(3) == '7' || taxId.charAt(3) == '8');
    }

    private String formatTaxIdForFile(final String taxId) {
        Validate.validState(StringUtils.length(taxId) == 9,
                "Detected a tax ID that was only %s characters long", StringUtils.length(taxId));
        if (scrubOutput) {
            return CUTaxConstants.MASKED_VALUE_11_CHARS;
        }
        return StringUtils.joinWith(KFSConstants.DASH,
                taxId.substring(0, 3), taxId.substring(3, 5), taxId.substring(5));
    }

    private String getChapter3StatusCodeByVendorOwnershipCode(final String vendorOwnershipCode) {
        return getSubParameter(Tax1042SParameterNames.VENDOR_OWNERSHIP_TO_CHAPTER3_STATUS_CODE, vendorOwnershipCode);
    }

    private void prepareForPrintingPaymentRow(final SprintaxPayee currentPayee, final SprintaxHelper helper) {
        final SprintaxPayment currentPayment = currentPayee.getCurrentPayment();

        if (KualiDecimal.ZERO.equals(currentPayment.getGrossAmount())) {
            currentPayment.setIncomeCodeForOutput(getNonReportableIncomeCode());
        } else {
            currentPayment.setIncomeCodeForOutput(currentPayment.getIncomeCode());
        }

        if (currentPayment.getFederalTaxWithheldAmount().compareTo(KualiDecimal.ZERO) > 0) {
            LOG.warn("prepareForPrintingPaymentRow, Found a payment for payee {} with a net federal tax withheld "
                    + "amount that is positive!  Adjustments might be needed to make it non-positive.",
                    currentPayee.getPayeeId());
            helper.increment(TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_FTW_AMOUNT);
        }

        if (currentPayment.getStateIncomeTaxWithheldAmount().compareTo(KualiDecimal.ZERO) > 0) {
            LOG.warn("prepareForPrintingPaymentRow, Found a payment for payee {} with a net state income tax withheld "
                    + "amount that is positive!  Adjustments might be needed to make it non-positive.",
                    currentPayee.getPayeeId());
            helper.increment(TaxStatType.NUM_DETAIL_LINES_WITH_POSITIVE_SITW_AMOUNT);
        }

        if (currentPayment.isExplicitlyMarkedAsTaxTreatyExemptIncome()) {
            currentPayment.setChapter3ExemptionCode(getChapter3TaxTreatyExemptionCode());
            currentPayment.setChapter3TaxRate(KualiDecimal.ZERO);
        } else if (currentPayment.isExplicitlyMarkedAsNotTaxTreatyExemptIncome()
                && currentPayment.isExplicitlyMarkedAsForeignSourceIncome()) {
            currentPayment.setChapter3ExemptionCode(getChapter3ForeignSourceExemptionCode());
            currentPayment.setChapter3TaxRate(KualiDecimal.ZERO);
        } else {
            final KualiDecimal fedIncomeTaxPercent = currentPayment.getFedIncomeTaxPercent();
            currentPayment.setChapter3ExemptionCode(getChapter3ExemptionCodeRepresentingNoExemption());
            currentPayment.setChapter3TaxRate(fedIncomeTaxPercent != null ? fedIncomeTaxPercent : KualiDecimal.ZERO);
        }
    }

    private String getNonReportableIncomeCode() {
        return getParameter(Tax1042SParameterNames.NON_REPORTABLE_INCOME_CODE);
    }

    private String getChapter3ExemptionCodeRepresentingNoExemption() {
        return getChapter3ExemptionCode(CUTaxConstants.CH3_EXEMPTION_NOT_EXEMPT_KEY);
    }

    private String getChapter3TaxTreatyExemptionCode() {
        return getChapter3ExemptionCode(CUTaxConstants.CH3_EXEMPTION_TAX_TREATY_KEY);
    }

    private String getChapter3ForeignSourceExemptionCode() {
        return getChapter3ExemptionCode(CUTaxConstants.CH3_EXEMPTION_FOREIGN_SOURCE_KEY);
    }

    private String getChapter3ExemptionCode(final String key) {
        return getSubParameter(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, key);
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

    public void setTaxPayeeHelperService(final TaxPayeeHelperService taxPayeeHelperService) {
        this.taxPayeeHelperService = taxPayeeHelperService;
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

    public void setSprintaxDemographicFileDefinitionFilePath(final String sprintaxBioFileDefinitionFilePath) {
        this.sprintaxDemographicFileDefinitionFilePath = sprintaxBioFileDefinitionFilePath;
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
        private final TaxFileRowWriterImpl demographicFileWriter;
        private final TaxFileRowWriterImpl paymentsFileWriter;
        private final Map<String, String> transactionOverrides;
        private final TaxStatistics statistics;
        private final List<TransactionDetail> pendingBatchUpdates;

        private SprintaxHelper(final TaxBatchConfig config, final TaxDtoRowMapper<TransactionDetail> rowMapper,
                final TaxFileRowWriterImpl demographicFileWriter, final TaxFileRowWriterImpl paymentsFileWriter,
                final Map<String, String> transactionOverrides) {
            this.config = config;
            this.rowMapper = rowMapper;
            this.demographicFileWriter = demographicFileWriter;
            this.paymentsFileWriter = paymentsFileWriter;
            this.transactionOverrides = transactionOverrides;
            this.statistics = TaxUtils.generateBaseStatisticsFor1042S();
            this.pendingBatchUpdates = new ArrayList<>(MAX_BATCH_UPDATE_SIZE);
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
