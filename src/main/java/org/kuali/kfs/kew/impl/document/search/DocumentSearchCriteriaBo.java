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
package org.kuali.kfs.kew.impl.document.search;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.search.DocumentSearchResult;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.context.SpringContext;

import java.sql.Timestamp;

/**
 * ====
 * CU Customization: Modified Person name references to use the potentially masked equivalents instead.
 * ====
 * 
 * Defines the business object that specifies the criteria used on document searches.
 */
public class DocumentSearchCriteriaBo implements BusinessObject {

    private String documentTypeName;
    private String documentId;
    private String statusCode;
    private String applicationDocumentId;
    private String applicationDocumentStatus;
    private String title;
    private String initiatorPrincipalName;
    private String initiatorPrincipalId;
    private String viewerPrincipalName;
    private String viewerPrincipalId;
    private String groupViewerName;
    private String groupViewerId;
    private String approverPrincipalName;
    private String approverPrincipalId;
    private String routeNodeName;
    private String routeNodeLogic;
    private Timestamp dateCreated;
    private Timestamp dateLastModified;
    private Timestamp dateApproved;
    private Timestamp dateFinalized;
    private Timestamp dateApplicationDocumentStatusChanged;
    private String saveName;

    protected Person initiatorPerson;
    protected Person approverPerson;
    protected Person viewerPerson;

    @Override
    public void refresh() {
        // nothing to refresh
    }

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(final String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(final String statusCode) {
        this.statusCode = statusCode;
    }

    public String getApplicationDocumentId() {
        return applicationDocumentId;
    }

    public void setApplicationDocumentId(final String applicationDocumentId) {
        this.applicationDocumentId = applicationDocumentId;
    }

    public String getApplicationDocumentStatus() {
        return applicationDocumentStatus;
    }

    public void setApplicationDocumentStatus(final String applicationDocumentStatus) {
        this.applicationDocumentStatus = applicationDocumentStatus;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getInitiatorPrincipalName() {
        return initiatorPrincipalName;
    }

    public void setInitiatorPrincipalName(final String initiatorPrincipalName) {
        this.initiatorPrincipalName = initiatorPrincipalName;
    }

    public String getInitiatorPrincipalId() {
        return initiatorPrincipalId;
    }

    public void setInitiatorPrincipalId(final String initiatorPrincipalId) {
        this.initiatorPrincipalId = initiatorPrincipalId;
    }

    public String getViewerPrincipalName() {
        return viewerPrincipalName;
    }

    public void setViewerPrincipalName(final String viewerPrincipalName) {
        this.viewerPrincipalName = viewerPrincipalName;
    }

    public String getViewerPrincipalId() {
        return viewerPrincipalId;
    }

    public void setViewerPrincipalId(final String viewerPrincipalId) {
        this.viewerPrincipalId = viewerPrincipalId;
    }

    public String getGroupViewerName() {
        return groupViewerName;
    }

    public void setGroupViewerName(final String groupViewerName) {
        this.groupViewerName = groupViewerName;
    }

    public String getGroupViewerId() {
        return groupViewerId;
    }

    public void setGroupViewerId(final String groupViewerId) {
        this.groupViewerId = groupViewerId;
    }

    public String getApproverPrincipalName() {
        return approverPrincipalName;
    }

    public void setApproverPrincipalName(final String approverPrincipalName) {
        this.approverPrincipalName = approverPrincipalName;
    }

    public String getApproverPrincipalId() {
        return approverPrincipalId;
    }

    public void setApproverPrincipalId(final String approverPrincipalId) {
        this.approverPrincipalId = approverPrincipalId;
    }

    public String getRouteNodeName() {
        return routeNodeName;
    }

    public void setRouteNodeName(final String routeNodeName) {
        this.routeNodeName = routeNodeName;
    }

    public String getRouteNodeLogic() {
        return routeNodeLogic;
    }

    public void setRouteNodeLogic(final String routeNodeLogic) {
        this.routeNodeLogic = routeNodeLogic;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(final Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(final Timestamp dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    public Timestamp getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(final Timestamp dateApproved) {
        this.dateApproved = dateApproved;
    }

    public Timestamp getDateFinalized() {
        return dateFinalized;
    }

    public void setDateFinalized(final Timestamp dateFinalized) {
        this.dateFinalized = dateFinalized;
    }

    public Timestamp getDateApplicationDocumentStatusChanged() {
        return dateApplicationDocumentStatusChanged;
    }

    public void setDateApplicationDocumentStatusChanged(final Timestamp dateApplicationDocumentStatusChanged) {
        this.dateApplicationDocumentStatusChanged = dateApplicationDocumentStatusChanged;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(final String saveName) {
        this.saveName = saveName;
    }

    public DocumentType getDocumentType() {
        if (documentTypeName == null) {
            return null;
        }
        return KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
    }

    public Person getInitiatorPerson() {
        return initiatorPerson;
    }

    /**
     * Gets the initiators display name, if the  principal is not found the the initiator principal id is returned.
     *
     * @return The principal composite name if it exists, otherwise the initiator principal id
     */
    public String getInitiatorDisplayName() {
        if (StringUtils.isNotBlank(initiatorPrincipalId)) {
            final Person person = SpringContext.getBean(PersonService.class).getPerson(getInitiatorPrincipalId());
            // ==== CU Customization: Return potentially masked Person name instead. ====
            return person == null ? initiatorPrincipalId : person.getNameMaskedIfNecessary();
        }

        return initiatorPrincipalId;
    }

    public Person getApproverPerson() {
        return approverPerson;
    }

    public Person getViewerPerson() {
        return viewerPerson;
    }

    public Group getGroupViewer() {
        if (groupViewerId == null) {
            return null;
        }
        return KimApiServiceLocator.getGroupService().getGroup(groupViewerId);
    }

    public String getStatusLabel() {
        if (statusCode == null) {
            return "";
        }
        return DocumentStatus.fromCode(statusCode).getLabel();
    }

    public String getDocumentTypeLabel() {
        final DocumentType documentType = getDocumentType();
        if (documentType != null) {
            return documentType.getLabel();
        }
        return "";
    }

    public String getRouteLog() {
        return "View";
    }

    public void populateFromDocumentSearchResult(final DocumentSearchResult result) {
        final DocumentRouteHeaderValue document = result.getDocument();
        documentTypeName = document.getDocumentTypeName();
        documentId = document.getDocumentId();
        statusCode = document.getStatus().getCode();
        applicationDocumentId = document.getApplicationDocumentId();
        applicationDocumentStatus = document.getApplicationDocumentStatus();
        title = document.getTitle();
        initiatorPrincipalName = principalIdToName(document.getInitiatorPrincipalId());
        initiatorPrincipalId = document.getInitiatorPrincipalId();
        dateCreated = SpringContext.getBean(DateTimeService.class).getTimestamp(document.getDateCreated());
    }

    /**
     * Returns the principal name for the given principal id, if the Person is not found then the principal id is
     * returned.
     *
     * @param principalId the unique identifier for the Person
     * @return the principal name for the given principal id, if the Person is not found then the principal id is
     * returned.
     */
    private String principalIdToName(final String principalId) {
        if (StringUtils.isNotBlank(principalId)) {
            final Person person = KimApiServiceLocator.getPersonService().getPerson(principalId);
            if (person != null) {
                return person.getPrincipalName();
            }
        }
        return principalId;
    }

}
