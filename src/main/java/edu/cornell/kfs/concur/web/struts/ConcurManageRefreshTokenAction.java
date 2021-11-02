package edu.cornell.kfs.concur.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigContext;

import edu.cornell.kfs.concur.web.struts.form.ConcurManageRefreshTokenForm;

@SuppressWarnings("deprecation")
public class ConcurManageRefreshTokenAction extends KualiAction {
    private static final Logger LOG = LogManager.getLogger();
    
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("start, entering");
        updateFormValues((ConcurManageRefreshTokenForm) form);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected void updateFormValues(ConcurManageRefreshTokenForm concurTokenForm) {
    }
    
    protected boolean isProduction() {
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (LOG.isDebugEnabled()) {
            LOG.debug("isProduction, isProd: " + isProd);
        }
        return isProd;
    }

}
