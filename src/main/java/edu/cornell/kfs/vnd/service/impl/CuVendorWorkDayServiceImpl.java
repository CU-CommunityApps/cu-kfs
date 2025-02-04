package edu.cornell.kfs.vnd.service.impl;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;
import edu.cornell.kfs.vnd.service.CuVendorWorkDayService;

public class CuVendorWorkDayServiceImpl implements CuVendorWorkDayService {

    @Override
    public WorkdayKfsVendorLookupRoot findEmployeeBySocialSecurityNumber(String socialSecurityNumber) {
        throw new RuntimeException("Calling workday endpoint is not implemented yet.");
    }

}
