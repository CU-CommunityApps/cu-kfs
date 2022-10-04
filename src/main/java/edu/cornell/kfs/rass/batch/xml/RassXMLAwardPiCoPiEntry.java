package edu.cornell.kfs.rass.batch.xml;

import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.cornell.kfs.sys.xmladapters.BooleanNullPossibleXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "PI_or_CoPI", namespace = StringUtils.EMPTY)
public class RassXMLAwardPiCoPiEntry implements RassXmlObject{
    
    @XmlElement(name = "Primary", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(BooleanNullPossibleXmlAdapter.class)
    private Boolean primary;
    
    @XmlElement(name = "Project_Director_Principal_Name", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String projectDirectorPrincipalName;
    
    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }
    
    public String getProjectDirectorPrincipalName() {
        return projectDirectorPrincipalName;
    }

    public void setProjectDirectorPrincipalName(String projectDirectorPrincipalName) {
        this.projectDirectorPrincipalName = projectDirectorPrincipalName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof RassXMLAwardPiCoPiEntry) {
            RassXMLAwardPiCoPiEntry other = (RassXMLAwardPiCoPiEntry) o;
            return Objects.equals(primary, other.getPrimary()) &&
                    StringUtils.equals(projectDirectorPrincipalName, other.getProjectDirectorPrincipalName());
            
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(primary, projectDirectorPrincipalName);
    }

}
