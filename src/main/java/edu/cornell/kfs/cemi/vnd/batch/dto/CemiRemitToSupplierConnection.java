package edu.cornell.kfs.cemi.vnd.batch.dto;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;

/**
 * DTO representing a Remit To Supplier Connection row for the CEMI Remit To Supplier extract.
 * 
 * Based on the simpler "Remit_To_Supplier" sheet with 14 columns:
 * - Supplier (Required)
 * - Supplier_Connection_Name (Required)
 * - Default_Payment_Type (Required)
 * - Accepted_Payment_Type_1 (Required, may have multiples)
 * - Accepted_Payment_Type_2 (Optional)
 * - Accepted_Payment_Type_3 (Optional)
 * - Settlement_Bank_Account (Conditionally Required - if payment types include electronic payments)
 * - Remit_To_Address_ID (Required)
 * - Remit_To_Email_Address (Optional)
 * - Payee_Alternate_Name (Conditionally Required - if Alternate_Name_Usage is provided)
 * - Alternate_Name_Usage (Conditionally Required - if Payee_Alternate_Name is provided)
 * - Is_Default (Required - first line for Supplier must be true, all remaining must be false)
 * - Default_Payment_Terms (Optional - defaults to overall Supplier payment terms if left blank)
 * - Always_Separate_Payments (Optional)
 */
public class CemiRemitToSupplierConnection {

    public static final String SUPPLIER_CONNECTION_ID_FORMAT = "{0}_{1}";
    public static final int MAX_ACCEPTED_PAYMENT_TYPES = 3;

    private final String supplierId;
    private final String supplierConnectionName;
    private final String defaultPaymentType;
    private final List<String> acceptedPaymentTypes;
    private final String settlementBankAccount;
    private final String remitToAddressId;
    private final String remitToEmailAddress;
    private final String payeeAlternateName;
    private final String alternateNameUsage;
    private final String isDefault;
    private final String defaultPaymentTerms;
    private final String alwaysSeparatePayments;

    /**
     * Constructor for CemiRemitToSupplierConnection.
     * 
     * @param supplierId The Supplier ID (e.g., "SUPP000001")
     * @param supplierConnectionName The connection name (displayed during invoice creation)
     * @param defaultPaymentType The default payment type (e.g., "Check", "EFT", "WIRE")
     * @param acceptedPaymentTypes List of accepted payment types (up to 3)
     * @param settlementBankAccount The settlement bank account ID (required for electronic payments)
     * @param remitToAddressId The remit-to address ID
     * @param remitToEmailAddress The remit-to email address (optional)
     * @param payeeAlternateName The payee alternate name (optional)
     * @param alternateNameUsage The alternate name usage (required if payeeAlternateName provided)
     * @param isDefault Whether this is the default connection for the supplier
     * @param defaultPaymentTerms The payment terms (optional, defaults to supplier's terms)
     * @param alwaysSeparatePayments Whether to always separate payments
     */
    public CemiRemitToSupplierConnection(
            final String supplierId,
            final String supplierConnectionName,
            final String defaultPaymentType,
            final List<String> acceptedPaymentTypes,
            final String settlementBankAccount,
            final String remitToAddressId,
            final String remitToEmailAddress,
            final String payeeAlternateName,
            final String alternateNameUsage,
            final boolean isDefault,
            final String defaultPaymentTerms,
            final boolean alwaysSeparatePayments) {
        
        this.supplierId = supplierId;
        this.supplierConnectionName = supplierConnectionName;
        this.defaultPaymentType = defaultPaymentType;
        this.acceptedPaymentTypes = CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                MAX_ACCEPTED_PAYMENT_TYPES, acceptedPaymentTypes.toArray(String[]::new));
        this.settlementBankAccount = StringUtils.defaultString(settlementBankAccount);
        this.remitToAddressId = remitToAddressId;
        this.remitToEmailAddress = StringUtils.defaultString(remitToEmailAddress);
        this.payeeAlternateName = StringUtils.defaultString(payeeAlternateName);
        this.alternateNameUsage = StringUtils.defaultString(alternateNameUsage);
        this.isDefault = CemiUtils.convertToBooleanValueForFileExtract(isDefault);
        this.defaultPaymentTerms = StringUtils.defaultString(defaultPaymentTerms);
        this.alwaysSeparatePayments = alwaysSeparatePayments 
                ? CemiUtils.convertToBooleanValueForFileExtract(true) 
                : KFSConstants.EMPTY_STRING;
    }

    /**
     * Factory method to create a CemiRemitToSupplierConnection with minimal required fields
     * and sensible defaults.
     */
    public static CemiRemitToSupplierConnection createWithDefaults(
            final String supplierId,
            final String supplierConnectionName,
            final String remitToAddressId,
            final boolean isDefault) {
        return new CemiRemitToSupplierConnection(
                supplierId,
                supplierConnectionName,
                CemiVendorConstants.DEFAULT_PAYMENT_TYPE,
                List.of(CemiVendorConstants.DEFAULT_PAYMENT_TYPE),
                KFSConstants.EMPTY_STRING,
                remitToAddressId,
                KFSConstants.EMPTY_STRING,
                KFSConstants.EMPTY_STRING,
                KFSConstants.EMPTY_STRING,
                isDefault,
                KFSConstants.EMPTY_STRING,
                false);
    }

    /**
     * Generates a connection name based on supplier name and address line.
     * Recommended format per the template documentation.
     */
    public static String buildConnectionName(final String supplierName, final String addressLine1) {
        if (StringUtils.isNotBlank(addressLine1)) {
            return MessageFormat.format(SUPPLIER_CONNECTION_ID_FORMAT, supplierName, addressLine1);
        }
        return supplierName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierConnectionName() {
        return supplierConnectionName;
    }

    public String getDefaultPaymentType() {
        return defaultPaymentType;
    }

    public List<String> getAcceptedPaymentTypes() {
        return acceptedPaymentTypes;
    }

    public String getSettlementBankAccount() {
        return settlementBankAccount;
    }

    public String getRemitToAddressId() {
        return remitToAddressId;
    }

    public String getRemitToEmailAddress() {
        return remitToEmailAddress;
    }

    public String getPayeeAlternateName() {
        return payeeAlternateName;
    }

    public String getAlternateNameUsage() {
        return alternateNameUsage;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public String getDefaultPaymentTerms() {
        return defaultPaymentTerms;
    }

    public String getAlwaysSeparatePayments() {
        return alwaysSeparatePayments;
    }

}