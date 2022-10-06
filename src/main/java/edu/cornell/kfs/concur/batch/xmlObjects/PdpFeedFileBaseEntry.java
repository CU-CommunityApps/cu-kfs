package edu.cornell.kfs.concur.batch.xmlObjects;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import edu.cornell.kfs.concur.ConcurConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "header",
    "group",
    "trailer"
})
@XmlRootElement(name = "pdp_file", namespace = ConcurConstants.PDP_XML_NAMESPACE)
public class PdpFeedFileBaseEntry {

    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected PdpFeedHeaderEntry header;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
    protected List<PdpFeedGroupEntry> group;
    @XmlElement(namespace = ConcurConstants.PDP_XML_NAMESPACE, required = true)
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
