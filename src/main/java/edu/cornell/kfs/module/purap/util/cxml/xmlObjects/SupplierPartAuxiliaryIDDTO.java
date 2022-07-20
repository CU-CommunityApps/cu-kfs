package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contents"
})
@XmlRootElement(name = "SupplierPartAuxiliaryID")
public class SupplierPartAuxiliaryIDDTO {

    @XmlMixed
    @XmlAnyElement
    private List<Object> contents;

    public List<Object> getContents() {
        return contents;
    }

    public void setContents(List<Object> contents) {
        this.contents = contents;
    }

}
