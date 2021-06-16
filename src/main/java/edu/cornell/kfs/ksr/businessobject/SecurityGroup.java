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
package edu.cornell.kfs.ksr.businessobject;

import org.kuali.kfs.core.api.mo.common.active.Inactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/**
 * ==== CU Customization: ====
 * 
 * @author rSmart Development Team
 */
public class SecurityGroup extends PersistableBusinessObjectBase implements Inactivatable {

    private static final long serialVersionUID = -234206146013644273L;

    private Long securityGroupId;
    private String securityGroupName;
    private String securityGroupDescription;
    private boolean active;

    // private List<SecurityGroupTab> securityGroupTabs;

    public SecurityGroup() {
        // securityGroupTabs = new ArrayList<>();
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

    // public List<SecurityGroupTab> getSecurityGroupTabs() {
    // return securityGroupTabs;
    // }
    //
    // public void setSecurityGroupTabs(List<SecurityGroupTab> securityGroupTabs) {
    // this.securityGroupTabs = securityGroupTabs;
    // }

}
