package edu.cornell.kfs.module.purap.util.cxml.xmlObjects;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class IgnoredElementDTO {

    @XmlAnyAttribute
    private Map<QName, Object> attributes;

    @XmlAnyElement
    private List<Element> elements;

    public Map<QName, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<QName, Object> attributes) {
        this.attributes = attributes;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

}
