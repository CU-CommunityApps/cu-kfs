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

import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.sys.CUKFSConstants.EndpointCodes;
import edu.cornell.kfs.sys.service.ApiAuthenticationService;

public class CuEinvoiceServerAuthFilter implements Filter {

    private ApiAuthenticationService apiAuthenticationService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.checkAuthorization((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isAuthorized(request)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(CUPurapConstants.Einvoice.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        String submittedApiKey = request.getHeader(CUPurapConstants.Einvoice.EINVOICE_API_KEY_CREDENTIAL_NAME);
        return getApiAuthenticationService().isAuthorized(EndpointCodes.EINVOICE, submittedApiKey);
    }

    @Override
    public void destroy() {
    }

    public ApiAuthenticationService getApiAuthenticationService() {
        if (apiAuthenticationService == null) {
            apiAuthenticationService = SpringContext.getBean(ApiAuthenticationService.class);
        }
        return apiAuthenticationService;
    }

}
