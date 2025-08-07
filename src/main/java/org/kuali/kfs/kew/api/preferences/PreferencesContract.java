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

import java.util.Map;

/**
 * ====
 * CU Customization: Added a preference for controlling the visibility of the action list notes column.
 * CU Customization: Added a preference for controlling the visibility of the action list last modified date column.
 * ====
 * 
 * A contract defining the method for a {@link Preferences} model object.
 *
 * @see Preferences
 */
public interface PreferencesContract {

    boolean isRequiresSave();

    String getEmailNotification();

    String getNotifyPrimaryDelegation();

    String getNotifySecondaryDelegation();

    String getOpenNewWindow();

    String getShowActionRequested();

    String getShowDateCreated();

    String getShowDocumentStatus();

    String getShowAppDocStatus();

    String getShowDocType();

    String getShowInitiator();

    String getShowDocTitle();

    String getShowWorkgroupRequest();

    String getShowDelegator();

    String getShowClearFyi();

    String getPageSize();

    String getRefreshRate();

    String getDelegatorFilter();

    String getUseOutbox();

    String getShowDateApproved();

    String getShowCurrentNode();

    String getPrimaryDelegateFilter();

    String getNotifyAcknowledge();

    String getNotifyApprove();

    String getNotifyComplete();

    String getNotifyFYI();

    String getShowNotes();
    
    String getShowLastModifiedDate();

    Map<String, String> getDocumentTypeNotificationPreferences();

}
