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

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.kuali.rice.core.api.mo.common.active.Inactivatable;
import org.kuali.rice.krad.bo.DataObjectBase;
import org.kuali.rice.krad.data.jpa.PortableSequenceGenerator;
import org.kuali.rice.krad.data.jpa.converters.BooleanYNConverter;

/**
 * ====
 * CU Customization:
 * Copied over the version of this class from a more up-to-date rSmart KSR repository,
 * since it contains some fixes and features that are not present in the older KSR repository.
 * 
 * Also Remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@IdClass(SecurityGroupTabId.class)
@Entity
@Table(name = "KRSR_SEC_GRP_TB_T")
public class SecurityGroupTab extends DataObjectBase implements Inactivatable {

    private static final long serialVersionUID = -4877261432186568198L;

    @Id
    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @Id
    @GeneratedValue(generator = "KRSR_SEC_GRP_TB_ID_SEQ")
    @PortableSequenceGenerator(name = "KRSR_SEC_GRP_TB_ID_SEQ")
    @Column(name = "TB_ID")
    private Long tabId;

    @Column(name = "TB_NM")
    private String tabName;

    @Column(name = "TB_ORD")
	private Long tabOrder;

    @Column(name = "ACTV_IND")
    @Convert(converter = BooleanYNConverter.class)
    private boolean active;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SEC_GRP_ID", referencedColumnName = "SEC_GRP_ID"),
            @JoinColumn(name = "TB_ID", referencedColumnName = "TB_ID")
    })
    List<SecurityProvisioningGroup> securityProvisioningGroups;

    public SecurityGroupTab() {
        securityProvisioningGroups = new ArrayList<>();
        active = true;
    }

    public Long getSecurityGroupId() {
        return this.securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public Long getTabId() {
        return this.tabId;
    }

    public void setTabId(Long tabId) {
        this.tabId = tabId;
    }

    public String getTabName() {
        return this.tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }

    public Long getTabOrder() {
        return this.tabOrder;
    }

    public void setTabOrder(Long tabOrder) {
        this.tabOrder = tabOrder;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SecurityProvisioningGroup> getSecurityProvisioningGroups() {
        return securityProvisioningGroups;
    }

    public void setSecurityProvisioningGroups(List<SecurityProvisioningGroup> securityProvisioningGroups) {
        this.securityProvisioningGroups = securityProvisioningGroups;
    }

}
