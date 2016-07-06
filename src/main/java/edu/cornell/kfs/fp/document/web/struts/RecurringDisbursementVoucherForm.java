package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.document.RecurringDisbursementVoucherDocument;

public class RecurringDisbursementVoucherForm extends CuDisbursementVoucherForm {
	
	private static final long serialVersionUID = 7035540080454973823L;
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RecurringDisbursementVoucherForm.class);
	protected static final String RECURRING_DV_DOCUMENT_TYPE_NAME = "RCDV";
	
	@Override
	public String getDocTypeName() {
		return RECURRING_DV_DOCUMENT_TYPE_NAME;
	}
	
	@Override
    protected String getDefaultDocumentTypeName() {
        return RECURRING_DV_DOCUMENT_TYPE_NAME;
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
		return !((RecurringDisbursementVoucherDocument)this.getDocument()).getSourceAccountingLines().isEmpty();
	}

}
