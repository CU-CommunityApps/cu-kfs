package edu.cornell.kfs.concur.rest.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;

import edu.cornell.kfs.concur.rest.jsonObjects.ConcurAccountDetailDto;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class ConcurAIResource {
    private static final Logger LOG = LogManager.getLogger();
    
    private AccountService accountService;
    private Gson gson = new Gson();
    
    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;
    
    @GET
    public Response describeConcurAIResource() {
        return Response.ok("Concur AI resouce.").build();
    }
    
    @GET
    @Path("/getAccountDetails")
    public Response getAccountDetails(@RequestParam(value = "chartOfAccountsCode") String chart, 
            @RequestParam(value = "accountNumber") String accountNumber) {
        if (StringUtils.isBlank(chart) || StringUtils.isBlank(accountNumber)) {
            //return Response.i
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both chart and accountNumber must be provided");
        }
        
        Account account = getAccountService().getByPrimaryId(chart, accountNumber);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account found");
        }
        
        ConcurAccountDetailDto dto = new ConcurAccountDetailDto(account);
        return Response.ok(gson.toJson(dto)).build();
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = SpringContext.getBean(AccountService.class);;
        }
        return accountService;
    }
    
    

}
