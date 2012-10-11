/**
 * 
 */
package org.kuali.kfs.module.purap;

import java.util.HashSet;

/**
 * Cornell University specific constants class for holding and defining constants necessary for Cornell's implementation of the Kuali Financial System.
 *
 */
public class CUPurapConstants extends PurapConstants {

    private static final long serialVersionUID = 1L;

    public static class PaymentRequestDefaults {
        public static final String DEFAULT_PROCESSING_CAMPUS_CODE = "IT";
    }
    
    public static final class CUPaymentRequestStatuses {

        public static final HashSet<String> STATUSES_ALLOWING_AUTO_CLOSE = new HashSet<String>();

        static {
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.DEPARTMENT_APPROVED);
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.AUTO_APPROVED);
            STATUSES_ALLOWING_AUTO_CLOSE.addAll(PaymentRequestStatuses.CANCELLED_STATUSES);
        }
    }
    
    public static final class IWantDocumentSteps {
        public static final String CUSTOMER_DATA_STEP = "customerDataStep";
        public static final String ITEMS_AND_ACCT_DATA_STEP = "itemAndAcctDataStep";
        public static final String VENDOR_STEP = "vendorStep";
        public static final String ROUTING_STEP = "routingStep";
        public static final String REGULAR = "regular";
    }
    
    public static final String USER_OPTIONS_DEFAULT_COLLEGE = "DEFAULT_COLLEGE";
    public static final String USER_OPTIONS_DEFAULT_DEPARTMENT = "DEFAULT_DEPARTMENT";
    

    
}
