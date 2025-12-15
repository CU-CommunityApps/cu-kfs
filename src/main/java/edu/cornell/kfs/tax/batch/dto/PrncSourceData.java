package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestAccount;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class PrncSourceData {

    // Fields from AP_PMT_RQST_ACCT_T (PaymentRequestAccount)
    private Integer accountIdentifier;
    private Integer accountItemIdentifier;
    private KualiDecimal amount;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    // Fields from AP_PMT_RQST_ITM_T (PaymentRequestItem)
    private Integer purapDocumentIdentifier;
    private Integer itemIdentifier;
    // Fields from AP_PMT_RQST_T (PaymentRequestDocument)
    private String preqDocumentNumber;
    private Integer preqPurapDocumentIdentifier;
    private String paymentMethodCode;
    private String taxClassificationCode;
    private Integer preqVendorHeaderGeneratedIdentifier;
    private Integer preqVendorDetailAssignedIdentifier;
    private String preqVendorName;
    private String vendorLine1Address;
    private String vendorCountryCode;
    // Fields from PUR_VNDR_HDR_T (VendorHeader)
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorTaxNumber;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private Boolean vendorForeignIndicator;
    // Fields from SH_UNIV_DATE_T (UniversityDate)
    private Date universityDate;

    public Integer getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(final Integer accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public Integer getAccountItemIdentifier() {
        return accountItemIdentifier;
    }

    public void setAccountItemIdentifier(final Integer accountItemIdentifier) {
        this.accountItemIdentifier = accountItemIdentifier;
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

    public Integer getPurapDocumentIdentifier() {
        return purapDocumentIdentifier;
    }

    public void setPurapDocumentIdentifier(final Integer purapDocumentIdentifier) {
        this.purapDocumentIdentifier = purapDocumentIdentifier;
    }

    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    public void setItemIdentifier(final Integer itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    public String getPreqDocumentNumber() {
        return preqDocumentNumber;
    }

    public void setPreqDocumentNumber(final String preqDocumentNumber) {
        this.preqDocumentNumber = preqDocumentNumber;
    }

    public Integer getPreqPurapDocumentIdentifier() {
        return preqPurapDocumentIdentifier;
    }

    public void setPreqPurapDocumentIdentifier(final Integer preqPurapDocumentIdentifier) {
        this.preqPurapDocumentIdentifier = preqPurapDocumentIdentifier;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(final String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getTaxClassificationCode() {
        return taxClassificationCode;
    }

    public void setTaxClassificationCode(final String taxClassificationCode) {
        this.taxClassificationCode = taxClassificationCode;
    }

    public Integer getPreqVendorHeaderGeneratedIdentifier() {
        return preqVendorHeaderGeneratedIdentifier;
    }

    public void setPreqVendorHeaderGeneratedIdentifier(final Integer preqVendorHeaderGeneratedIdentifier) {
        this.preqVendorHeaderGeneratedIdentifier = preqVendorHeaderGeneratedIdentifier;
    }

    public Integer getPreqVendorDetailAssignedIdentifier() {
        return preqVendorDetailAssignedIdentifier;
    }

    public void setPreqVendorDetailAssignedIdentifier(final Integer preqVendorDetailAssignedIdentifier) {
        this.preqVendorDetailAssignedIdentifier = preqVendorDetailAssignedIdentifier;
    }

    public String getPreqVendorName() {
        return preqVendorName;
    }

    public void setPreqVendorName(final String preqVendorName) {
        this.preqVendorName = preqVendorName;
    }

    public String getVendorLine1Address() {
        return vendorLine1Address;
    }

    public void setVendorLine1Address(final String vendorLine1Address) {
        this.vendorLine1Address = vendorLine1Address;
    }

    public String getVendorCountryCode() {
        return vendorCountryCode;
    }

    public void setVendorCountryCode(final String vendorCountryCode) {
        this.vendorCountryCode = vendorCountryCode;
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

    public Date getUniversityDate() {
        return universityDate;
    }

    public void setUniversityDate(final Date universityDate) {
        this.universityDate = universityDate;
    }

    public enum PrncSourceDataField implements TaxDtoFieldEnum {
        // Fields from AP_PMT_RQST_ACCT_T (PaymentRequestAccount)
        accountIdentifier(PaymentRequestAccount.class),
        accountItemIdentifier(PaymentRequestAccount.class, PurapPropertyConstants.ITEM_IDENTIFIER),
        amount(PaymentRequestAccount.class),
        chartOfAccountsCode(PaymentRequestAccount.class),
        accountNumber(PaymentRequestAccount.class),
        financialObjectCode(PaymentRequestAccount.class),
        // Fields from AP_PMT_RQST_ITM_T (PaymentRequestItem)
        purapDocumentIdentifier(PaymentRequestItem.class),
        itemIdentifier(PaymentRequestItem.class),
        // Fields from AP_PMT_RQST_T (PaymentRequestDocument)
        preqDocumentNumber(CuPaymentRequestDocument.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        preqPurapDocumentIdentifier(CuPaymentRequestDocument.class, PurapPropertyConstants.PURAP_DOC_ID),
        paymentMethodCode(CuPaymentRequestDocument.class),
        taxClassificationCode(CuPaymentRequestDocument.class),
        preqVendorHeaderGeneratedIdentifier(CuPaymentRequestDocument.class,
                VendorPropertyConstants.VENDOR_HEADER_GENERATED_ID),
        preqVendorDetailAssignedIdentifier(CuPaymentRequestDocument.class,
                VendorPropertyConstants.VENDOR_DETAIL_ASSIGNED_ID),
        preqVendorName(CuPaymentRequestDocument.class, VendorPropertyConstants.VENDOR_NAME),
        vendorLine1Address(CuPaymentRequestDocument.class),
        vendorCountryCode(CuPaymentRequestDocument.class),
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        vendorHeaderGeneratedIdentifier(VendorHeader.class),
        vendorTaxNumber(VendorHeader.class),
        vendorTypeCode(VendorHeader.class),
        vendorOwnershipCode(VendorHeader.class),
        vendorOwnershipCategoryCode(VendorHeader.class),
        vendorForeignIndicator(VendorHeader.class),
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        universityDate(UniversityDate.class);

        private final Class<? extends BusinessObject> boClass;
        private final String boFieldName;

        private PrncSourceDataField(final Class<? extends BusinessObject> boClass) {
            this(boClass, null);
        }

        private PrncSourceDataField(final Class<? extends BusinessObject> boClass, final String boFieldName) {
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
