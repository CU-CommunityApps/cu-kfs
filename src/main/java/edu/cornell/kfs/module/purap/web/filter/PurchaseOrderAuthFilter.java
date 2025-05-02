package edu.cornell.kfs.module.purap.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import java.nio.charset.StandardCharsets;

import edu.cornell.kfs.module.purap.CUPurapConstants;

import com.google.gson.Gson;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;

public class PurchaseOrderAuthFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson gson = new Gson();
    
    private WebServiceCredentialService webServiceCredentialService;
    
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug("doFilter, entering");
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        httpServletResponse.setHeader(CUKFSConstants.ACCESS_CONTROL_HEADER_NAME, HttpMethod.GET);

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
                response.getWriter().println(gson.toJson(CUKFSConstants.UNAUTHORIZED));
            }
        } catch (Exception ex) {
            LOG.error("checkAuthorization", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(gson.toJson(CUKFSConstants.UNAUTHORIZED));
        }
    }
    
    private boolean isAuthorized(HttpServletRequest request) {
        byte[] byteArrayEncodedAllowableUserNamePassword = Base64
                .encodeBase64(getAllowableUserNamePassword().getBytes());
        String encodedAllowableUserNamePassword = new String(byteArrayEncodedAllowableUserNamePassword,
                StandardCharsets.UTF_8);
        String authorizationFromRequest = request.getHeader(CUKFSConstants.AUTHORIZATION_HEADER_KEY);
        return StringUtils.equals(authorizationFromRequest,
                CUKFSConstants.BASIC_AUTHENTICATION_STARTER + encodedAllowableUserNamePassword);

    }
    
    private String getAllowableUserNamePassword() {
        return getWebServiceCredentialService().getWebServiceCredentialValue(
                CUPurapConstants.PAYFLOW_CREDENTIAL_GROUP_CODE, CUKFSConstants.WEBSERVICE_CREDENTIAL_KEY_USERNAMEPASSWORD);
    }

    public WebServiceCredentialService getWebServiceCredentialService() {
        if (webServiceCredentialService == null) {
            webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        }
        return webServiceCredentialService;
    }

}
