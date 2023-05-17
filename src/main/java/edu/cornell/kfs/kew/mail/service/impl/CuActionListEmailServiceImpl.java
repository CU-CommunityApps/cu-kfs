package edu.cornell.kfs.kew.mail.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.mail.service.impl.CustomizableActionListEmailServiceImpl;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kew.CuKewConstants;
import edu.cornell.kfs.kew.CuKewParameterConstants;

public class CuActionListEmailServiceImpl extends CustomizableActionListEmailServiceImpl {

    private static final Logger LOG = LogManager.getLogger();

    private ParameterService parameterService;

    @Override
    protected boolean isProduction() {
        return super.isProduction() || shouldSimulateProductionWorkflowEmailBehavior();
    }

    protected boolean shouldSimulateProductionWorkflowEmailBehavior() {
        String workflowEmailMode = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.ACTION_LIST_DETAIL_TYPE,
                CuKewParameterConstants.NON_PRODUCTION_WORKFLOW_EMAIL_MODE);

        switch (StringUtils.defaultIfBlank(workflowEmailMode, KFSConstants.EMPTY_STRING)) {
            case CuKewConstants.TEST_WORKFLOW_EMAIL_MODE :
                LOG.debug("shouldSimulateProductionWorkflowEmailBehavior, Production-style emails are DISABLED.");
                return false;
            case CuKewConstants.PROD_WORKFLOW_EMAIL_MODE :
                LOG.debug("shouldSimulateProductionWorkflowEmailBehavior, Production-style emails are ENABLED.");
                return true;
            default :
                LOG.warn("shouldSimulateProductionWorkflowEmailBehavior, The value of parameter "
                        + CuKewParameterConstants.NON_PRODUCTION_WORKFLOW_EMAIL_MODE
                        + " is missing or invalid; Production-style emails will be disabled by default.");
                return false;
        }
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
        super.setParameterService(parameterService);
    }

}
