package edu.cornell.kfs.module.purap.identity;

import org.kuali.kfs.module.purap.identity.PurapKimAttributes;

public class CuPurapKimAttributes extends PurapKimAttributes {
    private static final long serialVersionUID = 5268027448509178843L;

    public static final String JAGGAER_ROLE = "jaggaerRole";

    protected String jaggaerRole;

    public String getJaggaerRole() {
        return jaggaerRole;
    }

    public void setJaggaerRole(final String jaggaerRole) {
        this.jaggaerRole = jaggaerRole;
    }

}
