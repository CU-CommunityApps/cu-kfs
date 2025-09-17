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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
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

        HttpSession session = httpRequest.getSession(false);
        String sessionUser = (session != null) ? (String) session.getAttribute(CUWebAuthAuthenticationService.CUWAL_REMOTE_USER_HEADER) : null;

        if (remoteUser != null && (sessionUser == null || !StringUtils.equals(remoteUser,sessionUser))) {
            LOG.info("doFilter, create a new session to remediate session fixation vulernability for user {}", remoteUser);
            if (session != null) {
                session.invalidate();
            }
            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute(CUWebAuthAuthenticationService.CUWAL_REMOTE_USER_HEADER, remoteUser);
        } else if (session != null && remoteUser != null) {
            LOG.info("doFilter, keep existing session up to date for user {}", remoteUser);
            session.setAttribute(CUWebAuthAuthenticationService.CUWAL_REMOTE_USER_HEADER, remoteUser);
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
