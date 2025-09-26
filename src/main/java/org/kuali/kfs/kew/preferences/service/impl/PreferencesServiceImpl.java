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
package org.kuali.kfs.kew.preferences.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.actionlist.service.ActionListService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.preferences.Preferences;
import org.kuali.kfs.kew.api.preferences.PreferencesService;
import org.kuali.kfs.kew.exception.WorkflowServiceError;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.useroptions.UserOptions;
import org.kuali.kfs.kew.useroptions.UserOptionsService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ====
 * CU Customization: Added handling of the custom "showNotes" action list preference.
 * CU Customization: Added handling of the custom "showLastModifiedDate" action list preference.
 * ====
 * 
 * An implementation of the {@link PreferencesService}.
 */
public class PreferencesServiceImpl implements PreferencesService {

    private static final Logger LOG = LogManager.getLogger();

    private static final Map<String, String> USER_OPTION_KEY_DEFAULT_MAP;

    private ActionListService actionListService;
    private ConfigurationService configurationService;

    static {
        USER_OPTION_KEY_DEFAULT_MAP = new HashMap<>();
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.EMAIL_NOTIFICATION, "userOptions.default.email");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.NOTIFY_PRIMARY_DELEGATION, "userOptions.default.notifyPrimary");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.NOTIFY_SECONDARY_DELEGATION, "userOptions.default.notifySecondary");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.OPEN_NEW_WINDOW, "userOptions.default.openNewWindow");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.PAGE_SIZE, "userOptions.default.actionListSize");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.REFRESH_RATE, "userOptions.default.refreshRate");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.SHOW_ACTION_REQUESTED, "userOptions.default.showActionRequired");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_DATE_CREATED, "userOptions.default.showDateCreated");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_DOC_TYPE, "userOptions.default.showDocumentType");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.SHOW_DOCUMENT_STATUS, "userOptions.default.showDocumentStatus");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_INITIATOR, "userOptions.default.showInitiator");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_DELEGATOR, "userOptions.default.showDelegator");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_DOC_TITLE, "userOptions.default.showTitle");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.SHOW_GROUP_REQUEST, "userOptions.default.showWorkgroupRequest");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_CLEAR_FYI, "userOptions.default.showClearFYI");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.DELEGATOR_FILTER, "userOptions.default.delegatorFilterOnActionList");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.PRIMARY_DELEGATE_FILTER,
                "userOptions.default.primaryDelegatorFilterOnActionList");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.SHOW_DATE_APPROVED, "userOptions.default.showLastApprovedDate");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_CURRENT_NODE, "userOptions.default.showCurrentNode");
        USER_OPTION_KEY_DEFAULT_MAP
                .put(Preferences.KEYS.USE_OUT_BOX, KewApiConstants.USER_OPTIONS_DEFAULT_USE_OUTBOX_PARAM);
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.NOTIFY_ACKNOWLEDGE, "userOptions.default.notifyAcknowledge");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.NOTIFY_APPROVE, "userOptions.default.notifyApprove");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.NOTIFY_COMPLETE, "userOptions.default.notifyComplete");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.NOTIFY_FYI, "userOptions.default.notifyFYI");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_NOTES, "userOptions.default.showNotes");
        USER_OPTION_KEY_DEFAULT_MAP.put(Preferences.KEYS.SHOW_LAST_MODIFIED_DATE, "userOptions.default.showLastModifiedDate");
    }

    @Override
    public Preferences getPreferences(final String principalId) {
        LOG.debug("start preferences fetch user {}", principalId);
        final Collection<UserOptions> options = getUserOptionService().findByWorkflowUser(principalId);
        final Map<String, UserOptions> optionMap = new HashMap<>();
        final Map<String, String> optionValueMap = new HashMap<>();
        final Map<String, String> documentTypeNotificationPreferences = new HashMap<>();
        for (final UserOptions option : options) {
            if (option.getOptionId().endsWith(KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_PREFERENCE_SUFFIX)) {
                String preferenceName = option.getOptionId();
                preferenceName = StringUtils.substringBeforeLast(preferenceName,
                        KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_PREFERENCE_SUFFIX);
                documentTypeNotificationPreferences.put(preferenceName, option.getOptionVal());
            } else {
                optionMap.put(option.getOptionId(), option);
            }
        }

        boolean isSaveRequired = false;

        for (final Map.Entry<String, String> entry : USER_OPTION_KEY_DEFAULT_MAP.entrySet()) {
            final String optionKey = entry.getKey();
            final String defaultValue = configurationService.getPropertyValueAsString(entry.getValue());
            LOG.debug("start fetch option {} user {}", optionKey, principalId);

            UserOptions option = optionMap.get(optionKey);
            if (option == null) {
                LOG.debug(
                        "User option '{}' on user {} has no stored value.  Preferences will require save.",
                        optionKey,
                        principalId
                );
                option = new UserOptions();
                option.setWorkflowId(principalId);
                option.setOptionId(optionKey);
                option.setOptionVal(defaultValue);
                // just in case referenced a second time
                optionMap.put(optionKey, option);

                if (!isSaveRequired) {
                    if (!optionKey.equals(Preferences.KEYS.USE_OUT_BOX)
                            || actionListService.isOutBoxOn()) {
                        isSaveRequired = true;
                    }
                }
            }
            LOG.debug("End fetch option {} user {}", optionKey, principalId);

            optionValueMap.put(optionKey, option.getOptionVal());
        }

        return Preferences.Builder.create(optionValueMap, documentTypeNotificationPreferences, isSaveRequired)
                .build();
    }

    @Override
    public void savePreferences(final String principalId, final Preferences preferences) {
        // NOTE: this previously displayed the principalName.  Now it's just the id
        LOG.debug("saving preferences user {}", principalId);

        validate(preferences);
        final Map<String, String> optionsMap = new HashMap<>(50);

        optionsMap.put(Preferences.KEYS.REFRESH_RATE, preferences.getRefreshRate().trim());
        optionsMap.put(Preferences.KEYS.OPEN_NEW_WINDOW, preferences.getOpenNewWindow());
        optionsMap.put(Preferences.KEYS.SHOW_DOC_TYPE, preferences.getShowDocType());
        optionsMap.put(Preferences.KEYS.SHOW_DOC_TITLE, preferences.getShowDocTitle());
        optionsMap.put(Preferences.KEYS.SHOW_ACTION_REQUESTED, preferences.getShowActionRequested());
        optionsMap.put(Preferences.KEYS.SHOW_INITIATOR, preferences.getShowInitiator());
        optionsMap.put(Preferences.KEYS.SHOW_DELEGATOR, preferences.getShowDelegator());
        optionsMap.put(Preferences.KEYS.SHOW_DATE_CREATED, preferences.getShowDateCreated());
        optionsMap.put(Preferences.KEYS.SHOW_DOCUMENT_STATUS, preferences.getShowDocumentStatus());
        optionsMap.put(Preferences.KEYS.SHOW_APP_DOC_STATUS, preferences.getShowAppDocStatus());
        optionsMap.put(Preferences.KEYS.SHOW_GROUP_REQUEST, preferences.getShowWorkgroupRequest());
        optionsMap.put(Preferences.KEYS.SHOW_CLEAR_FYI, preferences.getShowClearFyi());
        optionsMap.put(Preferences.KEYS.PAGE_SIZE, preferences.getPageSize().trim());
        optionsMap.put(Preferences.KEYS.EMAIL_NOTIFICATION, preferences.getEmailNotification());
        optionsMap.put(Preferences.KEYS.NOTIFY_PRIMARY_DELEGATION, preferences.getNotifyPrimaryDelegation());
        optionsMap.put(Preferences.KEYS.NOTIFY_SECONDARY_DELEGATION, preferences.getNotifySecondaryDelegation());
        optionsMap.put(Preferences.KEYS.DELEGATOR_FILTER, preferences.getDelegatorFilter());
        optionsMap.put(Preferences.KEYS.PRIMARY_DELEGATE_FILTER, preferences.getPrimaryDelegateFilter());
        optionsMap.put(Preferences.KEYS.SHOW_DATE_APPROVED, preferences.getShowDateApproved());
        optionsMap.put(Preferences.KEYS.SHOW_CURRENT_NODE, preferences.getShowCurrentNode());
        optionsMap.put(Preferences.KEYS.NOTIFY_ACKNOWLEDGE, preferences.getNotifyAcknowledge());
        optionsMap.put(Preferences.KEYS.NOTIFY_APPROVE, preferences.getNotifyApprove());
        optionsMap.put(Preferences.KEYS.NOTIFY_COMPLETE, preferences.getNotifyComplete());
        optionsMap.put(Preferences.KEYS.NOTIFY_FYI, preferences.getNotifyFYI());
        optionsMap.put(Preferences.KEYS.SHOW_NOTES, preferences.getShowNotes());
        optionsMap.put(Preferences.KEYS.SHOW_LAST_MODIFIED_DATE, preferences.getShowLastModifiedDate());
        if (actionListService.isOutBoxOn()) {
            optionsMap.put(Preferences.KEYS.USE_OUT_BOX, preferences.getUseOutbox());
        }
        for (final Entry<String, String> documentTypePreference : preferences.getDocumentTypeNotificationPreferences()
                .entrySet()) {
            optionsMap.put(documentTypePreference.getKey() +
                    KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_PREFERENCE_SUFFIX, documentTypePreference.getValue());
        }
        getUserOptionService().save(principalId, optionsMap);

        // Find which document type notification preferences have been deleted and remove them from the database
        final Preferences storedPreferences = getPreferences(principalId);
        for (final Entry<String, String> storedEntry : storedPreferences.getDocumentTypeNotificationPreferences()
                .entrySet()) {
            if (preferences.getDocumentTypeNotificationPreference(storedEntry.getKey()) == null) {
                getUserOptionService().deleteUserOptions(getUserOptionService().findByOptionId(
                        storedEntry.getKey() + KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_PREFERENCE_SUFFIX,
                        principalId));
            }
        }
        LOG.debug("saved preferences user {}", principalId);
    }

    private void validate(final Preferences preferences) {
        LOG.debug("validating preferences");

        final List<WorkflowServiceError> errors = new ArrayList<>();
        try {
            Integer.valueOf(preferences.getRefreshRate().trim());
        } catch (NumberFormatException | NullPointerException e) {
            errors.add(new WorkflowServiceErrorImpl("ActionList Refresh Rate must be in whole " +
                    "minutes", Preferences.KEYS.ERR_KEY_REFRESH_RATE_WHOLE_NUM));
        }

        try {
            if (Integer.parseInt(preferences.getPageSize().trim()) == 0) {
                errors.add(new WorkflowServiceErrorImpl("ActionList Page Size must be non-zero ",
                        Preferences.KEYS.ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM));
            }
        } catch (NumberFormatException | NullPointerException e) {
            errors.add(new WorkflowServiceErrorImpl("ActionList Page Size must be in whole " +
                    "minutes", Preferences.KEYS.ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM));
        }

        LOG.debug("end validating preferences");
        if (!errors.isEmpty()) {
            throw new WorkflowServiceErrorException("Preference Validation Error", errors);
        }
    }

    public UserOptionsService getUserOptionService() {
        return KEWServiceLocator.getService(KEWServiceLocator.USER_OPTIONS_SRV);
    }

    public void setActionListService(final ActionListService actionListService) {
        this.actionListService = actionListService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
