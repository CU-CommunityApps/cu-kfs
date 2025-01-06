package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.TaxColumns.TransactionDetailColumn;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailExtractor;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public class TransactionDetailExtractorImpl extends TaxDataExtractorBase<TransactionDetail>
        implements TransactionDetailExtractor {

    public TransactionDetailExtractorImpl(final ResultSet resultSet, final EncryptionService encryptionService) {
        super(resultSet, encryptionService);
    }

    @Override
    public TransactionDetail getCurrentRow() throws SQLException {
        final TransactionDetail detail = new TransactionDetail();
        detail.setTransactionDetailId(getString(TransactionDetailColumn.IRS_1099_1042S_DETAIL_ID));
        detail.setReportYear(getInt(TransactionDetailColumn.REPORT_YEAR));
        detail.setDocumentNumber(getString(TransactionDetailColumn.FDOC_NBR));
        detail.setDocumentType(getString(TransactionDetailColumn.DOC_TYPE));
        detail.setFinancialDocumentLineNumber(getInt(TransactionDetailColumn.FDOC_LINE_NBR));
        detail.setFinObjectCode(getString(TransactionDetailColumn.FIN_OBJECT_CD));
        detail.setNetPaymentAmount(getAsKualiDecimal(TransactionDetailColumn.NET_PMT_AMT));
        detail.setDocumentTitle(getString(TransactionDetailColumn.DOC_TITLE));
        detail.setVendorTaxNumber(getAndDecrypt(TransactionDetailColumn.VENDOR_TAX_NBR));
        detail.setIncomeCode(getString(TransactionDetailColumn.INCOME_CODE));
        detail.setIncomeCodeSubType(getString(TransactionDetailColumn.INCOME_CODE_SUB_TYPE));
        detail.setDvCheckStubText(getString(TransactionDetailColumn.DV_CHK_STUB_TXT));
        detail.setPayeeId(getString(TransactionDetailColumn.PAYEE_ID));
        detail.setVendorName(getString(TransactionDetailColumn.VENDOR_NAME));
        detail.setParentVendorName(getString(TransactionDetailColumn.PARENT_VENDOR_NAME));
        detail.setVendorTypeCode(getString(TransactionDetailColumn.VNDR_TYP_CD));
        detail.setVendorOwnershipCode(getString(TransactionDetailColumn.VNDR_OWNR_CD));
        detail.setVendorOwnershipCategoryCode(getString(TransactionDetailColumn.VNDR_OWNR_CTGRY_CD));
        detail.setVendorForeignIndicator(getAsBoolean(TransactionDetailColumn.VNDR_FRGN_IND));
        detail.setVendorEmailAddress(getString(TransactionDetailColumn.VNDR_EMAIL_ADDR));
        detail.setVendorChapter4StatusCode(getString(TransactionDetailColumn.VNDR_CHAP_4_STAT_CD));
        detail.setVendorGIIN(getString(TransactionDetailColumn.VNDR_GIIN));
        detail.setVendorLine1Address(getString(TransactionDetailColumn.VNDR_LN1_ADDR));
        detail.setVendorLine2Address(getString(TransactionDetailColumn.VNDR_LN2_ADDR));
        detail.setVendorCityName(getString(TransactionDetailColumn.VNDR_CTY_NM));
        detail.setVendorStateCode(getString(TransactionDetailColumn.VNDR_ST_CD));
        detail.setVendorZipCode(getString(TransactionDetailColumn.VNDR_ZIP_CD));
        detail.setVendorForeignLine1Address(getString(TransactionDetailColumn.VNDR_FRGN_LN1_ADDR));
        detail.setVendorForeignLine2Address(getString(TransactionDetailColumn.VNDR_FRGN_LN2_ADDR));
        detail.setVendorForeignCityName(getString(TransactionDetailColumn.VNDR_FRGN_CTY_NM));
        detail.setVendorForeignZipCode(getString(TransactionDetailColumn.VNDR_FRGN_ZIP_CD));
        detail.setVendorForeignProvinceName(getString(TransactionDetailColumn.VNDR_FRGN_PROV_NM));
        detail.setVendorForeignCountryCode(getString(TransactionDetailColumn.VNDR_FRGN_CNTRY_CD));
        detail.setNraPaymentIndicator(getAsBoolean(TransactionDetailColumn.NRA_PAYMENT_IND));
        detail.setPaymentDate(getDate(TransactionDetailColumn.PMT_DT));
        detail.setPaymentPayeeName(getString(TransactionDetailColumn.PMT_PAYEE_NM));
        detail.setIncomeClassCode(getString(TransactionDetailColumn.INC_CLS_CD));
        detail.setIncomeTaxTreatyExemptIndicator(getAsBoolean(TransactionDetailColumn.INC_TAX_TRTY_EXMPT_IND));
        detail.setForeignSourceIncomeIndicator(getAsBoolean(TransactionDetailColumn.FRGN_SRC_INC_IND));
        detail.setFederalIncomeTaxPercent(getAsKualiDecimal(TransactionDetailColumn.FED_INC_TAX_PCT));
        detail.setPaymentDescription(getString(TransactionDetailColumn.PMT_DESC));
        detail.setPaymentLine1Address(getString(TransactionDetailColumn.PMT_LN1_ADDR));
        detail.setPaymentCountryName(getString(TransactionDetailColumn.PMT_CNTRY_NM));
        detail.setChartCode(getString(TransactionDetailColumn.KFS_CHART));
        detail.setAccountNumber(getString(TransactionDetailColumn.KFS_ACCOUNT));
        detail.setInitiatorNetId(getString(TransactionDetailColumn.INITIATOR_NETID));
        detail.setForm1099Type(getString(TransactionDetailColumn.FORM_1099_TYPE));
        detail.setForm1099Box(getString(TransactionDetailColumn.FORM_1099_BOX));
        detail.setForm1099OverriddenType(getString(TransactionDetailColumn.FORM_1099_OVERRIDDEN_TYPE));
        detail.setForm1099OverriddenBox(getString(TransactionDetailColumn.FORM_1099_OVERRIDDEN_BOX));
        detail.setForm1042SBox(getString(TransactionDetailColumn.FORM_1042S_BOX));
        detail.setForm1042SOverriddenBox(getString(TransactionDetailColumn.FORM_1042S_OVERRIDDEN_BOX));
        detail.setPaymentReasonCode(getString(TransactionDetailColumn.PMT_REASON_CD));
        detail.setDisbursementNbr(getAsKualiInteger(TransactionDetailColumn.DISB_NBR));
        detail.setPaymentStatusCode(getString(TransactionDetailColumn.PMT_STAT_CD));
        detail.setDisbursementTypeCode(getString(TransactionDetailColumn.DISB_TYP_CD));
        detail.setLedgerDocumentTypeCode(getString(TransactionDetailColumn.LEDGER_DOC_TYP_CD));
        return detail;
    }

    @Override
    public void updateCurrentRow(final Map<TransactionDetailColumn, String> fieldsToUpdate) throws SQLException {
        for (final Map.Entry<TransactionDetailColumn, String> field : fieldsToUpdate.entrySet()) {
            resultSet.updateString(field.getKey().name(), field.getValue());
        }
        resultSet.updateRow();
    }

}
