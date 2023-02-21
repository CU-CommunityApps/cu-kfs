
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

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
