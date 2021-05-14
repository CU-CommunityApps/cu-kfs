/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.kfs.ksr.document;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kuali.kfs.ksr.bo.SecurityGroup;
import org.kuali.kfs.ksr.bo.SecurityRequestRole;
import org.kuali.kfs.ksr.service.KSRServiceLocator;
import org.kuali.kfs.ksr.service.SecurityRequestPostProcessingService;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.role.RoleBo;
import org.kuali.rice.krad.document.TransactionalDocumentBase;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@AttributeOverrides({
    @AttributeOverride(name = "documentNumber", column = @Column(name = "FDOC_NBR"))
})
@Entity
@Table(name = "KRSR_SEC_RQ_T")
public class SecurityRequestDocument extends TransactionalDocumentBase {

    private static final long serialVersionUID = 4006821516580654441L;

    @Column(name = "PRNCPL_ID")
    private String principalId;

    @Column(name = "PRMRY_DEPT_CD")
    private String primaryDepartmentCode;

    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SEC_GRP_ID", insertable = false, updatable = false)
    private SecurityGroup securityGroup;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "FDOC_NBR")
    private List<SecurityRequestRole> securityRequestRoles;

    @Transient
    private Person requestPerson;

    public SecurityRequestDocument() {
        securityRequestRoles = new ArrayList<>();
    }

    /**
     * Invoke <code>SecurityRequestPostProcessingService</code> to update KIM when security request becomes final
     * 
     * @see org.kuali.rice.krad.document.DocumentBase#doRouteStatusChange(org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        super.doRouteStatusChange(statusChangeEvent);

        if (getDocumentHeader().getWorkflowDocument().isProcessed()) {
            getSecurityRequestPostProcessingService().postProcessSecurityRequest(this);
        }
    }

    public String getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(String principalId) {
        this.principalId = principalId;
    }

    public String getPrimaryDepartmentCode() {
        return primaryDepartmentCode;
    }

    public void setPrimaryDepartmentCode(String primaryDepartmentCode) {
        this.primaryDepartmentCode = primaryDepartmentCode;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public Person getRequestPerson() {
        requestPerson = getPersonService().getPerson(principalId);
        if (requestPerson == null) {
            try {
                requestPerson = getPersonService().getPersonImplementationClass().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public List<SecurityRequestRole> getSecurityRequestRoles() {
        return securityRequestRoles;
    }

    public void setSecurityRequestRoles(List<SecurityRequestRole> securityRequestRoles) {
        this.securityRequestRoles = securityRequestRoles;
    }

    public String getPrincipalNameForSearch() {
        return null;
    }
    
    public void setPrincipalNameForSearch(String principalNameForSearch) {
    }
    
    public RoleBo getRoleForSearch() {
        return null;
    }
    
    public void setRoleForSearch(RoleBo roleForSearch) {
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

    /*
     * This is a workaround for some "document.document" property path duplication
     * that occurs when building the request role qualification fields.
     */
    public SecurityRequestDocument getDocument() {
        return this;
    }

    protected SecurityRequestPostProcessingService getSecurityRequestPostProcessingService() {
        return KSRServiceLocator.getSecurityRequestPostProcessingService();
    }

    protected PersonService getPersonService() {
        return KimApiServiceLocator.getPersonService();
    }

}
