package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiRemitToSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;

public class CemiRemitToSupplierBoFactory {

    private CemiSupplierAddressBo supplierAddress;
    private CemiSupplierBo supplier;
    private String emailAddress;
    private int remitIndexForSupplier;
    private boolean defaultConnection;

    public CemiRemitToSupplierBoFactory withSupplierAddress(final CemiSupplierAddressBo supplierAddress) {
        this.supplierAddress = supplierAddress;
        return this;
    }

    public CemiRemitToSupplierBoFactory withSupplier(final CemiSupplierBo supplier) {
        this.supplier = supplier;
        return this;
    }

    public CemiRemitToSupplierBoFactory withOptionalEmailAddress(final String emailAddress) {
        this.emailAddress = StringUtils.defaultString(emailAddress);
        return this;
    }

    public CemiRemitToSupplierBoFactory withRemitIndex(final int remitIndexForSupplier) {
        this.remitIndexForSupplier = remitIndexForSupplier;
        return this;
    }

    public CemiRemitToSupplierBoFactory withDefaultConnectionFlag(final boolean defaultConnection) {
        this.defaultConnection = defaultConnection;
        return this;
    }

    public CemiRemitToSupplierBoFactory withMaskingFlag(final boolean maskSensitiveData) {
        // Does nothing yet; being left here as a hook if we need to update the connection name
        // to use a portion of the bank account number.
        return this;
    }

    public CemiRemitToSupplierBo createCemiRemitToSupplierBo() {
        Validate.validState(ObjectUtils.isNotNull(supplierAddress), "Supplier Address BO cannot be null");
        Validate.validState(ObjectUtils.isNotNull(supplier), "Supplier BO cannot be null");
        Validate.validState(remitIndexForSupplier > 0, "Remittance index must be greater than zero");

        final CemiRemitToSupplierBo remitToSupplier = new CemiRemitToSupplierBo();
        final String connectionName = generateConnectionName();
        final String connectionId = generateConnectionId(connectionName);
        final String isDefault = CemiUtils.convertToBooleanValueForFileExtract(defaultConnection);
        final String alwaysSeparatePayments = CemiUtils.convertToBooleanValueForFileExtract(false);

        remitToSupplier.setSupplierId(supplier.getSupplierId());
        remitToSupplier.setSupplierConnectionId(connectionId);
        remitToSupplier.setSupplierConnectionName(connectionName);
        remitToSupplier.setDefaultPaymentType(supplier.getDefaultPaymentType());
        remitToSupplier.setAcceptedPaymentType1(supplier.getPaymentTypesAccepted1());
        remitToSupplier.setAcceptedPaymentType2(supplier.getPaymentTypesAccepted2());
        remitToSupplier.setAcceptedPaymentType3(supplier.getPaymentTypesAccepted3());
        remitToSupplier.setSettlementBankAccount(KFSConstants.EMPTY_STRING);
        remitToSupplier.setRemitToAddressId(supplierAddress.getAddressId());
        remitToSupplier.setRemitToEmailAddress(emailAddress);
        remitToSupplier.setPayeeAlternateName(KFSConstants.EMPTY_STRING);
        remitToSupplier.setAlternateNameUsage(KFSConstants.EMPTY_STRING);
        remitToSupplier.setPaymentMemo(CemiVendorConstants.ITHACA_PAYMENT_MEMO);
        remitToSupplier.setIsDefault(isDefault);
        remitToSupplier.setDefaultPaymentTerms(KFSConstants.EMPTY_STRING);
        remitToSupplier.setAlwaysSeparatePayments(alwaysSeparatePayments);

        return remitToSupplier;
    }

    private String generateConnectionName() {
        return MessageFormat.format(CemiVendorConstants.SUPPLIER_REMIT_TO_CONNECTION_NAME_FORMAT,
                supplierAddress.getAddressLine1(), supplier.getDefaultPaymentType());
    }

    private String generateConnectionId(final String connectionName) {
        return MessageFormat.format(CemiVendorConstants.SUPPLIER_REMIT_TO_CONNECTION_ID_FORMAT,
                supplier.getSupplierId(), connectionName, remitIndexForSupplier);
    }

}
