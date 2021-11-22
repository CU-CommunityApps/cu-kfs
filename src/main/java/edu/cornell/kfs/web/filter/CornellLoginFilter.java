package edu.cornell.kfs.web.filter;

import edu.cornell.kfs.kim.impl.identity.CUWebAuthAuthenticationService;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

public class CornellLoginFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse)servletResponse;

        String remoteUser = getRemoteUser(httpRequest);
        HttpServletRequestWrapper request =  new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getRemoteUser() {
                return remoteUser;
            }
        };

        filterChain.doFilter(request,httpResponse);
    }

    private String getRemoteUser(HttpServletRequest request) {
        String user = request.getHeader(CUWebAuthAuthenticationService.CUWAL_REMOTE_USER_HEADER);
        if (user == null) {
            user = request.getHeader(CUWebAuthAuthenticationService.CUWAL_REMOTE_USER_HEADER.toUpperCase(Locale.US));
        }
        if (user == null) {
            logNullUser(request);
        }
        return user;
    }
    
    private void logNullUser(HttpServletRequest request) {
        LOG.error("logNullUser, the remote user was not found in the HTTP request header.  Path: "  + request.getPathInfo() + 
                " auth type: " + request.getAuthType() + " query string: " + request.getQueryString());
    }

    @Override
    public void destroy() {
    }

}
