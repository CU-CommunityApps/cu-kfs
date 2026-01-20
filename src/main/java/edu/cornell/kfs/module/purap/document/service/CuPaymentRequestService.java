package edu.cornell.kfs.module.purap.document.service;

import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestDto;
import edu.cornell.kfs.module.purap.rest.jsonObjects.PaymentRequestResultsDto;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
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

    /**
     * Determines whether a Payment Request's associated Purchase Order
     * is within the threshold to allow for automatic Payment Request approval.
     * 
     * @param document The Payment Request Document whose Purchase Order should be evaluated.
     * @return true if the Purchase Order's amount is within the limit for automatic Payment Request approval, false otherwise.
     */
    boolean purchaseOrderForPaymentRequestIsWithinAutoApproveAmountLimit(PaymentRequestDocument document);

    PaymentRequestDocument createPaymentRequestDocumentFromDto(PaymentRequestDto paymentRequestDto, PaymentRequestResultsDto results);

}
