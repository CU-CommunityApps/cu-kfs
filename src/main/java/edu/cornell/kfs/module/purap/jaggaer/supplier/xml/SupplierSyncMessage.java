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
@XmlType(name = "", propOrder = { "header",
        "supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage" })
@XmlRootElement(name = "SupplierSyncMessage")
public class SupplierSyncMessage {

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String version;
    @XmlElement(name = "Header", required = true)
    protected Header header;
    @XmlElements({ @XmlElement(name = "SupplierRequestMessage", required = true, type = SupplierRequestMessage.class),
            @XmlElement(name = "SupplierResponseMessage", required = true, type = SupplierResponseMessage.class),
            @XmlElement(name = "LookupRequestMessage", required = true, type = LookupRequestMessage.class),
            @XmlElement(name = "LookupResponseMessage", required = true, type = LookupResponseMessage.class) })
    protected List<Object> supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public List<Object> getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage() {
        if (supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage == null) {
            supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage = new ArrayList<Object>();
        }
        return this.supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage;
    }

}
