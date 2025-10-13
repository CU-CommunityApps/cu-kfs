package edu.cornell.kfs.tax.batch.dto;

import java.sql.Date;
import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.PaymentAccountDetail;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.document.CuPaymentRequestDocument;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.tax.batch.annotation.HasNestedEnumWithDtoFieldListing;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;

@HasNestedEnumWithDtoFieldListing
public class PdpSourceData {
    // Fields from PDP_PROC_SUM_T (ProcessSummary)
    private KualiInteger summaryId;
    private KualiInteger beginDisbursementNbr;
    private KualiInteger endDisbursementNbr;
    private KualiInteger summaryCustomerId;
    private KualiInteger summaryProcessId;
    private Timestamp summaryLastUpdatedTimestamp;
    // Fields from PDP_CUST_PRFL_T (CustomerProfile)
    private KualiInteger customerId;
    private String customerCampusCode;
    private String unitCode;
    private String subUnitCode;
    private String achPaymentDescription;
    // Fields from PDP_PMT_GRP_T (PaymentGroup)
    private KualiInteger paymentGroupId;
    private KualiInteger paymentGroupProcessId;
    private String payeeName;
    private String payeeId;
    private String country;
    private Date disbursementDate;
    private KualiInteger disbursementNbr;
    private String payeeIdTypeCode;
    private String line1Address;
    private Boolean nonresidentPayment;
    private String paymentStatusCode;
    private String disbursementTypeCode;
    // Fields from PDP_PMT_DTL_T (PaymentDetail)
    private KualiInteger paymentDetailId;
    private String custPaymentDocNbr;
    private KualiInteger paymentDetailPaymentGroupId;
    private String financialDocumentTypeCode;
    // Fields from PDP_PMT_ACCT_DTL_T (PaymentAccountDetail)
    private KualiInteger accountDetailId;
    private KualiInteger accountDetailPaymentDetailId;
    private String accountDetailFinChartCode;
    private String accountNbr;
    private String finObjectCode;
    private String accountNetAmount;
    // Fields from PUR_VNDR_HDR_T (VendorHeader)
    private Integer vendorHeaderGeneratedIdentifier;
    private String vendorTaxNumber;
    private String vendorTypeCode;
    private String vendorOwnershipCode;
    private String vendorOwnershipCategoryCode;
    private String vendorForeignIndicator;
    // Fields from from AP_PMT_RQST_T (CuPaymentRequestDocument)
    private String preqDocumentNumber;
    private String taxClassificationCode;
    // Fields from FP_DV_DOC_T (CuDisbursementVoucherDocument)
    private String dvDocumentNumber;
    // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
    private String dvNraDocumentNumber;
    private String dvIncomeClassCode;

    public KualiInteger getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(KualiInteger summaryId) {
        this.summaryId = summaryId;
    }

    public KualiInteger getBeginDisbursementNbr() {
        return beginDisbursementNbr;
    }

    public void setBeginDisbursementNbr(KualiInteger beginDisbursementNbr) {
        this.beginDisbursementNbr = beginDisbursementNbr;
    }

    public KualiInteger getEndDisbursementNbr() {
        return endDisbursementNbr;
    }

    public void setEndDisbursementNbr(KualiInteger endDisbursementNbr) {
        this.endDisbursementNbr = endDisbursementNbr;
    }

    public KualiInteger getSummaryCustomerId() {
        return summaryCustomerId;
    }

    public void setSummaryCustomerId(KualiInteger summaryCustomerId) {
        this.summaryCustomerId = summaryCustomerId;
    }

    public KualiInteger getSummaryProcessId() {
        return summaryProcessId;
    }

    public void setSummaryProcessId(KualiInteger summaryProcessId) {
        this.summaryProcessId = summaryProcessId;
    }

    public Timestamp getSummaryLastUpdatedTimestamp() {
        return summaryLastUpdatedTimestamp;
    }

    public void setSummaryLastUpdatedTimestamp(Timestamp summaryLastUpdatedTimestamp) {
        this.summaryLastUpdatedTimestamp = summaryLastUpdatedTimestamp;
    }

    public KualiInteger getCustomerId() {
        return customerId;
    }

    public void setCustomerId(KualiInteger customerId) {
        this.customerId = customerId;
    }

    public String getCustomerCampusCode() {
        return customerCampusCode;
    }

    public void setCustomerCampusCode(String customerCampusCode) {
        this.customerCampusCode = customerCampusCode;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getSubUnitCode() {
        return subUnitCode;
    }

    public void setSubUnitCode(String subUnitCode) {
        this.subUnitCode = subUnitCode;
    }

    public String getAchPaymentDescription() {
        return achPaymentDescription;
    }

    public void setAchPaymentDescription(String achPaymentDescription) {
        this.achPaymentDescription = achPaymentDescription;
    }

    public KualiInteger getPaymentGroupId() {
        return paymentGroupId;
    }

    public void setPaymentGroupId(KualiInteger paymentGroupId) {
        this.paymentGroupId = paymentGroupId;
    }

    public KualiInteger getPaymentGroupProcessId() {
        return paymentGroupProcessId;
    }

    public void setPaymentGroupProcessId(KualiInteger paymentGroupProcessId) {
        this.paymentGroupProcessId = paymentGroupProcessId;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(Date disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public KualiInteger getDisbursementNbr() {
        return disbursementNbr;
    }

    public void setDisbursementNbr(KualiInteger disbursementNbr) {
        this.disbursementNbr = disbursementNbr;
    }

    public String getPayeeIdTypeCode() {
        return payeeIdTypeCode;
    }

    public void setPayeeIdTypeCode(String payeeIdTypeCode) {
        this.payeeIdTypeCode = payeeIdTypeCode;
    }

    public String getLine1Address() {
        return line1Address;
    }

    public void setLine1Address(String line1Address) {
        this.line1Address = line1Address;
    }

    public Boolean getNonresidentPayment() {
        return nonresidentPayment;
    }

    public void setNonresidentPayment(Boolean nonresidentPayment) {
        this.nonresidentPayment = nonresidentPayment;
    }

    public String getPaymentStatusCode() {
        return paymentStatusCode;
    }

    public void setPaymentStatusCode(String paymentStatusCode) {
        this.paymentStatusCode = paymentStatusCode;
    }

    public String getDisbursementTypeCode() {
        return disbursementTypeCode;
    }

    public void setDisbursementTypeCode(String disbursementTypeCode) {
        this.disbursementTypeCode = disbursementTypeCode;
    }

    public KualiInteger getPaymentDetailId() {
        return paymentDetailId;
    }

    public void setPaymentDetailId(KualiInteger paymentDetailId) {
        this.paymentDetailId = paymentDetailId;
    }

    public String getCustPaymentDocNbr() {
        return custPaymentDocNbr;
    }

    public void setCustPaymentDocNbr(String custPaymentDocNbr) {
        this.custPaymentDocNbr = custPaymentDocNbr;
    }

    public KualiInteger getPaymentDetailPaymentGroupId() {
        return paymentDetailPaymentGroupId;
    }

    public void setPaymentDetailPaymentGroupId(KualiInteger paymentDetailPaymentGroupId) {
        this.paymentDetailPaymentGroupId = paymentDetailPaymentGroupId;
    }

    public String getFinancialDocumentTypeCode() {
        return financialDocumentTypeCode;
    }

    public void setFinancialDocumentTypeCode(String financialDocumentTypeCode) {
        this.financialDocumentTypeCode = financialDocumentTypeCode;
    }

    public KualiInteger getAccountDetailId() {
        return accountDetailId;
    }

    public void setAccountDetailId(KualiInteger accountDetailId) {
        this.accountDetailId = accountDetailId;
    }

    public KualiInteger getAccountDetailPaymentDetailId() {
        return accountDetailPaymentDetailId;
    }

    public void setAccountDetailPaymentDetailId(KualiInteger accountDetailPaymentDetailId) {
        this.accountDetailPaymentDetailId = accountDetailPaymentDetailId;
    }

    public String getAccountDetailFinChartCode() {
        return accountDetailFinChartCode;
    }

    public void setAccountDetailFinChartCode(String accountDetailFinChartCode) {
        this.accountDetailFinChartCode = accountDetailFinChartCode;
    }

    public String getAccountNbr() {
        return accountNbr;
    }

    public void setAccountNbr(String accountNbr) {
        this.accountNbr = accountNbr;
    }

    public String getFinObjectCode() {
        return finObjectCode;
    }

    public void setFinObjectCode(String finObjectCode) {
        this.finObjectCode = finObjectCode;
    }

    public String getAccountNetAmount() {
        return accountNetAmount;
    }

    public void setAccountNetAmount(String accountNetAmount) {
        this.accountNetAmount = accountNetAmount;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public String getVendorTaxNumber() {
        return vendorTaxNumber;
    }

    public void setVendorTaxNumber(String vendorTaxNumber) {
        this.vendorTaxNumber = vendorTaxNumber;
    }

    public String getVendorTypeCode() {
        return vendorTypeCode;
    }

    public void setVendorTypeCode(String vendorTypeCode) {
        this.vendorTypeCode = vendorTypeCode;
    }

    public String getVendorOwnershipCode() {
        return vendorOwnershipCode;
    }

    public void setVendorOwnershipCode(String vendorOwnershipCode) {
        this.vendorOwnershipCode = vendorOwnershipCode;
    }

    public String getVendorOwnershipCategoryCode() {
        return vendorOwnershipCategoryCode;
    }

    public void setVendorOwnershipCategoryCode(String vendorOwnershipCategoryCode) {
        this.vendorOwnershipCategoryCode = vendorOwnershipCategoryCode;
    }

    public String getVendorForeignIndicator() {
        return vendorForeignIndicator;
    }

    public void setVendorForeignIndicator(String vendorForeignIndicator) {
        this.vendorForeignIndicator = vendorForeignIndicator;
    }

    public String getPreqDocumentNumber() {
        return preqDocumentNumber;
    }

    public void setPreqDocumentNumber(String preqDocumentNumber) {
        this.preqDocumentNumber = preqDocumentNumber;
    }

    public String getTaxClassificationCode() {
        return taxClassificationCode;
    }

    public void setTaxClassificationCode(String taxClassificationCode) {
        this.taxClassificationCode = taxClassificationCode;
    }

    public String getDvDocumentNumber() {
        return dvDocumentNumber;
    }

    public void setDvDocumentNumber(String dvDocumentNumber) {
        this.dvDocumentNumber = dvDocumentNumber;
    }

    public String getDvNraDocumentNumber() {
        return dvNraDocumentNumber;
    }

    public void setDvNraDocumentNumber(String dvNraDocumentNumber) {
        this.dvNraDocumentNumber = dvNraDocumentNumber;
    }

    public String getDvIncomeClassCode() {
        return dvIncomeClassCode;
    }

    public void setDvIncomeClassCode(String dvIncomeClassCode) {
        this.dvIncomeClassCode = dvIncomeClassCode;
    }

    public enum PdpSourceDataField implements TaxDtoFieldEnum {
        // Fields from PDP_PROC_SUM_T (ProcessSummary)
        summaryId(ProcessSummary.class, KFSPropertyConstants.ID),
        beginDisbursementNbr(ProcessSummary.class),
        endDisbursementNbr(ProcessSummary.class),
        summaryCustomerId(ProcessSummary.class, PdpPropertyConstants.CUSTOMER_ID),
        summaryProcessId(ProcessSummary.class, PdpPropertyConstants.ProcessSummary.PROCESS_SUMMARY_PROCESS_ID),
        summaryLastUpdatedTimestamp(ProcessSummary.class, KFSPropertyConstants.LAST_UPDATED_TIMESTAMP),
        // Fields from PDP_CUST_PRFL_T (CustomerProfile)
        customerId(CustomerProfile.class, KFSPropertyConstants.ID),
        customerCampusCode(CustomerProfile.class, KFSPropertyConstants.CAMPUS_CODE),
        unitCode(CustomerProfile.class),
        subUnitCode(CustomerProfile.class),
        achPaymentDescription(CustomerProfile.class),
        // Fields from PDP_PMT_GRP_T (PaymentGroup)
        paymentGroupId(PaymentGroup.class, KFSPropertyConstants.ID),
        paymentGroupProcessId(PaymentGroup.class, PdpPropertyConstants.PaymentGroup.PAYMENT_GROUP_PROCESS_ID),
        payeeName(PaymentGroup.class),
        payeeId(PaymentGroup.class),
        country(PaymentGroup.class),
        disbursementDate(PaymentGroup.class),
        disbursementNbr(PaymentGroup.class),
        payeeIdTypeCode(PaymentGroup.class),
        line1Address(PaymentGroup.class),
        nonresidentPayment(PaymentGroup.class),
        paymentStatusCode(PaymentGroup.class),
        disbursementTypeCode(PaymentGroup.class),
        // Fields from PDP_PMT_DTL_T (PaymentDetail)
        paymentDetailId(PaymentDetail.class, KFSPropertyConstants.ID),
        custPaymentDocNbr(PaymentDetail.class),
        paymentDetailPaymentGroupId(PaymentDetail.class,
                PdpPropertyConstants.PaymentDetail.PAYMENT_DETAIL_PAYMENT_GROUP_ID),
        financialDocumentTypeCode(PaymentDetail.class),
        // Fields from PDP_PMT_ACCT_DTL_T (PaymentAccountDetail)
        accountDetailId(PaymentAccountDetail.class, KFSPropertyConstants.ID),
        accountDetailPaymentDetailId(PaymentAccountDetail.class, CUPdpPropertyConstants.PAYMENT_DETAIL_ID),
        accountDetailFinChartCode(PaymentAccountDetail.class, PdpConstants.PaymentAccountDetail.CHART),
        accountNbr(PaymentAccountDetail.class),
        finObjectCode(PaymentAccountDetail.class),
        accountNetAmount(PaymentAccountDetail.class),
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        vendorHeaderGeneratedIdentifier(VendorHeader.class),
        vendorTaxNumber(VendorHeader.class),
        vendorTypeCode(VendorHeader.class),
        vendorOwnershipCode(VendorHeader.class),
        vendorOwnershipCategoryCode(VendorHeader.class),
        vendorForeignIndicator(VendorHeader.class),
        // Fields from from AP_PMT_RQST_T (CuPaymentRequestDocument)
        preqDocumentNumber(CuPaymentRequestDocument.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        taxClassificationCode(CuPaymentRequestDocument.class),
        // Fields from FP_DV_DOC_T (CuDisbursementVoucherDocument)
        dvDocumentNumber(CuDisbursementVoucherDocument.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
        dvNraDocumentNumber(DisbursementVoucherNonresidentTax.class, KFSPropertyConstants.DOCUMENT_NUMBER),
        dvIncomeClassCode(DisbursementVoucherNonresidentTax.class, KFSPropertyConstants.INCOME_CLASS_CODE);

        private final Class<? extends BusinessObject> boClass;
        private final String boFieldName;

        private PdpSourceDataField(final Class<? extends BusinessObject> boClass) {
            this(boClass, null);
        }

        private PdpSourceDataField(final Class<? extends BusinessObject> boClass, final String boFieldName) {
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
