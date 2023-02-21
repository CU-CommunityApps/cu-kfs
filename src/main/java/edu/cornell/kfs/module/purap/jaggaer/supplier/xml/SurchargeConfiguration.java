
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "fee", "useOrderThreshold", "orderThreshold" })
@XmlRootElement(name = "SurchargeConfiguration")
public class SurchargeConfiguration {

    @XmlElement(name = "Fee", required = true)
    protected Fee fee;
    @XmlElement(name = "UseOrderThreshold")
    protected UseOrderThreshold useOrderThreshold;
    @XmlElement(name = "OrderThreshold")
    protected OrderThreshold orderThreshold;

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee value) {
        this.fee = value;
    }

    public UseOrderThreshold getUseOrderThreshold() {
        return useOrderThreshold;
    }

    public void setUseOrderThreshold(UseOrderThreshold value) {
        this.useOrderThreshold = value;
    }

    public OrderThreshold getOrderThreshold() {
        return orderThreshold;
    }

    public void setOrderThreshold(OrderThreshold value) {
        this.orderThreshold = value;
    }

}
