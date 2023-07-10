package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "erpNumber", "sqIntegrationNumber" })
@XmlRootElement(name = "ParentSupplier")
public class ParentSupplier {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "ERPNumber")
    private ErpNumber erpNumber;
    @XmlElement(name = "SQIntegrationNumber")
    private SQIntegrationNumber sqIntegrationNumber;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public ErpNumber getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(ErpNumber erpNumber) {
        this.erpNumber = erpNumber;
    }

    public SQIntegrationNumber getSqIntegrationNumber() {
        return sqIntegrationNumber;
    }

    public void setSqIntegrationNumber(SQIntegrationNumber sqIntegrationNumber) {
        this.sqIntegrationNumber = sqIntegrationNumber;
    }

}
