package edu.cornell.kfs.sys.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;

public class CuEmailServiceImpl extends EmailServiceImpl {

    private static final String UNSPECIFIED_TENANT_MESSAGE = "No tenant specified";

    private final Environment environment;

    public CuEmailServiceImpl(final Environment environment) {
        super(environment);
        this.environment = environment;
    }

    /**
     * Overridden to exclude the "No tenant specified" message from the email subject.
     */
    @Override
    protected String modifyMessageSubject(final String subject) {
        final StringBuilder builder =
                new StringBuilder(KFSConstants.APPLICATION_NAMESPACE_CODE)
                        .append(" ");
        if (!StringUtils.equalsIgnoreCase(environment.getTenant(), UNSPECIFIED_TENANT_MESSAGE)) {
            builder.append(environment.getTenant())
                    .append(" ");
        }
        if (!environment.isProductionEnvironment()) {
            builder.append(environment.getLane());
        }
        return builder
                .append(": ")
                .append(subject)
                .toString();
    }

}
