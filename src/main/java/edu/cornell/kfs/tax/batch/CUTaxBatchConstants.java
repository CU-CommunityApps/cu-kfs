package edu.cornell.kfs.tax.batch;

import org.kuali.kfs.module.purap.PurapPropertyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * Helper class containing tax-batch-related constants.
 */
public final class CUTaxBatchConstants {

    public static final String CLASS_AND_PROPERTY_SEPARATOR = "::";

    public static final String FIN_OBJECT_CODE = "finObjectCode";
    public static final String PAYEE_ID = "payeeId";
    public static final String FEDERAL_INCOME_TAX_PERCENT = "federalIncomeTaxPercent";
    public static final String INCOME_CLASS_CODE = "incomeClassCode";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String INCOME_CODE = "incomeCode";
    public static final String VENDOR_EMAIL_ADDRESS = "vendorEmailAddress";

    /**
     * Helper enum identifying the various sources/types of the tax data/output fields
     * from a TaxDataDefinition or TaxOutputDefinition.
     * 
     * <p>Current allowable values:</p>
     * 
     * <ul>
     *   <li>BLANK :: An unconditionally-blank field.</li>
     *   <li>STATIC :: A field containing a static String value.</li>
     *   <li>RAW_DETAIL :: A field from raw transaction detail row loaded during first pass processing.</li>
     *   <li>DETAIL :: A field from transaction detail row that has been updated by the second pass processing.</li>
     *   <li>PDP :: A field from the PDP tax source data.</li>
     *   <li>DV :: A field from the DV tax source data.</li>
     *   <li>VENDOR :: A vendor object field.</li>
     *   <li>VENDOR_US_ADDRESS :: A vendor address field from a US-address-only query.</li>
     *   <li>VENDOR_ANY_ADDRESS :: A vendor address field from an any-vendor-address query.</li>
     *   <li>DOCUMENT_NOTE :: A document note object field.</li>
     *   <li>DERIVED :: A field whose value is derived by the tax processing and is not directly tied to the database.</li>
     * </ul>
     * 
     * NOTE: Due to complications in cases involving both US and foreign addresses,
     * vendor address fields for foreign-address-only queries should go under the DERIVED type.
     */
    public static enum TaxFieldSource {
        BLANK,
        STATIC,
        RAW_DETAIL,
        DETAIL,
        PDP,
        DV,
        PRNC,
        VENDOR,
        VENDOR_US_ADDRESS,
        VENDOR_ANY_ADDRESS,
        DOCUMENT_NOTE,
        DERIVED;
    }

    public enum Tax1099FilerAddressField {
        NAME1,
        NAME2,
        ADDRESS1,
        ADDRESS2,
        CITY,
        STATE,
        ZIP_CODE,
        PHONE_NUMBER;
    }

    public enum TaxOutputFieldType {
        STATIC,
        DERIVED;
    }

    /**
     * Helper subclass containing aliases for vendor fields used by the tax processing.
     */
    public static final class CommonVendorFieldNames {
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        public static final String VENDOR_HEADER_GENERATED_ID = "vendorHeaderGeneratedIdentifier";
        public static final String VENDOR_TAX_NUMBER = "vendorTaxNumber";
        public static final String VENDOR_TYPE_CODE = "vendorTypeCode";
        public static final String VENDOR_OWNERSHIP_CODE = "vendorOwnershipCode";
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = "vendorOwnershipCategoryCode";
        public static final String VENDOR_FOREIGN_IND = "vendorForeignIndicator";
        public static final String VENDOR_GIIN = "vendorGIIN";
        public static final String VENDOR_CHAPTER4_STATUS_CODE = "vendorChapter4StatusCode";
        // Fields from PUR_VNDR_DTL_T (VendorDetail)
        public static final String VENDOR_DETAIL_VENDOR_HEADER_GENERATED_ID = "vendorDetail.vendorHeaderGeneratedIdentifier";
        public static final String VENDOR_DETAIL_ASSIGNED_ID = "vendorDetailAssignedIdentifier";
        public static final String VENDOR_PARENT_IND = "vendorParentIndicator";
        public static final String VENDOR_FIRST_LAST_NAME_IND = "vendorFirstLastNameIndicator";
        public static final String VENDOR_NAME = "vendorName";
        
        private CommonVendorFieldNames() {
            throw new UnsupportedOperationException("do not call CommonVendorFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for PDP tax source fields used by the tax processing.
     */
    public static final class CommonPdpSourceFieldNames {
        // Fields from PDP_PROC_SUM_T (ProcessSummary)
        public static final String SUMMARY_ID = "summary.id";
        public static final String BEGIN_DISBURSEMENT_NUMBER = "beginDisbursementNbr";
        public static final String END_DISBURSEMENT_NUMBER = "endDisbursementNbr";
        public static final String SUMMARY_CUSTOMER_ID = "summary.customerId";
        public static final String SUMMARY_PROCESS_ID = "summary.processId";
        public static final String SUMMARY_LAST_UPDATED_TIMESTAMP = "summary.lastUpdatedTimestamp";
        // Fields from PDP_CUST_PRFL_T (CustomerProfile)
        public static final String CUSTOMER_ID = "customer.id";
        public static final String CUSTOMER_CAMPUS_CODE = "customer.campusCode";
        public static final String UNIT_CODE = "unitCode";
        public static final String SUB_UNIT_CODE = "subUnitCode";
        public static final String ACH_PAYMENT_DESCRIPTION = "achPaymentDescription";
        // Fields from PDP_PMT_GRP_T (PaymentGroup)
        public static final String PAYMENT_GROUP_ID = "paymentGroup.id";
        public static final String PAYMENT_GROUP_PROCESS_ID = "paymentGroup.processId";
        public static final String PAYEE_NAME = "payeeName";
        public static final String PAYEE_ID = CUTaxBatchConstants.PAYEE_ID;
        public static final String COUNTRY = "country";
        public static final String DISBURSEMENT_DATE = "disbursementDate";
        public static final String DISBURSEMENT_NUMBER = "disbursementNbr";
        public static final String PAYEE_ID_TYPE_CODE = "payeeIdTypeCode";
        public static final String LINE1_ADDRESS = "line1Address";
        public static final String NRA_PAYMENT_IND = "nraPayment";
        public static final String PAYMENT_STATUS_CODE = "paymentStatusCode";
        public static final String DISBURSEMENT_TYPE_CODE = "disbursementTypeCode";
        // Fields from PDP_PMT_DTL_T (PaymentDetail)
        public static final String PAYMENT_DETAIL_ID = "paymentDetail.id";
        public static final String CUSTOMER_PAYMENT_DOC_NUMBER = "custPaymentDocNbr";
        public static final String PAYMENT_DETAIL_PAYMENT_GROUP_ID = "paymentDetail.paymentGroupId";
        public static final String FINANCIAL_DOCUMENT_TYPE_CODE = "financialDocumentTypeCode";
        // Fields from PDP_PMT_ACCT_DTL_T (PaymentAccountDetail)
        public static final String ACCOUNT_DETAIL_ID = "accountDetail.id";
        public static final String ACCOUNT_DETAIL_PAYMENT_DETAIL_ID = "accountDetail.paymentDetailId";
        public static final String ACCOUNT_DETAIL_CHART_CODE = "accountDetail.finChartCode";
        public static final String ACCOUNT_NUMBER = "accountNbr";
        public static final String FIN_OBJECT_CODE = CUTaxBatchConstants.FIN_OBJECT_CODE;
        public static final String ACCOUNT_NET_AMOUNT = "accountNetAmount";
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        public static final String VENDOR_HEADER_GENERATED_ID = CommonVendorFieldNames.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_TAX_NUMBER = CommonVendorFieldNames.VENDOR_TAX_NUMBER;
        public static final String VENDOR_TYPE_CODE = CommonVendorFieldNames.VENDOR_TYPE_CODE;
        public static final String VENDOR_OWNERSHIP_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE;
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE;
        public static final String VENDOR_FOREIGN_IND = CommonVendorFieldNames.VENDOR_FOREIGN_IND;
        //fields from AP_PMT_RQST_T (PaymentRequestDocument)
        public static final String TAX_CLASSIFICATION_CODE = PurapPropertyConstants.TAX_CLASSIFICATION_CODE;
        
        
        
        private CommonPdpSourceFieldNames() {
            throw new UnsupportedOperationException("do not call CommonPdpSourceFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for DV tax source fields used by the tax processing.
     */
    public static final class CommonDvSourceFieldNames {
        // Fields from FP_DV_PAYEE_DTL_T (DisbursementVoucherPayeeDetail)
        public static final String PAYEE_DETAIL_DOCUMENT_NUMBER = "payeeDetail.documentNumber";
        public static final String DV_PAYMENT_REASON_CODE = "disbVchrPaymentReasonCode";
        public static final String DV_NONRESIDENT_PAYMENT_CODE = "disbVchrNonresidentPaymentCode";
        public static final String DV_PAYEE_ID_NUMBER = "disbVchrPayeeIdNumber";
        public static final String DV_PAYEE_PERSON_NAME = "disbVchrPayeePersonName";
        public static final String DV_PAYEE_LINE1_ADDRESS = "disbVchrPayeeLine1Addr";
        public static final String DV_PAYEE_COUNTRY_CODE = "disbVchrPayeeCountryCode";
        public static final String DV_PAYEE_TYPE_CODE = "disbursementVoucherPayeeTypeCode";
        // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
        public static final String NRA_DOCUMENT_NUMBER = "nra.documentNumber";
        public static final String FEDERAL_INCOME_TAX_PERCENT = CUTaxBatchConstants.FEDERAL_INCOME_TAX_PERCENT;
        public static final String INCOME_CLASS_CODE = CUTaxBatchConstants.INCOME_CLASS_CODE;
        public static final String INCOME_TAX_TREATY_EXEMPT_CODE = "incomeTaxTreatyExemptCode";
        public static final String FOREIGN_SOURCE_INCOME_CODE = "foreignSourceIncomeCode";
        // Fields from FP_ACCT_LINES_T (Accounting lines table)
        public static final String ACCOUNTING_LINE_DOCUMENT_NUMBER = "accountingLine.documentNumber";
        public static final String ACCOUNTING_LINE_SEQUENCE_NUMBER = "accountingLine.sequenceNumber";
        public static final String FDOC_LINE_TYPE_CODE = "financialDocumentLineTypeCode";
        public static final String AMOUNT = "amount";
        public static final String CHART_OF_ACCOUNTS_CODE = "chartOfAccountsCode";
        public static final String ACCOUNT_NUMBER = CUTaxBatchConstants.ACCOUNT_NUMBER;
        public static final String FIN_OBJECT_CODE = "financialObjectCode";
        public static final String FDOC_LINE_DESCRIPTION = "financialDocumentLineDescription";
        public static final String DEBIT_CREDIT_CODE = "debitCreditCode";
        // Fields from FP_DV_DOC_T (DisbursementVoucherDocument)
        public static final String DV_DOCUMENT_NUMBER = "dv.documentNumber";
        public static final String DV_CHECK_STUB_TEXT = "disbVchrCheckStubText";
        public static final String DOCUMENT_DV_PAYMENT_METHOD_CODE = "document.disbVchrPaymentMethodCode";
        public static final String EXTRACT_DATE = "extractDate";
        public static final String PAID_DATE = "paidDate";
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        public static final String VENDOR_HEADER_GENERATED_ID = CommonVendorFieldNames.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_TAX_NUMBER = CommonVendorFieldNames.VENDOR_TAX_NUMBER;
        public static final String VENDOR_TYPE_CODE = CommonVendorFieldNames.VENDOR_TYPE_CODE;
        public static final String VENDOR_OWNERSHIP_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE;
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE;
        public static final String VENDOR_FOREIGN_IND = CommonVendorFieldNames.VENDOR_FOREIGN_IND;
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        public static final String UNIVERSITY_DATE = "universityDate";
        // Fields from PDP_PMT_GRP_T (PaymentGroup)
        public static final String DISBURSEMENT_NUMBER = "disbursementNbr";
        public static final String PAYMENT_STATUS_CODE = "paymentStatusCode";
        public static final String DISBURSEMENT_TYPE_CODE = "disbursementTypeCode";
        
        
        
        private CommonDvSourceFieldNames() {
            throw new UnsupportedOperationException("do not call CommonDvSourceFieldNames constructor");
        }
    }

    /**
     * Helper subclass containing aliases for PRNC tax source fields used by the tax processing.
     */
    public static final class CommonPRNCSourceFieldNames {

        // Fields from AP_PMT_RQST_ACCT_T (Payment Request Accounting lines table)
        public static final String ACCOUNTING_LINE_IDENTIFIER = "accountIdentifier";
        public static final String ACCOUNTING_LINE_ITEM_IDENTIFIER = "accountItemIdentifier";
        public static final String AMOUNT = "amount";
        public static final String CHART_OF_ACCOUNTS_CODE = "chartOfAccountsCode";
        public static final String ACCOUNT_NUMBER = CUTaxBatchConstants.ACCOUNT_NUMBER;
        public static final String FIN_OBJECT_CODE = "financialObjectCode";
        
        // Fields from AP_PMT_RQST_ITM_T (Payment Request Item table)
        public static final String PURAP_DOC_IDENTIFIER = "purapDocumentIdentifier";
        public static final String ITEM_IDENTIFIER = "itemIdentifier";

        // Fields from AP_PMT_RQST_T (PaymentRequestDocument)
        public static final String PREQ_DOCUMENT_NUMBER = "preq.documentNumber";
        public static final String PREQ_PURAP_DOC_IDENTIFIER = "preqPurapDocumentIdentifier";
        public static final String DOCUMENT_PREQ_PAYMENT_METHOD_CODE = "document.paymentMethodCode";
        public static final String DOCUMENT_PREQ_TAX_CLASSIFICATION_CODE = "document.taxClassificationCode";
        public static final String PREQ_VENDOR_HEADER_GENERATED_ID = "document.vendorHeaderGeneratedIdentifier";
        public static final String PREQ_VENDOR_DETAIL_ASSIGNED_ID = "document.vendorDetailAssignedIdentifier";
        public static final String PREQ_VENDOR_NAME = "preqVendorName";
        public static final String PREQ_VENDOR_LINE1_ADDRESS = "vendorLine1Address";
        public static final String PREQ_VENDOR_COUNTRY_CODE = "vendorCountryCode";

        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        public static final String VENDOR_HEADER_GENERATED_ID = CommonVendorFieldNames.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_TAX_NUMBER = CommonVendorFieldNames.VENDOR_TAX_NUMBER;
        public static final String VENDOR_TYPE_CODE = CommonVendorFieldNames.VENDOR_TYPE_CODE;
        public static final String VENDOR_OWNERSHIP_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE;
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE;
        public static final String VENDOR_FOREIGN_IND = CommonVendorFieldNames.VENDOR_FOREIGN_IND;
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        public static final String UNIVERSITY_DATE = "universityDate";    
        
        private CommonPRNCSourceFieldNames() {
            throw new UnsupportedOperationException("do not call CommonPRNCSourceFieldNames constructor");
        }
    }


    /**
     * Helper subclass containing aliases for vendor address fields used by the tax processing.
     */
    public static final class CommonVendorAddressFieldNames {
        // Fields from PUR_VNDR_ADDR_T (VendorAddress)
        public static final String VENDOR_ADDRESS_GENERATED_ID = "vendorAddressGeneratedIdentifier";
        public static final String VENDOR_HEADER_GENERATED_ID = CommonVendorFieldNames.VENDOR_HEADER_GENERATED_ID;
        public static final String VENDOR_DETAIL_ASSIGNED_ID = CommonVendorFieldNames.VENDOR_DETAIL_ASSIGNED_ID;
        public static final String VENDOR_ADDRESS_TYPE_CODE = "vendorAddressTypeCode";
        public static final String VENDOR_LINE1_ADDRESS = "vendorLine1Address";
        public static final String VENDOR_LINE2_ADDRESS = "vendorLine2Address";
        public static final String VENDOR_CITY_NAME = "vendorCityName";
        public static final String VENDOR_STATE_CODE = "vendorStateCode";
        public static final String VENDOR_ZIP_CODE = "vendorZipCode";
        public static final String VENDOR_COUNTRY_CODE = "vendorCountryCode";
        public static final String VENDOR_ATTENTION_NAME = "vendorAttentionName";
        public static final String VENDOR_ADDRESS_INTERNATIONAL_PROVINCE_NAME = "vendorAddressInternationalProvinceName";
        public static final String VENDOR_ADDRESS_EMAIL_ADDRESS = "vendorAddressEmailAddress";
        public static final String ACTIVE = KFSPropertyConstants.ACTIVE;
        
        
        
        private CommonVendorAddressFieldNames() {
            throw new UnsupportedOperationException("do not call CommonVendorAddressFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for document note object fields used by the tax processing.
     */
    public static final class CommonDocumentNoteFieldNames {
        // Fields from KRNS_NTE_T (Note)
        public static final String NOTE_ID = "noteIdentifier";
        public static final String REMOTE_OBJECT_ID = "remoteObjectIdentifier";
        public static final String NOTE_TEXT = "noteText";
        
        
        
        private CommonDocumentNoteFieldNames() {
            throw new UnsupportedOperationException("do not call CommonDocumentNoteFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for raw transaction detail fields used by the tax first pass processing.
     */
    public static final class RawTransactionDetailFieldNames {
        // Fields from TX_RAW_TRANSACTION_DETAIL_T (RawTransactionDetail)
        public static final String TRANSACTION_DETAIL_ID = "transactionDetailId";
        public static final String REPORT_YEAR = "reportYear";
        public static final String DOCUMENT_NUMBER = "documentNumber";
        public static final String DOCUMENT_TYPE = "documentType";
        public static final String FINANCIAL_DOCUMENT_LINE_NUMBER = "financialDocumentLineNumber";
        public static final String FIN_OBJECT_CODE = CUTaxBatchConstants.FIN_OBJECT_CODE;
        public static final String NET_PAYMENT_AMOUNT = "netPaymentAmount";
        public static final String DOCUMENT_TITLE = "documentTitle";
        public static final String VENDOR_TAX_NUMBER = CommonVendorFieldNames.VENDOR_TAX_NUMBER;
        public static final String INCOME_CODE = CUTaxBatchConstants.INCOME_CODE;
        public static final String INCOME_CODE_SUB_TYPE = "incomeCodeSubType";
        public static final String DV_CHECK_STUB_TEXT = "dvCheckStubText";
        public static final String PAYEE_ID = CUTaxBatchConstants.PAYEE_ID;
        public static final String VENDOR_NAME = CommonVendorFieldNames.VENDOR_NAME;
        public static final String PARENT_VENDOR_NAME = "parentVendorName";
        public static final String VENDOR_TYPE_CODE = CommonVendorFieldNames.VENDOR_TYPE_CODE;
        public static final String VENDOR_OWNERSHIP_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE;
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE;
        public static final String VENDOR_FOREIGN_INDICATOR = CommonVendorFieldNames.VENDOR_FOREIGN_IND;
        public static final String VENDOR_EMAIL_ADDRESS = CUTaxBatchConstants.VENDOR_EMAIL_ADDRESS;
        public static final String VENDOR_CHAPTER4_STATUS_CODE = CommonVendorFieldNames.VENDOR_CHAPTER4_STATUS_CODE;
        public static final String VENDOR_GIIN = CommonVendorFieldNames.VENDOR_GIIN;
        public static final String VENDOR_LINE1_ADDRESS = CommonVendorAddressFieldNames.VENDOR_LINE1_ADDRESS;
        public static final String VENDOR_LINE2_ADDRESS = CommonVendorAddressFieldNames.VENDOR_LINE2_ADDRESS;
        public static final String VENDOR_CITY_NAME = CommonVendorAddressFieldNames.VENDOR_CITY_NAME;
        public static final String VENDOR_STATE_CODE = CommonVendorAddressFieldNames.VENDOR_STATE_CODE;
        public static final String VENDOR_ZIP_CODE = CommonVendorAddressFieldNames.VENDOR_ZIP_CODE;
        public static final String VENDOR_FOREIGN_LINE1_ADDRESS = "vendorForeignLine1Address";
        public static final String VENDOR_FOREIGN_LINE2_ADDRESS = "vendorForeignLine2Address";
        public static final String VENDOR_FOREIGN_CITY_NAME = "vendorForeignCityName";
        public static final String VENDOR_FOREIGN_ZIP_CODE = "vendorForeignZipCode";
        public static final String VENDOR_FOREIGN_PROVINCE_NAME = "vendorForeignProvinceName";
        public static final String VENDOR_FOREIGN_COUNTRY_CODE = "vendorForeignCountryCode";
        public static final String NRA_PAYMENT_INDICATOR = "nraPaymentIndicator";
        public static final String PAYMENT_DATE = "paymentDate";
        public static final String PAYMENT_PAYEE_NAME = "paymentPayeeName";
        public static final String INCOME_CLASS_CODE = CUTaxBatchConstants.INCOME_CLASS_CODE;
        public static final String INCOME_TAX_TREATY_EXEMPT_INDICATOR = "incomeTaxTreatyExemptIndicator";
        public static final String FOREIGN_SOURCE_INCOME_INDICATOR = "foreignSourceIncomeIndicator";
        public static final String FEDERAL_INCOME_TAX_PERCENT = CUTaxBatchConstants.FEDERAL_INCOME_TAX_PERCENT;
        public static final String PAYMENT_DESCRIPTION = "paymentDescription";
        public static final String PAYMENT_LINE1_ADDRESS = "paymentLine1Address";
        public static final String PAYMENT_COUNTRY_NAME = "paymentCountryName";
        public static final String CHART_CODE = "chartCode";
        public static final String ACCOUNT_NUMBER = CUTaxBatchConstants.ACCOUNT_NUMBER;
        public static final String INITIATOR_NETID = "initiatorNetId";
        public static final String FORM_1099_TYPE = "form1099Type";
        public static final String FORM_1099_BOX = "form1099Box";
        public static final String FORM_1099_OVERRIDDEN_TYPE = "form1099OverriddenType";
        public static final String FORM_1099_OVERRIDDEN_BOX = "form1099OverriddenBox";
        public static final String FORM_1042S_BOX = "form1042SBox";
        public static final String FORM_1042S_OVERRIDDEN_BOX = "form1042SOverriddenBox";
        public static final String PAYMENT_REASON_CODE = "paymentReasonCode";
        
        public static final String DISBURSEMENT_NUMBER = "disbursementNbr";
        public static final String PAYMENT_STATUS_CODE = "paymentStatusCode";
        public static final String DISBURSEMENT_TYPE_CODE = "disbursementTypeCode";
        public static final String LEDGER_DOCUMENT_TYPE_CODE = "ledgerDocumentTypeCode";

        private RawTransactionDetailFieldNames() {
            throw new UnsupportedOperationException("do not call RawTransactionDetailFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for transaction detail fields used by the tax processing.
     */
    public static final class TransactionDetailFieldNames {
        // Fields from TX_TRANSACTION_DETAIL_T (TransactionDetail)
        public static final String TRANSACTION_DETAIL_ID = "transactionDetailId";
        public static final String REPORT_YEAR = "reportYear";
        public static final String DOCUMENT_NUMBER = "documentNumber";
        public static final String DOCUMENT_TYPE = "documentType";
        public static final String FINANCIAL_DOCUMENT_LINE_NUMBER = "financialDocumentLineNumber";
        public static final String FIN_OBJECT_CODE = CUTaxBatchConstants.FIN_OBJECT_CODE;
        public static final String NET_PAYMENT_AMOUNT = "netPaymentAmount";
        public static final String DOCUMENT_TITLE = "documentTitle";
        public static final String VENDOR_TAX_NUMBER = CommonVendorFieldNames.VENDOR_TAX_NUMBER;
        public static final String INCOME_CODE = CUTaxBatchConstants.INCOME_CODE;
        public static final String INCOME_CODE_SUB_TYPE = "incomeCodeSubType";
        public static final String DV_CHECK_STUB_TEXT = "dvCheckStubText";
        public static final String PAYEE_ID = CUTaxBatchConstants.PAYEE_ID;
        public static final String VENDOR_NAME = CommonVendorFieldNames.VENDOR_NAME;
        public static final String PARENT_VENDOR_NAME = "parentVendorName";
        public static final String VENDOR_TYPE_CODE = CommonVendorFieldNames.VENDOR_TYPE_CODE;
        public static final String VENDOR_OWNERSHIP_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE;
        public static final String VENDOR_OWNERSHIP_CATEGORY_CODE = CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE;
        public static final String VENDOR_FOREIGN_INDICATOR = CommonVendorFieldNames.VENDOR_FOREIGN_IND;
        public static final String VENDOR_EMAIL_ADDRESS = CUTaxBatchConstants.VENDOR_EMAIL_ADDRESS;
        public static final String VENDOR_CHAPTER4_STATUS_CODE = CommonVendorFieldNames.VENDOR_CHAPTER4_STATUS_CODE;
        public static final String VENDOR_GIIN = CommonVendorFieldNames.VENDOR_GIIN;
        public static final String VENDOR_LINE1_ADDRESS = CommonVendorAddressFieldNames.VENDOR_LINE1_ADDRESS;
        public static final String VENDOR_LINE2_ADDRESS = CommonVendorAddressFieldNames.VENDOR_LINE2_ADDRESS;
        public static final String VENDOR_CITY_NAME = CommonVendorAddressFieldNames.VENDOR_CITY_NAME;
        public static final String VENDOR_STATE_CODE = CommonVendorAddressFieldNames.VENDOR_STATE_CODE;
        public static final String VENDOR_ZIP_CODE = CommonVendorAddressFieldNames.VENDOR_ZIP_CODE;
        public static final String VENDOR_FOREIGN_LINE1_ADDRESS = "vendorForeignLine1Address";
        public static final String VENDOR_FOREIGN_LINE2_ADDRESS = "vendorForeignLine2Address";
        public static final String VENDOR_FOREIGN_CITY_NAME = "vendorForeignCityName";
        public static final String VENDOR_FOREIGN_ZIP_CODE = "vendorForeignZipCode";
        public static final String VENDOR_FOREIGN_PROVINCE_NAME = "vendorForeignProvinceName";
        public static final String VENDOR_FOREIGN_COUNTRY_CODE = "vendorForeignCountryCode";
        public static final String NRA_PAYMENT_INDICATOR = "nraPaymentIndicator";
        public static final String PAYMENT_DATE = "paymentDate";
        public static final String PAYMENT_PAYEE_NAME = "paymentPayeeName";
        public static final String INCOME_CLASS_CODE = CUTaxBatchConstants.INCOME_CLASS_CODE;
        public static final String INCOME_TAX_TREATY_EXEMPT_INDICATOR = "incomeTaxTreatyExemptIndicator";
        public static final String FOREIGN_SOURCE_INCOME_INDICATOR = "foreignSourceIncomeIndicator";
        public static final String FEDERAL_INCOME_TAX_PERCENT = CUTaxBatchConstants.FEDERAL_INCOME_TAX_PERCENT;
        public static final String PAYMENT_DESCRIPTION = "paymentDescription";
        public static final String PAYMENT_LINE1_ADDRESS = "paymentLine1Address";
        public static final String PAYMENT_COUNTRY_NAME = "paymentCountryName";
        public static final String CHART_CODE = "chartCode";
        public static final String ACCOUNT_NUMBER = CUTaxBatchConstants.ACCOUNT_NUMBER;
        public static final String INITIATOR_NETID = "initiatorNetId";
        public static final String FORM_1099_TYPE = "form1099Type";
        public static final String FORM_1099_BOX = "form1099Box";
        public static final String FORM_1099_OVERRIDDEN_TYPE = "form1099OverriddenType";
        public static final String FORM_1099_OVERRIDDEN_BOX = "form1099OverriddenBox";
        public static final String FORM_1042S_BOX = "form1042SBox";
        public static final String FORM_1042S_OVERRIDDEN_BOX = "form1042SOverriddenBox";
        public static final String PAYMENT_REASON_CODE = "paymentReasonCode";
        
        public static final String DISBURSEMENT_NUMBER = "disbursementNbr";
        public static final String PAYMENT_STATUS_CODE = "paymentStatusCode";
        public static final String DISBURSEMENT_TYPE_CODE = "disbursementTypeCode";
        public static final String LEDGER_DOCUMENT_TYPE_CODE = "ledgerDocumentTypeCode";

        private TransactionDetailFieldNames() {
            throw new UnsupportedOperationException("do not call TransactionDetailFieldNames constructor");
        }
    }



    /**
     * Helper subclass containing aliases for derived-value fields used by the tax processing.
     */
    public static final class DerivedFieldNames {
        // These fields are derived from other object fields or from parameters. A few refer to DB-backed fields that have been modified in some way.
        public static final String VENDOR_LAST_NAME = "vendorLastName";
        public static final String VENDOR_FIRST_NAME = "vendorFirstName";
        public static final String VENDOR_EMAIL_ADDRESS = CUTaxBatchConstants.VENDOR_EMAIL_ADDRESS;
        public static final String VENDOR_US_ADDRESS_LINE_1 = "vendorUSAddressLine1";
        public static final String VENDOR_FOREIGN_ADDRESS_LINE_1 = "vendorForeignAddressLine1";
        public static final String VENDOR_FOREIGN_ADDRESS_LINE_2 = "vendorForeignAddressLine2";
        public static final String VENDOR_FOREIGN_CITY_NAME = "vendorForeignCityName";
        public static final String VENDOR_FOREIGN_ZIP_CODE = "vendorForeignZipCode";
        public static final String VENDOR_FOREIGN_PROVINCE_NAME = "vendorForeignProvinceName";
        public static final String VENDOR_FOREIGN_COUNTRY_CODE = "vendorForeignCountryCode";
        public static final String VENDOR_FOREIGN_COUNTRY_NAME = "vendorForeignCountryName";
        public static final String VENDOR_FOREIGN_COUNTRY_INDICATOR = "vendorForeignCountryIndicator";
        public static final String VENDOR_ANY_ADDRESS_LINE_1 = "vendorAnyAddressLine1";
        public static final String VENDOR_ZIP_CODE_NUM_ONLY = "vendorZipCodeNumOnly";
        public static final String SSN = "ssn";
        public static final String ITIN = "itin";
        public static final String CHAPTER3_STATUS_CODE = "chapter3StatusCode";
        public static final String CHAPTER3_EXEMPTION_CODE = "chapter3ExemptionCode";
        public static final String CHAPTER4_EXEMPTION_CODE = "chapter4ExemptionCode";
        public static final String INCOME_CODE = CUTaxBatchConstants.INCOME_CODE;
        public static final String EIN = "ein";
        public static final String CHAPTER3_TAX_RATE = "chapter3TaxRate";
        public static final String GROSS_AMOUNT = "grossAmount";
        public static final String FTW_AMOUNT = "fedTaxWithheldAmount";
        public static final String SITW_AMOUNT = "stateIncomeTaxWithheldAmount";
        public static final String STATE_CODE = "stateCode";
        public static final String DV_CHECK_STUB_WITH_UPDATED_WHITESPACE = "dvCheckStubTextWithUpdatedWhitespace";
        public static final String TAB_SITE_ID = "tabSiteId";
        public static final String MISC_RENTS = "miscRents";
        public static final String MISC_ROYALTIES = "miscRoyalties";
        public static final String MISC_OTHER_INCOME = "miscOtherIncome";
        public static final String MISC_FED_INCOME_TAX_WITHHELD = "miscFedIncomeTaxWithheld";
        public static final String MISC_FISHING_BOAT_PROCEEDS = "miscFishingBoatProceeds";
        public static final String MISC_MEDICAL_HEALTHCARE_PAYMENTS = "miscMedicalHealthcarePayments";
        public static final String MISC_DIRECT_SALES_IND = "miscDirectSalesInd";
        public static final String MISC_SUBSTITUTE_PAYMENTS = "miscSubstitutePayments";
        public static final String MISC_CROP_INSURANCE_PROCEEDS = "miscCropInsuranceProceeds";
        public static final String MISC_GROSS_PROCEEDS_ATTORNEY = "miscGrossProceedsAttorney";
        public static final String MISC_SECTION_409A_DEFERRAL = "miscSection409ADeferral";
        public static final String MISC_GOLDEN_PARACHUTE = "miscGoldenParachute";
        public static final String MISC_NONQUALIFIED_DEFERRED_COMPENSATION = "miscNonqualifiedDeferredCompensation";
        public static final String MISC_STATE_TAX_WITHHELD = "miscStateTaxWithheld";
        public static final String MISC_PAYER_STATE_NUMBER = "miscPayerStateNumber";
        public static final String MISC_STATE_INCOME = "miscStateIncome";
        public static final String NEC_NONEMPLOYEE_COMPENSATION = "necNonemployeeCompensation";
        public static final String NEC_FED_INCOME_TAX_WITHHELD = "necFedIncomeTaxWithheld";
        public static final String NEC_STATE_TAX_WITHHELD = "necStateTaxWithheld";
        public static final String NEC_PAYER_STATE_NUMBER = "necPayerStateNumber";
        public static final String NEC_STATE_INCOME = "necStateIncome";
        public static final String BOX_UNKNOWN_1099 = "box???";
        public static final String BOX_UNKNOWN_1042S = "box????";
        public static final String END_DATE = "endDate";
        public static final String TAX_YEAR = "taxYear";
        public static final String FILER_NAME_1 = "filerName1";
        public static final String FILER_NAME_2 = "filerName2";
        public static final String FILER_ADDRESS_1 = "filerAddress1";
        public static final String FILER_ADDRESS_2 = "filerAddress2";
        public static final String FILER_CITY = "filerCity";
        public static final String FILER_STATE = "filerState";
        public static final String FILER_ZIP_CODE = "filerZipCode";
        public static final String FILER_PHONE_NUMBER = "filerPhoneNumber";
        
        private DerivedFieldNames() {
            throw new UnsupportedOperationException("do not call DerivedFieldNames constructor");
        }
    }



    public static final class QueryTableAliases {
        public static final String VENDOR_HEADER = "HDR";
        public static final String VENDOR_DETAIL = "DTL";
    }



    private CUTaxBatchConstants() {
        throw new UnsupportedOperationException("do not call");
    }

}
