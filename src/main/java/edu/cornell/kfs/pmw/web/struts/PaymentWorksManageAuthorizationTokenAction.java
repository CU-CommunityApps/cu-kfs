package edu.cornell.kfs.pmw.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceCallsService;

@SuppressWarnings("deprecation")
public class PaymentWorksManageAuthorizationTokenAction extends KualiAction {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksManageAuthorizationTokenAction.class);

    public ActionForward refreshToken(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            getPaymentWorksWebServiceCallsService().refreshPaymentWorksAuthorizationToken();
            GlobalVariables.getMessageMap().putInfo(KFSConstants.GLOBAL_MESSAGES, PaymentWorksKeyConstants.MESSAGE_AUTH_TOKEN_REFRESH_SUCCESS);
        } catch (RuntimeException e) {
            LOG.error("refreshToken(): An unexpected error occurred while refreshing PaymentWorks token", e);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, PaymentWorksKeyConstants.ERROR_AUTH_TOKEN_REFRESH_FAILURE);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected PaymentWorksWebServiceCallsService getPaymentWorksWebServiceCallsService() {
        return SpringContext.getBean(PaymentWorksWebServiceCallsService.class);
    }

}
