package edu.cornell.kfs.module.purap.web.filter;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.KRADUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CuEinvoiceServerAuthFilter implements Filter {

    private static final String UNAUTHORIZED_JSON = "Unauthorized";

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
        }
        else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(UNAUTHORIZED_JSON));
        }
    }


    private boolean isAuthorized(HttpServletRequest request) {
        String eInvoiceApiKey = request.getHeader("auth_einvoice_server_api_key");
        if (!StringUtils.isEmpty(eInvoiceApiKey)) {
            return true; //todo compare with config value
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    protected boolean isUserSessionEstablished(HttpServletRequest request) {
        return KRADUtils.getUserSessionFromRequest(request) != null;
    }
}
