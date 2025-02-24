package edu.cornell.kfs.tax.batch;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.CsvBatchInputFileTypeBase;

import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public class TestTransactionDetailCsvInputFileType extends CsvBatchInputFileTypeBase<TransactionDetailField> {

    public static final String IDENTIFIER = "testTransactionDetailSourceData";

    @Override
    public String getFileName(final String principalName, final Object parsedFileContents,
            final String fileUserIdentifier) {
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public String getFileTypeIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        return null;
    }

    @Override
    public String getTitleKey() {
        return KFSConstants.EMPTY_STRING;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object convertParsedObjectToVO(final Object parsedContent) {
        final List<Map<String, String>> parsedCsvRows = (List<Map<String, String>>) parsedContent;
        return parsedCsvRows.stream()
                .map(this::buildTransactionDetail)
                .collect(Collectors.toUnmodifiableList());
    }

    private TransactionDetail buildTransactionDetail(final Map<String, String> parsedRow) {
        final TestTransactionDetailFieldConverter converter = new TestTransactionDetailFieldConverter(parsedRow);
        final TransactionDetail detail = new TransactionDetail();

        detail.setTransactionDetailId(converter.getString(TransactionDetailField.transactionDetailId));
        detail.setReportYear(converter.getInteger(TransactionDetailField.reportYear));
        detail.setDocumentNumber(converter.getString(TransactionDetailField.documentNumber));
        detail.setDocumentType(converter.getString(TransactionDetailField.documentType));
        detail.setFinancialDocumentLineNumber(
                converter.getInteger(TransactionDetailField.financialDocumentLineNumber));
        detail.setFinObjectCode(converter.getString(TransactionDetailField.finObjectCode));
        detail.setNetPaymentAmount(converter.getKualiDecimal(TransactionDetailField.netPaymentAmount));
        detail.setDocumentTitle(converter.getString(TransactionDetailField.documentTitle));
        detail.setVendorTaxNumber(converter.getString(TransactionDetailField.vendorTaxNumber));
        detail.setIncomeCode(converter.getString(TransactionDetailField.incomeCode));
        detail.setIncomeCodeSubType(converter.getString(TransactionDetailField.incomeCodeSubType));
        detail.setDvCheckStubText(converter.getString(TransactionDetailField.dvCheckStubText));
        detail.setPayeeId(converter.getString(TransactionDetailField.payeeId));
        detail.setVendorName(converter.getString(TransactionDetailField.vendorName));
        detail.setParentVendorName(converter.getString(TransactionDetailField.parentVendorName));
        detail.setVendorTypeCode(converter.getString(TransactionDetailField.vendorTypeCode));
        detail.setVendorOwnershipCode(converter.getString(TransactionDetailField.vendorOwnershipCode));
        detail.setVendorOwnershipCategoryCode(converter.getString(TransactionDetailField.vendorOwnershipCategoryCode));
        detail.setVendorForeignIndicator(converter.getBoolean(TransactionDetailField.vendorForeignIndicator));
        detail.setVendorEmailAddress(converter.getString(TransactionDetailField.vendorEmailAddress));
        detail.setVendorChapter4StatusCode(converter.getString(TransactionDetailField.vendorChapter4StatusCode));
        detail.setVendorGIIN(converter.getString(TransactionDetailField.vendorGIIN));
        detail.setVendorLine1Address(converter.getString(TransactionDetailField.vendorLine1Address));
        detail.setVendorLine2Address(converter.getString(TransactionDetailField.vendorLine2Address));
        detail.setVendorCityName(converter.getString(TransactionDetailField.vendorCityName));
        detail.setVendorStateCode(converter.getString(TransactionDetailField.vendorStateCode));
        detail.setVendorZipCode(converter.getString(TransactionDetailField.vendorZipCode));
        detail.setVendorForeignLine1Address(converter.getString(TransactionDetailField.vendorForeignLine1Address));
        detail.setVendorForeignLine2Address(converter.getString(TransactionDetailField.vendorForeignLine2Address));
        detail.setVendorForeignCityName(converter.getString(TransactionDetailField.vendorForeignCityName));
        detail.setVendorForeignZipCode(converter.getString(TransactionDetailField.vendorForeignZipCode));
        detail.setVendorForeignProvinceName(converter.getString(TransactionDetailField.vendorForeignProvinceName));
        detail.setVendorForeignCountryCode(converter.getString(TransactionDetailField.vendorForeignCountryCode));
        detail.setNraPaymentIndicator(converter.getBoolean(TransactionDetailField.nraPaymentIndicator));
        detail.setPaymentDate(converter.getSqlDate(TransactionDetailField.paymentDate));
        detail.setPaymentPayeeName(converter.getString(TransactionDetailField.paymentPayeeName));
        detail.setIncomeClassCode(converter.getString(TransactionDetailField.incomeClassCode));
        detail.setIncomeTaxTreatyExemptIndicator(
                converter.getBoolean(TransactionDetailField.incomeTaxTreatyExemptIndicator));
        detail.setForeignSourceIncomeIndicator(
                converter.getBoolean(TransactionDetailField.foreignSourceIncomeIndicator));
        detail.setFederalIncomeTaxPercent(converter.getKualiDecimal(TransactionDetailField.federalIncomeTaxPercent));
        detail.setPaymentDescription(converter.getString(TransactionDetailField.paymentDescription));
        detail.setPaymentLine1Address(converter.getString(TransactionDetailField.paymentLine1Address));
        detail.setPaymentCountryName(converter.getString(TransactionDetailField.paymentCountryName));
        detail.setChartCode(converter.getString(TransactionDetailField.chartCode));
        detail.setAccountNumber(converter.getString(TransactionDetailField.accountNumber));
        detail.setInitiatorNetId(converter.getString(TransactionDetailField.initiatorNetId));
        detail.setForm1099Type(converter.getString(TransactionDetailField.form1099Type));
        detail.setForm1099Box(converter.getString(TransactionDetailField.form1099Box));
        detail.setForm1099OverriddenType(converter.getString(TransactionDetailField.form1099OverriddenType));
        detail.setForm1099OverriddenBox(converter.getString(TransactionDetailField.form1099OverriddenBox));
        detail.setForm1042SBox(converter.getString(TransactionDetailField.form1042SBox));
        detail.setForm1042SOverriddenBox(converter.getString(TransactionDetailField.form1042SOverriddenBox));
        detail.setPaymentReasonCode(converter.getString(TransactionDetailField.paymentReasonCode));
        detail.setDisbursementNbr(converter.getKualiInteger(TransactionDetailField.disbursementNbr));
        detail.setPaymentStatusCode(converter.getString(TransactionDetailField.paymentStatusCode));
        detail.setDisbursementTypeCode(converter.getString(TransactionDetailField.disbursementTypeCode));
        detail.setLedgerDocumentTypeCode(converter.getString(TransactionDetailField.ledgerDocumentTypeCode));

        return detail;
    }



    private static final class TestTransactionDetailFieldConverter {

        private final Map<String, String> parsedRow;

        private TestTransactionDetailFieldConverter(final Map<String, String> parsedRow) {
            this.parsedRow = parsedRow;
        }
        
        private String getString(final TransactionDetailField field) {
            final String fieldName = field.getFieldName();
            return StringUtils.defaultIfEmpty(parsedRow.get(fieldName), null);
        }

        private Boolean getBoolean(final TransactionDetailField field) {
            final String value = getString(field);
            return value != null ? Truth.strToBooleanIgnoreCase(value) : null;
        }

        private Integer getInteger(final TransactionDetailField field) {
            final String value = getString(field);
            return value != null ? Integer.valueOf(value) : null;
        }

        private KualiDecimal getKualiDecimal(final TransactionDetailField field) {
            final String value = getString(field);
            return value != null ? new KualiDecimal(value) : null;
        }

        private KualiInteger getKualiInteger(final TransactionDetailField field) {
            final String value = getString(field);
            return value != null ? new KualiInteger(value) : null;
        }

        private java.sql.Date getSqlDate(final TransactionDetailField field) {
            final String value = getString(field);
            return value != null ? TestDateUtils.toSqlDate(value) : null;
        }

    }

}
