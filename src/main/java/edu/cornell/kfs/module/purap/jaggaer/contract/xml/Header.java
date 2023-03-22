package edu.cornell.kfs.module.purap.jaggaer.contract.xml;

import java.util.Date;

import edu.cornell.kfs.sys.xmladapters.ZonedStringToJavaDateXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "messageId",
    "timestamp",
    "authentication"
})
@XmlRootElement(name = "Header")
public class Header {

    @XmlElement(name = "MessageId", required = true)
    private String messageId;

    @XmlElement(name = "Timestamp", required = true)
    @XmlJavaTypeAdapter(ZonedStringToJavaDateXmlAdapter.class)
    private Date timestamp;

    @XmlElement(name = "Authentication")
    private Authentication authentication;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

}
