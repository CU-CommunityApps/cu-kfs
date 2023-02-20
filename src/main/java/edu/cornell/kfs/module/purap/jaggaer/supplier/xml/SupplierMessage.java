
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
