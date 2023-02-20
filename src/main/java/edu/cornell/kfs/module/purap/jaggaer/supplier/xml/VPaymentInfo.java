
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
