package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "suppliers" })
@XmlRootElement(name = "SupplierRequestMessage")
public class SupplierRequestMessage implements SupplierSyncMessageItem {

    @XmlElement(name = "Supplier", required = true)
    private List<Supplier> suppliers;

    public List<Supplier> getSuppliers() {
        if (suppliers == null) {
            suppliers = new ArrayList<Supplier>();
        }
        return suppliers;
    }

}
