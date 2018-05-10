package edu.cornell.kfs.pdp;

public final class CUPdpPropertyConstants {

    public static final String PAYEE_PRINCIPAL_NAME = "payeePrincipalName";
    public static final String PAYMENT_COUNTRY = "paymentGroup.country";
    public static final String BANK_ACCOUNT_TYPE_CODE = "bankAccountTypeCode";
    public static final String PAYEE_ACH_BANK_NAME = "bankRouting.bankName";

    private CUPdpPropertyConstants() {
        throw new UnsupportedOperationException("do not call");
    }
}
