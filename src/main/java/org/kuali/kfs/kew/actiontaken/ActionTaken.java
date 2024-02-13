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
package org.kuali.kfs.kew.actiontaken;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.KimGroupRecipient;
import org.kuali.kfs.kew.actionrequest.PersonRecipient;
import org.kuali.kfs.kew.actionrequest.Recipient;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.util.CodeTranslator;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ====
 * CU Customization: Modified Person name references to use the potentially masked equivalents instead.
 * ====
 * 
 * Model object mapped to ojb for representing actions taken on documents by users.
 */
public class ActionTaken extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = -81505450567067594L;
    private String actionTakenId;
    private String documentId;
    private String actionTaken;
    private Timestamp actionDate;
    private String annotation = "";
    private Integer docVersion;
    private String principalId;
    private String delegatorPrincipalId;
    private String delegatorGroupId;
    private Collection<ActionRequest> actionRequests;
    private Boolean currentIndicator = Boolean.TRUE;
    private String actionDateString;

    public Person getPerson() {
        return getPersonForId(principalId);
    }

    public String getPrincipalDisplayName() {
        final Person person = KimApiServiceLocator.getPersonService().getPerson(principalId);
        if (person == null) {
            throw new IllegalArgumentException("Could not locate a person with the given principal id of " +
                                               principalId);
        }
        return person.getNameMaskedIfNecessary();
    }

    public Person getDelegatorPerson() {
        return getPersonForId(delegatorPrincipalId);
    }

    public Group getDelegatorGroup() {
        return KimApiServiceLocator.getGroupService()
                .getGroup(String.valueOf(delegatorGroupId));
    }

    public void setDelegator(final Recipient recipient) {
        if (recipient instanceof PersonRecipient) {
            setDelegatorPrincipalId(((PersonRecipient) recipient).getPrincipalId());
        } else if (recipient instanceof KimGroupRecipient) {
            setDelegatorGroupId(((KimGroupRecipient) recipient).getGroup().getId());
        } else {
            setDelegatorPrincipalId(null);
            setDelegatorGroupId(null);
        }
    }

    public boolean isForDelegator() {
        return getDelegatorPrincipalId() != null || getDelegatorGroupId() != null || getDelegatorRoleId() != null;
    }

    public String getDelegatorDisplayName() {
        if (getDelegatorPrincipalId() != null) {
            final Person person = KimApiServiceLocator.getPersonService().getPerson(delegatorPrincipalId);
            if (person == null) {
                throw new IllegalArgumentException("Could not locate a person with the given principal id of " +
                                                   delegatorPrincipalId);
            }
            return person.getNameMaskedIfNecessary();
        } else if (getDelegatorGroupId() != null) {
            return getDelegatorGroup().getName();
        } else {
            final String delegatorRoleId = getDelegatorRoleId();
            if (delegatorRoleId != null) {
                final RoleLite role = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(delegatorRoleId);
                if (role != null) {
                    return role.getName();
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }
    }

    private Person getPersonForId(final String principalId) {
        Person person = null;

        if (StringUtils.isNotBlank(principalId)) {
            person = KimApiServiceLocator.getPersonService().getPerson(principalId);
        }

        return person;
    }

    public String getActionTakenLabel() {
        return CodeTranslator.getActionTakenLabel(actionTaken);
    }

    public Collection<ActionRequest> getActionRequests() {
        if (actionRequests == null) {
            setActionRequests(new ArrayList<>());
        }
        return actionRequests;
    }

    public void setActionRequests(final Collection<ActionRequest> actionRequests) {
        this.actionRequests = actionRequests;
    }

    public Timestamp getActionDate() {
        return actionDate;
    }

    public void setActionDate(final Timestamp actionDate) {
        this.actionDate = actionDate;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(final String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getActionTakenId() {
        return actionTakenId;
    }

    public void setActionTakenId(final String actionTakenId) {
        this.actionTakenId = actionTakenId;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(final String annotation) {
        this.annotation = annotation;
    }

    public String getDelegatorPrincipalId() {
        return delegatorPrincipalId;
    }

    public void setDelegatorPrincipalId(final String delegatorPrincipalId) {
        this.delegatorPrincipalId = delegatorPrincipalId;
    }

    public String getDelegatorGroupId() {
        return delegatorGroupId;
    }

    public void setDelegatorGroupId(final String delegatorGroupId) {
        this.delegatorGroupId = delegatorGroupId;
    }

    public String getDelegatorRoleId() {
        // this could (perhaps) happen when running a simulation
        if (actionTakenId == null) {
            return null;
        }
        final ActionRequest actionRequest =
                KEWServiceLocator.getActionRequestService().getActionRequestForRole(actionTakenId);
        if (actionRequest != null) {
            return actionRequest.getRoleName();
        } else {
            return null;
        }
    }

    public Integer getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(final Integer docVersion) {
        this.docVersion = docVersion;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(final String principalId) {
        this.principalId = principalId;
    }

    public Boolean getCurrentIndicator() {
        return currentIndicator;
    }

    public void setCurrentIndicator(final Boolean currentIndicator) {
        this.currentIndicator = currentIndicator;
    }

    public Collection getRootActionRequests() {
        return getActionRequestService().getRootRequests(getActionRequests());
    }

    private ActionRequestService getActionRequestService() {
        return KEWServiceLocator.getService(KEWServiceLocator.ACTION_REQUEST_SRV);
    }

    public String getActionDateString() {
        if (actionDateString == null || actionDateString.trim().equals("")) {
            return KFSConstants.getDefaultDateFormat().format(getActionDate());
        } else {
            return actionDateString;
        }
    }

    public void setActionDateString(final String actionDateString) {
        this.actionDateString = actionDateString;
    }

    public boolean isApproval() {
        return KewApiConstants.ACTION_TAKEN_APPROVED_CD.equals(getActionTaken());
    }

    public boolean isCompletion() {
        return KewApiConstants.ACTION_TAKEN_COMPLETED_CD.equals(getActionTaken());
    }

    public ActionTaken deepCopy(final Map<Object, Object> visited) {
        if (visited.containsKey(this)) {
            return (ActionTaken) visited.get(this);
        }
        final ActionTaken copy = new ActionTaken();
        visited.put(this, copy);
        copy.actionTakenId = actionTakenId;
        copy.documentId = documentId;
        copy.actionTaken = actionTaken;
        if (actionDate != null) {
            copy.actionDate = new Timestamp(actionDate.getTime());
        }
        copy.annotation = annotation;
        copy.docVersion = docVersion;
        copy.principalId = principalId;
        copy.delegatorPrincipalId = delegatorPrincipalId;
        copy.delegatorGroupId = delegatorGroupId;
        copy.currentIndicator = currentIndicator;
        copy.versionNumber = versionNumber;
        if (actionRequests != null) {
            final List<ActionRequest> copies = new ArrayList<>();
            for (final ActionRequest actionRequest : actionRequests) {
                copies.add(actionRequest.deepCopy(visited));
            }
            copy.actionRequests = copies;
        }
        copy.actionDateString = actionDateString;
        return copy;
    }

    public boolean isSuperUserAction() {
        return KewApiConstants.ACTION_TAKEN_SU_ACTION_REQUEST_ACKNOWLEDGED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_ACTION_REQUEST_FYI_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_ACTION_REQUEST_COMPLETED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_ACTION_REQUEST_APPROVED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_DISAPPROVED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_CANCELED_CD.equals(actionTaken)
                || KewApiConstants.ACTION_TAKEN_SU_APPROVED_CD.equals(actionTaken);
    }
}
