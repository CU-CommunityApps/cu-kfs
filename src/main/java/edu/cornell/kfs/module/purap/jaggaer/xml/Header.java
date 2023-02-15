package edu.cornell.kfs.module.purap.jaggaer.xml;

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
    protected String messageId;
    @XmlElement(name = "RelatedMessageId")
    protected String relatedMessageId;
    @XmlElement(name = "Timestamp", required = true)
    protected String timestamp;
    @XmlElement(name = "Authentication")
    protected Authentication authentication;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String value) {
        this.messageId = value;
    }

    public String getRelatedMessageId() {
        return relatedMessageId;
    }

    public void setRelatedMessageId(String value) {
        this.relatedMessageId = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String value) {
        this.timestamp = value;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication value) {
        this.authentication = value;
    }

}
