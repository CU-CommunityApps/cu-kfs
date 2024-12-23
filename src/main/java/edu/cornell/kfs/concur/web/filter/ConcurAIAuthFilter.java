package edu.cornell.kfs.concur.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;

import edu.cornell.kfs.concur.ConcurConstants.ConcurAIConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class ConcurAIAuthFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger();

    private WebServiceCredentialService webServiceCredentialService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        httpServletResponse.setHeader(ConcurAIConstants.ACCESS_CONTROL_HEADER_NAME, HttpMethod.GET);

        checkAuthorization(httpServletRequest, httpServletResponse, chain);

    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {
        try {
            if (isAuthorized(request)) {
                chain.doFilter(request, response);
            } else {
                LOG.warn("checkAuthorization unauthorized {} {}", request.getMethod(), request.getPathInfo());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(new Gson().toJson(ConcurAIConstants.UNAUTHORIZED));
            }
        } catch (Exception ex) {
            LOG.error("checkAuthorization", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(ConcurAIConstants.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        byte[] byteArrayEncodedAllowableUserNamePassword = Base64
                .encodeBase64(getAllowableUserNamePassword().getBytes());
        String encodedAllowableUserNamePassword = new String(byteArrayEncodedAllowableUserNamePassword,
                StandardCharsets.UTF_8);
        String authorizationFromRequest = request.getHeader(ConcurAIConstants.AUTHORIZATION_HEADER_KEY);
        return StringUtils.equals(authorizationFromRequest,
                ConcurAIConstants.BASIC_AUTHENTICATION_STARTER + encodedAllowableUserNamePassword);

    }

    private String getAllowableUserNamePassword() {
        return getWebServiceCredentialService().getWebServiceCredentialValue(
                ConcurAIConstants.WEBSERVICE_CRED_GROUP_CODE, ConcurAIConstants.WEBSERVICE_CRED_KEY);
    }

    public WebServiceCredentialService getWebServiceCredentialService() {
        if (webServiceCredentialService == null) {
            webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        }
        return webServiceCredentialService;
    }

}
