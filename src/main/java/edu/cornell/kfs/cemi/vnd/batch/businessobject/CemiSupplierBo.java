package edu.cornell.kfs.cemi.vnd.batch.businessobject;

// CHANGE extends TO USE edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
// MAKE ALL RELATED CODING CHANGES NEEDED TO MAKE BO FOLLOW PATTERN. 
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiSupplierBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 8273259452867160348L;

    private String supplierId;
    private String supplierReferenceId;
    private String supplierName;
    private String taxAuthorityFormType;
    private String irs1099Supplier;
    private String report1099WithParent;
    private String taxIdType;
    private String taxIdText;
    private String transactionTaxId;
    private String defaultWithholdingTaxCode;
    private String primaryTaxId;
    private String countryTaxId;
    private String supplierCategory;
    private String supplierGroup1;
    private String supplierGroup2;
    private String supplierGroup3;
    private String supplierGroup4;
    private String customerAccountNumber;
    private String dunsNumber;
    private String paymentTerms;
    private String defaultPaymentType;
    private String paymentTypesAccepted1;
    private String paymentTypesAccepted2;
    private String paymentTypesAccepted3;
    private String currency;
    private String acceptedCurrencies;
    private String procurementCreditCard;
    private String alwaysSeparatePayments;
    private String textForDefaultSupplierPaymentMemo;
    private String useSupplierReferenceAsDefaultSupplierPaymentMemo;
    private String useInvoiceMemoAsDefaultSupplierPaymentMemo;
    private String useSupplierConnectionMemo;
    private String doNotReplaceAll;
    private String supplierClassification1;
    private String supplierClassificationField1;
    private String fieldDateValue1;
    private String fieldNumberValue1;
    private String fieldTextValue1;
    private String fieldSingleSelectChoice1;
    private String fieldMultiSelectChoice1;
    private String supplierClassification2;
    private String supplierClassificationField2;
    private String fieldDateValue2;
    private String fieldNumberValue2;
    private String fieldTextValue2;
    private String fieldSingleSelectChoice2;
    private String fieldMultiSelectChoice2;
    private String alternateNameBusinessEntity1;
    private String alternateNameUsageBusinessEntity1;
    private String alternateNameBusinessEntity2;
    private String alternateNameUsageBusinessEntity2;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierReferenceId() {
        return supplierReferenceId;
    }

    public void setSupplierReferenceId(final String supplierReferenceId) {
        this.supplierReferenceId = supplierReferenceId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(final String supplierName) {
        this.supplierName = supplierName;
    }

    public String getTaxAuthorityFormType() {
        return taxAuthorityFormType;
    }

    public void setTaxAuthorityFormType(final String taxAuthorityFormType) {
        this.taxAuthorityFormType = taxAuthorityFormType;
    }

    public String getIrs1099Supplier() {
        return irs1099Supplier;
    }

    public void setIrs1099Supplier(final String irs1099Supplier) {
        this.irs1099Supplier = irs1099Supplier;
    }

    public String getReport1099WithParent() {
        return report1099WithParent;
    }

    public void setReport1099WithParent(final String report1099WithParent) {
        this.report1099WithParent = report1099WithParent;
    }

    public String getTaxIdType() {
        return taxIdType;
    }

    public void setTaxIdType(final String taxIdType) {
        this.taxIdType = taxIdType;
    }

    public String getTaxIdText() {
        return taxIdText;
    }

    public void setTaxIdText(final String taxIdText) {
        this.taxIdText = taxIdText;
    }

    public String getTransactionTaxId() {
        return transactionTaxId;
    }

    public void setTransactionTaxId(final String transactionTaxId) {
        this.transactionTaxId = transactionTaxId;
    }

    public String getDefaultWithholdingTaxCode() {
        return defaultWithholdingTaxCode;
    }

    public void setDefaultWithholdingTaxCode(final String defaultWithholdingTaxCode) {
        this.defaultWithholdingTaxCode = defaultWithholdingTaxCode;
    }

    public String getPrimaryTaxId() {
        return primaryTaxId;
    }

    public void setPrimaryTaxId(final String primaryTaxId) {
        this.primaryTaxId = primaryTaxId;
    }

    public String getCountryTaxId() {
        return countryTaxId;
    }

    public void setCountryTaxId(final String countryTaxId) {
        this.countryTaxId = countryTaxId;
    }

    public String getSupplierCategory() {
        return supplierCategory;
    }

    public void setSupplierCategory(final String supplierCategory) {
        this.supplierCategory = supplierCategory;
    }

    public String getSupplierGroup1() {
        return supplierGroup1;
    }

    public void setSupplierGroup1(final String supplierGroup1) {
        this.supplierGroup1 = supplierGroup1;
    }

    public String getSupplierGroup2() {
        return supplierGroup2;
    }

    public void setSupplierGroup2(final String supplierGroup2) {
        this.supplierGroup2 = supplierGroup2;
    }

    public String getSupplierGroup3() {
        return supplierGroup3;
    }

    public void setSupplierGroup3(final String supplierGroup3) {
        this.supplierGroup3 = supplierGroup3;
    }

    public String getSupplierGroup4() {
        return supplierGroup4;
    }

    public void setSupplierGroup4(final String supplierGroup4) {
        this.supplierGroup4 = supplierGroup4;
    }

    public String getCustomerAccountNumber() {
        return customerAccountNumber;
    }

    public void setCustomerAccountNumber(final String customerAccountNumber) {
        this.customerAccountNumber = customerAccountNumber;
    }

    public String getDunsNumber() {
        return dunsNumber;
    }

    public void setDunsNumber(final String dunsNumber) {
        this.dunsNumber = dunsNumber;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(final String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getDefaultPaymentType() {
        return defaultPaymentType;
    }

    public void setDefaultPaymentType(final String defaultPaymentType) {
        this.defaultPaymentType = defaultPaymentType;
    }

    public String getPaymentTypesAccepted1() {
        return paymentTypesAccepted1;
    }

    public void setPaymentTypesAccepted1(final String paymentTypesAccepted1) {
        this.paymentTypesAccepted1 = paymentTypesAccepted1;
    }

    public String getPaymentTypesAccepted2() {
        return paymentTypesAccepted2;
    }

    public void setPaymentTypesAccepted2(final String paymentTypesAccepted2) {
        this.paymentTypesAccepted2 = paymentTypesAccepted2;
    }

    public String getPaymentTypesAccepted3() {
        return paymentTypesAccepted3;
    }

    public void setPaymentTypesAccepted3(final String paymentTypesAccepted3) {
        this.paymentTypesAccepted3 = paymentTypesAccepted3;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getAcceptedCurrencies() {
        return acceptedCurrencies;
    }

    public void setAcceptedCurrencies(final String acceptedCurrencies) {
        this.acceptedCurrencies = acceptedCurrencies;
    }

    public String getProcurementCreditCard() {
        return procurementCreditCard;
    }

    public void setProcurementCreditCard(final String procurementCreditCard) {
        this.procurementCreditCard = procurementCreditCard;
    }

    public String getAlwaysSeparatePayments() {
        return alwaysSeparatePayments;
    }

    public void setAlwaysSeparatePayments(final String alwaysSeparatePayments) {
        this.alwaysSeparatePayments = alwaysSeparatePayments;
    }

    public String getTextForDefaultSupplierPaymentMemo() {
        return textForDefaultSupplierPaymentMemo;
    }

    public void setTextForDefaultSupplierPaymentMemo(final String textForDefaultSupplierPaymentMemo) {
        this.textForDefaultSupplierPaymentMemo = textForDefaultSupplierPaymentMemo;
    }

    public String getUseSupplierReferenceAsDefaultSupplierPaymentMemo() {
        return useSupplierReferenceAsDefaultSupplierPaymentMemo;
    }

    public void setUseSupplierReferenceAsDefaultSupplierPaymentMemo(
            String useSupplierReferenceAsDefaultSupplierPaymentMemo) {
        this.useSupplierReferenceAsDefaultSupplierPaymentMemo = useSupplierReferenceAsDefaultSupplierPaymentMemo;
    }

    public String getUseInvoiceMemoAsDefaultSupplierPaymentMemo() {
        return useInvoiceMemoAsDefaultSupplierPaymentMemo;
    }

    public void setUseInvoiceMemoAsDefaultSupplierPaymentMemo(final String useInvoiceMemoAsDefaultSupplierPaymentMemo) {
        this.useInvoiceMemoAsDefaultSupplierPaymentMemo = useInvoiceMemoAsDefaultSupplierPaymentMemo;
    }

    public String getUseSupplierConnectionMemo() {
        return useSupplierConnectionMemo;
    }

    public void setUseSupplierConnectionMemo(final String useSupplierConnectionMemo) {
        this.useSupplierConnectionMemo = useSupplierConnectionMemo;
    }

    public String getDoNotReplaceAll() {
        return doNotReplaceAll;
    }

    public void setDoNotReplaceAll(final String doNotReplaceAll) {
        this.doNotReplaceAll = doNotReplaceAll;
    }

    public String getSupplierClassification1() {
        return supplierClassification1;
    }

    public void setSupplierClassification1(final String supplierClassification1) {
        this.supplierClassification1 = supplierClassification1;
    }

    public String getSupplierClassificationField1() {
        return supplierClassificationField1;
    }

    public void setSupplierClassificationField1(final String supplierClassificationField1) {
        this.supplierClassificationField1 = supplierClassificationField1;
    }

    public String getFieldDateValue1() {
        return fieldDateValue1;
    }

    public void setFieldDateValue1(final String fieldDateValue1) {
        this.fieldDateValue1 = fieldDateValue1;
    }

    public String getFieldNumberValue1() {
        return fieldNumberValue1;
    }

    public void setFieldNumberValue1(final String fieldNumberValue1) {
        this.fieldNumberValue1 = fieldNumberValue1;
    }

    public String getFieldTextValue1() {
        return fieldTextValue1;
    }

    public void setFieldTextValue1(final String fieldTextValue1) {
        this.fieldTextValue1 = fieldTextValue1;
    }

    public String getFieldSingleSelectChoice1() {
        return fieldSingleSelectChoice1;
    }

    public void setFieldSingleSelectChoice1(final String fieldSingleSelectChoice1) {
        this.fieldSingleSelectChoice1 = fieldSingleSelectChoice1;
    }

    public String getFieldMultiSelectChoice1() {
        return fieldMultiSelectChoice1;
    }

    public void setFieldMultiSelectChoice1(final String fieldMultiSelectChoice1) {
        this.fieldMultiSelectChoice1 = fieldMultiSelectChoice1;
    }

    public String getSupplierClassification2() {
        return supplierClassification2;
    }

    public void setSupplierClassification2(final String supplierClassification2) {
        this.supplierClassification2 = supplierClassification2;
    }

    public String getSupplierClassificationField2() {
        return supplierClassificationField2;
    }

    public void setSupplierClassificationField2(final String supplierClassificationField2) {
        this.supplierClassificationField2 = supplierClassificationField2;
    }

    public String getFieldDateValue2() {
        return fieldDateValue2;
    }

    public void setFieldDateValue2(final String fieldDateValue2) {
        this.fieldDateValue2 = fieldDateValue2;
    }

    public String getFieldNumberValue2() {
        return fieldNumberValue2;
    }

    public void setFieldNumberValue2(final String fieldNumberValue2) {
        this.fieldNumberValue2 = fieldNumberValue2;
    }

    public String getFieldTextValue2() {
        return fieldTextValue2;
    }

    public void setFieldTextValue2(final String fieldTextValue2) {
        this.fieldTextValue2 = fieldTextValue2;
    }

    public String getFieldSingleSelectChoice2() {
        return fieldSingleSelectChoice2;
    }

    public void setFieldSingleSelectChoice2(final String fieldSingleSelectChoice2) {
        this.fieldSingleSelectChoice2 = fieldSingleSelectChoice2;
    }

    public String getFieldMultiSelectChoice2() {
        return fieldMultiSelectChoice2;
    }

    public void setFieldMultiSelectChoice2(final String fieldMultiSelectChoice2) {
        this.fieldMultiSelectChoice2 = fieldMultiSelectChoice2;
    }

    public String getAlternateNameBusinessEntity1() {
        return alternateNameBusinessEntity1;
    }

    public void setAlternateNameBusinessEntity1(final String alternateNameBusinessEntity1) {
        this.alternateNameBusinessEntity1 = alternateNameBusinessEntity1;
    }

    public String getAlternateNameUsageBusinessEntity1() {
        return alternateNameUsageBusinessEntity1;
    }

    public void setAlternateNameUsageBusinessEntity1(final String alternateNameUsageBusinessEntity1) {
        this.alternateNameUsageBusinessEntity1 = alternateNameUsageBusinessEntity1;
    }

    public String getAlternateNameBusinessEntity2() {
        return alternateNameBusinessEntity2;
    }

    public void setAlternateNameBusinessEntity2(final String alternateNameBusinessEntity2) {
        this.alternateNameBusinessEntity2 = alternateNameBusinessEntity2;
    }

    public String getAlternateNameUsageBusinessEntity2() {
        return alternateNameUsageBusinessEntity2;
    }

    public void setAlternateNameUsageBusinessEntity2(final String alternateNameUsageBusinessEntity2) {
        this.alternateNameUsageBusinessEntity2 = alternateNameUsageBusinessEntity2;
    }

}
