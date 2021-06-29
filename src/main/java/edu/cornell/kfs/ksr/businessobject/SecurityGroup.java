package edu.cornell.kfs.ksr.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class SecurityGroup extends PersistableBusinessObjectBase implements MutableInactivatable {

    private static final long serialVersionUID = -234206146013644273L;

    private Long securityGroupId;
    private String securityGroupName;
    private String securityGroupDescription;
    private boolean active;

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
