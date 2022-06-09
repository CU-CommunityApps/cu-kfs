package edu.cornell.kfs.fp.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.COMPONENT;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants.NAMESPACE;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherDetail;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;

@NAMESPACE(namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL)
@COMPONENT(component = "RecurringDisbursementVoucher")
public class RecurringDisbursementVoucherDocument extends CuDisbursementVoucherDocument {

	private static final long serialVersionUID = 5578411133159987973L;
	private static final Logger LOG = LogManager.getLogger(RecurringDisbursementVoucherDocument.class);
	
	private List<RecurringDisbursementVoucherDetail> recurringDisbursementVoucherDetails;
	private transient String paymentCancelReason;
	
	@Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
		//Intentionally left blank as we don't need to create GLPE from a recurring DV
    }
	
	@Override
    protected void processExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail generalLedgerPendingEntryDetail, GeneralLedgerPendingEntry explicitEntry) {
		//Intentionally left blank as we don't need to create GLPE from a recurring DV
    }
	
	
	@Override
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail,
        GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
		//Intentionally left blank as we don't need to create GLPE from a recurring DV
        return true;
    }
	
	@Override
    protected boolean processOffsetGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
        GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry, GeneralLedgerPendingEntry offsetEntry) {
		//Intentionally left blank as we don't need to create GLPE from a recurring DV
		return true;
    }
	
	@Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
		LOG.info("called postProcessSave");
		super.doRouteStatusChange(statusChangeEvent);
		if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
        	try {
        		getRecurringDisbursementVoucherDocumentService().generateDisbursementDocumentsFromRecurringDV(this);
			} catch (WorkflowException e) {
				LOG.error("There was an error generating disbursement voucher doucments.", e);
				throw new RuntimeException(e);
			}
        }
	}
	
	public List<RecurringDisbursementVoucherDetail> getRecurringDisbursementVoucherDetails() {
		if (recurringDisbursementVoucherDetails == null) {
			recurringDisbursementVoucherDetails = new ArrayList<RecurringDisbursementVoucherDetail>();
		}
		return recurringDisbursementVoucherDetails;
	}
	
	public void setRecurringDisbursementVoucherDetails(
			List<RecurringDisbursementVoucherDetail> recurringDisbursementVoucherDetails) {
		this.recurringDisbursementVoucherDetails = recurringDisbursementVoucherDetails;
	}
	
	@Override
    public void toCopy() {
		setRecurringDisbursementVoucherDetails(new ArrayList<RecurringDisbursementVoucherDetail>());
		super.toCopy();
	}
	
	@Override
    public void prepareForSave() {
		if (!GlobalVariables.getMessageMap().hasErrors()) {
			getRecurringDisbursementVoucherDocumentService().updateRecurringDisbursementVoucherDetails(this);
		}
		super.prepareForSave();
		
	}

	public RecurringDisbursementVoucherDocumentService getRecurringDisbursementVoucherDocumentService() {
		return SpringContext.getBean(RecurringDisbursementVoucherDocumentService.class);
	}

    public String getPaymentCancelReason() {
        return paymentCancelReason;
    }

    public void setPaymentCancelReason(String paymentCancelReason) {
        this.paymentCancelReason = paymentCancelReason;
    }

}
