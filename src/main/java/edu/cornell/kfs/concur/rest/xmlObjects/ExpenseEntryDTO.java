package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExpenseEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpenseEntryDTO {

    @XmlElementWrapper(name = "ItemizationsList")
    @XmlElement(name = "ItemizationEntryDetails")
    private List<ItemizationEntryDTO> itemizationsList;

    @XmlElement(name = "OrgUnit6")
    private String orgUnit6;
    
    @XmlElementWrapper(name = "Allocations")
    @XmlElement(name = "Allocations")
    private List<AllocationsDTO> allocations;

    public List<ItemizationEntryDTO> getItemizationsList() {
        return itemizationsList;
    }

    public void setItemizationsList(List<ItemizationEntryDTO> itemizationsList) {
        this.itemizationsList = itemizationsList;
    }

    public String getOrgUnit6() {
        return orgUnit6;
    }

    public void setOrgUnit6(String orgUnit6) {
        this.orgUnit6 = orgUnit6;
    }
    
    public List<AllocationsDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationsDTO> allocations) {
        this.allocations = allocations;
    }

}
