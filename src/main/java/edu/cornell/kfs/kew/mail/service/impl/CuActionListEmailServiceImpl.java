package edu.cornell.kfs.kew.mail.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.kew.mail.service.impl.CustomizableActionListEmailServiceImpl;

public class CuActionListEmailServiceImpl extends CustomizableActionListEmailServiceImpl {

    @Override
    protected boolean isProduction() {
        return super.isProduction() || shouldForceProductionBehavior();
    }

    protected boolean shouldForceProductionBehavior() {
        String actionListEmailEnvironment = getDeploymentEnvironment();
        String prodEnvironmentCode = ConfigContext.getCurrentContextConfig().getProductionEnvironmentCode();
        return StringUtils.isNotBlank(prodEnvironmentCode)
                && StringUtils.equalsIgnoreCase(actionListEmailEnvironment, prodEnvironmentCode);
    }

}
