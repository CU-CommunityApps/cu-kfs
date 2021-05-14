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
package org.kuali.kfs.ksr.bo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.kuali.rice.krad.bo.DataObjectBase;

/**
 * ====
 * CU Customization:
 * Remediated as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@Entity
@Table(name = "KRSR_SEC_PRV_T")
public class SecurityProvisioning extends DataObjectBase {

    private static final long serialVersionUID = 5942007486659650949L;

    @Id
    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SEC_GRP_ID")
    private List<SecurityProvisioningGroup> securityProvisioningGroups;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEC_GRP_ID", insertable = false, updatable = false)
    private SecurityGroup securityGroup;

    public SecurityProvisioning() {
        securityProvisioningGroups = new ArrayList<>();
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public List<SecurityProvisioningGroup> getSecurityProvisioningGroups() {
        return securityProvisioningGroups;
    }

    public void setSecurityProvisioningGroups(List<SecurityProvisioningGroup> securityProvisioningGroups) {
        this.securityProvisioningGroups = securityProvisioningGroups;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

}
