
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
@XmlType(name = "", propOrder = {
    "annualSales"
})
@XmlRootElement(name = "AnnualSalesList")
public class AnnualSalesList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "AnnualSales")
    protected List<AnnualSales> annualSales;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public List<AnnualSales> getAnnualSales() {
        if (annualSales == null) {
            annualSales = new ArrayList<AnnualSales>();
        }
        return this.annualSales;
    }

}
