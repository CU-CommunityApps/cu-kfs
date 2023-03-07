
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fee",
    "useOrderThreshold",
    "orderThreshold"
})
@XmlRootElement(name = "SurchargeConfiguration")
public class SurchargeConfiguration {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Fee")
    protected Fee fee;
    @XmlElement(name = "UseOrderThreshold")
    protected JaggaerBasicValue useOrderThreshold;
    @XmlElement(name = "OrderThreshold")
    protected JaggaerBasicValue orderThreshold;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public Fee getFee() {
        return fee;
    }

    
    public void setFee(Fee value) {
        this.fee = value;
    }

    
    public JaggaerBasicValue getUseOrderThreshold() {
        return useOrderThreshold;
    }

    
    public void setUseOrderThreshold(JaggaerBasicValue value) {
        this.useOrderThreshold = value;
    }

    
    public JaggaerBasicValue getOrderThreshold() {
        return orderThreshold;
    }

    
    public void setOrderThreshold(JaggaerBasicValue value) {
        this.orderThreshold = value;
    }

}
