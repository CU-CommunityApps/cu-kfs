package edu.cornell.kfs.module.purap.document.web.struts;

import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestItem;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestAction;
import org.kuali.kfs.module.purap.document.web.struts.PaymentRequestForm;

import edu.cornell.kfs.module.purap.businessobject.CuPaymentRequestItemExtension;

public class CuPaymentRequestAction extends PaymentRequestAction {

	@Override
	public ActionForward docHandler(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		ActionForward forward =  super.docHandler(mapping, form, request, response);
        PaymentRequestForm preqForm = (PaymentRequestForm) form;
        PaymentRequestDocument document = (PaymentRequestDocument) preqForm.getDocument();

		if (CollectionUtils.isNotEmpty(document.getItems())) {
			Collections.sort(document.getItems(), new Comparator() {
                public int compare(Object o1, Object o2) {                   
                    PaymentRequestItem item1 = (PaymentRequestItem) o1;
                    PaymentRequestItem item2 = (PaymentRequestItem) o2;
                    Integer inv1 = ((CuPaymentRequestItemExtension)item1.getExtension()).getInvLineNumber();
                    Integer inv2 = ((CuPaymentRequestItemExtension)item2.getExtension()).getInvLineNumber();
                    if (inv1 == null) {
                    	if (inv2 == null) {
                    		return -1;
                    	} else {
                    		return 1;
                    	}
                    } else {
                    	if (inv2 == null) {
                    		return -1;
                    	} else {
                    		return inv1.compareTo(inv2);
                    	}
                    }
                }
            });

	    }
        return forward;
    }
}
