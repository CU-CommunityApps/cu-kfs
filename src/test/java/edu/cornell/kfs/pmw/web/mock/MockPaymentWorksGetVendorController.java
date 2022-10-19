package edu.cornell.kfs.pmw.web.mock;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockPaymentWorksGetVendorController {

    @GetMapping("/getsomething")
    public String getSomething() {
        return "Got it!";
    }

}
