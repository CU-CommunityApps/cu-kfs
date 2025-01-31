package edu.cornell.kfs.vnd.service;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;

public interface CuVendorWorkDayService {
    
    public WorkdayKfsVendorLookupResult findEmployeeBySocialSecurityNumber(String socialSecurityNumber);  

}
