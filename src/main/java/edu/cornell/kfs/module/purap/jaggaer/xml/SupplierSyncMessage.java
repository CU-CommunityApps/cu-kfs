package edu.cornell.kfs.module.purap.jaggaer.xml;

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
@XmlType(name = "", propOrder = { "header",
        "supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage" })
@XmlRootElement(name = "SupplierSyncMessage")
public class SupplierSyncMessage {

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlElement(name = "Header", required = true)
    protected Header header;
    /*
     * Jaggaer's supplier.dtd generated SupplierResponseMessage, LookupRequestMessage and LookupResponseMessage 
     * in addition to SupplierRequestMessage.  The jaggaer upload supplier with XML integration only needs 
     * SupplierRequestMessage.  Therefore the other types will be added yet.
     * If and when those POJOs are needed, they can be generated with XJC using the following command
     * xjc -p edu.cornell.kfs.module.purap.jaggaer.supplier.xml -no-header -dtd supplier.dtd
     */
    @XmlElements({ @XmlElement(name = "SupplierRequestMessage", required = true, type = SupplierRequestMessage.class)})
    protected List<Object> supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage;

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

    public List<Object> getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage() {
        if (supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage == null) {
            supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage = new ArrayList<Object>();
        }
        return this.supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage;
    }

}
