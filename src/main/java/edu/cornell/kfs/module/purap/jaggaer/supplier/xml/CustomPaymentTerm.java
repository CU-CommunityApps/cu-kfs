
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "CustomPaymentTerm")
public class CustomPaymentTerm {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlAttribute(name = "useCustomPaymentTerm")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useCustomPaymentTerm;
    @XmlAttribute(name = "id", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String id;
    @XmlValue
    protected String value;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public String getUseCustomPaymentTerm() {
        return useCustomPaymentTerm;
    }

    
    public void setUseCustomPaymentTerm(String value) {
        this.useCustomPaymentTerm = value;
    }

    
    public String getId() {
        return id;
    }

    
    public void setId(String value) {
        this.id = value;
    }

    
    public String getvalue() {
        return value;
    }

    
    public void setvalue(String value) {
        this.value = value;
    }

}
