package edu.cornell.kfs.sys.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CUTransactionFilter implements Filter {
	private static final Logger LOG = LogManager.getLogger(CUTransactionFilter.class);
 
    public void doFilter(ServletRequest req, ServletResponse res,	           
    		FilterChain chain) throws IOException, ServletException {
    	
    	boolean error = false;
    	String errorText = "";
    	HttpServletRequest httpReq = (HttpServletRequest)req;
						    	
    	if (!TransactionSynchronizationManager.getResourceMap().isEmpty()){
    		errorText = errorText + ("Before: The Resource map is not empty.   THIS IS SOOO BAD.   URL: " + httpReq.getRequestURL());
    		LOG.error("Before: The Resource map is not empty.   THIS IS SOOO BAD.   URL: " + httpReq.getRequestURL());
    		
    		Map aMap = TransactionSynchronizationManager.getResourceMap();
    		int mapsize = aMap.size();

    		Iterator keyValuePairs1 = aMap.entrySet().iterator();
    		for (int i = 0; i < mapsize; i++)
    		{
    		  Map.Entry entry = (Map.Entry) keyValuePairs1.next();
    		  errorText = errorText + (";  Resources:  key: " + entry.getKey().toString() + " ; Value " + entry.getValue().toString());
    		  LOG.error("Resources:  key: " + entry.getKey().toString() + " ; Value " + entry.getValue().toString());
    		}
    		error = true;
    	}    	
		try {
			chain.doFilter(req, res);
		} finally { 
				
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				LOG.error("JTA synchronizations are not active.   THIS IS SOOO BAD.   " + httpReq.getRequestURL());
				error = true;
			}
    	
			if (!TransactionSynchronizationManager.getResourceMap().isEmpty()) {
				errorText = errorText + "After: The Resource map is not empty.   THIS IS SOOO BAD.";
				LOG.error("After: The Resource map is not empty.   THIS IS SOOO BAD.   URL: " + httpReq.getRequestURL());
    		
				Map aMap = TransactionSynchronizationManager.getResourceMap();
				int mapsize = aMap.size();

				Iterator keyValuePairs1 = aMap.entrySet().iterator();
				for (int i = 0; i < mapsize; i++) {
					Map.Entry entry = (Map.Entry) keyValuePairs1.next();
					errorText = errorText + (";  Resources:  key: " + entry.getKey().toString() + " ; Value " + entry.getValue().toString());
					LOG.error("Resources:  key: " + entry.getKey().toString() + " ; Value " + entry.getValue().toString());
				}
				error = true;
			} 
    	
			if (error) {
				try {
					LOG.error("ERROR Found!  Sending an email...");
					
        
					BodyMailMessage mm = new BodyMailMessage();
					mm.addToAddress("kwk43@cornell.edu");
					mm.setFromAddress("kwk43@cornell.edu");
					mm.setSubject("There might be a closed connection problem.  Please check the logs. Seach for the following phrase: SOOO BAD");
					mm.setMessage("Request URL which had the problem: " + httpReq.getRequestURL() + "    errorText = " + errorText);

					SpringContext.getBean(EmailService.class).sendMessage(mm, false);
					
				} catch (Throwable t) {
					LOG.error("Error sending email.", t);
				}
			}
		}
    }

	public void destroy() {		
	}

	public void init(FilterConfig arg0) throws ServletException {
	}
}
