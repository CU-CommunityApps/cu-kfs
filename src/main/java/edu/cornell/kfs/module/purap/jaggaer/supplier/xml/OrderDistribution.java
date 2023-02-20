
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "language", "distributionMethod" })
@XmlRootElement(name = "OrderDistribution")
public class OrderDistribution {

    @XmlElement(name = "Language")
    protected Language language;
    @XmlElement(name = "DistributionMethod", required = true)
    protected List<DistributionMethod> distributionMethod;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language value) {
        this.language = value;
    }

    public List<DistributionMethod> getDistributionMethod() {
        if (distributionMethod == null) {
            distributionMethod = new ArrayList<DistributionMethod>();
        }
        return this.distributionMethod;
    }

}
