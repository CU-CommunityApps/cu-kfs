
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "defaultExpirationDays", "cardName" })
@XmlRootElement(name = "VPaymentInfo")
public class VPaymentInfo {

    @XmlElement(name = "DefaultExpirationDays", required = true)
    protected DefaultExpirationDays defaultExpirationDays;
    @XmlElement(name = "CardName")
    protected CardName cardName;

    public DefaultExpirationDays getDefaultExpirationDays() {
        return defaultExpirationDays;
    }

    public void setDefaultExpirationDays(DefaultExpirationDays value) {
        this.defaultExpirationDays = value;
    }

    public CardName getCardName() {
        return cardName;
    }

    public void setCardName(CardName value) {
        this.cardName = value;
    }

}
