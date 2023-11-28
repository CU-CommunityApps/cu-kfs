package edu.cornell.kfs.pdp.businessobject;

import org.kuali.kfs.pdp.businessobject.ACHPayee;
import org.kuali.kfs.kim.impl.identity.Person;

/**
 * Custom subclass of ACHPayee that has an extra netID/principalName property.
 */
public class CuACHPayee extends ACHPayee {
    private static final long serialVersionUID = -6586239257492551426L;

    private String principalName;

    public CuACHPayee() {
        super();
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

    /**
     * Getter for ACH person that always returns null; it is only intended to aid with
     * generating a lookup icon for the "principalName" property.
     * 
     * @return null.
     */
    public Person getAchPerson() {
        return null;
    }

    /**
     * No-op setter for ACH person; it is only intended to aid with
     * generating a lookup icon for the "principalName" property.
     * 
     * @param achPerson The ACH person to set; not actually used.
     */
    public void setAchPerson(Person achPerson) {
        // Do nothing.
    }

}
