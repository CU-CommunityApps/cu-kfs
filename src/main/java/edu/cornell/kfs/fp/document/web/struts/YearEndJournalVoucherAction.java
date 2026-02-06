package edu.cornell.kfs.fp.document.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.document.web.struts.JournalVoucherAction;

public class YearEndJournalVoucherAction extends JournalVoucherAction {
	
	public YearEndJournalVoucherAction() {
		super();
	}
	
	@Override
	protected ActionForward processRouteOutOfBalanceDocumentConfirmationQuestion(
			ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		// we return null as we don't want pre rule to check if doc is out of balance, BR is going to check if doc is out of balance
		return null;
	}

}
