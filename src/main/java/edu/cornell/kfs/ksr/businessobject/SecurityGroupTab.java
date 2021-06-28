package edu.cornell.kfs.ksr.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SecurityGroupTab extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = -4877261432186568198L;
    private Long securityGroupId;
    private Long tabId;
    private String tabName;
    private Long tabOrder;
    private boolean active;

    //List<SecurityProvisioningGroup> securityProvisioningGroups;

    public SecurityGroupTab() {
        //securityProvisioningGroups = new ArrayList<>();
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

    // public List<SecurityProvisioningGroup> getSecurityProvisioningGroups() {
    // return securityProvisioningGroups;
    // }
    //
    // public void setSecurityProvisioningGroups(List<SecurityProvisioningGroup> securityProvisioningGroups) {
    // this.securityProvisioningGroups = securityProvisioningGroups;
    // }

}
