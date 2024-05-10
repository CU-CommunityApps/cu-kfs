package edu.cornell.kfs.vnd.batch.service.impl;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.vnd.batch.service.VendorEmployeeSearchService;

public class VendorEmployeeSearchServiceImpl implements VendorEmployeeSearchService {

    private BusinessObjectService businessObjectService;

    @Override
    public void generateFileContainingPotentialVendorEmployees() {
        
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
