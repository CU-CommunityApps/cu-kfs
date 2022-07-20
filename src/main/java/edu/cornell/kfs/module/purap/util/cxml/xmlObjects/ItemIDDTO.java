package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "supplierPartID",
    "supplierPartAuxiliaryID"
})
@XmlRootElement(name = "ItemID")
public class ItemIDDTO {

    @XmlElement(name = "SupplierPartID", required = true)
    private String supplierPartID;

    @XmlElement(name = "SupplierPartAuxiliaryID")
    private SupplierPartAuxiliaryIDDTO supplierPartAuxiliaryID;

    public String getSupplierPartID() {
        return supplierPartID;
    }

    public void setSupplierPartID(String supplierPartID) {
        this.supplierPartID = supplierPartID;
    }

    public SupplierPartAuxiliaryIDDTO getSupplierPartAuxiliaryID() {
        return supplierPartAuxiliaryID;
    }

    public void setSupplierPartAuxiliaryID(SupplierPartAuxiliaryIDDTO supplierPartAuxiliaryID) {
        this.supplierPartAuxiliaryID = supplierPartAuxiliaryID;
    }

}
