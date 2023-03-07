
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    protected JaggaerBasicValue taxDocumentName;
    @XmlElement(name = "TaxDocumentYear")
    protected JaggaerBasicValue taxDocumentYear;
    @XmlElement(name = "TaxDocument")
    protected TaxDocument taxDocument;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getTaxDocumentName() {
        return taxDocumentName;
    }

    public void setTaxDocumentName(JaggaerBasicValue taxDocumentName) {
        this.taxDocumentName = taxDocumentName;
    }

    public JaggaerBasicValue getTaxDocumentYear() {
        return taxDocumentYear;
    }

    public void setTaxDocumentYear(JaggaerBasicValue taxDocumentYear) {
        this.taxDocumentYear = taxDocumentYear;
    }

    public TaxDocument getTaxDocument() {
        return taxDocument;
    }

    public void setTaxDocument(TaxDocument taxDocument) {
        this.taxDocument = taxDocument;
    }

}
