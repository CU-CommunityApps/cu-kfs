package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTimeUtils;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Kfs", namespace = StringUtils.EMPTY)
public class RassXmlDocumentWrapper {
    
    
    @XmlElement(name = "Extract_Begin_Timestamp", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(StringToJavaDateAdapter.class)
    private Date extractDate;
    
    @XmlElementWrapper(name = "Awards", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "Award", namespace = StringUtils.EMPTY, required = true)
    private List<RassXmlAwardEntry> awards;
    
    @XmlElementWrapper(name = "Agencies", namespace = StringUtils.EMPTY, required = true)
    @XmlElement(name = "Agency", namespace = StringUtils.EMPTY, required = true)
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
            return DateUtils.isSameInstant(extractDate, other.getExtractDate());
        } else {
            return false;
        }
        
    }

}
