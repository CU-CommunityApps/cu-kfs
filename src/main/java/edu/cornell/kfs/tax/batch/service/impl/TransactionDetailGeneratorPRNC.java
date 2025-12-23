package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dto.PrncSourceData;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TransactionDetailGeneratorPRNC extends TransactionDetailGeneratorBase<PrncSourceData> {

    private static final Logger LOG = LogManager.getLogger();

    public TransactionDetailGeneratorPRNC(final TaxBatchConfig config, final TaxDtoRowMapper<PrncSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) {
        super(config, rowMapper, helperService, CUTaxConstants.TAX_SOURCE_PRNC, getInitialZeroValueStats(config));
    }

    private static TaxStatType[] getInitialZeroValueStats(final TaxBatchConfig config) {
        final TaxStatType[] baseStats = {
            TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_SELECTED,
            TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_IGNORED,
            TaxStatType.NUM_PRNC_WIRE_TRANSFERS_SELECTED,
            TaxStatType.NUM_PRNC_WIRE_TRANSFERS_IGNORED
        };
        if (StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)) {
            return ArrayUtils.addAll(baseStats,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_PRNC,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES,
                    TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_PRNC,
                    TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_PRNC,
                    TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES,
                    TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_PRNC,
                    TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES
            );
        } else {
            return baseStats;
        }
    }

    @Override
    protected TransactionDetail generateTransactionDetailFromCurrentSourceDataRow() throws SQLException {
        final TransactionDetail detail = new TransactionDetail();
        final PrncSourceData prncRow = rowMapper.readCurrentRow();
        final String vendorId = StringUtils.join(prncRow.getVendorHeaderGeneratedIdentifier(),
                KFSConstants.DASH, prncRow.getPreqVendorDetailAssignedIdentifier());

        detail.setReportYear(config.getReportYear());
        detail.setDocumentNumber(StringUtils.defaultIfBlank(prncRow.getPreqDocumentNumber(), null));
        detail.setDocumentType(PurapConstants.PurapDocTypeCodes.PAYMENT_REQUEST_DOCUMENT);
        detail.setFinancialDocumentLineNumber(prncRow.getAccountIdentifier());
        detail.setFinObjectCode(prncRow.getFinancialObjectCode());
        detail.setNetPaymentAmount(defaultToZeroIfNull(prncRow.getAmount()));
        detail.setPaymentMethodCode(prncRow.getPaymentMethodCode());
        detail.setIncomeClassCode(prncRow.getTaxClassificationCode());
        detail.setVendorTaxNumber(prncRow.getVendorTaxNumber());
        detail.setPayeeId(vendorId);
        detail.setVendorTypeCode(prncRow.getVendorTypeCode());
        detail.setVendorOwnershipCode(prncRow.getVendorOwnershipCode());
        detail.setVendorOwnershipCategoryCode(prncRow.getVendorOwnershipCategoryCode());
        detail.setVendorForeignIndicator(prncRow.getVendorForeignIndicator());
        detail.setNraPaymentIndicator(prncRow.getVendorForeignIndicator());
        detail.setChartCode(prncRow.getChartOfAccountsCode());
        detail.setAccountNumber(prncRow.getAccountNumber());
        detail.setPaymentPayeeName(prncRow.getPreqVendorName());
        detail.setPaymentLine1Address(prncRow.getVendorLine1Address());
        detail.setPaymentCountryName(prncRow.getVendorCountryCode());
        detail.setLedgerDocumentTypeCode(CuPaymentRequestDocument.DOCUMENT_TYPE_NON_CHECK);
        setInitialValueForTaxBoxField(detail);

        return detail;
    }

    @Override
    protected TransactionDetail prepareTransactionDetailForInsertionIfPossible(final TransactionDetail detail,
            final Optional<RouteHeaderLite> relatedRouteHeader) {
        final RouteHeaderLite routeHeader = relatedRouteHeader.orElse(emptyRouteHeader);
        final String paymentMethodCode = detail.getPaymentMethodCode();

        if (isForeignDraftOrWireTransfer(paymentMethodCode)) {
            if (documentWasFinalizedDuringTaxReportingTimeframe(routeHeader)) {
                final TaxStatType statToUpdate = isForeignDraft(paymentMethodCode)
                        ? TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_SELECTED : TaxStatType.NUM_PRNC_WIRE_TRANSFERS_SELECTED;
                statistics.increment(statToUpdate);
                detail.setPaymentDate(routeHeader.getFinalizedDateAsSqlDate());
            } else {
                LOG.debug("prepareTransactionDetailForInsertionIfPossible, Skipping transaction detail persistence: "
                        + "Document {}, Line {}, Payment Method {}",
                        detail.getDocumentNumber(), detail.getFinancialDocumentLineNumber(), paymentMethodCode);
                final TaxStatType statToUpdate = isForeignDraft(paymentMethodCode)
                        ? TaxStatType.NUM_PRNC_FOREIGN_DRAFTS_IGNORED : TaxStatType.NUM_PRNC_WIRE_TRANSFERS_IGNORED;
                statistics.increment(statToUpdate);
                return null;
            }
        }

        final String initiatorPrincipalId = StringUtils.defaultIfBlank(
                routeHeader.getInitiatorPrincipalId(), CUTaxConstants.NETID_IF_NOT_FOUND);
        final String docTitle = StringUtils.defaultIfBlank(
                routeHeader.getDocTitle(), CUTaxConstants.DOC_TITLE_IF_NOT_FOUND);
        final String initiatorPrincipalName = getPrincipalNameIfPresentForPerson(initiatorPrincipalId);

        checkForAccountAndOrganizationExistence(detail);
        detail.setInitiatorNetId(StringUtils.defaultIfBlank(initiatorPrincipalName, CUTaxConstants.NETID_IF_NOT_FOUND));

        if (StringUtils.isBlank(detail.getVendorTaxNumber())) {
            detail.setVendorTaxNumber(helperService.getOrGeneratePlaceholderTaxIdForPayee(detail.getPayeeId()));
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

    @Override
    protected void populateIncomeCodeFieldsOnTransaction(final TransactionDetail detail) {
        final String incomeClassCode = StringUtils.defaultString(detail.getIncomeClassCode());
        final String objectCode = StringUtils.defaultString(detail.getFinObjectCode());
        String incomeCode;
        String incomeCodeSubType;

        if (StringUtils.isNotBlank(incomeClassCode)) {
            incomeCode = getIrsIncomeCodeByKfsIncomeClassCode(incomeClassCode);
            incomeCodeSubType = getIrsIncomeCodeSubTypeByKfsIncomeClassCode(incomeClassCode);
            if (StringUtils.isBlank(incomeCodeSubType)) {
                incomeCodeSubType = getExcludedIncomeCodeSubType();
                statistics.increment(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES, taxSourceType);
            }
        } else {
            if (shouldWithholdFederalTaxFor1042S(objectCode)) {
                incomeCode = getNonReportableIncomeCode();
                statistics.increment(TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES, taxSourceType);
            } else {
                statistics.increment(TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES);
                if (shouldWithholdStateIncomeTaxFor1042S(objectCode)) {
                    incomeCode = getNonReportableIncomeCode();
                    statistics.increment(TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES, taxSourceType);
                } else {
                    statistics.increment(TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES);
                    incomeCode = getExcludedIncomeCode();
                    statistics.increment(TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES, taxSourceType);
                }
            }
            incomeCodeSubType = getExcludedIncomeCodeSubType();
        }

        detail.setIncomeCode(incomeCode);
        detail.setIncomeCodeSubType(incomeCodeSubType);
    }

}
