package edu.cornell.kfs.kew.mail.service.impl;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.mail.EmailContent;
import org.kuali.kfs.core.impl.config.property.Config;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionitem.ActionItemActionListExtension;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.mail.service.impl.EmailContentServiceImpl;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kew.CuKewConstants;
import edu.cornell.kfs.kew.CuKewParameterConstants;
import edu.cornell.kfs.kew.mail.service.CuEmailContentService;

public class CuEmailContentServiceImpl extends EmailContentServiceImpl implements CuEmailContentService {

    private static final Logger LOG = LogManager.getLogger();

    private ParameterService parameterService;

    @Override
    public EmailContent generateImmediateReminder(Person user, ActionItem actionItem, DocumentType documentType) {
        updateEmailEnvironmentIfNotRunningInProduction();
        return super.generateImmediateReminder(user, actionItem, documentType);
    }

    @Override
    public EmailContent generateWeeklyReminder(Person user, Collection<ActionItemActionListExtension> actionItems) {
        updateEmailEnvironmentIfNotRunningInProduction();
        return super.generateWeeklyReminder(user, actionItems);
    }

    @Override
    public EmailContent generateDailyReminder(Person user, Collection<ActionItemActionListExtension> actionItems) {
        updateEmailEnvironmentIfNotRunningInProduction();
        return super.generateDailyReminder(user, actionItems);
    }

    private void updateEmailEnvironmentIfNotRunningInProduction() {
        if (isProductionEnvironment()) {
            return;
        }
        if (isProductionWorkflowEmailModeEnabled()) {
            setWorkflowEmailDeploymentEnvironmentFromProperty(Config.PROD_ENVIRONMENT_CODE);
        } else {
            setWorkflowEmailDeploymentEnvironmentFromProperty(Config.ENVIRONMENT);
        }
    }

    private boolean isProductionEnvironment() {
        return ConfigContext.getCurrentContextConfig().isProductionEnvironment();
    }

    private void setWorkflowEmailDeploymentEnvironmentFromProperty(String propertyName) {
        String environmentCode = ConfigContext.getCurrentContextConfig().getProperty(propertyName);
        setDeploymentEnvironment(environmentCode);
    }

    @Override
    public boolean isProductionWorkflowEmailModeEnabled() {
        if (isProductionEnvironment()) {
            LOG.warn("isProductionWorkflowEmailModeEnabled, This method is meant for non-Production use only; "
                    + "ideally it should not be invoked when KFS is running in a Production environment.");
            return true;
        }
        String workflowEmailMode = parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.WORKFLOW, KRADConstants.DetailTypes.ACTION_LIST_DETAIL_TYPE,
                CuKewParameterConstants.NON_PRODUCTION_WORKFLOW_EMAIL_MODE);

        switch (StringUtils.defaultIfBlank(workflowEmailMode, KFSConstants.EMPTY_STRING)) {
            case CuKewConstants.TEST_WORKFLOW_EMAIL_MODE :
                LOG.debug("isProductionWorkflowEmailModeEnabled, Production-style emails are currently DISABLED.");
                return false;
            case CuKewConstants.PROD_WORKFLOW_EMAIL_MODE :
                LOG.debug("isProductionWorkflowEmailModeEnabled, Production-style emails are currently ENABLED.");
                return true;
            default :
                LOG.warn("isProductionWorkflowEmailModeEnabled, invalid or nonexistent value detected for parameter "
                        + CuKewParameterConstants.NON_PRODUCTION_WORKFLOW_EMAIL_MODE
                        + "; Production-style workflow email mode will be disabled by default.");
                return false;
        }
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
