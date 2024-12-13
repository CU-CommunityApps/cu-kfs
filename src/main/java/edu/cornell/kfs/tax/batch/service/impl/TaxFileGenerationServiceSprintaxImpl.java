package edu.cornell.kfs.tax.batch.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.CUTaxKeyConstants;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxBoxType1042S;
import edu.cornell.kfs.tax.batch.TaxColumns.TransactionDetailColumn;
import edu.cornell.kfs.tax.batch.TaxOutputConfig;
import edu.cornell.kfs.tax.batch.TaxOutputDefinitionV2FileType;
import edu.cornell.kfs.tax.batch.TaxRowClusionBuilderBase.ClusionResult;
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
import edu.cornell.kfs.tax.businessobject.TransactionOverride;
import edu.cornell.kfs.tax.businessobject.VendorAddressLite;
import edu.cornell.kfs.tax.businessobject.VendorDetailLite;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;
import edu.cornell.kfs.tax.service.TransactionOverrideService;
import edu.cornell.kfs.tax.util.TaxUtils;

public class TaxFileGenerationServiceSprintaxImpl implements TaxFileGenerationService {

    private TransactionDetailProcessorDao transactionDetailProcessorDao;
    private TaxOutputDefinitionV2FileType taxOutputDefinitionV2FileType;
    private TransactionOverrideService transactionOverrideService;
    private ConfigurationService configurationService;
    private TaxParameterService taxParameterService;
    private String sprintaxBioFileDefinitionFilePath;
    private String sprintaxPaymentsFileDefinitionFilePath;
    private String fileOutputDirectory;

    @Override
    public TaxStatistics generateFiles(final TaxOutputConfig config) throws IOException, SQLException {
        return transactionDetailProcessorDao.processTransactionDetails(config, this::generateFilesFromTransactions);
    }

    private TaxStatistics generateFilesFromTransactions(final TaxOutputConfig config,
            final TransactionDetailExtractor rowExtractor) throws Exception {
        final Map<String, String> transactionOverrides = getTransactionOverrides(config);
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
            final SprintaxHelper helper = new SprintaxHelper(config, rowExtractor, bioFileWriter, paymentsFileWriter,
                    transactionOverrides);
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

    private Map<String, String> getTransactionOverrides(final TaxOutputConfig config) {
        final List<TransactionOverride> transactionOverrides = transactionOverrideService.getTransactionOverrides(
                CUTaxConstants.TAX_TYPE_1042S, config.getStartDate(), config.getEndDate());
        return TaxUtils.buildTransactionOverridesMap(transactionOverrides);
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
            nextInfo.setVendorGIIN(vendorToProcess.getVendorGIIN());
            nextInfo.setChapter4StatusCode(vendorToProcess.getVendorChapter4StatusCode());
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
            nextInfo.setVendorUSAddressLine2(usAddress.getVendorLine2Address());
            nextInfo.setVendorUSCityName(usAddress.getVendorCityName());
            nextInfo.setVendorUSStateCode(usAddress.getVendorStateCode());
            nextInfo.setVendorUSZipCode(usAddress.getVendorZipCode());
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
        final SprintaxPayment1042S currentPayment = currentInfo.getCurrentPayment();
        currentPayment.setFedIncomeTaxPercent(currentRow.getFederalIncomeTaxPercent());

        final String incomeClassCode = findIncomeClassCodeForObject(currentRow.getFinObjectCode());
        final TaxBoxType1042S taxBoxType = determineTaxBoxType(currentRow, incomeClassCode, helper);
        Validate.validState(taxBoxType != null, "Tax Box Type cannot be null; it should be UNKNOWN if undetermined");

        ClusionResult rowClusionResult = checkForExclusions(currentInfo, currentRow, helper, taxBoxType);
        final TaxBoxType1042S taxBoxOverride = findTaxBoxOverride(currentRow, helper);
        final TaxBoxType1042S taxBoxToUse = determineTaxBoxToUse(taxBoxType, taxBoxOverride, rowClusionResult);
        final TaxBoxType1042S overriddenTaxBox = (taxBoxOverride != null) ? taxBoxType : null;

        if (taxBoxToUse == TaxBoxType1042S.GROSS_AMOUNT) {
            currentPayment.addToGrossAmount(currentRow.getNetPaymentAmount());
        } else if (taxBoxToUse == TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT) {
            currentPayment.addToFederalTaxWithheldAmount(currentRow.getNetPaymentAmount());
        } else if (taxBoxToUse == TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT) {
            currentPayment.addToStateIncomeTaxWithheldAmount(currentRow.getNetPaymentAmount());
        } else {
            helper.increment(TaxStatType.NUM_NO_BOX_DETERMINED_ROWS);
        }

        final Map<TransactionDetailColumn, String> updatedFieldValues = generateUpdatedTransactionDetailFieldValues(
                currentInfo, taxBoxToUse, overriddenTaxBox);
        helper.rowExtractor.updateCurrentRow(updatedFieldValues);
    }

    private String findIncomeClassCodeForObject(final String objectCode) {
        if (StringUtils.isBlank(objectCode)) {
            return null;
        }
        final Map<String, String> objectCodeToIncomeClassCodeMappings = taxParameterService
                .getValueToKeyMapFromParameterContainingMultiValueEntries(CUTaxConstants.TAX_1042S_PARM_DETAIL,
                        Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES);
        return objectCodeToIncomeClassCodeMappings.get(objectCode);
    }

    private TaxBoxType1042S determineTaxBoxType(final TransactionDetail currentRow, final String incomeClassCode,
            final SprintaxHelper helper) {
        if (StringUtils.isNotBlank(incomeClassCode)) {
            helper.increment(TaxStatType.NUM_GROSS_AMOUNTS_DETERMINED);
            return TaxBoxType1042S.GROSS_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES);
            return TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT;
        } else if (multiValueParameterContains(Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES,
                currentRow.getFinObjectCode())) {
            helper.increment(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES);
            return TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT;
        } else {
            return TaxBoxType1042S.UNKNOWN;
        }
    }

    private ClusionResult checkForExclusions(final SprintaxInfo1042S currentInfo, final TransactionDetail currentRow,
            final SprintaxHelper helper, final TaxBoxType1042S taxBoxType) throws SQLException {
        final String objectCode = currentRow.getFinObjectCode();
        final TaxRowClusionBuilderSprintaxImpl clusionBuilder = new TaxRowClusionBuilderSprintaxImpl(
                helper, taxParameterService);

        if (taxBoxType != TaxBoxType1042S.UNKNOWN) {
            clusionBuilder.appendCheckForVendorType(currentInfo.getVendorTypeCode())
                    .appendCheckForVendorOwnershipType(currentInfo.getVendorOwnershipCode())
                    .appendCheckForDocumentType(currentRow.getDocumentType());
        }

        boolean paymentReasonIsBlankOrProcessable = true;
        if (StringUtils.isNotBlank(currentRow.getPaymentReasonCode())) {
            clusionBuilder.appendCheckForPaymentReason(currentRow.getPaymentReasonCode());
            paymentReasonIsBlankOrProcessable = clusionBuilder.getResultOfPreviousCheck() == ClusionResult.INCLUDE;
        }

        if (paymentReasonIsBlankOrProcessable && taxBoxType != TaxBoxType1042S.UNKNOWN) {
            final String chartAndAccountPair = StringUtils.join(
                    currentRow.getChartCode(), KFSConstants.DASH, currentRow.getAccountNumber());
            final boolean isRoyaltyAmount = isRoyaltyAmount(currentRow, taxBoxType);
            final List<NoteLite> documentNotes = transactionDetailProcessorDao.getNotesByDocumentNumber(
                    currentRow.getDocumentNumber());
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

        if (clusionBuilder.getCumulativeResult() != ClusionResult.EXCLUDE) {
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
            final ClusionResult rowClusionResult) {
        if (taxBoxOverride != null) {
            return taxBoxOverride;
        } else if (rowClusionResult == ClusionResult.EXCLUDE) {
            return TaxBoxType1042S.UNKNOWN;
        } else {
            return taxBox;
        }
    }

    private Map<TransactionDetailColumn, String> generateUpdatedTransactionDetailFieldValues(
            final SprintaxInfo1042S currentInfo, final TaxBoxType1042S taxBox,
            final TaxBoxType1042S overriddenTaxBox) {
        final String overriddenTaxBoxForOutput = overriddenTaxBox != null ? overriddenTaxBox.toString() : null;
        return Map.ofEntries(
                Map.entry(TransactionDetailColumn.FORM_1042S_BOX, taxBox.toString()),
                Map.entry(TransactionDetailColumn.FORM_1042S_OVERRIDDEN_BOX, overriddenTaxBoxForOutput),
                Map.entry(TransactionDetailColumn.VENDOR_NAME, currentInfo.getVendorNameForOutput()),
                Map.entry(TransactionDetailColumn.PARENT_VENDOR_NAME, currentInfo.getParentVendorNameForOutput()),
                Map.entry(TransactionDetailColumn.VNDR_EMAIL_ADDR, currentInfo.getVendorEmailAddress()),
                Map.entry(TransactionDetailColumn.VNDR_CHAP_4_STAT_CD, currentInfo.getChapter4StatusCode()),
                Map.entry(TransactionDetailColumn.VNDR_GIIN, currentInfo.getVendorGIIN()),
                Map.entry(TransactionDetailColumn.VNDR_LN1_ADDR, currentInfo.getVendorUSAddressLine1()),
                Map.entry(TransactionDetailColumn.VNDR_LN2_ADDR, currentInfo.getVendorUSAddressLine2()),
                Map.entry(TransactionDetailColumn.VNDR_CTY_NM, currentInfo.getVendorUSCityName()),
                Map.entry(TransactionDetailColumn.VNDR_ST_CD, currentInfo.getVendorUSStateCode()),
                Map.entry(TransactionDetailColumn.VNDR_ZIP_CD, currentInfo.getVendorUSZipCode()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_LN1_ADDR, currentInfo.getVendorForeignAddressLine1()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_LN2_ADDR, currentInfo.getVendorForeignAddressLine2()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_CTY_NM, currentInfo.getVendorForeignCityName()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_ZIP_CD, currentInfo.getVendorForeignZipCode()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_PROV_NM, currentInfo.getVendorForeignProvinceName()),
                Map.entry(TransactionDetailColumn.VNDR_FRGN_CNTRY_CD, currentInfo.getVendorForeignCountryCode())
        );
    }



    private void finalizeCurrentInfo(final SprintaxInfo1042S currentInfo, final SprintaxHelper helper) {
        
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
        return taxParameterService.getParameterValuesAsString(CUTaxConstants.TAX_1042S_PARM_DETAIL, parameterName);
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



    private static final class SprintaxHelper implements TaxStatisticsHandler {
        private final TaxOutputConfig config;
        private final TransactionDetailExtractor rowExtractor;
        private final TaxFileRowWriterSprintaxBioFileImpl bioFileWriter;
        private final TaxFileRowWriterSprintaxPaymentsFileImpl paymentsFileWriter;
        private final Map<String, String> transactionOverrides;
        private final TaxStatistics statistics;

        private SprintaxHelper(final TaxOutputConfig config, final TransactionDetailExtractor rowExtractor,
                final TaxFileRowWriterSprintaxBioFileImpl bioFileWriter,
                final TaxFileRowWriterSprintaxPaymentsFileImpl paymentsFileWriter,
                final Map<String, String> transactionOverrides) {
            this.config = config;
            this.rowExtractor = rowExtractor;
            this.bioFileWriter = bioFileWriter;
            this.paymentsFileWriter = paymentsFileWriter;
            this.transactionOverrides = transactionOverrides;
            this.statistics = new TaxStatistics();
        }

        @Override
        public void increment(final TaxStatType entryType) {
            statistics.increment(entryType);
        }
    }

}
