
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "cardName", "cardNumber", "expirationDate" })
@XmlRootElement(name = "CreditCard")
public class CreditCard {

    @XmlElement(name = "CardName", required = true)
    protected CardName cardName;
    @XmlElement(name = "CardNumber", required = true)
    protected String cardNumber;
    @XmlElement(name = "ExpirationDate", required = true)
    protected String expirationDate;

    public CardName getCardName() {
        return cardName;
    }

    public void setCardName(CardName value) {
        this.cardName = value;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String value) {
        this.cardNumber = value;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String value) {
        this.expirationDate = value;
    }

}
