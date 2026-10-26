package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiRemitToSupplierFileRemitToSupplierTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileSupplierTabRowBo;

public class CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory {

    private CemiSupplierFileAddressesTabRowBo supplierAddress;
    private CemiSupplierFileSupplierTabRowBo supplier;
    private Optional<String> settlementBankAccountId;
    private Optional<String> emailAddress;
    private int remitIndexForSupplier;
    private boolean defaultConnection;

    public CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory(final CemiSupplierFileAddressesTabRowBo supplierAddress,
            final CemiSupplierFileSupplierTabRowBo supplier, final Optional<String> settlementBankAccountId,
            final Optional<String> emailAddress, final int remitIndexForSupplier, final boolean defaultConnection) {
        Validate.notNull(supplierAddress, "supplierAddress cannot be null");
        Validate.notNull(supplier, "supplier cannot be null");
        Validate.notNull(settlementBankAccountId, "settlementBankAccountId wrapper object cannot be null");
        Validate.notNull(emailAddress, "emailAddress wrapper object cannot be null");
        Validate.isTrue(remitIndexForSupplier > 0, "remitIndexForSupplier must be a positive integer");

        this.supplierAddress = supplierAddress;
        this.supplier = supplier;
        this.settlementBankAccountId = settlementBankAccountId;
        this.emailAddress = emailAddress;
        this.remitIndexForSupplier = remitIndexForSupplier;
        this.defaultConnection = defaultConnection;
    }

    public static CemiRemitToSupplierFileRemitToSupplierTabRowBo createTabRowBoFrom(
            final CemiSupplierFileAddressesTabRowBo supplierAddress,
            final CemiSupplierFileSupplierTabRowBo supplier, final Optional<String> settlementBankAccountId,
            final Optional<String> emailAddress, final int remitIndexForSupplier, final boolean defaultConnection) {
        final CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory factory =
                new CemiRemitToSupplierFileRemitToSupplierTabRowBoFactory(supplierAddress, supplier,
                        settlementBankAccountId, emailAddress, remitIndexForSupplier, defaultConnection);
        return factory.createCemiRemitToSupplierFileRemitToSupplierTabRowBo();
    }

    public CemiRemitToSupplierFileRemitToSupplierTabRowBo createCemiRemitToSupplierFileRemitToSupplierTabRowBo() {
        Validate.validState(ObjectUtils.isNotNull(supplierAddress), "Supplier Address BO cannot be null");
        Validate.validState(ObjectUtils.isNotNull(supplier), "Supplier BO cannot be null");
        Validate.validState(remitIndexForSupplier > 0, "Remittance index must be greater than zero");

        final CemiRemitToSupplierFileRemitToSupplierTabRowBo remitToSupplier = new CemiRemitToSupplierFileRemitToSupplierTabRowBo();
        final String connectionName = determineConnectionName();
        final String connectionId = determineConnectionId(connectionName);
        final String settlementBankAccount = settlementBankAccountId.orElse(CemiBaseConstants.EMPTY_STRING);
        final String remitToEmailAddress = emailAddress.orElse(CemiBaseConstants.EMPTY_STRING);
        final String isDefault = CemiUtils.convertToBooleanValueForFileExtract(defaultConnection);
        final String alwaysSeparatePayments = CemiUtils.convertToBooleanValueForFileExtract(false);

        remitToSupplier.setSupplierId(supplier.getSupplierId());
        remitToSupplier.setSupplierConnectionId(connectionId);
        remitToSupplier.setSupplierConnectionName(connectionName);
        remitToSupplier.setDefaultPaymentType(supplier.getDefaultPaymentType());
        remitToSupplier.setAcceptedPaymentType1(supplier.getPaymentTypesAccepted1());
        remitToSupplier.setAcceptedPaymentType2(supplier.getPaymentTypesAccepted2());
        remitToSupplier.setAcceptedPaymentType3(supplier.getPaymentTypesAccepted3());
        remitToSupplier.setSettlementBankAccount(settlementBankAccount);
        remitToSupplier.setRemitToAddressId(supplierAddress.getAddressId());
        remitToSupplier.setRemitToEmailAddress(remitToEmailAddress);
        remitToSupplier.setPayeeAlternateName(CemiBaseConstants.EMPTY_STRING);
        remitToSupplier.setAlternateNameUsage(CemiBaseConstants.EMPTY_STRING);
        remitToSupplier.setPaymentMemo(CemiRemitToSupplierConstants.ITHACA_PAYMENT_MEMO);
        remitToSupplier.setIsDefault(isDefault);
        remitToSupplier.setDefaultPaymentTerms(CemiBaseConstants.EMPTY_STRING);
        remitToSupplier.setAlwaysSeparatePayments(alwaysSeparatePayments);

        return remitToSupplier;
    }

    private String determineConnectionName() {
        return MessageFormat.format(CemiRemitToSupplierConstants.SUPPLIER_REMIT_TO_CONNECTION_NAME_FORMAT,
                supplierAddress.getAddressLine1(), supplier.getDefaultPaymentType());
    }

    private String determineConnectionId(final String connectionName) {
        return MessageFormat.format(CemiRemitToSupplierConstants.SUPPLIER_REMIT_TO_CONNECTION_ID_FORMAT,
                supplier.getSupplierId(), connectionName, remitIndexForSupplier);
    }

}
