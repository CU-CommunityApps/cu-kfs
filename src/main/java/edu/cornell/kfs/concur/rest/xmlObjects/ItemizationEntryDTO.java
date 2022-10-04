package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ItemizationEntryDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemizationEntryDTO {
    
    @XmlElementWrapper(name="AllocationsList")
    @XmlElement(name = "Allocations")
    private List<AllocationsDTO> allocationsList;
    
    @XmlElementWrapper(name="Allocations")
    @XmlElement(name = "Allocations")
    private List<AllocationsDTO> allocations;

    public List<AllocationsDTO> getAllocationsList() {
        return allocationsList;
    }

    public void setAllocationsList(List<AllocationsDTO> allocationsList) {
        this.allocationsList = allocationsList;
    }

    public List<AllocationsDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationsDTO> allocations) {
        this.allocations = allocations;
    }

}
