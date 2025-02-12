package edu.cornell.kfs.tax.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.kuali.kfs.core.api.encryption.EncryptionService;

import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailRowMapper;
import edu.cornell.kfs.tax.batch.metadata.TaxDtoDbMetadata;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

public abstract class TransactionDetailMapperBase<U> extends TaxDtoRowMapperBase<TransactionDetail, U>
        implements TransactionDetailRowMapper<U> {

    public TransactionDetailMapperBase(final EncryptionService encryptionService, final TaxDtoDbMetadata metadata,
            final ResultSet resultSet) {
        super(TransactionDetail::new, encryptionService, metadata, resultSet);
    }

    @Override
    protected void populateDtoFromCurrentRow(final TransactionDetail detail) throws SQLException {
        detail.setTransactionDetailId(getString(TransactionDetailField.transactionDetailId));
        detail.setReportYear(getInt(TransactionDetailField.reportYear));
        detail.setDocumentNumber(getString(TransactionDetailField.documentNumber));
        detail.setDocumentType(getString(TransactionDetailField.documentType));
        detail.setFinancialDocumentLineNumber(getInt(TransactionDetailField.financialDocumentLineNumber));
        detail.setFinObjectCode(getString(TransactionDetailField.finObjectCode));
        detail.setNetPaymentAmount(getKualiDecimal(TransactionDetailField.netPaymentAmount));
        detail.setDocumentTitle(getString(TransactionDetailField.documentTitle));
        detail.setVendorTaxNumber(getAndDecryptString(TransactionDetailField.vendorTaxNumber));
        detail.setIncomeCode(getString(TransactionDetailField.incomeCode));
        detail.setIncomeCodeSubType(getString(TransactionDetailField.incomeCodeSubType));
        detail.setDvCheckStubText(getString(TransactionDetailField.dvCheckStubText));
        detail.setPayeeId(getString(TransactionDetailField.payeeId));
        detail.setVendorName(getString(TransactionDetailField.vendorName));
        detail.setParentVendorName(getString(TransactionDetailField.parentVendorName));
        detail.setVendorTypeCode(getString(TransactionDetailField.vendorTypeCode));
        detail.setVendorOwnershipCode(getString(TransactionDetailField.vendorOwnershipCode));
        detail.setVendorOwnershipCategoryCode(getString(TransactionDetailField.vendorOwnershipCategoryCode));
        detail.setVendorForeignIndicator(getBoolean(TransactionDetailField.vendorForeignIndicator));
        detail.setVendorEmailAddress(getString(TransactionDetailField.vendorEmailAddress));
        detail.setVendorChapter4StatusCode(getString(TransactionDetailField.vendorChapter4StatusCode));
        detail.setVendorGIIN(getString(TransactionDetailField.vendorGIIN));
        detail.setVendorLine1Address(getString(TransactionDetailField.vendorLine1Address));
        detail.setVendorLine2Address(getString(TransactionDetailField.vendorLine2Address));
        detail.setVendorCityName(getString(TransactionDetailField.vendorCityName));
        detail.setVendorStateCode(getString(TransactionDetailField.vendorStateCode));
        detail.setVendorZipCode(getString(TransactionDetailField.vendorZipCode));
        detail.setVendorForeignLine1Address(getString(TransactionDetailField.vendorForeignLine1Address));
        detail.setVendorForeignLine2Address(getString(TransactionDetailField.vendorForeignLine2Address));
        detail.setVendorForeignCityName(getString(TransactionDetailField.vendorForeignCityName));
        detail.setVendorForeignZipCode(getString(TransactionDetailField.vendorForeignZipCode));
        detail.setVendorForeignProvinceName(getString(TransactionDetailField.vendorForeignProvinceName));
        detail.setVendorForeignCountryCode(getString(TransactionDetailField.vendorForeignCountryCode));
        detail.setNraPaymentIndicator(getBoolean(TransactionDetailField.nraPaymentIndicator));
        detail.setPaymentDate(getDate(TransactionDetailField.paymentDate));
        detail.setPaymentPayeeName(getString(TransactionDetailField.paymentPayeeName));
        detail.setIncomeClassCode(getString(TransactionDetailField.incomeClassCode));
        detail.setIncomeTaxTreatyExemptIndicator(getBoolean(TransactionDetailField.incomeTaxTreatyExemptIndicator));
        detail.setForeignSourceIncomeIndicator(getBoolean(TransactionDetailField.foreignSourceIncomeIndicator));
        detail.setFederalIncomeTaxPercent(getKualiDecimal(TransactionDetailField.federalIncomeTaxPercent));
        detail.setPaymentDescription(getString(TransactionDetailField.paymentDescription));
        detail.setPaymentLine1Address(getString(TransactionDetailField.paymentLine1Address));
        detail.setPaymentCountryName(getString(TransactionDetailField.paymentCountryName));
        detail.setChartCode(getString(TransactionDetailField.chartCode));
        detail.setAccountNumber(getString(TransactionDetailField.accountNumber));
        detail.setInitiatorNetId(getString(TransactionDetailField.initiatorNetId));
        detail.setForm1099Type(getString(TransactionDetailField.form1099Type));
        detail.setForm1099Box(getString(TransactionDetailField.form1099Box));
        detail.setForm1099OverriddenType(getString(TransactionDetailField.form1099OverriddenType));
        detail.setForm1099OverriddenBox(getString(TransactionDetailField.form1099OverriddenBox));
        detail.setForm1042SBox(getString(TransactionDetailField.form1042SBox));
        detail.setForm1042SOverriddenBox(getString(TransactionDetailField.form1042SOverriddenBox));
        detail.setPaymentReasonCode(getString(TransactionDetailField.paymentReasonCode));
        detail.setDisbursementNbr(getKualiInteger(TransactionDetailField.disbursementNbr));
        detail.setPaymentStatusCode(getString(TransactionDetailField.paymentStatusCode));
        detail.setDisbursementTypeCode(getString(TransactionDetailField.disbursementTypeCode));
        detail.setLedgerDocumentTypeCode(getString(TransactionDetailField.ledgerDocumentTypeCode));
    }

}
