/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.Job;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CU Customization: Fixed a bug related to the creation of .error files when a batch step throws an exception.
 * 
 * BatchStepExecutor executes a Step in its own Thread and writes either a .success or .error file after execution.
 * This class notifies the ContainerStepListener when a Step has started and when it has completed.
 * <p>
 * BatchStepExecutor adds a ConsoleAppender to its Logger if one hasn't been configured.
 */
public class BatchStepExecutor implements Runnable {

    private static final Logger LOG = LogManager.getLogger();

    private final ParameterService parameterService;
    private final DateTimeService dateTimeService;
    private final BatchContainerDirectory batchContainerDirectory;
    private final BatchStepFileDescriptor batchStepFile;
    private final Step step;
    private final int stepIndex;

    private String logFileName;

    private final List<ContainerStepListener> containerStepListeners;

    /**
     * @param parameterService        the ParameterService used by Job
     * @param dateTimeService         the DateTimeService used by Job
     * @param batchContainerDirectory the batch container directory
     * @param batchStepFile           the descriptor containing information about the step to execute
     * @param step                    the Step to execute
     * @param stepIndex               the index of the step in the job
     */
    public BatchStepExecutor(
            final ParameterService parameterService, final DateTimeService dateTimeService,
            final BatchContainerDirectory batchContainerDirectory, final BatchStepFileDescriptor batchStepFile, final Step step,
            final int stepIndex) {
        this.parameterService = parameterService;
        this.dateTimeService = dateTimeService;

        this.batchContainerDirectory = batchContainerDirectory;
        this.batchStepFile = batchStepFile;
        this.step = step;
        this.stepIndex = stepIndex;

        containerStepListeners = new ArrayList<>();

        LOG.info("Initialized thread executor for {}", batchStepFile);
    }

    /**
     * Execute the Step via Job.runStep(). Setup NDC logging so the Step has its own log file. Remove the NDC logging
     * once the step is finished executing. Notify the ContainerStepListeners when the step starts and finishes.
     */
    @Override
    public void run() {
        final Date stepRunDate = dateTimeService.getCurrentDate();
        batchStepFile.setStartedDate(stepRunDate);
        batchStepFile.setStepIndex(stepIndex);

        try (CloseableThreadContext.Instance ignored = setupNDCLogging()) {
            runWithLogging(stepRunDate);
            /*
             * CU Customization: Copied the contents of the "catch" block below this line into the runWithLogging()
             * method instead, and replaced the original contents below with warning-only error handling. 
             */
        } catch (final Exception throwable) {
            LOG.warn(
                    "{} threw {} either during setup, during cleanup, or while handling a different error. "
                            + "Look at the step log to see the details. throwable.getMessage(): {}",
                    () -> batchStepFile,
                    () -> throwable.getClass().getName(),
                    throwable::getMessage
            );
        }
    }

    private void runWithLogging(final Date stepRunDate) throws InterruptedException {
        notifyStepStarted();

        try {
            LOG.info("Running {}", batchStepFile);

            final boolean result = Job.runStep(parameterService, batchStepFile.getJobName(), stepIndex, step, stepRunDate);

            if (result) {
                LOG.info("Step returned true");
                batchContainerDirectory.writeBatchStepSuccessfulResultFile(batchStepFile);
            } else {
                LOG.info("Step returned false");
                batchContainerDirectory.writeBatchStepErrorResultFile(batchStepFile);
            }
            /*
             * CU Customization: Copied the run() method's original "catch" block contents to this spot instead.
             * That way, if the batch step throws an exception, KFS will create the .error file BEFORE running
             * the code that checks for .success or .error files.
             */
        } catch (final Exception throwable) {
            LOG.warn(
                    "{} threw {}. Look at the step log to see the details. throwable.getMessage(): {}",
                    () -> batchStepFile,
                    () -> throwable.getClass().getName(),
                    throwable::getMessage
            );
            batchContainerDirectory.writeBatchStepErrorResultFile(batchStepFile, throwable);
        } finally {

            notifyStepFinished();
        }
    }

    /**
     * Adds a ContainerStepListener for step start and completion notifications
     *
     * @param listener the ContainerStepListener
     */
    public void addContainerStepListener(final ContainerStepListener listener) {
        containerStepListeners.add(listener);
    }

    /**
     * Add a new appender and context to the NDC for this execution of the step
     */
    private CloseableThreadContext.Instance setupNDCLogging() {
        final String nestedDiagnosticContext = getNestedDiagnosticContext();
        logFileName = getLogFileName(nestedDiagnosticContext);

        return CloseableThreadContext.put(KFSConstants.BATCH_LOGGER_THREAD_CONTEXT_KEY, nestedDiagnosticContext);
    }

    /**
     * Constructs the name of the log file to write to for this execution of the step
     *
     * @param nestedDiagnosticContext the context returned by getNestedDiagnosticContext() for this step
     * @return the name of the log file
     */
    private String getLogFileName(final String nestedDiagnosticContext) {
        return getReportsDirectory()
            + File.separator
            + nestedDiagnosticContext + ".log";
    }

    /**
     * @return the nested diagnostic context string for this step's log file
     */
    @SuppressWarnings("unchecked")
    private String getNestedDiagnosticContext() {
        final Step unProxiedStep = (Step) ProxyUtils.getTargetIfProxied(step);
        final Class stepClass = unProxiedStep.getClass();
        final ModuleService module = getModuleService(stepClass);

        return getReportsDirectory() + File.separator + StringUtils.substringAfter(module.getModuleConfiguration()
                .getNamespaceCode(), "-").toLowerCase(Locale.US) + File.separator + step.getName() + "-" +
                dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate());
    }

    protected ModuleService getModuleService(final Class stepClass) {
        return SpringContext.getBean(KualiModuleService.class).getResponsibleModuleService(stepClass);
    }

    protected String getReportsDirectory() {
        return SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                KFSConstants.REPORTS_DIRECTORY_KEY);
    }

    /**
     * Notify the ContainerStepListeners that the Step has started
     */
    private void notifyStepStarted() {
        final String shortLogFileName = getShortLogFileName();

        for (final ContainerStepListener listener : containerStepListeners) {
            listener.stepStarted(batchStepFile, shortLogFileName);
        }
    }

    /**
     * Notify the ContainerStepListeners that the Step has completed
     */
    private void notifyStepFinished() {
        final BatchStepFileDescriptor resultFile = batchContainerDirectory.getResultFile(batchStepFile);
        resultFile.setCompletedDate(dateTimeService.getCurrentDate());
        resultFile.setStepIndex(stepIndex);

        final String shortLogFileName = getShortLogFileName();

        for (final ContainerStepListener listener : containerStepListeners) {
            listener.stepFinished(resultFile, shortLogFileName);
        }
    }

    /**
     * Returns just the name of the log file without the absolute path
     *
     * @return just the name of the log file (not the entire path)
     */
    private String getShortLogFileName() {
        String shortLogFileName = logFileName;

        final File logFile = new File(logFileName);
        if (logFile.exists()) {
            shortLogFileName = logFile.getName();
        }
        return shortLogFileName;
    }
}
