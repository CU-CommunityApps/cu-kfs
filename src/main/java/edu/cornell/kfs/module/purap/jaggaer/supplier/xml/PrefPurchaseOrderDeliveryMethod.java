package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "deliveryMethodTypes" })
@XmlRootElement(name = "PrefPurchaseOrderDeliveryMethod")
public class PrefPurchaseOrderDeliveryMethod {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlAttribute(name = "type")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;
    @XmlElements({ @XmlElement(name = "Email", type = Email.class), @XmlElement(name = "Fax", type = Fax.class) })
    private List<PurchaseOrderDeliveryMethodType> deliveryMethodTypes;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PurchaseOrderDeliveryMethodType> getDeliveryMethodTypes() {
        if (deliveryMethodTypes == null) {
            deliveryMethodTypes = new ArrayList<PurchaseOrderDeliveryMethodType>();
        }
        return this.deliveryMethodTypes;
    }

}
