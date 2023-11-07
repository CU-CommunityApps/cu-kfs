/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.pdp.web.struts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpKeyConstants;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.FormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.FormatSelection;
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.pdp.service.FormatService;
import org.kuali.kfs.pdp.service.PdpAuthorizationService;
import org.kuali.kfs.pdp.service.impl.exception.FormatException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * This class provides actions for the format process
 */
public class FormatAction extends KualiAction {

    protected FormatService formatService;

    public FormatAction() {
        formatService = SpringContext.getBean(FormatService.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        PdpAuthorizationService authorizationService = SpringContext.getBean(PdpAuthorizationService.class);

        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        String methodToCall = findMethodToCall(form, request);

        if (!authorizationService.hasFormatPermission(kualiUser.getPrincipalId())) {
            throw new AuthorizationException(kualiUser.getPrincipalName(), methodToCall, kualiUser.getCampusCode());
        }

        return super.execute(mapping, form, request, response);
    }

    /**
     * This method prepares the data for the format process
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        FormatSelection formatSelection = formatService.getDataForFormat(kualiUser);
        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

        formatForm.setCampus(kualiUser.getCampusCode());

        // no data for format because another format process is already running
        if (formatSelection.getStartDate() != null) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS,
                    PdpKeyConstants.Format.ERROR_PDP_FORMAT_PROCESS_ALREADY_RUNNING,
                    dateTimeService.toDateTimeString(formatSelection.getStartDate()));
        } else {
            List<CustomerProfile> customers = formatSelection.getCustomerList();

            for (CustomerProfile element : customers) {
                if (formatSelection.getCampus().equals(element.getFormatCampusCode())) {
                    element.setSelectedForFormat(Boolean.TRUE);
                } else {
                    element.setSelectedForFormat(Boolean.FALSE);
                }
            }

            formatForm.setPaymentDate(dateTimeService.toDateString(dateTimeService.getCurrentTimestamp()));
            formatForm.setPaymentTypes(PdpConstants.PaymentTypes.ALL);
            formatForm.setCustomers(customers);
            formatForm.setRanges(formatSelection.getRangeList());
        }

        return mapping.findForward(PdpConstants.MAPPING_SELECTION);
    }

    /**
     * This method marks the payments for format
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward prepare(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);

        if (formatForm.getCampus() == null) {
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }

        // Figure out which ones they have selected
        List<CustomerProfile> selectedCustomers = new ArrayList<>();

        for (CustomerProfile customer : formatForm.getCustomers()) {
            if (customer.isSelectedForFormat()) {
                selectedCustomers.add(customer);
            }
        }

        Date paymentDate = dateTimeService.convertToSqlDate(formatForm.getPaymentDate());
        Person kualiUser = GlobalVariables.getUserSession().getPerson();

        FormatProcessSummary formatProcessSummary = formatService.startFormatProcess(kualiUser, formatForm.getCampus(),
                selectedCustomers, paymentDate, formatForm.getPaymentTypes());
        if (formatProcessSummary.getProcessSummaryList().size() == 0) {
            KNSGlobalVariables.getMessageList().add(PdpKeyConstants.Format.ERROR_PDP_NO_MATCHING_PAYMENT_FOR_FORMAT);
            return mapping.findForward(PdpConstants.MAPPING_SELECTION);
        }

        formatForm.setFormatProcessSummary(formatProcessSummary);

        return mapping.findForward(PdpConstants.MAPPING_CONTINUE);
    }

    /**
     * This method performs the format process.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward continueFormat(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;
        KualiInteger processId = formatForm.getFormatProcessSummary().getProcessId();

        try {
            formatService.performFormat(processId.intValue());
        }
        catch (FormatException e) {
            // errors added to global message map
            return mapping.findForward(PdpConstants.MAPPING_CONTINUE);
        }

        String lookupUrl = buildUrl(String.valueOf(processId.intValue()));
        return new ActionForward(lookupUrl, true);
    }

    /**
     * This method clears all the customer checkboxes.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        List<CustomerProfile> customers = formatForm.getCustomers();
        for (CustomerProfile customerProfile : customers) {
            customerProfile.setSelectedForFormat(false);
        }
        formatForm.setCustomers(customers);

        return mapping.findForward(PdpConstants.MAPPING_SELECTION);

    }

    /**
     * This method cancels the format process
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        FormatForm formatForm = (FormatForm) form;

        KualiInteger processId = formatForm.getFormatProcessSummary().getProcessId();

        if (processId != null) {
            formatService.clearUnfinishedFormat(processId.intValue());
        }
        return mapping.findForward(KRADConstants.MAPPING_PORTAL);

    }

    /**
     * This method clears the unfinished format process and is called from the FormatProcess lookup page.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward clearUnfinishedFormat(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String processIdParam = request.getParameter(PdpConstants.PROCESS_ID);
        Integer processId = Integer.parseInt(processIdParam);

        if (processId != null) {
            formatService.resetFormatPayments(processId);
        }

        return mapping.findForward(KRADConstants.MAPPING_PORTAL);
    }

    /**
     * This method builds the forward url for the format summary lookup page.
     *
     * @param processId the batch id
     * @return the built url
     */
    private String buildUrl(String processId) {
        String basePath = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY);
        String lookupUrl = StringUtils.join(basePath, CUKFSConstants.WebappPaths.LOOKUP,
                CUKFSConstants.SLASH, ProcessSummary.class.getSimpleName());
        Map<String, String> parameters = Map.ofEntries(
                Map.entry(PdpPropertyConstants.ProcessSummary.PROCESS_SUMMARY_PROCESS_ID, processId),
                Map.entry(KFSConstants.Search.SKIP, KFSConstants.ZERO),
                Map.entry(KFSConstants.Search.LIMIT, CUKFSConstants.NumericStrings.ONE_HUNDRED));
        return UrlFactory.parameterizeUrl(lookupUrl, parameters);
    }
}
