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
import java.io.IOException;
import java.util.Locale;

public class CornellLoginFilter implements Filter {

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
        return user;
    }

    @Override
    public void destroy() {
    }

}
