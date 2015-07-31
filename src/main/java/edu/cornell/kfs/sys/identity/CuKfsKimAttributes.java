package edu.cornell.kfs.sys.identity;

import org.kuali.kfs.sys.identity.KfsKimAttributes;

/**
 * Custom KIM attributes class containing CU-specific attributes.
 */
public class CuKfsKimAttributes extends KfsKimAttributes {

    public static final String WITHIN_PAY_PERIOD_LIMIT = "withinPayPeriodLimit";

    private static final long serialVersionUID = -6710437507818860443L;



    protected String withinPayPeriodLimit;



    public String isWithinPayPeriodLimit() {
        return withinPayPeriodLimit;
    }

    public void setWithinPayPeriodLimit(String withinPayPeriodLimit) {
        this.withinPayPeriodLimit = withinPayPeriodLimit;
    }

}
