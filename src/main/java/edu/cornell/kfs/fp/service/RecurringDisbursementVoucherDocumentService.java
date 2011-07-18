package edu.cornell.kfs.fp.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;

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
	 * Cancels all PDP payments that are in open status there were created from the passed in recurring DV. 
	 * @param recurringDisbursementVoucherDocument
	 * @param cancelMessage
	 * @return List of payment groups canceled.
	 */
	Set<String> cancelPDPPayments(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument, String cancelMessage);
	
	/**
	 * Find all of the payment detail records associated with a recurring DV.
	 * @param recurringDisbursementVoucherDocument
	 * @return
	 */
	Collection<PaymentDetail> findPaymentDetailsFromRecurringDisbursementVoucher(RecurringDisbursementVoucherDocument recurringDisbursementVoucherDocument);
	
	/**
	 * Determins if an individual payment can be canceled from a recurring DV.
	 * @param user
	 * @param paymentDetailStatus
	 * @return
	 */
	boolean isPaymentCancelable(Person user, String paymentDetailStatus);
}
