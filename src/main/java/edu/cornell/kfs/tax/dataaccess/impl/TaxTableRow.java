package edu.cornell.kfs.tax.dataaccess.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonDocumentNoteFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonDvSourceFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonPRNCSourceFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonPdpSourceFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonVendorAddressFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.CommonVendorFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.DerivedFieldNames;
import edu.cornell.kfs.tax.batch.CUTaxBatchConstants.TransactionDetailFieldNames;

/**
 * Helper class containing metadata on how a section of tax data maps to the database,
 * or on how a section of tax data relates to some non-DB-backed properties. This base
 * class is immutable, and its subclasses are expected to be immutable as well.
 * 
 * <p>For proper compatibility with the TaxTableMetadataService, each implementation
 * class's constructor *must* have the following arguments:</p>
 * 
 * <ol>
 *   <li>The row's unique String ID; typically refers to a CUTaxBatchConstants.TaxFieldSource enum constant name.</li>
 *   <li>A String-to-TaxTableField Map containing all the TaxTableFields keyed according to property name.</li>
 *   <li>A String List containing the names of the tables corresponding to the configured TaxDataRow object classes.</li>
 *   <li>A String-to-TaxTableField Map containing the TaxTableFields keyed according to property alias.</li>
 *   <li>An Integer representing the number of fields at the beginning of the ordered fields list that do not need parameter placeholders for INSERTs.</li>
 * </ol>
 * 
 * <p>NOTE: It is assumed that the keyed-by-propName Map's iterator will return the fields
 * in the order that they should be defined in INSERT or SELECT queries.</p>
 * 
 * <p>The subclasses generally contain helper constants for conveniently accessing commonly-used fields.</p>
 */
abstract class TaxTableRow {

    // The row's unique ID.
    final String rowId;
    // The table fields, ordered according to how they should appear in INSERT or SELECT queries.
    final List<TaxTableField> orderedFields;
    // The fields, keyed according to TaxDataField property name.
    final Map<String,TaxTableField> fields;
    // The fields with aliases, keyed according to alias.
    final Map<String,TaxTableField> aliasedFields;
    // The tables to use for queries.
    final List<String> tables;
    // The index in the orderedFields list at which to start using parameter placeholders when generating INSERT queries.
    final int insertOffset;

    /**
     * Constructs a new TaxTableRow instance. Subclasses are expected to contain constructors
     * of their own that have this exact same argument list.
     * 
     * @param rowId The row's unique String ID; typically refers to a CUTaxBatchConstants.TaxFieldSource enum constant name.
     * @param fields A String-to-TaxTableField Map containing all the TaxTableFields keyed according to property name.
     * @param tables A String List containing the names of the tables corresponding to the configured TaxDataRow object classes.
     * @param aliasedFields A String-to-TaxTableField Map containing the TaxTableFields keyed according to property alias.
     * @param insertOffset The number of fields at the beginning of the ordered fields list that do not need parameter placeholders for INSERTs.
     */
    TaxTableRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
        if (StringUtils.isBlank(rowId)) {
            throw new IllegalArgumentException("rowId cannot be blank");
        } else if (fields == null) {
            throw new IllegalArgumentException("fields cannot be null");
        } else if (tables == null) {
            throw new IllegalArgumentException("tables cannot be null");
        } else if (aliasedFields == null) {
            throw new IllegalArgumentException("aliasedFields cannot be null");
        } else if (insertOffset == null || insertOffset.intValue() < 0) {
            throw new IllegalArgumentException("insertOffset cannot be null or negative");
        }
        this.rowId = rowId;
        this.orderedFields = Collections.unmodifiableList(new ArrayList<TaxTableField>(fields.values()));
        this.fields = Collections.unmodifiableMap(new HashMap<String,TaxTableField>(fields));
        this.aliasedFields = Collections.unmodifiableMap(new HashMap<String,TaxTableField>(aliasedFields));
        this.tables = Collections.unmodifiableList(new ArrayList<String>(tables));
        this.insertOffset = insertOffset.intValue();
    }

    /**
     * Convenience method for retrieving a field by propertyName or alias
     * (with the latter taking precedence).
     * 
     * @param keyOrAlias The propertyName key or alias.
     * @return The TaxTableField with the given key or alias.
     * @throws IllegalStateException if no such field exists.
     */
    final TaxTableField getField(String keyOrAlias) {
        TaxTableField field = aliasedFields.get(keyOrAlias);
        if (field == null) {
            field = fields.get(keyOrAlias);
            if (field == null) {
                throw new IllegalStateException("Could not find field with key or alias: " + keyOrAlias);
            }
        }
        return field;
    }

    /**
     * Convenience method for retrieving a field by alias.
     * 
     * @param alias The field's alias.
     * @return The TaxTableField with the given alias.
     * @throws IllegalStateException if no such field exists.
     */
    final TaxTableField getAliasedField(String alias) {
        TaxTableField field = aliasedFields.get(alias);
        if (field == null) {
            throw new IllegalStateException("Could not find field with alias: " + alias);
        }
        return field;
    }



    /**
     * Base TaxTableRow class for those containing certain vendor header fields.
     */
    abstract static class TaxSourceRowWithVendorData extends TaxTableRow {
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        final TaxTableField vendorHeaderGeneratedId;
        final TaxTableField vendorTaxNumber;
        final TaxTableField vendorTypeCode;
        final TaxTableField vendorOwnershipCode;
        final TaxTableField vendorOwnershipCategoryCode;
        final TaxTableField vendorForeignInd;
        
        TaxSourceRowWithVendorData(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields,
                Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.vendorHeaderGeneratedId = getAliasedField(CommonPdpSourceFieldNames.VENDOR_HEADER_GENERATED_ID);
            this.vendorTaxNumber = getAliasedField(CommonPdpSourceFieldNames.VENDOR_TAX_NUMBER);
            this.vendorTypeCode = getAliasedField(CommonPdpSourceFieldNames.VENDOR_TYPE_CODE);
            this.vendorOwnershipCode = getAliasedField(CommonPdpSourceFieldNames.VENDOR_OWNERSHIP_CODE);
            this.vendorOwnershipCategoryCode = getAliasedField(CommonPdpSourceFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE);
            this.vendorForeignInd = getAliasedField(CommonPdpSourceFieldNames.VENDOR_FOREIGN_IND);
        }
    }



    /**
     * Default TaxTableRow implementation for PDP tax source data.
     */
    static final class PdpSourceRow extends TaxSourceRowWithVendorData {
        // Fields from PDP_PROC_SUM_T (ProcessSummary)
        final TaxTableField summaryId;
        final TaxTableField beginDisbursementNbr;
        final TaxTableField endDisbursementNbr;
        final TaxTableField summaryCustomerId;
        final TaxTableField summaryProcessId;
        final TaxTableField summaryLastUpdatedTimestamp;
        // Fields from PDP_CUST_PRFL_T (CustomerProfile)
        final TaxTableField customerId;
        final TaxTableField customerCampusCode;
        final TaxTableField unitCode;
        final TaxTableField subUnitCode;
        final TaxTableField achPaymentDescription;
        // Fields from PDP_PMT_GRP_T (PaymentGroup)
        final TaxTableField paymentGroupId;
        final TaxTableField paymentGroupProcessId;
        final TaxTableField payeeName;
        final TaxTableField payeeId;
        final TaxTableField country;
        final TaxTableField disbursementDate;
        final TaxTableField disbursementNbr;
        final TaxTableField payeeIdTypeCode;
        final TaxTableField line1Address;
        final TaxTableField nraPayment;
        final TaxTableField paymentStatusCode;
        final TaxTableField disbursementTypeCode;
        // Fields from PDP_PMT_DTL_T (PaymentDetail)
        final TaxTableField paymentDetailId;
        final TaxTableField custPaymentDocNbr;
        final TaxTableField paymentDetailPaymentGroupId;
        final TaxTableField financialDocumentTypeCode;
        // Fields from PDP_PMT_ACCT_DTL_T (PaymentAccountDetail)
        final TaxTableField accountDetailId;
        final TaxTableField accountDetailPaymentDetailId;
        final TaxTableField accountDetailFinChartCode;
        final TaxTableField accountNbr;
        final TaxTableField finObjectCode;
        final TaxTableField accountNetAmount;
        final TaxTableField taxClassificationCode;
        final TaxTableField preqDocumentNumber;
        final TaxTableField dvDocumentNumber;
        final TaxTableField nraDocumentNumber;
        final TaxTableField dvIncomeClassCode;
        
        PdpSourceRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.summaryId = getAliasedField(CommonPdpSourceFieldNames.SUMMARY_ID);
            this.beginDisbursementNbr = getAliasedField(CommonPdpSourceFieldNames.BEGIN_DISBURSEMENT_NUMBER);
            this.endDisbursementNbr = getAliasedField(CommonPdpSourceFieldNames.END_DISBURSEMENT_NUMBER);
            this.summaryCustomerId = getAliasedField(CommonPdpSourceFieldNames.SUMMARY_CUSTOMER_ID);
            this.summaryProcessId = getAliasedField(CommonPdpSourceFieldNames.SUMMARY_PROCESS_ID);
            this.summaryLastUpdatedTimestamp = getAliasedField(CommonPdpSourceFieldNames.SUMMARY_LAST_UPDATED_TIMESTAMP);
            this.customerId = getAliasedField(CommonPdpSourceFieldNames.CUSTOMER_ID);
            this.customerCampusCode = getAliasedField(CommonPdpSourceFieldNames.CUSTOMER_CAMPUS_CODE);
            this.unitCode = getAliasedField(CommonPdpSourceFieldNames.UNIT_CODE);
            this.subUnitCode = getAliasedField(CommonPdpSourceFieldNames.SUB_UNIT_CODE);
            this.achPaymentDescription = getAliasedField(CommonPdpSourceFieldNames.ACH_PAYMENT_DESCRIPTION);
            this.paymentGroupId = getAliasedField(CommonPdpSourceFieldNames.PAYMENT_GROUP_ID);
            this.paymentGroupProcessId = getAliasedField(CommonPdpSourceFieldNames.PAYMENT_GROUP_PROCESS_ID);
            this.payeeName = getAliasedField(CommonPdpSourceFieldNames.PAYEE_NAME);
            this.payeeId = getAliasedField(CommonPdpSourceFieldNames.PAYEE_ID);
            this.country = getAliasedField(CommonPdpSourceFieldNames.COUNTRY);
            this.disbursementDate = getAliasedField(CommonPdpSourceFieldNames.DISBURSEMENT_DATE);
            this.disbursementNbr = getAliasedField(CommonPdpSourceFieldNames.DISBURSEMENT_NUMBER);
            this.payeeIdTypeCode = getAliasedField(CommonPdpSourceFieldNames.PAYEE_ID_TYPE_CODE);
            this.line1Address = getAliasedField(CommonPdpSourceFieldNames.LINE1_ADDRESS);
            this.nraPayment = getAliasedField(CommonPdpSourceFieldNames.NRA_PAYMENT_IND);
            this.paymentStatusCode = getAliasedField(CommonPdpSourceFieldNames.PAYMENT_STATUS_CODE);
            this.disbursementTypeCode = getAliasedField(CommonPdpSourceFieldNames.DISBURSEMENT_TYPE_CODE);
            this.paymentDetailId = getAliasedField(CommonPdpSourceFieldNames.PAYMENT_DETAIL_ID);
            this.custPaymentDocNbr = getAliasedField(CommonPdpSourceFieldNames.CUSTOMER_PAYMENT_DOC_NUMBER);
            this.paymentDetailPaymentGroupId = getAliasedField(CommonPdpSourceFieldNames.PAYMENT_DETAIL_PAYMENT_GROUP_ID);
            this.financialDocumentTypeCode = getAliasedField(CommonPdpSourceFieldNames.FINANCIAL_DOCUMENT_TYPE_CODE);
            this.accountDetailId = getAliasedField(CommonPdpSourceFieldNames.ACCOUNT_DETAIL_ID);
            this.accountDetailPaymentDetailId = getAliasedField(CommonPdpSourceFieldNames.ACCOUNT_DETAIL_PAYMENT_DETAIL_ID);
            this.accountDetailFinChartCode = getAliasedField(CommonPdpSourceFieldNames.ACCOUNT_DETAIL_CHART_CODE);
            this.accountNbr = getAliasedField(CommonPdpSourceFieldNames.ACCOUNT_NUMBER);
            this.finObjectCode = getAliasedField(CommonPdpSourceFieldNames.FIN_OBJECT_CODE);
            this.accountNetAmount = getAliasedField(CommonPdpSourceFieldNames.ACCOUNT_NET_AMOUNT);
            this.taxClassificationCode = getAliasedField(CommonPdpSourceFieldNames.TAX_CLASSIFICATION_CODE);
            this.preqDocumentNumber = getAliasedField(CommonPRNCSourceFieldNames.PREQ_DOCUMENT_NUMBER);
            this.dvDocumentNumber = getAliasedField(CommonDvSourceFieldNames.DV_DOCUMENT_NUMBER);
            this.nraDocumentNumber = getAliasedField(CommonDvSourceFieldNames.NRA_DOCUMENT_NUMBER);
            this.dvIncomeClassCode = getAliasedField(CommonDvSourceFieldNames.INCOME_CLASS_CODE);
            // Vendor-related fields will be configured by the superclass.
        }
    }
    
    /**
     * Default TaxTableRow implementation for PRNC tax source data.
     */
    static final class PRNCSourceRow extends TaxSourceRowWithVendorData {
        
        // Fields from AP_PMT_RQST_ACCT_T 
        final TaxTableField accountIdentifier;
        final TaxTableField accountItemIdentifier;
        final TaxTableField amount;
        final TaxTableField chartOfAccountsCode;
        final TaxTableField accountNumber;
        final TaxTableField financialObjectCode;
        
        // Fields from AP_PMT_RQST_ITM_T 
        final TaxTableField purapDocumentIdentifier;
        final TaxTableField itemIdentifier;

        // Fields from AP_PMT_RQST_T (PaymentRequestDocument)
        final TaxTableField preqDocumentNumber;
        final TaxTableField preqPurapDocumentIdentifier;
        final TaxTableField paymentMethodCode;
        final TaxTableField taxClassificationCode;
        final TaxTableField preqVendorHeaderGeneratedIdentifier;
        final TaxTableField preqVendorDetailAssignedIdentifier;
        final TaxTableField preqVendorName;
        final TaxTableField vendorLine1Address;
        final TaxTableField vendorCountryCode;
        
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        final TaxTableField universityDate;

        
        PRNCSourceRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.accountIdentifier = getAliasedField(CommonPRNCSourceFieldNames.ACCOUNTING_LINE_IDENTIFIER);
            this.accountItemIdentifier =  getAliasedField(CommonPRNCSourceFieldNames.ACCOUNTING_LINE_ITEM_IDENTIFIER);
            this.amount =  getAliasedField(CommonPRNCSourceFieldNames.AMOUNT);
            this.chartOfAccountsCode = getAliasedField(CommonPRNCSourceFieldNames.CHART_OF_ACCOUNTS_CODE);
            this.accountNumber = getAliasedField(CommonPRNCSourceFieldNames.ACCOUNT_NUMBER);
            this.financialObjectCode = getAliasedField(CommonPRNCSourceFieldNames.FIN_OBJECT_CODE);
            this.purapDocumentIdentifier = getAliasedField(CommonPRNCSourceFieldNames.PURAP_DOC_IDENTIFIER);
            this.itemIdentifier = getAliasedField(CommonPRNCSourceFieldNames.ITEM_IDENTIFIER);           
            this.preqDocumentNumber = getAliasedField(CommonPRNCSourceFieldNames.PREQ_DOCUMENT_NUMBER);
            this.preqPurapDocumentIdentifier = getAliasedField(CommonPRNCSourceFieldNames.PREQ_PURAP_DOC_IDENTIFIER);
            this.paymentMethodCode = getAliasedField(CommonPRNCSourceFieldNames.DOCUMENT_PREQ_PAYMENT_METHOD_CODE); 
            this.taxClassificationCode = getAliasedField(CommonPRNCSourceFieldNames.DOCUMENT_PREQ_TAX_CLASSIFICATION_CODE);
            this.preqVendorHeaderGeneratedIdentifier = getAliasedField(CommonPRNCSourceFieldNames.PREQ_VENDOR_HEADER_GENERATED_ID);
            this.preqVendorDetailAssignedIdentifier = getAliasedField(CommonPRNCSourceFieldNames.PREQ_VENDOR_DETAIL_ASSIGNED_ID);
            this.preqVendorName = getAliasedField(CommonPRNCSourceFieldNames.PREQ_VENDOR_NAME);
            this.vendorLine1Address = getAliasedField(CommonPRNCSourceFieldNames.PREQ_VENDOR_LINE1_ADDRESS);
            this.vendorCountryCode = getAliasedField(CommonPRNCSourceFieldNames.PREQ_VENDOR_COUNTRY_CODE);;
            this.universityDate = getAliasedField(CommonPRNCSourceFieldNames.UNIVERSITY_DATE);           
            // Vendor-related fields will be configured by the superclass.
        }
    }



    /**
     * Default TaxTableRow implementation for DV tax source data.
     */
    static final class DvSourceRow extends TaxSourceRowWithVendorData {
        // Fields from FP_DV_PAYEE_DTL_T (DisbursementVoucherPayeeDetail)
        final TaxTableField payeeDetailDocumentNumber;
        final TaxTableField disbVchrPaymentReasonCode;
        final TaxTableField disbVchrNonresidentPaymentCode;
        final TaxTableField disbVchrPayeeIdNumber;
        final TaxTableField disbVchrPayeePersonName;
        final TaxTableField disbVchrPayeeLine1Addr;
        final TaxTableField disbVchrPayeeCountryCode;
        final TaxTableField disbursementVoucherPayeeTypeCode;
        // Fields from FP_DV_NRA_TAX_T (DisbursementVoucherNonresidentTax)
        final TaxTableField nraDocumentNumber;
        final TaxTableField federalIncomeTaxPercent;
        final TaxTableField incomeClassCode;
        final TaxTableField incomeTaxTreatyExemptCode;
        final TaxTableField foreignSourceIncomeCode;
        // Fields from FP_ACCT_LINES_T (Accounting lines table)
        final TaxTableField accountingLineDocumentNumber;
        final TaxTableField accountingLineSequenceNumber;
        final TaxTableField financialDocumentLineTypeCode;
        final TaxTableField amount;
        final TaxTableField chartOfAccountsCode;
        final TaxTableField accountNumber;
        final TaxTableField financialObjectCode;
        final TaxTableField financialDocumentLineDescription;
        final TaxTableField debitCreditCode;
        // Fields from FP_DV_DOC_T (DisbursementVoucherDocument)
        final TaxTableField dvDocumentNumber;
        final TaxTableField disbVchrCheckStubText;
        final TaxTableField documentDisbVchrPaymentMethodCode;
        final TaxTableField extractDate;
        final TaxTableField paidDate;
        // Fields from SH_UNIV_DATE_T (UniversityDate)
        final TaxTableField universityDate;
        // Fields from TX_DV_DISBURSEMENT_V (Disbursement Fields)
        final TaxTableField custPaymentDocNbr;
        final TaxTableField disbursementNbr;
        final TaxTableField paymentStatusCode;
        final TaxTableField disbursementTypeCode;
        
        DvSourceRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.payeeDetailDocumentNumber = getAliasedField(CommonDvSourceFieldNames.PAYEE_DETAIL_DOCUMENT_NUMBER);
            this.disbVchrPaymentReasonCode = getAliasedField(CommonDvSourceFieldNames.DV_PAYMENT_REASON_CODE);
            this.disbVchrNonresidentPaymentCode = getAliasedField(CommonDvSourceFieldNames.DV_NONRESIDENT_PAYMENT_CODE);
            this.disbVchrPayeeIdNumber = getAliasedField(CommonDvSourceFieldNames.DV_PAYEE_ID_NUMBER);
            this.disbVchrPayeePersonName = getAliasedField(CommonDvSourceFieldNames.DV_PAYEE_PERSON_NAME);
            this.disbVchrPayeeLine1Addr = getAliasedField(CommonDvSourceFieldNames.DV_PAYEE_LINE1_ADDRESS);
            this.disbVchrPayeeCountryCode = getAliasedField(CommonDvSourceFieldNames.DV_PAYEE_COUNTRY_CODE);
            this.disbursementVoucherPayeeTypeCode = getAliasedField(CommonDvSourceFieldNames.DV_PAYEE_TYPE_CODE);
            this.nraDocumentNumber = getAliasedField(CommonDvSourceFieldNames.NRA_DOCUMENT_NUMBER);
            this.federalIncomeTaxPercent = getAliasedField(CommonDvSourceFieldNames.FEDERAL_INCOME_TAX_PERCENT);
            this.incomeClassCode = getAliasedField(CommonDvSourceFieldNames.INCOME_CLASS_CODE);
            this.incomeTaxTreatyExemptCode = getAliasedField(CommonDvSourceFieldNames.INCOME_TAX_TREATY_EXEMPT_CODE);
            this.foreignSourceIncomeCode = getAliasedField(CommonDvSourceFieldNames.FOREIGN_SOURCE_INCOME_CODE);
            this.accountingLineDocumentNumber = getAliasedField(CommonDvSourceFieldNames.ACCOUNTING_LINE_DOCUMENT_NUMBER);
            this.accountingLineSequenceNumber = getAliasedField(CommonDvSourceFieldNames.ACCOUNTING_LINE_SEQUENCE_NUMBER);
            this.financialDocumentLineTypeCode = getAliasedField(CommonDvSourceFieldNames.FDOC_LINE_TYPE_CODE);
            this.amount = getAliasedField(CommonDvSourceFieldNames.AMOUNT);
            this.chartOfAccountsCode = getAliasedField(CommonDvSourceFieldNames.CHART_OF_ACCOUNTS_CODE);
            this.accountNumber = getAliasedField(CommonDvSourceFieldNames.ACCOUNT_NUMBER);
            this.financialObjectCode = getAliasedField(CommonDvSourceFieldNames.FIN_OBJECT_CODE);
            this.financialDocumentLineDescription = getAliasedField(CommonDvSourceFieldNames.FDOC_LINE_DESCRIPTION);
            this.debitCreditCode = getAliasedField(CommonDvSourceFieldNames.DEBIT_CREDIT_CODE);
            this.dvDocumentNumber = getAliasedField(CommonDvSourceFieldNames.DV_DOCUMENT_NUMBER);
            this.disbVchrCheckStubText = getAliasedField(CommonDvSourceFieldNames.DV_CHECK_STUB_TEXT);
            this.documentDisbVchrPaymentMethodCode = getAliasedField(CommonDvSourceFieldNames.DOCUMENT_DV_PAYMENT_METHOD_CODE);
            this.extractDate = getAliasedField(CommonDvSourceFieldNames.EXTRACT_DATE);
            this.paidDate = getAliasedField(CommonDvSourceFieldNames.PAID_DATE);
            this.universityDate = getAliasedField(CommonDvSourceFieldNames.UNIVERSITY_DATE);

            this.custPaymentDocNbr = getAliasedField(CommonPdpSourceFieldNames.CUSTOMER_PAYMENT_DOC_NUMBER);
            this.disbursementNbr = getAliasedField(CommonDvSourceFieldNames.DISBURSEMENT_NUMBER);
            this.disbursementTypeCode = getAliasedField(CommonDvSourceFieldNames.DISBURSEMENT_TYPE_CODE);
            this.paymentStatusCode = getAliasedField(CommonDvSourceFieldNames.PAYMENT_STATUS_CODE);

            // Vendor-related fields will be configured by the superclass.
        }
    }



    /**
     * Default TaxTableRow implementation for vendor data.
     */
    static final class VendorRow extends TaxTableRow {
        // Fields from PUR_VNDR_HDR_T (VendorHeader)
        final TaxTableField vendorHeaderGeneratedIdentifier;
        final TaxTableField vendorTaxNumber;
        final TaxTableField vendorTypeCode;
        final TaxTableField vendorOwnershipCode;
        final TaxTableField vendorOwnershipCategoryCode;
        final TaxTableField vendorForeignIndicator;
        final TaxTableField vendorGIIN;
        final TaxTableField vendorChapter4StatusCode;
        // Fields from PUR_VNDR_DTL_T (VendorDetail)
        final TaxTableField vendorDetailVendorHeaderGeneratedIdentifier;
        final TaxTableField vendorDetailAssignedIdentifier;
        final TaxTableField vendorParentIndicator;
        final TaxTableField vendorFirstLastNameIndicator;
        final TaxTableField vendorName;
        
        VendorRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.vendorHeaderGeneratedIdentifier = getAliasedField(CommonVendorFieldNames.VENDOR_HEADER_GENERATED_ID);
            this.vendorTaxNumber = getAliasedField(CommonVendorFieldNames.VENDOR_TAX_NUMBER);
            this.vendorTypeCode = getAliasedField(CommonVendorFieldNames.VENDOR_TYPE_CODE);
            this.vendorOwnershipCode = getAliasedField(CommonVendorFieldNames.VENDOR_OWNERSHIP_CODE);
            this.vendorOwnershipCategoryCode = getAliasedField(CommonVendorFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE);
            this.vendorForeignIndicator = getAliasedField(CommonVendorFieldNames.VENDOR_FOREIGN_IND);
            this.vendorGIIN = getAliasedField(CommonVendorFieldNames.VENDOR_GIIN);
            this.vendorChapter4StatusCode = getAliasedField(CommonVendorFieldNames.VENDOR_CHAPTER4_STATUS_CODE);
            this.vendorDetailVendorHeaderGeneratedIdentifier = getAliasedField(CommonVendorFieldNames.VENDOR_DETAIL_VENDOR_HEADER_GENERATED_ID);
            this.vendorDetailAssignedIdentifier = getAliasedField(CommonVendorFieldNames.VENDOR_DETAIL_ASSIGNED_ID);
            this.vendorParentIndicator = getAliasedField(CommonVendorFieldNames.VENDOR_PARENT_IND);
            this.vendorFirstLastNameIndicator = getAliasedField(CommonVendorFieldNames.VENDOR_FIRST_LAST_NAME_IND);
            this.vendorName = getAliasedField(CommonVendorFieldNames.VENDOR_NAME);
        }
    }



    /**
     * Default TaxTableRow implementation for vendor address data.
     */
    static final class VendorAddressRow extends TaxTableRow {
        // Fields from PUR_VNDR_ADDR_T (VendorAddress)
        final TaxTableField vendorAddressGeneratedIdentifier;
        final TaxTableField vendorHeaderGeneratedIdentifier;
        final TaxTableField vendorDetailAssignedIdentifier;
        final TaxTableField vendorAddressTypeCode;
        final TaxTableField vendorLine1Address;
        final TaxTableField vendorLine2Address;
        final TaxTableField vendorCityName;
        final TaxTableField vendorStateCode;
        final TaxTableField vendorZipCode;
        final TaxTableField vendorCountryCode;
        final TaxTableField vendorAttentionName;
        final TaxTableField vendorAddressInternationalProvinceName;
        final TaxTableField vendorAddressEmailAddress;
        final TaxTableField active;
        
        VendorAddressRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.vendorAddressGeneratedIdentifier = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ADDRESS_GENERATED_ID);
            this.vendorHeaderGeneratedIdentifier = getAliasedField(CommonVendorAddressFieldNames.VENDOR_HEADER_GENERATED_ID);
            this.vendorDetailAssignedIdentifier = getAliasedField(CommonVendorAddressFieldNames.VENDOR_DETAIL_ASSIGNED_ID);
            this.vendorAddressTypeCode = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ADDRESS_TYPE_CODE);
            this.vendorLine1Address = getAliasedField(CommonVendorAddressFieldNames.VENDOR_LINE1_ADDRESS);
            this.vendorLine2Address = getAliasedField(CommonVendorAddressFieldNames.VENDOR_LINE2_ADDRESS);
            this.vendorCityName = getAliasedField(CommonVendorAddressFieldNames.VENDOR_CITY_NAME);
            this.vendorStateCode = getAliasedField(CommonVendorAddressFieldNames.VENDOR_STATE_CODE);
            this.vendorZipCode = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ZIP_CODE);
            this.vendorCountryCode = getAliasedField(CommonVendorAddressFieldNames.VENDOR_COUNTRY_CODE);
            this.vendorAttentionName = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ATTENTION_NAME);
            this.vendorAddressInternationalProvinceName = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ADDRESS_INTERNATIONAL_PROVINCE_NAME);
            this.vendorAddressEmailAddress = getAliasedField(CommonVendorAddressFieldNames.VENDOR_ADDRESS_EMAIL_ADDRESS);
            this.active = getAliasedField(CommonVendorAddressFieldNames.ACTIVE);
        }
    }



    /**
     * Default TaxTableRow implementation for document note object data.
     */
    static final class DocumentNoteRow extends TaxTableRow {
        // Fields from KRNS_NTE_T (Note)
        final TaxTableField noteIdentifier;
        final TaxTableField remoteObjectIdentifier;
        final TaxTableField noteText;
        
        DocumentNoteRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.noteIdentifier = getAliasedField(CommonDocumentNoteFieldNames.NOTE_ID);
            this.remoteObjectIdentifier = getAliasedField(CommonDocumentNoteFieldNames.REMOTE_OBJECT_ID);
            this.noteText = getAliasedField(CommonDocumentNoteFieldNames.NOTE_TEXT);
        }
    }



    /**
     * Default TaxTableRow implementation for transaction details.
     */
    static final class TransactionDetailRow extends TaxTableRow {
        // Fields from TX_TRANSACTION_DETAIL_T (TransactionDetail)
        final TaxTableField transactionDetailId;
        final TaxTableField reportYear;
        final TaxTableField documentNumber;
        final TaxTableField documentType;
        final TaxTableField financialDocumentLineNumber;
        final TaxTableField finObjectCode;
        final TaxTableField netPaymentAmount;
        final TaxTableField documentTitle;
        final TaxTableField vendorTaxNumber;
        final TaxTableField incomeCode;
        final TaxTableField incomeCodeSubType;
        final TaxTableField dvCheckStubText;
        final TaxTableField payeeId;
        final TaxTableField vendorName;
        final TaxTableField parentVendorName;
        final TaxTableField vendorTypeCode;
        final TaxTableField vendorOwnershipCode;
        final TaxTableField vendorOwnershipCategoryCode;
        final TaxTableField vendorForeignIndicator;
        final TaxTableField vendorEmailAddress;
        final TaxTableField vendorChapter4StatusCode;
        final TaxTableField vendorGIIN;
        final TaxTableField vendorLine1Address;
        final TaxTableField vendorLine2Address;
        final TaxTableField vendorCityName;
        final TaxTableField vendorStateCode;
        final TaxTableField vendorZipCode;
        final TaxTableField vendorForeignLine1Address;
        final TaxTableField vendorForeignLine2Address;
        final TaxTableField vendorForeignCityName;
        final TaxTableField vendorForeignZipCode;
        final TaxTableField vendorForeignProvinceName;
        final TaxTableField vendorForeignCountryCode;
        final TaxTableField nraPaymentIndicator;
        final TaxTableField paymentDate;
        final TaxTableField paymentPayeeName;
        final TaxTableField incomeClassCode;
        final TaxTableField incomeTaxTreatyExemptIndicator;
        final TaxTableField foreignSourceIncomeIndicator;
        final TaxTableField federalIncomeTaxPercent;
        final TaxTableField paymentDescription;
        final TaxTableField paymentLine1Address;
        final TaxTableField paymentCountryName;
        final TaxTableField chartCode;
        final TaxTableField accountNumber;
        final TaxTableField initiatorNetId;
        final TaxTableField form1099Type;
        final TaxTableField form1099Box;
        final TaxTableField form1099OverriddenType;
        final TaxTableField form1099OverriddenBox;
        final TaxTableField form1042SBox;
        final TaxTableField form1042SOverriddenBox;
        final TaxTableField paymentReasonCode;
        
        final TaxTableField disbursementNbr;
        final TaxTableField paymentStatusCode;
        final TaxTableField disbursementTypeCode;
        final TaxTableField ledgerDocumentTypeCode;

        TransactionDetailRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields,
                Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.transactionDetailId = getAliasedField(TransactionDetailFieldNames.TRANSACTION_DETAIL_ID);
            this.reportYear = getAliasedField(TransactionDetailFieldNames.REPORT_YEAR);
            this.documentNumber = getAliasedField(TransactionDetailFieldNames.DOCUMENT_NUMBER);
            this.documentType = getAliasedField(TransactionDetailFieldNames.DOCUMENT_TYPE);
            this.financialDocumentLineNumber = getAliasedField(TransactionDetailFieldNames.FINANCIAL_DOCUMENT_LINE_NUMBER);
            this.finObjectCode = getAliasedField(TransactionDetailFieldNames.FIN_OBJECT_CODE);
            this.netPaymentAmount = getAliasedField(TransactionDetailFieldNames.NET_PAYMENT_AMOUNT);
            this.documentTitle = getAliasedField(TransactionDetailFieldNames.DOCUMENT_TITLE);
            this.vendorTaxNumber = getAliasedField(TransactionDetailFieldNames.VENDOR_TAX_NUMBER);
            this.incomeCode = getAliasedField(TransactionDetailFieldNames.INCOME_CODE);
            this.incomeCodeSubType = getAliasedField(TransactionDetailFieldNames.INCOME_CODE_SUB_TYPE);
            this.dvCheckStubText = getAliasedField(TransactionDetailFieldNames.DV_CHECK_STUB_TEXT);
            this.payeeId = getAliasedField(TransactionDetailFieldNames.PAYEE_ID);
            this.vendorName = getAliasedField(TransactionDetailFieldNames.VENDOR_NAME);
            this.parentVendorName = getAliasedField(TransactionDetailFieldNames.PARENT_VENDOR_NAME);
            this.vendorTypeCode = getAliasedField(TransactionDetailFieldNames.VENDOR_TYPE_CODE);
            this.vendorOwnershipCode = getAliasedField(TransactionDetailFieldNames.VENDOR_OWNERSHIP_CODE);
            this.vendorOwnershipCategoryCode = getAliasedField(TransactionDetailFieldNames.VENDOR_OWNERSHIP_CATEGORY_CODE);
            this.vendorForeignIndicator = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_INDICATOR);
            this.vendorEmailAddress = getAliasedField(TransactionDetailFieldNames.VENDOR_EMAIL_ADDRESS);
            this.vendorChapter4StatusCode = getAliasedField(TransactionDetailFieldNames.VENDOR_CHAPTER4_STATUS_CODE);
            this.vendorGIIN = getAliasedField(TransactionDetailFieldNames.VENDOR_GIIN);
            this.vendorLine1Address = getAliasedField(TransactionDetailFieldNames.VENDOR_LINE1_ADDRESS);
            this.vendorLine2Address = getAliasedField(TransactionDetailFieldNames.VENDOR_LINE2_ADDRESS);
            this.vendorCityName = getAliasedField(TransactionDetailFieldNames.VENDOR_CITY_NAME);
            this.vendorStateCode = getAliasedField(TransactionDetailFieldNames.VENDOR_STATE_CODE);
            this.vendorZipCode = getAliasedField(TransactionDetailFieldNames.VENDOR_ZIP_CODE);
            this.vendorForeignLine1Address = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_LINE1_ADDRESS);
            this.vendorForeignLine2Address = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_LINE2_ADDRESS);
            this.vendorForeignCityName = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_CITY_NAME);
            this.vendorForeignZipCode = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_ZIP_CODE);
            this.vendorForeignProvinceName = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_PROVINCE_NAME);
            this.vendorForeignCountryCode = getAliasedField(TransactionDetailFieldNames.VENDOR_FOREIGN_COUNTRY_CODE);
            this.nraPaymentIndicator = getAliasedField(TransactionDetailFieldNames.NRA_PAYMENT_INDICATOR);
            this.paymentDate = getAliasedField(TransactionDetailFieldNames.PAYMENT_DATE);
            this.paymentPayeeName = getAliasedField(TransactionDetailFieldNames.PAYMENT_PAYEE_NAME);
            this.incomeClassCode = getAliasedField(TransactionDetailFieldNames.INCOME_CLASS_CODE);
            this.incomeTaxTreatyExemptIndicator = getAliasedField(TransactionDetailFieldNames.INCOME_TAX_TREATY_EXEMPT_INDICATOR);
            this.foreignSourceIncomeIndicator = getAliasedField(TransactionDetailFieldNames.FOREIGN_SOURCE_INCOME_INDICATOR);
            this.federalIncomeTaxPercent = getAliasedField(TransactionDetailFieldNames.FEDERAL_INCOME_TAX_PERCENT);
            this.paymentDescription = getAliasedField(TransactionDetailFieldNames.PAYMENT_DESCRIPTION);
            this.paymentLine1Address = getAliasedField(TransactionDetailFieldNames.PAYMENT_LINE1_ADDRESS);
            this.paymentCountryName = getAliasedField(TransactionDetailFieldNames.PAYMENT_COUNTRY_NAME);
            this.chartCode = getAliasedField(TransactionDetailFieldNames.CHART_CODE);
            this.accountNumber = getAliasedField(TransactionDetailFieldNames.ACCOUNT_NUMBER);
            this.initiatorNetId = getAliasedField(TransactionDetailFieldNames.INITIATOR_NETID);
            this.form1099Type = getAliasedField(TransactionDetailFieldNames.FORM_1099_TYPE);
            this.form1099Box = getAliasedField(TransactionDetailFieldNames.FORM_1099_BOX);
            this.form1099OverriddenType = getAliasedField(TransactionDetailFieldNames.FORM_1099_OVERRIDDEN_TYPE);
            this.form1099OverriddenBox = getAliasedField(TransactionDetailFieldNames.FORM_1099_OVERRIDDEN_BOX);
            this.form1042SBox = getAliasedField(TransactionDetailFieldNames.FORM_1042S_BOX);
            this.form1042SOverriddenBox = getAliasedField(TransactionDetailFieldNames.FORM_1042S_OVERRIDDEN_BOX);
            this.paymentReasonCode = getAliasedField(TransactionDetailFieldNames.PAYMENT_REASON_CODE);

            this.disbursementNbr = getAliasedField(TransactionDetailFieldNames.DISBURSEMENT_NUMBER);
            this.paymentStatusCode = getAliasedField(TransactionDetailFieldNames.PAYMENT_STATUS_CODE);
            this.disbursementTypeCode = getAliasedField(TransactionDetailFieldNames.DISBURSEMENT_TYPE_CODE);
            this.ledgerDocumentTypeCode = getAliasedField(TransactionDetailFieldNames.LEDGER_DOCUMENT_TYPE_CODE);
        }
    }



    /**
     * Default TaxTableRow implementation for non-DB-backed fields.
     */
    static final class DerivedValuesRow extends TaxTableRow {
        // These fields are derived from other object fields or from parameters. A few refer to DB-backed fields that have been modified in some way.
        final TaxTableField vendorLastName;
        final TaxTableField vendorFirstName;
        final TaxTableField vendorEmailAddress;
        final TaxTableField vendorUSAddressLine1;
        final TaxTableField vendorForeignAddressLine1;
        final TaxTableField vendorForeignAddressLine2;
        final TaxTableField vendorForeignCityName;
        final TaxTableField vendorForeignZipCode;
        final TaxTableField vendorForeignProvinceName;
        final TaxTableField vendorForeignCountryCode;
        final TaxTableField vendorForeignCountryName;
        final TaxTableField vendorForeignCountryIndicator;
        final TaxTableField vendorAnyAddressLine1;
        final TaxTableField vendorZipCodeNumOnly;
        final TaxTableField ssn;
        final TaxTableField itin;
        final TaxTableField chapter3StatusCode;
        final TaxTableField chapter3ExemptionCode;
        final TaxTableField chapter4ExemptionCode;
        final TaxTableField incomeCode;
        final TaxTableField ein;
        final TaxTableField chapter3TaxRate;
        final TaxTableField grossAmount;
        final TaxTableField fedTaxWithheldAmount;
        final TaxTableField stateIncomeTaxWithheldAmount;
        final TaxTableField stateCode;
        final TaxTableField dvCheckStubTextWithUpdatedWhitespace;
        final TaxTableField tabSiteId;
        final TaxTableField miscRents;
        final TaxTableField miscRoyalties;
        final TaxTableField miscOtherIncome;
        final TaxTableField miscFedIncomeTaxWithheld;
        final TaxTableField miscFishingBoatProceeds;
        final TaxTableField miscMedicalHealthcarePayments;
        final TaxTableField miscDirectSalesInd;
        final TaxTableField miscSubstitutePayments;
        final TaxTableField miscCropInsuranceProceeds;
        final TaxTableField miscGrossProceedsAttorney;
        final TaxTableField miscSection409ADeferral;
        final TaxTableField miscGoldenParachute;
        final TaxTableField miscNonqualifiedDeferredCompensation;
        final TaxTableField miscStateTaxWithheld;
        final TaxTableField miscPayerStateNumber;
        final TaxTableField miscStateIncome;
        final TaxTableField necNonemployeeCompensation;
        final TaxTableField necFedIncomeTaxWithheld;
        final TaxTableField necStateTaxWithheld;
        final TaxTableField necPayerStateNumber;
        final TaxTableField necStateIncome;
        // These two represent unknown/undefined 1099/1042S tax boxes. They allow for using TransactionOverride BOs to exclude transactions.
        final TaxTableField boxUnknown1099;
        final TaxTableField boxUnknown1042s;
        final TaxTableField endDate;
        final TaxTableField taxYear;
        final TaxTableField filerName1;
        final TaxTableField filerName2;
        final TaxTableField filerAddress1;
        final TaxTableField filerAddress2;
        final TaxTableField filerCity;
        final TaxTableField filerState;
        final TaxTableField filerZipCode;
        final TaxTableField filerPhoneNumber;
        
        DerivedValuesRow(String rowId, Map<String,TaxTableField> fields, List<String> tables, Map<String,TaxTableField> aliasedFields, Integer insertOffset) {
            super(rowId, fields, tables, aliasedFields, insertOffset);
            
            this.vendorLastName = getAliasedField(DerivedFieldNames.VENDOR_LAST_NAME);
            this.vendorFirstName = getAliasedField(DerivedFieldNames.VENDOR_FIRST_NAME);
            this.vendorEmailAddress = getAliasedField(DerivedFieldNames.VENDOR_EMAIL_ADDRESS);
            this.vendorUSAddressLine1 = getAliasedField(DerivedFieldNames.VENDOR_US_ADDRESS_LINE_1);
            this.vendorForeignAddressLine1 = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_ADDRESS_LINE_1);
            this.vendorForeignAddressLine2 = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_ADDRESS_LINE_2);
            this.vendorForeignCityName = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_CITY_NAME);
            this.vendorForeignZipCode = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_ZIP_CODE);
            this.vendorForeignProvinceName = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_PROVINCE_NAME);
            this.vendorForeignCountryCode = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_COUNTRY_CODE);
            this.vendorForeignCountryName = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_COUNTRY_NAME);
            this.vendorForeignCountryIndicator = getAliasedField(DerivedFieldNames.VENDOR_FOREIGN_COUNTRY_INDICATOR);
            this.vendorAnyAddressLine1 = getAliasedField(DerivedFieldNames.VENDOR_ANY_ADDRESS_LINE_1);
            this.vendorZipCodeNumOnly = getAliasedField(DerivedFieldNames.VENDOR_ZIP_CODE_NUM_ONLY);
            this.ssn = getAliasedField(DerivedFieldNames.SSN);
            this.itin = getAliasedField(DerivedFieldNames.ITIN);
            this.chapter3StatusCode = getAliasedField(DerivedFieldNames.CHAPTER3_STATUS_CODE);
            this.chapter3ExemptionCode = getAliasedField(DerivedFieldNames.CHAPTER3_EXEMPTION_CODE);
            this.chapter4ExemptionCode = getAliasedField(DerivedFieldNames.CHAPTER4_EXEMPTION_CODE);
            this.incomeCode = getAliasedField(DerivedFieldNames.INCOME_CODE);
            this.ein = getAliasedField(DerivedFieldNames.EIN);
            this.chapter3TaxRate = getAliasedField(DerivedFieldNames.CHAPTER3_TAX_RATE);
            this.grossAmount = getAliasedField(DerivedFieldNames.GROSS_AMOUNT);
            this.fedTaxWithheldAmount = getAliasedField(DerivedFieldNames.FTW_AMOUNT);
            this.stateIncomeTaxWithheldAmount = getAliasedField(DerivedFieldNames.SITW_AMOUNT);
            this.stateCode = getAliasedField(DerivedFieldNames.STATE_CODE);
            this.dvCheckStubTextWithUpdatedWhitespace = getAliasedField(DerivedFieldNames.DV_CHECK_STUB_WITH_UPDATED_WHITESPACE);
            this.tabSiteId = getAliasedField(DerivedFieldNames.TAB_SITE_ID);
            this.miscRents = getAliasedField(DerivedFieldNames.MISC_RENTS);
            this.miscRoyalties = getAliasedField(DerivedFieldNames.MISC_ROYALTIES);
            this.miscOtherIncome = getAliasedField(DerivedFieldNames.MISC_OTHER_INCOME);
            this.miscFedIncomeTaxWithheld = getAliasedField(DerivedFieldNames.MISC_FED_INCOME_TAX_WITHHELD);
            this.miscFishingBoatProceeds = getAliasedField(DerivedFieldNames.MISC_FISHING_BOAT_PROCEEDS);
            this.miscMedicalHealthcarePayments = getAliasedField(DerivedFieldNames.MISC_MEDICAL_HEALTHCARE_PAYMENTS);
            this.miscDirectSalesInd = getAliasedField(DerivedFieldNames.MISC_DIRECT_SALES_IND);
            this.miscSubstitutePayments = getAliasedField(DerivedFieldNames.MISC_SUBSTITUTE_PAYMENTS);
            this.miscCropInsuranceProceeds = getAliasedField(DerivedFieldNames.MISC_CROP_INSURANCE_PROCEEDS);
            this.miscGrossProceedsAttorney = getAliasedField(DerivedFieldNames.MISC_GROSS_PROCEEDS_ATTORNEY);
            this.miscSection409ADeferral = getAliasedField(DerivedFieldNames.MISC_SECTION_409A_DEFERRAL);
            this.miscGoldenParachute = getAliasedField(DerivedFieldNames.MISC_GOLDEN_PARACHUTE);
            this.miscNonqualifiedDeferredCompensation = getAliasedField(
                    DerivedFieldNames.MISC_NONQUALIFIED_DEFERRED_COMPENSATION);
            this.miscStateTaxWithheld = getAliasedField(DerivedFieldNames.MISC_STATE_TAX_WITHHELD);
            this.miscPayerStateNumber = getAliasedField(DerivedFieldNames.MISC_PAYER_STATE_NUMBER);
            this.miscStateIncome = getAliasedField(DerivedFieldNames.MISC_STATE_INCOME);
            this.necNonemployeeCompensation = getAliasedField(DerivedFieldNames.NEC_NONEMPLOYEE_COMPENSATION);
            this.necFedIncomeTaxWithheld = getAliasedField(DerivedFieldNames.NEC_FED_INCOME_TAX_WITHHELD);
            this.necStateTaxWithheld = getAliasedField(DerivedFieldNames.NEC_STATE_TAX_WITHHELD);
            this.necPayerStateNumber = getAliasedField(DerivedFieldNames.NEC_PAYER_STATE_NUMBER);
            this.necStateIncome = getAliasedField(DerivedFieldNames.NEC_STATE_INCOME);
            this.boxUnknown1099 = getAliasedField(DerivedFieldNames.BOX_UNKNOWN_1099);
            this.boxUnknown1042s = getAliasedField(DerivedFieldNames.BOX_UNKNOWN_1042S);
            this.endDate = getAliasedField(DerivedFieldNames.END_DATE);
            this.taxYear = getAliasedField(DerivedFieldNames.TAX_YEAR);
            this.filerName1 = getAliasedField(DerivedFieldNames.FILER_NAME_1);
            this.filerName2 = getAliasedField(DerivedFieldNames.FILER_NAME_2);
            this.filerAddress1 = getAliasedField(DerivedFieldNames.FILER_ADDRESS_1);
            this.filerAddress2 = getAliasedField(DerivedFieldNames.FILER_ADDRESS_2);
            this.filerCity = getAliasedField(DerivedFieldNames.FILER_CITY);
            this.filerState = getAliasedField(DerivedFieldNames.FILER_STATE);
            this.filerZipCode = getAliasedField(DerivedFieldNames.FILER_ZIP_CODE);
            this.filerPhoneNumber = getAliasedField(DerivedFieldNames.FILER_PHONE_NUMBER);
        }
    }

}
