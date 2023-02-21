
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "paymentMethodInfo" })
@XmlRootElement(name = "PaymentMethod")
public class PaymentMethod {

    @XmlElement(name = "PaymentMethodInfo", required = true)
    protected List<PaymentMethodInfo> paymentMethodInfo;

    public List<PaymentMethodInfo> getPaymentMethodInfo() {
        if (paymentMethodInfo == null) {
            paymentMethodInfo = new ArrayList<PaymentMethodInfo>();
        }
        return this.paymentMethodInfo;
    }

}
