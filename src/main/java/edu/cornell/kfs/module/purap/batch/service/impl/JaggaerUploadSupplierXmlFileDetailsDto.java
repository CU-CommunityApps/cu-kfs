package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JaggaerUploadSupplierXmlFileDetailsDto {
    private String xmlFileName;
    private List<JaggaerUploadSupplierVendorDetailDto> activeVendors;
    private List<JaggaerUploadSupplierVendorDetailDto> inactiveVendors;
    
    public JaggaerUploadSupplierXmlFileDetailsDto(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        this.activeVendors = new ArrayList<JaggaerUploadSupplierVendorDetailDto>();
        this.inactiveVendors = new ArrayList<JaggaerUploadSupplierVendorDetailDto>();
    }
    
    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public List<JaggaerUploadSupplierVendorDetailDto> getActiveVendors() {
        return activeVendors;
    }

    public List<JaggaerUploadSupplierVendorDetailDto> getInactiveVendors() {
        return inactiveVendors;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
