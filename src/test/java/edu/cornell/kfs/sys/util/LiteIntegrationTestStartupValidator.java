package edu.cornell.kfs.sys.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.context.PropertyLoadingFactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Utility class for integration tests that use "cu-spring-base-lite-integration-test-beans.xml",
 * to verify that an appropriate local KFS config file was loaded and that the VM argument
 * "-Doracle.jdbc.DateZeroTime" was specified. The latter setting should already be included
 * as part of the surefire plugin configuration in the cu-kfs POM. 
 * 
 * This bean is primarily for validating the execution of integration tests from within an IDE,
 * especially to verify that the run/debug configuration explicitly specifies a local config
 * properties file.
 */
public class LiteIntegrationTestStartupValidator implements InitializingBean {

    private String kfsDataSourceUrl;

    @Override
    public void afterPropertiesSet() throws Exception {
        checkKfsDataSourceUrl();
        checkDateZeroTimeSetting();
    }

    private void checkKfsDataSourceUrl() {
        Validate.validState(StringUtils.isNotBlank(kfsDataSourceUrl),
                "The KFS datasource URL was not initialized; has a local KFS config file been specified?");
        Validate.validState(!StringUtils.equalsIgnoreCase(kfsDataSourceUrl, "${kfs.datasource.url}"),
                "The KFS datasource URL placeholder was not filled; has a local KFS config file been specified?");
        Validate.validState(!StringUtils.containsIgnoreCase(kfsDataSourceUrl, "mysql"),
                "The KFS datasource URL may still be set to its default value; "
                        + "has a local KFS config file been specified?");
    }

    private void checkDateZeroTimeSetting() {
        final String dateZeroTimeSetting = PropertyLoadingFactoryBean.getBaseProperty("oracle.jdbc.DateZeroTime");
        Validate.validState(StringUtils.isNotBlank(dateZeroTimeSetting),
                "The Oracle Date-Zero-Time setting was not initialized; please check the VM arguments");
        Validate.validState(StringUtils.equalsIgnoreCase(dateZeroTimeSetting, "true"),
                "The Oracle Date-Zero-Time setting was not set to true; please check the VM arguments");
    }

    public void setKfsDataSourceUrl(final String kfsDataSourceUrl) {
        this.kfsDataSourceUrl = kfsDataSourceUrl;
    }

}
