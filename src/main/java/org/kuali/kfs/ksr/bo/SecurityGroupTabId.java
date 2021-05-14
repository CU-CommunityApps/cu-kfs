package org.kuali.kfs.ksr.bo;

import javax.persistence.Column;
import javax.persistence.Id;

import org.kuali.rice.krad.data.jpa.IdClassBase;

/**
 * ====
 * CU Customization:
 * Added this security group tab ID class as part of
 * the KSR module's JPA conversion.
 * ====
 */
public class SecurityGroupTabId extends IdClassBase {

    private static final long serialVersionUID = 5204232845331040419L;

    @Id
    @Column(name = "SEC_GRP_ID")
    private Long securityGroupId;

    @Id
    @Column(name = "TB_ID")
    private Long tabId;

    public SecurityGroupTabId() {
    }

    public SecurityGroupTabId(Long securityGroupId, Long tabId) {
        this.securityGroupId = securityGroupId;
        this.tabId = tabId;
    }

    public Long getSecurityGroupId() {
        return securityGroupId;
    }

    public Long getTabId() {
        return tabId;
    }

}
