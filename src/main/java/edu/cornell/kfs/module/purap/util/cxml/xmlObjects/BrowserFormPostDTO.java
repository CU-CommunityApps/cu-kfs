package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "url"
})
@XmlRootElement(name = "BrowserFormPost")
public class BrowserFormPostDTO {

    @XmlElement(name = "URL", required = true)
    private UrlDTO url;

    public UrlDTO getUrl() {
        return url;
    }

    public void setUrl(UrlDTO url) {
        this.url = url;
    }

}
