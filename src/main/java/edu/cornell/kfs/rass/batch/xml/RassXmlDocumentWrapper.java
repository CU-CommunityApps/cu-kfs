package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import edu.cornell.kfs.sys.xmladapters.RassStringToJavaLongDateTimeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Kfs", namespace = StringUtils.EMPTY)
public class RassXmlDocumentWrapper {
    
    @XmlElement(name = "Extract_Begin_Timestamp", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(RassStringToJavaLongDateTimeAdapter.class)
    private Date extractDate;
    
    @XmlElementWrapper(name = "Awards", namespace = StringUtils.EMPTY)
    @XmlElement(name = "Award", namespace = StringUtils.EMPTY)
    private List<RassXmlAwardEntry> awards;
    
    @XmlElementWrapper(name = "Agencies", namespace = StringUtils.EMPTY)
    @XmlElement(name = "Agency", namespace = StringUtils.EMPTY)
    private List<RassXmlAgencyEntry> agencies;
    
    public RassXmlDocumentWrapper() {
        awards = new ArrayList<RassXmlAwardEntry>();
        agencies = new ArrayList<RassXmlAgencyEntry>();
    }

    public Date getExtractDate() {
        return extractDate;
    }

    public void setExtractDate(Date extractDate) {
        this.extractDate = extractDate;
    }

    public List<RassXmlAwardEntry> getAwards() {
        return awards;
    }

    public void setAwards(List<RassXmlAwardEntry> awards) {
        this.awards = awards;
    }

    public List<RassXmlAgencyEntry> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<RassXmlAgencyEntry> agencies) {
        this.agencies = agencies;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof RassXmlDocumentWrapper) {
            RassXmlDocumentWrapper other = (RassXmlDocumentWrapper) o;
            return Objects.equals(extractDate, other.getExtractDate()) &&
                    Objects.equals(awards, other.getAwards()) &&
                    Objects.equals(agencies, other.getAgencies());

        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(extractDate, awards, agencies);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
