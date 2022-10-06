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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Agency", namespace = StringUtils.EMPTY)
public class RassXmlAgencyEntry implements RassXmlObject {
    
    @XmlElement(name = "Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String number;
    
    @XmlElement(name = "Reporting_Name", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String reportingName;
    
    @XmlElement(name = "Full_Name", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String fullName;
    
    @XmlElement(name = "Type_Code", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String typeCode;
    
    @XmlElement(name = "Reports_to_Agency_Number", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String reportsToAgencyNumber;
    
    @XmlElement(name = "Common_Name", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String commonName;
    
    @XmlElement(name = "Agency_Origin", namespace = StringUtils.EMPTY)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String agencyOrigin;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getReportingName() {
        return reportingName;
    }

    public void setReportingName(String reportingName) {
        this.reportingName = reportingName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getReportsToAgencyNumber() {
        return reportsToAgencyNumber;
    }

    public void setReportsToAgencyNumber(String reportsToAgencyNumber) {
        this.reportsToAgencyNumber = reportsToAgencyNumber;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getAgencyOrigin() {
        return agencyOrigin;
    }

    public void setAgencyOrigin(String agencyOrigin) {
        this.agencyOrigin = agencyOrigin;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof RassXmlAgencyEntry) {
            RassXmlAgencyEntry other = (RassXmlAgencyEntry) o;
            return StringUtils.equals(number, other.getNumber()) &&
                    StringUtils.equals(reportingName, other.getReportingName()) &&
                    StringUtils.equals(fullName, other.getFullName()) &&
                    StringUtils.equals(typeCode, other.getTypeCode()) &&
                    StringUtils.equals(reportsToAgencyNumber, other.getReportsToAgencyNumber()) &&
                    StringUtils.equals(commonName, other.getCommonName()) &&
                    StringUtils.equals(agencyOrigin, other.getAgencyOrigin());
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(number, reportingName, fullName, typeCode, reportsToAgencyNumber, commonName, agencyOrigin);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
