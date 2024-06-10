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
package org.kuali.kfs.kim.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.PredicateUtils;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.delegation.DelegationType;
import org.kuali.kfs.core.api.membership.MemberType;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.KimConstants.KimGroupMemberTypes;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.role.RoleContract;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.bo.ui.GroupDocumentMember;
import org.kuali.kfs.kim.bo.ui.GroupDocumentQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRolePermission;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibility;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibilityAction;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.bo.ui.PersonDocumentRole;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.document.IdentityManagementGroupDocument;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMemberAttributeData;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.group.GroupAttribute;
import org.kuali.kfs.kim.impl.group.GroupInternalService;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.permission.Permission;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalService;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleInternalService;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleMemberAttributeData;
import org.kuali.kfs.kim.impl.role.RolePermission;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kim.util.KimCommonUtilsInternal;
import org.kuali.kfs.kns.datadictionary.control.CheckboxControlDefinition;
import org.kuali.kfs.kns.datadictionary.exporter.AttributesMapBuilder;
import org.kuali.kfs.kns.kim.type.DataDictionaryTypeServiceHelper;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.exporter.ExportMap;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CU Customization: Backported the FINP-9525 fix into the 2023-03-05 financials version of this file.
 *                   This overlay should be removed when we upgrade to the 2023-05-17 financials patch.
 */
@SuppressWarnings("deprecation")
public class UiDocumentServiceImpl implements UiDocumentService {

    private static final Logger LOG = LogManager.getLogger();
    private static final String SHOW_BLANK_QUALIFIERS = "kim.show.blank.qualifiers";
    // Access set to protected per Cornell's request
    protected BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private DocumentHelperService documentHelperService;
    private GroupInternalService groupInternalService;
    private GroupService groupService;
    private KimTypeInfoService kimTypeInfoService;
    private ParameterService parameterService;
    private PermissionService permissionService;
    private PersonService personService;
    private ResponsibilityInternalService responsibilityInternalService;
    private RoleInternalService roleInternalService;
    private RoleService roleService;
    // ==== CU Customization: Backport FINP-9525 fix ====
    private UiDocumentWorkflowHelper workflowHelper;
    // ==== End CU Customization ====

    protected final GroupMemberNameComparator groupMemberNameComparator = new GroupMemberNameComparator();

    @CacheEvict(
            value = {
                Person.CACHE_NAME,
                Role.CACHE_NAME,
                GroupMember.CACHE_NAME,
                RoleMember.CACHE_NAME
            },
            allEntries = true
    )
    @Override
    public void savePerson(final IdentityManagementPersonDocument identityManagementPersonDocument) {
        Person person = personService.getPerson(identityManagementPersonDocument.getPrincipalId());
        if (person == null) {
            person = new Person();
            person.setEntityId(identityManagementPersonDocument.getEntityId());
            person.setEntityTypeCode(KimConstants.EntityTypes.PERSON);
            person.setPrincipalId(identityManagementPersonDocument.getPrincipalId());
            person.setActive(true);
        }
        person.setPrincipalName(identityManagementPersonDocument.getPrincipalName());
        setupAffiliation(identityManagementPersonDocument, person);
        setupName(identityManagementPersonDocument, person);
        person.setPhoneNumber(identityManagementPersonDocument.getPhoneNumber());
        person.setEmailAddress(identityManagementPersonDocument.getEmailAddress());
        setupAddress(identityManagementPersonDocument, person);

        final boolean inactivatingPrincipal = person.isActive() && !identityManagementPersonDocument.isActive();
        person.setActive(identityManagementPersonDocument.isActive());

        businessObjectService.save(person);

        // If person is being inactivated, do not bother populating roles, groups etc. for this member since
        // none of this is reinstated on activation.
        if (inactivatingPrincipal) {
            //when a person is inactivated, inactivate their group, role, and delegation memberships
            roleInternalService.principalInactivated(identityManagementPersonDocument.getPrincipalId(),
                    identityManagementPersonDocument.getDocumentNumber());
        } else {
            final List<GroupMember> groupPrincipals = populateGroupMembers(identityManagementPersonDocument);
            final List<RoleMember> rolePrincipals = populateRoleMembers(identityManagementPersonDocument);
            final List<DelegateType> personDelegations = populateDelegations(identityManagementPersonDocument);
            final List<RoleResponsibilityAction> roleRspActions =
                    populateRoleRspActions(identityManagementPersonDocument);
            final List<PersistableBusinessObject> bos = new ArrayList<>();
            bos.addAll(groupPrincipals);
            bos.addAll(rolePrincipals);
            bos.addAll(roleRspActions);
            bos.addAll(personDelegations);
            businessObjectService.save(bos);
            final List<RoleMemberAttributeData> blankRoleMemberAttrs = getBlankRoleMemberAttrs(rolePrincipals);
            if (!blankRoleMemberAttrs.isEmpty()) {
                for (final RoleMemberAttributeData blankRoleMemberAttr : blankRoleMemberAttrs) {
                    businessObjectService.delete(blankRoleMemberAttr);
                }
            }
        }
    }

    private String getInitiatorPrincipalId(final Document document) {
        try {
            return document.getDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId();
        } catch (final Exception ex) {
            return null;
        }
    }

    @Override
    public Map<String, Object> getAttributeEntries(final List<KimAttributeField> definitions) {
        final Map<String, Object> attributeEntries = new HashMap<>();
        if (definitions != null) {
            for (final AttributeDefinition definition : DataDictionaryTypeServiceHelper
                    .toKimAttributeDefinitions(definitions)) {
                final AttributesMapBuilder builder = new AttributesMapBuilder();
                final ExportMap map = builder.buildAttributeMap(definition, "");
                attributeEntries.put(definition.getName(), map.getExportData());
            }
        }
        return attributeEntries;
    }

    @Override
    public void loadPersonDoc(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final String principalId
    ) {
        final Person person = personService.getPerson(principalId);

        if (ObjectUtils.isNull(person)) {
            throw new RuntimeException("Person does not exist for principal id:" + principalId);
        }

        identityManagementPersonDocument.setPrincipalId(person.getPrincipalId());
        identityManagementPersonDocument.setPrincipalName(person.getPrincipalName());
        identityManagementPersonDocument.setActive(person.isActive());
        identityManagementPersonDocument.setEntityId(person.getEntityId());
        identityManagementPersonDocument.setAffiliationTypeCode(person.getAffiliationTypeCode());
        identityManagementPersonDocument.setCampusCode(person.getCampusCode());
        identityManagementPersonDocument.setEmployeeId(person.getEmployeeId());
        identityManagementPersonDocument.setEmployeeStatusCode(person.getEmployeeStatusCode());
        identityManagementPersonDocument.setEmployeeTypeCode(person.getEmployeeTypeCode());
        identityManagementPersonDocument.setPrimaryDepartmentCode(person.getPrimaryDepartmentCode());
        identityManagementPersonDocument.setBaseSalaryAmount(person.getBaseSalaryAmount());
        identityManagementPersonDocument.setFirstName(person.getFirstName());
        identityManagementPersonDocument.setMiddleName(person.getMiddleName());
        identityManagementPersonDocument.setLastName(person.getLastName());
        identityManagementPersonDocument.setAddressTypeCode(person.getAddressTypeCode());
        identityManagementPersonDocument.setAddressLine1(person.getAddressLine1());
        identityManagementPersonDocument.setAddressLine2(person.getAddressLine2());
        identityManagementPersonDocument.setAddressLine3(person.getAddressLine3());
        identityManagementPersonDocument.setAddressCity(person.getAddressCity());
        identityManagementPersonDocument.setAddressStateProvinceCode(person.getAddressStateProvinceCode());
        identityManagementPersonDocument.setAddressPostalCode(person.getAddressPostalCode());
        identityManagementPersonDocument.setAddressCountryCode(person.getAddressCountryCode());
        identityManagementPersonDocument.setPhoneNumber(person.getPhoneNumber());
        identityManagementPersonDocument.setEmailAddress(person.getEmailAddress());

        final List<Group> groups = groupService.getGroups(
                        groupService.getDirectGroupIdsByPrincipalId(identityManagementPersonDocument.getPrincipalId()));
        loadGroupToPersonDoc(identityManagementPersonDocument, groups);
        loadRoleToPersonDoc(identityManagementPersonDocument);
        loadDelegationsToPersonDoc(identityManagementPersonDocument);
    }

    public List<DelegateType> getPersonDelegations(final String principalId) {
        if (principalId == null) {
            return new ArrayList<>();
        }
        Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.MEMBER_ID, principalId);
        criteria.put(KIMPropertyConstants.DelegationMember.MEMBER_TYPE_CODE, MemberType.PRINCIPAL.getCode());
        final List<DelegateMember> delegationMembers = (List<DelegateMember>) businessObjectService
                .findMatching(DelegateMember.class, criteria);
        final List<DelegateType> delegations = new ArrayList<>();
        final List<String> delegationIds = new ArrayList<>();
        if (ObjectUtils.isNotNull(delegationMembers)) {
            for (final DelegateMember delegationMember : delegationMembers) {
                if (delegationMember.getDelegationId() != null
                        && !delegationIds.contains(delegationMember.getDelegationId())) {
                    delegationIds.add(delegationMember.getDelegationId());
                    criteria = new HashMap<>(1);
                    criteria.put(KimConstants.PrimaryKeyConstants.DELEGATION_ID, delegationMember.getDelegationId());
                    delegations.add(businessObjectService.findByPrimaryKey(DelegateType.class, criteria));
                }
            }
        }
        return delegations;
    }

    protected void loadDelegationsToPersonDoc(final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<RoleDocumentDelegation> delList = new ArrayList<>();
        RoleDocumentDelegation documentDelegation;
        final List<DelegateType> origDelegations =
                getPersonDelegations(identityManagementPersonDocument.getPrincipalId());
        if (ObjectUtils.isNotNull(origDelegations)) {
            for (final DelegateType del : origDelegations) {
                if (del.isActive()) {
                    documentDelegation = new RoleDocumentDelegation();
                    documentDelegation.setActive(del.isActive());
                    documentDelegation.setDelegationId(del.getDelegationId());
                    documentDelegation.setDelegationTypeCode(del.getDelegationTypeCode());
                    documentDelegation.setKimTypeId(del.getKimTypeId());
                    documentDelegation.setMembers(loadDelegationMembers(identityManagementPersonDocument,
                            del.getMembers(), (Role) getMember(MemberType.ROLE, del.getRoleId())));
                    documentDelegation.setRoleId(del.getRoleId());
                    documentDelegation.setEdit(true);
                    delList.add(documentDelegation);
                }
            }
        }
        identityManagementPersonDocument.setDelegations(delList);
        setDelegationMembersInDocument(identityManagementPersonDocument);
    }

    /**
     * This method loads related group data to pending person document when user initiates the 'edit' or 'inquiry'.
     *
     * @param identityManagementPersonDocument
     * @param groups
     */
    protected void loadGroupToPersonDoc(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final List<Group> groups) {
        final List<PersonDocumentGroup> docGroups = new ArrayList<>();
        if (ObjectUtils.isNotNull(groups)) {
            for (final Group group : groups) {
                if (groupService.isDirectMemberOfGroup(identityManagementPersonDocument.getPrincipalId(),
                        group.getId())) {
                    final PersonDocumentGroup docGroup = new PersonDocumentGroup();
                    docGroup.setGroupId(group.getId());
                    docGroup.setGroupName(group.getName());
                    docGroup.setNamespaceCode(group.getNamespaceCode());
                    docGroup.setPrincipalId(identityManagementPersonDocument.getPrincipalId());
                    final Collection<GroupMember> groupMemberships = groupService.getMembersOfGroup(group.getId());

                    if (ObjectUtils.isNotNull(groupMemberships)) {
                        for (final GroupMember groupMember : groupMemberships) {
                            if (StringUtils.equals(groupMember.getMemberId(),
                                    identityManagementPersonDocument.getPrincipalId())
                                    && KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.equals(groupMember.getType())) {
                                docGroup.setGroupMemberId(groupMember.getId());
                                if (groupMember.getActiveFromDate() != null) {
                                    docGroup.setActiveFromDate(groupMember.getActiveFromDate() == null ? null :
                                            new Timestamp(groupMember.getActiveFromDate().getMillis()));
                                }
                                if (groupMember.getActiveToDate() != null) {
                                    docGroup.setActiveToDate(groupMember.getActiveToDate() == null ? null :
                                            new Timestamp(groupMember.getActiveToDate().getMillis()));
                                }
                            }
                        }
                    }
                    docGroup.setKimTypeId(group.getKimTypeId());
                    docGroup.setEdit(true);
                    docGroups.add(docGroup);
                }
            }
        }
        docGroups.sort(Comparator.comparing(PersonDocumentGroup::getGroupId));
        identityManagementPersonDocument.setGroups(docGroups);
    }

    /**
     * Used to populate the {@link PersonDocumentRole} objects for a {@link IdentityManagementPersonDocument}
     *
     * @param identityManagementPersonDocument {@link IdentityManagementPersonDocument}
     */
    protected void loadRoleToPersonDoc(final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<PersonDocumentRole> docRoles = new ArrayList<>();
        // a list for Id's of the roles added to docRoles
        final List<String> roleIds = new ArrayList<>();

        // get the membership objects for the PrincipalId
        final List<RoleMember> roleMembers =
                getRoleMembersForPrincipal(identityManagementPersonDocument.getPrincipalId());

        // if the PrincipalId is a member of any roles, add those roles to docRoles
        if (ObjectUtils.isNotNull(roleMembers)) {
            // for each membership get the role and add it, if not already added
            for (final RoleMember member : roleMembers) {
                if (member.isActive() && !roleIds.contains(member.getRoleId())) {
                    loadDocRoles(docRoles, roleIds, member, roleMembers);
                }
            }
        }

        // complete the attributes for each role being being returned
        for (final PersonDocumentRole role : docRoles) {
            role.setDefinitions(getAttributeDefinitionsForRole(role));

            final KimDocumentRoleMember newRolePrncpl = new KimDocumentRoleMember();
            newRolePrncpl.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
            newRolePrncpl.setMemberId(identityManagementPersonDocument.getPrincipalId());
            role.setNewRolePrncpl(newRolePrncpl);

            if (role.getDefinitions() != null) {
                for (final KimAttributeField key : role.getDefinitions()) {
                    final KimDocumentRoleQualifier qualifier = new KimDocumentRoleQualifier();
                    setAttrDefnIdForQualifier(qualifier, key);
                    role.getNewRolePrncpl().getQualifiers().add(qualifier);
                }
            }

            // load the role's ResponsibilityActions
            loadRoleRstAction(role);

            role.setAttributeEntry(getAttributeEntries(role.getDefinitions()));
        }

        // add the PersonDocumentRoles to the IdentityManagementPersonDocument
        identityManagementPersonDocument.setRoles(docRoles);
    }

    /**
     * Selects a {@link RoleLite} for passed {@link RoleMember} and adds to List of {@link PersonDocumentRole}
     * objects
     *
     * @param docRoles    a list of {@link PersonDocumentRole} roles
     * @param roleIds     a list of the Ids of the Roles already added
     * @param member      a {@link RoleMember} of a {@link RoleLite}
     * @param roleMembers a list of {@link RoleMember} membership objects for the PrincipalId
     */
    private void loadDocRoles(
            final List<PersonDocumentRole> docRoles, final List<String> roleIds, final RoleMember member,
            final List<RoleMember> roleMembers) {
        // get the RoleLite object by it's Id from a role membership object
        final RoleLite role = businessObjectService.findBySinglePrimaryKey(RoleLite.class, member.getRoleId());

        // create list of RoleMembers for the same role
        final List<RoleMember> matchingMembers = new ArrayList<>();
        for (final RoleMember tempMember : roleMembers) {
            if (tempMember.getRoleId().equals(member.getRoleId())) {
                matchingMembers.add(tempMember);
            }
        }

        // if not already found add role to docRoles
        if (ObjectUtils.isNotNull(role) && !roleIds.contains(role.getId())) {
            final PersonDocumentRole docRole = new PersonDocumentRole();
            docRole.setKimTypeId(role.getKimTypeId());
            docRole.setActive(role.isActive());
            docRole.setNamespaceCode(role.getNamespaceCode());
            docRole.setEdit(true);
            docRole.setRoleId(role.getId());
            docRole.setRoleName(role.getName());
            docRole.setRolePrncpls(populateDocRolePrncpl(role.getNamespaceCode(), matchingMembers,
                    member.getMemberId(), getAttributeDefinitionsForRole(docRole)));
            docRoles.add(docRole);
            roleIds.add(role.getId());
        }
    }

    protected List<KimAttributeField> getAttributeDefinitionsForRole(final PersonDocumentRole role) {
        final KimType kimRoleType = role.getKimRoleType();
        final KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(kimRoleType);
        if (kimTypeService != null) {
            return kimTypeService.getAttributeDefinitions(role.getKimTypeId());
        } else {
            LOG.warn("Not able to retrieve KimTypeService for KIM Role Type: {}", kimRoleType);
        }
        return Collections.emptyList();
    }

    protected void loadRoleRstAction(final PersonDocumentRole role) {
        if (role != null && CollectionUtils.isNotEmpty(role.getRolePrncpls())) {
            for (final KimDocumentRoleMember roleMbr : role.getRolePrncpls()) {
                final List<RoleResponsibilityAction> actions = getRoleRspActions(roleMbr.getRoleMemberId());
                if (ObjectUtils.isNotNull(actions)) {
                    for (final RoleResponsibilityAction entRoleRspAction : actions) {
                        final KimDocumentRoleResponsibilityAction roleRspAction = new KimDocumentRoleResponsibilityAction();
                        roleRspAction.setRoleResponsibilityActionId(entRoleRspAction.getId());
                        roleRspAction.setRoleResponsibilityId(entRoleRspAction.getRoleResponsibilityId());
                        roleRspAction.setActionTypeCode(entRoleRspAction.getActionTypeCode());
                        roleRspAction.setActionPolicyCode(entRoleRspAction.getActionPolicyCode());
                        roleRspAction.setPriorityNumber(entRoleRspAction.getPriorityNumber());
                        roleRspAction.setRoleResponsibilityActionId(entRoleRspAction.getId());
                        roleRspAction.refreshReferenceObject("roleResponsibility");
                        roleMbr.getRoleRspActions().add(roleRspAction);
                    }
                }
            }
        }
    }

    protected void setAttrDefnIdForQualifier(final KimDocumentRoleQualifier qualifier, final KimAttributeField definition) {
        qualifier.setKimAttrDefnId(getAttributeDefnId(definition));
        qualifier.refreshReferenceObject("kimAttribute");
    }

    protected String getAttributeDefnId(final KimAttributeField definition) {
        return definition.getId();
    }

    protected List<Role> getRolesForPrincipal(final String principalId) {
        if (principalId == null) {
            return new ArrayList<>();
        }
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put("members.memberId", principalId);
        criteria.put("members.typeCode", MemberType.PRINCIPAL.getCode());
        return (List<Role>) businessObjectService.findMatching(Role.class, criteria);
    }

    protected List<RoleMember> getRoleMembersForPrincipal(final String principalId) {
        if (principalId == null) {
            return new ArrayList<>();
        }
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put("memberId", principalId);
        criteria.put("typeCode", MemberType.PRINCIPAL.getCode());
        return (List<RoleMember>) businessObjectService.findMatching(RoleMember.class, criteria);
    }

    @Override
    public RoleMember getRoleMember(final String id) {
        if (id == null) {
            return null;
        }
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put("id", id);
        return businessObjectService.findByPrimaryKey(RoleMember.class, criteria);
    }

    protected List<RoleResponsibilityAction> getRoleRspActions(final String roleMemberId) {
        final Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KIMPropertyConstants.RoleMember.ROLE_MEMBER_ID, roleMemberId);
        return (List<RoleResponsibilityAction>) businessObjectService.findMatching(
                RoleResponsibilityAction.class, criteria);
    }

    protected List<KimDocumentRoleMember> populateDocRolePrncpl(
            final String namespaceCode, final List<RoleMember> roleMembers,
            final String principalId, final List<KimAttributeField> definitions) {
        final List<KimDocumentRoleMember> docRoleMembers = new ArrayList<>();
        if (ObjectUtils.isNotNull(roleMembers)) {
            for (final RoleMember rolePrincipal : roleMembers) {
                if (rolePrincipal.isActive(dateTimeService.getCurrentTimestamp())
                        && MemberType.PRINCIPAL.equals(rolePrincipal.getType())
                        && StringUtils.equals(rolePrincipal.getMemberId(), principalId)) {
                    final KimDocumentRoleMember docRolePrncpl = new KimDocumentRoleMember();
                    docRolePrncpl.setMemberId(rolePrincipal.getMemberId());
                    docRolePrncpl.setRoleMemberId(rolePrincipal.getId());
                    docRolePrncpl.setActive(rolePrincipal.isActive(dateTimeService.getCurrentTimestamp()));
                    docRolePrncpl.setRoleId(rolePrincipal.getRoleId());
                    docRolePrncpl.setActiveFromDate(rolePrincipal.getActiveFromDateValue());
                    docRolePrncpl.setActiveToDate(rolePrincipal.getActiveToDateValue());
                    docRolePrncpl.setQualifiers(populateDocRoleQualifier(namespaceCode,
                            rolePrincipal.getAttributeDetails(), definitions));
                    docRolePrncpl.setEdit(true);
                    docRoleMembers.add(docRolePrncpl);
                }
            }
        }
        return docRoleMembers;
    }

    // UI layout for rolequalifier is a little different from kimroleattribute set up. each principal may have member
    // with same role multiple times with different qualifier, but the role only displayed once, and the qualifier d
    // isplayed multiple times.
    protected List<KimDocumentRoleQualifier> populateDocRoleQualifier(
            final String namespaceCode,
                                                                      final List<RoleMemberAttributeData> qualifiers, final List<KimAttributeField> definitions) {
        final List<KimDocumentRoleQualifier> docRoleQualifiers = new ArrayList<>();
        if (definitions != null) {
            for (final KimAttributeField definition : definitions) {
                final String attrDefId = definition.getId();
                boolean qualifierFound = false;
                if (ObjectUtils.isNotNull(qualifiers)) {
                    for (final RoleMemberAttributeData qualifier : qualifiers) {
                        if (attrDefId != null && StringUtils.equals(attrDefId, qualifier.getKimAttributeId())) {
                            final KimDocumentRoleQualifier docRoleQualifier = new KimDocumentRoleQualifier();
                            docRoleQualifier.setAttrDataId(qualifier.getId());
                            docRoleQualifier.setAttrVal(qualifier.getAttributeValue());
                            docRoleQualifier.setKimAttrDefnId(qualifier.getKimAttributeId());
                            docRoleQualifier.setKimAttribute(qualifier.getKimAttribute());
                            docRoleQualifier.setKimTypId(qualifier.getKimTypeId());
                            docRoleQualifier.setRoleMemberId(qualifier.getAssignedToId());
                            docRoleQualifier.setEdit(true);
                            formatAttrValIfNecessary(docRoleQualifier);
                            docRoleQualifiers.add(docRoleQualifier);
                            qualifierFound = true;
                            break;
                        }
                    }
                }
                if (!qualifierFound) {
                    final KimDocumentRoleQualifier docRoleQualifier = new KimDocumentRoleQualifier();
                    docRoleQualifier.setAttrVal("");
                    docRoleQualifier.setKimAttrDefnId(attrDefId);
                    docRoleQualifier.refreshReferenceObject("kimAttribute");
                    docRoleQualifiers.add(docRoleQualifier);
                }
            }
            // If all of the qualifiers are empty, return an empty list
            // This is to prevent dynamic qualifiers from appearing in the person maintenance roles tab.
            // see KULRICE-3989 for more detail and KULRICE-5071 for detail on switching from config value to
            // application-scoped parameter
            if (!isBlankRoleQualifierVisible(namespaceCode)) {
                int qualCount = 0;
                for (final KimDocumentRoleQualifier qual : docRoleQualifiers) {
                    if (StringUtils.isEmpty(qual.getAttrVal())) {
                        qualCount++;
                    }
                }
                if (qualCount == docRoleQualifiers.size()) {
                    return new ArrayList<>();
                }
            }
        }
        return docRoleQualifiers;
    }

    @Override
    public boolean canModifyPerson(final String currentUserPrincipalId, final String toModifyPrincipalId) {
        return StringUtils.equals(currentUserPrincipalId, toModifyPrincipalId) || permissionService.isAuthorized(
                currentUserPrincipalId,
                KimConstants.NAMESPACE_CODE,
                KimConstants.PermissionNames.MODIFY_PERSON,
                Collections.singletonMap(KimConstants.AttributeConstants.PRINCIPAL_ID, currentUserPrincipalId)
        );
    }

    protected boolean canAssignToRole(final IdentityManagementRoleDocument document, final String initiatorPrincipalId) {
        boolean rulePassed = true;
        final Map<String, String> additionalPermissionDetails = new HashMap<>();
        additionalPermissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, document.getRoleNamespace());
        additionalPermissionDetails.put(KimConstants.AttributeConstants.ROLE_NAME, document.getRoleName());
        if (!documentHelperService.getDocumentAuthorizer(document).isAuthorizedByTemplate(
                document, KimConstants.NAMESPACE_CODE, KimConstants.PermissionTemplateNames.ASSIGN_ROLE,
                initiatorPrincipalId, additionalPermissionDetails, null)) {
            rulePassed = false;
        }
        return rulePassed;
    }

    private void setupName(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        person.setFirstName(identityManagementPersonDocument.getFirstName());
        person.setLastName(identityManagementPersonDocument.getLastName());
        person.setMiddleName(identityManagementPersonDocument.getMiddleName());
    }

    private void setupAffiliation(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        person.setAffiliationTypeCode(identityManagementPersonDocument.getAffiliationTypeCode());
        person.setCampusCode(identityManagementPersonDocument.getCampusCode());
        person.setEmployeeId(identityManagementPersonDocument.getEmployeeId());
        person.setEmployeeStatusCode(identityManagementPersonDocument.getEmployeeStatusCode());
        person.setEmployeeTypeCode(identityManagementPersonDocument.getEmployeeTypeCode());
        person.setPrimaryDepartmentCode(identityManagementPersonDocument.getPrimaryDepartmentCode());
        person.setBaseSalaryAmount(identityManagementPersonDocument.getBaseSalaryAmount());
    }

    /*
     * Added to address KULRICE-5071 : "Move the 'show blank qualifier' kim toggle from a Config param to a System
     *  param"
     *
     * This method first checks for a namespace specific parameter with a detailTypeCode of "All" and parameterName
     * of "KIM_SHOW_BLANK_QUALIFIERS". If no parameter is found, it checks for the config property
     * "kim.show.blank.qualifiers", and defaults to true if no config property exists.
     */
    private boolean isBlankRoleQualifierVisible(final String namespaceCode) {
        boolean showBlankQualifiers = true;

        final Parameter param = parameterService.getParameter(namespaceCode, KRADConstants.DetailTypes.ALL_DETAIL_TYPE,
                KimConstants.ParameterKey.SHOW_BLANK_QUALIFIERS);
        if (param != null) {
            showBlankQualifiers = "Y".equals(param.getValue());
        } else {
            final String configProperty = ConfigContext.getCurrentContextConfig().getProperty(SHOW_BLANK_QUALIFIERS);
            if (configProperty != null) {
                showBlankQualifiers = Boolean.parseBoolean(configProperty);
            }
        }

        return showBlankQualifiers;
    }

    protected void setupAddress(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        person.setAddressTypeCode(identityManagementPersonDocument.getAddressTypeCode());
        person.setAddressLine1(identityManagementPersonDocument.getAddressLine1());
        person.setAddressLine2(identityManagementPersonDocument.getAddressLine2());
        person.setAddressLine3(identityManagementPersonDocument.getAddressLine3());
        person.setAddressCity(identityManagementPersonDocument.getAddressCity());
        person.setAddressStateProvinceCode(identityManagementPersonDocument.getAddressStateProvinceCode());
        person.setAddressPostalCode(identityManagementPersonDocument.getAddressPostalCode());
        person.setAddressCountryCode(identityManagementPersonDocument.getAddressCountryCode());
    }

    protected List<GroupMember> populateGroupMembers(
            final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<GroupMember> groupPrincipals = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementPersonDocument.getGroups())) {
            for (final PersonDocumentGroup group : identityManagementPersonDocument.getGroups()) {
                final GroupMember groupPrincipalImpl = new GroupMember();
                groupPrincipalImpl.setGroupId(group.getGroupId());
                groupPrincipalImpl.setMemberId(identityManagementPersonDocument.getPrincipalId());
                groupPrincipalImpl.setType(KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
                if (group.getActiveFromDate() != null) {
                    groupPrincipalImpl.setActiveFromDateValue(group.getActiveFromDate());
                }
                if (group.getActiveToDate() != null) {
                    groupPrincipalImpl.setActiveToDateValue(group.getActiveToDate());
                }
                groupPrincipalImpl.setId(group.getGroupMemberId());

                // get the ORM-layer optimisic locking value
                // TODO: this should be replaced with the retrieval and storage of that value
                // in the document tables and not re-retrieved here
                final Collection<GroupMember> currGroupMembers = groupService.getMembers(
                        Collections.singletonList(group.getGroupId()));
                if (ObjectUtils.isNotNull(currGroupMembers)) {
                    for (final GroupMember origGroupMember : currGroupMembers) {
                        if (origGroupMember.isActive(new DateTime(System.currentTimeMillis()))
                                && KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.equals(origGroupMember.getType())) {
                            if (origGroupMember.getId() != null
                                    && StringUtils.equals(origGroupMember.getId(), group.getGroupMemberId())) {
                                groupPrincipalImpl.setObjectId(origGroupMember.getObjectId());
                                groupPrincipalImpl.setVersionNumber(origGroupMember.getVersionNumber());
                            }
                        }
                    }
                }

                groupPrincipals.add(groupPrincipalImpl);
            }
        }
        return groupPrincipals;
    }

    protected List<RoleMember> populateRoleMembers(
            final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<Role> origRoles = getRolesForPrincipal(identityManagementPersonDocument.getPrincipalId());

        final List<RoleMember> roleMembers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementPersonDocument.getRoles())) {
            for (final PersonDocumentRole role : identityManagementPersonDocument.getRoles()) {
                List<RoleMember> origRoleMembers = new ArrayList<>();
                if (ObjectUtils.isNotNull(origRoles)) {
                    for (final Role origRole : origRoles) {
                        if (origRole.getId() != null && StringUtils.equals(origRole.getId(), role.getRoleId())) {
                            origRoleMembers = origRole.getMembers();
                            break;
                        }
                    }
                }
                if (role.getRolePrncpls().isEmpty()) {
                    if (!role.getDefinitions().isEmpty()) {
                        final RoleMember roleMemberImpl = new RoleMember();
                        roleMemberImpl.setRoleId(role.getRoleId());
                        roleMemberImpl.setMemberId(identityManagementPersonDocument.getPrincipalId());
                        roleMemberImpl.setType(MemberType.PRINCIPAL);
                        roleMembers.add(roleMemberImpl);
                    }
                } else {
                    for (final KimDocumentRoleMember roleMember : role.getRolePrncpls()) {
                        final RoleMember roleMemberImpl = new RoleMember();
                        roleMemberImpl.setRoleId(role.getRoleId());
                        // TODO : principalId is not ready here yet ?
                        roleMemberImpl.setMemberId(identityManagementPersonDocument.getPrincipalId());
                        roleMemberImpl.setType(MemberType.PRINCIPAL);
                        roleMemberImpl.setId(roleMember.getRoleMemberId());
                        if (roleMember.getActiveFromDate() != null) {
                            roleMemberImpl.setActiveFromDateValue(
                                    new java.sql.Timestamp(roleMember.getActiveFromDate().getTime()));
                        }
                        if (roleMember.getActiveToDate() != null) {
                            roleMemberImpl.setActiveToDateValue(
                                    new java.sql.Timestamp(roleMember.getActiveToDate().getTime()));
                        }
                        List<RoleMemberAttributeData> origAttributes = new ArrayList<>();
                        if (ObjectUtils.isNotNull(origRoleMembers)) {
                            for (final RoleMember origMember : origRoleMembers) {
                                if (origMember.getId() != null
                                        && StringUtils.equals(origMember.getId(), roleMember.getRoleMemberId())) {
                                    origAttributes = origMember.getAttributeDetails();
                                    roleMemberImpl.setVersionNumber(origMember.getVersionNumber());
                                }
                            }
                        }
                        final List<RoleMemberAttributeData> attributes = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(roleMember.getQualifiers())) {
                            for (final KimDocumentRoleQualifier qualifier : roleMember.getQualifiers()) {
                                final RoleMemberAttributeData attribute = new RoleMemberAttributeData();
                                attribute.setId(qualifier.getAttrDataId());
                                attribute.setAttributeValue(qualifier.getAttrVal());
                                attribute.setKimAttributeId(qualifier.getKimAttrDefnId());
                                attribute.setAssignedToId(qualifier.getRoleMemberId());
                                attribute.setKimTypeId(qualifier.getKimTypId());

                                updateAttrValIfNecessary(attribute);

                                if (ObjectUtils.isNotNull(origAttributes)) {
                                    for (final RoleMemberAttributeData origAttribute : origAttributes) {
                                        if (origAttribute.getId() != null
                                                && StringUtils
                                                .equals(origAttribute.getId(), qualifier.getAttrDataId())) {
                                            attribute.setVersionNumber(origAttribute.getVersionNumber());
                                        }
                                    }
                                }
                                if (attribute.getVersionNumber() != null
                                        || StringUtils.isNotBlank(qualifier.getAttrVal())) {
                                    attributes.add(attribute);
                                }
                            }
                        }
                        roleMemberImpl.setAttributeDetails(attributes);
                        roleMembers.add(roleMemberImpl);
                    }
                }
            }
        }
        return roleMembers;
    }

    protected List<DelegateType> populateDelegations(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
                                                     final List<DelegateType> originalDelegations) {
        final List<DelegateType> delegations = combineAndCreateMissingDelegations(
                identityManagementRoleDocument.getDelegations(), originalDelegations);
        adjustForDelegationTypeChange(delegations, identityManagementRoleDocument.getDelegations());

        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getDelegations())) {
            for (final RoleDocumentDelegation roleDocumentDelegation : identityManagementRoleDocument.getDelegations()) {
                for (final DelegateType delegation : delegations) {
                    if (roleDocumentDelegation.getDelegationId().equals(delegation.getDelegationId())) {
                        delegation.setDelegationMembers(populateDelegationMembers(delegation, roleDocumentDelegation));
                    }
                }
            }
        }

        return delegations;
    }

    protected List<DelegateType> populateDelegations(
            final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<DelegateType> originalDelegations = getPersonDelegations(
                identityManagementPersonDocument.getPrincipalId());
        final List<DelegateType> delegations = combineAndCreateMissingDelegations(
                identityManagementPersonDocument.getDelegations(), originalDelegations);
        adjustForDelegationTypeChange(delegations, identityManagementPersonDocument.getDelegations());

        if (CollectionUtils.isNotEmpty(identityManagementPersonDocument.getDelegations())) {
            for (final RoleDocumentDelegation roleDocumentDelegation : identityManagementPersonDocument.getDelegations()) {
                for (final DelegateType delegation : delegations) {
                    final boolean sameDelegation = roleDocumentDelegation.getDelegationId().equals(
                            delegation.getDelegationId());
                    final boolean similarDelegation = roleDocumentDelegation.getRoleId().equals(delegation.getRoleId())
                                                      && roleDocumentDelegation.getDelegationTypeCode().equals(
                            delegation.getDelegationTypeCode());
                    if (sameDelegation || similarDelegation) {
                        delegation.setDelegationMembers(populateDelegationMembers(delegation, roleDocumentDelegation));
                    }
                }
            }
        }

        return delegations;
    }

    protected List<DelegateType> combineAndCreateMissingDelegations(
            final List<RoleDocumentDelegation> documentDelegations, final List<DelegateType> originalDelegations) {
        final List<DelegateType> delegations = new ArrayList<>(originalDelegations);
        if (CollectionUtils.isNotEmpty(documentDelegations)) {
            outer:
            for (final RoleDocumentDelegation roleDocumentDelegation : documentDelegations) {
                for (final DelegateType delegation : delegations) {
                    if (delegation.getDelegationId().equals(roleDocumentDelegation.getDelegationId())) {
                        continue outer;
                    }
                }
                // first, let's make sure there's not already a delegation defined for the given role with the given
                // delegation type

                final Map<String, String> criteria = new HashMap<>();
                criteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, roleDocumentDelegation.getRoleId());
                criteria.put(KIMPropertyConstants.Delegation.DELEGATION_TYPE_CODE,
                        roleDocumentDelegation.getDelegationTypeCode());
                final List<DelegateType> matchingDelegations = (List<DelegateType>) businessObjectService
                        .findMatching(DelegateType.class, criteria);

                if (matchingDelegations.isEmpty()) {
                    final DelegateType newDelegation = new DelegateType();
                    newDelegation.setDelegationId(roleDocumentDelegation.getDelegationId());
                    newDelegation.setKimTypeId(roleDocumentDelegation.getKimTypeId());
                    newDelegation.setDelegationTypeCode(roleDocumentDelegation.getDelegationTypeCode());
                    newDelegation.setRoleId(roleDocumentDelegation.getRoleId());
                    delegations.add(newDelegation);
                } else {
                    delegations.add(matchingDelegations.get(0));
                }
            }
        }
        return delegations;
    }

    protected void adjustForDelegationTypeChange(
            final List<DelegateType> delegations,
            final List<RoleDocumentDelegation> documentDelegations) {
        final Map<String, Map<DelegationType, DelegateType>> roleDelegationTypeIndex =
                indexDelegationsByRoleAndType(delegations);
        final Map<String, DelegateType> memberDelegationIndex = indexDelegationsByMemberId(delegations);
        final Map<String, DelegateMember> delegationMemberIndex = indexDelegationMembersByMemberId(delegations);
        for (final RoleDocumentDelegation documentDelegation : documentDelegations) {
            final Map<DelegationType, DelegateType> delegationTypeMap = roleDelegationTypeIndex.get(
                    documentDelegation.getRoleId());
            if (delegationTypeMap != null) {
                for (final RoleDocumentDelegationMember documentMember : documentDelegation.getMembers()) {
                    final DelegateType originalMemberDelegation = memberDelegationIndex.get(
                            documentMember.getDelegationMemberId());
                    // if the following happens, we know that this document member changed it's delegation type
                    if (originalMemberDelegation != null && !originalMemberDelegation.getDelegationTypeCode()
                            .equals(documentDelegation.getDelegationTypeCode())) {
                        final DelegateMember delegateMember = delegationMemberIndex.get(
                                documentMember.getDelegationMemberId());
                        // remove the original member from the original delegation and move it to the target one
                        originalMemberDelegation.getMembers().remove(delegateMember);
                        delegationTypeMap.get(DelegationType.fromCode(documentDelegation.getDelegationTypeCode()))
                                .getMembers().add(delegateMember);
                    }
                }
            }
        }
    }

    private Map<String, Map<DelegationType, DelegateType>> indexDelegationsByRoleAndType(
            final List<DelegateType> delegations) {
        final Map<String, Map<DelegationType, DelegateType>> index = new HashMap<>();
        for (final DelegateType delegation : delegations) {
            final String roleId = delegation.getRoleId();
            if (!index.containsKey(roleId)) {
                index.put(roleId, new HashMap<>());
            }
            final Map<DelegationType, DelegateType> delegationTypeMap = index.get(roleId);
            delegationTypeMap.put(delegation.getDelegationType(), delegation);
        }
        return index;
    }

    private Map<String, DelegateType> indexDelegationsByMemberId(final List<DelegateType> delegations) {
        final Map<String, DelegateType> index = new HashMap<>();
        for (final DelegateType delegation : delegations) {
            for (final DelegateMember member : delegation.getMembers()) {
                index.put(member.getDelegationMemberId(), delegation);
            }
        }
        return index;
    }

    private Map<String, DelegateMember> indexDelegationMembersByMemberId(final List<DelegateType> delegations) {
        final Map<String, DelegateMember> index = new HashMap<>();
        for (final DelegateType delegation : delegations) {
            for (final DelegateMember member : delegation.getMembers()) {
                index.put(member.getDelegationMemberId(), member);
            }
        }
        return index;
    }

    protected List<DelegateMember> populateDelegationMembers(
            final DelegateType delegation,
                                                             final RoleDocumentDelegation documentDelegation) {
        final List<DelegateMember> delegationMembers = delegation.getMembers();

        if (CollectionUtils.isNotEmpty(documentDelegation.getMembers())) {
            for (final RoleDocumentDelegationMember documentDelegationMember : documentDelegation.getMembers()) {
                DelegateMember originalMember = null;
                DelegateMember newMember = new DelegateMember();
                for (final DelegateMember anOriginalMember : delegationMembers) {
                    if (StringUtils.equals(anOriginalMember.getDelegationMemberId(),
                            documentDelegationMember.getDelegationMemberId())) {
                        originalMember = anOriginalMember;
                        newMember = originalMember;
                        break;
                    }
                }
                if (originalMember == null) {
                    delegationMembers.add(newMember);
                }
                // set the delegation id to that of the parent delegation
                newMember.setDelegationId(delegation.getDelegationId());
                // set the rest of the values
                newMember.setDelegationMemberId(documentDelegationMember.getDelegationMemberId());
                newMember.setRoleMemberId(documentDelegationMember.getRoleMemberId());
                newMember.setMemberId(documentDelegationMember.getMemberId());
                newMember.setTypeCode(documentDelegationMember.getMemberTypeCode());
                newMember.setActiveFromDateValue(documentDelegationMember.getActiveFromDate());
                newMember.setActiveToDateValue(documentDelegationMember.getActiveToDate());
                newMember.setAttributeDetails(populateDelegateMemberAttributes(newMember, documentDelegationMember));
            }
        }
        return delegationMembers;
    }

    protected List<DelegateMemberAttributeData> populateDelegateMemberAttributes(
            final DelegateMember member,
                                                                                 final RoleDocumentDelegationMember documentMember) {
        final List<DelegateMemberAttributeData> originalAttributes = member.getAttributeDetails();
        final List<DelegateMemberAttributeData> newAttributes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(documentMember.getQualifiers())) {
            for (final RoleDocumentDelegationMemberQualifier qualifier : documentMember.getQualifiers()) {
                DelegateMemberAttributeData newAttribute = new DelegateMemberAttributeData();
                for (final DelegateMemberAttributeData originalAttribute : originalAttributes) {
                    // they will have the same id if they represent the same attribute
                    if (StringUtils.equals(originalAttribute.getId(), qualifier.getAttrDataId())) {
                        newAttribute = originalAttribute;
                        break;
                    }
                }
                // only save an attribute if it has an actual value
                if (StringUtils.isNotBlank(qualifier.getAttrVal())) {
                    newAttribute.setId(qualifier.getAttrDataId());
                    newAttribute.setAttributeValue(qualifier.getAttrVal());
                    newAttribute.setAssignedToId(qualifier.getDelegationMemberId());
                    newAttribute.setKimTypeId(qualifier.getKimTypId());
                    newAttribute.setKimAttributeId(qualifier.getKimAttrDefnId());
                    newAttributes.add(newAttribute);
                }
            }
        }
        return newAttributes;
    }

    protected List<RoleMemberAttributeData> getBlankRoleMemberAttrs(final List<RoleMember> rolePrncpls) {
        final List<RoleMemberAttributeData> blankRoleMemberAttrs = new ArrayList<>();
        if (ObjectUtils.isNotNull(rolePrncpls)) {
            for (final RoleMember roleMbr : rolePrncpls) {
                final List<RoleMemberAttributeData> roleMemberAttrs = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(roleMbr.getAttributeDetails())) {
                    for (final RoleMemberAttributeData attr : roleMbr.getAttributeDetails()) {
                        if (StringUtils.isBlank(attr.getAttributeValue())) {
                            roleMemberAttrs.add(attr);
                        }
                    }
                    if (!roleMemberAttrs.isEmpty()) {
                        roleMbr.getAttributeDetails().removeAll(roleMemberAttrs);
                        blankRoleMemberAttrs.addAll(roleMemberAttrs);
                    }
                }
            }
        }

        return blankRoleMemberAttrs;
    }

    protected List<RoleResponsibilityAction> populateRoleRspActions(
            final IdentityManagementPersonDocument identityManagementPersonDocument) {
        final List<RoleResponsibilityAction> roleRspActions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementPersonDocument.getRoles())) {
            for (final PersonDocumentRole role : identityManagementPersonDocument.getRoles()) {
                if (CollectionUtils.isNotEmpty(role.getRolePrncpls())) {
                    for (final KimDocumentRoleMember roleMbr : role.getRolePrncpls()) {
                        if (CollectionUtils.isNotEmpty(roleMbr.getRoleRspActions())) {
                            for (final KimDocumentRoleResponsibilityAction roleRspAction : roleMbr.getRoleRspActions()) {
                                final RoleResponsibilityAction entRoleRspAction = new RoleResponsibilityAction();
                                entRoleRspAction.setId(roleRspAction.getRoleResponsibilityActionId());
                                entRoleRspAction.setActionPolicyCode(roleRspAction.getActionPolicyCode());
                                entRoleRspAction.setActionTypeCode(roleRspAction.getActionTypeCode());
                                entRoleRspAction.setPriorityNumber(roleRspAction.getPriorityNumber());
                                entRoleRspAction.setRoleMemberId(roleRspAction.getRoleMemberId());
                                entRoleRspAction.setRoleResponsibilityId(roleRspAction.getRoleResponsibilityId());
                                final List<RoleResponsibilityAction> actions =
                                        getRoleRspActions(roleMbr.getRoleMemberId());
                                if (ObjectUtils.isNotNull(actions)) {
                                    for (final RoleResponsibilityAction orgRspAction : actions) {
                                        if (orgRspAction.getId() != null && StringUtils.equals(orgRspAction.getId(),
                                                roleRspAction.getRoleResponsibilityActionId())) {
                                            entRoleRspAction.setVersionNumber(orgRspAction.getVersionNumber());
                                        }
                                    }
                                }
                                roleRspActions.add(entRoleRspAction);
                            }
                        }
                    }
                }
            }
        }
        return roleRspActions;
    }

    /* Role document methods */

    @Override
    public void loadRoleDoc(final IdentityManagementRoleDocument identityManagementRoleDocument, final RoleLite roleLite) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, roleLite.getId());
        final Role role = businessObjectService.findByPrimaryKey(Role.class, criteria);

        final Map<String, String> subClassCriteria = new HashMap<>();
        subClassCriteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, role.getId());

        identityManagementRoleDocument.setRoleId(role.getId());
        identityManagementRoleDocument.setKimType(role.getKimRoleType());
        identityManagementRoleDocument.setRoleTypeName(role.getKimRoleType().getName());
        identityManagementRoleDocument.setRoleTypeId(role.getKimTypeId());
        identityManagementRoleDocument.setRoleName(role.getName());
        identityManagementRoleDocument.setRoleDescription(role.getDescription());
        identityManagementRoleDocument.setActive(role.isActive());
        identityManagementRoleDocument.setRoleNamespace(role.getNamespaceCode());
        identityManagementRoleDocument.setEditing(true);

        identityManagementRoleDocument.setPermissions(loadPermissions(
                (List<RolePermission>) businessObjectService.findMatching(RolePermission.class,
                        subClassCriteria)));
        identityManagementRoleDocument.setResponsibilities(loadResponsibilities(
                (List<RoleResponsibility>) businessObjectService.findMatching(RoleResponsibility.class,
                        subClassCriteria)));
        loadResponsibilityRoleRspActions(identityManagementRoleDocument);
        identityManagementRoleDocument
                .setMembers(loadRoleMembers(identityManagementRoleDocument, role.getMembers()));
        loadMemberRoleRspActions(identityManagementRoleDocument);
        identityManagementRoleDocument.setDelegations(loadRoleDocumentDelegations(identityManagementRoleDocument,
                getRoleDelegations(role.getId())));
        //Since delegation members are flattened out on the UI...
        setDelegationMembersInDocument(identityManagementRoleDocument);
        identityManagementRoleDocument.setKimType(role.getKimRoleType());
    }

    @Override
    public void loadRoleMembersBasedOnSearch(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final String memberSearchValue) {
        final List<KimDocumentRoleMember> roleMembersRestricted = new ArrayList<>();
        final List<KimDocumentRoleMember> members = identityManagementRoleDocument.getMembers();
        for (final KimDocumentRoleMember roleMember : members) {
            final String memberName = roleMember.getMemberName().toLowerCase(Locale.US);
            if (memberName.startsWith(memberSearchValue.toLowerCase(Locale.US))) {
                roleMembersRestricted.add(roleMember);
            }
        }

        identityManagementRoleDocument.setSearchResultMembers(roleMembersRestricted);
    }

    @Override
    public void clearRestrictedRoleMembersSearchResults(
            final IdentityManagementRoleDocument identityManagementRoleDocument) {
        final List<KimDocumentRoleMember> roleMembersRestricted = new ArrayList<>();
        final List<KimDocumentRoleMember> members = identityManagementRoleDocument.getMembers();
        identityManagementRoleDocument.setSearchResultMembers(roleMembersRestricted);
        identityManagementRoleDocument.setMembers(members);
    }

    @Override
    public void setDelegationMembersInDocument(final IdentityManagementPersonDocument identityManagementPersonDocument) {
        if (CollectionUtils.isNotEmpty(identityManagementPersonDocument.getDelegations())) {
            for (final RoleDocumentDelegation delegation : identityManagementPersonDocument.getDelegations()) {
                if (CollectionUtils.isNotEmpty(delegation.getMembers())) {
                    for (final RoleDocumentDelegationMember member : delegation.getMembers()) {
                        if (StringUtils.equals(member.getMemberId(),
                                identityManagementPersonDocument.getPrincipalId())) {
                            member.setDelegationTypeCode(delegation.getDelegationTypeCode());
                            member.loadTransientRoleFields();
                            sortMemberQualifiersToMatchAttributeDefinitions(member);
                            identityManagementPersonDocument.getDelegationMembers().add(member);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setDelegationMembersInDocument(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getDelegations())) {
            for (final RoleDocumentDelegation delegation : identityManagementRoleDocument.getDelegations()) {
                if (CollectionUtils.isNotEmpty(delegation.getMembers())) {
                    RoleMember roleMember;
                    for (final RoleDocumentDelegationMember member : delegation.getMembers()) {
                        member.setDelegationTypeCode(delegation.getDelegationTypeCode());
                        if (StringUtils.isEmpty(member.getRoleMemberName())) {
                            roleMember = getRoleMemberForRoleMemberId(member.getRoleMemberId());
                            if (roleMember != null) {
                                member.setRoleMemberName(
                                        getMemberName(roleMember.getType(), roleMember.getMemberId()));
                                member.setRoleMemberNamespaceCode(getMemberNamespaceCode(roleMember.getType(),
                                        roleMember.getMemberId()));
                            }
                        }
                        member.setEdit(false);
                        identityManagementRoleDocument.getDelegationMembers().add(member);
                    }
                }
            }
        }
    }

    private static void sortMemberQualifiersToMatchAttributeDefinitions(final RoleDocumentDelegationMember member) {
        final Role memberRole = member.getMemberRole();
        if (ObjectUtils.isNotNull(memberRole)) {
            final KimType kimType = memberRole.getKimRoleType();
            if (kimType != null) {
                final KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(kimType);
                final List<KimAttributeField> attributeDefinitions =
                        kimTypeService.getAttributeDefinitions(kimType.getId());

                // After saving, the qualifiers might be in a different order if all the qualifiers weren't populated.
                // This sorts them to match the order of the attribute definitions so the values from qualifiers
                // will be matched up correctly with the labels from the attribute definitions.
                final Map<String, RoleDocumentDelegationMemberQualifier> qualifierMap =
                        member.getQualifiers().stream()
                                .collect(Collectors.toMap(
                                        RoleDocumentDelegationMemberQualifier::getKimAttrDefnId, qualifier -> qualifier)
                                );

                final List<RoleDocumentDelegationMemberQualifier> sortedQualifiers = attributeDefinitions.stream()
                        .map(attributeDefinition -> qualifierMap.get(attributeDefinition.getId()))
                        .collect(Collectors.toList());

                member.setQualifiers(sortedQualifiers);
            }
        }
    }

    protected List<KimDocumentRoleResponsibility> loadResponsibilities(
            final List<RoleResponsibility> roleResponsibilities) {
        final List<KimDocumentRoleResponsibility> documentRoleResponsibilities = new ArrayList<>();
        if (ObjectUtils.isNotNull(roleResponsibilities)) {
            for (final RoleResponsibility roleResponsibility : roleResponsibilities) {
                if (roleResponsibility.isActive()) {
                    final KimDocumentRoleResponsibility roleResponsibilityCopy = new KimDocumentRoleResponsibility();
                    KimCommonUtilsInternal.copyProperties(roleResponsibilityCopy, roleResponsibility);
                    roleResponsibilityCopy.setEdit(true);
                    documentRoleResponsibilities.add(roleResponsibilityCopy);
                }
            }
        }
        return documentRoleResponsibilities;
    }

    protected List<KimDocumentRolePermission> loadPermissions(final List<RolePermission> rolePermissions) {
        final List<KimDocumentRolePermission> documentRolePermissions = new ArrayList<>();
        KimDocumentRolePermission rolePermissionCopy;
        if (ObjectUtils.isNotNull(rolePermissions)) {
            for (final RolePermission rolePermission : rolePermissions) {
                if (rolePermission.isActive()) {
                    rolePermissionCopy = new KimDocumentRolePermission();
                    rolePermissionCopy.setRolePermissionId(rolePermission.getId());
                    rolePermissionCopy.setRoleId(rolePermission.getRoleId());
                    rolePermissionCopy.setPermissionId(rolePermission.getPermissionId());
                    rolePermissionCopy.setPermission(rolePermission.getPermission());
                    rolePermissionCopy.setEdit(true);
                    documentRolePermissions.add(rolePermissionCopy);
                }
            }
        }
        return documentRolePermissions;
    }

    @Override
    public void setMembersInDocument(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getDelegations())) {
            final Map<String, String> criteria = new HashMap<>();
            criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, identityManagementRoleDocument.getRoleId());
            final Role role = businessObjectService.findByPrimaryKey(Role.class, criteria);
            final List<RoleMember> members = role.getMembers();
            final List<RoleMember> membersToRemove = new ArrayList<>();
            boolean found = false;
            for (final KimDocumentRoleMember modifiedMember : identityManagementRoleDocument.getModifiedMembers()) {
                for (final RoleMember member : members) {
                    if (modifiedMember.getRoleMemberId().equals(member.getId())) {
                        membersToRemove.add(member);
                        found = true;
                    }
                    if (found) {
                        break;
                    }
                }
            }
            for (final RoleMember memberToRemove : membersToRemove) {
                members.remove(memberToRemove);
            }

            identityManagementRoleDocument.setMembers(loadRoleMembers(identityManagementRoleDocument, members));
            loadMemberRoleRspActions(identityManagementRoleDocument);
        }
    }

    Map<String, Group> findGroupsForRole(final String roleId) {
        final Map<String, Group> roleGroupMembers = new HashMap<>();

        // Find group members of a given role
        final List<RoleMember> groupRoleMembers = roleService.findRoleMembers(QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KIMPropertyConstants.RoleMember.ROLE_ID, roleId),
                PredicateFactory.equal(KIMPropertyConstants.RoleMember.MEMBER_TYPE_CODE,
                        MemberType.GROUP.getCode()))).getResults();

        final List<String> groupIds = new ArrayList<>(groupRoleMembers.size());
        for (final RoleMember rm : groupRoleMembers) {
            groupIds.add(rm.getMemberId());
        }

        final List<Group> groups = groupService.getGroups(groupIds);
        for (final Group g : groups) {
            roleGroupMembers.put(g.getId(), g);
        }
        return roleGroupMembers;
    }

    protected List<KimDocumentRoleMember> loadRoleMembers(
            final IdentityManagementRoleDocument identityManagementRoleDocument, final List<RoleMember> members) {
        final List<KimDocumentRoleMember> pndMembers = new ArrayList<>();

        if (ObjectUtils.isNull(members) || members.isEmpty()) {
            return pndMembers;
        }

        // extract all the principal role member IDs
        final List<String> roleMemberPrincipalIds = new ArrayList<>();
        for (final RoleMember roleMember : members) {
            if (roleMember.getType().getCode().equals(
                    KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE.getCode())) {
                if (!roleMemberPrincipalIds.contains(roleMember.getMemberId())) {
                    roleMemberPrincipalIds.add(roleMember.getMemberId());
                }
            }
        }

        // pull in all the group members of this role
        final Map<String, Group> roleGroupMembers = findGroupsForRole(identityManagementRoleDocument.getRoleId());

        for (final RoleMember member : members) {
            final KimDocumentRoleMember pndMember = new KimDocumentRoleMember();
            pndMember.setActiveFromDate(member.getActiveFromDateValue());
            pndMember.setActiveToDate(member.getActiveToDateValue());
            pndMember.setActive(member.isActive(dateTimeService.getCurrentTimestamp()));
            if (pndMember.isActive()) {
                pndMember.setRoleMemberId(member.getId());
                pndMember.setRoleId(member.getRoleId());
                pndMember.setMemberTypeCode(member.getType().getCode());
                pndMember.setMemberId(member.getMemberId());
                pndMember.setMemberNamespaceCode(getMemberNamespaceCode(member.getType(), member.getMemberId()));

                if (StringUtils.equals(pndMember.getMemberTypeCode(), MemberType.PRINCIPAL.getCode())) {
                    final Person person = personService.getPerson(member.getMemberId());
                    if (person != null) {
                        pndMember.setMemberName(person.getPrincipalName());
                        pndMember.setMemberFullName(person.getFirstName() + " " + person.getLastName());
                    }
                } else if (StringUtils.equals(pndMember.getMemberTypeCode(), MemberType.GROUP.getCode())) {
                    final Group group = roleGroupMembers.get(member.getMemberId());
                    if (group != null) {
                        pndMember.setMemberName(group.getName());
                        pndMember.setMemberNamespaceCode(group.getNamespaceCode());
                        pndMember.setMemberFullName(group.getName());
                    }
                } else if (StringUtils.equals(pndMember.getMemberTypeCode(), MemberType.ROLE.getCode())) {
                    pndMember.setMemberName(getMemberName(member.getType(), member.getMemberId()));
                    pndMember.setMemberFullName(getMemberFullName(member.getType(), member.getMemberId()));
                }

                pndMember.setQualifiers(loadRoleMemberQualifiers(identityManagementRoleDocument,
                        member.getAttributeDetails()));
                pndMember.setEdit(true);
                pndMembers.add(pndMember);
            }
        }
        pndMembers.sort(identityManagementRoleDocument.getMemberMetaDataType());
        return pndMembers;
    }

    protected void loadResponsibilityRoleRspActions(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getResponsibilities())) {
            for (final KimDocumentRoleResponsibility responsibility :
                    identityManagementRoleDocument.getResponsibilities()) {
                responsibility.getRoleRspActions().addAll(loadKimDocumentRoleRespActions(
                        getRoleResponsibilityActionImpls(responsibility.getRoleResponsibilityId())));
            }
        }
    }

    protected RoleResponsibilityAction getRoleResponsibilityActionImpl(final String roleResponsibilityActionId) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ID, roleResponsibilityActionId);
        return businessObjectService.findByPrimaryKey(RoleResponsibilityAction.class, criteria);
    }

    protected List<RoleResponsibilityAction> getRoleResponsibilityActionImpls(final String roleResponsibilityId) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, "*");
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_RESPONSIBILITY_ID, roleResponsibilityId);
        return (List<RoleResponsibilityAction>)
                businessObjectService.findMatching(RoleResponsibilityAction.class, criteria);
    }

    private List<RoleResponsibilityAction> getRoleMemberResponsibilityActionImpls(final String roleMemberId) {
        final Map<String, String> criteria = new HashMap<>(1);
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID, roleMemberId);
        return (List<RoleResponsibilityAction>)
                businessObjectService.findMatching(RoleResponsibilityAction.class, criteria);
    }

    protected void loadMemberRoleRspActions(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getMembers())) {
            for (final KimDocumentRoleMember member : identityManagementRoleDocument.getMembers()) {
                member.getRoleRspActions().addAll(loadKimDocumentRoleRespActions(
                        getRoleMemberResponsibilityActionImpls(member.getRoleMemberId())));
            }
        }
    }

    protected List<KimDocumentRoleResponsibilityAction> loadKimDocumentRoleRespActions(
            final List<RoleResponsibilityAction> roleRespActionImpls) {
        final List<KimDocumentRoleResponsibilityAction> documentRoleRespActions = new ArrayList<>();
        KimDocumentRoleResponsibilityAction documentRoleRespAction;
        if (ObjectUtils.isNotNull(roleRespActionImpls)) {
            for (final RoleResponsibilityAction roleRespActionImpl : roleRespActionImpls) {
                documentRoleRespAction = new KimDocumentRoleResponsibilityAction();
                KimCommonUtilsInternal.copyProperties(documentRoleRespAction, roleRespActionImpl);

                //primary key has different name in these objects!  we need to make sure to copy it over
                documentRoleRespAction.setRoleResponsibilityActionId(roleRespActionImpl.getId());

                // handle the roleResponsibility object being null since not all may be defined when ID value is "*"
                if (ObjectUtils.isNotNull(roleRespActionImpl.getRoleResponsibility())) {
                    documentRoleRespAction.setKimResponsibility(roleRespActionImpl.getRoleResponsibility()
                            .getKimResponsibility());
                }
                documentRoleRespActions.add(documentRoleRespAction);
            }
        }
        return documentRoleRespActions;
    }

    private BusinessObjectBase getMember(final MemberType memberType, final String memberId) {
        Class<? extends BusinessObjectBase> roleMemberTypeClass = null;
        String roleMemberIdName = "";
        if (MemberType.PRINCIPAL.equals(memberType)) {
            roleMemberTypeClass = Person.class;
            roleMemberIdName = KimConstants.PrimaryKeyConstants.PRINCIPAL_ID;
        } else if (MemberType.GROUP.equals(memberType)) {
            roleMemberTypeClass = Group.class;
            roleMemberIdName = KimConstants.PrimaryKeyConstants.GROUP_ID;
        } else if (MemberType.ROLE.equals(memberType)) {
            roleMemberTypeClass = Role.class;
            roleMemberIdName = KimConstants.PrimaryKeyConstants.ROLE_ID;
        }
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(roleMemberIdName, memberId);
        return businessObjectService.findByPrimaryKey(roleMemberTypeClass, criteria);
    }

    public String getMemberFullName(final MemberType memberType, final String memberId) {
        if (memberType == null || StringUtils.isEmpty(memberId)) {
            return "";
        }
        if (MemberType.PRINCIPAL.equals(memberType)) {
            final Person person = personService.getPerson(memberId);
            if (person != null) {
                return person.getFirstName() + " " + person.getLastName();
            }
        } else if (MemberType.GROUP.equals(memberType)) {
            final Group group = (Group) getMember(memberType, memberId);
            if (group != null) {
                return group.getName();
            }
        } else if (MemberType.ROLE.equals(memberType)) {
            final Role role = (Role) getMember(memberType, memberId);
            if (role != null) {
                return role.getName();
            }
        }
        return "";
    }

    private String getMemberIdByName(final MemberType memberType, final String memberNamespaceCode, final String memberName) {
        if (MemberType.PRINCIPAL.equals(memberType)) {
            final Person person = personService.getPersonByPrincipalName(memberName);
            if (person != null) {
                return person.getPrincipalId();
            }

        } else if (MemberType.GROUP.equals(memberType)) {
            final Group groupInfo = groupService.getGroupByNamespaceCodeAndName(memberNamespaceCode, memberName);
            if (groupInfo != null) {
                return groupInfo.getId();
            }

        } else if (MemberType.ROLE.equals(memberType)) {
            return roleService.getRoleIdByNamespaceCodeAndName(memberNamespaceCode, memberName);
        }
        return "";
    }

    @Override
    public String getMemberName(final MemberType memberType, final String memberId) {
        if (memberType == null || StringUtils.isEmpty(memberId)) {
            return "";
        }
        final Object member = getMember(memberType, memberId);
        if (member == null) {
            return "";
        }
        return getMemberName(memberType, member);
    }

    private String getMemberName(final MemberType memberType, final Object member) {
        if (MemberType.PRINCIPAL.equals(memberType)) {
            return ((Person) member).getPrincipalName();
        } else if (MemberType.GROUP.equals(memberType)) {
            return ((Group) member).getName();
        } else if (MemberType.ROLE.equals(memberType)) {
            return ((RoleContract) member).getName();
        }
        return "";
    }

    @Override
    public String getMemberNamespaceCode(final MemberType memberType, final String memberId) {
        if (memberType == null || StringUtils.isEmpty(memberId)) {
            return "";
        }
        final Object member = getMember(memberType, memberId);
        if (member == null) {
            return "";
        }
        if (MemberType.GROUP.equals(memberType)) {
            return ((Group) member).getNamespaceCode();
        } else if (MemberType.ROLE.equals(memberType)) {
            return ((RoleContract) member).getNamespaceCode();
        }
        return "";
    }

    private String getMemberNamespaceCode(final MemberType memberType, final Object member) {
        if (MemberType.GROUP.equals(memberType)) {
            return ((Group) member).getNamespaceCode();
        } else if (MemberType.ROLE.equals(memberType)) {
            return ((RoleContract) member).getNamespaceCode();
        }
        return "";
    }

    protected List<KimDocumentRoleQualifier> loadRoleMemberQualifiers(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final List<RoleMemberAttributeData> attributeDataList) {
        final List<KimDocumentRoleQualifier> pndMemberRoleQualifiers = new ArrayList<>();
        KimDocumentRoleQualifier pndMemberRoleQualifier;

        // add all attributes from attributeDataList
        if (attributeDataList != null) {
            for (final RoleMemberAttributeData memberRoleQualifier : attributeDataList) {
                pndMemberRoleQualifier = new KimDocumentRoleQualifier();
                pndMemberRoleQualifier.setAttrDataId(memberRoleQualifier.getId());
                pndMemberRoleQualifier.setAttrVal(memberRoleQualifier.getAttributeValue());
                pndMemberRoleQualifier.setRoleMemberId(memberRoleQualifier.getAssignedToId());
                pndMemberRoleQualifier.setKimTypId(memberRoleQualifier.getKimTypeId());
                pndMemberRoleQualifier.setKimAttrDefnId(memberRoleQualifier.getKimAttributeId());
                pndMemberRoleQualifier.setKimAttribute(memberRoleQualifier.getKimAttribute());
                formatAttrValIfNecessary(pndMemberRoleQualifier);
                pndMemberRoleQualifiers.add(pndMemberRoleQualifier);
            }
        }
        // also add any attributes already in the document that are not in the attributeDataList
        int countOfOriginalAttributesNotPresent = 0;
        final List<KimDocumentRoleQualifier> fillerRoleQualifiers = new ArrayList<>();

        final List<KimAttributeField> origAttributes = identityManagementRoleDocument.getDefinitions();
        if (origAttributes != null) {
            for (final KimAttributeField key : origAttributes) {
                boolean attributePresent = false;
                final String origAttributeId = identityManagementRoleDocument.getKimAttributeDefnId(key);
                if (attributeDataList != null) {
                    for (final RoleMemberAttributeData memberRoleQualifier : attributeDataList) {
                        if (origAttributeId != null
                                && StringUtils.equals(origAttributeId, memberRoleQualifier.getKimAttribute().getId())) {
                            attributePresent = true;
                            break;
                        }
                    }
                }
                if (!attributePresent) {
                    countOfOriginalAttributesNotPresent++;
                    pndMemberRoleQualifier = new KimDocumentRoleQualifier();
                    pndMemberRoleQualifier.setKimAttrDefnId(origAttributeId);
                    pndMemberRoleQualifier.refreshReferenceObject("kimAttribute");
                    fillerRoleQualifiers.add(pndMemberRoleQualifier);
                }
            }

            if (countOfOriginalAttributesNotPresent != origAttributes.size()) {
                pndMemberRoleQualifiers.addAll(fillerRoleQualifiers);
            }
        }
        return pndMemberRoleQualifiers;
    }

    protected List<RoleDocumentDelegation> loadRoleDocumentDelegations(
            final IdentityManagementRoleDocument identityManagementRoleDocument, final List<DelegateType> delegations) {
        final List<RoleDocumentDelegation> delList = new ArrayList<>();
        RoleDocumentDelegation documentDelegation;
        if (ObjectUtils.isNotNull(delegations)) {
            for (final DelegateType del : delegations) {
                documentDelegation = new RoleDocumentDelegation();
                documentDelegation.setActive(del.isActive());
                if (documentDelegation.isActive()) {
                    documentDelegation.setDelegationId(del.getDelegationId());
                    documentDelegation.setDelegationTypeCode(del.getDelegationTypeCode());
                    documentDelegation.setKimTypeId(del.getKimTypeId());
                    documentDelegation.setMembers(loadDelegationMembers(identityManagementRoleDocument,
                            del.getMembers()));
                    documentDelegation.setRoleId(del.getRoleId());
                    documentDelegation.setEdit(true);
                    delList.add(documentDelegation);
                }
            }
        }
        return delList;
    }

    protected List<RoleDocumentDelegationMember> loadDelegationMembers(
            final IdentityManagementPersonDocument identityManagementPersonDocument, final List<DelegateMember> members,
            final Role roleImpl) {
        final List<RoleDocumentDelegationMember> pndMembers = new ArrayList<>();
        RoleDocumentDelegationMember pndMember;
        RoleMember roleMember;
        if (ObjectUtils.isNotNull(members)) {
            for (final DelegateMember member : members) {
                // only include delegation members that match the person
                if (MemberType.PRINCIPAL.equals(member.getType())
                        && member.getMemberId().equals(identityManagementPersonDocument.getPrincipalId())) {
                    pndMember = new RoleDocumentDelegationMember();
                    pndMember.setActiveFromDate(member.getActiveFromDateValue());
                    pndMember.setActiveToDate(member.getActiveToDateValue());
                    pndMember.setActive(member.isActive(dateTimeService.getCurrentTimestamp()));
                    pndMember.setMemberRole(Role.from(roleImpl));

                    pndMember.setMemberId(member.getMemberId());
                    pndMember.setDelegationMemberId(member.getDelegationMemberId());
                    pndMember.setMemberTypeCode(member.getType().getCode());
                    pndMember.setDelegationId(member.getDelegationId());
                    pndMember.setVersionNumber(member.getVersionNumber());
                    pndMember.setObjectId(member.getObjectId());

                    pndMember.setRoleMemberId(member.getRoleMemberId());
                    roleMember = getRoleMemberForRoleMemberId(member.getRoleMemberId());
                    if (roleMember != null) {
                        pndMember.setRoleMemberName(getMemberName(roleMember.getType(), roleMember.getMemberId()));
                        pndMember.setRoleMemberNamespaceCode(getMemberNamespaceCode(roleMember.getType(),
                                roleMember.getMemberId()));
                    }
                    pndMember.setMemberNamespaceCode(getMemberNamespaceCode(member.getType(), member.getMemberId()));
                    pndMember.setMemberName(getMemberName(member.getType(), member.getMemberId()));
                    pndMember.setEdit(true);
                    pndMember.setQualifiers(loadDelegationMemberQualifiers(identityManagementPersonDocument,
                            pndMember.getAttributesHelper().getDefinitions(), member.getAttributeDetails()));
                    pndMembers.add(pndMember);
                }
            }
        }
        return pndMembers;
    }

    protected List<RoleDocumentDelegationMember> loadDelegationMembers(
            final IdentityManagementRoleDocument identityManagementRoleDocument, final List<DelegateMember> members) {
        final List<RoleDocumentDelegationMember> pndMembers = new ArrayList<>();
        RoleDocumentDelegationMember pndMember;
        RoleMember roleMember;
        if (ObjectUtils.isNotNull(members)) {
            for (final DelegateMember member : members) {
                pndMember = new RoleDocumentDelegationMember();
                pndMember.setActiveFromDate(member.getActiveFromDateValue());
                pndMember.setActiveToDate(member.getActiveToDateValue());
                pndMember.setActive(member.isActive(dateTimeService.getCurrentTimestamp()));
                if (pndMember.isActive()) {
                    pndMember.setDelegationId(member.getDelegationId());
                    pndMember.setDelegationMemberId(member.getDelegationMemberId());
                    pndMember.setDelegationTypeCode(member.getType().getCode());
                    pndMember.setRoleMemberId(member.getRoleMemberId());
                    pndMember.setMemberId(member.getMemberId());
                    pndMember.setMemberTypeCode(member.getType().getCode());

                    roleMember = getRoleMemberForRoleMemberId(member.getRoleMemberId());
                    if (roleMember != null) {
                        pndMember.setRoleMemberName(getMemberName(roleMember.getType(), roleMember.getMemberId()));
                        pndMember.setRoleMemberNamespaceCode(getMemberNamespaceCode(roleMember.getType(),
                                roleMember.getMemberId()));
                    }
                    pndMember.setMemberNamespaceCode(getMemberNamespaceCode(member.getType(), member.getMemberId()));
                    pndMember.setMemberName(getMemberName(member.getType(), member.getMemberId()));
                    pndMember.setEdit(true);
                    pndMember.setQualifiers(loadDelegationMemberQualifiers(identityManagementRoleDocument,
                            member.getAttributeDetails()));
                    pndMembers.add(pndMember);
                }
            }
        }
        return pndMembers;
    }

    protected RoleMember getRoleMemberForRoleMemberId(final String roleMemberId) {
        final Map<String, String> criteria = new HashMap<>(2);
        criteria.put(KimConstants.PrimaryKeyConstants.ID, roleMemberId);
        return businessObjectService.findByPrimaryKey(RoleMember.class, criteria);
    }

    protected List<RoleDocumentDelegationMemberQualifier> loadDelegationMemberQualifiers(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final List<KimAttributeField> origAttributeDefinitions, final List<DelegateMemberAttributeData> attributeDataList) {
        final List<RoleDocumentDelegationMemberQualifier> pndMemberRoleQualifiers = new ArrayList<>();
        RoleDocumentDelegationMemberQualifier pndMemberRoleQualifier;
        boolean attributePresent = false;
        String origAttributeId;
        if (origAttributeDefinitions != null) {
            for (final KimAttributeField key : origAttributeDefinitions) {
                origAttributeId = identityManagementPersonDocument.getKimAttributeDefnId(key);
                if (ObjectUtils.isNotNull(attributeDataList)) {
                    for (final DelegateMemberAttributeData memberRoleQualifier : attributeDataList) {
                        if (StringUtils.equals(origAttributeId, memberRoleQualifier.getKimAttribute().getId())) {
                            pndMemberRoleQualifier = new RoleDocumentDelegationMemberQualifier();
                            pndMemberRoleQualifier.setAttrDataId(memberRoleQualifier.getId());
                            pndMemberRoleQualifier.setAttrVal(memberRoleQualifier.getAttributeValue());
                            pndMemberRoleQualifier.setDelegationMemberId(memberRoleQualifier.getAssignedToId());
                            pndMemberRoleQualifier.setKimTypId(memberRoleQualifier.getKimTypeId());
                            pndMemberRoleQualifier.setKimAttrDefnId(memberRoleQualifier.getKimAttributeId());
                            pndMemberRoleQualifier.setKimAttribute(memberRoleQualifier.getKimAttribute());
                            pndMemberRoleQualifiers.add(pndMemberRoleQualifier);
                            attributePresent = true;
                        }
                    }
                }
                if (!attributePresent) {
                    pndMemberRoleQualifier = new RoleDocumentDelegationMemberQualifier();
                    pndMemberRoleQualifier.setKimAttrDefnId(origAttributeId);
                    pndMemberRoleQualifiers.add(pndMemberRoleQualifier);
                }
                attributePresent = false;
            }
        }
        return pndMemberRoleQualifiers;
    }

    protected List<RoleDocumentDelegationMemberQualifier> loadDelegationMemberQualifiers(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final List<DelegateMemberAttributeData> attributeDataList) {
        final List<RoleDocumentDelegationMemberQualifier> pndMemberRoleQualifiers = new ArrayList<>();
        RoleDocumentDelegationMemberQualifier pndMemberRoleQualifier;
        final List<KimAttributeField> origAttributes = identityManagementRoleDocument.getDefinitions();
        boolean attributePresent = false;
        String origAttributeId;
        if (origAttributes != null) {
            for (final KimAttributeField key : origAttributes) {
                origAttributeId = identityManagementRoleDocument.getKimAttributeDefnId(key);
                if (attributeDataList != null) {
                    for (final DelegateMemberAttributeData memberRoleQualifier : attributeDataList) {
                        if (origAttributeId != null
                                && StringUtils.equals(origAttributeId, memberRoleQualifier.getKimAttribute().getId())) {
                            pndMemberRoleQualifier = new RoleDocumentDelegationMemberQualifier();
                            pndMemberRoleQualifier.setAttrDataId(memberRoleQualifier.getId());
                            pndMemberRoleQualifier.setAttrVal(memberRoleQualifier.getAttributeValue());
                            pndMemberRoleQualifier.setDelegationMemberId(memberRoleQualifier.getAssignedToId());
                            pndMemberRoleQualifier.setKimTypId(memberRoleQualifier.getKimTypeId());
                            pndMemberRoleQualifier.setKimAttrDefnId(memberRoleQualifier.getKimAttributeId());
                            pndMemberRoleQualifier.setKimAttribute(memberRoleQualifier.getKimAttribute());
                            pndMemberRoleQualifiers.add(pndMemberRoleQualifier);
                            attributePresent = true;
                        }
                    }
                }
                if (!attributePresent) {
                    pndMemberRoleQualifier = new RoleDocumentDelegationMemberQualifier();
                    pndMemberRoleQualifier.setKimAttrDefnId(origAttributeId);
                    pndMemberRoleQualifier.refreshReferenceObject("kimAttribute");
                    pndMemberRoleQualifiers.add(pndMemberRoleQualifier);
                }
                attributePresent = false;
            }
        }
        return pndMemberRoleQualifiers;
    }

    @CacheEvict(value = {Role.CACHE_NAME, RoleMember.CACHE_NAME, Permission.CACHE_NAME}, allEntries = true)
    @Override
    public void saveRole(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        final Map<String, String> criteria = new HashMap<>();
        final String roleId = identityManagementRoleDocument.getRoleId();
        criteria.put(KimConstants.PrimaryKeyConstants.ID, roleId);
        Role role = businessObjectService.findByPrimaryKey(Role.class, criteria);

        final List<PersistableBusinessObject> objectsToSave = new ArrayList<>();

        if (role == null) {
            role = new Role();
            role.setId(roleId);
            role.setKimTypeId(identityManagementRoleDocument.getRoleTypeId());
            role.setNamespaceCode(identityManagementRoleDocument.getRoleNamespace());
            identityManagementRoleDocument.setActive(true);
        }

        // ==== CU Customization: Backport FINP-9525 fix ====
        boolean updateActionRequests = role.isActive() != identityManagementRoleDocument.isActive();
        // ==== End CU Customization ====

        final Map<String, String> altCriteria = new HashMap<>();
        altCriteria.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, roleId);

        final List<RolePermission> origRolePermissions = (List<RolePermission>) (businessObjectService
                .findMatching(RolePermission.class, altCriteria));
        final List<RoleResponsibility> origRoleResponsibilities = (List<RoleResponsibility>) businessObjectService
                .findMatching(RoleResponsibility.class, altCriteria);
        final List<DelegateType> origRoleDelegations = (List<DelegateType>) businessObjectService
                .findMatching(DelegateType.class, altCriteria);
        // ==== CU Customization: Backport FINP-9525 fix ====
        // Due to (caching?), the members on the role delegations above are the same instances as the members
        // on the role itself, i.e. role.getDelegations().get(0).getMembers() == origRoleDelegations.get(0)
        // .getMembers().  These members end up being modified by the process, therefore we can't use them later
        // to determine if anything changed.  So we'll save off a copy of the delegations for that purpose.
        final List<DelegateType> unchangedRoleDelegations = origRoleDelegations.stream()
                .map(ObjectUtils::deepCopy)
                .map(DelegateType.class::cast)
                .collect(Collectors.toList());
        // ==== End CU Customization ====

        role.setName(identityManagementRoleDocument.getRoleName());
        role.setDescription(identityManagementRoleDocument.getRoleDescription());
        role.setActive(identityManagementRoleDocument.isActive());
        role.setPermissions(getRolePermissions(identityManagementRoleDocument, origRolePermissions));
        role.setResponsibilities(getRoleResponsibilities(identityManagementRoleDocument, origRoleResponsibilities));
        // ==== CU Customization: Backport FINP-9525 fix ====
        updateActionRequests = updateActionRequests
                || workflowHelper.roleResponsibilitiesChanged(role.getResponsibilities(), origRoleResponsibilities);
        // ==== End CU Customization ====
        objectsToSave.add(role);
        objectsToSave.addAll(getRoleResponsibilitiesActions(identityManagementRoleDocument));

        if (kimTypeInfoService.getKimType(identityManagementRoleDocument.getRoleTypeId()) == null) {
            LOG.error(
                    "Kim type not found for:{}",
                    identityManagementRoleDocument::getRoleTypeId,
                    Throwable::new
            );
        }

        final String initiatorPrincipalId = getInitiatorPrincipalId(identityManagementRoleDocument);

        if (canAssignToRole(identityManagementRoleDocument, initiatorPrincipalId)) {
            updateRoleMembers(role.getId(),
                    identityManagementRoleDocument.getModifiedMembers(), role.getMembers());
            // ==== CU Customization: Backport FINP-9525 fix ====
            updateActionRequests = updateActionRequests
                    || !identityManagementRoleDocument.getModifiedMembers().isEmpty();

            objectsToSave.addAll(getRoleMemberResponsibilityActions(role.getMembers()));
            final List<DelegateType> newRoleDelegations =
                    populateDelegations(identityManagementRoleDocument, origRoleDelegations);
            objectsToSave.addAll(newRoleDelegations);
            updateActionRequests = updateActionRequests
                    || workflowHelper.delegationsChanged(newRoleDelegations, unchangedRoleDelegations);
            // ==== End CU Customization ====
        }

        businessObjectService.save(objectsToSave);
        // ==== CU Customization: Backport FINP-9525 fix ====
        if (updateActionRequests) {
            responsibilityInternalService.updateActionRequestsForResponsibilityChange(
                    getChangedRoleResponsibilityIds(identityManagementRoleDocument, origRoleResponsibilities));
        }
        // ==== End CU Customization ====
        if (!role.isActive()) {
            // when a role is inactivated, inactivate the memberships of principals, groups, and roles in
            // that role, delegations, and delegation members, and that roles memberships in other roles
            roleInternalService.roleInactivated(identityManagementRoleDocument.getRoleId());
        }
    }

    protected List<RolePermission> getRolePermissions(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final List<RolePermission> origRolePermissions) {
        final List<RolePermission> rolePermissions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getPermissions())) {
            for (final KimDocumentRolePermission documentRolePermission : identityManagementRoleDocument.getPermissions()) {
                final RolePermission newRolePermission = new RolePermission();
                newRolePermission.setId(documentRolePermission.getRolePermissionId());
                newRolePermission.setRoleId(identityManagementRoleDocument.getRoleId());
                newRolePermission.setPermissionId(documentRolePermission.getPermissionId());
                newRolePermission.setActive(documentRolePermission.isActive());

                newRolePermission.setActive(documentRolePermission.isActive());
                if (ObjectUtils.isNotNull(origRolePermissions)) {
                    for (final RolePermission origPermissionImpl : origRolePermissions) {
                        if (!StringUtils.equals(origPermissionImpl.getRoleId(), newRolePermission.getRoleId())
                                && StringUtils.equals(origPermissionImpl.getPermissionId(),
                                    newRolePermission.getPermissionId())
                                && origPermissionImpl.isActive()
                                && newRolePermission.isActive()) {
                            newRolePermission.setId(origPermissionImpl.getId());
                        }
                        if (origPermissionImpl.getId() != null
                                && StringUtils.equals(origPermissionImpl.getId(), newRolePermission.getId())) {
                            newRolePermission.setVersionNumber(origPermissionImpl.getVersionNumber());
                            newRolePermission.setObjectId(origPermissionImpl.getObjectId());
                        }
                    }
                }
                rolePermissions.add(newRolePermission);
            }
        }
        return rolePermissions;
    }

    protected List<RoleResponsibility> getRoleResponsibilities(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final List<RoleResponsibility> origRoleResponsibilities) {
        final List<RoleResponsibility> roleResponsibilities = new ArrayList<>();
        RoleResponsibility newRoleResponsibility;
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getResponsibilities())) {
            for (final KimDocumentRoleResponsibility documentRoleResponsibility :
                    identityManagementRoleDocument.getResponsibilities()) {
                newRoleResponsibility = new RoleResponsibility();
                KimCommonUtilsInternal.copyProperties(newRoleResponsibility, documentRoleResponsibility);
                newRoleResponsibility.setActive(documentRoleResponsibility.isActive());
                newRoleResponsibility.setRoleId(identityManagementRoleDocument.getRoleId());
                newRoleResponsibility.setVersionNumber(null);
                newRoleResponsibility.setObjectId(null);
                if (ObjectUtils.isNotNull(origRoleResponsibilities)) {
                    for (final RoleResponsibility origResponsibilityImpl : origRoleResponsibilities) {
                        if (!StringUtils.equals(origResponsibilityImpl.getRoleId(), newRoleResponsibility.getRoleId())
                                && StringUtils.equals(origResponsibilityImpl.getResponsibilityId(),
                                    newRoleResponsibility.getResponsibilityId())
                                && !origResponsibilityImpl.isActive() && newRoleResponsibility.isActive()) {
                            newRoleResponsibility.setRoleResponsibilityId(
                                    origResponsibilityImpl.getRoleResponsibilityId());
                        }
                        if (origResponsibilityImpl.getRoleResponsibilityId() != null
                                && StringUtils.equals(origResponsibilityImpl.getRoleResponsibilityId(),
                                    newRoleResponsibility.getRoleResponsibilityId())) {
                            newRoleResponsibility.setVersionNumber(origResponsibilityImpl.getVersionNumber());
                            newRoleResponsibility.setObjectId(origResponsibilityImpl.getObjectId());
                        }
                    }
                }
                roleResponsibilities.add(newRoleResponsibility);
            }
        }
        return roleResponsibilities;
    }

    protected List<RoleResponsibilityAction> getRoleResponsibilitiesActions(
            final IdentityManagementRoleDocument identityManagementRoleDocument) {
        final List<RoleResponsibilityAction> roleRspActions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getResponsibilities())) {
            // loop over the responsibilities assigned to the role
            for (final KimDocumentRoleResponsibility roleResponsibility : identityManagementRoleDocument
                    .getResponsibilities()) {
                // only process if the actions are not assigned at the role member level
                if (!responsibilityInternalService
                        .areActionsAtAssignmentLevelById(roleResponsibility.getResponsibilityId())) {
                    final List<KimDocumentRoleResponsibilityAction> documentRoleResponsibilityActions =
                            roleResponsibility.getRoleRspActions();
                    if (ObjectUtils.isNotNull(documentRoleResponsibilityActions)
                            && !documentRoleResponsibilityActions.isEmpty()
                            && StringUtils.isNotBlank(documentRoleResponsibilityActions.get(0)
                                .getRoleResponsibilityActionId())) {
                        final RoleResponsibilityAction roleRspAction = new RoleResponsibilityAction();
                        roleRspAction.setId(documentRoleResponsibilityActions.get(0).getRoleResponsibilityActionId());
                        roleRspAction.setActionPolicyCode(documentRoleResponsibilityActions.get(0)
                                .getActionPolicyCode());
                        roleRspAction.setActionTypeCode(documentRoleResponsibilityActions.get(0).getActionTypeCode());
                        roleRspAction.setPriorityNumber(documentRoleResponsibilityActions.get(0).getPriorityNumber());
                        roleRspAction.setForceAction(documentRoleResponsibilityActions.get(0).isForceAction());
                        roleRspAction.setRoleMemberId("*");
                        roleRspAction.setRoleResponsibilityId(documentRoleResponsibilityActions.get(0)
                                .getRoleResponsibilityId());
                        updateResponsibilityActionVersionNumber(roleRspAction, getRoleResponsibilityActionImpl(
                                roleRspAction.getId()));
                        roleRspActions.add(roleRspAction);
                    }
                }
            }
        }
        return roleRspActions;
    }

    // FIXME: This should be pulling by the PK, not using another method which pulls multiple records and then finds
    // the right one here!

    protected void updateResponsibilityActionVersionNumber(
            final RoleResponsibilityAction newRoleRspAction,
                                                           final RoleResponsibilityAction origRoleRespActionImpl) {
        if (ObjectUtils.isNotNull(origRoleRespActionImpl)) {
            if (origRoleRespActionImpl.getId() != null
                    && StringUtils.equals(origRoleRespActionImpl.getId(), newRoleRspAction.getId())) {
                newRoleRspAction.setVersionNumber(origRoleRespActionImpl.getVersionNumber());
                newRoleRspAction.setObjectId(origRoleRespActionImpl.getObjectId());
            }
        }
    }

    protected List<RoleResponsibilityAction> getRoleMemberResponsibilityActions(
            final List<RoleMember> newRoleMembersList) {
        final List<RoleResponsibilityAction> roleRspActions = new ArrayList<>();
        if (ObjectUtils.isNotNull(newRoleMembersList)) {
            for (final RoleMember roleMember : newRoleMembersList) {
                if (roleMember.getRoleRspActions() != null) {
                    roleRspActions.addAll(roleMember.getRoleRspActions());
                }
            }
        }
        return roleRspActions;
    }

    protected void updateRoleMembers(
            final String roleId, final List<KimDocumentRoleMember> modifiedRoleMembers,
            final List<RoleMember> roleMembers) {
        if (CollectionUtils.isEmpty(modifiedRoleMembers)) {
            return;
        }
        for (final KimDocumentRoleMember documentRoleMember : modifiedRoleMembers) {
            boolean isNewRoleMember = true;
            for (final RoleMember roleMember : roleMembers) {
                // are we editing an existing record?
                if (StringUtils.equals(roleMember.getId(), documentRoleMember.getRoleMemberId())) {
                    // yes we are
                    roleMember.setActiveFromDateValue(documentRoleMember.getActiveFromDate());
                    roleMember.setActiveToDateValue(documentRoleMember.getActiveToDate());
                    isNewRoleMember = false;
                    final List<RoleResponsibilityAction> responsibilityActions =
                            getRoleMemberResponsibilityActionImpls(roleMember.getId());
                    roleMember.setRoleRspActions(responsibilityActions);
                    updateRoleMemberResponsibilityActions(documentRoleMember.getRoleRspActions(),
                            roleMember.getRoleRspActions());
                    break;
                }
            }
            if (isNewRoleMember) {
                final RoleMember roleMember = new RoleMember();
                roleMember.setId(documentRoleMember.getRoleMemberId());
                roleMember.setRoleId(roleId);
                roleMember.setTypeCode(documentRoleMember.getMemberTypeCode());
                roleMember.setMemberId(documentRoleMember.getMemberId());
                roleMember.setType(MemberType.fromCode(documentRoleMember.getMemberTypeCode()));
                roleMember.setActiveFromDateValue(documentRoleMember.getActiveFromDate());
                roleMember.setActiveToDateValue(documentRoleMember.getActiveToDate());

                roleMember.setAttributeDetails(getRoleMemberAttributeData(documentRoleMember.getQualifiers()));
                roleMember.setRoleRspActions(new ArrayList<>());
                updateRoleMemberResponsibilityActions(documentRoleMember.getRoleRspActions(),
                        roleMember.getRoleRspActions());

                roleMembers.add(roleMember);
            }
        }
    }

    // FIXME : this is not working yet

    protected void updateRoleMemberResponsibilityActions(
            final List<KimDocumentRoleResponsibilityAction> documentRoleMemberActions,
            final List<RoleResponsibilityAction> roleMemberActions) {
        // Make a copy of the list which we can modify - so that we can use it to
        // remove leftovers from the original list when done with updates and inserts
        final List<RoleResponsibilityAction> existingRoleMemberActions = new ArrayList<>(roleMemberActions);
        // loop over document items
        for (final KimDocumentRoleResponsibilityAction docRoleRspAction : documentRoleMemberActions) {
            boolean isNewAction = true;
            // loop over role member items
            final Iterator<RoleResponsibilityAction> rraInterator = existingRoleMemberActions.iterator();
            while (rraInterator.hasNext()) {
                final RoleResponsibilityAction roleRspAction = rraInterator.next();
                // we have a match, update the existing record
                // If the ID's match
                if (StringUtils.equals(roleRspAction.getId(), docRoleRspAction.getRoleResponsibilityActionId())) {
                    // update the existing record
                    roleRspAction.setActionPolicyCode(docRoleRspAction.getActionPolicyCode());
                    roleRspAction.setActionTypeCode(docRoleRspAction.getActionTypeCode());
                    roleRspAction.setPriorityNumber(docRoleRspAction.getPriorityNumber());
                    roleRspAction.setRoleMemberId(docRoleRspAction.getRoleMemberId());
                    roleRspAction.setForceAction(docRoleRspAction.isForceAction());
                    // mark it as a "found" record
                    rraInterator.remove();
                    isNewAction = false;
                }
            }
            // if no match on the loop, then we have a new record
            if (isNewAction) {
                // create the new item and add it to the list
                final RoleResponsibilityAction newRoleRspAction = new RoleResponsibilityAction();
                newRoleRspAction.setId(docRoleRspAction.getRoleResponsibilityActionId());
                newRoleRspAction.setActionPolicyCode(docRoleRspAction.getActionPolicyCode());
                newRoleRspAction.setActionTypeCode(docRoleRspAction.getActionTypeCode());
                newRoleRspAction.setPriorityNumber(docRoleRspAction.getPriorityNumber());
                newRoleRspAction.setRoleMemberId(docRoleRspAction.getRoleMemberId());
                newRoleRspAction.setForceAction(docRoleRspAction.isForceAction());
                newRoleRspAction.setRoleResponsibilityId("*");
                roleMemberActions.add(newRoleRspAction);
            }
        }
        // for all items not "found", they are no longer present, delete them
        for (final RoleResponsibilityAction missingRra : existingRoleMemberActions) {
            roleMemberActions.remove(missingRra);
        }
    }

    protected List<RoleMemberAttributeData> getRoleMemberAttributeData(final List<KimDocumentRoleQualifier> qualifiers) {
        final List<RoleMemberAttributeData> roleMemberAttributeDataList = new ArrayList<>();
        RoleMemberAttributeData newRoleMemberAttributeData;
        if (CollectionUtils.isNotEmpty(qualifiers)) {
            for (final KimDocumentRoleQualifier memberRoleQualifier : qualifiers) {
                if (StringUtils.isNotBlank(memberRoleQualifier.getAttrVal())) {
                    newRoleMemberAttributeData = new RoleMemberAttributeData();
                    newRoleMemberAttributeData.setId(memberRoleQualifier.getAttrDataId());
                    newRoleMemberAttributeData.setAttributeValue(memberRoleQualifier.getAttrVal());
                    newRoleMemberAttributeData.setAssignedToId(memberRoleQualifier.getRoleMemberId());
                    newRoleMemberAttributeData.setKimTypeId(memberRoleQualifier.getKimTypId());
                    newRoleMemberAttributeData.setKimAttributeId(memberRoleQualifier.getKimAttrDefnId());
                    updateAttrValIfNecessary(newRoleMemberAttributeData);
                    roleMemberAttributeDataList.add(newRoleMemberAttributeData);
                }
            }
        }
        return roleMemberAttributeDataList;
    }

    /**
     * Determines if the attribute value on the attribute data should be updated; if so, it performs some attribute
     * value formatting. In the default implementation, this method formats checkbox controls
     *
     * @param roleMemberAttributeData a role member qualifier attribute to update
     */
    protected void updateAttrValIfNecessary(final RoleMemberAttributeData roleMemberAttributeData) {
        if (doCheckboxLogic(roleMemberAttributeData.getKimTypeId(), roleMemberAttributeData.getKimAttributeId())) {
            convertCheckboxAttributeData(roleMemberAttributeData);
        }
    }

    protected void formatAttrValIfNecessary(final KimDocumentRoleQualifier roleQualifier) {
        if (doCheckboxLogic(roleQualifier.getKimTypId(), roleQualifier.getKimAttrDefnId())) {
            formatCheckboxAttributeData(roleQualifier);
        }
    }

    private boolean doCheckboxLogic(final String kimTypeId, final String attrId) {
        final KimAttributeField attributeDefinition = getAttributeDefinition(kimTypeId, attrId);
        return attributeDefinition != null
                && attributeDefinition.getAttributeField().getControl() != null
                && attributeDefinition.getAttributeField().getControl() instanceof CheckboxControlDefinition;
    }

    protected void formatCheckboxAttributeData(final KimDocumentRoleQualifier roleQualifier) {
        if (roleQualifier.getAttrVal().equals(KimConstants.KIM_ATTRIBUTE_BOOLEAN_TRUE_STR_VALUE)) {
            roleQualifier.setAttrVal(KimConstants.KIM_ATTRIBUTE_BOOLEAN_TRUE_STR_VALUE_DISPLAY);
        } else if (roleQualifier.getAttrVal().equals(KimConstants.KIM_ATTRIBUTE_BOOLEAN_FALSE_STR_VALUE)) {
            roleQualifier.setAttrVal(KimConstants.KIM_ATTRIBUTE_BOOLEAN_FALSE_STR_VALUE_DISPLAY);
        }
    }

    /**
     * Finds the KNS attribute used to render the given KimAttributeData
     *
     * @return the KNS attribute used to render that qualifier, or null if the AttributeDefinition cannot be determined
     */
    protected KimAttributeField getAttributeDefinition(final String kimTypId, final String attrDefnId) {
        final KimType type = kimTypeInfoService.getKimType(kimTypId);
        if (type != null && StringUtils.isNotBlank(type.getServiceName())) {
            final KimTypeService typeService = (KimTypeService) KimImplServiceLocator.getBean(type.getServiceName());
            if (typeService != null) {
                final KimTypeAttribute attributeInfo = type.getAttributeDefinitionById(attrDefnId);
                if (attributeInfo != null) {
                    final List<KimAttributeField> attributeMap = typeService.getAttributeDefinitions(type.getId());
                    if (attributeMap != null) {
                        return DataDictionaryTypeServiceHelper.findAttributeField(
                                attributeInfo.getKimAttribute().getAttributeName(), attributeMap);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Formats the attribute value on this checkbox attribute, changing "on" to "Y" and "off" to "N"
     *
     * @param roleMemberAttributeData the attribute data to format the attribute value of
     */
    protected void convertCheckboxAttributeData(final RoleMemberAttributeData roleMemberAttributeData) {
        if (roleMemberAttributeData.getAttributeValue().equalsIgnoreCase(
                KimConstants.KIM_ATTRIBUTE_BOOLEAN_TRUE_STR_VALUE_DISPLAY)) {
            roleMemberAttributeData.setAttributeValue(KimConstants.KIM_ATTRIBUTE_BOOLEAN_TRUE_STR_VALUE);
        } else if (roleMemberAttributeData.getAttributeValue().equalsIgnoreCase(
                KimConstants.KIM_ATTRIBUTE_BOOLEAN_FALSE_STR_VALUE_DISPLAY)) {
            roleMemberAttributeData.setAttributeValue(KimConstants.KIM_ATTRIBUTE_BOOLEAN_FALSE_STR_VALUE);
        }
    }

    private List<DelegateType> getRoleDelegations(final String roleId) {
        if (roleId == null) {
            return new ArrayList<>();
        }
        final Map<String, String> criteria = new HashMap<>(1);
        criteria.put("roleId", roleId);
        return (List<DelegateType>) businessObjectService.findMatching(DelegateType.class, criteria);
    }

    protected List<DelegateMember> getDelegationMembers(
            final List<RoleDocumentDelegationMember> delegationMembers,
                                                        final List<DelegateMember> origDelegationMembers, final List<DelegateMember> allOrigMembers,
                                                        final boolean activatingInactive, final String newDelegationIdAssigned) {
        final List<DelegateMember> delegationsMembersList = new ArrayList<>();
        DelegateMember newDelegationMemberImpl;
        DelegateMember origDelegationMemberImplTemp = null;
        List<DelegateMemberAttributeData> origAttributes;
        String delegationMemberId = "";
        if (CollectionUtils.isNotEmpty(delegationMembers)) {
            for (final RoleDocumentDelegationMember delegationMember : delegationMembers) {
                newDelegationMemberImpl = new DelegateMember();
                KimCommonUtilsInternal.copyProperties(newDelegationMemberImpl, delegationMember);
                newDelegationMemberImpl.setVersionNumber(null);
                newDelegationMemberImpl.setObjectId(null);
                newDelegationMemberImpl.setType(MemberType.fromCode(delegationMember.getMemberTypeCode()));
                if (ObjectUtils.isNotNull(origDelegationMembers)) {
                    for (final DelegateMember origDelegationMember : origDelegationMembers) {
                        if (activatingInactive
                                && StringUtils.equals(origDelegationMember.getMemberId(),
                                    newDelegationMemberImpl.getMemberId())
                                && StringUtils.equals(newDelegationMemberImpl.getDelegationId(),
                                    newDelegationIdAssigned)
                                && !origDelegationMember.isActive(dateTimeService.getCurrentTimestamp())) {
                            newDelegationMemberImpl.setDelegationId(origDelegationMember.getDelegationId());
                            delegationMemberId = newDelegationMemberImpl.getDelegationMemberId();
                            newDelegationMemberImpl.setDelegationMemberId(
                                    origDelegationMember.getDelegationMemberId());
                        }
                        if (origDelegationMember.getDelegationMemberId() != null
                                && StringUtils.equals(origDelegationMember.getDelegationMemberId(),
                                    newDelegationMemberImpl.getDelegationMemberId())) {
                            newDelegationMemberImpl.setVersionNumber(origDelegationMember.getVersionNumber());
                            newDelegationMemberImpl.setObjectId(origDelegationMember.getObjectId());
                            origDelegationMemberImplTemp = origDelegationMember;
                        }
                    }
                }
                if (ObjectUtils.isNotNull(allOrigMembers)) {
                    for (final DelegateMember origMember : allOrigMembers) {
                        if (origMember.getDelegationMemberId() != null
                                && origMember.getDelegationMemberId().equals(delegationMember.getDelegationMemberId())
                                && origMember.getRoleMemberId() != null
                                && origMember.getRoleMemberId().equals(delegationMember.getRoleMemberId())) {
                            newDelegationMemberImpl.setVersionNumber(origMember.getVersionNumber());
                            newDelegationMemberImpl.setObjectId(origMember.getObjectId());
                            origDelegationMemberImplTemp = origMember;
                        }
                    }
                }
                origAttributes = origDelegationMemberImplTemp == null
                        || origDelegationMemberImplTemp.getAttributeDetails() == null ? new ArrayList<>() :
                            origDelegationMemberImplTemp.getAttributeDetails();
                newDelegationMemberImpl.setAttributeDetails(getDelegationMemberAttributeData(
                        delegationMember.getQualifiers(), origAttributes, activatingInactive, delegationMemberId));
                newDelegationMemberImpl.setActiveFromDateValue(delegationMember.getActiveFromDate());
                newDelegationMemberImpl.setActiveToDateValue(delegationMember.getActiveToDate());
                delegationsMembersList.add(newDelegationMemberImpl);
            }
        }
        return delegationsMembersList;
    }

    //TODO: implement logic same as role members - do not insert qualifiers with blank values

    protected List<DelegateMemberAttributeData> getDelegationMemberAttributeData(
            final List<RoleDocumentDelegationMemberQualifier> qualifiers,
            final List<DelegateMemberAttributeData> origAttributes,
            final boolean activatingInactive, final String delegationMemberId) {
        final List<DelegateMemberAttributeData> delegationMemberAttributeDataList = new ArrayList<>();
        DelegateMemberAttributeData newDelegationMemberAttributeData;
        if (CollectionUtils.isNotEmpty(qualifiers)) {
            for (final RoleDocumentDelegationMemberQualifier memberRoleQualifier : qualifiers) {
                if (StringUtils.isNotBlank(memberRoleQualifier.getAttrVal())) {
                    newDelegationMemberAttributeData = new DelegateMemberAttributeData();
                    newDelegationMemberAttributeData.setId(memberRoleQualifier.getAttrDataId());
                    newDelegationMemberAttributeData.setAttributeValue(memberRoleQualifier.getAttrVal());
                    newDelegationMemberAttributeData.setAssignedToId(memberRoleQualifier.getDelegationMemberId());
                    newDelegationMemberAttributeData.setKimTypeId(memberRoleQualifier.getKimTypId());
                    newDelegationMemberAttributeData.setKimAttributeId(memberRoleQualifier.getKimAttrDefnId());
                    if (ObjectUtils.isNotNull(origAttributes)) {
                        for (final DelegateMemberAttributeData origAttribute : origAttributes) {
                            if (activatingInactive
                                    && StringUtils.equals(origAttribute.getKimAttributeId(),
                                        newDelegationMemberAttributeData.getKimAttributeId())
                                    && StringUtils.equals(newDelegationMemberAttributeData.getAssignedToId(),
                                        delegationMemberId)) {
                                newDelegationMemberAttributeData.setAssignedToId(origAttribute.getAssignedToId());
                                newDelegationMemberAttributeData.setId(origAttribute.getId());
                            }
                            if (StringUtils.equals(origAttribute.getId(), newDelegationMemberAttributeData.getId())) {
                                newDelegationMemberAttributeData.setVersionNumber(origAttribute.getVersionNumber());
                            }
                        }
                    }
                    delegationMemberAttributeDataList.add(newDelegationMemberAttributeData);
                }
            }
        }
        return delegationMemberAttributeDataList;
    }
    /* Group document methods */

    @Override
    public void loadGroupDoc(final IdentityManagementGroupDocument identityManagementGroupDocument, final Group groupInfo) {
        identityManagementGroupDocument.setGroupId(groupInfo.getId());
        final KimType kimType = kimTypeInfoService.getKimType(groupInfo.getKimTypeId());
        identityManagementGroupDocument.setKimType(kimType);
        identityManagementGroupDocument.setGroupTypeName(kimType.getName());
        identityManagementGroupDocument.setGroupTypeId(kimType.getId());
        identityManagementGroupDocument.setGroupName(groupInfo.getName());
        identityManagementGroupDocument.setGroupDescription(groupInfo.getDescription());
        identityManagementGroupDocument.setActive(groupInfo.isActive());
        identityManagementGroupDocument.setGroupNamespace(groupInfo.getNamespaceCode());

        final List<GroupMember> members = new ArrayList<>(groupService.getMembersOfGroup(groupInfo.getId()));
        identityManagementGroupDocument.setMembers(loadGroupMembers(members));

        identityManagementGroupDocument.setQualifiers(loadGroupQualifiers(identityManagementGroupDocument,
                groupInfo.getAttributes()));
        identityManagementGroupDocument.setEditing(true);
    }

    protected List<GroupDocumentMember> loadGroupMembers(final List<GroupMember> members) {
        final List<GroupDocumentMember> pndMembers = new ArrayList<>();
        if (ObjectUtils.isNotNull(members)) {
            for (final GroupMember member : members) {
                final GroupDocumentMember pndMember = new GroupDocumentMember();

                pndMember.setActiveFromDate(member.getActiveFromDate() == null ? null :
                        new Timestamp(member.getActiveFromDate().getMillis()));
                pndMember.setActiveToDate(member.getActiveToDate() == null ? null :
                        new Timestamp(member.getActiveToDate().getMillis()));
                pndMember.setGroupMemberId(member.getMemberId());
                pndMember.setGroupId(member.getGroupId());
                pndMember.setMemberId(member.getMemberId());
                pndMember.setMemberName(getMemberName(member.getType(), member.getMemberId()));
                pndMember.setMemberFullName(getMemberFullName(member.getType(), member.getMemberId()));
                pndMember.setMemberTypeCode(member.getType().getCode());
                pndMember.setEdit(true);
                pndMembers.add(pndMember);
            }
        }
        pndMembers.sort(groupMemberNameComparator);
        return pndMembers;
    }

    @Override
    public List<GroupDocumentQualifier> loadGroupQualifiers(
            final IdentityManagementGroupDocument identityManagementGroupDocument, final Map<String, String> attributes
    ) {
        final List<GroupDocumentQualifier> pndGroupQualifiers = new ArrayList<>();
        GroupDocumentQualifier pndGroupQualifier;
        final List<KimAttributeField> origAttributes = identityManagementGroupDocument.getDefinitions();
        boolean attributePresent = false;
        String origAttributeId;
        if (origAttributes != null) {
            for (final KimAttributeField key : origAttributes) {
                origAttributeId = identityManagementGroupDocument.getKimAttributeDefnId(key);
                if (!attributes.isEmpty()) {
                    for (final GroupAttribute groupQualifier : KimAttributeData.createFrom(GroupAttribute.class,
                            attributes, identityManagementGroupDocument.getGroupTypeId())) {
                        if (origAttributeId != null && ObjectUtils.isNotNull(groupQualifier.getKimAttribute())
                                && StringUtils.equals(origAttributeId, groupQualifier.getKimAttribute().getId())) {
                            pndGroupQualifier = new GroupDocumentQualifier();
                            KimCommonUtilsInternal.copyProperties(pndGroupQualifier, groupQualifier);
                            pndGroupQualifier.setAttrDataId(groupQualifier.getId());
                            pndGroupQualifier.setAttrVal(groupQualifier.getAttributeValue());
                            pndGroupQualifier.setKimAttrDefnId(groupQualifier.getKimAttribute().getId());
                            pndGroupQualifier.setKimTypId(groupQualifier.getKimType().getId());
                            pndGroupQualifier.setGroupId(groupQualifier.getAssignedToId());
                            pndGroupQualifiers.add(pndGroupQualifier);
                            attributePresent = true;
                        }
                    }
                }
                if (!attributePresent) {
                    pndGroupQualifier = new GroupDocumentQualifier();
                    pndGroupQualifier.setKimAttrDefnId(origAttributeId);
                    pndGroupQualifiers.add(pndGroupQualifier);
                }
                attributePresent = false;
            }
        }
        return pndGroupQualifiers;
    }

    @CacheEvict(value = {Group.CACHE_NAME, GroupMember.CACHE_NAME, Role.CACHE_NAME}, allEntries = true)
    @Override
    public void saveGroup(final IdentityManagementGroupDocument identityManagementGroupDocument) {
        Group kimGroup = new Group();
        final Map<String, String> criteria = new HashMap<>();
        final String groupId = identityManagementGroupDocument.getGroupId();
        criteria.put("groupId", groupId);
        Group origGroup = businessObjectService.findBySinglePrimaryKey(Group.class, groupId);
        List<GroupMember> origGroupMembers = new ArrayList<>();
        if (ObjectUtils.isNull(origGroup)) {
            origGroup = new Group();
            kimGroup.setActive(true);
        } else {
            kimGroup.setVersionNumber(origGroup.getVersionNumber());
            //TODO: when a group is inactivated, inactivate the memberships of principals in that group
            //and the memberships of that group in roles
            kimGroup.setActive(identityManagementGroupDocument.isActive());
            origGroupMembers = (List<GroupMember>) businessObjectService.findMatching(GroupMember.class,
                    criteria);
        }

        kimGroup.setId(identityManagementGroupDocument.getGroupId());
        final KimType kimType = kimTypeInfoService.getKimType(identityManagementGroupDocument.getGroupTypeId());
        if (kimType == null) {
            throw new RuntimeException("Kim type not found for:" + identityManagementGroupDocument.getGroupTypeId());
        }

        kimGroup.setKimTypeId(kimType.getId());
        kimGroup.setNamespaceCode(identityManagementGroupDocument.getGroupNamespace());
        kimGroup.setName(identityManagementGroupDocument.getGroupName());
        kimGroup.setDescription(identityManagementGroupDocument.getGroupDescription());
        kimGroup.setAttributeDetails(getGroupAttributeData(identityManagementGroupDocument,
                origGroup.getAttributeDetails()));

        final List<GroupMember> newGroupMembersList = getGroupMembers(identityManagementGroupDocument, origGroupMembers);
        kimGroup.setMembers(newGroupMembersList);

        final List<String> oldIds;
        final List<String> newIds;
        oldIds = groupService.getMemberPrincipalIds(kimGroup.getId());

        kimGroup.setMembers(newGroupMembersList);

        kimGroup = businessObjectService.save(kimGroup);

        newIds = kimGroup.getMemberPrincipalIds();

        // Do an async update of the action list for the updated groups
        groupInternalService.updateForWorkgroupChange(kimGroup.getId(), oldIds, newIds);
        if (!kimGroup.isActive()) {
            // when a group is inactivated, inactivate the memberships of principals in that group
            // and the memberships of that group in roles
            roleInternalService.groupInactivated(identityManagementGroupDocument.getGroupId());
        }
    }

    protected List<GroupMember> getGroupMembers(
            final IdentityManagementGroupDocument identityManagementGroupDocument,
                                                final List<GroupMember> origGroupMembers) {
        final List<GroupMember> groupMembers = new ArrayList<>();
        GroupMember newGroupMember;
        if (CollectionUtils.isNotEmpty(identityManagementGroupDocument.getMembers())) {
            for (final GroupDocumentMember documentGroupMember : identityManagementGroupDocument.getMembers()) {
                newGroupMember = new GroupMember();
                newGroupMember.setGroupId(identityManagementGroupDocument.getGroupId());
                newGroupMember.setActiveFromDateValue(documentGroupMember.getActiveFromDate());
                newGroupMember.setActiveToDateValue(documentGroupMember.getActiveToDate());
                newGroupMember.setMemberId(documentGroupMember.getMemberId());
                newGroupMember.setTypeCode(documentGroupMember.getMemberTypeCode());
                if (ObjectUtils.isNotNull(origGroupMembers)) {
                    for (final GroupMember origGroupMemberImpl : origGroupMembers) {
                        if (StringUtils.equals(origGroupMemberImpl.getGroupId(), newGroupMember.getGroupId())
                                && StringUtils.equals(origGroupMemberImpl.getMemberId(), newGroupMember.getMemberId())
                                && !origGroupMemberImpl.isActive(dateTimeService.getCurrentTimestamp())) {
                            //TODO: verify if you want to add  && newGroupMember.isActive() condition to if...
                            newGroupMember.setMemberId(origGroupMemberImpl.getMemberId());
                        }
                        if (StringUtils.equals(origGroupMemberImpl.getGroupId(), newGroupMember.getGroupId())
                                && StringUtils.equals(origGroupMemberImpl.getMemberId(), newGroupMember.getMemberId())
                                && origGroupMemberImpl.isActive(dateTimeService.getCurrentTimestamp())) {
                            newGroupMember.setId(origGroupMemberImpl.getId());
                            newGroupMember.setVersionNumber(origGroupMemberImpl.getVersionNumber());
                        }
                    }
                }
                groupMembers.add(newGroupMember);
            }
        }
        return groupMembers;
    }

    protected List<GroupAttribute> getGroupAttributeData(
            final IdentityManagementGroupDocument identityManagementGroupDocument, final List<GroupAttribute> origAttributes) {
        final List<GroupAttribute> groupAttributeDataList = new ArrayList<>();
        GroupAttribute newGroupAttributeData;
        if (CollectionUtils.isNotEmpty(identityManagementGroupDocument.getQualifiers())) {
            for (final GroupDocumentQualifier groupQualifier : identityManagementGroupDocument.getQualifiers()) {
                if (StringUtils.isNotBlank(groupQualifier.getAttrVal())) {
                    newGroupAttributeData = new GroupAttribute();
                    newGroupAttributeData.setId(groupQualifier.getAttrDataId());
                    newGroupAttributeData.setAttributeValue(groupQualifier.getAttrVal());
                    newGroupAttributeData.setAssignedToId(groupQualifier.getGroupId());
                    newGroupAttributeData.setKimTypeId(groupQualifier.getKimTypId());
                    newGroupAttributeData.setKimAttributeId(groupQualifier.getKimAttrDefnId());
                    if (ObjectUtils.isNotNull(origAttributes)) {
                        for (final GroupAttribute origAttribute : origAttributes) {
                            if (StringUtils.equals(origAttribute.getKimAttributeId(),
                                    newGroupAttributeData.getKimAttributeId())
                                    && StringUtils.equals(newGroupAttributeData.getAssignedToId(),
                                    origAttribute.getAssignedToId())) {
                                newGroupAttributeData.setId(origAttribute.getId());
                            }
                            if (origAttribute.getId() != null
                                    && StringUtils.equals(origAttribute.getId(), newGroupAttributeData.getId())) {
                                newGroupAttributeData.setVersionNumber(origAttribute.getVersionNumber());
                            }
                        }
                    }
                    groupAttributeDataList.add(newGroupAttributeData);
                }
            }
        }
        return groupAttributeDataList;
    }

    protected Set<String> getChangedRoleResponsibilityIds(
            final IdentityManagementRoleDocument identityManagementRoleDocument,
            final List<RoleResponsibility> origRoleResponsibilities) {
        final Set<String> lRet = new HashSet<>();
        final List<String> newResp = new ArrayList<>();
        final List<String> oldResp = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(identityManagementRoleDocument.getResponsibilities())) {
            for (final KimDocumentRoleResponsibility documentRoleResponsibility :
                    identityManagementRoleDocument.getResponsibilities()) {
                newResp.add(documentRoleResponsibility.getResponsibilityId());
            }
        }
        if (ObjectUtils.isNotNull(origRoleResponsibilities)) {
            for (final RoleResponsibility roleRespBo : origRoleResponsibilities) {
                oldResp.add(roleRespBo.getResponsibilityId());
            }
        }
        lRet.addAll(newResp);
        lRet.addAll(oldResp);

        return lRet;
    }

    @Override
    public List<KimDocumentRoleMember> getRoleMembers(final Map<String, String> fieldValues) {
        final List<KimDocumentRoleMember> matchingRoleMembers = new ArrayList<>();
        //Remove since they are KNS fieldValues and not BO
        fieldValues.remove(KRADConstants.BACK_LOCATION);
        fieldValues.remove(KRADConstants.DOC_FORM_KEY);
        fieldValues.remove(KRADConstants.DOC_NUM);

        final List<RoleMember> matchingRoleMembersTemp = roleService.findRoleMembers(toQuery(fieldValues))
                .getResults();
        KimDocumentRoleMember matchingRoleMember;
        BusinessObjectBase roleMemberObject;
        RoleMember roleMember;
        if (CollectionUtils.isNotEmpty(matchingRoleMembersTemp)) {
            for (final RoleMember matchingRoleMemberTemp : matchingRoleMembersTemp) {
                roleMember = getRoleMember(matchingRoleMemberTemp.getId());
                roleMemberObject = getMember(roleMember.getType(), roleMember.getMemberId());
                matchingRoleMember = new KimDocumentRoleMember();
                KimDocumentRoleMember.copyProperties(matchingRoleMember, roleMember);
                matchingRoleMember.setMemberId(roleMember.getMemberId());
                matchingRoleMember.setRoleMemberId(roleMember.getId());
                matchingRoleMember.setMemberName(getMemberName(roleMember.getType(), roleMemberObject));
                matchingRoleMember.setMemberNamespaceCode(getMemberNamespaceCode(roleMember.getType(),
                        roleMemberObject));
                matchingRoleMember.setQualifiers(getQualifiers(roleMember.getAttributeDetails()));
                matchingRoleMembers.add(matchingRoleMember);
            }
        }
        return matchingRoleMembers;
    }

    private QueryByCriteria toQuery(final Map<String, String> fieldValues) {
        final String memberTypeCode = fieldValues.get(KIMPropertyConstants.KimMember.MEMBER_TYPE_CODE);
        final String memberName = fieldValues.get(KimConstants.KimUIConstants.MEMBER_NAME);
        final String memberNamespaceCode = fieldValues.get(KimConstants.KimUIConstants.MEMBER_NAMESPACE_CODE);

        if (StringUtils.isNotEmpty(memberName) || StringUtils.isNotEmpty(memberNamespaceCode)) {
            final String memberId = getMemberIdByName(MemberType.fromCode(memberTypeCode), memberNamespaceCode, memberName);
            if (StringUtils.isNotEmpty(memberId)) {
                fieldValues.put(KIMPropertyConstants.KimMember.MEMBER_ID, memberId);
            }
        }

        final List<Predicate> pred = new ArrayList<>();

        pred.add(PredicateUtils.convertMapToPredicate(fieldValues));
        Predicate[] predicates = new Predicate[0];
        predicates = pred.toArray(predicates);
        return QueryByCriteria.Builder.fromPredicates(predicates);
    }

    private List<KimDocumentRoleQualifier> getQualifiers(final List<RoleMemberAttributeData> attributes) {
        if (attributes == null) {
            return null;
        }
        final List<KimDocumentRoleQualifier> qualifiers = new ArrayList<>();
        KimDocumentRoleQualifier qualifier;
        if (ObjectUtils.isNotNull(attributes)) {
            for (final RoleMemberAttributeData attribute : attributes) {
                qualifier = new KimDocumentRoleQualifier();
                qualifier.setAttrDataId(attribute.getId());
                qualifier.setAttrVal(attribute.getAttributeValue());
                qualifier.setRoleMemberId(attribute.getAssignedToId());
                qualifier.setKimTypId(attribute.getKimTypeId());
                qualifier.setKimAttrDefnId(attribute.getKimAttributeId());
                qualifier.setKimAttribute(attribute.getKimAttribute());
                qualifiers.add(qualifier);
            }
        }
        return qualifiers;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDocumentHelperService(final DocumentHelperService documentHelperService) {
        this.documentHelperService = documentHelperService;
    }

    public void setGroupInternalService(final GroupInternalService groupInternalService) {
        this.groupInternalService = groupInternalService;
    }

    public void setGroupService(final GroupService groupService) {
        this.groupService = groupService;
    }

    public void setKimTypeInfoService(final KimTypeInfoService kimTypeInfoService) {
        this.kimTypeInfoService = kimTypeInfoService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setResponsibilityInternalService(final ResponsibilityInternalService responsibilityInternalService) {
        this.responsibilityInternalService = responsibilityInternalService;
    }

    public void setRoleInternalService(final RoleInternalService roleInternalService) {
        this.roleInternalService = roleInternalService;
    }

    public void setRoleService(final RoleService roleService) {
        this.roleService = roleService;
    }

    // ==== CU Customization: Backport FINP-9525 fix ====
    public void setWorkflowHelper(final UiDocumentWorkflowHelper workflowHelper) {
        this.workflowHelper = workflowHelper;
    }
    // ==== End CU Customization ====

    protected static class GroupMemberNameComparator implements Comparator<GroupDocumentMember> {
        @Override
        public int compare(final GroupDocumentMember m1, final GroupDocumentMember m2) {
            return m1.getMemberName().compareToIgnoreCase(m2.getMemberName());
        }
    }
}
