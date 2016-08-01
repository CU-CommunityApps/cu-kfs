package edu.cornell.kfs.fp.document.web.struts;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.RecurringDisbursementVoucherPDPStatus;
import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentService;

public class RecurringDisbursementVoucherForm extends CuDisbursementVoucherForm {
	
	private static final long serialVersionUID = 7035540080454973823L;
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecurringDisbursementVoucherForm.class);
	
	private transient List<RecurringDisbursementVoucherPDPStatus> pdpStatuses;
	private transient RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService;
	
	@Override
	public String getDocTypeName() {
		return CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME;
	}
	
	@Override
    protected String getDefaultDocumentTypeName() {
        return CuFPConstants.RecurringDisbursementVoucherDocumentConstants.RECURRING_DV_DOCUMENT_TYPE_NAME;
    }
	
	@Override
	public String getTripUrl() {
		return StringUtils.EMPTY;
	}
	
	@Override
	public String getTripID() {
		return StringUtils.EMPTY;
	}
	
	public boolean isRecurringDVDetailsDefaultOpen() {
		return !((RecurringDisbursementVoucherDocument)this.getDocument()).getRecurringDisbursementVoucherDetails().isEmpty();
	}
	
	public boolean isPreDisbursementProcessorTabDefaultOpen() {
	    return !getPdpStatuses().isEmpty();
	}

    public List<RecurringDisbursementVoucherPDPStatus> getPdpStatuses() {
        if(pdpStatuses == null) {
            pdpStatuses = getRecurringDisbursementVoucherDocumentService().findPdpStatuses((RecurringDisbursementVoucherDocument)getDocument());
        }
        return pdpStatuses;
    }

    public void setPdpStatuses(List<RecurringDisbursementVoucherPDPStatus> pdpStatuses) {
        this.pdpStatuses = pdpStatuses;
    }
    
    public RecurringDisbursementVoucherDocumentService getRecurringDisbursementVoucherDocumentService() {
        if (recurringDisbursementVoucherDocumentService == null) {
            recurringDisbursementVoucherDocumentService = SpringContext.getBean(RecurringDisbursementVoucherDocumentService.class);
        }
        return recurringDisbursementVoucherDocumentService;
    }

    public void setRecurringDisbursementVoucherDocumentService(
            RecurringDisbursementVoucherDocumentService recurringDisbursementVoucherDocumentService) {
        this.recurringDisbursementVoucherDocumentService = recurringDisbursementVoucherDocumentService;
    }

}
