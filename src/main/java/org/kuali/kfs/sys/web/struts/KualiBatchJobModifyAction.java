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
package org.kuali.kfs.sys.web.struts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kns.util.CookieUtils;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.rest.resource.requests.BatchJobStatusRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.kfs.sys.CUKFSConstants;

/*
 * CU Customization: Fixed an issue that was preventing the API calls from honoring the backdoor login state.
 */
public class KualiBatchJobModifyAction extends KualiAction {

    private static final Logger LOG = LogManager.getLogger();

    private static final String JOB_NAME_PARAMETER = "name";
    private static final String JOB_GROUP_PARAMETER = "group";
    private static final String START_STEP_PARAMETER = "startStep";
    private static final String END_STEP_PARAMETER = "endStep";
    private static final String START_TIME_PARAMETER = "startTime";
    private static final String EMAIL_PARAMETER = "emailAddress";
    private static final String BATCH_JOB_STATUS_ENDPOINT = "/api/business-objects/BatchJobStatus";

    private static ConfigurationService configurationService;
    private static DateTimeService dateTimeService;
    private static ParameterService parameterService;
    private static PermissionService permissionService;
    private static SchedulerService schedulerService;

    @Override
    protected void checkAuthorization(final ActionForm form, final String methodToCall) {
        if (form instanceof KualiBatchJobModifyForm) {
            if (!getPermissionService().isAuthorizedByTemplate(
                    GlobalVariables.getUserSession().getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                    KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                    KRADUtils.getNamespaceAndComponentSimpleName(BatchJobStatus.class),
                    new HashMap<>(getRoleQualification(form, "use")))) {
                throw new AuthorizationException(GlobalVariables.getUserSession().getUserToLog(), "view",
                        "batch jobs");
            }
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
     */
    protected boolean canModifyJob(final KualiBatchJobModifyForm form, final String actionType) {
        final Map<String, String> permissionDetails = new HashMap<>();
        permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, form.getJob().getNamespaceCode());
        permissionDetails.put(KimConstants.AttributeConstants.BEAN_NAME, form.getJob().getName());
        return getPermissionService().isAuthorizedByTemplate(
                GlobalVariables.getUserSession().getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                KFSConstants.PermissionTemplate.MODIFY_BATCH_JOB.name, permissionDetails,
                new HashMap<>(getRoleQualification(form, actionType)));
    }

    protected void checkJobAuthorization(final KualiBatchJobModifyForm form, final String actionType) {
        if (!canModifyJob(form, actionType)) {
            throw new AuthorizationException(GlobalVariables.getUserSession().getUserToLog(), "actionType",
                    form.getJob().getName());
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
            final BatchJobStatus job = getSchedulerService().getJob(jobGroup, jobName);
            getBatchJobStatus(request, jobGroup, jobName).ifPresent(job::setStatus);
            ((KualiBatchJobModifyForm) form).setJob(job);
        }
        return super.execute(mapping, form, request, response);
    }

    private static Optional<String> getBatchJobStatus(
            final HttpServletRequest request, final String jobGroup, final String jobName
    ) {
        // CU Customization: Append the backdoor ID to the URL (or an empty string if no backdoor is in use).
        final String optionalBackdoorIdFragment = buildBackdoorIdUrlParameterIfNecessary(
                request, CUKFSConstants.AMPERSAND);
        final String uri = getConfigurationService().getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY)
                           + BATCH_JOB_STATUS_ENDPOINT + "?name=" + jobName + "&group=" + jobGroup
                           + optionalBackdoorIdFragment;
        final String webClientResponse;
        try {
            webClientResponse = WebClient.create()
                    .get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, buildAuthHeader(request))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LOG.debug("getBatchJobStatus() - WebClient response: {}", webClientResponse);
        } catch (final WebClientResponseException e) {
            LOG.atError().withThrowable(e).log("getBatchJobStatus(...) - REST Client error, {}", e::getMessage);
            throw e;
        }

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(webClientResponse);
        } catch (final JsonProcessingException e) {
            LOG.error("getBatchJobStatus() - failed to get BatchJobStatus for {}", jobName);
            throw new RuntimeException(e);
        }

        final JsonNode statusNode = jsonNode.get("data").get(0).get("status");
        if (statusNode.isNull()) {
            return Optional.empty();
        }
        return Optional.of(statusNode.asText());
    }

    /*
     * CU Customization: Added a custom method to build a "backdoorId=value" URL fragment if necessary.
     * Returns an empty string if a backdoor user is not currently in use.
     */
    private static String buildBackdoorIdUrlParameterIfNecessary(
            final HttpServletRequest request, final String parameterSeparator) {
        final UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
        if (userSession != null && userSession.isBackdoorInUse()) {
            final String backdoorPrincipalName = userSession.getPrincipalName();
            return StringUtils.join(parameterSeparator, KRADConstants.BACKDOOR_PARAMETER,
                    CUKFSConstants.EQUALS_SIGN, backdoorPrincipalName);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private static String buildAuthHeader(final HttpServletRequest request) {
        final CookieUtils cookieUtil = new CookieUtils();
        final Optional<String> finAuthToken = cookieUtil.getFinancialsAuthToken(request);
        return KFSConstants.AUTHORIZATION_PREFIX + finAuthToken.orElseThrow(() -> {
            LOG.error("buildAuthHeader() - Invalid Financials Auth Token");
            return new RuntimeException("Invalid Financials Auth Token");
        });
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

        final BatchJobStatus job = batchModifyForm.getJob();
        final int startStep = Integer.parseInt(request.getParameter(START_STEP_PARAMETER));
        final int endStep = Integer.parseInt(request.getParameter(END_STEP_PARAMETER));

        final BatchJobStatusRequest batchJobStatusRequest = new BatchJobStatusRequest();
        batchJobStatusRequest.setGroup(job.getGroup());
        batchJobStatusRequest.setName(job.getName());
        batchJobStatusRequest.setStartStep(startStep);
        batchJobStatusRequest.setEndStep(endStep);
        batchJobStatusRequest.setEmailRecipient(request.getParameter(EMAIL_PARAMETER));
        batchJobStatusRequest.setStartTimestamp(request.getParameter(START_TIME_PARAMETER));

        // CU Customization: Append the backdoor ID to the URL (or an empty string if no backdoor is in use).
        final String optionalBackdoorIdFragment = buildBackdoorIdUrlParameterIfNecessary(
                request, KFSConstants.QUESTION_MARK);
        final String uri = getConfigurationService().getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY)
                           + BATCH_JOB_STATUS_ENDPOINT + optionalBackdoorIdFragment;
        try {
            final String webClientResponse = WebClient.create()
                    .post()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, buildAuthHeader(request))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(batchJobStatusRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            LOG.debug("runJob() - WebClient response: {}", webClientResponse);
        } catch (final WebClientResponseException e) {
            LOG.atError().withThrowable(e).log("runJob(...) - REST Client error, {}", e::getMessage);
            throw e;
        }

        // redirect to display form to prevent re-execution of the job by mistake
        return getForward(job);
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
        return new ActionForward(getConfigurationService().getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY) + "/batchModify.do?methodToCall=start&name=" + UrlFactory
                .encode(job.getName()) + "&group=" + UrlFactory.encode(job.getGroup()), true);
    }

    private static ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

    public static DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        return dateTimeService;
    }

    public static ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
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
}
