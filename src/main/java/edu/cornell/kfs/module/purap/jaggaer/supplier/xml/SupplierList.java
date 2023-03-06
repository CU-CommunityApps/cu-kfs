
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "supplierSQId"
})
@XmlRootElement(name = "SupplierList")
public class SupplierList {

    @XmlElement(name = "SupplierSQId", required = true)
    protected List<SupplierSQId> supplierSQId;

    
    public List<SupplierSQId> getSupplierSQId() {
        if (supplierSQId == null) {
            supplierSQId = new ArrayList<SupplierSQId>();
        }
        return this.supplierSQId;
    }

}
