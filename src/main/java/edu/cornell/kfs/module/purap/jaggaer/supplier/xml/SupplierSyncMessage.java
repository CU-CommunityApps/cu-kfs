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
        "supplierRequestMessageItems" })
@XmlRootElement(name = "SupplierSyncMessage")
public class SupplierSyncMessage {

    @XmlAttribute(name = "version", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String version;
    @XmlElement(name = "Header", required = true)
    private Header header;
    
    /*
     * XJC produced this XML annotation.  We only need SupplierRequestMessage for the upload suppliers functionality.
     * THe following classes can be passed into this collection: SupplierResponseMessage, LookupRequestMessage, and LookupResponseMessage.
     * Those classes have been removed as they aren't required at this time.
     * If they are needed, this object can be updated to include them.  To regenerate the classes and child objects, run the following command
     * 
     * xjc -p edu.cornell.kfs.module.purap.jaggaer.supplier.xml -no-header -dtd TSMSupplierXML.dtd
     */
    @XmlElements({ @XmlElement(name = "SupplierRequestMessage", required = true, type = SupplierRequestMessage.class)})
    private List<SupplierRequestMessageItem> supplierRequestMessageItems;

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

    public List<SupplierRequestMessageItem> getSupplierRequestMessageItems() {
        if (supplierRequestMessageItems == null) {
            supplierRequestMessageItems = new ArrayList<SupplierRequestMessageItem>();
        }
        return supplierRequestMessageItems;
    }

}
