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
public class PdpFeedFileBaseEntry {

    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected PdpFeedHeaderEntry header;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected List<PdpFeedGroupEntry> group;
    @XmlElement(namespace = "http://www.kuali.org/kfs/pdp/payment", required = true)
    protected PdpFeedTrailerEntry trailer;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    public PdpFeedHeaderEntry getHeader() {
        return header;
    }

    public void setHeader(PdpFeedHeaderEntry value) {
        this.header = value;
    }

    public List<PdpFeedGroupEntry> getGroup() {
        if (group == null) {
            group = new ArrayList<PdpFeedGroupEntry>();
        }
        return this.group;
    }

    public void setGroup(List<PdpFeedGroupEntry> group) {
        this.group = group;
    }

    public PdpFeedTrailerEntry getTrailer() {
        return trailer;
    }

    public void setTrailer(PdpFeedTrailerEntry value) {
        this.trailer = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        this.version = value;
    }

}
