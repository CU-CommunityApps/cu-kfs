package edu.cornell.kfs.cemi.vnd.batch.businessobject;

// CHANGE extends TO USE edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase
// MAKE ALL RELATED CODING CHANGES NEEDED TO MAKE BO FOLLOW PATTERN. 
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiOrderFromSupplierBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 6408159426321723358L;

    private String spreadsheetKey;
    private String supplierIdHeader;
    private String autoComplete;
    private String comment;
    private String worker;
    private String supplierReferenceId;
    private String supplierId;
    private String supplierConnectionRowId;
    private String supplierConnection;
    private String supplierConnectionId;
    private String supplierConnectionName;
    private String defaultForPoType1;
    private String defaultForPoType2;
    private String defaultForPoType3;
    private String shippingMethod;
    private String shippingTerms;
    private String purchaseOrderIssueOption;
    private String emailRowId;
    private String emailId;
    private String emailAddress;
    private String remitToSupplierConnection;
    private String orderFromAddressReference;
    private String alternateNameRowId;
    private String alternateName;
    private String alternateNameUsage;
    private String isDefault;
    private String isInactive;
    private String memo;

    public String getSpreadsheetKey() {
        return spreadsheetKey;
    }

    public void setSpreadsheetKey(final String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
    }

    public String getSupplierIdHeader() {
        return supplierIdHeader;
    }

    public void setSupplierIdHeader(final String supplierIdHeader) {
        this.supplierIdHeader = supplierIdHeader;
    }

    public String getAutoComplete() {
        return autoComplete;
    }

    public void setAutoComplete(final String autoComplete) {
        this.autoComplete = autoComplete;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(final String worker) {
        this.worker = worker;
    }

    public String getSupplierReferenceId() {
        return supplierReferenceId;
    }

    public void setSupplierReferenceId(final String supplierReferenceId) {
        this.supplierReferenceId = supplierReferenceId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierConnectionRowId() {
        return supplierConnectionRowId;
    }

    public void setSupplierConnectionRowId(final String supplierConnectionRowId) {
        this.supplierConnectionRowId = supplierConnectionRowId;
    }

    public String getSupplierConnection() {
        return supplierConnection;
    }

    public void setSupplierConnection(final String supplierConnection) {
        this.supplierConnection = supplierConnection;
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

    public String getDefaultForPoType1() {
        return defaultForPoType1;
    }

    public void setDefaultForPoType1(final String defaultForPoType1) {
        this.defaultForPoType1 = defaultForPoType1;
    }

    public String getDefaultForPoType2() {
        return defaultForPoType2;
    }

    public void setDefaultForPoType2(final String defaultForPoType2) {
        this.defaultForPoType2 = defaultForPoType2;
    }

    public String getDefaultForPoType3() {
        return defaultForPoType3;
    }

    public void setDefaultForPoType3(final String defaultForPoType3) {
        this.defaultForPoType3 = defaultForPoType3;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public void setShippingMethod(final String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public String getShippingTerms() {
        return shippingTerms;
    }

    public void setShippingTerms(final String shippingTerms) {
        this.shippingTerms = shippingTerms;
    }

    public String getPurchaseOrderIssueOption() {
        return purchaseOrderIssueOption;
    }

    public void setPurchaseOrderIssueOption(final String purchaseOrderIssueOption) {
        this.purchaseOrderIssueOption = purchaseOrderIssueOption;
    }

    public String getEmailRowId() {
        return emailRowId;
    }

    public void setEmailRowId(final String emailRowId) {
        this.emailRowId = emailRowId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getRemitToSupplierConnection() {
        return remitToSupplierConnection;
    }

    public void setRemitToSupplierConnection(final String remitToSupplierConnection) {
        this.remitToSupplierConnection = remitToSupplierConnection;
    }

    public String getOrderFromAddressReference() {
        return orderFromAddressReference;
    }

    public void setOrderFromAddressReference(final String orderFromAddressReference) {
        this.orderFromAddressReference = orderFromAddressReference;
    }

    public String getAlternateNameRowId() {
        return alternateNameRowId;
    }

    public void setAlternateNameRowId(final String alternateNameRowId) {
        this.alternateNameRowId = alternateNameRowId;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(final String alternateName) {
        this.alternateName = alternateName;
    }

    public String getAlternateNameUsage() {
        return alternateNameUsage;
    }

    public void setAlternateNameUsage(final String alternateNameUsage) {
        this.alternateNameUsage = alternateNameUsage;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(final String isDefault) {
        this.isDefault = isDefault;
    }

    public String getIsInactive() {
        return isInactive;
    }

    public void setIsInactive(final String isInactive) {
        this.isInactive = isInactive;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(final String memo) {
        this.memo = memo;
    }

}
