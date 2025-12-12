package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dto.DvSourceData;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TransactionDetailGeneratorDV extends TransactionDetailGeneratorBase<DvSourceData> {

    private static final Logger LOG = LogManager.getLogger();
    private static final Pattern NON_PRINTABLE_CHARS = Pattern.compile("[^\\p{Graph}\\p{Space}]");

    public TransactionDetailGeneratorDV(final TaxBatchConfig config, final TaxDtoRowMapper<DvSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) {
        super(config, rowMapper, helperService, CUTaxConstants.TAX_SOURCE_DV, getInitialZeroValueStats(config));
    }

    private static TaxStatType[] getInitialZeroValueStats(final TaxBatchConfig config) {
        final TaxStatType[] baseStats = {
            TaxStatType.NUM_DV_CHECK_STUB_TEXTS_ALTERED,
            TaxStatType.NUM_DV_CHECK_STUB_TEXTS_NOT_ALTERED,
            TaxStatType.NUM_DV_FOREIGN_DRAFTS_SELECTED,
            TaxStatType.NUM_DV_FOREIGN_DRAFTS_IGNORED,
            TaxStatType.NUM_DV_WIRE_TRANSFERS_SELECTED,
            TaxStatType.NUM_DV_WIRE_TRANSFERS_IGNORED
        };
        if (StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)) {
            return ArrayUtils.addAll(baseStats,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_DV,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_DV,
                    TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_DV,
                    TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_DV,
                    TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES
            );
        } else {
            return baseStats;
        }
    }

    @Override
    protected TransactionDetail generateTransactionDetailFromCurrentSourceDataRow() throws SQLException {
        final TransactionDetail detail = new TransactionDetail();
        final DvSourceData dvRow = rowMapper.readCurrentRow();
        final KualiDecimal netPaymentAmount = getNetPaymentAmount(dvRow);
        final String paymentMethodCode = dvRow.getDocumentDisbVchrPaymentMethodCode();
        final boolean foreignDraftOrWireTransfer = isForeignDraftOrWireTransfer(paymentMethodCode);
        final KualiInteger disbursementNumber = foreignDraftOrWireTransfer ? null : dvRow.getDisbursementNbr();
        final String disbursementTypeCode = foreignDraftOrWireTransfer ? null : dvRow.getDisbursementTypeCode();
        final String paymentStatusCode = foreignDraftOrWireTransfer ? null : dvRow.getPaymentStatusCode();
        final String ledgerDocumentTypeCode = foreignDraftOrWireTransfer
                ? DisbursementVoucherConstants.DOCUMENT_TYPE_WTFD : DisbursementVoucherConstants.DOCUMENT_TYPE_CHECKACH;

        detail.setReportYear(config.getReportYear());
        detail.setDocumentNumber(StringUtils.defaultIfBlank(dvRow.getPayeeDetailDocumentNumber(), null));
        detail.setDocumentType(DisbursementVoucherConstants.DOCUMENT_TYPE_CODE);
        detail.setFinancialDocumentLineNumber(dvRow.getAccountingLineSequenceNumber());
        detail.setFinObjectCode(dvRow.getFinancialObjectCode());
        detail.setNetPaymentAmount(netPaymentAmount);
        detail.setPaymentMethodCode(paymentMethodCode);
        detail.setVendorTaxNumber(dvRow.getVendorTaxNumber());
        detail.setDvCheckStubText(dvRow.getDisbVchrCheckStubText());
        detail.setPayeeId(dvRow.getDisbVchrPayeeIdNumber());
        detail.setVendorTypeCode(dvRow.getVendorTypeCode());
        detail.setVendorOwnershipCode(dvRow.getVendorOwnershipCode());
        detail.setVendorOwnershipCategoryCode(dvRow.getVendorOwnershipCategoryCode());
        detail.setVendorForeignIndicator(dvRow.getVendorForeignIndicator());
        detail.setNraPaymentIndicator(dvRow.getDisbVchrNonresidentPaymentCode());
        detail.setPaymentDate(dvRow.getPaidDate());
        detail.setPaymentPayeeName(dvRow.getDisbVchrPayeePersonName());
        detail.setIncomeClassCode(dvRow.getIncomeClassCode());
        detail.setIncomeTaxTreatyExemptIndicator(dvRow.getIncomeTaxTreatyExemptCode());
        detail.setForeignSourceIncomeIndicator(dvRow.getForeignSourceIncomeCode());
        detail.setFederalIncomeTaxPercent(dvRow.getFederalIncomeTaxPercent());
        detail.setPaymentDescription(dvRow.getFinancialDocumentLineDescription());
        detail.setPaymentLine1Address(dvRow.getDisbVchrPayeeLine1Addr());
        detail.setPaymentCountryName(dvRow.getDisbVchrPayeeCountryCode());
        detail.setChartCode(dvRow.getChartOfAccountsCode());
        detail.setAccountNumber(dvRow.getAccountNumber());
        detail.setPaymentReasonCode(dvRow.getDisbVchrPaymentReasonCode());
        detail.setDisbursementNbr(disbursementNumber);
        detail.setDisbursementTypeCode(disbursementTypeCode);
        detail.setPaymentStatusCode(paymentStatusCode);
        detail.setLedgerDocumentTypeCode(ledgerDocumentTypeCode);
        setInitialValueForTaxBoxField(detail);

        return detail;
    }

    private KualiDecimal getNetPaymentAmount(final DvSourceData dvRow) {
        final KualiDecimal amount = dvRow.getAmount();
        if (amount == null) {
            return KualiDecimal.ZERO;
        } else if (StringUtils.equals(dvRow.getDebitCreditCode(), KFSConstants.GL_DEBIT_CODE)) {
            return amount.negated();
        } else {
            return amount;
        }
    }

    @Override
    protected TransactionDetail prepareTransactionDetailForInsertionIfPossible(final TransactionDetail detail,
            final Optional<RouteHeaderLite> relatedRouteHeader) {
        final RouteHeaderLite routeHeader = relatedRouteHeader.orElse(emptyRouteHeader);
        final String paymentMethodCode = detail.getPaymentMethodCode();
    
        if (isForeignDraftOrWireTransfer(paymentMethodCode)) {
            if (documentWasFinalizedDuringTaxReportingTimeframe(routeHeader)) {
                final TaxStatType statToUpdate = isForeignDraft(paymentMethodCode)
                        ? TaxStatType.NUM_DV_FOREIGN_DRAFTS_SELECTED : TaxStatType.NUM_DV_WIRE_TRANSFERS_SELECTED;
                statistics.increment(statToUpdate);
                detail.setPaymentDate(routeHeader.getFinalizedDateAsSqlDate());
            } else {
                LOG.debug("prepareTransactionDetailForInsertionIfPossible, Skipping transaction detail persistence: "
                        + "Document {}, Line {}, Payment Method {}",
                        detail.getDocumentNumber(), detail.getFinancialDocumentLineNumber(), paymentMethodCode);
                final TaxStatType statToUpdate = isForeignDraft(paymentMethodCode)
                        ? TaxStatType.NUM_DV_FOREIGN_DRAFTS_IGNORED : TaxStatType.NUM_DV_WIRE_TRANSFERS_IGNORED;
                statistics.increment(statToUpdate);
                return null;
            }
        }

        final String initiatorPrincipalId = StringUtils.defaultIfBlank(
                routeHeader.getInitiatorPrincipalId(), CUTaxConstants.NETID_IF_NOT_FOUND);
        final String docTitle = StringUtils.defaultIfBlank(
                routeHeader.getTitle(), CUTaxConstants.DOC_TITLE_IF_NOT_FOUND);
        final String initiatorPrincipalName = getPrincipalNameIfPresentForPerson(initiatorPrincipalId);

        checkForAccountAndOrganizationExistence(detail);
        detail.setInitiatorNetId(StringUtils.defaultIfBlank(initiatorPrincipalName, CUTaxConstants.NETID_IF_NOT_FOUND));

        if (StringUtils.isBlank(detail.getVendorTaxNumber())) {
            detail.setVendorTaxNumber(helperService.getOrGeneratePlaceholderTaxIdForPayee(detail.getPayeeId()));
        }

        final Matcher nonPrintableCheckStubCharsMatcher = NON_PRINTABLE_CHARS.matcher(detail.getDvCheckStubText());
        if (nonPrintableCheckStubCharsMatcher.find()) {
            detail.setDvCheckStubText(nonPrintableCheckStubCharsMatcher.replaceAll(KFSConstants.EMPTY_STRING));
            statistics.increment(TaxStatType.NUM_DV_CHECK_STUB_TEXTS_ALTERED);
        } else {
            statistics.increment(TaxStatType.NUM_DV_CHECK_STUB_TEXTS_NOT_ALTERED);
        }

        if (StringUtils.isBlank(detail.getDocumentNumber())) {
            detail.setDocumentNumber(CUTaxConstants.DOC_ID_ZERO);
        }
        detail.setDocumentTitle(docTitle);

        if (StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)) {
            populateIncomeCodeFieldsOnTransaction(detail);
        }

        return detail;
    }

}
