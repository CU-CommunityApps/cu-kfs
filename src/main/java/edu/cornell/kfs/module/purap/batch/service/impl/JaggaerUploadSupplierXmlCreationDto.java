package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class JaggaerUploadSupplierXmlCreationDto {
    private String xmlFileName;
    private List<JaggaerSupplierXmlCreationDTO> activeSuppliers;
    private List<JaggaerSupplierXmlCreationDTO> inactiveSuppliers;
    
    public String getXmlFileName() {
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public List<JaggaerSupplierXmlCreationDTO> getActiveSuppliers() {
        if (activeSuppliers == null) {
            activeSuppliers = new ArrayList<JaggaerSupplierXmlCreationDTO>();
        }
        return activeSuppliers;
    }

    public List<JaggaerSupplierXmlCreationDTO> getInactiveSuppliers() {
        if (inactiveSuppliers == null) {
            inactiveSuppliers = new ArrayList<JaggaerSupplierXmlCreationDTO>();
        }
        return inactiveSuppliers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
