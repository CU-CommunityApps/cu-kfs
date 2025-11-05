package edu.cornell.kfs.concur.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "concur")
public class MyTempController {

    @GetMapping(path = "hello")
    public ResponseEntity<String> doHelloAction() {
        return ResponseEntity.ok().body("Hi there!");
    }

}
