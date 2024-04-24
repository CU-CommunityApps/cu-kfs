package edu.cornell.kfs.kim.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ojb.broker.core.proxy.CollectionProxyDefaultImpl;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.document.IdentityManagementRoleDocument;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleInternalService;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleMemberAttributeData;
import org.kuali.kfs.kim.impl.role.RoleResponsibilityAction;
import org.kuali.kfs.kim.service.impl.UiDocumentServiceImpl;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;

import edu.cornell.kfs.kim.bo.ui.PersonDocumentAffiliation;
import edu.cornell.kfs.kim.document.IdentityManagementPersonDocumentExtension;
import edu.cornell.kfs.kim.impl.identity.PersonAffiliation;
import edu.cornell.kfs.kim.impl.identity.PersonExtension;

public class CuUiDocumentServiceImpl extends UiDocumentServiceImpl {

    private PermissionService permissionService;
    private PersonService personService;
    private RoleInternalService roleInternalService;

    /**
     * Overridden to allow for loading unmodified role members even when there are no delegations,
     * and to create a copy of the RoleBo's members list (to prevent potential member auto-deletion).
     */
    @Override
    public void setMembersInDocument(final IdentityManagementRoleDocument identityManagementRoleDocument) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KimConstants.PrimaryKeyConstants.ROLE_ID, identityManagementRoleDocument.getRoleId());
        final Role roleBo = businessObjectService.findByPrimaryKey(Role.class, criteria);
        if (ObjectUtils.isNotNull(roleBo)) {
            final List<RoleMember> members = new ArrayList<>(roleBo.getMembers());
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

    /**
     * Overridden to also iterate over the role's members and forcibly load their lists of responsibility actions.
     * This is needed to fix a bug that sometimes occurs when an object contains a lazy-loaded OJB collection proxy
     * but the code forcibly replaces that list with a different type. Such situations may trigger a rare exception
     * when OJB tries to perform a bulk pre-fetch of that specific collection property across multiple objects.
     */
    @Override
    protected void updateRoleMembers(
            final String roleId, final List<KimDocumentRoleMember> modifiedRoleMembers,
            final List<RoleMember> roleMembers) {
        if (CollectionUtils.isNotEmpty(modifiedRoleMembers) && CollectionUtils.isNotEmpty(roleMembers)) {
            for (final RoleMember roleMember : roleMembers) {
                final List<RoleResponsibilityAction> rspActions = roleMember.getRoleRspActions();
                if (rspActions instanceof CollectionProxyDefaultImpl) {
                    ((CollectionProxyDefaultImpl) rspActions).getData();
                }
            }
        }
        super.updateRoleMembers(roleId, modifiedRoleMembers, roleMembers);
    }

    /**
     * Overridden to backport the FINP-9360 changes.
     * This override (and the setPermissionService() override) can be removed for our 2023-03-08 financials upgrade.
     */
    @Override
    public boolean canModifyPerson(final String currentUserPrincipalId, final String toModifyPrincipalId) {
        return permissionService.isAuthorized(
                currentUserPrincipalId,
                KimConstants.NAMESPACE_CODE,
                KimConstants.PermissionNames.MODIFY_PERSON,
                Collections.singletonMap(KimConstants.AttributeConstants.PRINCIPAL_ID, currentUserPrincipalId)
        );
    }

    /**
     * Overridden to also handle CU-specific Person fields when adding/updating a Person.
     */
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
            person.setExtension(new PersonExtension());
            person.setEntityId(identityManagementPersonDocument.getEntityId());
            person.setEntityTypeCode(KimConstants.EntityTypes.PERSON);
            person.setPrincipalId(identityManagementPersonDocument.getPrincipalId());
            person.getPersonExtension().setPrincipalId(identityManagementPersonDocument.getPrincipalId());
            person.setActive(true);
        }
        person.setPrincipalName(identityManagementPersonDocument.getPrincipalName());
        setupAffiliation(identityManagementPersonDocument, person);
        setupName(identityManagementPersonDocument, person);
        person.setPhoneNumber(identityManagementPersonDocument.getPhoneNumber());
        person.setEmailAddress(identityManagementPersonDocument.getEmailAddress());
        setupAddress(identityManagementPersonDocument, person);
        setupPrivacyPreferences(identityManagementPersonDocument, person);

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

    /*
     * Copied this private method from the superclass.
     */
    private void setupName(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        person.setFirstName(identityManagementPersonDocument.getFirstName());
        person.setLastName(identityManagementPersonDocument.getLastName());
        person.setMiddleName(identityManagementPersonDocument.getMiddleName());
    }

    /*
     * Copied this private method from the superclass, and modified it to also configure CU-specific affiliation data.
     */
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
        for (final PersonDocumentAffiliation docAffiliation :
                identityManagementPersonDocument.getPersonDocumentExtension().getAffiliations()) {
            final PersonAffiliation affiliation = findOrAddPersonAffiliation(person,
                    docAffiliation.getAffiliationTypeCode());
            affiliation.setAffiliationStatus(docAffiliation.getAffiliationStatus());
            affiliation.setPrimary(docAffiliation.isPrimary());
        }
    }

    private PersonAffiliation findOrAddPersonAffiliation(
            final Person person,
            final String affiliationTypeCode
    ) {
        final List<PersonAffiliation> affiliations = person.getPersonExtension().getAffiliations();
        final Optional<PersonAffiliation> existingAffiliation = affiliations.stream()
                .filter(personAffil -> StringUtils.equals(personAffil.getAffiliationTypeCode(), affiliationTypeCode))
                .findFirst();
        if (existingAffiliation.isPresent()) {
            return existingAffiliation.get();
        } else {
            final PersonAffiliation affiliation = new PersonAffiliation();
            affiliation.setPrincipalId(person.getPrincipalId());
            affiliation.setAffiliationTypeCode(affiliationTypeCode);
            affiliations.add(affiliation);
            return affiliation;
        }
    }

    /**
     * Overridden to also populate CU-specific Person address fields.
     */
    @Override
    protected void setupAddress(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        super.setupAddress(identityManagementPersonDocument, person);
        final PersonExtension personExtension = person.getPersonExtension();
        final IdentityManagementPersonDocumentExtension docExtension =
                identityManagementPersonDocument.getPersonDocumentExtension();
        personExtension.setAltAddressTypeCode(docExtension.getAltAddressTypeCode());
        personExtension.setAltAddressLine1(docExtension.getAltAddressLine1());
        personExtension.setAltAddressLine2(docExtension.getAltAddressLine2());
        personExtension.setAltAddressLine3(docExtension.getAltAddressLine3());
        personExtension.setAltAddressCity(docExtension.getAltAddressCity());
        personExtension.setAltAddressStateProvinceCode(docExtension.getAltAddressStateProvinceCode());
        personExtension.setAltAddressPostalCode(docExtension.getAltAddressPostalCode());
        personExtension.setAltAddressCountryCode(docExtension.getAltAddressCountryCode());
    }

    private void setupPrivacyPreferences(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final Person person
    ) {
        final PersonExtension personExtension = person.getPersonExtension();
        final IdentityManagementPersonDocumentExtension docExtension =
                identityManagementPersonDocument.getPersonDocumentExtension();
        personExtension.setSuppressName(docExtension.isSuppressName());
        personExtension.setSuppressEmail(docExtension.isSuppressEmail());
        personExtension.setSuppressPhone(docExtension.isSuppressPhone());
        personExtension.setSuppressPersonal(docExtension.isSuppressPersonal());
    }

    /**
     * Overridden to also set up CU-specific Person Document fields.
     */
    @Override
    public void loadPersonDoc(
            final IdentityManagementPersonDocument identityManagementPersonDocument,
            final String principalId
    ) {
        super.loadPersonDoc(identityManagementPersonDocument, principalId);
        identityManagementPersonDocument.initializeDocumentExtension();
        final Person person = personService.getPerson(principalId);

        if (ObjectUtils.isNull(person)) {
            throw new RuntimeException("Person does not exist for principal id:" + principalId);
        }

        final PersonExtension personExtension = person.getPersonExtension();
        final IdentityManagementPersonDocumentExtension docExtension =
                identityManagementPersonDocument.getPersonDocumentExtension();

        docExtension.setAltAddressTypeCode(personExtension.getAltAddressTypeCode());
        docExtension.setAltAddressLine1(personExtension.getAltAddressLine1());
        docExtension.setAltAddressLine2(personExtension.getAltAddressLine2());
        docExtension.setAltAddressLine3(personExtension.getAltAddressLine3());
        docExtension.setAltAddressCity(personExtension.getAltAddressCity());
        docExtension.setAltAddressStateProvinceCode(personExtension.getAltAddressStateProvinceCode());
        docExtension.setAltAddressPostalCode(personExtension.getAltAddressPostalCode());
        docExtension.setAltAddressCountryCode(personExtension.getAltAddressCountryCode());
        docExtension.setSuppressName(personExtension.isSuppressName());
        docExtension.setSuppressEmail(personExtension.isSuppressEmail());
        docExtension.setSuppressPhone(personExtension.isSuppressPhone());
        docExtension.setSuppressPersonal(personExtension.isSuppressPersonal());

        loadPersonDocAffiliations(docExtension, personExtension);
    }

    private void loadPersonDocAffiliations(
            final IdentityManagementPersonDocumentExtension docExtension,
            final PersonExtension personExtension
    ) {
        List<PersonDocumentAffiliation> docAffiliations = docExtension.getAffiliations();
        for (PersonAffiliation personAffiliation : personExtension.getAffiliations()) {
            PersonDocumentAffiliation docAffiliation = new PersonDocumentAffiliation();
            docAffiliation.setDocumentNumber(docExtension.getDocumentNumber());
            docAffiliation.setAffiliationTypeCode(personAffiliation.getAffiliationTypeCode());
            docAffiliation.setAffiliationStatus(personAffiliation.getAffiliationStatus());
            docAffiliation.setPrimary(personAffiliation.isPrimary());
            docAffiliations.add(docAffiliation);
        }
    }

    @Override
    public void setPermissionService(final PermissionService permissionService) {
        super.setPermissionService(permissionService);
        this.permissionService = permissionService;
    }

    @Override
    public void setPersonService(PersonService personService) {
        super.setPersonService(personService);
        this.personService = personService;
    }

    @Override
    public void setRoleInternalService(RoleInternalService roleInternalService) {
        super.setRoleInternalService(roleInternalService);
        this.roleInternalService = roleInternalService;
    }

}
