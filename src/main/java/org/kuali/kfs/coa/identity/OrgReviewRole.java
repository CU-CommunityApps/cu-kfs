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
package org.kuali.kfs.coa.identity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.COAConstants;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.OrgReviewRoleService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.PredicateUtils;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.PersonImpl;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.identity.KfsKimAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Cornell Customization: backport redis*/
public class OrgReviewRole extends PersistableBusinessObjectBase implements MutableInactivatable {
    private static final Logger LOG = LogManager.getLogger();

    public static final String CACHE_NAME = "OrgReviewRole";

    protected static final String ORR_INQUIRY_TITLE_PROPERTY = "message.inquiry.org.review.role.title";
    protected static String INQUIRY_TITLE_VALUE = null;

    private static transient OrgReviewRoleService orgReviewRoleService;
    private static transient OrganizationService organizationService;
    private static transient ChartService chartService;
    private static transient BusinessObjectService businessObjectService;

    //Dummy variable
    protected String organizationTypeCode = "99";
    private static final long serialVersionUID = 1L;

    public static final String REVIEW_ROLES_INDICATOR_FIELD_NAME = "reviewRolesIndicator";
    public static final String ROLE_NAME_FIELD_NAMESPACE_CODE = "roleMemberRoleNamespaceCode";
    public static final String ROLE_NAME_FIELD_NAME = "roleMemberRoleName";
    public static final String GROUP_NAME_FIELD_NAMESPACE_CODE = "groupMemberGroupNamespaceCode";
    public static final String GROUP_NAME_FIELD_NAME = "groupMemberGroupName";
    public static final String PRINCIPAL_NAME_FIELD_NAME = "principalMemberPrincipalName";
    public static final String CHART_CODE_FIELD_NAME = KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE;
    public static final String ORG_CODE_FIELD_NAME = KFSPropertyConstants.ORGANIZATION_CODE;
    public static final String DOC_TYPE_NAME_FIELD_NAME = "financialSystemDocumentTypeCode";
    public static final String DELEGATE_FIELD_NAME = "delegate";
    public static final String DELEGATION_TYPE_CODE = "delegationTypeCode";
    public static final String FROM_AMOUNT_FIELD_NAME = "fromAmount";
    public static final String TO_AMOUNT_FIELD_NAME = "toAmount";
    public static final String OVERRIDE_CODE_FIELD_NAME = KFSPropertyConstants.OVERRIDE_CODE;
    public static final String ACTION_TYPE_CODE_FIELD_NAME = "actionTypeCode";
    public static final String PRIORITY_CODE_FIELD_NAME = "priorityNumber";
    public static final String ACTION_POLICY_CODE_FIELD_NAME = "actionPolicyCode";
    public static final String FORCE_ACTION_FIELD_NAME = "forceAction";
    public static final String ACTIVE_FROM_DATE = "activeFromDate";
    public static final String ACTIVE_TO_DATE = "activeToDate";

    public static final String ORIGINAL_DELEGATION_MEMBER_ID_TO_MODIFY = "ODelMId";
    public static final String ORIGINAL_ROLE_MEMBER_ID_TO_MODIFY = "ORMId";

    public static final String NEW_DELEGATION_ID_KEY_VALUE = "New";

    protected String methodToCall;
    protected String kimTypeId;

    protected String orgReviewRoleMemberId;
    protected Chart chart;
    protected Organization organization;
    protected boolean edit;
    protected boolean copy;

    protected Role role;
    protected Group group;
    protected PersonImpl person;

    protected List<KfsKimDocumentAttributeData> attributes = new ArrayList<>();
    protected List<RoleResponsibilityAction> roleRspActions = new ArrayList<>();

    //Identifying information for the 3 kinds of role members this document caters to
    protected String roleMemberRoleId;
    protected String roleMemberRoleNamespaceCode;
    protected String roleMemberRoleName;

    protected String groupMemberGroupId;
    protected String groupMemberGroupNamespaceCode;
    protected String groupMemberGroupName;

    protected String principalMemberPrincipalId;
    protected String principalMemberPrincipalName;
    protected String principalMemberName;

    //The role id this object corresponds to ( org review / acct review )
    protected String roleId;
    protected String namespaceCode;
    protected String roleName;

    //Identifying information for a single member (of any type)
    protected String memberTypeCode;

    //In case the document is dealing with delegations
    protected String delegationTypeCode;

    protected String delegationMemberId;
    protected String roleMemberId;

    protected String oDelMId;
    protected String oRMId;

    protected String financialSystemDocumentTypeCode;
    protected DocumentType financialSystemDocumentType;
    protected List<String> roleNamesToConsider;
    protected String reviewRolesIndicator;

    protected String actionTypeCode;
    protected String priorityNumber;
    protected String actionPolicyCode;
    protected boolean forceAction;
    protected String chartOfAccountsCode;
    protected String organizationCode;
    protected KualiDecimal fromAmount;
    protected KualiDecimal toAmount;
    protected String overrideCode;
    protected boolean active = true;
    protected boolean delegate;

    protected Date activeFromDate;
    protected Date activeToDate;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDelegate() {
        return delegate;
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public Chart getChart() {
        if (StringUtils.isBlank(getChartOfAccountsCode())) {
            chart = null;
        } else {
            if (chart == null || !StringUtils.equals(getChartOfAccountsCode(), chart.getChartOfAccountsCode())) {
                chart = getChartService().getByPrimaryId(getChartOfAccountsCode());
            }
        }
        return chart;
    }

    public String getGroupMemberGroupId() {
        if (StringUtils.isBlank(groupMemberGroupId)) {
            if (StringUtils.isNotBlank(groupMemberGroupNamespaceCode) && StringUtils.isNotBlank(groupMemberGroupName)) {
                getGroup();
            }
        }
        return groupMemberGroupId;
    }

    public void setGroupMemberGroupId(String groupMemberGroupId) {
        this.groupMemberGroupId = groupMemberGroupId;
    }

    public String getGroupMemberGroupName() {
        return groupMemberGroupName;
    }

    public void setGroupMemberGroupName(String groupMemberGroupName) {
        this.groupMemberGroupName = groupMemberGroupName;
    }

    public String getGroupMemberGroupNamespaceCode() {
        return groupMemberGroupNamespaceCode;
    }

    public void setGroupMemberGroupNamespaceCode(String groupMemberGroupNamespaceCode) {
        this.groupMemberGroupNamespaceCode = groupMemberGroupNamespaceCode;
    }

    public String getPrincipalMemberPrincipalId() {
        if (StringUtils.isBlank(principalMemberPrincipalId)) {
            if (StringUtils.isNotBlank(principalMemberPrincipalName)) {
                getPerson();
            }
        }
        return principalMemberPrincipalId;
    }

    public void setPrincipalMemberPrincipalId(String principalMemberPrincipalId) {
        this.principalMemberPrincipalId = principalMemberPrincipalId;
    }

    public String getPrincipalMemberPrincipalName() {
        if (StringUtils.isBlank(principalMemberPrincipalName)) {
            getPerson();
        }
        return principalMemberPrincipalName;
    }

    public String getPrincipalMemberName() {
        if (StringUtils.isBlank(principalMemberName)) {
            getPerson();
        }
        return principalMemberName;
    }

    public void setPrincipalMemberPrincipalName(String principalMemberPrincipalName) {
        this.principalMemberPrincipalName = principalMemberPrincipalName;
    }

    public String getRoleMemberRoleId() {
        if (StringUtils.isBlank(roleMemberRoleId)) {
            if (StringUtils.isNotBlank(roleMemberRoleName) && StringUtils.isNotBlank(roleMemberRoleName)) {
                getRole();
            }
        }
        return roleMemberRoleId;
    }

    public void setRoleMemberRoleId(String roleMemberRoleId) {
        this.roleMemberRoleId = roleMemberRoleId;
    }

    public String getRoleMemberRoleName() {
        return roleMemberRoleName;
    }

    public void setRoleMemberRoleName(String roleMemberRoleName) {
        this.roleMemberRoleName = roleMemberRoleName;
    }

    public String getRoleMemberRoleNamespaceCode() {
        return roleMemberRoleNamespaceCode;
    }

    public void setRoleMemberRoleNamespaceCode(String roleMemberRoleNamespaceCode) {
        this.roleMemberRoleNamespaceCode = roleMemberRoleNamespaceCode;
    }

    public Organization getOrganization() {
        if (StringUtils.isBlank(getChartOfAccountsCode()) || StringUtils.isBlank(getOrganizationCode())) {
            organization = null;
        } else {
            if (organization == null || !StringUtils.equals(getChartOfAccountsCode(), chart.getChartOfAccountsCode())
                    || !StringUtils.equals(getOrganizationCode(), organization.getOrganizationCode())) {
                organization = getOrganizationService().getByPrimaryIdWithCaching(getChartOfAccountsCode(), getOrganizationCode());
            }
        }
        return organization;
    }

    public String getOverrideCode() {
        return this.overrideCode;
    }

    public void setOverrideCode(String overrideCode) {
        this.overrideCode = overrideCode;
    }

    public KualiDecimal getFromAmount() {
        return fromAmount;
    }

    public String getFromAmountStr() {
        return fromAmount == null ? null : fromAmount.toString();
    }

    public void setFromAmount(KualiDecimal fromAmount) {
        this.fromAmount = fromAmount;
    }

    public void setFromAmount(String fromAmount) {
        if (StringUtils.isNotEmpty(fromAmount) && NumberUtils.isCreatable(fromAmount)) {
            this.fromAmount = new KualiDecimal(fromAmount);
        } else {
            this.fromAmount = null;
        }
    }

    public KualiDecimal getToAmount() {
        return toAmount;
    }

    public String getToAmountStr() {
        return toAmount == null ? null : toAmount.toString();
    }

    public void setToAmount(KualiDecimal toAmount) {
        this.toAmount = toAmount;
    }

    public void setToAmount(String toAmount) {
        if (StringUtils.isNotEmpty(toAmount) && NumberUtils.isCreatable(toAmount)) {
            this.toAmount = new KualiDecimal(toAmount);
        } else {
            this.toAmount = null;
        }
    }

    public Date getActiveFromDate() {
        return activeFromDate;
    }

    public void setActiveFromDate(java.util.Date activeFromDate) {
        this.activeFromDate = activeFromDate;
    }

    public Date getActiveToDate() {
        return activeToDate;
    }

    public void setActiveToDate(java.util.Date activeToDate) {
        this.activeToDate = activeToDate;
    }

    public String getOrgReviewRoleMemberId() {
        return orgReviewRoleMemberId;
    }

    public void setOrgReviewRoleMemberId(String orgReviewRoleMemberId) {
        this.orgReviewRoleMemberId = orgReviewRoleMemberId;
    }

    @Override
    public void refresh() {
    }

    public DocumentType getFinancialSystemDocumentType() {
        financialSystemDocumentType = SpringContext.getBean(DocumentTypeService.class)
                .updateDocumentTypeIfNecessary(financialSystemDocumentTypeCode, financialSystemDocumentType);
        return financialSystemDocumentType;
    }

    public String getFinancialSystemDocumentTypeCode() {
        return financialSystemDocumentTypeCode;
    }

    public void setFinancialSystemDocumentTypeCode(String financialSystemDocumentTypeCode) {
        boolean isChanged = !StringUtils.equals(this.financialSystemDocumentTypeCode, financialSystemDocumentTypeCode);
        this.financialSystemDocumentTypeCode = financialSystemDocumentTypeCode;
        setRoleNamesAndReviewIndicator(isChanged);
    }

    private void setRoleNamesAndReviewIndicator(boolean hasFinancialSystemDocumentTypeCodeChanged) {
        if (hasFinancialSystemDocumentTypeCodeChanged) {
            //If role id is populated role names to consider have already been narrowed down
            if (StringUtils.isNotBlank(getRoleId()) && StringUtils.isNotBlank(getRoleName())) {
                setRoleNamesToConsider(Collections.singletonList(getRoleName()));
            } else {
                setRoleNamesToConsider();
            }
            if (isBothReviewRolesIndicator()) {
                setReviewRolesIndicatorOnDocTypeChange(COAConstants.ORG_REVIEW_ROLE_ORG_ACC_BOTH_CODE);
            } else if (isAccountingOrgReviewRoleIndicator()) {
                setReviewRolesIndicatorOnDocTypeChange(COAConstants.ORG_REVIEW_ROLE_ORG_ACC_ONLY_CODE);
            } else if (isOrgReviewRoleIndicator()) {
                setReviewRolesIndicatorOnDocTypeChange(COAConstants.ORG_REVIEW_ROLE_ORG_ONLY_CODE);
            }
        }
    }

    public void setFinancialSystemDocumentType(DocumentType financialSystemDocumentType) {
        this.financialSystemDocumentType = financialSystemDocumentType;
    }

    public String getDelegationTypeCode() {
        return delegationTypeCode;
    }

    public String getDelegationTypeCodeDescription() {
        if (getDelegationType() != null) {
            return getDelegationType().getLabel();
        }
        return "";
    }

    public DelegationType getDelegationType() {
        return DelegationType.parseCode(delegationTypeCode);
    }

    public void setDelegationTypeCode(String delegationTypeCode) {
        this.delegationTypeCode = delegationTypeCode;
    }

    public String getMemberTypeCodeDescription() {
        return KimConstants.KimUIConstants.KIM_MEMBER_TYPES_MAP.get(getMemberTypeCode());
    }

    public void setMemberTypeCode(String memberTypeCode) {
        this.memberTypeCode = memberTypeCode;
    }

    public void setAttributes(List<KfsKimDocumentAttributeData> attributes) {
        this.attributes = attributes;
    }

    public List<KfsKimDocumentAttributeData> getAttributes() {
        return attributes;
    }

    public String getAttributeValue(String attributeName) {
        KfsKimDocumentAttributeData attributeData = getAttribute(attributeName);
        return attributeData == null ? "" : attributeData.getAttrVal();
    }

    protected KfsKimDocumentAttributeData getAttribute(String attributeName) {
        if (StringUtils.isNotBlank(attributeName)) {
            for (KfsKimDocumentAttributeData attribute : attributes) {
                if (attribute.getKimAttribute() != null
                    && StringUtils.equals(attribute.getKimAttribute().getAttributeName(), attributeName)) {
                    return attribute;
                }
            }
        }
        return null;
    }

    public String getChartOfAccountsCode() {
        return this.chartOfAccountsCode;
    }

    public String getOrganizationCode() {
        return this.organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public List<String> getRoleNamesToConsider() {
        if (roleNamesToConsider == null && getFinancialSystemDocumentTypeCode() != null) {
            setRoleNamesToConsider();
        }
        return roleNamesToConsider;
    }

    public void setRoleNamesToConsider(List<String> narrowedDownRoleNames) {
        roleNamesToConsider = new ArrayList<>(narrowedDownRoleNames);
    }

    public void setRoleNamesToConsider() {
        roleNamesToConsider = getOrgReviewRoleService().getRolesToConsider(getFinancialSystemDocumentTypeCode());
    }

    public boolean isAccountingOrgReviewRoleIndicator() {
        return getRoleNamesToConsider() != null
                && getRoleNamesToConsider().contains(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
    }

    public boolean isBothReviewRolesIndicator() {
        return getRoleNamesToConsider() != null
                && getRoleNamesToConsider().contains(KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME)
                && getRoleNamesToConsider().contains(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
    }

    public boolean isOrgReviewRoleIndicator() {
        return getRoleNamesToConsider() != null
                && getRoleNamesToConsider().contains(KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME);
    }

    public String getActionTypeCode() {
        return actionTypeCode;
    }

    public String getActionTypeCodeToDisplay() {
        if (roleRspActions == null || roleRspActions.isEmpty()) {
            return "";
        }
        return roleRspActions.get(0).getActionTypeCode();
    }

    public String getActionTypeCodeDescription() {
        WorkflowAction workflowAction = WorkflowAction.fromCode(getActionTypeCodeToDisplay(), true);
        return (workflowAction == null) ? "" : workflowAction.getLabel();
    }

    public void setActionTypeCode(String actionTypeCode) {
        this.actionTypeCode = actionTypeCode;
    }

    public String getPriorityNumber() {
        return priorityNumber;
    }

    public String getPriorityNumberToDisplay() {
        if (roleRspActions == null || roleRspActions.isEmpty()) {
            return "";
        }
        return roleRspActions.get(0).getPriorityNumber() == null ? "" : roleRspActions.get(0).getPriorityNumber() + "";
    }

    public void setPriorityNumber(String priorityNumber) {
        this.priorityNumber = priorityNumber;
    }

    public String getActionPolicyCode() {
        return actionPolicyCode;
    }

    public void setActionPolicyCode(String actionPolicyCode) {
        this.actionPolicyCode = actionPolicyCode;
    }

    public boolean isForceAction() {
        return forceAction;
    }

    public void setForceAction(boolean forceAction) {
        this.forceAction = forceAction;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        RoleLite roleInfo = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(roleId);
        if (roleInfo != null) {
            setNamespaceCode(roleInfo.getNamespaceCode());
            setRoleName(roleInfo.getName());
            setKimTypeId(roleInfo.getKimTypeId());
        }
        this.roleId = roleId;
    }

    public String getReviewRolesIndicator() {
        return reviewRolesIndicator;
    }

    public void setReviewRolesIndicator(String reviewRolesIndicator) {
        this.reviewRolesIndicator = reviewRolesIndicator;
    }

    private void setReviewRolesIndicatorOnDocTypeChange(String reviewRolesIndicator) {
        this.reviewRolesIndicator = reviewRolesIndicator;
    }

    public boolean hasRole() {
        getRole();
        return StringUtils.isNotBlank(roleMemberRoleName);
    }

    public boolean hasGroup() {
        getGroup();
        return StringUtils.isNotBlank(groupMemberGroupName);
    }

    public boolean hasPrincipal() {
        getPerson();
        return StringUtils.isNotBlank(principalMemberPrincipalName);
    }

    public boolean hasAnyMember() {
        return hasRole() || hasGroup() || hasPrincipal();
    }

    public void setRoleMember(RoleMember roleMember) {
        memberTypeCode = roleMember.getType().getCode();
        if (MemberType.ROLE.equals(roleMember.getType())) {
            roleMemberRoleId = roleMember.getMemberId();
            roleMemberRoleNamespaceCode = roleMember.getMemberNamespaceCode();
            roleMemberRoleName = roleMember.getMemberName();
        } else if (MemberType.GROUP.equals(roleMember.getType())) {
            groupMemberGroupId = roleMember.getMemberId();
            groupMemberGroupNamespaceCode = roleMember.getMemberNamespaceCode();
            groupMemberGroupName = roleMember.getMemberName();
        } else if (MemberType.PRINCIPAL.equals(roleMember.getType())) {
            principalMemberPrincipalId = roleMember.getMemberId();
            principalMemberPrincipalName = roleMember.getMemberName();
        }

        if (roleMember.getActiveFromDate() != null) {
            setActiveFromDate(roleMember.getActiveFromDate().toDate());
        } else {
            setActiveFromDate(null);
        }
        if (roleMember.getActiveToDate() != null) {
            setActiveToDate(roleMember.getActiveToDate().toDate());
        } else {
            setActiveToDate(null);
        }
        setActive(roleMember.isActive());

        setRoleMemberId(roleMember.getId());
        setDelegate(false);
        setRoleId(roleMember.getRoleId());

        setRoleRspActions(KimApiServiceLocator.getRoleService().getRoleMemberResponsibilityActions(roleMember.getId()));

        extractAttributesFromMap(roleMember.getAttributes());
    }

    public void extractAttributesFromMap(Map<String, String> attributes) {
        setAttributes(getAttributeSetAsQualifierList(attributes));

        setChartOfAccountsCode(getAttributeValue(KfsKimAttributes.CHART_OF_ACCOUNTS_CODE));
        setOrganizationCode(getAttributeValue(KfsKimAttributes.ORGANIZATION_CODE));
        setOverrideCode(getAttributeValue(KfsKimAttributes.ACCOUNTING_LINE_OVERRIDE_CODE));
        setFromAmount(getAttributeValue(KfsKimAttributes.FROM_AMOUNT));
        setToAmount(getAttributeValue(KfsKimAttributes.TO_AMOUNT));
        setFinancialSystemDocumentTypeCode(getAttributeValue(KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME));
    }

    public void setDelegateMember(RoleMember roleMember, DelegateMember delegateMember) {
        if (roleMember == null) {
            roleMember = getRoleMemberFromKimRoleService(delegateMember.getRoleMemberId());
        }
        setRoleId(roleMember.getRoleId());
        memberTypeCode = delegateMember.getType().getCode();
        if (MemberType.ROLE.equals(delegateMember.getType())) {
            roleMemberRoleId = delegateMember.getMemberId();
            getRole();
        } else if (MemberType.GROUP.equals(delegateMember.getType())) {
            groupMemberGroupId = delegateMember.getMemberId();
            getGroup();
        } else if (MemberType.PRINCIPAL.equals(delegateMember.getType())) {
            principalMemberPrincipalId = delegateMember.getMemberId();
            getPerson();
        }

        if (delegateMember.getActiveFromDate() != null) {
            setActiveFromDate(delegateMember.getActiveFromDate().toDate());
        }
        if (delegateMember.getActiveToDate() != null) {
            setActiveToDate(delegateMember.getActiveToDate().toDate());
        }
        setActive(delegateMember.isActive());
        setDelegate(true);
        setDelegationMemberId(delegateMember.getDelegationMemberId());
        setRoleMemberId(roleMember.getId());

        LOG.debug("populating org review role from delegate member: " + delegateMember.getType().code + " " +
                delegateMember.getMemberId() + " delegate for " + delegateMember.getRoleMemberId());
        extractAttributesFromMap(delegateMember.getAttributes());
    }

    protected RoleMember getRoleMemberFromKimRoleService(String roleMemberId) {
        GenericQueryResults<RoleMember> roleMembers = KimApiServiceLocator.getRoleService().findRoleMembers(
                QueryByCriteria.Builder.fromPredicates(PredicateUtils.convertMapToPredicate(
                        Collections.singletonMap(KimConstants.PrimaryKeyConstants.ID, roleMemberId))));
        if (roleMembers == null || roleMembers.getResults() == null || roleMembers.getResults().isEmpty()) {
            throw new IllegalArgumentException("Unknown role member ID passed in - nothing returned from KIM RoleService: " +
                    roleMemberId);
        }
        return roleMembers.getResults().get(0);
    }

    public String getMemberId() {
        if (MemberType.ROLE.getCode().equals(getMemberTypeCode())) {
            return getRoleMemberRoleId();
        } else if (MemberType.GROUP.getCode().equals(getMemberTypeCode())) {
            return getGroupMemberGroupId();
        } else if (MemberType.PRINCIPAL.getCode().equals(getMemberTypeCode())) {
            return getPrincipalMemberPrincipalId();
        }
        return "";
    }

    public String getMemberName() {
        if (MemberType.ROLE.getCode().equals(getMemberTypeCode())) {
            return getRoleMemberRoleName();
        } else if (MemberType.GROUP.getCode().equals(getMemberTypeCode())) {
            return getGroupMemberGroupName();
        } else if (MemberType.PRINCIPAL.getCode().equals(getMemberTypeCode())) {
            return getPrincipalMemberName();
        }
        return "";
    }

    public String getMemberNamespaceCode() {
        if (MemberType.ROLE.getCode().equals(getMemberTypeCode())) {
            return getRoleMemberRoleNamespaceCode();
        } else if (MemberType.GROUP.getCode().equals(getMemberTypeCode())) {
            return getGroupMemberGroupNamespaceCode();
        } else if (MemberType.PRINCIPAL.getCode().equals(getMemberTypeCode())) {
            return "";
        }
        return "";
    }

    public String getMemberFieldName() {
        if (MemberType.ROLE.equals(getMemberType())) {
            return ROLE_NAME_FIELD_NAME;
        } else if (MemberType.GROUP.equals(getMemberType())) {
            return GROUP_NAME_FIELD_NAME;
        } else if (MemberType.PRINCIPAL.equals(getMemberType())) {
            return PRINCIPAL_NAME_FIELD_NAME;
        }
        return null;
    }

    public String getMemberTypeCode() {
        if (StringUtils.isBlank(memberTypeCode)) {
            if (StringUtils.isNotBlank(principalMemberPrincipalId)) {
                memberTypeCode = MemberType.PRINCIPAL.getCode();
            } else if (StringUtils.isNotBlank(groupMemberGroupId)) {
                memberTypeCode = MemberType.GROUP.getCode();
            } else if (StringUtils.isNotBlank(roleMemberRoleId)) {
                memberTypeCode = MemberType.ROLE.getCode();
            }
        }
        return memberTypeCode;
    }

    public MemberType getMemberType() {
        if (StringUtils.isBlank(getMemberTypeCode())) {
            return null;
        }
        return MemberType.fromCode(getMemberTypeCode());
    }

    public Group getGroup() {
        if ((group == null || !StringUtils.equals(group.getId(), groupMemberGroupId))
                && StringUtils.isNotBlank(groupMemberGroupId)) {
            group = getBusinessObjectService().findBySinglePrimaryKey(Group.class, groupMemberGroupId);
            groupMemberGroupNamespaceCode = group.getNamespaceCode();
            groupMemberGroupName = group.getName();
        } else if (StringUtils.isNotBlank(groupMemberGroupName)) {
            // if we have both a namespace and a name
            if (StringUtils.isNotBlank(groupMemberGroupNamespaceCode)) {
                Map<String, Object> keys = new HashMap<>(2);
                keys.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, groupMemberGroupNamespaceCode);
                keys.put(KimConstants.UniqueKeyConstants.GROUP_NAME, groupMemberGroupName);
                List<Group> groups = (List<Group>) getBusinessObjectService().findMatching(Group.class, keys);
                // this *should* only retrieve a single record
                if (groups != null && !groups.isEmpty()) {
                    group = groups.get(0);
                    groupMemberGroupId = group.getId();
                } else {
                    group = null;
                    groupMemberGroupId = "";
                }
            } else {
                // if we only have the name - see if it's unique
                Map<String, Object> keys = new HashMap<>(1);
                keys.put(KimConstants.UniqueKeyConstants.GROUP_NAME, groupMemberGroupName);
                List<Group> groups = (List<Group>) getBusinessObjectService().findMatching(Group.class, keys);
                // if retrieves a single record, then it's unique, we set it and the namespace
                if (groups != null && groups.size() == 1) {
                    group = groups.get(0);
                    groupMemberGroupId = group.getId();
                    groupMemberGroupNamespaceCode = group.getNamespaceCode();
                } else {
                    group = null;
                    groupMemberGroupId = "";
                }
            }
        } else {
            group = null;
        }
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
        if (group != null) {
            groupMemberGroupNamespaceCode = group.getNamespaceCode();
            groupMemberGroupName = group.getName();
            groupMemberGroupId = group.getId();
        } else {
            groupMemberGroupNamespaceCode = "";
            groupMemberGroupName = "";
            groupMemberGroupId = "";
        }
    }

    public PersonImpl getPerson() {
        if ((StringUtils.isNotEmpty(principalMemberPrincipalId) || StringUtils.isNotEmpty(principalMemberPrincipalName))
                && (person == null || !StringUtils.equals(person.getPrincipalId(), principalMemberPrincipalId))) {
            if (StringUtils.isNotEmpty(principalMemberPrincipalId)) {
                person = (PersonImpl) KimApiServiceLocator.getPersonService().getPerson(principalMemberPrincipalId);
            } else if (StringUtils.isNotEmpty(principalMemberPrincipalName)) {
                person = (PersonImpl) KimApiServiceLocator.getPersonService()
                        .getPersonByPrincipalName(principalMemberPrincipalName);
            } else {
                person = null;
            }
            if (person != null) {
                principalMemberPrincipalId = person.getPrincipalId();
                principalMemberPrincipalName = person.getPrincipalName();
                principalMemberName = person.getName();
            } else {
                principalMemberPrincipalId = "";
                principalMemberName = "";
            }
        }
        return person;
    }

    public void setPerson(PersonImpl person) {
        this.person = person;
        if (person != null) {
            principalMemberPrincipalName = person.getPrincipalName();
            principalMemberPrincipalId = person.getPrincipalId();
            principalMemberName = person.getName();
        } else {
            principalMemberPrincipalId = "";
            principalMemberPrincipalName = "";
            principalMemberName = "";
        }
    }

    public Role getRole() {
        if ((role == null || !StringUtils.equals(role.getId(), roleMemberRoleId))
                && StringUtils.isNotBlank(roleMemberRoleId)) {
            Map<String, Object> keys = new HashMap<>(1);
            keys.put(KimConstants.PrimaryKeyConstants.ROLE_ID, roleMemberRoleId);
            role = getBusinessObjectService().findBySinglePrimaryKey(Role.class, roleMemberRoleId);
            roleMemberRoleNamespaceCode = role.getNamespaceCode();
            roleMemberRoleName = role.getName();
        } else if (StringUtils.isNotBlank(roleMemberRoleName)) {
            // if we have both a namespace and a name
            if (StringUtils.isNotBlank(roleMemberRoleNamespaceCode)) {
                Map<String, Object> keys = new HashMap<>(2);
                keys.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, roleMemberRoleNamespaceCode);
                keys.put(KimConstants.UniqueKeyConstants.NAME, roleMemberRoleName);
                List<Role> roles = (List<Role>) getBusinessObjectService().findMatching(Role.class, keys);
                // this *should* only retrieve a single record
                if (roles != null && !roles.isEmpty()) {
                    role = roles.get(0);
                    roleMemberRoleId = role.getId();
                } else {
                    role = null;
                    roleMemberRoleId = "";
                }
            } else {
                // if we only have the name - see if it's unique
                Map<String, Object> keys = new HashMap<>(1);
                keys.put(KimConstants.UniqueKeyConstants.NAME, roleMemberRoleName);
                List<Role> roles = (List<Role>) getBusinessObjectService().findMatching(Role.class, keys);
                // if retrieves a single record, then it's unique, we set it and the namespace
                if (roles != null && roles.size() == 1) {
                    role = roles.get(0);
                    roleMemberRoleId = role.getId();
                    roleMemberRoleNamespaceCode = role.getNamespaceCode();
                } else {
                    role = null;
                    roleMemberRoleId = "";
                }
            }
        } else {
            role = null;
        }
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
        if (role != null) {
            roleMemberRoleNamespaceCode = role.getNamespaceCode();
            roleMemberRoleName = role.getName();
            roleMemberRoleId = role.getId();
        } else {
            roleMemberRoleNamespaceCode = "";
            roleMemberRoleName = "";
            roleMemberRoleId = "";
        }
    }

    public boolean isCopy() {
        return copy || KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL.equalsIgnoreCase(methodToCall);
    }

    public void setCopy(boolean copy) {
        this.copy = copy;
    }

    public boolean isEdit() {
        return edit || KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL.equalsIgnoreCase(methodToCall);
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public String getODelMId() {
        return oDelMId;
    }

    public void setODelMId(String delMId) {
        oDelMId = delMId;
    }

    public String getORMId() {
        return oRMId;
    }

    public void setORMId(String id) {
        oRMId = id;
    }

    public String getDelegationMemberId() {
        return delegationMemberId;
    }

    public void setDelegationMemberId(String delegationMemberId) {
        this.delegationMemberId = delegationMemberId;
    }

    public String getRoleMemberId() {
        return roleMemberId;
    }

    public void setRoleMemberId(String roleMemberId) {
        this.roleMemberId = roleMemberId;
    }

    public String getMethodToCall() {
        return methodToCall;
    }

    public void setMethodToCall(String methodToCall) {
        this.methodToCall = methodToCall;
    }

    public boolean isEditDelegation() {
        return isEdit() && isDelegate();
    }

    public boolean isEditRoleMember() {
        return isEdit() && !isDelegate();
    }

    public boolean isCopyDelegation() {
        return isCopy() && isDelegate();
    }

    public boolean isCopyRoleMember() {
        return isCopy() && !isDelegate();
    }

    public boolean isCreateDelegation() {
        return NEW_DELEGATION_ID_KEY_VALUE.equals(getODelMId()) || (isEditDelegation()
                && StringUtils.isBlank(getDelegationMemberId()));
    }

    public boolean isCreateRoleMember() {
        return StringUtils.isEmpty(methodToCall);
    }

    public String getOrganizationTypeCode() {
        return "99";
    }

    public void setOrganizationTypeCode(String organizationTypeCode) {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
        setRoleNamesToConsider(Collections.singletonList(roleName));
    }

    public String getNamespaceCode() {
        return namespaceCode;
    }

    public void setNamespaceCode(String namespaceCode) {
        this.namespaceCode = namespaceCode;
    }

    @Override
    public Long getVersionNumber() {
        return 1L;
    }

    public String getKimTypeId() {
        return kimTypeId;
    }

    public void setKimTypeId(String kimTypeId) {
        this.kimTypeId = kimTypeId;
    }

    public Map<String, String> getQualifierAsAttributeSet(List<KfsKimDocumentAttributeData> qualifiers) {
        Map<String, String> m = new HashMap<>();
        for (KfsKimDocumentAttributeData data : qualifiers) {
            m.put(data.getKimAttribute().getAttributeName(), data.getAttrVal());
        }
        return m;
    }

    public List<KfsKimDocumentAttributeData> getAttributeSetAsQualifierList(Map<String, String> qualifiers) {
        KimType kimTypeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(kimTypeId);
        List<KfsKimDocumentAttributeData> attributesList = new ArrayList<>();
        KfsKimDocumentAttributeData attribData;
        if (LOG.isDebugEnabled()) {
            LOG.debug("passed qualifiers: " + StringUtils.join(qualifiers.keySet(), ", "));
        }
        for (String key : qualifiers.keySet()) {
            KimTypeAttribute attribInfo = kimTypeInfo.getAttributeDefinitionByName(key);
            if (attribInfo == null) {
                LOG.debug("attribute info for qualifier " + key + " is null");
            }
            attribData = new KfsKimDocumentAttributeData();
            attribData.setKimAttribute(attribInfo.getKimAttribute());
            attribData.setKimTypId(kimTypeInfo.getId());
            attribData.setKimAttrDefnId(attribInfo.getId());
            //attribData.setAttrDataId(attrDataId) - Not Available
            attribData.setAttrVal(qualifiers.get(key));
            attributesList.add(attribData);
        }
        return attributesList;
    }

    public List<RoleResponsibilityAction> getRoleRspActions() {
        if (roleRspActions == null) {
            roleRspActions = new ArrayList<>(1);
        }
        return roleRspActions;
    }

    public void setRoleRspActions(List<RoleResponsibilityAction> roleRspActions) {
        this.roleRspActions = roleRspActions;
    }

    public String getOrgReviewRoleInquiryTitle() {
        if (INQUIRY_TITLE_VALUE == null) {
            INQUIRY_TITLE_VALUE = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(ORR_INQUIRY_TITLE_PROPERTY);
        }
        return INQUIRY_TITLE_VALUE;
    }

    @Override
    public void refreshNonUpdateableReferences() {
        // do nothing
    }

    @Override
    public void refreshReferenceObject(String referenceObjectName) {
        // do nothing
    }

    protected static OrgReviewRoleService getOrgReviewRoleService() {
        if (orgReviewRoleService == null) {
            orgReviewRoleService = SpringContext.getBean(OrgReviewRoleService.class);
        }
        return orgReviewRoleService;
    }

    protected static ChartService getChartService() {
        if (chartService == null) {
            chartService = SpringContext.getBean(ChartService.class);
        }
        return chartService;
    }

    protected static OrganizationService getOrganizationService() {
        if (organizationService == null) {
            organizationService = SpringContext.getBean(OrganizationService.class);
        }
        return organizationService;
    }

    protected static BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }
}
