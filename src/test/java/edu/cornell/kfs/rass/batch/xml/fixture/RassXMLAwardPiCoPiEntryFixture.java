package edu.cornell.kfs.rass.batch.xml.fixture;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public enum RassXMLAwardPiCoPiEntryFixture {
    cah292_PRIMARY(Boolean.TRUE, "cah292"),
    NO_NAME_PRIMARY(Boolean.TRUE, StringUtils.EMPTY),
    jdh34_CO_PI(Boolean.FALSE, "jdh34");
    
    public final Boolean primary;
    public final String projectDirectorPrincipalName;
    
    private RassXMLAwardPiCoPiEntryFixture(Boolean primary, String projectDirectorPrincipalName) {
        this.primary = primary;
        this.projectDirectorPrincipalName = projectDirectorPrincipalName;
    }
    
    public RassXMLAwardPiCoPiEntry toRassXMLAwardPiCoPiEntry() {
        RassXMLAwardPiCoPiEntry piEntry = new RassXMLAwardPiCoPiEntry();
        piEntry.setPrimary(primary);
        piEntry.setProjectDirectorPrincipalName(projectDirectorPrincipalName);
        return piEntry;
    }

}
