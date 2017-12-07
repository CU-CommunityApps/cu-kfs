package edu.cornell.kfs.pdp;

public final class CUPdpPropertyConstants {

    public static final String PAYEE_PRINCIPAL_NAME = "payeePrincipalName";

    public static final class PaymentDetail {
        public static final String PAYMENT_COUNTRY = "paymentGroup.country";
        
        private PaymentDetail() {
            throw new UnsupportedOperationException("do not call constructor");
        }
    }

    private CUPdpPropertyConstants() {
        throw new UnsupportedOperationException("do not call");
    }
}
