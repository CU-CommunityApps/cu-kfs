package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "taxDocumentName", "taxDocumentYear", "taxDocument" })
@XmlRootElement(name = "TaxInformation")
public class TaxInformation {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "TaxDocumentName")
    protected TaxDocumentName taxDocumentName;
    @XmlElement(name = "TaxDocumentYear")
    protected TaxDocumentYear taxDocumentYear;
    @XmlElement(name = "TaxDocument")
    protected TaxDocument taxDocument;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public TaxDocumentName getTaxDocumentName() {
        return taxDocumentName;
    }

    public void setTaxDocumentName(TaxDocumentName value) {
        this.taxDocumentName = value;
    }

    public TaxDocumentYear getTaxDocumentYear() {
        return taxDocumentYear;
    }

    public void setTaxDocumentYear(TaxDocumentYear value) {
        this.taxDocumentYear = value;
    }

    public TaxDocument getTaxDocument() {
        return taxDocument;
    }

    public void setTaxDocument(TaxDocument value) {
        this.taxDocument = value;
    }

}
