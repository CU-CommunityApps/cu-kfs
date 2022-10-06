package edu.cornell.kfs.concur.rest.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ExpenseEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExpenseEntryDTO {

    @XmlElementWrapper(name = "ItemizationsList")
    @XmlElement(name = "ItemizationEntryDetails")
    private List<ItemizationEntryDTO> itemizationsList;
       
    @XmlElement(name = "IsPersonal")
    private String isPersonal;
    
    @XmlElement(name = "IsCreditCardCharge")
    private String isCreditCardCharge;
    
    @XmlElement(name = "Allocations")
    private List<AllocationsDTO> allocations;

    public List<ItemizationEntryDTO> getItemizationsList() {
        return itemizationsList;
    }

    public void setItemizationsList(List<ItemizationEntryDTO> itemizationsList) {
        this.itemizationsList = itemizationsList;
    }
    
    public String getIsPersonal() {
        return isPersonal;
    }

    public void setIsPersonal(String isPersonal) {
        this.isPersonal = isPersonal;
    }
    
    public List<AllocationsDTO> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<AllocationsDTO> allocations) {
        this.allocations = allocations;
    }

    public String getIsCreditCardCharge() {
        return isCreditCardCharge;
    }

    public void setIsCreditCardCharge(String isCreditCardCharge) {
        this.isCreditCardCharge = isCreditCardCharge;
    }

}
