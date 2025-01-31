package edu.cornell.kfs.vnd.service.impl;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupResult;
import edu.cornell.kfs.vnd.service.CuVendorWorkDayService;

public class CuVendorWorkDayServiceImpl implements CuVendorWorkDayService {

    @Override
    public WorkdayKfsVendorLookupResult findEmployeeBySocialSecurityNumber(String socialSecurityNumber) {
        throw new RuntimeException("Calling workday enpoint is not implemented yet.");
    }

}
