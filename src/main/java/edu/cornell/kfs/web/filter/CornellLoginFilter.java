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

        // Session fixation mitigation: invalidate session if remoteUser is authenticated and different from session principal
        if (remoteUser != null && !remoteUser.isEmpty()) {
            javax.servlet.http.HttpSession session = httpRequest.getSession(false);
            boolean shouldInvalidate = false;

            if (session != null) {
                Object principalObj = session.getAttribute("principalName");
                if (principalObj instanceof String) {
                    String currentPrincipal = (String) principalObj;
                    if (!remoteUser.equals(currentPrincipal)) {
                        shouldInvalidate = true;
                    }
                } else {
                    // No principalName set, treat as privilege change
                    shouldInvalidate = true;
                }
            }

            if (shouldInvalidate && session != null) {
                session.invalidate();
                session = null;
            }

            javax.servlet.http.HttpSession newSession = (session == null) ? httpRequest.getSession(true) : session;
            newSession.setAttribute("principalName", remoteUser);
        }

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
                 " query string: " + request.getQueryString() + " auth type: " + request.getAuthType() + " class: " + request.getClass());
    }

    @Override
    public void destroy() {
    }

}
