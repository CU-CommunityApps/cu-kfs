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
package org.kuali.kfs.kew.api.preferences;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.mo.ModelBuilder;
import org.kuali.kfs.kew.api.KewApiConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ====
 * CU Customization: Added a preference for controlling the visibility of the action list notes column.
 * CU Customization: Added a preference for controlling the visibility of the action list last modified date column.
 * ====
 * 
 * When loaded, Preferences could be in a state where they require being saved to the database.
 * If this is the case then {{@link #requiresSave} will evaluate to true.
 *
 * @see PreferencesContract
 */
public final class Preferences implements PreferencesContract, Serializable {

    private static final long serialVersionUID = 642820621349964439L;

    private final boolean requiresSave;
    private final String emailNotification;
    private final String notifyPrimaryDelegation;
    private final String notifySecondaryDelegation;
    private final String openNewWindow;
    private final String showActionRequested;
    private final String showDateCreated;
    private final String showDocumentStatus;
    private final String showAppDocStatus;
    private final String showDocType;
    private final String showInitiator;
    private final String showDocTitle;
    private final String showWorkgroupRequest;
    private final String showDelegator;
    private final String showClearFyi;
    private final String pageSize;
    private final String refreshRate;
    private final String delegatorFilter;
    private final String useOutbox;
    private final String showDateApproved;
    private final String showCurrentNode;
    private final String primaryDelegateFilter;
    private final String notifyAcknowledge;
    private final String notifyApprove;

    private final String notifyComplete;
    private final String notifyFYI;

    private final String showNotes;
    private final String showLastModifiedDate;

    /*
     * @Deprecated for 2.1.1.  Invalid @XmlJavaTypeAdapter.  Use documentTypeNotitificationPreferenceMap instead.
     */
    @Deprecated
    private final Map<String, String> documentTypeNotificationPreferences;

    private final Map<String, String> documentTypeNotificationPreferenceMap;

    public Preferences(final Builder builder) {
        emailNotification = builder.getEmailNotification();
        notifyPrimaryDelegation = builder.getNotifyPrimaryDelegation();
        notifySecondaryDelegation = builder.getNotifySecondaryDelegation();
        openNewWindow = builder.getOpenNewWindow();
        showActionRequested = builder.getShowActionRequested();
        showDateCreated = builder.getShowDateCreated();
        showDocumentStatus = builder.getShowDocumentStatus();
        showAppDocStatus = builder.getShowAppDocStatus();
        showDocType = builder.getShowDocType();
        showInitiator = builder.getShowInitiator();
        showDocTitle = builder.getShowDocTitle();
        showWorkgroupRequest = builder.getShowWorkgroupRequest();
        showDelegator = builder.getShowDelegator();
        showClearFyi = builder.getShowClearFyi();
        pageSize = builder.getPageSize();
        refreshRate = builder.getRefreshRate();
        delegatorFilter = builder.getDelegatorFilter();
        useOutbox = builder.getUseOutbox();
        showDateApproved = builder.getShowDateApproved();
        showCurrentNode = builder.getShowCurrentNode();
        primaryDelegateFilter = builder.getPrimaryDelegateFilter();
        requiresSave = builder.isRequiresSave();
        notifyAcknowledge = builder.getNotifyAcknowledge();
        notifyApprove = builder.getNotifyApprove();
        notifyComplete = builder.getNotifyComplete();
        notifyFYI = builder.getNotifyFYI();
        showNotes = builder.getShowNotes();
        showLastModifiedDate = builder.getShowLastModifiedDate();
        documentTypeNotificationPreferences = null;
        documentTypeNotificationPreferenceMap = builder.getDocumentTypeNotificationPreferences();
    }

    @Override
    public boolean isRequiresSave() {
        return requiresSave;
    }

    @Override
    public String getEmailNotification() {
        return emailNotification;
    }

    @Override
    public String getNotifyPrimaryDelegation() {
        return notifyPrimaryDelegation;
    }

    @Override
    public String getNotifySecondaryDelegation() {
        return notifySecondaryDelegation;
    }

    @Override
    public String getOpenNewWindow() {
        return openNewWindow;
    }

    @Override
    public String getShowActionRequested() {
        return showActionRequested;
    }

    @Override
    public String getShowDateCreated() {
        return showDateCreated;
    }

    @Override
    public String getShowDocumentStatus() {
        return showDocumentStatus;
    }

    @Override
    public String getShowAppDocStatus() {
        return showAppDocStatus;
    }

    @Override
    public String getShowDocType() {
        return showDocType;
    }

    @Override
    public String getShowInitiator() {
        return showInitiator;
    }

    @Override
    public String getShowDocTitle() {
        return showDocTitle;
    }

    @Override
    public String getShowWorkgroupRequest() {
        return showWorkgroupRequest;
    }

    @Override
    public String getShowDelegator() {
        return showDelegator;
    }

    @Override
    public String getShowClearFyi() {
        return showClearFyi;
    }

    @Override
    public String getPageSize() {
        return pageSize;
    }

    @Override
    public String getRefreshRate() {
        return refreshRate;
    }

    @Override
    public String getDelegatorFilter() {
        return delegatorFilter;
    }

    @Override
    public String getUseOutbox() {
        return useOutbox;
    }

    @Override
    public String getShowDateApproved() {
        return showDateApproved;
    }

    @Override
    public String getShowCurrentNode() {
        return showCurrentNode;
    }

    @Override
    public String getPrimaryDelegateFilter() {
        return primaryDelegateFilter;
    }

    @Override
    public String getNotifyComplete() {
        return notifyComplete;
    }

    @Override
    public String getNotifyApprove() {
        return notifyApprove;
    }

    @Override
    public String getNotifyAcknowledge() {
        return notifyAcknowledge;
    }

    @Override
    public String getNotifyFYI() {
        return notifyFYI;
    }

    @Override
    public String getShowNotes() {
        return this.showNotes;
    }
    
    public String getShowLastModifiedDate() {
        return this.showLastModifiedDate;
    }

    public String getDocumentTypeNotificationPreference(final String documentType) {
        final String preferenceName = documentType.replace(KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_DELIMITER, ".");
        final String preferenceValue = getDocumentTypeNotificationPreferences().get(preferenceName);
        if (StringUtils.isNotBlank(preferenceValue)) {
            return preferenceValue;
        }
        return null;
    }

    @Override
    public Map<String, String> getDocumentTypeNotificationPreferences() {
        return documentTypeNotificationPreferenceMap == null ? documentTypeNotificationPreferences :
                documentTypeNotificationPreferenceMap;
    }

    public boolean isUsingOutbox() {
        return getUseOutbox() != null && getUseOutbox().equals(Constants.PREFERENCES_YES_VAL);
    }

    public static final class Builder implements Serializable, ModelBuilder, PreferencesContract {

        private boolean requiresSave;

        private String emailNotification;
        private String notifyPrimaryDelegation;
        private String notifySecondaryDelegation;
        private String openNewWindow;
        private String showActionRequested;
        private String showDateCreated;
        private String showDocumentStatus;
        private String showAppDocStatus;
        private String showDocType;
        private String showInitiator;
        private String showDocTitle;
        private String showWorkgroupRequest;
        private String showDelegator;
        private String showClearFyi;
        private String pageSize;
        private String refreshRate;
        private String delegatorFilter;
        private String useOutbox;
        private String showDateApproved;
        private String showCurrentNode;
        private String primaryDelegateFilter;
        private String notifyAcknowledge;
        private String notifyApprove;
        private String notifyComplete;
        private String notifyFYI;
        private String showNotes;
        private String showLastModifiedDate;
        private Map<String, String> documentTypeNotificationPreferences;

        private Builder() {
            documentTypeNotificationPreferences = new HashMap<>();
        }

        private Builder(
                final String emailNotification, final String notifyPrimaryDelegation, final String notifySecondaryDelegation,
                final String openNewWindow, final String showActionRequested, final String showDateCreated, final String showDocumentStatus,
                final String showAppDocStatus, final String showDocType, final String showInitiator, final String showDocTitle,
                final String showWorkgroupRequest, final String showDelegator, final String showClearFyi, final String pageSize,
                final String refreshRate, final String delegatorFilter, final String useOutbox,
                final String showDateApproved, final String showCurrentNode, final String primaryDelegateFilter,
                final String notifyAcknowledge,
                final String notifyApprove, final String notifyComplete,final String notifyFYI, final String showNotes, final String showLastModifiedDate,
                final Map<String, String> documentTypeNotificationPreferences,
                final boolean requiresSave) {
            this.emailNotification = emailNotification;
            this.notifyPrimaryDelegation = notifyPrimaryDelegation;
            this.notifySecondaryDelegation = notifySecondaryDelegation;
            this.openNewWindow = openNewWindow;
            this.showActionRequested = showActionRequested;
            this.showDateCreated = showDateCreated;
            this.showDocumentStatus = showDocumentStatus;
            this.showAppDocStatus = showAppDocStatus;
            this.showDocType = showDocType;
            this.showInitiator = showInitiator;
            this.showDocTitle = showDocTitle;
            this.showWorkgroupRequest = showWorkgroupRequest;
            this.showDelegator = showDelegator;
            this.showClearFyi = showClearFyi;
            this.pageSize = pageSize;
            this.refreshRate = refreshRate;
            this.delegatorFilter = delegatorFilter;
            this.useOutbox = useOutbox;
            this.showDateApproved = showDateApproved;
            this.showCurrentNode = showCurrentNode;
            this.primaryDelegateFilter = primaryDelegateFilter;
            this.requiresSave = requiresSave;
            this.notifyAcknowledge = notifyAcknowledge;
            this.notifyApprove = notifyApprove;
            this.notifyComplete = notifyComplete;
            this.notifyFYI = notifyFYI;
            this.showNotes = showNotes;
            this.showLastModifiedDate = showLastModifiedDate;
            this.documentTypeNotificationPreferences = documentTypeNotificationPreferences;
        }

        @Override
        public org.kuali.kfs.kew.api.preferences.Preferences build() {
            return new org.kuali.kfs.kew.api.preferences.Preferences(this);
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(
                final String emailNotification, final String notifyPrimaryDelegation, final String notifySecondaryDelegation,
                final String openNewWindow, final String showActionRequested, final String showDateCreated, final String showDocumentStatus,
                final String showAppDocStatus, final String showDocType, final String showInitiator, final String showDocTitle,
                final String showWorkgroupRequest, final String showDelegator, final String showClearFyi, final String pageSize,
                final String refreshRate, final String delegatorFilter, final String useOutbox,
                final String showDateApproved, final String showCurrentNode, final String primaryDelegateFilter,
                final String notifyAcknowledge,
                final String notifyApprove, final String notifyComplete, final String notifyFYI, final String showNotes, final String showLastModifiedDate,
                final Map<String, String> documentTypeNotificationPreferences,
                final boolean requiresSave) {
            return new Builder(emailNotification, notifyPrimaryDelegation, notifySecondaryDelegation, openNewWindow,
                    showActionRequested, showDateCreated,
                    showDocumentStatus, showAppDocStatus, showDocType, showInitiator, showDocTitle,
                    showWorkgroupRequest, showDelegator, showClearFyi,
                    pageSize, refreshRate, delegatorFilter, useOutbox, showDateApproved,
                    showCurrentNode, primaryDelegateFilter,
                    notifyAcknowledge, notifyApprove, notifyComplete, notifyFYI, showNotes, showLastModifiedDate,
                    documentTypeNotificationPreferences, requiresSave);
        }

        public static Builder create(final PreferencesContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            return create(contract.getEmailNotification(), contract.getNotifyPrimaryDelegation(),
                    contract.getNotifySecondaryDelegation(), contract.getOpenNewWindow(),
                    contract.getShowActionRequested(), contract.getShowDateCreated(),
                    contract.getShowDocumentStatus(), contract.getShowAppDocStatus(), contract.getShowDocType(),
                    contract.getShowInitiator(), contract.getShowDocTitle(), contract.getShowWorkgroupRequest(),
                    contract.getShowDelegator(), contract.getShowClearFyi(),
                    contract.getPageSize(), contract.getRefreshRate(), contract.getDelegatorFilter(),
                    contract.getUseOutbox(), contract.getShowDateApproved(),
                    contract.getShowCurrentNode(), contract.getPrimaryDelegateFilter(),
                    contract.getNotifyAcknowledge(), contract.getNotifyApprove(), contract.getNotifyComplete(),
                    contract.getNotifyFYI(), contract.getShowNotes(), contract.getShowLastModifiedDate(),
                    contract.getDocumentTypeNotificationPreferences(), contract.isRequiresSave());
        }

        public static Builder create(
                final Map<String, String> map, final Map<String, String> documentTypeNotificationPreferences,
                final boolean requiresSave) {
            return create(map.get(KEYS.EMAIL_NOTIFICATION), map.get(KEYS.NOTIFY_PRIMARY_DELEGATION), map.get(
                    KEYS.NOTIFY_SECONDARY_DELEGATION), map.get(KEYS.OPEN_NEW_WINDOW),
                    map.get(KEYS.SHOW_ACTION_REQUESTED), map.get(KEYS.SHOW_DATE_CREATED),
                    map.get(KEYS.SHOW_DOCUMENT_STATUS), map.get(
                            KEYS.SHOW_APP_DOC_STATUS), map.get(KEYS.SHOW_DOC_TYPE),
                    map.get(KEYS.SHOW_INITIATOR), map.get(KEYS.SHOW_DOC_TITLE),
                    map.get(KEYS.SHOW_GROUP_REQUEST), map.get(
                            KEYS.SHOW_DELEGATOR), map.get(KEYS.SHOW_CLEAR_FYI),
                    map.get(KEYS.PAGE_SIZE), map.get(KEYS.REFRESH_RATE),
                    map.get(KEYS.DELEGATOR_FILTER), map.get(
                            KEYS.USE_OUT_BOX), map.get(KEYS.SHOW_DATE_APPROVED),
                    map.get(KEYS.SHOW_CURRENT_NODE), map.get(KEYS.PRIMARY_DELEGATE_FILTER),
                    map.get(KEYS.NOTIFY_ACKNOWLEDGE), map.get(
                            KEYS.NOTIFY_APPROVE), map.get(KEYS.NOTIFY_COMPLETE),
                    map.get(KEYS.NOTIFY_FYI), map.get(KEYS.SHOW_NOTES), map.get(KEYS.SHOW_LAST_MODIFIED_DATE),
                    documentTypeNotificationPreferences, requiresSave);
        }

        @Override
        public synchronized boolean isRequiresSave() {
            return requiresSave;
        }

        public synchronized void setRequiresSave(final boolean requiresSave) {
            this.requiresSave = requiresSave;
        }

        @Override
        public synchronized String getEmailNotification() {
            return emailNotification;
        }

        public synchronized void setEmailNotification(final String emailNotification) {
            this.emailNotification = emailNotification;
        }

        @Override
        public synchronized String getNotifyPrimaryDelegation() {
            return notifyPrimaryDelegation;
        }

        public synchronized void setNotifyPrimaryDelegation(final String notifyPrimaryDelegation) {
            this.notifyPrimaryDelegation = notifyPrimaryDelegation;
        }

        @Override
        public synchronized String getNotifySecondaryDelegation() {
            return notifySecondaryDelegation;
        }

        public synchronized void setNotifySecondaryDelegation(final String notifySecondaryDelegation) {
            this.notifySecondaryDelegation = notifySecondaryDelegation;
        }

        @Override
        public synchronized String getOpenNewWindow() {
            return openNewWindow;
        }

        public synchronized void setOpenNewWindow(final String openNewWindow) {
            this.openNewWindow = openNewWindow;
        }

        @Override
        public synchronized String getShowActionRequested() {
            return showActionRequested;
        }

        public synchronized void setShowActionRequested(final String showActionRequested) {
            this.showActionRequested = showActionRequested;
        }

        @Override
        public synchronized String getShowDateCreated() {
            return showDateCreated;
        }

        public synchronized void setShowDateCreated(final String showDateCreated) {
            this.showDateCreated = showDateCreated;
        }

        @Override
        public synchronized String getShowDocumentStatus() {
            return showDocumentStatus;
        }

        public synchronized void setShowDocumentStatus(final String showDocumentStatus) {
            this.showDocumentStatus = showDocumentStatus;
        }

        @Override
        public synchronized String getShowAppDocStatus() {
            return showAppDocStatus;
        }

        public synchronized void setShowAppDocStatus(final String showAppDocStatus) {
            this.showAppDocStatus = showAppDocStatus;
        }

        @Override
        public synchronized String getShowDocType() {
            return showDocType;
        }

        public synchronized void setShowDocType(final String showDocType) {
            this.showDocType = showDocType;
        }

        @Override
        public synchronized String getShowInitiator() {
            return showInitiator;
        }

        public synchronized void setShowInitiator(final String showInitiator) {
            this.showInitiator = showInitiator;
        }

        @Override
        public synchronized String getShowDocTitle() {
            return showDocTitle;
        }

        public synchronized void setShowDocTitle(final String showDocTitle) {
            this.showDocTitle = showDocTitle;
        }

        @Override
        public synchronized String getShowWorkgroupRequest() {
            return showWorkgroupRequest;
        }

        public synchronized void setShowWorkgroupRequest(final String showWorkgroupRequest) {
            this.showWorkgroupRequest = showWorkgroupRequest;
        }

        @Override
        public synchronized String getShowDelegator() {
            return showDelegator;
        }

        public synchronized void setShowDelegator(final String showDelegator) {
            this.showDelegator = showDelegator;
        }

        @Override
        public synchronized String getShowClearFyi() {
            return showClearFyi;
        }

        public synchronized void setShowClearFyi(final String showClearFyi) {
            this.showClearFyi = showClearFyi;
        }

        @Override
        public synchronized String getPageSize() {
            return pageSize;
        }

        public synchronized void setPageSize(final String pageSize) {
            this.pageSize = pageSize;
        }

        @Override
        public synchronized String getRefreshRate() {
            return refreshRate;
        }

        public synchronized void setRefreshRate(final String refreshRate) {
            this.refreshRate = refreshRate;
        }

        @Override
        public synchronized String getDelegatorFilter() {
            return delegatorFilter;
        }

        public synchronized void setDelegatorFilter(final String delegatorFilter) {
            this.delegatorFilter = delegatorFilter;
        }

        @Override
        public synchronized String getUseOutbox() {
            return useOutbox;
        }

        public synchronized void setUseOutbox(final String useOutbox) {
            this.useOutbox = useOutbox;
        }

        @Override
        public synchronized String getShowDateApproved() {
            return showDateApproved;
        }

        public synchronized void setShowDateApproved(final String showDateApproved) {
            this.showDateApproved = showDateApproved;
        }

        @Override
        public synchronized String getShowCurrentNode() {
            return showCurrentNode;
        }

        public synchronized void setShowCurrentNode(final String showCurrentNode) {
            this.showCurrentNode = showCurrentNode;
        }

        @Override
        public synchronized String getPrimaryDelegateFilter() {
            return primaryDelegateFilter;
        }

        public synchronized void setPrimaryDelegateFilter(final String primaryDelegateFilter) {
            this.primaryDelegateFilter = primaryDelegateFilter;
        }

        @Override
        public synchronized String getNotifyAcknowledge() {
            return notifyAcknowledge;
        }

        public synchronized void setNotifyAcknowledge(final String notifyAcknowledge) {
            this.notifyAcknowledge = notifyAcknowledge;
        }

        @Override
        public synchronized String getNotifyApprove() {
            return notifyApprove;
        }

        public synchronized void setNotifyApprove(final String notifyApprove) {
            this.notifyApprove = notifyApprove;
        }

        @Override
        public synchronized String getNotifyComplete() {
            return notifyComplete;
        }

        public synchronized void setNotifyComplete(final String notifyComplete) {
            this.notifyComplete = notifyComplete;
        }

        @Override
        public synchronized String getNotifyFYI() {
            return notifyFYI;
        }

        public synchronized void setNotifyFYI(final String notifyFYI) {
            this.notifyFYI = notifyFYI;
        }

        @Override
        public synchronized String getShowNotes() {
            return this.showNotes;
        }

        public synchronized void setShowNotes(final String showNotes) {
            this.showNotes = showNotes;
        }
        
        @Override
        public synchronized String getShowLastModifiedDate() {
            return this.showLastModifiedDate;
        }

        public synchronized void setShowLastModifiedDate(final String showLastModifiedDate) {
            this.showLastModifiedDate = showLastModifiedDate;
        }

        public synchronized String getDocumentTypeNotificationPreference(final String documentType) {
            final String preferenceName = documentType.replace(KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_DELIMITER, ".");
            final String preferenceValue = documentTypeNotificationPreferences.get(preferenceName);
            if (StringUtils.isNotBlank(preferenceValue)) {
                return preferenceValue;
            }
            return null;
        }

        public synchronized void setDocumentTypeNotificationPreference(String documentType, final String preference) {
            documentType = documentType.replace(KewApiConstants.DOCUMENT_TYPE_NOTIFICATION_DELIMITER, ".");
            documentTypeNotificationPreferences.put(documentType, preference);
        }

        @Override
        public synchronized Map<String, String> getDocumentTypeNotificationPreferences() {
            if (documentTypeNotificationPreferences == null) {
                documentTypeNotificationPreferences = new HashMap<>();
            }
            return documentTypeNotificationPreferences;
        }

        public synchronized void setDocumentTypeNotificationPreferences(
                final Map<String, String> documentTypeNotificationPreferences) {
            this.documentTypeNotificationPreferences = documentTypeNotificationPreferences;
        }

        public synchronized void addDocumentTypeNotificationPreference(final String documentType, final String preference) {
            getDocumentTypeNotificationPreferences().put(documentType, preference);
        }

        public synchronized void removeDocumentTypeNotificationPreference(final String documentType) {
            getDocumentTypeNotificationPreferences().remove(documentType);
        }
    }

    static class Constants {
        static final String PREFERENCES_YES_VAL = "yes";
    }

    public static class KEYS {
        public static final String REFRESH_RATE = "REFRESH_RATE";
        public static final String OPEN_NEW_WINDOW = "OPEN_ITEMS_NEW_WINDOW";
        public static final String SHOW_DOC_TYPE = "DOC_TYPE_COL_SHOW_NEW";
        public static final String SHOW_DOC_TITLE = "TITLE_COL_SHOW_NEW";
        public static final String SHOW_ACTION_REQUESTED = "ACTION_REQUESTED_COL_SHOW_NEW";
        public static final String SHOW_INITIATOR = "INITIATOR_COL_SHOW_NEW";
        public static final String SHOW_DELEGATOR = "DELEGATOR_COL_SHOW_NEW";
        public static final String SHOW_DATE_CREATED = "DATE_CREATED_COL_SHOW_NEW";
        public static final String SHOW_DOCUMENT_STATUS = "DOCUMENT_STATUS_COL_SHOW_NEW";
        public static final String SHOW_APP_DOC_STATUS = "APP_DOC_STATUS_COL_SHOW_NEW";
        public static final String SHOW_GROUP_REQUEST = "WORKGROUP_REQUEST_COL_SHOW_NEW";
        public static final String SHOW_CLEAR_FYI = "CLEAR_FYI_COL_SHOW_NEW";
        public static final String PAGE_SIZE = "ACTION_LIST_SIZE_NEW";
        public static final String EMAIL_NOTIFICATION = "EMAIL_NOTIFICATION";
        public static final String NOTIFY_PRIMARY_DELEGATION = "EMAIL_NOTIFY_PRIMARY";
        public static final String NOTIFY_SECONDARY_DELEGATION = "EMAIL_NOTIFY_SECONDARY";
        public static final String DEFAULT_COLOR = "white";
        public static final String DEFAULT_ACTION_LIST_SIZE = "10";
        public static final String DEFAULT_REFRESH_RATE = "15";
        public static final String ERR_KEY_REFRESH_RATE_WHOLE_NUM = "preferences.refreshRate";
        public static final String ERR_KEY_ACTION_LIST_PAGE_SIZE_WHOLE_NUM = "preferences.pageSize";
        public static final String DELEGATOR_FILTER = "DELEGATOR_FILTER";
        public static final String PRIMARY_DELEGATE_FILTER = "PRIMARY_DELEGATE_FILTER";
        public static final String USE_OUT_BOX = "USE_OUT_BOX";
        public static final String SHOW_DATE_APPROVED = "LAST_APPROVED_DATE_COL_SHOW_NEW";
        public static final String SHOW_CURRENT_NODE = "CURRENT_NODE_COL_SHOW_NEW";
        public static final String NOTIFY_ACKNOWLEDGE = "NOTIFY_ACKNOWLEDGE";
        public static final String NOTIFY_APPROVE = "NOTIFY_APPROVE";
        public static final String NOTIFY_COMPLETE = "NOTIFY_COMPLETE";
        public static final String NOTIFY_FYI = "NOTIFY_FYI";
        public static final String SHOW_NOTES = "SHOW_NOTES";
        public static final String SHOW_LAST_MODIFIED_DATE = "SHOW_LAST_MODIFIED_DATE";
        public static final String DOCUMENT_TYPE_NOTIFICATION_PREFERENCES = "DOCUMENT_TYPE_NOTIFICATION_PREFERENCES";
    }
}
