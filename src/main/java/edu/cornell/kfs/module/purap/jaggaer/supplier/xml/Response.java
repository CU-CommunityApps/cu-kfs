
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierRef", "status" })
@XmlRootElement(name = "Response")
public class Response {

    @XmlElement(name = "SupplierRef")
    protected List<SupplierRef> supplierRef;
    @XmlElement(name = "Status", required = true)
    protected Status status;

    public List<SupplierRef> getSupplierRef() {
        if (supplierRef == null) {
            supplierRef = new ArrayList<SupplierRef>();
        }
        return this.supplierRef;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        this.status = value;
    }

}
