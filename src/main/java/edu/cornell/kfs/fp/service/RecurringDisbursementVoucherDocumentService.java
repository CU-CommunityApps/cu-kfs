package edu.cornell.kfs.fp.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherPDPStatus;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;

public interface RecurringDisbursementVoucherDocumentService {
	
	/**
	 * Returns a list of DisbursementVoucherDocuments that were generated from the Recurring DV, and it's scheduled accounting lines.
	 * @param recurringDisbursementVoucherDocument
	 * @return
	 */
	List<DisbursementVoucherDocument> generateDisbursementDocumentsFromRecurringDV(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument) throws WorkflowException;
	
	/**
	 * Updates the the recurring DV details.
	 * @param recurringDisbursementVoucherDocument
	 */
	void updateRecurringDisbursementVoucherDetails(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument);
	
	/**
	 * Finds the PDPstatus for all the DVs that the recurring DV has generated
	 * @param recurringDisbursementVoucherDocument
	 * @return
	 */
	List<RecurringDisbursementVoucherPDPStatus> findPdpStatuses(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument);
	
	/**
	 * Find all of the payment detail records associated with a recurring DV.
	 * @param recurringDisbursementVoucherDocument
	 * @return
	 */
	Collection<PaymentDetail> findPaymentDetailsFromRecurringDisbursementVoucher(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument);

	/**
	 * This function should be called by a batch process.
	 * Automatically approve all saved disbursement voucher documents with a
	 * payment date in the current or past fiscal period spawned by the
	 * recurring disbursement voucher document.
	 */
	void autoApproveDisbursementVouchersSpawnedByRecurringDvs();

    /**
     * Cancels all DVs spawned by the Recurring Disbursement Voucher that are in saved status.
     * @param recurringDisbursementVoucherDocument
     * @param cancelMessage
     * @return A Set<String> of DV document numbers that have been canceled.
     */
    Set<String> cancelSavedDisbursementVouchers(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage);

    /**
     * Cancels DVs that have been approved and finalized but haven't had their payment extracted yet.
     * @param recurringDisbursementVoucherDocument
     * @param cancelMessage
     * @return
     */
    Set<String> cancelDisbursementVouchersFinalizedNotExtracted(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage);

    /**
     * Cancels all PDP payments that are in open status there were created from the passed in recurring DV. 
     * @param recurringDisbursementVoucherDocument
     * @param cancelMessage
     * @return Set<String> of payment groups canceled.
     */
    Set<String> cancelOpenPDPPayments(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage);

    /**
     * Determines if the payment can be canceled from the Recurring DV by the user.
     * @param paymentDetailStatus
     * @return True if the payment can be canceled.  False if ii can't be.
     */
    boolean isPaymentCancelableByLoggedInUser(String paymentDetailStatus);
}
