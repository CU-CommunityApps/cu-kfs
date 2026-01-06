package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.businessobject.DvDisbursementView;

@HasNestedEnumWithDtoFieldListing
public class DvSourceData {
    // Fields from FP_DV_PAYEE_DTL_T (DisbursementVoucherPayeeDetail)
    private String payeeDetailDocumentNumber;
    private String disbVchrPaymentReasonCode;
    private Boolean disbVchrNonresidentPaymentCode;
    private String disbVchrPayeeIdNumber;
    private String disbVchrPayeePersonName;
    private String disbVchrPayeeLine1Addr;
    private String disbVchrPayeeCountryCode;
    private String disbursementVoucherPayeeTypeCode;
    // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
    private String nraDocumentNumber;
    private KualiDecimal federalIncomeTaxPercent;
    private String incomeClassCode;
    private Boolean incomeTaxTreatyExemptCode;
    private Boolean foreignSourceIncomeCode;
    // Fields from FP_ACCT_LINES_T (Accounting lines table)
    private String accountingLineDocumentNumber;
    private Integer accountingLineSequenceNumber;
    private String financialDocumentLineTypeCode;
    private KualiDecimal amount;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String financialDocumentLineDescription;
    private String debitCreditCode;
    // Fields from FP_DV_DOC_T (CuDisbursementVoucherDocument)
    private String dvDocumentNumber;
    private String disbVchrCheckStubText;
    private String documentDisbVchrPaymentMethodCode;
    private Date extractDate;
    private Date paidDate;
    // Fields from PUR_VNDR_HDR_T (VendorHeader)
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorTaxNumber;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private Boolean vendorForeignIndicator;
    // Fields from TX_DV_DISBURSEMENT_V (Disbursement Fields)
    private String custPaymentDocNbr;
    private KualiInteger disbursementNbr;
    private String paymentStatusCode;
    private String disbursementTypeCode;

    public String getPayeeDetailDocumentNumber() {
        return payeeDetailDocumentNumber;
    }

    public void setPayeeDetailDocumentNumber(final String payeeDetailDocumentNumber) {
        this.payeeDetailDocumentNumber = payeeDetailDocumentNumber;
    }

    public String getDisbVchrPaymentReasonCode() {
        return disbVchrPaymentReasonCode;
    }

    public void setDisbVchrPaymentReasonCode(final String disbVchrPaymentReasonCode) {
        this.disbVchrPaymentReasonCode = disbVchrPaymentReasonCode;
    }

    public Boolean getDisbVchrNonresidentPaymentCode() {
        return disbVchrNonresidentPaymentCode;
    }

    public void setDisbVchrNonresidentPaymentCode(final Boolean disbVchrNonresidentPaymentCode) {
        this.disbVchrNonresidentPaymentCode = disbVchrNonresidentPaymentCode;
    }

    public String getDisbVchrPayeeIdNumber() {
        return disbVchrPayeeIdNumber;
    }

    public void setDisbVchrPayeeIdNumber(final String disbVchrPayeeIdNumber) {
        this.disbVchrPayeeIdNumber = disbVchrPayeeIdNumber;
    }

    public String getDisbVchrPayeePersonName() {
        return disbVchrPayeePersonName;
    }

    public void setDisbVchrPayeePersonName(final String disbVchrPayeePersonName) {
        this.disbVchrPayeePersonName = disbVchrPayeePersonName;
    }

    public String getDisbVchrPayeeLine1Addr() {
        return disbVchrPayeeLine1Addr;
    }

    public void setDisbVchrPayeeLine1Addr(final String disbVchrPayeeLine1Addr) {
        this.disbVchrPayeeLine1Addr = disbVchrPayeeLine1Addr;
    }

    public String getDisbVchrPayeeCountryCode() {
        return disbVchrPayeeCountryCode;
    }

    public void setDisbVchrPayeeCountryCode(final String disbVchrPayeeCountryCode) {
        this.disbVchrPayeeCountryCode = disbVchrPayeeCountryCode;
    }

    public String getDisbursementVoucherPayeeTypeCode() {
        return disbursementVoucherPayeeTypeCode;
    }

    public void setDisbursementVoucherPayeeTypeCode(final String disbursementVoucherPayeeTypeCode) {
        this.disbursementVoucherPayeeTypeCode = disbursementVoucherPayeeTypeCode;
    }

    public String getNraDocumentNumber() {
        return nraDocumentNumber;
    }

    public void setNraDocumentNumber(final String nraDocumentNumber) {
        this.nraDocumentNumber = nraDocumentNumber;
    }

    public KualiDecimal getFederalIncomeTaxPercent() {
        return federalIncomeTaxPercent;
    }

    public void setFederalIncomeTaxPercent(final KualiDecimal federalIncomeTaxPercent) {
        this.federalIncomeTaxPercent = federalIncomeTaxPercent;
    }

    public String getIncomeClassCode() {
        return incomeClassCode;
    }

    public void setIncomeClassCode(final String incomeClassCode) {
        this.incomeClassCode = incomeClassCode;
    }

    public Boolean getIncomeTaxTreatyExemptCode() {
        return incomeTaxTreatyExemptCode;
    }

    public void setIncomeTaxTreatyExemptCode(final Boolean incomeTaxTreatyExemptCode) {
        this.incomeTaxTreatyExemptCode = incomeTaxTreatyExemptCode;
    }

    public Boolean getForeignSourceIncomeCode() {
        return foreignSourceIncomeCode;
    }

    public void setForeignSourceIncomeCode(final Boolean foreignSourceIncomeCode) {
        this.foreignSourceIncomeCode = foreignSourceIncomeCode;
    }

    public String getAccountingLineDocumentNumber() {
        return accountingLineDocumentNumber;
    }

    public void setAccountingLineDocumentNumber(final String accountingLineDocumentNumber) {
        this.accountingLineDocumentNumber = accountingLineDocumentNumber;
    }

    public Integer getAccountingLineSequenceNumber() {
        return accountingLineSequenceNumber;
    }

    public void setAccountingLineSequenceNumber(final Integer accountingLineSequenceNumber) {
        this.accountingLineSequenceNumber = accountingLineSequenceNumber;
    }

    public String getFinancialDocumentLineTypeCode() {
        return financialDocumentLineTypeCode;
    }

    public void setFinancialDocumentLineTypeCode(final String financialDocumentLineTypeCode) {
        this.financialDocumentLineTypeCode = financialDocumentLineTypeCode;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(final KualiDecimal amount) {
        this.amount = amount;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(final String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getFinancialDocumentLineDescription() {
        return financialDocumentLineDescription;
    }

    public void setFinancialDocumentLineDescription(final String financialDocumentLineDescription) {
        this.financialDocumentLineDescription = financialDocumentLineDescription;
    }

    public String getDebitCreditCode() {
        return debitCreditCode;
    }

    public void setDebitCreditCode(final String debitCreditCode) {
        this.debitCreditCode = debitCreditCode;
    }

    public String getDvDocumentNumber() {
        return dvDocumentNumber;
    }

    public void setDvDocumentNumber(final String dvDocumentNumber) {
        this.dvDocumentNumber = dvDocumentNumber;
    }

    public String getDisbVchrCheckStubText() {
        return disbVchrCheckStubText;
    }

    public void setDisbVchrCheckStubText(final String disbVchrCheckStubText) {
        this.disbVchrCheckStubText = disbVchrCheckStubText;
    }

    public String getDocumentDisbVchrPaymentMethodCode() {
        return documentDisbVchrPaymentMethodCode;
    }

    public void setDocumentDisbVchrPaymentMethodCode(final String documentDisbVchrPaymentMethodCode) {
        this.documentDisbVchrPaymentMethodCode = documentDisbVchrPaymentMethodCode;
    }

    public Date getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(final Date extractDate) {
        this.extractDate = extractDate;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(final Date paidDate) {
        this.paidDate = paidDate;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(final Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(final String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(final String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(final String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getVendorOwnershipCategoryCode() {
        return vendorOwnershipCategoryCode;
    }

    public void setVendorOwnershipCategoryCode(final String vendorOwnershipCategoryCode) {
        this.vendorOwnershipCategoryCode = vendorOwnershipCategoryCode;
    }

    public Boolean getVendorForeignIndicator() {
        return vendorForeignIndicator;
    }

    public void setVendorForeignIndicator(final Boolean vendorForeignIndicator) {
        this.vendorForeignIndicator = vendorForeignIndicator;
    }

    public String getCustPaymentDocNbr() {
        return custPaymentDocNbr;
    }

    public void setCustPaymentDocNbr(final String custPaymentDocNbr) {
        this.custPaymentDocNbr = custPaymentDocNbr;
    }

    public KualiInteger getDisbursementNbr() {
        return disbursementNbr;
    }

    public void setDisbursementNbr(final KualiInteger disbursementNbr) {
        this.disbursementNbr = disbursementNbr;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    public void setPaymentStatusCode(final String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public String getDisbursementTypeCode() {
        return disbursementTypeCode;
    }

    public void setDisbursementTypeCode(final String disbursementTypeCode) {
        this.disbursementTypeCode = disbursementTypeCode;
    }

    public enum DvSourceDataField implements TaxDtoFieldEnum {
        // Fields from FP_DV_PAYEE_DTL_T (DisbursementVoucherPayeeDetail)
        payeeDetailDocumentNumber(DisbursementVoucherPayeeDetail.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        disbVchrPaymentReasonCode(DisbursementVoucherPayeeDetail.class),
        disbVchrNonresidentPaymentCode(DisbursementVoucherPayeeDetail.class),
        disbVchrPayeeIdNumber(DisbursementVoucherPayeeDetail.class),
        disbVchrPayeePersonName(DisbursementVoucherPayeeDetail.class),
        disbVchrPayeeLine1Addr(DisbursementVoucherPayeeDetail.class),
        disbVchrPayeeCountryCode(DisbursementVoucherPayeeDetail.class),
        disbursementVoucherPayeeTypeCode(DisbursementVoucherPayeeDetail.class),
        // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
        nraDocumentNumber(DisbursementVoucherNonresidentTax.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        federalIncomeTaxPercent(DisbursementVoucherNonresidentTax.class),
        incomeClassCode(DisbursementVoucherNonresidentTax.class),
        incomeTaxTreatyExemptCode(DisbursementVoucherNonresidentTax.class),
        foreignSourceIncomeCode(DisbursementVoucherNonresidentTax.class),
        // Fields from FP_ACCT_LINES_T (Accounting lines table)
        accountingLineDocumentNumber(SourceAccountingLine.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        accountingLineSequenceNumber(SourceAccountingLine.class, KFSPropertyConstants.SEQUENCE_NUMBER),
        financialDocumentLineTypeCode(SourceAccountingLine.class),
        amount(SourceAccountingLine.class),
        chartOfAccountsCode(SourceAccountingLine.class),
        accountNumber(SourceAccountingLine.class),
        financialObjectCode(SourceAccountingLine.class),
        financialDocumentLineDescription(SourceAccountingLine.class),
        debitCreditCode(SourceAccountingLine.class),
        // Fields from FP_DV_DOC_T (CuDisbursementVoucherDocument)
        dvDocumentNumber(CuDisbursementVoucherDocument.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        disbVchrCheckStubText(CuDisbursementVoucherDocument.class),
        documentDisbVchrPaymentMethodCode(CuDisbursementVoucherDocument.class,
                KFSPropertyConstants.DISB_VCHR_PAYMENT_METHOD_CODE),
        extractDate(CuDisbursementVoucherDocument.class),
        paidDate(CuDisbursementVoucherDocument.class),
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        vendorHeaderGeneratedIdentifier(VendorHeader.class),
        vendorTaxNumber(VendorHeader.class),
        vendorTypeCode(VendorHeader.class),
        vendorOwnershipCode(VendorHeader.class),
        vendorOwnershipCategoryCode(VendorHeader.class),
        vendorForeignIndicator(VendorHeader.class),
        // Fields from TX_DV_DISBURSEMENT_V (Disbursement Fields)
        custPaymentDocNbr(DvDisbursementView.class),
        disbursementNbr(DvDisbursementView.class),
        paymentStatusCode(DvDisbursementView.class),
        disbursementTypeCode(DvDisbursementView.class);

        private final Class<? extends BusinessObject> boClass;
        private final String boFieldName;

        private DvSourceDataField(final Class<? extends BusinessObject> boClass) {
            this(boClass, null);
        }

        private DvSourceDataField(final Class<? extends BusinessObject> boClass, final String boFieldName) {
            this.boClass = boClass;
            this.boFieldName = StringUtils.defaultIfBlank(boFieldName, name());
        }

        @Override
        public Class<? extends BusinessObject> getMappedBusinessObjectClass() {
            return boClass;
        }

        @Override
        public String getBusinessObjectFieldName() {
            return boFieldName;
        }

        @Override
        public boolean needsEncryptedStorage() {
            return this == vendorTaxNumber;
        }
    }

}
