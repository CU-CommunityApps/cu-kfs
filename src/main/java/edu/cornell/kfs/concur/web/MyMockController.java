package edu.cornell.kfs.concur.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller should get excluded by "cu-spring-kfs-mvc.xml" because it has "mock" in the classname.
 */
@RestController
@RequestMapping(path = "other")
public class MyMockController {

    @GetMapping(path = "hello")
    public ResponseEntity<String> doHelloAction() {
        return ResponseEntity.ok().body("Hi there!");
    }

}
