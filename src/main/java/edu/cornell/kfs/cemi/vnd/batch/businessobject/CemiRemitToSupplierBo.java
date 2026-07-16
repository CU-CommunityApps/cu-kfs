package edu.cornell.kfs.cemi.vnd.batch.businessobject;

// CHANGE extends TO USE edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
// MAKE ALL RELATED CODING CHANGES NEEDED TO MAKE BO FOLLOW PATTERN.
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiRemitToSupplierBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 189635335599203418L;

    private String supplierId;
    private String supplierConnectionId;
    private String supplierConnectionName;
    private String defaultPaymentType;
    private String acceptedPaymentType1;
    private String acceptedPaymentType2;
    private String acceptedPaymentType3;
    private String settlementBankAccount;
    private String remitToAddressId;
    private String remitToEmailAddress;
    private String payeeAlternateName;
    private String alternateNameUsage;
    private String paymentMemo;
    private String isDefault;
    private String defaultPaymentTerms;
    private String alwaysSeparatePayments;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierConnectionId() {
        return supplierConnectionId;
    }

    public void setSupplierConnectionId(final String supplierConnectionId) {
        this.supplierConnectionId = supplierConnectionId;
    }

    public String getSupplierConnectionName() {
        return supplierConnectionName;
    }

    public void setSupplierConnectionName(final String supplierConnectionName) {
        this.supplierConnectionName = supplierConnectionName;
    }

    public String getDefaultPaymentType() {
        return defaultPaymentType;
    }

    public void setDefaultPaymentType(final String defaultPaymentType) {
        this.defaultPaymentType = defaultPaymentType;
    }

    public String getAcceptedPaymentType1() {
        return acceptedPaymentType1;
    }

    public void setAcceptedPaymentType1(final String acceptedPaymentType1) {
        this.acceptedPaymentType1 = acceptedPaymentType1;
    }

    public String getAcceptedPaymentType2() {
        return acceptedPaymentType2;
    }

    public void setAcceptedPaymentType2(final String acceptedPaymentType2) {
        this.acceptedPaymentType2 = acceptedPaymentType2;
    }

    public String getAcceptedPaymentType3() {
        return acceptedPaymentType3;
    }

    public void setAcceptedPaymentType3(final String acceptedPaymentType3) {
        this.acceptedPaymentType3 = acceptedPaymentType3;
    }

    public String getSettlementBankAccount() {
        return settlementBankAccount;
    }

    public void setSettlementBankAccount(final String settlementBankAccount) {
        this.settlementBankAccount = settlementBankAccount;
    }

    public String getRemitToAddressId() {
        return remitToAddressId;
    }

    public void setRemitToAddressId(final String remitToAddressId) {
        this.remitToAddressId = remitToAddressId;
    }

    public String getRemitToEmailAddress() {
        return remitToEmailAddress;
    }

    public void setRemitToEmailAddress(final String remitToEmailAddress) {
        this.remitToEmailAddress = remitToEmailAddress;
    }

    public String getPayeeAlternateName() {
        return payeeAlternateName;
    }

    public void setPayeeAlternateName(final String payeeAlternateName) {
        this.payeeAlternateName = payeeAlternateName;
    }

    public String getAlternateNameUsage() {
        return alternateNameUsage;
    }

    public void setAlternateNameUsage(final String alternateNameUsage) {
        this.alternateNameUsage = alternateNameUsage;
    }

    public String getPaymentMemo() {
        return paymentMemo;
    }

    public void setPaymentMemo(final String paymentMemo) {
        this.paymentMemo = paymentMemo;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(final String isDefault) {
        this.isDefault = isDefault;
    }

    public String getDefaultPaymentTerms() {
        return defaultPaymentTerms;
    }

    public void setDefaultPaymentTerms(final String defaultPaymentTerms) {
        this.defaultPaymentTerms = defaultPaymentTerms;
    }

    public String getAlwaysSeparatePayments() {
        return alwaysSeparatePayments;
    }

    public void setAlwaysSeparatePayments(final String alwaysSeparatePayments) {
        this.alwaysSeparatePayments = alwaysSeparatePayments;
    }

}
