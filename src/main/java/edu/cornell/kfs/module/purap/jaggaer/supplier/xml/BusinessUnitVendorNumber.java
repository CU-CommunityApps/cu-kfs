
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "BusinessUnitVendorNumber")
public class BusinessUnitVendorNumber {

    @XmlAttribute(name = "businessUnitInternalName", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String businessUnitInternalName;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlValue
    protected String value;

    
    public String getBusinessUnitInternalName() {
        return businessUnitInternalName;
    }

    
    public void setBusinessUnitInternalName(String value) {
        this.businessUnitInternalName = value;
    }

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public String getvalue() {
        return value;
    }

    
    public void setvalue(String value) {
        this.value = value;
    }

}
