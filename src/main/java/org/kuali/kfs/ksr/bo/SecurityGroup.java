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
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
 * Also remediated this class as needed for Rice 2.x and JPA compatibility.
 * ====
 * 
 * @author rSmart Development Team
 */
@Entity
@Table(name = "KRSR_SEC_GRP_T")
public class SecurityGroup extends DataObjectBase implements Inactivatable {

    private static final long serialVersionUID = -234206146013644273L;

    @Id
    @GeneratedValue(generator = "KRSR_SEC_GRP_ID_SEQ")
    @PortableSequenceGenerator(name = "KRSR_SEC_GRP_ID_SEQ")
    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @Column(name = "SEC_GRP_NM")
    private String securityGroupName;

    @Column(name = "SEC_GRP_DESC")
    private String securityGroupDescription;

    @Column(name = "ACTV_IND")
    @Convert(converter = BooleanYNConverter.class)
    private boolean active;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SEC_GRP_ID")
    private List<SecurityGroupTab> securityGroupTabs;

    public SecurityGroup() {
        securityGroupTabs = new ArrayList<>();
        active = true;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(Long securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public void setSecurityGroupName(String securityGroupName) {
        this.securityGroupName = securityGroupName;
    }

    public String getSecurityGroupDescription() {
        return securityGroupDescription;
    }

    public void setSecurityGroupDescription(String securityGroupDescription) {
        this.securityGroupDescription = securityGroupDescription;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<SecurityGroupTab> getSecurityGroupTabs() {
        return securityGroupTabs;
    }

    public void setSecurityGroupTabs(List<SecurityGroupTab> securityGroupTabs) {
        this.securityGroupTabs = securityGroupTabs;
    }

}
