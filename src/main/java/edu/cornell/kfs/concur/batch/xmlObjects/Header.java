package edu.cornell.kfs.concur.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "chart",
    "unit",
    "subUnit",
    "creationDate"
})
@XmlRootElement(name = "header", namespace = "http://www.kuali.org/kfs/pdp/payment")
public class Header {

    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String chart;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String unit;
    @XmlElement(name = "sub_unit", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String subUnit;
    @XmlElement(name = "creation_date", namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String creationDate;

    public String getChart() {
        return chart;
    }

    public void setChart(String value) {
        this.chart = value;
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