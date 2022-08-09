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
package org.kuali.kfs.coa.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.kfs.coa.COAConstants;
import org.kuali.kfs.coa.COAKeyConstants;
import org.kuali.kfs.kim.impl.role.RoleMemberAttributeData;
import org.kuali.kfs.coa.identity.OrgReviewRole;
import org.kuali.kfs.coa.service.OrgReviewRoleService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.PredicateUtils;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.common.attribute.KimAttribute;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.kim.util.KimCommonUtils;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kim.bo.impl.KimAttributes;
import org.springframework.cache.annotation.Cacheable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Cornell Customization: backport redis*/
public class OrgReviewRoleServiceImpl implements OrgReviewRoleService {
    private static final Logger LOG = LogManager.getLogger();

    // note: this assumes that all use the KFS-SYS namespace
    protected static final Map<String, RoleLite> ROLE_CACHE = new HashMap<>();
    protected static final Map<String, Map<String, KimAttribute>> ATTRIBUTE_CACHE = new HashMap<>();

    protected Set<String> potentialParentDocumentTypeNames = new HashSet<>();
    {
        potentialParentDocumentTypeNames.add(KFSConstants.FINANCIAL_SYSTEM_TRANSACTIONAL_DOCUMENT);
        potentialParentDocumentTypeNames.add(KFSConstants.FINANCIAL_SYSTEM_COMPLEX_MAINTENANCE_DOCUMENT);
        potentialParentDocumentTypeNames.add(KFSConstants.FINANCIAL_SYSTEM_SIMPLE_MAINTENANCE_DOCUMENT);
        potentialParentDocumentTypeNames = Collections.unmodifiableSet(potentialParentDocumentTypeNames);
    }

    protected DocumentTypeService documentTypeService;

    @Override
    public RoleMember getRoleMemberFromKimRoleService(String roleMemberId) {
        if (StringUtils.isEmpty(roleMemberId)) {
            throw new IllegalArgumentException("Role member ID may not be blank.");
        }
        GenericQueryResults<RoleMember> roleMembers = KimApiServiceLocator.getRoleService().findRoleMembers(
                QueryByCriteria.Builder.fromPredicates(PredicateUtils.convertMapToPredicate(
                        Collections.singletonMap(KimConstants.PrimaryKeyConstants.ID, roleMemberId))));
        if (roleMembers == null || roleMembers.getResults() == null || roleMembers.getResults().isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown role member ID passed in - nothing returned from KIM RoleService: "
                            + roleMemberId);
        }
        return roleMembers.getResults().get(0);
    }

    @Override
    public void populateOrgReviewRoleFromRoleMember(OrgReviewRole orr, String roleMemberId) {
        if (StringUtils.isBlank(roleMemberId)) {
            throw new IllegalArgumentException("Role member ID may not be blank");
        }
        RoleMember roleMember = getRoleMemberFromKimRoleService(roleMemberId);
        orr.setRoleMember(roleMember);

        populateObjectExtras(orr);
    }

    @Override
    public void populateOrgReviewRoleFromDelegationMember(OrgReviewRole orr, String roleMemberId,
            String delegationMemberId) {
        RoleMember roleMember = null;
        if (StringUtils.isNotBlank(roleMemberId)) {
            roleMember = getRoleMemberFromKimRoleService(roleMemberId);
        }
        RoleService roleService = KimApiServiceLocator.getRoleService();
        DelegateMember delegationMember = roleService.getDelegationMemberById(delegationMemberId);
        DelegateType delegation = roleService.getDelegateTypeByDelegationId(delegationMember.getDelegationId());

        orr.setDelegationTypeCode(delegation.getDelegationType().getCode());
        orr.setDelegateMember(roleMember, delegationMember);

        orr.setRoleRspActions(roleService.getRoleMemberResponsibilityActions(delegationMember.getRoleMemberId()));

        populateObjectExtras(orr);
    }

    protected void populateObjectExtras(OrgReviewRole orr) {
        if (!orr.getRoleRspActions().isEmpty()) {
            orr.setActionTypeCode(orr.getRoleRspActions().get(0).getActionTypeCode());
            orr.setPriorityNumber(orr.getRoleRspActions().get(0).getPriorityNumber() == null ? "" : String
                    .valueOf(orr.getRoleRspActions().get(0).getPriorityNumber()));
            orr.setActionPolicyCode(orr.getRoleRspActions().get(0).getActionPolicyCode());
            orr.setForceAction(orr.getRoleRspActions().get(0).isForceAction());
        }
    }

    @Override
    @Cacheable(cacheNames = OrgReviewRole.CACHE_NAME, key = "'{ValidDocumentTypeForOrgReview}'+#p0")
    public boolean isValidDocumentTypeForOrgReview(String documentTypeName) {
        if (StringUtils.isEmpty(documentTypeName)) {
            return false;
        }

        return !getRolesToConsider(documentTypeName).isEmpty();
    }

    @Override
    public void validateDocumentType(String documentTypeName) throws ValidationException {
        if (getRolesToConsider(documentTypeName).isEmpty()) {
            GlobalVariables.getMessageMap().putError(OrgReviewRole.DOC_TYPE_NAME_FIELD_NAME,
                    COAKeyConstants.ERROR_DOCUMENT_ORGREVIEW_INVALID_DOCUMENT_TYPE, documentTypeName);
        }
    }

    @Override
    @Cacheable(cacheNames = OrgReviewRole.CACHE_NAME, key = "'{hasOrganizationHierarchy}'+#p0")
    public boolean hasOrganizationHierarchy(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            return false;
        }
        return getDocumentTypeService().hasRouteNodeForDocumentTypeName(
                KFSConstants.RouteLevelNames.ORGANIZATION_HIERARCHY, documentTypeName);
    }

    @Override
    @Cacheable(cacheNames = OrgReviewRole.CACHE_NAME, key = "'{hasAccountingOrganizationHierarchy}'+#p0")
    public boolean hasAccountingOrganizationHierarchy(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            return false;
        }
        return getDocumentTypeService().hasRouteNodeForDocumentTypeName(
                KFSConstants.RouteLevelNames.ACCOUNTING_ORGANIZATION_HIERARCHY, documentTypeName);
    }

    @Override
    @Cacheable(cacheNames = OrgReviewRole.CACHE_NAME, key = "'{ClosestOrgReviewRoleParentDocumentTypeName}'+#p0")
    public String getClosestOrgReviewRoleParentDocumentTypeName(final String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            return null;
        }
        return KimCommonUtils.getClosestParentDocumentTypeName(
                getDocumentTypeService().getDocumentTypeByName(documentTypeName), potentialParentDocumentTypeNames);
    }

    /**
     * 1. Check WorkflowInfo.hasNode(documentTypeName, nodeName) to see if the document type selected has
     * OrganizationHierarchy and/or AccountingOrganizationHierarchy - if it has either or both,
     * set the Review Types radio group appropriately and make it read only.
     * 2. Else, if KFS is the document type selected, set the Review Types radio group to both and leave it editable.
     * 3. Else, if FinancialSystemTransactionalDocument is the closest parent (per KimCommonUtils.getClosestParent),
     * set the Review Types radio group to Organization Accounting Only and leave it editable.
     * 4. Else, if FinancialSystemComplexMaintenanceDocument is the closest parent (per KimCommonUtils.getClosestParent),
     * set the Review Types radio group to Organization Only and make read-only.
     * 5. Else, if FinancialSystemSimpleMaintenanceDocument is the closest parent (per KimCommonUtils.getClosestParent),
     * this makes no sense and should generate an error.
     *
     * @param documentTypeName
     * @return
     */
    @Override
    @Cacheable(cacheNames = OrgReviewRole.CACHE_NAME, key = "'{getRolesToConsider}'+#p0")
    public List<String> getRolesToConsider(String documentTypeName) throws ValidationException {
        List<String> rolesToConsider = new ArrayList<>(2);
        if (StringUtils.isBlank(documentTypeName) || KFSConstants.ROOT_DOCUMENT_TYPE.equals(documentTypeName)) {
            rolesToConsider.add(KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME);
            rolesToConsider.add(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
        } else {
            String closestParentDocumentTypeName = getClosestOrgReviewRoleParentDocumentTypeName(documentTypeName);
            if (documentTypeName.equals(KFSConstants.FINANCIAL_SYSTEM_TRANSACTIONAL_DOCUMENT)
                    || KFSConstants.FINANCIAL_SYSTEM_TRANSACTIONAL_DOCUMENT.equals(closestParentDocumentTypeName)) {
                rolesToConsider.add(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
            } else {
                boolean hasOrganizationHierarchy = hasOrganizationHierarchy(documentTypeName);
                boolean hasAccountingOrganizationHierarchy = hasAccountingOrganizationHierarchy(documentTypeName);
                if (hasOrganizationHierarchy || documentTypeName.equals(
                        KFSConstants.FINANCIAL_SYSTEM_COMPLEX_MAINTENANCE_DOCUMENT)
                        || KFSConstants.FINANCIAL_SYSTEM_COMPLEX_MAINTENANCE_DOCUMENT.equals(
                        closestParentDocumentTypeName)) {
                    rolesToConsider.add(KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME);
                }
                if (hasAccountingOrganizationHierarchy) {
                    rolesToConsider.add(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
                }
            }
        }
        return rolesToConsider;
    }

    @Override
    public void saveOrgReviewRoleToKim(OrgReviewRole orr) {
        if (orr.isDelegate() || orr.isCreateDelegation()) {
            saveDelegateMemberToKim(orr);
        } else {
            saveRoleMemberToKim(orr);
        }
    }

    protected void updateDelegateMemberFromDocDelegateMember(DelegateMember member, DelegateMember dm) {
        member.setMemberId(dm.getMemberId());
        member.setType(dm.getType());
        member.setRoleMemberId(dm.getRoleMemberId());
        member.setAttributes(dm.getAttributes());
        Timestamp activeFromDateValue = dm.getActiveFromDate() == null ? null :
                new Timestamp(dm.getActiveFromDate().getMillis());
        member.setActiveFromDateValue(activeFromDateValue);
        Timestamp activeToDateValue = dm.getActiveToDate() == null ? null :
                new Timestamp(dm.getActiveToDate().getMillis());
        member.setActiveToDateValue(activeToDateValue);
    }

    protected void saveDelegateMemberToKim(OrgReviewRole orr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving delegate member from OrgReviewRole: " + orr);
        }
        RoleService roleService = KimApiServiceLocator.getRoleService();
        // Save delegation(s)
        List<DelegateMember> delegationMembers = getDelegationMembersToSave(orr);

        for (DelegateMember dm : delegationMembers) {
            // retrieve the delegate type so it can be updated
            DelegationType delegationType = DelegationType.fromCode(dm.getDelegationType());
            DelegateType delegateType = roleService.getDelegateTypeByRoleIdAndDelegateTypeCode(orr.getRoleId(),
                    delegationType);
            if (shouldCreateNewDelegateType(delegateType)) {
                DelegateType newDelegateType = new DelegateType();
                newDelegateType.setRoleId(orr.getRoleId());
                newDelegateType.setDelegationType(delegationType);
                newDelegateType.setDelegationMembers(new ArrayList<>(1));
                // ensure this is set (for new delegation types)
                newDelegateType.setKimTypeId(orr.getKimTypeId());
                delegateType = roleService.createDelegateType(newDelegateType);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No DelegateType in KIM.  Created new one: " + delegateType);
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Pulled DelegateType from KIM: " + delegateType);
                }
            }

            boolean foundExistingMember = false;
            DelegateMember addedMember = null;

            // check for an existing delegation member given its unique ID if found, update that record
            if (StringUtils.isNotBlank(dm.getDelegationMemberId())) {
                DelegateMember member = roleService.getDelegationMemberById(dm.getDelegationMemberId());
                if (member != null) {
                    foundExistingMember = true;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found existing delegate member - updating existing record. " + member);
                    }
                    // KFSMI-9628 : fixing issue with the delegate switch from primary to secondary
                    // IN this case, we need to delete the member from the "other" delegate type

                    // need to determine what the "existing" type was
                    DelegateType originalDelegateType = roleService.getDelegateTypeByDelegationId(
                            member.getDelegationId());
                    // if they are the same, we can just update the existing record
                    if (originalDelegateType.getDelegationType().equals(dm.getDelegationType())) {
                        updateDelegateMemberFromDocDelegateMember(member, dm);
                        addedMember = roleService.updateDelegateMember(member);
                    } else {
                        // Otherwise, we need to remove the old one and add a new one
                        // Remove old
                        roleService.removeDelegateMembers(Collections.singletonList(member));
                        // add new
                        DelegateMember newMember = new DelegateMember();
                        newMember.setDelegationId(delegateType.getDelegationId());
                        updateDelegateMemberFromDocDelegateMember(newMember, dm);
                        addedMember = roleService.createDelegateMember(newMember);
                    }
                }
            }
            // if we did not find one, then we need to create a new member
            if (!foundExistingMember) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No existing delegate member found, adding as a new delegate: " + dm);
                }
                DelegateMember newMember = new DelegateMember();
                newMember.setDelegationId(delegateType.getDelegationId());
                updateDelegateMemberFromDocDelegateMember(newMember, dm);
                addedMember = roleService.createDelegateMember(newMember);
            }

            if (addedMember != null) {
                orr.setDelegationMemberId(addedMember.getDelegationMemberId());
            }
        }
    }

    private boolean shouldCreateNewDelegateType(DelegateType delegateType) {
        // If no delegate type exists, the Role Service returns a shell one, so we need to create a new one if
        // it's null, or if its delegationId is null (indicating it doesn't exist and was created new by the service).
        return delegateType == null || delegateType.getDelegationId() == null;
    }

    protected void saveRoleMemberToKim(OrgReviewRole orr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving role member from OrgReviewRole: " + orr);
        }
        RoleService roleService = KimApiServiceLocator.getRoleService();
        // Save role member(s)
        for (RoleMember roleMember : getRoleMembers(orr)) {
            List<RoleResponsibilityAction> roleRspActionsToSave = getRoleResponsibilityActions(orr, roleMember);
            // KFSCNTRB-1391
            GenericQueryResults<RoleMember> roleMembers = null;
            if (orr.isEdit()) {
                roleMembers = roleService.findRoleMembers(QueryByCriteria.Builder.fromPredicates(PredicateUtils
                        .convertMapToPredicate(
                                Collections.singletonMap(KimConstants.PrimaryKeyConstants.ID, roleMember.getId()))));
            }
            if (orr.isEdit() && roleMembers != null && roleMembers.getResults() != null && !roleMembers.getResults()
                    .isEmpty()) {
                RoleMember existingRoleMember = roleMembers.getResults().get(0);
                roleMember = roleService.updateRoleMember(existingRoleMember);
            } else {
                roleMember = roleService.createRoleMember(roleMember);
            }
            for (RoleResponsibilityAction rra : roleRspActionsToSave) {
                // ensure linked to the right record
                rra.setRoleMemberId(roleMember.getId());
                if (StringUtils.isBlank(rra.getId())) {
                    roleService.createRoleResponsibilityAction(rra);
                } else {
                    roleService.updateRoleResponsibilityAction(rra);
                }
            }
            orr.setRoleMemberId(roleMember.getId());
            orr.setORMId(roleMember.getId());
        }
    }

    protected RoleLite getRoleInfo(String roleName) {
        if (roleName == null) {
            return null;
        }
        RoleLite role = ROLE_CACHE.get(roleName);
        if (role == null) {
            role = KimApiServiceLocator.getRoleService().getRoleByNamespaceCodeAndName(
                    KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAMESPACECODE, roleName);
            synchronized (ROLE_CACHE) {
                ROLE_CACHE.put(roleName, role);
            }
        }
        return role;
    }

    protected List<DelegateMember> getDelegationMembersToSave(OrgReviewRole orr) {
    	DelegateMember delegationMember = null;
        if (orr.isEdit() && StringUtils.isNotBlank(orr.getDelegationMemberId())) {
            delegationMember = KimApiServiceLocator.getRoleService()
                    .getDelegationMemberById(orr.getDelegationMemberId());
        }

        if (delegationMember == null) {
            delegationMember = new DelegateMember();
            if (StringUtils.isNotEmpty(orr.getRoleMemberRoleNamespaceCode()) && StringUtils.isNotEmpty(
                    orr.getRoleMemberRoleName())) {
                String roleId = KimApiServiceLocator.getRoleService().getRoleIdByNamespaceCodeAndName(
                        orr.getRoleMemberRoleNamespaceCode(), orr.getRoleMemberRoleName());
                delegationMember.setMemberId(roleId);
                delegationMember.setType(MemberType.ROLE);
            } else if (StringUtils.isNotEmpty(orr.getGroupMemberGroupNamespaceCode()) && StringUtils.isNotEmpty(
                    orr.getGroupMemberGroupName())) {
                Group groupInfo = KimApiServiceLocator.getGroupService().getGroupByNamespaceCodeAndName(
                        orr.getGroupMemberGroupNamespaceCode(), orr.getGroupMemberGroupName());
                delegationMember.setMemberId(groupInfo.getId());
                delegationMember.setType(MemberType.GROUP);
            } else if (StringUtils.isNotEmpty(orr.getPrincipalMemberPrincipalName())) {
                Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(
                        orr.getPrincipalMemberPrincipalName());
                delegationMember.setMemberId(principal.getPrincipalId());
                delegationMember.setType(MemberType.PRINCIPAL);
            }
        }
        delegationMember.setDelegationType(orr.getDelegationTypeCode());
        delegationMember.setAttributes(getAttributes(orr, orr.getKimTypeId()));
        if (orr.getActiveFromDate() != null) {
            delegationMember.setActiveFromDateValue(new Timestamp(orr.getActiveFromDate().getTime()));
        }
        if (orr.getActiveToDate() != null) {
            delegationMember.setActiveToDateValue(new Timestamp(orr.getActiveToDate().getTime()));
        }
        delegationMember.setRoleMemberId(orr.getRoleMemberId());
        return Collections.singletonList(delegationMember);
    }

    protected RoleMember getRoleMemberToSave(RoleLite role, OrgReviewRole orr) {
        RoleMember roleMember = null;
        if (orr.getPerson() != null) {
            roleMember = new RoleMember();
            roleMember.setRoleId(role.getId());
            roleMember.setType(MemberType.PRINCIPAL);
            roleMember.setMemberId(orr.getPerson().getPrincipalId());
        } else if (orr.getGroup() != null) {
            roleMember = new RoleMember();
            roleMember.setRoleId(role.getId());
            roleMember.setType(MemberType.GROUP);
            roleMember.setMemberId(orr.getGroup().getId());
        } else if (orr.getRole() != null) {
            roleMember = new RoleMember();
            roleMember.setRoleId(role.getId());
            roleMember.setType(MemberType.ROLE);
            roleMember.setMemberId(orr.getRole().getId());
        }
        if (roleMember != null) {
            if (orr.isEdit()) {
                roleMember.setId(orr.getRoleMemberId());
            }
            roleMember.setAttributes(getAttributes(orr, role.getKimTypeId()));
            if (orr.getActiveFromDate() != null) {
                roleMember.setActiveFromDateValue(new Timestamp(orr.getActiveFromDate().getTime()));
            }
            if (orr.getActiveToDate() != null) {
                roleMember.setActiveToDateValue(new Timestamp(orr.getActiveToDate().getTime()));
            }
        }
        return roleMember;
    }

    protected List<String> getRolesToSaveFor(List<String> roleNamesToConsider, String reviewRolesIndicator) {
        if (roleNamesToConsider != null) {
            List<String> roleToSaveFor = new ArrayList<>();
            if (COAConstants.ORG_REVIEW_ROLE_ORG_ACC_ONLY_CODE.equals(reviewRolesIndicator)) {
                roleToSaveFor.add(KFSConstants.SysKimApiConstants.ACCOUNTING_REVIEWER_ROLE_NAME);
            } else if (COAConstants.ORG_REVIEW_ROLE_ORG_ONLY_CODE.equals(reviewRolesIndicator)) {
                roleToSaveFor.add(KFSConstants.SysKimApiConstants.ORGANIZATION_REVIEWER_ROLE_NAME);
            } else {
                roleToSaveFor.addAll(roleNamesToConsider);
            }
            return roleToSaveFor;
        } else {
            return Collections.emptyList();
        }
    }

    protected List<RoleMember> getRoleMembers(OrgReviewRole orr) {
        List<RoleMember> objectsToSave = new ArrayList<>();
        List<String> roleNamesToSaveFor = getRolesToSaveFor(orr.getRoleNamesToConsider(),
                orr.getReviewRolesIndicator());
        for (String roleName : roleNamesToSaveFor) {
            RoleLite roleInfo = getRoleInfo(roleName);
            RoleMember roleMemberToSave = getRoleMemberToSave(roleInfo, orr);
            if (roleMemberToSave != null) {
                objectsToSave.add(roleMemberToSave);
            }
        }
        return objectsToSave;
    }

    protected Map<String, String> getAttributes(OrgReviewRole orr, String kimTypeId) {
        if (StringUtils.isBlank(kimTypeId)) {
            return Collections.emptyMap();
        }

        Map<String, String> attributes = new HashMap<>();
        KimAttribute kimAttribute = getAttributeDefinition(kimTypeId, KimAttributes.CHART_OF_ACCOUNTS_CODE);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getChartOfAccountsCode());
        }

        kimAttribute = getAttributeDefinition(kimTypeId, KimAttributes.ORGANIZATION_CODE);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getOrganizationCode());
        }

        kimAttribute = getAttributeDefinition(kimTypeId, KimConstants.AttributeConstants.DOCUMENT_TYPE_NAME);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getFinancialSystemDocumentTypeCode());
        }

        kimAttribute = getAttributeDefinition(kimTypeId, KimAttributes.ACCOUNTING_LINE_OVERRIDE_CODE);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getOverrideCode());
        }

        kimAttribute = getAttributeDefinition(kimTypeId, KimAttributes.FROM_AMOUNT);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getFromAmountStr());
        }

        kimAttribute = getAttributeDefinition(kimTypeId, KimAttributes.TO_AMOUNT);
        if (kimAttribute != null) {
            attributes.put(kimAttribute.getAttributeName(), orr.getToAmountStr());
        }

        return attributes;
    }

    protected List<RoleResponsibilityAction> getRoleResponsibilityActions(OrgReviewRole orr,
                                                                          RoleMember roleMember) {
        List<RoleResponsibilityAction> roleResponsibilityActions = new ArrayList<>(1);

        RoleResponsibilityAction rra = new RoleResponsibilityAction();
        // if this is an existing role member, pull matching role resp action record (and set ID in object) so it can be updated
        // otherwise, it will be left blank and a new one will be created
        if (StringUtils.isNotBlank(roleMember.getId())) {
            List<RoleResponsibilityAction> origRoleRspActions = KimApiServiceLocator.getRoleService()
                    .getRoleMemberResponsibilityActions(roleMember.getId());
            if (origRoleRspActions != null && !origRoleRspActions.isEmpty()) {
                rra.setId(origRoleRspActions.get(0).getId());
                rra.setVersionNumber(origRoleRspActions.get(0).getVersionNumber());
            }
        }

        rra.setRoleMemberId(roleMember.getId());
        rra.setRoleResponsibilityId("*");
        rra.setActionTypeCode(orr.getActionTypeCode());
        rra.setActionPolicyCode(orr.getActionPolicyCode());

        if (StringUtils.isNotBlank(orr.getPriorityNumber())) {
            try {
                rra.setPriorityNumber(Integer.valueOf(orr.getPriorityNumber()));
            } catch (Exception nfx) {
                rra.setPriorityNumber(null);
            }
        }
        rra.setForceAction(orr.isForceAction());
        roleResponsibilityActions.add(rra);
        return roleResponsibilityActions;
    }

    protected RoleMemberAttributeData getAttribute(String kimTypeId, String attributeName, String attributeValue) {
        if (StringUtils.isNotBlank(attributeValue)) {
            KimAttribute attribute = getAttributeDefinition(kimTypeId, attributeName);
            if (attribute != null) {
            	RoleMemberAttributeData attributeData = new RoleMemberAttributeData();
                attributeData.setKimTypeId(kimTypeId);
                attributeData.setAttributeValue(attributeValue);
                attributeData.setKimAttributeId(attribute.getId());
                attributeData.setKimAttribute(attribute);
                return attributeData;
            }
        }
        return null;
    }

    protected KimAttribute getAttributeDefinition(String kimTypeId, String attributeName) {
        // attempt to pull from cache
        Map<String, KimAttribute> typeAttributes = ATTRIBUTE_CACHE.get(kimTypeId);
        // if type has not been loaded, init
        if (typeAttributes == null) {
            KimType kimType = KimApiServiceLocator.getKimTypeInfoService().getKimType(kimTypeId);
            if (kimType != null) {
                List<KimTypeAttribute> attributes = kimType.getAttributeDefinitions();
                typeAttributes = new HashMap<>();
                if (attributes != null) {
                    // build the map and put it into the cache
                    for (KimTypeAttribute att : attributes) {
                        typeAttributes.put(att.getKimAttribute().getAttributeName(), att.getKimAttribute());
                    }
                }
                synchronized (ATTRIBUTE_CACHE) {
                    ATTRIBUTE_CACHE.put(kimTypeId, typeAttributes);
                }
            }
        }
        // now, see if the attribute is in there
        if (typeAttributes != null) {
            return typeAttributes.get(attributeName);
        }
        return null;
    }

    protected DocumentTypeService getDocumentTypeService() {
        if (documentTypeService == null) {
            documentTypeService = KEWServiceLocator.getDocumentTypeService();
        }
        return documentTypeService;
    }
}
