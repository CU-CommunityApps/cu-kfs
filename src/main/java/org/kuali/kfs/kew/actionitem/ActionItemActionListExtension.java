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
package org.kuali.kfs.kew.actionitem;

import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.preferences.Preferences;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValueActionListExtension;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.web.RowStyleable;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sys.context.SpringContext;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * ====
 * CU Customization: Added a preference for controlling the visibility of the action list last modified date column.
 * ====
 */

/**
 * Alternate model object for action list fetches that do not automatically use
 * ojb collections.  This is here to make action list faster.
 */
public class ActionItemActionListExtension extends ActionItem implements RowStyleable {

    private static final long serialVersionUID = -8801104028828059623L;

    private Timestamp lastApprovedDate;
    private Timestamp lastModifiedDate;
    private Map<String, String> customActions = new HashMap<>();
    private String rowStyleClass;
    private Integer actionListIndex;

    private String delegatorName = "";
    private String groupName = "";
    private boolean isInitialized;
    private DocumentRouteHeaderValueActionListExtension routeHeader;

    private boolean lastApprovedDateInitialized;
    private boolean lastModifiedDateInitialized;
    private boolean delegatorNameInitialized;
    private boolean groupNameInitialized;

    public String getRouteHeaderRouteStatus() {
        return routeHeader.getDocRouteStatus();
    }

    public Integer getActionListIndex() {
        return actionListIndex;
    }

    public void setActionListIndex(final Integer actionListIndex) {
        this.actionListIndex = actionListIndex;
    }

    public Timestamp getLastApprovedDate() {
        initializeLastApprovedDate();
        return lastApprovedDate;
    }
    
    public Timestamp getLastModifiedDate() {
        initializeLastModifiedDate();
        return lastModifiedDate;
    }

    public Map<String, String> getCustomActions() {
        return customActions;
    }

    public void setCustomActions(final Map<String, String> customActions) {
        this.customActions = customActions;
    }

    @Override
    public String getRowStyleClass() {
        return rowStyleClass;
    }

    @Override
    public void setRowStyleClass(final String rowStyleClass) {
        this.rowStyleClass = rowStyleClass;
    }

    public String getDelegatorName() {
        initializeDelegatorName();
        return delegatorName;
    }

    public String getGroupName() {
        initializeGroupName();
        return groupName;
    }

    public void initialize(final Preferences preferences) {
        if (isInitialized) {
            return;
        }
        if (KewApiConstants.PREFERENCES_YES_VAL.equals(preferences.getShowWorkgroupRequest())) {
            initializeGroupName();
        }
        if (KewApiConstants.PREFERENCES_YES_VAL.equals(preferences.getShowDelegator())) {
            initializeDelegatorName();
        }
        if (KewApiConstants.PREFERENCES_YES_VAL.equals(preferences.getShowDateApproved())) {
            initializeLastApprovedDate();
        }
        if (KewApiConstants.PREFERENCES_YES_VAL.equals(preferences.getShowLastModifiedDate())) {
            initializeLastModifiedDate();
        }
        routeHeader.initialize(preferences);
        isInitialized = true;
    }

    private void initializeGroupName() {
        if (!groupNameInitialized) {
            if (getGroupId() != null) {
                final Group group = super.getGroup();
                groupName = group.getName();
            }
            groupNameInitialized = true;
        }
    }

    private void initializeDelegatorName() {
        if (!delegatorNameInitialized) {
            if (getDelegatorPrincipalId() != null) {
                final Person person = SpringContext.getBean(PersonService.class).getPerson(getDelegatorPrincipalId());
                if (person != null) {
                    delegatorName = person.getName();
                }
            }
            if (getDelegatorGroupId() != null) {
                final Group delegatorGroup = KimApiServiceLocator.getGroupService().getGroup(getDelegatorGroupId());
                if (delegatorGroup != null) {
                    delegatorName = delegatorGroup.getName();
                }
            }
            delegatorNameInitialized = true;
        }
    }

    private void initializeLastApprovedDate() {
        if (!lastApprovedDateInitialized) {
            lastApprovedDate = KEWServiceLocator.getActionTakenService().getLastApprovedDate(getDocumentId());
            lastApprovedDateInitialized = true;
        }
    }
    
    private void initializeLastModifiedDate() {
        if (!lastModifiedDateInitialized) {
            lastModifiedDate = KEWServiceLocator.getActionTakenService().getLastModifiedDate(getDocumentId());
            lastModifiedDateInitialized = true;
        }
    }

    public DocumentRouteHeaderValueActionListExtension getRouteHeader() {
        return routeHeader;
    }

    public void setRouteHeader(final DocumentRouteHeaderValueActionListExtension routeHeader) {
        this.routeHeader = routeHeader;
    }

}

