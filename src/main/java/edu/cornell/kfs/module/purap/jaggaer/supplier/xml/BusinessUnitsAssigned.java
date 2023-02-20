
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "businessUnit" })
@XmlRootElement(name = "BusinessUnitsAssigned")
public class BusinessUnitsAssigned {

    @XmlElement(name = "BusinessUnit")
    protected List<BusinessUnit> businessUnit;

    public List<BusinessUnit> getBusinessUnit() {
        if (businessUnit == null) {
            businessUnit = new ArrayList<BusinessUnit>();
        }
        return this.businessUnit;
    }

}
