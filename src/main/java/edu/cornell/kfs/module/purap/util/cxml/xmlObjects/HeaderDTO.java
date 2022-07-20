package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "from",
    "to",
    "sender",
    "path",
    "originalDocument"
})
@XmlRootElement(name = "Header")
public class HeaderDTO {

    @XmlElement(name = "From", required = true)
    private FromDTO from;

    @XmlElement(name = "To", required = true)
    private ToDTO to;

    @XmlElement(name = "Sender", required = true)
    private SenderDTO sender;

    @XmlElement(name = "Path")
    private PathDTO path;

    @XmlElement(name = "OriginalDocument")
    private OriginalDocumentDTO originalDocument;

    public FromDTO getFrom() {
        return from;
    }

    public void setFrom(FromDTO from) {
        this.from = from;
    }

    public ToDTO getTo() {
        return to;
    }

    public void setTo(ToDTO to) {
        this.to = to;
    }

    public SenderDTO getSender() {
        return sender;
    }

    public void setSender(SenderDTO sender) {
        this.sender = sender;
    }

    public PathDTO getPath() {
        return path;
    }

    public void setPath(PathDTO path) {
        this.path = path;
    }

    public OriginalDocumentDTO getOriginalDocument() {
        return originalDocument;
    }

    public void setOriginalDocument(OriginalDocumentDTO originalDocument) {
        this.originalDocument = originalDocument;
    }

}
