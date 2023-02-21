
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
    protected MessageId messageId;
    @XmlElement(name = "RelatedMessageId")
    protected RelatedMessageId relatedMessageId;
    @XmlElement(name = "Timestamp", required = true)
    protected Timestamp timestamp;
    @XmlElement(name = "Authentication")
    protected Authentication authentication;

    public MessageId getMessageId() {
        return messageId;
    }

    public void setMessageId(MessageId value) {
        this.messageId = value;
    }

    public RelatedMessageId getRelatedMessageId() {
        return relatedMessageId;
    }

    public void setRelatedMessageId(RelatedMessageId value) {
        this.relatedMessageId = value;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp value) {
        this.timestamp = value;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication value) {
        this.authentication = value;
    }

}
