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
package org.kuali.kfs.kew.actionlist.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.kew.actionlist.ActionListFilter;
import org.kuali.kfs.kew.actionlist.service.ActionListService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.preferences.Preferences;
import org.kuali.kfs.kew.api.preferences.PreferencesService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CU Customization: Changes required for method that clears last modified date from and to values.
 * Extended class could not be used due to private method getUserSession.
 */

/**
 * Action for Action List Filter page.
 */
public class ActionListFilterAction extends KualiAction {

    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        request.setAttribute("preferences", getUserSession().retrieveObject(KewApiConstants.PREFERENCES));
        initForm(request, form);
        return super.execute(mapping, form, request, response);
    }

    public ActionForward start(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws
            Exception {
        final ActionListFilterForm filterForm = (ActionListFilterForm) form;
        final UserSession uSession = getUserSession();
        final ActionListFilter filter =
                (ActionListFilter) uSession.retrieveObject(KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME);
        if (filter != null) {
            if (StringUtils.isNotEmpty(filterForm.getDocTypeFullName())) {
                filter.setDocumentType(filterForm.getDocTypeFullName());
                uSession.addObject(KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME, filter);
                filterForm.setFilter(filter);
            } else {
                filterForm.setFilter(filter);
                filterForm.setDocTypeFullName(filter.getDocumentType());
            }
        }
        return mapping.findForward("viewFilter");
    }

    public ActionForward filter(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws
            Exception {
        final ActionListFilterForm filterForm = (ActionListFilterForm) form;
        //validate the filter through the actionitem/actionlist service (I'm thinking actionlistservice)
        final UserSession uSession = getUserSession();
        final ActionListFilter alFilter = filterForm.getLoadedFilter();
        if (StringUtils.isNotBlank(alFilter.getDelegatorId()) &&
                !KewApiConstants.DELEGATION_DEFAULT.equals(alFilter.getDelegatorId()) &&
                StringUtils.isNotBlank(alFilter.getPrimaryDelegateId()) &&
                !KewApiConstants.PRIMARY_DELEGATION_DEFAULT.equals(alFilter.getPrimaryDelegateId())) {
            // If the primary and secondary delegation drop-downs are both visible and are both set to non-default values,
            // then reset the secondary delegation drop-down to its default value.
            alFilter.setDelegatorId(KewApiConstants.DELEGATION_DEFAULT);
        }
        uSession.addObject(KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME, alFilter);
        if (GlobalVariables.getMessageMap().hasNoErrors()) {
            request.getSession().setAttribute(KewApiConstants.REQUERY_ACTION_LIST_KEY, "true");
            final ActionForward forward = mapping.findForward("viewActionList");
            return new ActionRedirect(forward);
        }
        return mapping.findForward("viewFilter");
    }

    /**
     * CU Customization: Changes required for last modified date from and to values.
     */
    public ActionForward clear(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws
            Exception {
        final ActionListFilterForm filterForm = (ActionListFilterForm) form;
        filterForm.setFilter(new ActionListFilter());
        filterForm.setCreateDateFrom("");
        filterForm.setCreateDateTo("");
        filterForm.setLastAssignedDateFrom("");
        filterForm.setLastAssignedDateTo("");
        filterForm.setLastModifiedDateFrom("");
        filterForm.setLastModifiedDateTo("");
        filterForm.setDocTypeFullName("");
        final UserSession session = getUserSession();
        session.removeObject(KewApiConstants.ACTION_LIST_FILTER_ATTR_NAME);
        return mapping.findForward("viewFilter");
    }

    public void initForm(final HttpServletRequest request, final ActionForm form) throws Exception {
        final ActionListFilterForm filterForm = (ActionListFilterForm) form;
        filterForm.setUserWorkgroups(getUserWorkgroupsDropDownList(getUserSession().getPrincipalId()));
        final PreferencesService prefSrv = KewApiServiceLocator.getPreferencesService();
        final Preferences preferences = prefSrv.getPreferences(getUserSession().getPrincipalId());
        request.setAttribute("preferences", preferences);
        final ActionListService actionListSrv = KEWServiceLocator.getActionListService();
        request.setAttribute("delegators", ActionListUtil.getWebFriendlyRecipients(
                actionListSrv.findUserSecondaryDelegators(getUserSession().getPrincipalId())));
        request.setAttribute("primaryDelegates", ActionListUtil.getWebFriendlyRecipients(
                actionListSrv.findUserPrimaryDelegations(getUserSession().getPrincipalId())));
        if (!filterForm.getMethodToCall().equalsIgnoreCase("clear")) {
            filterForm.validateDates();
            filterForm.validateDocType();
        }
        if (StringUtils.isNotBlank(filterForm.getBackLocation())) {
            final String actionListUrl = SpringContext.getBean(ConfigurationService.class)
                    .getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) + "/ActionList.do";
            filterForm.setBackLocation(actionListUrl);
        }
    }

    private List<? extends KeyValue> getUserWorkgroupsDropDownList(final String principalId) {
        final List<String> userWorkgroups =
                KimApiServiceLocator.getGroupService().getGroupIdsByPrincipalId(principalId);

        //note that userWorkgroups is unmodifiable so we need to create a new list that we can sort
        final List<String> userGroupsToSort = new ArrayList<>(userWorkgroups);

        final List<KeyValue> sortedUserWorkgroups = new ArrayList<>();
        KeyValue keyValue;
        keyValue = new ConcreteKeyValue(KewApiConstants.NO_FILTERING, KewApiConstants.NO_FILTERING);
        sortedUserWorkgroups.add(keyValue);
        if (userGroupsToSort != null && userGroupsToSort.size() > 0) {
            Collections.sort(userGroupsToSort);

            Group group;
            for (final String groupId : userGroupsToSort) {
                group = KimApiServiceLocator.getGroupService().getGroup(groupId);
                keyValue = new ConcreteKeyValue(groupId, group.getName());
                sortedUserWorkgroups.add(keyValue);
            }
        }
        return sortedUserWorkgroups;
    }

    private UserSession getUserSession() {
        return GlobalVariables.getUserSession();
    }

}
