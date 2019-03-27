package edu.cornell.kfs.rass.batch.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY)
public class RassXMLAwardPiCoPiEntry {
    
    @XmlElement(name = "Primary", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String primaryString;
    
    @XmlElement(name = "ReportEmail", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String principalName;
    
    public boolean isPrimaryPI() {
        return StringUtils.equalsIgnoreCase(getPrimaryString(), KFSConstants.ParameterValues.YES);
    }

    public String getPrimaryString() {
        return primaryString;
    }

    public void setPrimaryString(String primaryString) {
        this.primaryString = primaryString;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RassXMLAwardPiCoPiEntry: [ primaryString:").append(primaryString);
        sb.append(", principalName:").append(principalName);
        sb.append(", isPrimaryPI:").append(isPrimaryPI());
        sb.append(KFSConstants.SQUARE_BRACKET_RIGHT);
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof RassXMLAwardPiCoPiEntry) {
            RassXMLAwardPiCoPiEntry other = (RassXMLAwardPiCoPiEntry) o;
            return StringUtils.equals(primaryString, other.getPrimaryString()) &&
                    StringUtils.equals(primaryString, other.getPrimaryString());
            
        } else {
            return false;
        }
    }

}
