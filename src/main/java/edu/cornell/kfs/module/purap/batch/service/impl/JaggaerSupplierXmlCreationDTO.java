package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JaggaerSupplierXmlCreationDTO {
    
    private String vendorNumber;
    private boolean isActive;
    private List<String> notes;
    
    public JaggaerSupplierXmlCreationDTO(String vendorNumber, boolean isActive) {
        this.vendorNumber = vendorNumber;
        this.isActive = isActive;
        notes = new ArrayList<String>();
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
