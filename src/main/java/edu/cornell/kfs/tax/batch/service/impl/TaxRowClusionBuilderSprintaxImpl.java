package edu.cornell.kfs.tax.batch.service.impl;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TaxBoxType1042S;
import edu.cornell.kfs.tax.batch.TaxParameterClusionHelper;
import edu.cornell.kfs.tax.batch.TaxRowClusionBuilderBase;
import edu.cornell.kfs.tax.batch.TaxRowClusionResult;
import edu.cornell.kfs.tax.batch.TaxStatisticsHandler;
import edu.cornell.kfs.tax.batch.dto.NoteLite;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.service.TaxParameterService;

public class TaxRowClusionBuilderSprintaxImpl extends TaxRowClusionBuilderBase {

    public TaxRowClusionBuilderSprintaxImpl(final TaxStatisticsHandler statsHandler,
            final TaxParameterService taxParameterService, final String transactionDocType) {
        super(statsHandler, taxParameterService, CUTaxConstants.TAX_1042S_PARM_DETAIL, transactionDocType);
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForVendorType(final String vendorTypeCode) {
        checkAgainstExclusions(
                CUTaxConstants.TAX_TYPE_1042S + TaxCommonParameterNames.EXCLUDE_BY_VENDOR_TYPE_PARAMETER_SUFFIX,
                vendorTypeCode, TaxStatType.NUM_VENDOR_TYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForVendorOwnershipType(
            final String vendorOwnershipTypeCode) {
        checkAgainstExclusions(
                CUTaxConstants.TAX_TYPE_1042S + TaxCommonParameterNames.EXCLUDE_BY_OWNERSHIP_TYPE_PARAMETER_SUFFIX,
                vendorOwnershipTypeCode, TaxStatType.NUM_OWNERSHIP_TYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForDocumentType(final String documentTypeCode) {
        checkAgainstExclusions(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, documentTypeCode,
                TaxStatType.NUM_PDP_DOCTYPE_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForPaymentReason(final String paymentReasonCode) {
        checkAgainstExclusions(Tax1042SParameterNames.EXCLUDED_PAYMENT_REASON_CODE, paymentReasonCode,
                TaxStatType.NUM_PAYMENT_REASON_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForPaymentLine1Address(final String paymentLine1Address) {
        checkAgainstExcludedPrefixes(Tax1042SParameterNames.OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR,
                paymentLine1Address, TaxStatType.NUM_PAYMENT_ADDRESS_EXCLUSIONS_DETERMINED);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForDocumentNotes(final List<NoteLite> documentNotes) {
        if (CollectionUtils.isNotEmpty(documentNotes)) {
            statsHandler.increment(TaxStatType.NUM_DOC_NOTE_SETS_RETRIEVED, transactionDocType);
            final Stream<String> noteTextStream = documentNotes.stream().map(NoteLite::getNoteText);
            checkAgainstExcludedPrefixes(
                    Tax1042SParameterNames.EXCLUDED_DOC_NOTE_TEXT, noteTextStream,
                    TaxStatType.NUM_DOC_NOTES_EXCLUSIONS_DETERMINED);
        } else {
            resultOfPreviousCheck = TaxRowClusionResult.INCLUDE;
            statsHandler.increment(TaxStatType.NUM_DOC_NOTE_SETS_NOT_RETRIEVED, transactionDocType);
        }
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForAccountOnRoyalties(
            final String objectCode, final String chartAndAccountPair) {
        final TaxParameterClusionHelper helper = new TaxParameterClusionHelper(
                Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, true,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_CHART_ACCOUNT_EXCLUSIONS_DETERMINED);
        checkAgainstSubValues(helper, objectCode, chartAndAccountPair);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForDvCheckStubTextOnRoyalties(
            final String objectCode, final String dvCheckStubText) {
        final TaxParameterClusionHelper helper = new TaxParameterClusionHelper(
                Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT, true,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_NEITHER_DETERMINED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_ROYALTY_OBJ_DV_CHK_STUB_EXCLUSIONS_DETERMINED);
        checkAgainstPatterns(helper, objectCode, dvCheckStubText);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForAccountOnFederalTaxWithholding(
            final String objectCode, final String chartAndAccountPair) {
        final TaxParameterClusionHelper helper = new TaxParameterClusionHelper(
                Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, true,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_FTW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED);
        checkAgainstSubValues(helper, objectCode, chartAndAccountPair);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForAccountOnStateTaxWithholding(
            final String objectCode, final String chartAndAccountPair) {
        final TaxParameterClusionHelper helper = new TaxParameterClusionHelper(
                Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, true,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_NEITHER_DETERMINED,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_INCLUSIONS_DETERMINED,
                TaxStatType.NUM_SITW_CHART_ACCOUNT_EXCLUSIONS_DETERMINED);
        checkAgainstSubValues(helper, objectCode, chartAndAccountPair);
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForIncomeCode(
            final String incomeCode, final TaxBoxType1042S taxBoxType) {
        final String excludedIncomeCode = getParameter(Tax1042SParameterNames.EXCLUDED_INCOME_CODE);
        if (StringUtils.equals(incomeCode, excludedIncomeCode)) {
            resultOfPreviousCheck = TaxRowClusionResult.EXCLUDE;
            cumulativeResult = TaxRowClusionResult.EXCLUDE;
            if (taxBoxType == TaxBoxType1042S.GROSS_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_EXCLUDED_GROSS_AMOUNTS, transactionDocType);
            } else if (taxBoxType == TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_EXCLUDED_FTW_AMOUNTS, transactionDocType);
            } else if (taxBoxType == TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_EXCLUDED_SITW_AMOUNTS, transactionDocType);
            }
        } else {
            resultOfPreviousCheck = TaxRowClusionResult.INCLUDE;
        }
        return this;
    }

    public TaxRowClusionBuilderSprintaxImpl appendCheckForIncomeCodeSubType(
            final String incomeCodeSubType, final TaxBoxType1042S taxBoxType) {
        final String excludedIncomeCodeSubType = getParameter(Tax1042SParameterNames.EXCLUDED_INCOME_CODE_SUB_TYPE);
        if (StringUtils.equals(incomeCodeSubType, excludedIncomeCodeSubType)) {
            resultOfPreviousCheck = TaxRowClusionResult.EXCLUDE;
            cumulativeResult = TaxRowClusionResult.EXCLUDE;
            if (taxBoxType == TaxBoxType1042S.GROSS_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_GROSS_AMOUNTS, transactionDocType);
            } else if (taxBoxType == TaxBoxType1042S.FEDERAL_TAX_WITHHELD_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_FTW_AMOUNTS, transactionDocType);
            } else if (taxBoxType == TaxBoxType1042S.STATE_INCOME_TAX_WITHHELD_AMOUNT) {
                statsHandler.increment(TaxStatType.NUM_INCOME_CODE_SUBTYPE_EXCLUDED_SITW_AMOUNTS, transactionDocType);
            }
        } else {
            resultOfPreviousCheck = TaxRowClusionResult.INCLUDE;
        }
        return this;
    }

}
