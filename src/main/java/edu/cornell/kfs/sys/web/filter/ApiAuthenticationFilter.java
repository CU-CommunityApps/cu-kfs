package edu.cornell.kfs.sys.web.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ApiAuthenticationService;

/**
 * Spring-managed Filter implementation that enforces web service authentication via the CU-specific
 * API Authentication framework. Can be used in conjunction with CuDelegatingFilterProxy so that
 * Spring manages this instance while "web.xml" manages the proxy. That way, each API can have
 * its own Spring-managed instance of this class for authentication handling.
 * 
 * As per the DelegatingFilterProxy documentation, if you want CuDelegatingFilterProxy to invoke
 * this instance properly, then the proxy's "web.xml" configuration should do ONE of the following:
 * 
 * [1] Have the proxy's "filter-name" correspond to the bean name (or a bean alias) of this instance.
 * [2] Specify an "init-param" named "targetBeanName" that contains the appropriate bean name.
 * 
 * This filter also has the option of configuring the "Access-Control-Allow-Methods" header
 * in the servlet response via the "accessControlAllowMethods" property. If this property
 * is not specified, then that header will not be modified.
 * 
 * To prepare a new bean instance of this, create a bean that has "apiAuthenticationFilter-baseBean"
 * as the parent and populates "endpointCode" and/or "accessControlAllowMethods" as appropriate.
 */
public class ApiAuthenticationFilter implements Filter, InitializingBean {

    private static final Logger LOG = LogManager.getLogger();

    private ApiAuthenticationService apiAuthenticationService;
    private String endpointCode;
    private String accessControlAllowMethods;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(apiAuthenticationService, "apiAuthenticationService cannot be null");
        Validate.notBlank(endpointCode, "endpointCode cannot be blank");
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        LOG.debug("doFilter, start");
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if (StringUtils.isNotBlank(accessControlAllowMethods)) {
            httpServletResponse.setHeader(CUKFSConstants.ACCESS_CONTROL_HEADER_NAME, accessControlAllowMethods);
        }
        checkAuthorization(httpServletRequest, httpServletResponse, chain);
    }

    private void checkAuthorization(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        try {
            if (apiAuthenticationService.isAuthorized(endpointCode, request)) {
                chain.doFilter(request, response);
            } else {
                LOG.warn("checkAuthorization, Unauthorized {} {}", request.getMethod(), request.getPathInfo());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(createJsonResponseForUnauthorizedRequest());
            }
        } catch (final Exception ex) {
            LOG.error("checkAuthorization, Unexpected error", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(createJsonResponseForUnauthorizedRequest());
        }
    }

    private String createJsonResponseForUnauthorizedRequest() {
        return StringUtils.join(CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.UNAUTHORIZED, CUKFSConstants.DOUBLE_QUOTE);
    }

    public void setApiAuthenticationService(final ApiAuthenticationService apiAuthenticationService) {
        this.apiAuthenticationService = apiAuthenticationService;
    }

    public void setEndpointCode(final String endpointCode) {
        this.endpointCode = endpointCode;
    }

    public void setAccessControlAllowMethods(final String accessControlAllowMethods) {
        this.accessControlAllowMethods = accessControlAllowMethods;
    }

}
