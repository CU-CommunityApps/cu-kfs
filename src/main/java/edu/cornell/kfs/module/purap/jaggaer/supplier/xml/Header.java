package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "messageId", "relatedMessageId", "timestamp", "authentication" })
@XmlRootElement(name = "Header")
public class Header {

    @XmlElement(name = "MessageId", required = true)
    private String messageId;
    @XmlElement(name = "RelatedMessageId")
    private String relatedMessageId;
    @XmlElement(name = "Timestamp", required = true)
    private String timestamp;
    @XmlElement(name = "Authentication")
    private Authentication authentication;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRelatedMessageId() {
        return relatedMessageId;
    }

    public void setRelatedMessageId(String relatedMessageId) {
        this.relatedMessageId = relatedMessageId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

}
