package edu.cornell.kfs.vnd.service;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public interface CuVendorWorkDayService {
    
    public WorkdayKfsVendorLookupRoot findEmployeeBySocialSecurityNumber(String socialSecurityNumber);  

}
