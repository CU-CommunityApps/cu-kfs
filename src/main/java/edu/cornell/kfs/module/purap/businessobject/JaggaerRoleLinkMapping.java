package edu.cornell.kfs.module.purap.businessobject;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class JaggaerRoleLinkMapping extends PersistableBusinessObjectBase implements MutableInactivatable {
    private static final long serialVersionUID = -7713390264764633449L;
    
    private String jaggaerRoleName;
    private boolean eShopLink;
    private boolean contractsPlusLink;
    private boolean jaggaerAdminLink;
    private boolean active;
    
    
    public String getJaggaerRoleName() {
        return jaggaerRoleName;
    }

    public void setJaggaerRoleName(String jaggaerRoleName) {
        this.jaggaerRoleName = jaggaerRoleName;
    }

    public boolean iseShopLink() {
        return eShopLink;
    }

    public void seteShopLink(boolean eShopLink) {
        this.eShopLink = eShopLink;
    }

    public boolean isContractsPlusLink() {
        return contractsPlusLink;
    }

    public void setContractsPlusLink(boolean contractsPlusLink) {
        this.contractsPlusLink = contractsPlusLink;
    }

    public boolean isJaggaerAdminLink() {
        return jaggaerAdminLink;
    }

    public void setJaggaerAdminLink(boolean jaggaerAdminLink) {
        this.jaggaerAdminLink = jaggaerAdminLink;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this);
        return builder.build();
    }

}
