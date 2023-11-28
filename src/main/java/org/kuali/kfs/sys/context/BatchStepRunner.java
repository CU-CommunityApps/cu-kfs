/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.context;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.bo.Step;
import org.kuali.kfs.kns.web.struts.form.pojo.PojoPlugin;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchSpringContext;
import org.kuali.kfs.sys.batch.Job;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public final class BatchStepRunner {
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private BatchStepRunner() {
    }

    public static void main(final String[] args) {
        if (args.length < 1) {
            System.err.println("ERROR: You must pass the name of the step to run on the command line.");
            System.exit(8);
        }
        try {
            SpringContextForBatchRunner.initializeKfs();
            
            /*
             * CU Customization to initialize PojoPropertyUtilsBean for batch processing 
             */
            PojoPlugin.initBeanUtils();
            
            final String[] stepNames;
            if (args[0].indexOf(",") > 0) {
                stepNames = StringUtils.split(args[0], ",");
            } else {
                stepNames = new String[]{args[0]};
            }
            final ParameterService parameterService = SpringContext.getBean(ParameterService.class);
            final DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

            final String jobName = args.length >= 2 ? args[1] : KFSConstants.BATCH_STEP_RUNNER_JOB_NAME;
            final Date jobRunDate = dateTimeService.getCurrentDate();
            LOG.info("Executing job: {} steps: {}", () -> jobName, () -> Arrays.toString(stepNames));
            for (int i = 0; i < stepNames.length; ++i) {
                final Step step = BatchSpringContext.getStep(stepNames[i]);
                if (step != null) {
                    final Step unProxiedStep = (Step) ProxyUtils.getTargetIfProxied(step);
                    final Class<?> stepClass = unProxiedStep.getClass();

                    final ModuleService module = getModuleService(stepClass);
                    final String nestedDiagnosticContext =
                        getReportsDirectory() + File.separator +
                        StringUtils.substringAfter(module.getModuleConfiguration().getNamespaceCode(), "-").toLowerCase(
                                Locale.US)
                            + File.separator + step.getName()
                            + "-" + dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());
                    try (CloseableThreadContext.Instance ignored = CloseableThreadContext.put(
                            KFSConstants.BATCH_LOGGER_THREAD_CONTEXT_KEY,
                            nestedDiagnosticContext)
                    ) {
                        if (!Job.runStep(parameterService, jobName, i, step, jobRunDate)) {
                            System.exit(4);
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unable to find step: " + stepNames[i]);
                }
            }
            LOG.info("Finished executing job: {} steps: {}", () -> jobName, () -> Arrays.toString(stepNames));
            System.exit(0);
        } catch (final Throwable t) {
            System.err.println("ERROR: Exception caught: ");
            t.printStackTrace(System.err);
            System.exit(8);
        }
    }

    protected static ModuleService getModuleService(final Class<?> stepClass) {
        return SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService(stepClass);
    }

    protected static String getReportsDirectory() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.REPORTS_DIRECTORY_KEY);
    }

}
