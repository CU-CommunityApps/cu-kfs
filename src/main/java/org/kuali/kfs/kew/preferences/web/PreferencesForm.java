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
package org.kuali.kfs.kew.preferences.web;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.api.preferences.Preferences;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ====
 * CU Customization: Added handling for the custom "showNotes" preference.
 * CU Customization: Added handling for the custom "showLastModifiedDate" preference.
 * ====
 * 
 * Struts ActionForm for {@link PreferencesAction}.
 */
public class PreferencesForm extends KualiForm {

    private static final long serialVersionUID = 4536869031291955777L;
    private static final String ERR_KEY_REFRESH_RATE_WHOLE_NUM = "preferences.refreshRate";
    private static final String ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM = "preferences.pageSize";
    private Preferences.Builder preferences;
    private String methodToCall = "";
    private String returnMapping;
    private boolean showOutbox = true;
    private String documentTypePreferenceName;
    private String documentTypePreferenceValue;

    // KULRICE-3137: Added a backLocation parameter similar to the one from lookups.
    private String backLocation;

    public PreferencesForm() {
        preferences = Preferences.Builder.create();
    }

    public String getReturnMapping() {
        return returnMapping;
    }

    public void setReturnMapping(String returnMapping) {
        this.returnMapping = returnMapping;
    }

    public String getMethodToCall() {
        return methodToCall;
    }

    public void setMethodToCall(String methodToCall) {
        Pattern p = Pattern.compile("\\w");
        if (StringUtils.isNotBlank(methodToCall)) {
            Matcher m = p.matcher(methodToCall);
            if (m.find()) {
                this.methodToCall = methodToCall;
            } else {
                throw new RuntimeException("invalid characters found in the parameter methodToCall");
            }
        } else {
            this.methodToCall = methodToCall;
        }
    }

    public Preferences.Builder getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences.Builder preferences) {
        this.preferences = preferences;
    }

    public boolean isShowOutbox() {
        return this.showOutbox;
    }

    public void setShowOutbox(boolean showOutbox) {
        this.showOutbox = showOutbox;
    }

    public String getBackLocation() {
        return WebUtils.sanitizeBackLocation(this.backLocation);
    }

    public void setBackLocation(String backLocation) {
        this.backLocation = backLocation;
    }

    public String getDocumentTypePreferenceName() {
        return documentTypePreferenceName;
    }

    public void setDocumentTypePreferenceName(String documentTypePreferenceName) {
        this.documentTypePreferenceName = documentTypePreferenceName;
    }

    public String getDocumentTypePreferenceValue() {
        return documentTypePreferenceValue;
    }

    public void setDocumentTypePreferenceValue(String documentTypePreferenceValue) {
        this.documentTypePreferenceValue = documentTypePreferenceValue;
    }

    public Object getDocumentTypeNotificationPreference(String documentType) {
        return preferences.getDocumentTypeNotificationPreference(documentType);
    }

    public void setDocumentTypeNotificationPreference(String documentType, String preferenceValue) {
        preferences.addDocumentTypeNotificationPreference(documentType, preferenceValue);
    }

    /**
     * Retrieves the "returnLocation" parameter after calling "populate" on the superclass.
     */
    @Override
    public void populate(HttpServletRequest request) {
        super.populate(request);

        if (getParameter(request, KRADConstants.RETURN_LOCATION_PARAMETER) != null) {
            String returnLocation = getParameter(request, KRADConstants.RETURN_LOCATION_PARAMETER);
            if (returnLocation.contains(">") || returnLocation.contains("<") || returnLocation.contains("\"")) {
                returnLocation = returnLocation.replaceAll("\"", "%22");
                returnLocation = returnLocation.replaceAll("<", "%3C");
                returnLocation = returnLocation.replaceAll(">", "%3E");

            }
            setBackLocation(returnLocation);
        }
    }

    public void validatePreferences() {
        if (!PreferencesConstants.EmailNotificationPreferences.getEmailNotificationPreferences()
                .contains(preferences.getEmailNotification())) {
            throw new RuntimeException("Email notifications cannot be saved since they have been tampered " +
                    "with. Please refresh the page and try again");
        }

        if (!PreferencesConstants.DelegatorFilterValues.getDelegatorFilterValues()
                .contains(preferences.getDelegatorFilter())) {
            throw new RuntimeException("Delegator filter values cannot be saved since they have been tampered " +
                    "with. Please refresh the page and try again");
        }

        if (!PreferencesConstants.PrimaryDelegateFilterValues.getPrimaryDelegateFilterValues()
                .contains(preferences.getPrimaryDelegateFilter())) {
            throw new RuntimeException("Primary delegator filter values cannot be saved since they have been " +
                    "tampered with. Please refresh the page and try again");
        }

        if (StringUtils.isNotBlank(preferences.getNotifyPrimaryDelegation())
                && !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getNotifyPrimaryDelegation())) {
            throw new RuntimeException("Invalid value found for checkbox \"Receive Primary Delegate Email\"");
        }

        if (StringUtils.isNotBlank(preferences.getNotifySecondaryDelegation())
                && !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getNotifySecondaryDelegation())) {
            throw new RuntimeException("Invalid value found for checkbox \"Receive Secondary Delegate Email\"");
        }

        if (StringUtils.isNotBlank(preferences.getShowDocType()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues().contains(preferences.getShowDocType()) ||
                StringUtils.isNotBlank(preferences.getShowDocTitle()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowDocTitle()) ||
                StringUtils.isNotBlank(preferences.getShowActionRequested()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowActionRequested()) ||
                StringUtils.isNotBlank(preferences.getShowInitiator()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowInitiator()) ||
                StringUtils.isNotBlank(preferences.getShowDelegator()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowDelegator()) ||
                StringUtils.isNotBlank(preferences.getShowDateCreated()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowDateCreated()) ||
                StringUtils.isNotBlank(preferences.getShowDateApproved()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowDateApproved()) ||
                StringUtils.isNotBlank(preferences.getShowLastModifiedDate()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowLastModifiedDate()) ||
                StringUtils.isNotBlank(preferences.getShowCurrentNode()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                        .contains(preferences.getShowCurrentNode()) ||
                StringUtils.isNotBlank(preferences.getShowWorkgroupRequest()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                         .contains(preferences.getShowWorkgroupRequest()) ||
                StringUtils.isNotBlank(preferences.getShowDocumentStatus()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                         .contains(preferences.getShowDocumentStatus()) ||
                StringUtils.isNotBlank(preferences.getShowClearFyi()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                         .contains(preferences.getShowClearFyi()) ||
                StringUtils.isNotBlank(preferences.getShowNotes()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                         .contains(preferences.getShowNotes()) ||
                StringUtils.isNotBlank(preferences.getUseOutbox()) &&
                !PreferencesConstants.CheckBoxValues.getCheckBoxValues()
                         .contains(preferences.getUseOutbox())) {
            throw new RuntimeException("Preferences for fields displayed in action list cannot be saved since " +
                    "they have in tampered with. Please refresh the page and try again");
        }

        try {
            Integer.valueOf(preferences.getRefreshRate().trim());
        } catch (NumberFormatException | NullPointerException e) {
            GlobalVariables.getMessageMap().putError(ERR_KEY_REFRESH_RATE_WHOLE_NUM, "general.message",
                    "ActionList Refresh Rate must be in whole minutes");
        }

        try {
            Integer.valueOf(preferences.getPageSize().trim());
            if (Integer.parseInt(preferences.getPageSize().trim()) <= 0
                    || Integer.parseInt(preferences.getPageSize().trim()) > 500) {
                GlobalVariables.getMessageMap().putError(ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM, "general.message",
                        "ActionList Page Size must be between 1 and 500");
            }
        } catch (NumberFormatException | NullPointerException e) {
            GlobalVariables.getMessageMap().putError(ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM, "general.message",
                    "ActionList Page Size must be a whole number");
        }

        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException("errors in preferences");
        }
    }
}
