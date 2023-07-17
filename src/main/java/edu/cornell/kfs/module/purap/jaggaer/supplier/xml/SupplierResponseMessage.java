package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "status", "supplierErrors" })
@XmlRootElement(name = "SupplierResponseMessage")
public class SupplierResponseMessage implements SupplierSyncMessageItem {

    @XmlElement(name = "Status", required = true)
    private Status status;
    @XmlElement(name = "SupplierErrors")
    private List<SupplierError> supplierErrors;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<SupplierError> getSupplierErrors() {
        if (supplierErrors == null) {
            supplierErrors = new ArrayList<SupplierError>();
        }
        return supplierErrors;
    }

}
