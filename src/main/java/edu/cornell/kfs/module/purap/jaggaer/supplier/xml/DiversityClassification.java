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
@XmlType(name = "", propOrder = { "internalName", "displayName", "additionalDataList", "dd214Certificate",
        "diversityCertificate" })
@XmlRootElement(name = "DiversityClassification")
public class DiversityClassification {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "InternalName", required = true)
    protected JaggaerBasicValue internalName;
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

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getInternalName() {
        return internalName;
    }

    public void setInternalName(JaggaerBasicValue internalName) {
        this.internalName = internalName;
    }

    public DisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(DisplayName displayName) {
        this.displayName = displayName;
    }

    public AdditionalDataList getAdditionalDataList() {
        return additionalDataList;
    }

    public void setAdditionalDataList(AdditionalDataList additionalDataList) {
        this.additionalDataList = additionalDataList;
    }

    public DD214Certificate getDd214Certificate() {
        return dd214Certificate;
    }

    public void setDd214Certificate(DD214Certificate dd214Certificate) {
        this.dd214Certificate = dd214Certificate;
    }

    public DiversityCertificate getDiversityCertificate() {
        return diversityCertificate;
    }

    public void setDiversityCertificate(DiversityCertificate diversityCertificate) {
        this.diversityCertificate = diversityCertificate;
    }

}
