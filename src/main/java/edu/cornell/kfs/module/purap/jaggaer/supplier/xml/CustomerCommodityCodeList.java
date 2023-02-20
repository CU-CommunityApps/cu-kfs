
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
