
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "xop:Include")
public class XopInclude {

    @XmlAttribute(name = "href", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String href;

    
    public String getHref() {
        return href;
    }

    
    public void setHref(String value) {
        this.href = value;
    }

}
