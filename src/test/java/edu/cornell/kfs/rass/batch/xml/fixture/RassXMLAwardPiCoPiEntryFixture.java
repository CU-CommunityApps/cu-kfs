package edu.cornell.kfs.rass.batch.xml.fixture;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public enum RassXMLAwardPiCoPiEntryFixture {
    cah292_PRIMARY(Boolean.TRUE, "cah292"),
    NO_NAME_PRIMARY(Boolean.TRUE, StringUtils.EMPTY),
    jdh34_CO_PI(Boolean.FALSE, "jdh34"),
    mgw3_PRIMARY(Boolean.TRUE, "mgw3"),
    mgw3_CO_PI_INACTIVE(Boolean.FALSE, "mgw3", Boolean.FALSE),
    mo14_PRIMARY(Boolean.TRUE, "mo14"),
    mo14_CO_PI(Boolean.FALSE, "mo14"),
    mo14_CO_PI_INACTIVE(Boolean.FALSE, "mo14", Boolean.FALSE),
    kan2_PRIMARY(Boolean.TRUE, "kan2");
    
    public final Boolean primary;
    public final String projectDirectorPrincipalName;
    public final Boolean active;
    
    private RassXMLAwardPiCoPiEntryFixture(Boolean primary, String projectDirectorPrincipalName) {
        this(primary, projectDirectorPrincipalName, Boolean.TRUE);
    }
    
    private RassXMLAwardPiCoPiEntryFixture(Boolean primary, String projectDirectorPrincipalName, Boolean active) {
        this.primary = primary;
        this.projectDirectorPrincipalName = projectDirectorPrincipalName;
        this.active = active;
    }
    
    public RassXMLAwardPiCoPiEntry toRassXMLAwardPiCoPiEntry() {
        RassXMLAwardPiCoPiEntry piEntry = new RassXMLAwardPiCoPiEntry();
        piEntry.setPrimary(primary);
        piEntry.setProjectDirectorPrincipalName(projectDirectorPrincipalName);
        return piEntry;
    }

}
