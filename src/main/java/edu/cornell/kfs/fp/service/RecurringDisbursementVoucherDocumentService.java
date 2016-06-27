package edu.cornell.kfs.fp.service;

import java.util.List;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
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
}
