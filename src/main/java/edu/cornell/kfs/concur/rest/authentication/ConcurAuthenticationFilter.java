package edu.cornell.kfs.concur.rest.authentication;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.service.ConcurAuthenticationService;

public class ConcurAuthenticationFilter implements Filter {
	private static final Logger LOG = LogManager.getLogger(ConcurAuthenticationFilter.class);

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        LOG.debug("doFilter() started");
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        logRequst(httpServletRequest);
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String authorizationHeader = httpServletRequest.getHeader(ConcurConstants.AUTHORIZATION_PROPERTY);

        if (StringUtils.isBlank(authorizationHeader)) {          
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            LOG.error("doFilter(): Concur authentication failed: missing authorization header.");
            return;
        } else {
            final String encodedUserNameAndPasswordAsToken = authorizationHeader.replaceFirst(ConcurConstants.BASIC_AUTHENTICATION_SCHEME + " ", "");

            if (!getConcurAuthenticationService().isConcurTokenValid(encodedUserNameAndPasswordAsToken)) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                LOG.error("doFilter(): Concur authentication failed: invalid token.");
                return;
            }
        }
        LOG.info("doFilter(): Concur authentication was successful");
        filterChain.doFilter(servletRequest, response);
    }
    
    private void logRequst(HttpServletRequest request) {
        LOG.info("logRequst, entering");
        boolean isProd = ConfigContext.getCurrentContextConfig().isProductionEnvironment();
        if (!isProd) {
            for (String attributeKey : request.getParameterMap().keySet()) {
                LOG.info("logRequst, attributeKey: '" + attributeKey + "' and attribute value: '" + request.getParameterMap().get(attributeKey) + "'");
            }
            
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerKey = headerNames.nextElement();
                String headerValue = request.getHeader(headerKey);
                LOG.info("logRequst, headerKey: '" + headerKey + "' and header value: '" + headerValue + "'");
            }
        } else {
            LOG.info("logRequst, this is production, so we aren't dooing this logging.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private ConcurAuthenticationService getConcurAuthenticationService() {
        return SpringContext.getBean(ConcurAuthenticationService.class);
    }

}
