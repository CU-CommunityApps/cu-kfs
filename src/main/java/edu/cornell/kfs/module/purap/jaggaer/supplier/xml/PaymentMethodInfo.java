
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "active", "cardSecurityCodeRequired", "poNumberSelection",
        "vPaymentInfoOrGhostCardInfoOrBlanketPOName" })
@XmlRootElement(name = "PaymentMethodInfo")
public class PaymentMethodInfo {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String type;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "CardSecurityCodeRequired")
    protected CardSecurityCodeRequired cardSecurityCodeRequired;
    @XmlElement(name = "PONumberSelection")
    protected PONumberSelection poNumberSelection;
    @XmlElements({ @XmlElement(name = "VPaymentInfo", type = VPaymentInfo.class),
            @XmlElement(name = "GhostCardInfo", type = GhostCardInfo.class),
            @XmlElement(name = "BlanketPOName", type = BlanketPOName.class) })
    protected List<Object> vPaymentInfoOrGhostCardInfoOrBlanketPOName;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public CardSecurityCodeRequired getCardSecurityCodeRequired() {
        return cardSecurityCodeRequired;
    }

    public void setCardSecurityCodeRequired(CardSecurityCodeRequired value) {
        this.cardSecurityCodeRequired = value;
    }

    public PONumberSelection getPONumberSelection() {
        return poNumberSelection;
    }

    public void setPONumberSelection(PONumberSelection value) {
        this.poNumberSelection = value;
    }

    public List<Object> getVPaymentInfoOrGhostCardInfoOrBlanketPOName() {
        if (vPaymentInfoOrGhostCardInfoOrBlanketPOName == null) {
            vPaymentInfoOrGhostCardInfoOrBlanketPOName = new ArrayList<Object>();
        }
        return this.vPaymentInfoOrGhostCardInfoOrBlanketPOName;
    }

}
