package edu.cornell.kfs.vnd.service;

import java.net.URISyntaxException;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public interface CuVendorWorkDayService {
    
    public WorkdayKfsVendorLookupRoot findEmployeeBySocialSecurityNumber(String socialSecurityNumber) throws URISyntaxException;  

}
