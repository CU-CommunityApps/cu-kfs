package edu.cornell.kfs.ksr.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.krad.document.TransactionalDocumentBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.ksr.KSRPropertyConstants;
import edu.cornell.kfs.ksr.businessobject.SecurityGroup;
import edu.cornell.kfs.ksr.businessobject.SecurityRequestRole;
import edu.cornell.kfs.ksr.service.SecurityRequestPostProcessingService;

public class SecurityRequestDocument extends TransactionalDocumentBase {
    private static final long serialVersionUID = 4006821516580654441L;

    private String principalId;
    private String primaryDepartmentCode;
    private Long securityGroupId;

    private Person requestPerson;
    private SecurityGroup securityGroup;

    private List<SecurityRequestRole> securityRequestRoles;

    public SecurityRequestDocument() {
        super();

        securityRequestRoles = new ArrayList<SecurityRequestRole>();
    }

    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            SpringContext.getBean(SecurityRequestPostProcessingService.class).postProcessSecurityRequest(this);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List buildListOfDeletionAwareLists() {
        List managedLists = super.buildListOfDeletionAwareLists();

        for (SecurityRequestRole requestRole : securityRequestRoles) {
            managedLists.add(requestRole.getRequestRoleQualifications());
        }

        return managedLists;
    }

    public String getPrincipalId() {
        return this.principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPrimaryDepartmentCode() {
        return this.primaryDepartmentCode;
    }

    public void setPrimaryDepartmentCode(String primaryDepartmentCode) {
        this.primaryDepartmentCode = primaryDepartmentCode;
    }

    public Long getSecurityGroupId() {
        return this.securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public Person getRequestPerson() {
        PersonService personService = KimApiServiceLocator.getPersonService();
        requestPerson = personService.getPerson(principalId);
        if (requestPerson == null) {
            requestPerson = new Person();
        }
        return requestPerson;
    }

    public void setRequestPerson(Person requestPerson) {
        if (requestPerson != null) {
            this.principalId = requestPerson.getPrincipalId();
        }
        this.requestPerson = requestPerson;
    }

    public SecurityGroup getSecurityGroup() {
        return this.securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public List<SecurityRequestRole> getSecurityRequestRoles() {
        return this.securityRequestRoles;
    }

    public void setSecurityRequestRoles(List<SecurityRequestRole> securityRequestRoles) {
        this.securityRequestRoles = securityRequestRoles;
    }

    public String getPrincipalNameForSearch() {
        return null;
    }

    public void setPrincipalNameForSearch(String principalNameForSearch) {
    }

    public Role getRoleForSearch() {
        return null;
    }

    public void setRoleForSearch(Role roleForSearch) {
    }

    public String getRoleIdForSearch() {
        return null;
    }

    public void setRoleIdForSearch(String roleIdForSearch) {
    }

    public String getRoleQualifierValueForSearch() {
        return null;
    }

    public void setRoleQualifierValueForSearch(String roleQualifierValueForSearch) {

    }

    @Override
    public void refreshReferenceObject(String referenceObjectName) {
        if (!KSRPropertyConstants.SECURITY_REQUEST_DOC_REQUEST_PERSON.equals(referenceObjectName)) {
            super.refreshReferenceObject(referenceObjectName);
        }
    }

    protected int findSecurityRequestRoleIndex(final String roleId) {
        int roleIndex = -1;

        for (int i = 0; i < getSecurityRequestRoles().size(); i++) {
            final SecurityRequestRole requestRole = getSecurityRequestRoles().get(i);

            if (StringUtils.equals(roleId, requestRole.getRoleId())) {
                roleIndex = i;
                break;
            }
        }

        return roleIndex;
    }

}
