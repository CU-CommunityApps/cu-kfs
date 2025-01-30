package edu.cornell.kfs.vnd.service;

import edu.cornell.kfs.vnd.businessobject.WorkdayKfsVendorLooupResultsDTO;

public interface CuVendorWorkDayService {
    
    public WorkdayKfsVendorLooupResultsDTO findEmployeeBySocialSecurityNumber(String socialSecurityNumber);  

}
