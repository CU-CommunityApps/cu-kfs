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
package org.kuali.kfs.sys.web.struts;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KualiBatchJobModifyAction extends KualiAction {

    private static final String JOB_NAME_PARAMETER = "name";
    private static final String JOB_GROUP_PARAMETER = "group";
    private static final String START_STEP_PARAMETER = "startStep";
    private static final String END_STEP_PARAMETER = "endStep";
    private static final String START_TIME_PARAMETER = "startTime";
    private static final String EMAIL_PARAMETER = "emailAddress";

    private static SchedulerService schedulerService;
    private static ParameterService parameterService;
    private static DateTimeService dateTimeService;
    private static PermissionService permissionService;

    @Override
    protected void checkAuthorization(final ActionForm form, final String methodToCall) throws AuthorizationException {
        if (form instanceof KualiBatchJobModifyForm) {
            return;
        } else {
            super.checkAuthorization(form, methodToCall);
        }
    }

    /**
     * Performs the actual authorization check for a given job and action against the current user. This method can be
     * overridden by sub-classes if more granular controls are desired.
     *
     * @param form
     * @param actionType
     * @throws AuthorizationException
     */
    protected boolean canModifyJob(final KualiBatchJobModifyForm form, final String actionType) {
        final Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, form.getJob().getNamespaceCode());
        permissionDetails.put(KimConstants.AttributeConstants.BEAN_NAME, form.getJob().getName());
        return true;
    }

    protected void checkJobAuthorization(final KualiBatchJobModifyForm form, final String actionType) throws AuthorizationException {
        if (!canModifyJob(form, actionType)) {
            return;
        }
    }

    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        // load the given job and map into the form
        final String jobName = request.getParameter(JOB_NAME_PARAMETER);
        final String jobGroup = request.getParameter(JOB_GROUP_PARAMETER);
        if (form instanceof KualiBatchJobModifyForm) {
            ((KualiBatchJobModifyForm) form).setJob(getSchedulerService().getJob(jobGroup, jobName));
        }
        return super.execute(mapping, form, request, response);
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = SpringContext.getBean(PermissionService.class);
        }
        return permissionService;
    }

    private SchedulerService getSchedulerService() {
        if (schedulerService == null) {
            schedulerService = SpringContext.getBean(SchedulerService.class);
        }
        return schedulerService;
    }

    public static ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public ActionForward start(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchJobModifyForm batchModifyForm = (KualiBatchJobModifyForm) form;

        request.setAttribute("job", batchModifyForm.getJob());
        request.setAttribute("canRunJob", canModifyJob(batchModifyForm, "runJob"));
        request.setAttribute("canSchedule", canModifyJob(batchModifyForm, "schedule"));
        request.setAttribute("canUnschedule", canModifyJob(batchModifyForm, "unschedule"));
        request.setAttribute("canStopJob", canModifyJob(batchModifyForm, "stopJob"));
        request.setAttribute("userEmailAddress", GlobalVariables.getUserSession().getPerson().getEmailAddress());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward runJob(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchJobModifyForm batchModifyForm = (KualiBatchJobModifyForm) form;

        checkJobAuthorization(batchModifyForm, "runJob");

        final String startStepStr = request.getParameter(START_STEP_PARAMETER);
        final String endStepStr = request.getParameter(END_STEP_PARAMETER);
        final String startTimeStr = request.getParameter(START_TIME_PARAMETER);
        final String emailAddress = request.getParameter(EMAIL_PARAMETER);

        final int startStep = Integer.parseInt(startStepStr);
        final int endStep = Integer.parseInt(endStepStr);
        final Date startTime;
        if (StringUtils.isNotBlank(startTimeStr)) {
            startTime = getDateTimeService().convertToDateTime(startTimeStr);
        } else {
            startTime = getDateTimeService().getCurrentDate();
        }

        batchModifyForm.getJob().runJob(startStep, endStep, startTime, emailAddress);

        // redirect to display form to prevent re-execution of the job by mistake
        return getForward(batchModifyForm.getJob());
    }

    public ActionForward stopJob(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchJobModifyForm batchModifyForm = (KualiBatchJobModifyForm) form;

        checkJobAuthorization(batchModifyForm, "stopJob");

        batchModifyForm.getJob().interrupt();

        return getForward(batchModifyForm.getJob());
    }

    public ActionForward schedule(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchJobModifyForm batchModifyForm = (KualiBatchJobModifyForm) form;

        checkJobAuthorization(batchModifyForm, "schedule");

        batchModifyForm.getJob().schedule();

        return getForward(batchModifyForm.getJob());
    }

    public ActionForward unschedule(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchJobModifyForm batchModifyForm = (KualiBatchJobModifyForm) form;

        checkJobAuthorization(batchModifyForm, "unschedule");

        batchModifyForm.getJob().unschedule();

        // move to the unscheduled job object since the scheduled one has been removed
        batchModifyForm.setJob(getSchedulerService().getJob(SchedulerService.UNSCHEDULED_GROUP,
                batchModifyForm.getJob().getName()));

        return getForward(batchModifyForm.getJob());
    }

    private ActionForward getForward(final BatchJobStatus job) {
        return new ActionForward(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY) + "/batchModify.do?methodToCall=start&name=" + UrlFactory
                .encode(job.getName()) + "&group=" + UrlFactory.encode(job.getGroup()), true);
    }

    public static DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }
}
