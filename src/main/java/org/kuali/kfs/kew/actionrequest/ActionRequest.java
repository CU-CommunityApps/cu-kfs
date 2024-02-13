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
package org.kuali.kfs.kew.actionrequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.action.ActionRequestStatus;
import org.kuali.kfs.kew.api.action.RecipientType;
import org.kuali.kfs.kew.api.util.CodeTranslator;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.user.RoleRecipient;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ====
 * CU Customization: Modified Person name references to use the potentially masked equivalents instead.
 * ====
 * 
 * Bean mapped to DB. Represents ActionRequest to a workgroup, user or role.  Contains
 * references to children/parent if a member of a graph
 */
public class ActionRequest extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 8781414791855848385L;

    private static final Logger LOG = LogManager.getLogger();

    private static final String ACTION_CODE_RANK = "FKACB";
    private static final String RECIPIENT_TYPE_RANK = "RWU";
    private static final List DELEGATION_TYPE_RANK = Arrays.asList(DelegationType.SECONDARY, DelegationType.PRIMARY,
            null);

    private String actionRequestId;
    private String actionRequested;
    private String documentId;
    private String status;
    private String responsibilityId;
    private String groupId;
    private String recipientTypeCd;
    private Integer priority;
    private Integer routeLevel;
    private String actionTakenId;
    private Integer docVersion = 1;
    private Timestamp createDate;
    private String responsibilityDesc;
    private String annotation;
    private String principalId;
    private Boolean forceAction;
    private String parentActionRequestId;
    private String qualifiedRoleName;
    private String roleName;
    private String qualifiedRoleNameLabel;
    private String displayStatus;

    private String delegationTypeCode;
    private String approvePolicy;

    private ActionRequest parentActionRequest;
    private List<ActionRequest> childrenRequests = new ArrayList<>();
    private ActionTaken actionTaken;
    private boolean currentIndicator = true;
    private String createDateString;

    /* New Workflow 2.1 Field */
    // The node instance at which this request was generated
    private RouteNodeInstance nodeInstance;

    private String requestLabel;

    private boolean resolveResponsibility = true;
    private DocumentRouteHeaderValue routeHeader;
    private List<ActionItem> simulatedActionItems;

    public ActionRequest() {
        createDate = new Timestamp(System.currentTimeMillis());
    }

    public Group getGroup() {
        if (getGroupId() == null) {
            LOG.error("Attempting to get a group with a blank group id");
            return null;
        }
        return KimApiServiceLocator.getGroupService().getGroup(getGroupId());
    }

    public String getRouteLevelName() {
        return nodeInstance == null ? "Exception" : nodeInstance.getName();
    }

    public boolean isUserRequest() {
        return principalId != null;
    }

    public Person getPerson() {
        if (getPrincipalId() == null) {
            return null;
        }
        return KimApiServiceLocator.getPersonService().getPerson(getPrincipalId());
    }

    public String getDisplayName() {
        if (isUserRequest()) {
            final Person person = getPerson();
            if (person != null) {
                return person.getNameMaskedIfNecessary();
            }
        } else if (isGroupRequest()) {
            final Group group = getGroup();
            if (group != null) {
                return group.getName();
            } else {
                return getGroupId();
            }
        } else if (isRoleRequest()) {
            return getRoleName();
        }
        return "";
    }

    public Recipient getRecipient() {
        if (getPrincipalId() != null) {
            return new PersonRecipient(getPerson());
        } else if (getGroupId() != null) {
            return new KimGroupRecipient(getGroup());
        } else {
            return new RoleRecipient(getRoleName());
        }
    }

    public boolean isPending() {
        return ActionRequestStatus.INITIALIZED.getCode().equals(getStatus()) ||
                ActionRequestStatus.ACTIVATED.getCode().equals(getStatus());
    }

    public String getStatusLabel() {
        return CodeTranslator.getActionRequestStatusLabel(getStatus());
    }

    public String getActionRequestedLabel() {
        if (StringUtils.isNotBlank(getRequestLabel())) {
            return getRequestLabel();
        }
        return CodeTranslator.getActionRequestLabel(getActionRequested());
    }

    public ActionTaken getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(final ActionTaken actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getActionRequested() {
        return actionRequested;
    }

    public void setActionRequested(final String actionRequested) {
        this.actionRequested = actionRequested;
    }

    public String getActionRequestId() {
        return actionRequestId;
    }

    public void setActionRequestId(final String actionRequestId) {
        this.actionRequestId = actionRequestId;
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

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Timestamp createDate) {
        this.createDate = createDate;
    }

    public Integer getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(final Integer docVersion) {
        this.docVersion = docVersion;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(final String principalId) {
        this.principalId = principalId;
    }

    public Boolean getForceAction() {
        return forceAction;
    }

    public void setForceAction(final Boolean forceAction) {
        this.forceAction = forceAction;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priority) {
        this.priority = priority;
    }

    public String getRecipientTypeCd() {
        return recipientTypeCd;
    }

    public void setRecipientTypeCd(final String recipientTypeCd) {
        this.recipientTypeCd = recipientTypeCd;
    }

    public String getResponsibilityDesc() {
        return responsibilityDesc;
    }

    public void setResponsibilityDesc(final String responsibilityDesc) {
        this.responsibilityDesc = responsibilityDesc;
    }

    public String getResponsibilityId() {
        return responsibilityId;
    }

    public void setResponsibilityId(final String responsibilityId) {
        this.responsibilityId = responsibilityId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public Integer getRouteLevel() {
        return routeLevel;
    }

    public void setRouteLevel(final Integer routeLevel) {
        this.routeLevel = routeLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public boolean isInitialized() {
        return ActionRequestStatus.INITIALIZED.getCode().equals(getStatus());
    }

    public boolean isActive() {
        return ActionRequestStatus.ACTIVATED.getCode().equals(getStatus());
    }

    public boolean isApproveOrCompleteRequest() {
        return KewApiConstants.ACTION_REQUEST_APPROVE_REQ.equals(getActionRequested())
                || KewApiConstants.ACTION_REQUEST_COMPLETE_REQ.equals(getActionRequested());
    }

    public boolean isDone() {
        return ActionRequestStatus.DONE.getCode().equals(getStatus());
    }

    public boolean isReviewerUser() {
        return RecipientType.PRINCIPAL.getCode().equals(getRecipientTypeCd());
    }

    /**
     * Determines whether the specified principalId is in the recipient graph of this action request
     *
     * @param principalId the principal id to check
     * @return whether the specified principalId is in the recipient graph of this action request
     */
    public boolean isRecipientRoutedRequest(final String principalId) {
        // before altering this method it is used in checkRouteLogAuthentication don't break that method
        if (StringUtils.isEmpty(principalId)) {
            return false;
        }

        boolean isRecipientInGraph = false;
        if (isReviewerUser()) {
            isRecipientInGraph = getPrincipalId().equals(principalId);
        } else if (isGroupRequest()) {
            final Group group = getGroup();
            if (group == null) {
                LOG.error("Was unable to retrieve workgroup {}", this::getGroupId);
            }
            isRecipientInGraph = KimApiServiceLocator.getGroupService().isMemberOfGroup(principalId, group.getId());
        }

        for (final ActionRequest childRequest : getChildrenRequests()) {
            isRecipientInGraph = isRecipientInGraph || childRequest.isRecipientRoutedRequest(principalId);
        }

        return isRecipientInGraph;
    }

    public boolean isRecipientRoutedRequest(final Recipient recipient) {
        // before altering this method it is used in checkRouteLogAuthentication don't break that method
        if (recipient == null) {
            return false;
        }

        boolean isRecipientInGraph = false;
        if (isReviewerUser()) {
            if (recipient instanceof PersonRecipient) {
                isRecipientInGraph = getPrincipalId().equals(((PersonRecipient) recipient).getPrincipalId());
            } else if (recipient instanceof KimGroupRecipient) {
                isRecipientInGraph = KimApiServiceLocator.getGroupService()
                        .isMemberOfGroup(getPrincipalId(), ((KimGroupRecipient) recipient).getGroup().getId());
            }

        } else if (isGroupRequest()) {
            final Group group = getGroup();
            if (group == null) {
                LOG.error("Was unable to retrieve workgroup {}", this::getGroupId);
            }
            if (recipient instanceof PersonRecipient) {
                final PersonRecipient principalRecipient = (PersonRecipient) recipient;
                isRecipientInGraph = KimApiServiceLocator.getGroupService()
                        .isMemberOfGroup(principalRecipient.getPrincipalId(), group.getId());
            } else if (recipient instanceof KimGroupRecipient) {
                isRecipientInGraph = ((KimGroupRecipient) recipient).getGroup().getId().equals(group.getId());
            }
        }

        for (final ActionRequest childRequest : getChildrenRequests()) {
            isRecipientInGraph = isRecipientInGraph || childRequest.isRecipientRoutedRequest(recipient);
        }

        return isRecipientInGraph;
    }

    public boolean isGroupRequest() {
        return RecipientType.GROUP.getCode().equals(getRecipientTypeCd());
    }

    public boolean isRoleRequest() {
        return RecipientType.ROLE.getCode().equals(getRecipientTypeCd());
    }

    public boolean isAcknowledgeRequest() {
        return KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ.equals(getActionRequested());
    }

    public boolean isApproveRequest() {
        return KewApiConstants.ACTION_REQUEST_APPROVE_REQ.equals(getActionRequested());
    }

    public boolean isCompleteRequest() {
        return KewApiConstants.ACTION_REQUEST_COMPLETE_REQ.equals(getActionRequested());
    }

    public boolean isFYIRequest() {
        return KewApiConstants.ACTION_REQUEST_FYI_REQ.equals(getActionRequested());
    }

    /**
     * Allows comparison of action requests to see which is greater responsibility. -1 : indicates code 1 is lesser
     * responsibility than code 2 0 : indicates the same responsibility 1 : indicates code1 is greater responsibility
     * than code 2 The priority of action requests is as follows: fyi < acknowledge < (approve == complete)
     *
     * @param code1
     * @param code2
     * @param completeAndApproveTheSame
     * @return -1 if less than, 0 if equal, 1 if greater than
     */
    public static int compareActionCode(final String code1, final String code2, final boolean completeAndApproveTheSame) {
        int cutoff = Integer.MAX_VALUE;
        if (completeAndApproveTheSame) {
            // hacked so that APPROVE and COMPLETE are equal
            cutoff = ACTION_CODE_RANK.length() - 3;
        }
        final Integer code1Index = Math.min(ACTION_CODE_RANK.indexOf(code1), cutoff);
        final Integer code2Index = Math.min(ACTION_CODE_RANK.indexOf(code2), cutoff);
        return code1Index.compareTo(code2Index);
    }

    /**
     * Allows comparison of action requests to see which is greater responsibility. -1 : indicates type 1 is lesser
     * responsibility than type 2 0 : indicates the same responsibility 1 : indicates type1 is greater responsibility
     * than type 2
     *
     * @param type1
     * @param type2
     * @return -1 if less than, 0 if equal, 1 if greater than
     */
    public static int compareRecipientType(final String type1, final String type2) {
        final Integer type1Index = RECIPIENT_TYPE_RANK.indexOf(type1);
        final Integer type2Index = RECIPIENT_TYPE_RANK.indexOf(type2);
        return type1Index.compareTo(type2Index);
    }

    public static int compareDelegationType(final DelegationType type1, final DelegationType type2) {
        final Integer type1Index = DELEGATION_TYPE_RANK.indexOf(type1);
        final Integer type2Index = DELEGATION_TYPE_RANK.indexOf(type2);
        return type1Index.compareTo(type2Index);
    }

    public List<ActionItem> getActionItems() {
        if (simulatedActionItems == null || simulatedActionItems.isEmpty()) {
            return (List<ActionItem>) KEWServiceLocator.getActionListService().findByActionRequestId(actionRequestId);
        } else {
            return simulatedActionItems;
        }
    }

    public List<ActionItem> getSimulatedActionItems() {
        if (simulatedActionItems == null) {
            simulatedActionItems = new ArrayList<>();
        }
        return simulatedActionItems;
    }

    public void setSimulatedActionItems(final List<ActionItem> simulatedActionItems) {
        this.simulatedActionItems = simulatedActionItems;
    }

    public boolean isCurrentIndicator() {
        return currentIndicator;
    }

    public void setCurrentIndicator(final boolean currentIndicator) {
        this.currentIndicator = currentIndicator;
    }

    public String getParentActionRequestId() {
        return parentActionRequestId;
    }

    public ActionRequest getParentActionRequest() {
        return parentActionRequest;
    }

    public void setParentActionRequest(final ActionRequest parentActionRequest) {
        this.parentActionRequest = parentActionRequest;
    }

    public List<ActionRequest> getChildrenRequests() {
        return childrenRequests;
    }

    public void setChildrenRequests(final List<ActionRequest> childrenRequests) {
        this.childrenRequests = childrenRequests;
    }

    public String getQualifiedRoleName() {
        return qualifiedRoleName;
    }

    public void setQualifiedRoleName(final String roleName) {
        qualifiedRoleName = roleName;
    }

    public DelegationType getDelegationType() {
        return DelegationType.fromCode(delegationTypeCode);
    }

    public void setDelegationType(final DelegationType delegationPolicy) {
        delegationTypeCode = delegationPolicy == null ? null : delegationPolicy.getCode();
    }

    public String getDelegationTypeCode() {
        return delegationTypeCode;
    }

    public void setDelegationTypeCode(final String delegationTypeCode) {
        this.delegationTypeCode = delegationTypeCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    public String getApprovePolicy() {
        return approvePolicy;
    }

    public void setApprovePolicy(final String requestType) {
        approvePolicy = requestType;
    }

    public boolean getHasApprovePolicy() {
        return getApprovePolicy() != null;
    }

    public boolean isDeactivated() {
        return ActionRequestStatus.DONE.getCode().equals(getStatus());
    }

    public boolean hasParent() {
        return getParentActionRequest() != null;
    }

    public boolean hasChild(final ActionRequest actionRequest) {
        if (actionRequest == null) {
            return false;
        }
        final String actionRequestId = actionRequest.getActionRequestId();
        for (final Iterator<ActionRequest> iter = getChildrenRequests().iterator(); iter.hasNext(); ) {
            final ActionRequest childRequest = iter.next();
            if (childRequest.equals(actionRequest) ||
                actionRequestId != null && actionRequestId.equals(childRequest.getActionRequestId())) {
                return true;
            }
        }
        return false;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(final String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getQualifiedRoleNameLabel() {
        return qualifiedRoleNameLabel;
    }

    public void setQualifiedRoleNameLabel(final String qualifiedRoleNameLabel) {
        this.qualifiedRoleNameLabel = qualifiedRoleNameLabel;
    }

    public String getCreateDateString() {
        if (createDateString == null || createDateString.trim().equals("")) {
            return KFSConstants.getDefaultDateFormat().format(getCreateDate());
        } else {
            return createDateString;
        }
    }

    public void setCreateDateString(final String createDateString) {
        this.createDateString = createDateString;
    }

    public RouteNodeInstance getNodeInstance() {
        return nodeInstance;
    }

    public String getPotentialNodeName() {
        return getNodeInstance() == null ? "" : getNodeInstance().getName();
    }

    public void setNodeInstance(final RouteNodeInstance nodeInstance) {
        this.nodeInstance = nodeInstance;
    }

    public String getRecipientTypeLabel() {
        return RecipientType.fromCode(getRecipientTypeCd()).getLabel();
    }

    public boolean isPrimaryDelegator() {
        boolean primaryDelegator = false;
        for (final Iterator<ActionRequest> iter = childrenRequests.iterator(); iter.hasNext(); ) {
            final ActionRequest childRequest = iter.next();
            primaryDelegator = DelegationType.PRIMARY.equals(childRequest.getDelegationType()) || primaryDelegator;
        }
        return primaryDelegator;
    }

    /**
     * Used to get primary delegate names on route log in the 'Requested Of' section so primary delegate requests
     * list the delegate and not the delegator as having the request 'IN ACTION LIST'.  This method doesn't recurse
     * and therefore assume an AR structure.
     *
     * @return primary delgate requests
     */
    public List<ActionRequest> getPrimaryDelegateRequests() {
        final List<ActionRequest> primaryDelegateRequests = new ArrayList<>();
        for (final ActionRequest childRequest : childrenRequests) {
            if (DelegationType.PRIMARY.equals(childRequest.getDelegationType())) {
                if (childRequest.isRoleRequest()) {
                    for (final ActionRequest actionRequest : childRequest.getChildrenRequests()) {
                        primaryDelegateRequests.add(actionRequest);
                    }
                } else {
                    primaryDelegateRequests.add(childRequest);
                }
            }
        }
        return primaryDelegateRequests;
    }

    public boolean isAdHocRequest() {
        return KewApiConstants.ADHOC_REQUEST_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isGeneratedRequest() {
        return KewApiConstants.MACHINE_GENERATED_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isExceptionRequest() {
        return KewApiConstants.EXCEPTION_REQUEST_RESPONSIBILITY_ID.equals(getResponsibilityId());
    }

    public boolean isRouteModuleRequest() {
        // FIXME: KULRICE-5201 switched rsp_id to a varchar, so the comparison below is no longer valid
        // return getResponsibilityId() > 0;
        // TODO: KULRICE-5329 Verify that this code below makes sense
        return getResponsibilityId() != null
                && !KewApiConstants.SPECIAL_RESPONSIBILITY_ID_SET.contains(getResponsibilityId());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("actionRequestId", actionRequestId)
                .append("actionRequested", actionRequested)
                .append("documentId", documentId)
                .append("status", status)
                .append("responsibilityId", responsibilityId)
                .append("groupId", groupId)
                .append("recipientTypeCd", recipientTypeCd)
                .append("priority", priority)
                .append("routeLevel", routeLevel)
                .append("actionTakenId", actionTakenId)
                .append("docVersion", docVersion)
                .append("createDate", createDate)
                .append("responsibilityDesc", responsibilityDesc)
                .append("annotation", annotation)
                .append("versionNumber", versionNumber)
                .append("principalId", principalId)
                .append("forceAction", forceAction)
                .append("parentActionRequestId", parentActionRequestId)
                .append("qualifiedRoleName", qualifiedRoleName)
                .append("roleName", roleName)
                .append("qualifiedRoleNameLabel", qualifiedRoleNameLabel)
                .append("displayStatus", displayStatus)
                .append("delegationType", delegationTypeCode)
                .append("approvePolicy", approvePolicy)
                .append("actionTaken", actionTaken)
                .append("currentIndicator", currentIndicator)
                .append("createDateString", createDateString)
                .append("nodeInstance", nodeInstance).toString();
    }

    public String getRequestLabel() {
        return requestLabel;
    }

    public void setRequestLabel(final String requestLabel) {
        this.requestLabel = requestLabel;
    }

    public String getGroupName() {
        return KimApiServiceLocator.getGroupService().getGroup(groupId).getName();
    }

    public boolean getResolveResponsibility() {
        return resolveResponsibility;
    }

    public void setResolveResponsibility(final boolean resolveResponsibility) {
        this.resolveResponsibility = resolveResponsibility;
    }

    public DocumentRouteHeaderValue getRouteHeader() {
        if (routeHeader == null && documentId != null) {
            routeHeader = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
        }
        return routeHeader;
    }

    public void setRouteHeader(final DocumentRouteHeaderValue routeHeader) {
        this.routeHeader = routeHeader;
    }

    public ActionRequest deepCopy(final Map<Object, Object> visited) {
        if (visited.containsKey(this)) {
            return (ActionRequest) visited.get(this);
        }
        final ActionRequest copy = new ActionRequest();
        visited.put(this, copy);
        copy.actionRequestId = actionRequestId;
        copy.actionRequested = actionRequested;
        copy.documentId = documentId;
        copy.status = status;
        copy.responsibilityId = responsibilityId;
        copy.groupId = groupId;
        copy.roleName = roleName;
        copy.qualifiedRoleName = qualifiedRoleName;
        copy.qualifiedRoleNameLabel = qualifiedRoleNameLabel;
        copy.recipientTypeCd = recipientTypeCd;
        copy.priority = priority;
        copy.routeLevel = routeLevel;
        copy.docVersion = docVersion;
        if (createDate != null) {
            copy.createDate = new Timestamp(createDate.getTime());
        }
        copy.responsibilityDesc = responsibilityDesc;
        copy.annotation = annotation;
        copy.versionNumber = versionNumber;
        copy.principalId = principalId;
        copy.forceAction = forceAction;
        copy.currentIndicator = currentIndicator;
        copy.approvePolicy = approvePolicy;
        copy.delegationTypeCode = delegationTypeCode;
        copy.requestLabel = requestLabel;
        if (parentActionRequest != null) {
            copy.parentActionRequest = parentActionRequest.deepCopy(visited);
        }
        if (actionTaken != null) {
            copy.actionTaken = actionTaken.deepCopy(visited);
        }
        if (nodeInstance != null) {
            copy.nodeInstance = nodeInstance.deepCopy(visited);
        }
        if (childrenRequests != null) {
            final List<ActionRequest> copies = new ArrayList<>();
            for (final ActionRequest childRequest : childrenRequests) {
                copies.add(childRequest.deepCopy(visited));
            }
            copy.childrenRequests = copies;
        }

        copy.createDateString = createDateString;
        copy.displayStatus = displayStatus;
        copy.resolveResponsibility = resolveResponsibility;
        if (routeHeader != null) {
            copy.routeHeader = routeHeader.deepCopy(visited);
        }
        if (simulatedActionItems != null) {
            final List<ActionItem> copies = new ArrayList<>();
            for (final ActionItem simulatedActionItem : simulatedActionItems) {
                copies.add(simulatedActionItem.deepCopy(visited));
            }
            copy.simulatedActionItems = copies;
        }
        return copy;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ActionRequest)) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(obj, this);
    }
}
