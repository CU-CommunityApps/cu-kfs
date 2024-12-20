package edu.cornell.kfs.concur.rest.controller;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurAccountDetailDto;

@RestController
public class ConcurAccountDetailController {
    
    @GetMapping("/api/concur/getAccountDetails")
    public ConcurAccountDetailDto getAccountingDetail(@RequestParam(value = "chart") String chart, 
            @RequestParam(value = "chart") String accountNumber) {
        if (StringUtils.isBlank(chart) || StringUtils.isBlank(accountNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both chart and accountNumber must be provided");
        }
        
        Account account = getAccountService().getByPrimaryId(chart, accountNumber);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found");
        }
        
        ConcurAccountDetailDto dto = new ConcurAccountDetailDto(account);
        return dto;
    }
    
    private AccountService getAccountService() {
        AccountService service = SpringContext.getBean(AccountService.class);
        return service;
    }

}
