/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.kew.actionlist;

import org.kuali.kfs.kew.api.KewApiConstants;

import java.io.Serializable;
import java.util.Date;

/**
 * ==
 * CU Customization: Added a preference for controlling the visibility of the action list last modified date column.
 * ====
 */

/**
 * model for the action list filter preferences
 */
public class ActionListFilter implements Serializable {

    private static final long serialVersionUID = -365729646389290478L;
    private String filterLegend;
    private String documentTitle = "";
    private boolean excludeDocumentTitle;
    private String docRouteStatus = KewApiConstants.ALL_CODE;
    private boolean excludeRouteStatus;
    private String actionRequestCd = KewApiConstants.ALL_CODE;
    private boolean excludeActionRequestCd;
    private String groupId;
    private String groupIdString = KewApiConstants.NO_FILTERING;
    private String groupName = "";
    private boolean excludeGroupId;
    private String documentType = "";
    private boolean excludeDocumentType;
    private Date createDateFrom;
    private Date createDateTo;
    private boolean excludeCreateDate;
    private Date lastAssignedDateFrom;
    private Date lastAssignedDateTo;
    private boolean excludeLastAssignedDate;
    private Date lastModifiedDateFrom;
    private Date lastModifiedDateTo;
    private boolean excludeLastModifiedDate;
    private String delegatorId = "";
    private String primaryDelegateId = "";
    private boolean excludeDelegatorId;
    private String delegationType;
    private boolean excludeDelegationType;
    private boolean filterOn;

    public String getActionRequestCd() {
        return actionRequestCd;
    }

    public void setActionRequestCd(String actionRequestCd) {
        this.actionRequestCd = actionRequestCd;
    }

    public Date getCreateDateFrom() {
        return createDateFrom;
    }

    public void setCreateDateFrom(Date createDate) {
        this.createDateFrom = createDate;
    }

    public String getDocRouteStatus() {
        return docRouteStatus;
    }

    public void setDocRouteStatus(String docRouteStatus) {
        this.docRouteStatus = docRouteStatus;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public boolean isExcludeCreateDate() {
        return excludeCreateDate;
    }

    public void setExcludeCreateDate(boolean excludeCreateDate) {
        this.excludeCreateDate = excludeCreateDate;
    }

    public boolean isExcludeDocumentType() {
        return excludeDocumentType;
    }

    public void setExcludeDocumentType(boolean excludeDocument) {
        this.excludeDocumentType = excludeDocument;
    }

    public boolean isExcludeDocumentTitle() {
        return excludeDocumentTitle;
    }

    public void setExcludeDocumentTitle(boolean excludeDocumentTitle) {
        this.excludeDocumentTitle = excludeDocumentTitle;
    }

    public boolean isExcludeLastAssignedDate() {
        return excludeLastAssignedDate;
    }

    public void setExcludeLastAssignedDate(boolean excludeLastAssignedDate) {
        this.excludeLastAssignedDate = excludeLastAssignedDate;
    }

    public boolean isExcludeActionRequestCd() {
        return excludeActionRequestCd;
    }

    public void setExcludeActionRequestCd(boolean excludeRequestCd) {
        this.excludeActionRequestCd = excludeRequestCd;
    }

    public boolean isExcludeRouteStatus() {
        return excludeRouteStatus;
    }

    public void setExcludeRouteStatus(boolean excludeRouteStatus) {
        this.excludeRouteStatus = excludeRouteStatus;
    }

    public boolean isExcludeGroupId() {
        return excludeGroupId;
    }

    public void setExcludeGroupId(boolean excludeGroupId) {
        this.excludeGroupId = excludeGroupId;
    }

    public Date getLastAssignedDateTo() {
        return lastAssignedDateTo;
    }

    public void setLastAssignedDateTo(Date lastAssignedDate) {
        this.lastAssignedDateTo = lastAssignedDate;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getCreateDateTo() {
        return createDateTo;
    }

    public void setCreateDateTo(Date createDateTo) {
        this.createDateTo = createDateTo;
    }

    public Date getLastAssignedDateFrom() {
        return lastAssignedDateFrom;
    }

    public void setLastAssignedDateFrom(Date lastAssignedDateFrom) {
        this.lastAssignedDateFrom = lastAssignedDateFrom;
    }

    public String getDelegatorId() {
        return delegatorId;
    }

    public void setDelegatorId(String delegatorId) {
        this.delegatorId = delegatorId;
    }

    public String getPrimaryDelegateId() {
        return this.primaryDelegateId;
    }

    public void setPrimaryDelegateId(String primaryDelegateId) {
        this.primaryDelegateId = primaryDelegateId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFilterLegend() {
        return filterLegend;
    }

    public void setFilterLegend(String filterLegend) {
        this.filterLegend = filterLegend;
    }

    public String getGroupIdString() {
        return groupIdString;
    }

    public void setGroupIdString(String groupIdString) {
        this.groupIdString = groupIdString;
    }

    public boolean isExcludeDelegatorId() {
        return excludeDelegatorId;
    }

    public void setExcludeDelegatorId(boolean excludeDelegatorId) {
        this.excludeDelegatorId = excludeDelegatorId;
    }

    public String getDelegationType() {
        return delegationType;
    }

    public void setDelegationType(String delegationType) {
        this.delegationType = delegationType;
    }

    public boolean isExcludeDelegationType() {
        return excludeDelegationType;
    }

    public void setExcludeDelegationType(boolean excludeDelegationType) {
        this.excludeDelegationType = excludeDelegationType;
    }

    public boolean isFilterOn() {
        return filterOn;
    }

    public void setFilterOn(boolean filterOn) {
        this.filterOn = filterOn;
    }
    
    public Date getLastModifiedDateFrom() {
        return lastModifiedDateFrom;
    }

    public void setLastModifiedDateFrom(Date lastModifiedDateFrom) {
        this.lastModifiedDateFrom = lastModifiedDateFrom;
    }

    public Date getLastModifiedDateTo() {
        return lastModifiedDateTo;
    }

    public void setLastModifiedDateTo(Date lastModifiedDateTo) {
        this.lastModifiedDateTo = lastModifiedDateTo;
    }

    public boolean isExcludeLastModifiedDate() {
        return excludeLastModifiedDate;
    }

    public void setExcludeLastModifiedDate(boolean excludeLastModifiedDate) {
        this.excludeLastModifiedDate = excludeLastModifiedDate;
    }
}
