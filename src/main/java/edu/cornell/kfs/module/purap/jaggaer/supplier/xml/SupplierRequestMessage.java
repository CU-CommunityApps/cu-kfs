package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierDetails" })
@XmlRootElement(name = "SupplierRequestMessage")
public class SupplierRequestMessage implements SupplierRequestMessageItemInterface{

    @XmlElement(name = "Supplier", required = true)
    private List<Supplier> supplierDetails;

    public List<Supplier> getSupplierDetails() {
        if (supplierDetails == null) {
            supplierDetails = new ArrayList<Supplier>();
        }
        return supplierDetails;
    }

}
