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
@XmlType(name = "", propOrder = { "businessUnitInternalName" })
@XmlRootElement(name = "AssignedBusinessUnitsList")
public class AssignedBusinessUnitsList {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "BusinessUnitInternalName")
    protected List<BusinessUnitInternalName> businessUnitInternalName;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public List<BusinessUnitInternalName> getBusinessUnitInternalName() {
        if (businessUnitInternalName == null) {
            businessUnitInternalName = new ArrayList<BusinessUnitInternalName>();
        }
        return this.businessUnitInternalName;
    }

}
