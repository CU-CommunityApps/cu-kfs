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
package org.kuali.kfs.kim.document.rule;

import edu.cornell.kfs.kim.CuKimConstants.AffiliationStatuses;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.role.RoleService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimAttributeField;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.bo.ui.PersonDocumentRole;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.framework.services.KimFrameworkServiceLocator;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.rule.event.ui.AddGroupEvent;
import org.kuali.kfs.kim.rule.event.ui.AddPersonDelegationMemberEvent;
import org.kuali.kfs.kim.rule.event.ui.AddRoleEvent;
import org.kuali.kfs.kim.rule.ui.AddGroupRule;
import org.kuali.kfs.kim.rule.ui.AddPersonDelegationMemberRule;
import org.kuali.kfs.kim.rule.ui.AddPersonDocumentRoleQualifierRule;
import org.kuali.kfs.kim.rule.ui.AddRoleRule;
import org.kuali.kfs.kns.kim.type.DataDictionaryTypeServiceHelper;
import org.kuali.kfs.kns.rules.TransactionalDocumentRuleBase;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * CU Customization:
 * 
 * Modified affiliation and employment validation to take the related CU-specific fields into consideration.
 */
public class IdentityManagementPersonDocumentRule extends TransactionalDocumentRuleBase implements AddGroupRule,
        AddRoleRule, AddPersonDocumentRoleQualifierRule, AddPersonDelegationMemberRule {

    private static final String ERROR_EXIST_PRINCIPAL_NAME = "error.exist.principalName";
    private static final String ERROR_NOT_EMPLOYMENT_AFFILIATION_TYPE = "error.not.employment.affilationType";
    private static final String ERROR_ONE_ITEM_REQUIRED = "error.one.item.required";
    private static final String ERROR_REQUIRED_CONDITIONALLY = "error.required.conditionally";
    private static final String NEW_GROUP = "newGroup";
    private static final String GROUP_ID_ERROR_PATH = NEW_GROUP + ".groupId";
    private static final String NEW_DELEGATION_MEMBER_ERROR_PATH = "document.newDelegationMember";
    public static final String ROLE_ID_ERROR_PATH = "newRole.roleId";

    private final ActiveRoleMemberHelper activeRoleMemberHelper = new ActiveRoleMemberHelper();
    private final AttributeValidationHelper attributeValidationHelper = new AttributeValidationHelper();
    private BusinessObjectService businessObjectService;
    private RoleService roleService;

    @Override
    protected boolean processCustomSaveDocumentBusinessRules(final Document document) {
        if (!(document instanceof IdentityManagementPersonDocument)) {
            return false;
        }

        final IdentityManagementPersonDocument personDoc = (IdentityManagementPersonDocument) document;

        GlobalVariables.getMessageMap().addToErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);

        getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(document,
                getMaxDictionaryValidationDepth(), true, false);
        boolean valid = validDuplicatePrincipalName(personDoc);
        valid &= validateEntityInformation(personDoc);
        valid &= validateRoleQualifier(personDoc.getRoles());
        valid &= validateDelegationMemberRoleQualifier(personDoc.getDelegationMembers());
        if (StringUtils.isNotBlank(personDoc.getPrincipalName())) {
            valid &= doesPrincipalNameExist(personDoc.getPrincipalName(), personDoc.getPrincipalId());
        }

        valid &= validActiveDatesForRole(personDoc.getRoles());
        valid &= validActiveDatesForGroup(personDoc.getGroups());
        valid &= validActiveDatesForDelegations(personDoc.getDelegationMembers());

        GlobalVariables.getMessageMap().removeFromErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);

        return valid;
    }

    private boolean validateEntityInformation(final IdentityManagementPersonDocument personDoc) {
        boolean valid = validEmployeeIDForAffiliation(personDoc);
        valid &= checkAffiliationTypeChange(personDoc);
        return valid;
    }

    private boolean validDuplicatePrincipalName(final IdentityManagementPersonDocument personDoc) {
        final Map<String, String> criteria = new HashMap<>();
        criteria.put(KIMPropertyConstants.Principal.PRINCIPAL_NAME, personDoc.getPrincipalName());
        final List<Person> people = (List<Person>) getBusinessObjectService().findMatching(Person.class, criteria);

        boolean rulePassed = true;
        if (!people.isEmpty()) {
            if (people.size() != 1 || !StringUtils.equals(people.get(0).getPrincipalId(), personDoc.getPrincipalId())) {
                GlobalVariables.getMessageMap().putError("document.principalName",
                        KFSKeyConstants.ERROR_DUPLICATE_ENTRY, "Principal Name");
                rulePassed = false;
            }
        }
        return rulePassed;
    }

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(final Document document) {
        super.processCustomRouteDocumentBusinessRules(document);
        final IdentityManagementPersonDocument personDoc = (IdentityManagementPersonDocument) document;
        GlobalVariables.getMessageMap().addToErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        boolean valid = validateAffiliationAndName(personDoc);
        valid &= checkAffiliationEithOneEMpInfo(personDoc);
        GlobalVariables.getMessageMap().removeFromErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);

        return valid;
    }

    /*
     * CU Customization: Allow for specifying Employee ID if the Person has a non-default affiliation supporting it.
     */
    private boolean checkAffiliationTypeChange(final IdentityManagementPersonDocument personDoc) {
        final String affiliationTypeCode = personDoc.getAffiliationTypeCode();

        if (StringUtils.isBlank(affiliationTypeCode)
                || StringUtils.isBlank(personDoc.getAffiliateAffiliation())
                || StringUtils.isBlank(personDoc.getFacultyAffiliation())
                || StringUtils.isBlank(personDoc.getStaffAffiliation())) {
            return true;
        }

        final EntityAffiliationType entityAffiliationType =
                getBusinessObjectService().findBySinglePrimaryKey(EntityAffiliationType.class, affiliationTypeCode);
        if (!entityAffiliationType.isEmploymentAffiliationType()
            && StringUtils.isNotBlank(personDoc.getEmployeeId())
            && StringUtils.equalsIgnoreCase(personDoc.getAffiliateAffiliation(), AffiliationStatuses.NONEXISTENT)
            && StringUtils.equalsIgnoreCase(personDoc.getFacultyAffiliation(), AffiliationStatuses.NONEXISTENT)
            && StringUtils.equalsIgnoreCase(personDoc.getStaffAffiliation(), AffiliationStatuses.NONEXISTENT)
        ) {
            GlobalVariables.getMessageMap().putError(
                    "affiliationTypeCode",
                    ERROR_NOT_EMPLOYMENT_AFFILIATION_TYPE,
                    entityAffiliationType.getName()
            );
            return false;
        }
        return true;
    }

    /*
     * CU Customization: Require an Employee ID if the Person has a non-default affiliation supporting it.
     */
    private boolean validEmployeeIDForAffiliation(final IdentityManagementPersonDocument personDoc) {
        final String affiliationTypeCode = personDoc.getAffiliationTypeCode();

        if (StringUtils.isBlank(affiliationTypeCode)
                || StringUtils.isBlank(personDoc.getAffiliateAffiliation())
                || StringUtils.isBlank(personDoc.getFacultyAffiliation())
                || StringUtils.isBlank(personDoc.getStaffAffiliation())) {
            return true;
        }

        final EntityAffiliationType entityAffiliationType =
                getBusinessObjectService().findBySinglePrimaryKey(EntityAffiliationType.class, affiliationTypeCode);
        if ((entityAffiliationType.isEmploymentAffiliationType()
                || !StringUtils.equalsIgnoreCase(personDoc.getAffiliateAffiliation(), AffiliationStatuses.NONEXISTENT)
                || !StringUtils.equalsIgnoreCase(personDoc.getFacultyAffiliation(), AffiliationStatuses.NONEXISTENT)
                || !StringUtils.equalsIgnoreCase(personDoc.getStaffAffiliation(), AffiliationStatuses.NONEXISTENT))
            && StringUtils.isBlank(personDoc.getEmployeeId())
        ) {
            GlobalVariables.getMessageMap().putError(
                    "employeeId",
                    ERROR_REQUIRED_CONDITIONALLY,
                    "Employee ID",
                    "an employee"
            );
            return false;
        }
        return true;
    }

    /*
     * CU Customization: Require an Employee ID if the Person has a non-default affiliation supporting it.
     */
    private boolean checkAffiliationEithOneEMpInfo(final IdentityManagementPersonDocument personDoc) {
        final String affiliationTypeCode = personDoc.getAffiliationTypeCode();

        if (StringUtils.isBlank(affiliationTypeCode)
                || StringUtils.isBlank(personDoc.getAffiliateAffiliation())
                || StringUtils.isBlank(personDoc.getFacultyAffiliation())
                || StringUtils.isBlank(personDoc.getStaffAffiliation())) {
            return true;
        }

        final EntityAffiliationType entityAffiliationType =
                getBusinessObjectService().findBySinglePrimaryKey(EntityAffiliationType.class, affiliationTypeCode);
        if ((entityAffiliationType.isEmploymentAffiliationType()
                || !StringUtils.equalsIgnoreCase(personDoc.getAffiliateAffiliation(), AffiliationStatuses.NONEXISTENT)
                || !StringUtils.equalsIgnoreCase(personDoc.getFacultyAffiliation(), AffiliationStatuses.NONEXISTENT)
                || !StringUtils.equalsIgnoreCase(personDoc.getStaffAffiliation(), AffiliationStatuses.NONEXISTENT))
            && employmentInfoIsMissing(personDoc)
        ) {
            GlobalVariables.getMessageMap().putError(
                    "affiliationTypeCode",
                    ERROR_ONE_ITEM_REQUIRED,
                    "Employment Information"
            );
            return false;
        }
        return true;
    }

    private static boolean employmentInfoIsMissing(final IdentityManagementPersonDocument personDoc) {
        return StringUtils.isBlank(personDoc.getEmployeeId())
               || StringUtils.isBlank(personDoc.getEmployeeStatusCode())
               || StringUtils.isBlank(personDoc.getEmployeeTypeCode())
               || StringUtils.isBlank(personDoc.getPrimaryDepartmentCode());
    }

    /*
     * Verify at least one affiliation and one default name
     * 
     * CU Customization: Also verify that the CU-specific affiliation fields have been populated.
     */
    private static boolean validateAffiliationAndName(final IdentityManagementPersonDocument personDoc) {
        boolean valid = true;
        if (StringUtils.isBlank(personDoc.getAffiliationTypeCode())) {
            GlobalVariables.getMessageMap().putError("affiliationTypeCode", KFSKeyConstants.ERROR_REQUIRED,
                    "Affiliation Type");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getCampusCode())) {
            GlobalVariables.getMessageMap().putError("campusCode", KFSKeyConstants.ERROR_REQUIRED,
                    "Campus Code");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getFirstName())) {
            GlobalVariables.getMessageMap().putError("firstName", KFSKeyConstants.ERROR_REQUIRED,
                    "First Name");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getLastName())) {
            GlobalVariables.getMessageMap().putError("lastName", KFSKeyConstants.ERROR_REQUIRED,
                    "Last Name");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getAcademicAffiliation())) {
            GlobalVariables.getMessageMap().putError("academicAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Academic");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getAffiliateAffiliation())) {
            GlobalVariables.getMessageMap().putError("affiliateAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Affiliate");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getAlumniAffiliation())) {
            GlobalVariables.getMessageMap().putError("alumniAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Alumni");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getExceptionAffiliation())) {
            GlobalVariables.getMessageMap().putError("exceptionAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Exception");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getFacultyAffiliation())) {
            GlobalVariables.getMessageMap().putError("facultyAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Faculty");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getStaffAffiliation())) {
            GlobalVariables.getMessageMap().putError("staffAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Staff");
            valid = false;
        }
        if (StringUtils.isBlank(personDoc.getStudentAffiliation())) {
            GlobalVariables.getMessageMap().putError("studentAffiliation", KFSKeyConstants.ERROR_REQUIRED,
                    "Student");
            valid = false;
        }
        return valid;
    }

    private boolean doesPrincipalNameExist(final String principalName, final String principalId) {
        final Person person = getPersonService().getPersonByPrincipalName(principalName);
        if (person != null && (StringUtils.isBlank(principalId) || !person.getPrincipalId().equals(principalId))) {
            GlobalVariables.getMessageMap().putError(KIMPropertyConstants.Principal.PRINCIPAL_NAME,
                    ERROR_EXIST_PRINCIPAL_NAME, principalName);
            return false;
        }
        return true;
    }

    private boolean validateRoleQualifier(final List<? extends PersonDocumentRole> roles) {
        final List<AttributeError> validationErrors = new ArrayList<>();
        GlobalVariables.getMessageMap().removeFromErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        int i = 0;
        for (final PersonDocumentRole role : roles) {
            final KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(role.getKimRoleType());
            if (CollectionUtils.isEmpty(role.getRolePrncpls()) && !role.getDefinitions().isEmpty()) {
                final KimType kimTypeInfo = KimApiServiceLocator.getKimTypeInfoService().getKimType(
                        role.getKimRoleType().getId());
                final Map<String, String> blankQualifiers = attributeValidationHelper.getBlankValueQualifiersMap(
                        kimTypeInfo.getAttributeDefinitions());
                final List<AttributeError> localErrors = kimTypeService.validateAttributes(
                        role.getKimRoleType().getId(), blankQualifiers);
                if (localErrors != null && !localErrors.isEmpty()) {
                    GlobalVariables.getMessageMap().putError("document.roles[" + i +
                                    "].newRolePrncpl.qualifiers[0].attrVal",
                            ERROR_ONE_ITEM_REQUIRED, "Role Qualifier");
                    return false;
                }
            }

            final List<KimAttributeField> attributeDefinitions = role.getDefinitions();
            final Set<String> uniqueQualifierAttributes = findUniqueQualificationAttributes(role, attributeDefinitions);

            if (kimTypeService != null) {
                int j = 0;

                for (final KimDocumentRoleMember rolePrincipal :
                        activeRoleMemberHelper.getActiveRoleMembers(role.getRolePrncpls())) {
                    final List<AttributeError> localErrors = kimTypeService.validateAttributes(
                            role.getKimRoleType().getId(),
                            attributeValidationHelper.convertQualifiersToMap(rolePrincipal.getQualifiers()));
                    validationErrors.addAll(attributeValidationHelper.convertErrors(
                            "roles[" + i + "].rolePrncpls[" + j + "]",
                            attributeValidationHelper.convertQualifiersToAttrIdxMap(rolePrincipal.getQualifiers()),
                            localErrors));

                    if (!uniqueQualifierAttributes.isEmpty()) {
                        validateUniquePersonRoleQualifiersUniqueForMembership(role, rolePrincipal, j,
                                uniqueQualifierAttributes, i, validationErrors);
                    }

                    j++;
                }
            }
            i++;
        }
        GlobalVariables.getMessageMap().addToErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        if (validationErrors.isEmpty()) {
            return true;
        } else {
            attributeValidationHelper.moveValidationErrorsToErrorMap(validationErrors);
            return false;
        }
    }

    /**
     * Checks all the qualifiers for the given membership, so that all qualifiers which should be unique are guaranteed
     * to be unique
     *
     * @param roleIndex              the index of the role on the document (for error reporting purposes)
     * @param membershipToCheckIndex the index of the person's membership in the role (for error reporting purposes)
     */
    private static void validateUniquePersonRoleQualifiersUniqueForMembership(
            final PersonDocumentRole role,
            final KimDocumentRoleMember membershipToCheck,
            final int membershipToCheckIndex,
            final Set<String> uniqueQualifierAttributes,
            final int roleIndex,
            final List<? super AttributeError> validationErrors
    ) {
        int count = 0;

        for (final KimDocumentRoleMember membership : role.getRolePrncpls()) {
            if (sameMembershipQualifications(membershipToCheck, membership, uniqueQualifierAttributes)) {
                if (count == 0) {
                    count += 1;
                } else {
                    count += 1;

                    int qualifierCount = 0;

                    for (final KimDocumentRoleQualifier qualifier : membership.getQualifiers()) {
                        if (qualifier != null && uniqueQualifierAttributes.contains(qualifier.getKimAttrDefnId())) {
                            validationErrors.add(AttributeError.Builder.create(
                                    "document.roles[" + roleIndex + "].rolePrncpls[" + membershipToCheckIndex
                                    + "].qualifiers[" + qualifierCount + "].attrVal",
                                    KFSKeyConstants.ERROR_DOCUMENT_IDENTITY_MANAGEMENT_PERSON_QUALIFIER_VALUE_NOT_UNIQUE
                                    + ":" + qualifier.getKimAttribute().getAttributeName() + ";"
                                    + qualifier.getAttrVal()
                            ).build());
                        }
                        qualifierCount += 1;
                    }
                }
            }
        }
    }

    /**
     * Determines if two separate memberships have the same qualifications
     *
     * @param membershipA               the first membership to check
     * @param membershipB               the second membership to check
     * @param uniqueQualifierAttributes the set of qualifier attributes which need to be unique
     * @return true if equal, false if otherwise
     */
    private static boolean sameMembershipQualifications(
            final KimDocumentRoleMember membershipA,
            final KimDocumentRoleMember membershipB,
            final Set<String> uniqueQualifierAttributes
    ) {
        boolean equalSoFar = true;
        for (final String uniqueQualifierAttributeDefinitionId : uniqueQualifierAttributes) {
            final KimDocumentRoleQualifier qualifierA = membershipA.getQualifier(uniqueQualifierAttributeDefinitionId);
            final KimDocumentRoleQualifier qualifierB = membershipB.getQualifier(uniqueQualifierAttributeDefinitionId);

            if (qualifierA != null && qualifierB != null) {
                equalSoFar &= qualifierA.getAttrVal() == null && qualifierB.getAttrVal() == null
                              || qualifierA.getAttrVal() == null || qualifierA.getAttrVal()
                                      .equals(qualifierB.getAttrVal());
            }
        }
        return equalSoFar;
    }

    /**
     * Finds the set of unique qualification attributes for the given role
     *
     * @param role                 the role associated with this person
     * @param attributeDefinitions the Map of attribute definitions where we can find out if a KimAttribute is
     *                             supposed to be unique
     * @return a Set of attribute definition ids for qualifications which are supposed to be unique
     */
    private static Set<String> findUniqueQualificationAttributes(
            final PersonDocumentRole role,
            final List<KimAttributeField> attributeDefinitions
    ) {
        final Set<String> uniqueQualifications = new HashSet<>();

        if (role.getRolePrncpls() != null && role.getRolePrncpls().size() > 1) {
            final KimDocumentRoleMember membership = role.getRolePrncpls().get(0);
            for (final KimDocumentRoleQualifier qualifier : membership.getQualifiers()) {
                if (qualifier != null && qualifier.getKimAttribute() != null
                        && StringUtils.isNotBlank(qualifier.getKimAttribute().getAttributeName())) {
                    final KimAttributeField relatedDefinition = DataDictionaryTypeServiceHelper.findAttributeField(
                            qualifier.getKimAttribute().getAttributeName(), attributeDefinitions);

                    if (relatedDefinition != null && relatedDefinition.isUnique()) {
                        uniqueQualifications.add(qualifier.getKimAttrDefnId());
                    }
                }
            }
        }

        return uniqueQualifications;
    }

    private static boolean validActiveDatesForRole(final List<? extends PersonDocumentRole> roles) {
        boolean valid = true;
        int i = 0;
        for (final PersonDocumentRole role : roles) {
            int j = 0;
            for (final KimDocumentRoleMember principal : role.getRolePrncpls()) {
                valid &= validateActiveDate("roles[" + i + "].rolePrncpls[" + j + "].activeToDate",
                        principal.getActiveFromDate(), principal.getActiveToDate());
                j++;
            }
            i++;
        }
        return valid;
    }

    private static boolean validActiveDatesForGroup(final List<? extends PersonDocumentGroup> groups) {
        boolean valid = true;
        int i = 0;
        for (final PersonDocumentGroup group : groups) {
            valid &= validateActiveDate("groups[" + i + "].activeToDate", group.getActiveFromDate(),
                    group.getActiveToDate());
            i++;
        }
        return valid;
    }

    private static boolean validActiveDatesForDelegations(
            final List<? extends RoleDocumentDelegationMember> delegationMembers
    ) {
        boolean valid = true;
        int i = 0;
        for (final RoleDocumentDelegationMember delegationMember : delegationMembers) {
            valid &= validateActiveDate("delegationMembers[" + i + "].activeToDate",
                    delegationMember.getActiveFromDate(), delegationMember.getActiveToDate());
            i++;
        }
        return valid;
    }

    private static boolean validateActiveDate(
            final String errorPath, final Timestamp activeFromDate, final Timestamp activeToDate
    ) {
        // TODO : do not have detail bus rule yet, so just check this for now.
        boolean valid = true;
        if (activeFromDate != null && activeToDate != null && activeToDate.before(activeFromDate)) {
            GlobalVariables.getMessageMap().putError(errorPath, KFSKeyConstants.ERROR_ACTIVE_TO_DATE_BEFORE_FROM_DATE);
            valid = false;
        }
        return valid;
    }

    @Override
    public boolean processAddGroup(final AddGroupEvent addGroupEvent) {
        final IdentityManagementPersonDocument document =
                (IdentityManagementPersonDocument) addGroupEvent.getDocument();
        final PersonDocumentGroup newGroup = addGroupEvent.getGroup();
        boolean rulePassed = validAssignGroup(document, newGroup);

        if (StringUtils.isBlank(newGroup.getGroupId())) {
            rulePassed = false;
            GlobalVariables.getMessageMap().putError(GROUP_ID_ERROR_PATH, KFSKeyConstants.ERROR_EMPTY_ENTRY, "Group");
        } else {
            for (final PersonDocumentGroup group : document.getGroups()) {
                if (group.getGroupId().equals(newGroup.getGroupId())) {
                    rulePassed = false;
                    GlobalVariables.getMessageMap()
                            .putError(GROUP_ID_ERROR_PATH, KFSKeyConstants.ERROR_DUPLICATE_ENTRY, "Group");
                }
            }
        }
        return rulePassed;
    }

    private static boolean validAssignGroup(
            final IdentityManagementPersonDocument document,
            final PersonDocumentGroup newGroup
    ) {
        boolean rulePassed = true;
        final Map<String, String> additionalPermissionDetails = Map.of(
            KimConstants.AttributeConstants.NAMESPACE_CODE, newGroup.getNamespaceCode(),
            KimConstants.AttributeConstants.GROUP_NAME, newGroup.getGroupName()
        );
        if (!getDocumentDictionaryService().getDocumentAuthorizer(document).isAuthorizedByTemplate(
                document,
                KimConstants.NAMESPACE_CODE,
                KimConstants.PermissionTemplateNames.POPULATE_GROUP,
                GlobalVariables.getUserSession().getPrincipalId(),
                additionalPermissionDetails,
                null
        )) {
            GlobalVariables.getMessageMap().putError(
                    GROUP_ID_ERROR_PATH,
                    KFSKeyConstants.ERROR_ASSIGN_GROUP,
                    newGroup.getNamespaceCode(),
                    newGroup.getGroupName()
            );
            rulePassed = false;
        }
        return rulePassed;
    }

    @Override
    public boolean processAddRole(final AddRoleEvent addRoleEvent) {
        final PersonDocumentRole newRole = addRoleEvent.getRole();
        final IdentityManagementPersonDocument document = (IdentityManagementPersonDocument) addRoleEvent.getDocument();
        boolean rulePassed = true;

        if (newRole == null || StringUtils.isBlank(newRole.getRoleId())) {
            rulePassed = false;
            GlobalVariables.getMessageMap().putError(ROLE_ID_ERROR_PATH, KFSKeyConstants.ERROR_EMPTY_ENTRY, "Role");
        } else {
            for (final PersonDocumentRole role : document.getRoles()) {
                if (role.getRoleId().equals(newRole.getRoleId())) {
                    rulePassed = false;
                    GlobalVariables.getMessageMap()
                            .putError(ROLE_ID_ERROR_PATH, KFSKeyConstants.ERROR_DUPLICATE_ENTRY, "Role");
                }
            }
        }

        return rulePassed;
    }

    @Override
    public boolean processAddPersonDelegationMember(
            final AddPersonDelegationMemberEvent addPersonDelegationMemberEvent
    ) {
        final RoleDocumentDelegationMember newDelegationMember = addPersonDelegationMemberEvent.getDelegationMember();
        if (newDelegationMember == null) {
            GlobalVariables.getMessageMap()
                    .putError(NEW_DELEGATION_MEMBER_ERROR_PATH, KFSKeyConstants.ERROR_EMPTY_ENTRY, "Delegation Member");
            return false;
        }
        if (StringUtils.isBlank(newDelegationMember.getRoleMemberId())) {
            GlobalVariables.getMessageMap()
                    .putError(NEW_DELEGATION_MEMBER_ERROR_PATH, KFSKeyConstants.ERROR_EMPTY_ENTRY, "Role Member");
            return false;
        }
        return true;
    }

    @Override
    public boolean processAddPersonDocumentRoleQualifier(
            final IdentityManagementPersonDocument document,
            final PersonDocumentRole role,
            final KimDocumentRoleMember kimDocumentRoleMember,
            final int selectedRoleIdx
    ) {
        final boolean dateValidationSuccess =
                validateActiveDate("document.roles[" + selectedRoleIdx + "].newRolePrncpl.activeFromDate",
                        kimDocumentRoleMember.getActiveFromDate(),
                        kimDocumentRoleMember.getActiveToDate()
                );
        final String errorPath = "roles[" + selectedRoleIdx + "].newRolePrncpl";
        final List<AttributeError> validationErrors = new ArrayList<>();
        GlobalVariables.getMessageMap().removeFromErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        final KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(role.getKimRoleType());

        List<AttributeError> errorsAttributesAgainstExisting;
        final Map<String, String> newMemberQualifiers = attributeValidationHelper.convertQualifiersToMap(
                kimDocumentRoleMember.getQualifiers());
        Map<String, String> oldMemberQualifiers;
        for (final KimDocumentRoleMember member : role.getRolePrncpls()) {
            oldMemberQualifiers = member.getQualifierAsMap();
            errorsAttributesAgainstExisting = kimTypeService.validateUniqueAttributes(
                    role.getKimRoleType().getId(), newMemberQualifiers, oldMemberQualifiers);
            validationErrors.addAll(attributeValidationHelper.convertErrors(errorPath,
                    attributeValidationHelper.convertQualifiersToAttrIdxMap(kimDocumentRoleMember.getQualifiers()),
                    errorsAttributesAgainstExisting));
        }

        if (kimTypeService != null) {
            final List<AttributeError> localErrors = kimTypeService.validateAttributes(
                    role.getKimRoleType().getId(), newMemberQualifiers);
            validationErrors.addAll(attributeValidationHelper.convertErrors(errorPath,
                    attributeValidationHelper.convertQualifiersToAttrIdxMap(kimDocumentRoleMember.getQualifiers()),
                    localErrors));
        }

        GlobalVariables.getMessageMap().addToErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        final boolean rulePassed;
        if (validationErrors.isEmpty()) {
            rulePassed = dateValidationSuccess;
        } else {
            attributeValidationHelper.moveValidationErrorsToErrorMap(validationErrors);
            rulePassed = false;
        }
        return rulePassed;
    }

    boolean validateDelegationMemberRoleQualifier(
            final List<? extends RoleDocumentDelegationMember> delegationMembers
    ) {
        final List<AttributeError> validationErrors = new ArrayList<>();
        final boolean valid;
        int memberCounter = 0;
        GlobalVariables.getMessageMap().removeFromErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        for (final RoleDocumentDelegationMember delegationMember : delegationMembers) {
            if (delegationMember.isActive()) {
                validateDelegationMember(validationErrors, memberCounter, delegationMember);
            }
            memberCounter++;
        }
        GlobalVariables.getMessageMap().addToErrorPath(KRADConstants.DOCUMENT_PROPERTY_NAME);
        if (validationErrors.isEmpty()) {
            valid = true;
        } else {
            attributeValidationHelper.moveValidationErrorsToErrorMap(validationErrors);
            valid = false;
        }
        return valid;
    }

    private void validateDelegationMember(
            final List<? super AttributeError> validationErrors,
            final int memberCounter,
            final RoleDocumentDelegationMember delegationMember
    ) {
        final KimType kimType = delegationMember.getMemberRole().getKimRoleType();
        final KimTypeService kimTypeService = KimFrameworkServiceLocator.getKimTypeService(kimType);
        final String errorPath = "delegationMembers[" + memberCounter + "]";
        final Map<String, String> mapToValidate = attributeValidationHelper.convertQualifiersToMap(
                delegationMember.getQualifiers());
        final List<AttributeError> errorsTemp = kimTypeService.validateAttributes(kimType.getId(),
                mapToValidate);
        validationErrors.addAll(attributeValidationHelper.convertErrors(errorPath,
                attributeValidationHelper.convertQualifiersToAttrIdxMap(delegationMember.getQualifiers()),
                errorsTemp));

        final List<RoleMember> roleMembers = getRoleService().findRoleMembers(QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KimConstants.PrimaryKeyConstants.ID,
                        delegationMember.getRoleMemberId()))).getResults();
        if (roleMembers.isEmpty()) {
            GlobalVariables.getMessageMap().putError("document." + errorPath,
                    KFSKeyConstants.ERROR_DELEGATE_ROLE_MEMBER_ASSOCIATION);
        } else {
            final List<AttributeError> unmodifiableAttributesErrorsTemp =
                    kimTypeService.validateUnmodifiableAttributes(kimType.getId(),
                    roleMembers.get(0).getAttributes(),
                    mapToValidate);
            validationErrors.addAll(attributeValidationHelper.convertErrors(errorPath,
                    attributeValidationHelper.convertQualifiersToAttrIdxMap(delegationMember.getQualifiers()),
                    unmodifiableAttributesErrorsTemp));
        }
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    protected RoleService getRoleService() {
        if (roleService == null) {
            roleService = KimApiServiceLocator.getRoleService();
        }
        return roleService;
    }
}
