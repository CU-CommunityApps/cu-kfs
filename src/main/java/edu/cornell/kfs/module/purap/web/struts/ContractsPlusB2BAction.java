package edu.cornell.kfs.module.purap.web.struts;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.exception.B2BConnectionException;
import org.kuali.kfs.module.purap.web.struts.B2BForm;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CUPurapConstants.JaggaerRoleSet;
import edu.cornell.kfs.module.purap.document.service.CuB2BShoppingService;

public class ContractsPlusB2BAction extends KualiAction {
    private static final Logger LOG = LogManager.getLogger();
    
    public ActionForward contractsPlus(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        LOG.debug("contractsPlus, calling Contracts Plus");
        B2BForm b2bForm = (B2BForm) form;
        String url = getCuB2BShoppingService().getPunchOutUrlForRoleSet(GlobalVariables.getUserSession().getPerson(), JaggaerRoleSet.CONTRACTS_PLUS);

        if (ObjectUtils.isNull(url)) {
            throw new B2BConnectionException("Unable to connect to remote site for punchout.");
        }

        b2bForm.setShopUrl(url);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    protected CuB2BShoppingService getCuB2BShoppingService() {
        return SpringContext.getBean(CuB2BShoppingService.class);
    }
}
