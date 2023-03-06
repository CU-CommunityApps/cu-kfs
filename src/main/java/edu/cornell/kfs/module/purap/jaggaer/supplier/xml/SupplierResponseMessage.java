
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
    "status",
    "supplierErrors"
})
@XmlRootElement(name = "SupplierResponseMessage")
public class SupplierResponseMessage {

    @XmlElement(name = "Status", required = true)
    protected Status status;
    @XmlElement(name = "SupplierErrors")
    protected List<SupplierErrors> supplierErrors;

    
    public Status getStatus() {
        return status;
    }

    
    public void setStatus(Status value) {
        this.status = value;
    }

    
    public List<SupplierErrors> getSupplierErrors() {
        if (supplierErrors == null) {
            supplierErrors = new ArrayList<SupplierErrors>();
        }
        return this.supplierErrors;
    }

}
