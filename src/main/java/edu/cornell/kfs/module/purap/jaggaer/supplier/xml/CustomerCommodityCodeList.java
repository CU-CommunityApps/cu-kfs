
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "customerCommodityCode" })
@XmlRootElement(name = "CustomerCommodityCodeList")
public class CustomerCommodityCodeList {

    @XmlElement(name = "CustomerCommodityCode")
    protected List<CustomerCommodityCode> customerCommodityCode;

    public List<CustomerCommodityCode> getCustomerCommodityCode() {
        if (customerCommodityCode == null) {
            customerCommodityCode = new ArrayList<CustomerCommodityCode>();
        }
        return this.customerCommodityCode;
    }

}
