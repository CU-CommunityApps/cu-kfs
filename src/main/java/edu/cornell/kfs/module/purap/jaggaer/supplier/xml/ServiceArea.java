
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
    "serviceAreaInternalName",
    "stateServiceAreaList"
})
@XmlRootElement(name = "ServiceArea")
public class ServiceArea {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "ServiceAreaInternalName", required = true)
    protected ServiceAreaInternalName serviceAreaInternalName;
    @XmlElement(name = "StateServiceAreaList")
    protected List<StateServiceAreaList> stateServiceAreaList;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public ServiceAreaInternalName getServiceAreaInternalName() {
        return serviceAreaInternalName;
    }

    
    public void setServiceAreaInternalName(ServiceAreaInternalName value) {
        this.serviceAreaInternalName = value;
    }

    
    public List<StateServiceAreaList> getStateServiceAreaList() {
        if (stateServiceAreaList == null) {
            stateServiceAreaList = new ArrayList<StateServiceAreaList>();
        }
        return this.stateServiceAreaList;
    }

}
