package edu.cornell.kfs.concur.rest.resource;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;

import edu.cornell.kfs.concur.ConcurConstants.ConcurAIConstants;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurAccountDetailDto;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class ConcurAIResource {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson gson = new Gson();
    
    private AccountService accountService;
    private DataDictionaryService dataDictionaryService;
    
    @Context
    protected HttpServletRequest servletRequest;

    @Context
    protected HttpServletResponse servletResponse;
    
    @GET
    public Response describeConcurAIResource() {
        return Response.ok(ConcurAIConstants.RESOURCE_NAME).build();
    }
    
    @GET
    @Path("/getAccountDetails")
    public Response getAccountDetails() {
        try {
            String chart = servletRequest.getParameter(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
            String accountNumber = servletRequest.getParameter(KFSPropertyConstants.ACCOUNT_NUMBER);
            LOG.debug("getAccountDetails, entering with chart {} and account {}", chart, accountNumber);
            
            if (!validatePropertyValue(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chart) || 
                    !validatePropertyValue(KFSPropertyConstants.ACCOUNT_NUMBER, accountNumber)) {
                LOG.debug("getAccountDetails, account or chart invalid");
                return Response.status(Status.BAD_REQUEST).entity(ConcurAIConstants.CHART_AND_ACCOUNT_MUST_BE_PROVIDED).build();
            }
            
            Account account = getAccountService().getByPrimaryId(chart, accountNumber);
            
            if (account != null) {
                ConcurAccountDetailDto dto = new ConcurAccountDetailDto(account);
                return Response.ok(gson.toJson(dto)).build();
            } else {
                String notFoundMessage = MessageFormat.format(ConcurAIConstants.ACCOUNT_NOT_FOUND_MESSAGE, chart, accountNumber);
                return Response.status(Status.NOT_FOUND).entity(notFoundMessage).build();
            }
            
        } catch (Exception e) {
            LOG.error("getAccountDetails, had an error getting account details", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(ConcurAIConstants.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private boolean validatePropertyValue(String propertyName, String propertyValue) {
        Pattern validationExpression = getDataDictionaryService().getAttributeValidatingExpression(Account.class.getName(), propertyName);
        return  StringUtils.isNotBlank(propertyValue) && validationExpression.matcher(propertyValue).matches();
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = SpringContext.getBean(AccountService.class);
        }
        return accountService;
    }

    public DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);;
        }
        return dataDictionaryService;
    }

}
