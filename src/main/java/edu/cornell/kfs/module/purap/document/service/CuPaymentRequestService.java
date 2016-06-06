package edu.cornell.kfs.module.purap.document.service;

import org.kuali.kfs.module.purap.document.service.PaymentRequestService;

public interface CuPaymentRequestService extends PaymentRequestService {

    /**
     * Returns the object ID of the given payment request's note target BO,
     * without loading the whole document. This allows for preserving the
     * KFS PaymentRequestView efficiency enhancement, while also allowing
     * for using objects aside from the doc header BO as the note target.
     * 
     * <p>See the specific implementations of this class for information
     * on which objects they use for the note targets.
     * 
     * @param documentNumber The PREQ's document number.
     * @return The payment request's note target object ID, or null if no such PREQ exists.
     */
    String getPaymentRequestNoteTargetObjectId(String documentNumber);

}
