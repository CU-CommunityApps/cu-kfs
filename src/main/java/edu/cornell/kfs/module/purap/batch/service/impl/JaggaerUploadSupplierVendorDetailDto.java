package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JaggaerUploadSupplierVendorDetailDto {
    
    private String vendorNumber;
    private String vendorName;
    private boolean isActive;
    private List<String> notes;
    
    public JaggaerUploadSupplierVendorDetailDto(String vendorNumber, String vendorName, boolean isActive) {
        this.vendorNumber = vendorNumber;
        this.vendorName = vendorName;
        this.isActive = isActive;
        notes = new ArrayList<String>();
    }

    public String getVendorNumber() {
        return vendorNumber;
    }

    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
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
    
    public boolean hasNotes() {
        return CollectionUtils.isNotEmpty(notes);
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
