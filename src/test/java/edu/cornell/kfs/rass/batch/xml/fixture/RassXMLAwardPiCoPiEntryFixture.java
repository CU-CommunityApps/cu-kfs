package edu.cornell.kfs.rass.batch.xml.fixture;

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.rass.batch.xml.RassXMLAwardPiCoPiEntry;

public enum RassXMLAwardPiCoPiEntryFixture {
    cah292_PRIMARY("Y", "cah292"),
    NO_NAME_PRIMARH("Y", StringUtils.EMPTY),
    jdh34_CO_PI("N", "jdh34");
    
    public final String primaryString;
    public final String principalName;
    
    private RassXMLAwardPiCoPiEntryFixture(String primaryString, String principalName) {
        this.primaryString = primaryString;
        this.principalName = principalName;
    }
    
    public RassXMLAwardPiCoPiEntry toRassXMLAwardPiCoPiEntry() {
        RassXMLAwardPiCoPiEntry piEntry = new RassXMLAwardPiCoPiEntry();
        piEntry.setPrimaryString(primaryString);
        piEntry.setPrincipalName(principalName);
        return piEntry;
    }

}
