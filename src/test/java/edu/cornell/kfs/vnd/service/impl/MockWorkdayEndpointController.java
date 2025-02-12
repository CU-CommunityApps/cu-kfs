package edu.cornell.kfs.vnd.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;
import edu.cornell.kfs.vnd.service.impl.fixture.CuVendorWorkdayServiceEnum;

@RestController
public class MockWorkdayEndpointController {
    private static final Logger LOG = LogManager.getLogger();

    @GetMapping(
            path = "service/customreport2/cornell/intsys-HRIS/CRINT127C_KFS_Vendor_Lookup", 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WorkdayKfsVendorLookupRoot> findEmployee(HttpServletRequest request,
            @RequestParam(CuVendorWorkDayServiceImpl.INCLUDE_TERMINATED_WORKERS_URL_PARAM) String includeTerminatedWorkers,
            @RequestParam(CuVendorWorkDayServiceImpl.SOCIAL_SECURITY_NUMBER_URL_PARAM) String ssn) {
        LOG.info("findEmployee, entering, includeTerminatedWorkers: {}, ssn: {}", includeTerminatedWorkers, ssn);
        CuVendorWorkdayServiceEnum serviceEnum = CuVendorWorkdayServiceEnum.findCuVendorWorkdayServiceEnum(includeTerminatedWorkers, ssn);
        return ResponseEntity.ok(serviceEnum.toWorkdayKfsVendorLookupRoot());

    }

}
