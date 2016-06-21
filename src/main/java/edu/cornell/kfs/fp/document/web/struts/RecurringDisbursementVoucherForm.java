package edu.cornell.kfs.fp.document.web.struts;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecurringDisbursementVoucherForm extends CuDisbursementVoucherForm {
	protected static Log LOG = LogFactory.getLog(RecurringDisbursementVoucherForm.class);
	
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

}
