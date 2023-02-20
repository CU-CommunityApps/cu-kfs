
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "currency" })
@XmlRootElement(name = "SupportedCurrencies")
public class SupportedCurrencies {

    @XmlElement(name = "Currency")
    protected List<Currency> currency;

    public List<Currency> getCurrency() {
        if (currency == null) {
            currency = new ArrayList<Currency>();
        }
        return this.currency;
    }

}
