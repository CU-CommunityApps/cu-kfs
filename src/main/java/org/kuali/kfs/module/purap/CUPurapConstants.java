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
}
