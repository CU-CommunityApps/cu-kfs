
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
@XmlType(name = "", propOrder = { "serviceArea", "stateServiceArea" })
@XmlRootElement(name = "GeographicServiceArea")
public class GeographicServiceArea {

    @XmlAttribute(name = "useInherited")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String useInherited;
    @XmlElement(name = "ServiceArea")
    protected List<ServiceArea> serviceArea;
    @XmlElement(name = "StateServiceArea")
    protected List<StateServiceArea> stateServiceArea;

    public String getUseInherited() {
        return useInherited;
    }

    public void setUseInherited(String value) {
        this.useInherited = value;
    }

    public List<ServiceArea> getServiceArea() {
        if (serviceArea == null) {
            serviceArea = new ArrayList<ServiceArea>();
        }
        return this.serviceArea;
    }

    public List<StateServiceArea> getStateServiceArea() {
        if (stateServiceArea == null) {
            stateServiceArea = new ArrayList<StateServiceArea>();
        }
        return this.stateServiceArea;
    }

}
