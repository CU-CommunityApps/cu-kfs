package edu.cornell.kfs.tax.batch.service.impl;

import java.sql.SQLException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dto.PdpSourceData;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;

public class TransactionDetailGeneratorPDP extends TransactionDetailGeneratorBase<PdpSourceData> {

    public TransactionDetailGeneratorPDP(final TaxBatchConfig config, final TaxDtoRowMapper<PdpSourceData> rowMapper,
            final TransactionDetailBuilderHelperService helperService) {
        super(config, rowMapper, helperService, CUTaxConstants.TAX_SOURCE_PDP, getInitialZeroValueStats(config));
    }

    private static TaxStatType[] getInitialZeroValueStats(final TaxBatchConfig config) {
        if (StringUtils.equals(config.getTaxType(), CUTaxConstants.TAX_TYPE_1042S)) {
            return new TaxStatType[] {
                TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES,
                TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODES_PDP,
                TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES,
                TaxStatType.NUM_EXCLUDED_ASSIGNED_INCOME_CODE_SUBTYPES_PDP,
                TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                TaxStatType.NUM_DETERMINED_FED_TAX_WITHHELD_INCOME_CODES_PDP,
                TaxStatType.NUM_UNDETERMINED_FED_TAX_WITHHELD_INCOME_CODES,
                TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES,
                TaxStatType.NUM_DETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES_PDP,
                TaxStatType.NUM_UNDETERMINED_STATE_INC_TAX_WITHHELD_INCOME_CODES
            };
        } else {
            return new TaxStatType[0];
        }
    }

    @Override
    protected TransactionDetail generateTransactionDetailFromCurrentSourceDataRow() throws SQLException {
        final TransactionDetail detail = new TransactionDetail();
        final PdpSourceData pdpRow = rowMapper.readCurrentRow();

        detail.setReportYear(config.getReportYear());
        detail.setDocumentNumber(StringUtils.defaultIfBlank(pdpRow.getCustPaymentDocNbr(), null));
        detail.setDocumentType(pdpRow.getFinancialDocumentTypeCode());
        detail.setFinancialDocumentLineNumber(getKualiIntegerAsPlainInteger(pdpRow.getAccountDetailId()));
        detail.setFinObjectCode(pdpRow.getFinObjectCode());
        detail.setNetPaymentAmount(defaultToZeroIfNull(pdpRow.getAccountNetAmount()));
        detail.setVendorTaxNumber(pdpRow.getVendorTaxNumber());
        detail.setPayeeId(pdpRow.getPayeeId());
        detail.setVendorTypeCode(pdpRow.getVendorTypeCode());
        detail.setVendorOwnershipCode(pdpRow.getVendorOwnershipCode());
        detail.setVendorOwnershipCategoryCode(pdpRow.getVendorOwnershipCategoryCode());
        detail.setVendorForeignIndicator(pdpRow.getVendorForeignIndicator());
        detail.setNraPaymentIndicator(pdpRow.getNonresidentPayment());
        detail.setPaymentDate(pdpRow.getDisbursementDate());
        detail.setPaymentPayeeName(pdpRow.getPayeeName());
        detail.setPaymentDescription(pdpRow.getAchPaymentDescription());
        detail.setPaymentLine1Address(pdpRow.getLine1Address());
        detail.setPaymentCountryName(pdpRow.getCountry());
        detail.setChartCode(pdpRow.getAccountDetailFinChartCode());
        detail.setAccountNumber(pdpRow.getAccountNbr());
        detail.setIncomeClassCode(getIncomeClassCode(pdpRow));
        detail.setDisbursementNbr(pdpRow.getDisbursementNbr());
        detail.setPaymentStatusCode(pdpRow.getPaymentStatusCode());
        detail.setDisbursementTypeCode(pdpRow.getDisbursementTypeCode());
        detail.setLedgerDocumentTypeCode(pdpRow.getFinancialDocumentTypeCode());

        return detail;
    }

    private String getIncomeClassCode(final PdpSourceData pdpRow) {
        final String documentType = pdpRow.getFinancialDocumentTypeCode();
        if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.PAYMENT_REQUEST)) {
            return pdpRow.getTaxClassificationCode();
        } else if (StringUtils.equalsIgnoreCase(documentType, CUPdpConstants.PdpDocumentTypes.DISBURSEMENT_VOUCHER)) {
            return pdpRow.getDvIncomeClassCode();
        } else {
            return null;
        }
    }

    @Override
    protected TransactionDetail prepareTransactionDetailForInsertionIfPossible(final TransactionDetail detail,
            final Optional<RouteHeaderLite> relatedRouteHeader) {
        final RouteHeaderLite routeHeader = relatedRouteHeader.orElse(emptyRouteHeader);

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
