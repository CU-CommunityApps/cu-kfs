package edu.cornell.kfs.pmw.web.mock;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksWebServiceConstants;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsRootDTO;

@RestController
public class MockPaymentWorksGetNewVendorsController {

    private String expectedAuthorizationHeader;
    private PaymentWorksNewVendorRequestsRootDTO vendorRequestsDTO;

    public MockPaymentWorksGetNewVendorsController withExpectedAuthorizationToken(String expectedAuthorizationToken) {
        setExpectedAuthorizationToken(expectedAuthorizationToken);
        return this;
    }

    public void setExpectedAuthorizationToken(String expectedAuthorizationToken) {
        this.expectedAuthorizationHeader = PaymentWorksWebServiceConstants.AUTHORIZATION_TOKEN_VALUE_STARTER
                + expectedAuthorizationToken;
    }

    public void setVendorRequestsDTO(PaymentWorksNewVendorRequestsRootDTO vendorRequestsDTO) {
        this.vendorRequestsDTO = vendorRequestsDTO;
    }

    @GetMapping(path = "/new-vendor-requests", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<PaymentWorksNewVendorRequestsRootDTO> getNewVendorRequests(
            @RequestParam(PaymentWorksWebServiceConstants.STATUS) int status,
            @RequestHeader(PaymentWorksWebServiceConstants.AUTHORIZATION_HEADER_KEY) String authorizationHeader) {
        if (!StringUtils.equals(authorizationHeader, expectedAuthorizationHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } else if (status != PaymentWorksConstants.PaymentWorksNewVendorRequestStatusType.APPROVED.code) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(vendorRequestsDTO);
        }
    }

}
