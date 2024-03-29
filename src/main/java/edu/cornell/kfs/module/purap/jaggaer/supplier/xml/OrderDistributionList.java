package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "distributionLanguage", "distributionMethods" })
@XmlRootElement(name = "OrderDistributionList")
public class OrderDistributionList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "DistributionLanguage")
    private String distributionLanguage;
    @XmlElement(name = "DistributionMethod", required = true)
    private List<DistributionMethod> distributionMethods;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public String getDistributionLanguage() {
        return distributionLanguage;
    }

    public void setDistributionLanguage(String distributionLanguage) {
        this.distributionLanguage = distributionLanguage;
    }

    public List<DistributionMethod> getDistributionMethods() {
        if (distributionMethods == null) {
            distributionMethods = new ArrayList<DistributionMethod>();
        }
        return distributionMethods;
    }

}
