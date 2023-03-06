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

/**
 * 
 */
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

    /**
     * Gets the value of the version property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the header property.
     * 
     * @return possible object is {@link Header }
     * 
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value allowed object is {@link Header }
     * 
     */
    public void setHeader(Header value) {
        this.header = value;
    }

    /**
     * Gets the value of the
     * supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage
     * property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the
     * supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage
     * property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupplierRequestMessage } {@link SupplierResponseMessage }
     * {@link LookupRequestMessage } {@link LookupResponseMessage }
     * 
     * 
     */
    public List<Object> getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage() {
        if (supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage == null) {
            supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage = new ArrayList<Object>();
        }
        return this.supplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage;
    }

}
