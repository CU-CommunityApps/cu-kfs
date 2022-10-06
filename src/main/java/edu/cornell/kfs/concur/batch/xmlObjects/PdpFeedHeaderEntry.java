package edu.cornell.kfs.concur.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.cornell.kfs.concur.ConcurConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "campus",
    "unit",
    "subUnit",
    "creationDate"
})
@XmlRootElement(name = "header", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedHeaderEntry {

    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String campus;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String unit;
    @XmlElement(name = "sub_unit", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subUnit;
    @XmlElement(name = "creation_date", namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String creationDate;

    public String getCampus() {
        return campus;
    }

    public void setCampus(String value) {
        this.campus = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String value) {
        this.unit = value;
    }

    public String getSubUnit() {
        return subUnit;
    }

    public void setSubUnit(String value) {
        this.subUnit = value;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String value) {
        this.creationDate = value;
    }

}