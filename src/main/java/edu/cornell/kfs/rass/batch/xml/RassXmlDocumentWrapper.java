package edu.cornell.kfs.rass.batch.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTimeUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.xmladapters.RassStringToJavaDateTimeAdapter;
import edu.emory.mathcs.backport.java.util.Collections;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Kfs", namespace = StringUtils.EMPTY)
public class RassXmlDocumentWrapper {
    
    
    @XmlElement(name = "Extract_Begin_Timestamp", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(RassStringToJavaDateTimeAdapter.class)
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
            return Objects.equals(extractDate, other.getExtractDate()) &&
                    Objects.equals(awards, other.getAwards()) &&
                    Objects.equals(agencies, other.getAgencies());

        } else {
            return false;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RassXmlDocumentWrapper: [ extractDate:").append(extractDate);
        if (CollectionUtils.isNotEmpty(awards)) {
            for (RassXmlAgencyEntry agency : agencies) {
                sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append(awards.toString()).append(KFSConstants.SQUARE_BRACKET_RIGHT);
            }
        } else {
            sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append("NO AWAWRDS").append(KFSConstants.SQUARE_BRACKET_RIGHT);
        }
        if (CollectionUtils.isNotEmpty(agencies)) {
            for (RassXmlAgencyEntry agency : agencies) {
                sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append(agency.toString()).append(KFSConstants.SQUARE_BRACKET_RIGHT);
            }
        } else {
            sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append("NO AGENCIES").append(KFSConstants.SQUARE_BRACKET_RIGHT);
        }
        sb.append(KFSConstants.SQUARE_BRACKET_RIGHT);
        return sb.toString();
    }

}
