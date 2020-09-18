/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.kim.web.struts.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.identity.entity.EntityDefault;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleResponsibility;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.api.type.KimAttributeField;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleMember;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleQualifier;
import org.kuali.kfs.kim.bo.ui.KimDocumentRoleResponsibilityAction;
import org.kuali.kfs.kim.bo.ui.PersonDocumentAddress;
import org.kuali.kfs.kim.bo.ui.PersonDocumentAffiliation;
import org.kuali.kfs.kim.bo.ui.PersonDocumentCitizenship;
import org.kuali.kfs.kim.bo.ui.PersonDocumentEmail;
import org.kuali.kfs.kim.bo.ui.PersonDocumentEmploymentInfo;
import org.kuali.kfs.kim.bo.ui.PersonDocumentGroup;
import org.kuali.kfs.kim.bo.ui.PersonDocumentName;
import org.kuali.kfs.kim.bo.ui.PersonDocumentPhone;
import org.kuali.kfs.kim.bo.ui.PersonDocumentRole;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegation;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMember;
import org.kuali.kfs.kim.bo.ui.RoleDocumentDelegationMemberQualifier;
import org.kuali.kfs.kim.document.IdentityManagementPersonDocument;
import org.kuali.kfs.kim.document.rule.AttributeValidationHelper;
import org.kuali.kfs.kim.framework.type.KimTypeService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityInternalService;
import org.kuali.kfs.kim.impl.role.RoleBo;
import org.kuali.kfs.kim.impl.role.RoleMemberBo;
import org.kuali.kfs.kim.impl.services.KimImplServiceLocator;
import org.kuali.kfs.kim.impl.type.KimTypeAttributesHelper;
import org.kuali.kfs.kim.impl.type.KimTypeBo;
import org.kuali.kfs.kim.rule.event.ui.AddGroupEvent;
import org.kuali.kfs.kim.rule.event.ui.AddPersonDelegationMemberEvent;
import org.kuali.kfs.kim.rule.event.ui.AddPersonDocumentRoleQualifierEvent;
import org.kuali.kfs.kim.rule.event.ui.AddRoleEvent;
import org.kuali.kfs.kim.rules.ui.PersonDocumentRoleRule;
import org.kuali.kfs.kim.service.KIMServiceLocatorInternal;
import org.kuali.kfs.kim.web.struts.form.IdentityManagementPersonDocumentForm;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.rice.core.api.membership.MemberType;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization:
 * Modified createDocument() method to update editability of groups, similar to what it does for roles.
 */
public class IdentityManagementPersonDocumentAction extends IdentityManagementDocumentActionBase {

    protected ResponsibilityInternalService responsibilityInternalService;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        ActionForward forward;
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        // accept either the principal name or principal ID, looking up the latter if necessary
        // this allows inquiry links to work even when only the principal name is present
        String principalId = request.getParameter(KIMPropertyConstants.Person.PRINCIPAL_ID);
        String principalName = request.getParameter(KIMPropertyConstants.Person.PRINCIPAL_NAME);
        if (StringUtils.isBlank(principalId) && StringUtils.isNotBlank(principalName)) {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipalByPrincipalName(principalName);
            if (principal != null) {
                principalId = principal.getPrincipalId();
            }
        }
        if (principalId != null) {
            personDocumentForm.setPrincipalId(principalId);
        }
        forward = super.execute(mapping, form, request, response);
        personDocumentForm.setCanModifyEntity(getUiDocumentService().canModifyEntity(
                GlobalVariables.getUserSession().getPrincipalId(), personDocumentForm.getPrincipalId()));
        EntityDefault origEntity = null;
        if (personDocumentForm.getPersonDocument() != null) {
            origEntity = getIdentityService().getEntityDefault(personDocumentForm.getPersonDocument().getEntityId());
        }
        boolean isCreatingNew = personDocumentForm.getPersonDocument() == null || origEntity == null;
        personDocumentForm.setCanOverrideEntityPrivacyPreferences(isCreatingNew
                || getUiDocumentService().canOverrideEntityPrivacyPreferences(
                        GlobalVariables.getUserSession().getPrincipalId(), personDocumentForm.getPrincipalId()));
        return forward;
    }

    @Override
    protected void loadDocument(KualiDocumentFormBase form) throws WorkflowException {
        super.loadDocument(form);
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        IdentityManagementPersonDocument personDoc = personDocumentForm.getPersonDocument();
        populateRoleInformation(personDoc);
        personDoc.resyncTransientState();
    }

    protected void populateRoleInformation(IdentityManagementPersonDocument personDoc) {
        for (PersonDocumentRole role : personDoc.getRoles()) {
            KimType type = KimApiServiceLocator.getKimTypeInfoService().getKimType(role.getKimTypeId());
            KimTypeService kimTypeService;
            if (StringUtils.isNotBlank(type.getServiceName())) {
                kimTypeService = (KimTypeService) KimImplServiceLocator.getBean(type.getServiceName());
            } else {
                kimTypeService = getKimTypeService(KimTypeBo.to(role.getKimRoleType()));
            }
            if (kimTypeService != null) {
                role.setDefinitions(kimTypeService.getAttributeDefinitions(role.getKimTypeId()));
            }
            // when post again, it will need this during populate
            role.setNewRolePrncpl(new KimDocumentRoleMember());
            for (KimAttributeField key : role.getDefinitions()) {
                KimDocumentRoleQualifier qualifier = new KimDocumentRoleQualifier();
                //qualifier.setQualifierKey(key);
                setAttrDefnIdForQualifier(qualifier, key);
                role.getNewRolePrncpl().getQualifiers().add(qualifier);
            }
            role.setAttributeEntry(getUiDocumentService().getAttributeEntries(role.getDefinitions()));
        }
    }

    @Override
    protected void createDocument(KualiDocumentFormBase form) throws WorkflowException {
        super.createDocument(form);
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        if (StringUtils.isBlank(personDocumentForm.getPrincipalId())) {
            personDocumentForm.getPersonDocument().initializeDocumentForNewPerson();
            personDocumentForm.setPrincipalId(personDocumentForm.getPersonDocument().getPrincipalId());
        } else {
            getUiDocumentService().loadEntityToPersonDoc(personDocumentForm.getPersonDocument(),
                    personDocumentForm.getPrincipalId());
            populateRoleInformation(personDocumentForm.getPersonDocument());
            if (personDocumentForm.getPersonDocument() != null) {
                personDocumentForm.getPersonDocument().setIfRolesEditable();
                // CU Customization: Added setup of group-editable flags.
                personDocumentForm.getPersonDocument().setIfGroupsEditable();
            }
        }
    }

    @Override
    public String getActionName() {
        return KimConstants.KimUIConstants.KIM_PERSON_DOCUMENT_ACTION;
    }

    public ActionForward addAffln(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentAffiliation newAffln = personDocumentForm.getNewAffln();
        newAffln.setDocumentNumber(personDocumentForm.getPersonDocument().getDocumentNumber());
        newAffln.refreshReferenceObject("affiliationType");
        personDocumentForm.getPersonDocument().getAffiliations().add(newAffln);
        personDocumentForm.setNewAffln(new PersonDocumentAffiliation());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteAffln(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getAffiliations().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addCitizenship(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentCitizenship newCitizenship = personDocumentForm.getNewCitizenship();
        personDocumentForm.getPersonDocument().getCitizenships().add(newCitizenship);
        personDocumentForm.setNewCitizenship(new PersonDocumentCitizenship());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteCitizenship(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getCitizenships().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addEmpInfo(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        IdentityManagementPersonDocument personDOc = personDocumentForm.getPersonDocument();
        PersonDocumentAffiliation affiliation = personDOc.getAffiliations().get(getSelectedLine(request));
        PersonDocumentEmploymentInfo newempInfo = affiliation.getNewEmpInfo();
        newempInfo.setDocumentNumber(personDOc.getDocumentNumber());
        newempInfo.setVersionNumber(1L);
        affiliation.getEmpInfos().add(newempInfo);
        affiliation.setNewEmpInfo(new PersonDocumentEmploymentInfo());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteEmpInfo(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        String selectedIndexes = getSelectedParentChildIdx(request);
        if (selectedIndexes != null) {
            String[] indexes = StringUtils.split(selectedIndexes, ":");
            PersonDocumentAffiliation affiliation = personDocumentForm.getPersonDocument().getAffiliations()
                    .get(Integer.parseInt(indexes[0]));
            affiliation.getEmpInfos().remove(Integer.parseInt(indexes[1]));
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    protected String getSelectedParentChildIdx(HttpServletRequest request) {
        String lineNumber = null;
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            lineNumber = StringUtils.substringBetween(parameterName, ".line", ".");
        }
        return lineNumber;
    }

    public ActionForward addName(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentName newName = personDocumentForm.getNewName();
        newName.setDocumentNumber(personDocumentForm.getDocument().getDocumentNumber());
        personDocumentForm.getPersonDocument().getNames().add(newName);
        personDocumentForm.setNewName(new PersonDocumentName());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteName(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getNames().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addAddress(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentAddress newAddress = personDocumentForm.getNewAddress();
        newAddress.setDocumentNumber(personDocumentForm.getDocument().getDocumentNumber());
        personDocumentForm.getPersonDocument().getAddrs().add(newAddress);
        personDocumentForm.setNewAddress(new PersonDocumentAddress());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteAddress(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getAddrs().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addPhone(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentPhone newPhone = personDocumentForm.getNewPhone();
        newPhone.setDocumentNumber(personDocumentForm.getDocument().getDocumentNumber());
        personDocumentForm.getPersonDocument().getPhones().add(newPhone);
        personDocumentForm.setNewPhone(new PersonDocumentPhone());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deletePhone(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getPhones().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addEmail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentEmail newEmail = personDocumentForm.getNewEmail();
        newEmail.setDocumentNumber(personDocumentForm.getDocument().getDocumentNumber());
        personDocumentForm.getPersonDocument().getEmails().add(newEmail);
        personDocumentForm.setNewEmail(new PersonDocumentEmail());
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteEmail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        personDocumentForm.getPersonDocument().getEmails().remove(getLineToDelete(request));
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addGroup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentGroup newGroup = personDocumentForm.getNewGroup();
        if (newGroup.getGroupName() == null && newGroup.getNamespaceCode() == null && newGroup.getGroupId() != null) {
            Group tempGroup = KimApiServiceLocator.getGroupService().getGroup(newGroup.getGroupId());
            if (tempGroup == null) {
                GlobalVariables.getMessageMap().putError("newGroup.groupId",
                        RiceKeyConstants.ERROR_ASSIGN_GROUP_INVALID, newGroup.getGroupId(), "");
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
            newGroup.setGroupName(tempGroup.getName());
            newGroup.setNamespaceCode(tempGroup.getNamespaceCode());
            newGroup.setKimTypeId(tempGroup.getKimTypeId());
        } else if (StringUtils.isBlank(newGroup.getGroupName())
                || StringUtils.isBlank(newGroup.getNamespaceCode())) {
            GlobalVariables.getMessageMap().putError("newGroup.groupName",
                    RiceKeyConstants.ERROR_ASSIGN_GROUP_INVALID,
                    newGroup.getNamespaceCode(), newGroup.getGroupName());
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
        Group tempGroup = KimApiServiceLocator.getGroupService().getGroupByNamespaceCodeAndName(
                newGroup.getNamespaceCode(), newGroup.getGroupName());
        if (tempGroup == null) {
            GlobalVariables.getMessageMap().putError("newGroup.groupName",
                    RiceKeyConstants.ERROR_ASSIGN_GROUP_INVALID,
                    newGroup.getNamespaceCode(), newGroup.getGroupName());
            return mapping.findForward(RiceConstants.MAPPING_BASIC);
        }
        newGroup.setGroupId(tempGroup.getId());
        newGroup.setKimTypeId(tempGroup.getKimTypeId());
        if (getKualiRuleService().applyRules(new AddGroupEvent("", personDocumentForm.getPersonDocument(),
                newGroup))) {
            Group group = getGroupService().getGroup(newGroup.getGroupId());
            newGroup.setGroupName(group.getName());
            newGroup.setNamespaceCode(group.getNamespaceCode());
            newGroup.setKimTypeId(group.getKimTypeId());
            personDocumentForm.getPersonDocument().getGroups().add(newGroup);
            personDocumentForm.setNewGroup(new PersonDocumentGroup());
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteGroup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentGroup inactivedGroupMembership = personDocumentForm.getPersonDocument().getGroups()
                .get(getLineToDelete(request));
        Calendar cal = Calendar.getInstance();
        inactivedGroupMembership.setActiveToDate(new Timestamp(cal.getTimeInMillis()));
        personDocumentForm.getPersonDocument().getGroups().set(getLineToDelete(request), inactivedGroupMembership);
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addRole(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentRole newRole = personDocumentForm.getNewRole();

        if (getKualiRuleService().applyRules(new AddRoleEvent("", personDocumentForm.getPersonDocument(),
                newRole))) {
            Role role = KimApiServiceLocator.getRoleService().getRole(newRole.getRoleId());
            if (!validateRole(newRole.getRoleId(), role, PersonDocumentRoleRule.ERROR_PATH, "Person")) {
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
            newRole.setRoleName(role.getName());
            newRole.setNamespaceCode(role.getNamespaceCode());
            newRole.setKimTypeId(role.getKimTypeId());
            KimDocumentRoleMember roleMember = new KimDocumentRoleMember();
            roleMember.setMemberId(personDocumentForm.getPrincipalId());
            roleMember.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
            roleMember.setRoleId(newRole.getRoleId());
            roleMember.setActiveFromDate(newRole.getNewRolePrncpl().getActiveFromDate());
            roleMember.setActiveToDate(newRole.getNewRolePrncpl().getActiveToDate());
            newRole.setNewRolePrncpl(roleMember);
            if (!validateRoleAssignment(personDocumentForm.getPersonDocument(), newRole)) {
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
            KimTypeService kimTypeService = getKimTypeService(KimTypeBo.to(newRole.getKimRoleType()));
            //AttributeDefinitionMap definitions = kimTypeService.getAttributeDefinitions();
            // role type populated from form is not a complete record
            if (kimTypeService != null) {
                newRole.setDefinitions(kimTypeService.getAttributeDefinitions(newRole.getKimTypeId()));
            }
            KimDocumentRoleMember newRolePrncpl = newRole.getNewRolePrncpl();

            for (KimAttributeField key : newRole.getDefinitions()) {
                KimDocumentRoleQualifier qualifier = new KimDocumentRoleQualifier();
                //qualifier.setQualifierKey(key);
                setAttrDefnIdForQualifier(qualifier, key);
                newRolePrncpl.getQualifiers().add(qualifier);
            }
            if (newRole.getDefinitions().isEmpty()) {
                List<KimDocumentRoleMember> rolePrncpls = new ArrayList<>();
                setupRoleRspActions(newRole, newRolePrncpl);
                rolePrncpls.add(newRolePrncpl);
                newRole.setRolePrncpls(rolePrncpls);
            }
            newRole.setAttributeEntry(getUiDocumentService().getAttributeEntries(newRole.getDefinitions()));
            personDocumentForm.getPersonDocument().getRoles().add(newRole);
            personDocumentForm.setNewRole(new PersonDocumentRole());
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    protected boolean validateRoleAssignment(IdentityManagementPersonDocument document, PersonDocumentRole newRole) {
        boolean rulePassed = true;
        if (!document.validAssignRole(newRole)) {
            GlobalVariables.getMessageMap().putError("newRole.roleId", RiceKeyConstants.ERROR_ASSIGN_ROLE,
                    newRole.getNamespaceCode(), newRole.getRoleName());
            rulePassed = false;
        }
        return rulePassed;
    }

    protected void setupRoleRspActions(PersonDocumentRole role, KimDocumentRoleMember rolePrncpl) {
        for (RoleResponsibility roleResp : getResponsibilityInternalService().getRoleResponsibilities(role.getRoleId())) {
            if (getResponsibilityInternalService().areActionsAtAssignmentLevelById(roleResp.getResponsibilityId())) {
                KimDocumentRoleResponsibilityAction roleRspAction = new KimDocumentRoleResponsibilityAction();
                roleRspAction.setRoleResponsibilityId("*");
                // not linked to a role responsibility - so we set the referenced object to null
                roleRspAction.setRoleResponsibility(null);
                roleRspAction.setDocumentNumber(role.getDocumentNumber());

                if (rolePrncpl.getRoleRspActions() == null || rolePrncpl.getRoleRspActions().isEmpty()) {
                    if (rolePrncpl.getRoleRspActions() == null) {
                        rolePrncpl.setRoleRspActions(new ArrayList<>());
                    }
                    rolePrncpl.getRoleRspActions().add(roleRspAction);
                }
            }
        }
    }

    protected void setAttrDefnIdForQualifier(KimDocumentRoleQualifier qualifier, KimAttributeField definition) {
        qualifier.setKimAttrDefnId(definition.getId());
        qualifier.refreshReferenceObject("kimAttribute");
    }

    public ActionForward deleteRole(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        PersonDocumentRole personDocumentRole = personDocumentForm.getPersonDocument().getRoles()
                .get(getLineToDelete(request));
        Calendar cal = Calendar.getInstance();
        personDocumentRole.getRolePrncpls().get(0).setActiveToDate(new Timestamp(cal.getTimeInMillis()));
        personDocumentForm.getPersonDocument().getRoles().set(getLineToDelete(request), personDocumentRole);
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward addRoleQualifier(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        IdentityManagementPersonDocument personDOc = personDocumentForm.getPersonDocument();
        int selectedRoleIdx = getSelectedLine(request);
        PersonDocumentRole role = personDOc.getRoles().get(selectedRoleIdx);
        KimDocumentRoleMember newRolePrncpl = role.getNewRolePrncpl();
        newRolePrncpl.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
        newRolePrncpl.setMemberId(personDOc.getPrincipalId());

        if (getKualiRuleService().applyRules(new AddPersonDocumentRoleQualifierEvent("", personDOc, newRolePrncpl,
                role, selectedRoleIdx))) {
            setupRoleRspActions(role, newRolePrncpl);
            role.getRolePrncpls().add(newRolePrncpl);
            KimDocumentRoleMember roleMember = new KimDocumentRoleMember();
            roleMember.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
            roleMember.setMemberId(personDocumentForm.getPrincipalId());
            role.setNewRolePrncpl(roleMember);
            for (KimAttributeField key : role.getDefinitions()) {
                KimDocumentRoleQualifier qualifier = new KimDocumentRoleQualifier();
                //qualifier.setQualifierKey(key);
                setAttrDefnIdForQualifier(qualifier, key);
                role.getNewRolePrncpl().getQualifiers().add(qualifier);
            }
        }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteRoleQualifier(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        String selectedIndexes = getSelectedParentChildIdx(request);
        if (selectedIndexes != null) {
            String[] indexes = StringUtils.split(selectedIndexes, ":");
            PersonDocumentRole role = personDocumentForm.getPersonDocument().getRoles().get(Integer.parseInt(indexes[0]));
            KimDocumentRoleMember member = role.getRolePrncpls().get(Integer.parseInt(indexes[1]));
            Calendar cal = Calendar.getInstance();
            member.setActiveToDate(new Timestamp(cal.getTimeInMillis()));
            // role.getRolePrncpls().remove(Integer.parseInt(indexes[1]));
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);

    }

    public ActionForward addDelegationMember(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm personDocumentForm = (IdentityManagementPersonDocumentForm) form;
        IdentityManagementPersonDocument personDocument = personDocumentForm.getPersonDocument();
        RoleDocumentDelegationMember newDelegationMember = personDocumentForm.getNewDelegationMember();
        KimTypeAttributesHelper attrHelper = newDelegationMember.getAttributesHelper();
        if (getKualiRuleService().applyRules(new AddPersonDelegationMemberEvent("",
                personDocumentForm.getPersonDocument(), newDelegationMember))) {
            Role role = KimApiServiceLocator.getRoleService().getRole(newDelegationMember.getRoleBo().getId());
            if (role != null) {
                if (!validateRole(newDelegationMember.getRoleBo().getId(), role, PersonDocumentRoleRule.ERROR_PATH,
                        "Person")) {
                    return mapping.findForward(RiceConstants.MAPPING_BASIC);
                }
                newDelegationMember.setRoleBo(RoleBo.from(role));
            }
            KimAttributeField attrDefinition;
            RoleMemberBo roleMember = KIMServiceLocatorInternal.getUiDocumentService().getRoleMember(
                    newDelegationMember.getRoleMemberId());
            Map<String, String>
                    roleMemberAttributes = (new AttributeValidationHelper()).convertAttributesToMap(
                            roleMember.getAttributeDetails());
            for (KimAttributeField key : attrHelper.getDefinitions()) {
                RoleDocumentDelegationMemberQualifier qualifier = new RoleDocumentDelegationMemberQualifier();
                attrDefinition = key;
                qualifier.setKimAttrDefnId(attrHelper.getKimAttributeDefnId(attrDefinition));
                qualifier.setAttrVal(attrHelper.getAttributeValue(roleMemberAttributes,
                        attrDefinition.getAttributeField().getName()));
                newDelegationMember.setMemberTypeCode(MemberType.PRINCIPAL.getCode());
                newDelegationMember.getQualifiers().add(qualifier);
            }
            newDelegationMember.setMemberId(personDocument.getPrincipalId());
            personDocument.getDelegationMembers().add(newDelegationMember);
            personDocumentForm.setNewDelegationMember(new RoleDocumentDelegationMember());
        }
        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward deleteDelegationMember(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocument personDocument =
                ((IdentityManagementPersonDocumentForm) form).getPersonDocument();
        int lineToDelete = getLineToDelete(request);
        RoleDocumentDelegationMember deletedMember = personDocument.getDelegationMembers().remove(lineToDelete);

        // determine if we just deleted the last member from the role delegation, there should only be one but we will
        // use a list just to make sure we get any delegations that no longer have any members
        List<RoleDocumentDelegation> delegationsToRemove = new ArrayList<>();
        for (RoleDocumentDelegation delegation : personDocument.getDelegations()) {
            delegation.getMembers().remove(deletedMember);
            if (delegation.getMembers().isEmpty()) {
                delegationsToRemove.add(delegation);
            }
        }
        for (RoleDocumentDelegation delegationToRemove : delegationsToRemove) {
            personDocument.getDelegations().remove(delegationToRemove);
        }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return super.save(mapping, form, request, response);
    }

    @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        IdentityManagementPersonDocumentForm impdForm = (IdentityManagementPersonDocumentForm) form;

        ActionForward forward = this.refreshAfterDelegationMemberRoleSelection(mapping, impdForm, request);
        if (forward != null) {
            return forward;
        }

        return super.refresh(mapping, form, request, response);
    }

    protected ActionForward refreshAfterDelegationMemberRoleSelection(ActionMapping mapping,
            IdentityManagementPersonDocumentForm impdForm, HttpServletRequest request) {
        String refreshCaller = impdForm.getRefreshCaller();

        boolean isRoleLookupable = KimConstants.KimUIConstants.ROLE_LOOKUPABLE_IMPL.equals(refreshCaller);
        boolean isRoleMemberLookupable = KimConstants.KimUIConstants.KIM_DOCUMENT_ROLE_MEMBER_LOOKUPABLE_IMPL.equals(
                refreshCaller);

        // do not execute the further refreshing logic if the refresh caller is not a lookupable
        if (!isRoleLookupable && !isRoleMemberLookupable) {
            return null;
        }

        //In case of delegation member lookup impdForm.getNewDelegationMemberRoleId() will be populated.
        if (impdForm.getNewDelegationMemberRoleId() == null) {
            return null;
        }
        if (isRoleLookupable) {
            return renderRoleMemberSelection(mapping, request, impdForm);
        }

        String roleMemberId = request.getParameter(KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID);
        if (StringUtils.isNotBlank(roleMemberId)) {
            impdForm.getNewDelegationMember().setRoleMemberId(roleMemberId);
            RoleMemberBo roleMember = getUiDocumentService().getRoleMember(roleMemberId);
            impdForm.getNewDelegationMember().setRoleMemberMemberId(roleMember.getMemberId());
            impdForm.getNewDelegationMember().setRoleMemberMemberTypeCode(roleMember.getType().getCode());
            impdForm.getNewDelegationMember()
                    .setRoleMemberName(getUiDocumentService().getMemberName(MemberType.fromCode(
                            impdForm.getNewDelegationMember().getRoleMemberMemberTypeCode()),
                            impdForm.getNewDelegationMember().getRoleMemberMemberId()));
            impdForm.getNewDelegationMember()
                    .setRoleMemberNamespaceCode(getUiDocumentService().getMemberNamespaceCode(MemberType.fromCode(
                            impdForm.getNewDelegationMember().getRoleMemberMemberTypeCode()),
                            impdForm.getNewDelegationMember().getRoleMemberMemberId()));

            Role role;
            role = KimApiServiceLocator.getRoleService().getRole(impdForm.getNewDelegationMember().getRoleBo()
                    .getId());
            if (role != null) {
                if (!validateRole(impdForm.getNewDelegationMember().getRoleBo().getId(), role,
                        PersonDocumentRoleRule.ERROR_PATH, "Person")) {
                    return mapping.findForward(RiceConstants.MAPPING_BASIC);
                }
                impdForm.getNewDelegationMember().setRoleBo(RoleBo.from(role));
            }
        }
        impdForm.setNewDelegationMemberRoleId(null);
        return null;
    }

    protected ActionForward renderRoleMemberSelection(ActionMapping mapping, HttpServletRequest request,
            IdentityManagementPersonDocumentForm impdForm) {
        Map<String, String> props = new HashMap<>();

        props.put(KRADConstants.SUPPRESS_ACTIONS, Boolean.toString(true));
        props.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, KimDocumentRoleMember.class.getName());
        props.put(KRADConstants.LOOKUP_ANCHOR, KRADConstants.ANCHOR_TOP_OF_FORM);
        props.put(KRADConstants.LOOKED_UP_COLLECTION_NAME, KimConstants.KimUIConstants.ROLE_MEMBERS_COLLECTION_NAME);

        String conversionPatttern = "{0}" + KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR + "{0}";
        String fieldConversion = MessageFormat.format(conversionPatttern,
                KimConstants.PrimaryKeyConstants.SUB_ROLE_ID) + KRADConstants.FIELD_CONVERSIONS_SEPARATOR +
                    MessageFormat.format(conversionPatttern, KimConstants.PrimaryKeyConstants.ROLE_MEMBER_ID) +
                    KRADConstants.FIELD_CONVERSIONS_SEPARATOR;
        props.put(KRADConstants.CONVERSION_FIELDS_PARAMETER, fieldConversion);

        props.put(KimConstants.PrimaryKeyConstants.SUB_ROLE_ID, impdForm.getNewDelegationMember().getRoleBo().getId());

        props.put(KRADConstants.RETURN_LOCATION_PARAMETER, this.getReturnLocation(request, mapping));
        //   props.put(KRADConstants.BACK_LOCATION, this.getReturnLocation(request, mapping));

        props.put(KRADConstants.LOOKUP_AUTO_SEARCH, "Yes");
        props.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.SEARCH_METHOD);

        props.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(impdForm));

        // TODO: how should this forward be handled
        String url = UrlFactory.parameterizeUrl(getApplicationBaseUrl() + "/kr/" + KRADConstants.LOOKUP_ACTION,
                props);

        impdForm.registerEditableProperty("methodToCall");

        return new ActionForward(url, true);
    }

    public ResponsibilityInternalService getResponsibilityInternalService() {
        if (responsibilityInternalService == null) {
            responsibilityInternalService = KimImplServiceLocator.getResponsibilityInternalService();
        }
        return responsibilityInternalService;
    }
}
