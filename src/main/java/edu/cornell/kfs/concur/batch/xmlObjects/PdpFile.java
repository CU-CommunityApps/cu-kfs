package edu.cornell.kfs.concur.batch.xmlObjects;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "header",
    "group",
    "trailer"
})
@XmlRootElement(name = "pdp_file", namespace = "http://www.kuali.org/kfs/pdp/payment")
public class PdpFile {

    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected Header header;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected List<Group> group;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected Trailer trailer;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header value) {
        this.header = value;
    }

    public List<Group> getGroup() {
        if (group == null) {
            group = new ArrayList<Group>();
        }
        return this.group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }

    public Trailer getTrailer() {
        return trailer;
    }

    public void setTrailer(Trailer value) {
        this.trailer = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

}
