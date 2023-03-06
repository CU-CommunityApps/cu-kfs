
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplier" })
@XmlRootElement(name = "SupplierRequestMessage")
public class SupplierRequestMessage {

    @XmlElement(name = "Supplier", required = true)
    protected List<Supplier> supplier;

    public List<Supplier> getSupplier() {
        if (supplier == null) {
            supplier = new ArrayList<Supplier>();
        }
        return this.supplier;
    }

}
