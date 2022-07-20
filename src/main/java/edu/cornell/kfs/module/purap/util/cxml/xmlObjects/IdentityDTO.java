package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contents"
})
@XmlRootElement(name = "Identity")
public class IdentityDTO {

    @XmlAttribute(name = "lastChangedTimestamp")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String lastChangedTimestamp;

    @XmlMixed
    @XmlAnyElement
    private List<Object> contents;

    public String getLastChangedTimestamp() {
        return lastChangedTimestamp;
    }

    public void setLastChangedTimestamp(String lastChangedTimestamp) {
        this.lastChangedTimestamp = lastChangedTimestamp;
    }

    public List<Object> getContents() {
        return contents;
    }

    public void setContents(List<Object> contents) {
        this.contents = contents;
    }

}
