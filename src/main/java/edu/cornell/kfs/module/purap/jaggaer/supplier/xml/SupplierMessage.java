package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "header", "supplierOrResponse" })
@XmlRootElement(name = "SupplierMessage")
public class SupplierMessage {

    @XmlAttribute(name = "version")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlElement(name = "Header", required = true)
    protected Header header;
    @XmlElements({ @XmlElement(name = "Supplier", required = true, type = Supplier.class),
            @XmlElement(name = "Response", required = true, type = Response.class) })
    protected List<Object> supplierOrResponse;

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header value) {
        this.header = value;
    }

    public List<Object> getSupplierOrResponse() {
        if (supplierOrResponse == null) {
            supplierOrResponse = new ArrayList<Object>();
        }
        return this.supplierOrResponse;
    }

}
