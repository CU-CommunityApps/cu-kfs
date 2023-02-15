package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "internalName", "displayName", "additionalDataList", "dd214Certificate",
        "diversityCertificate" })
@XmlRootElement(name = "DiversityClassification")
public class DiversityClassification {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "InternalName", required = true)
    protected InternalName internalName;
    @XmlElement(name = "DisplayName")
    protected DisplayName displayName;
    @XmlElement(name = "AdditionalDataList")
    protected AdditionalDataList additionalDataList;
    @XmlElement(name = "DD-214Certificate")
    protected DD214Certificate dd214Certificate;
    @XmlElement(name = "DiversityCertificate")
    protected DiversityCertificate diversityCertificate;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public InternalName getInternalName() {
        return internalName;
    }

    public void setInternalName(InternalName value) {
        this.internalName = value;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName value) {
        this.displayName = value;
    }

    public AdditionalDataList getAdditionalDataList() {
        return additionalDataList;
    }

    public void setAdditionalDataList(AdditionalDataList value) {
        this.additionalDataList = value;
    }

    public DD214Certificate getDD214Certificate() {
        return dd214Certificate;
    }

    public void setDD214Certificate(DD214Certificate value) {
        this.dd214Certificate = value;
    }

    public DiversityCertificate getDiversityCertificate() {
        return diversityCertificate;
    }

    public void setDiversityCertificate(DiversityCertificate value) {
        this.diversityCertificate = value;
    }

}
