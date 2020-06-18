package edu.cornell.kfs.module.cam.web.filter;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.kuali.kfs.sys.context.SpringContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CuCapAssetInventoryServerAuthFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger(CuCapAssetInventoryServerAuthFilter.class);

    private WebServiceCredentialService webServiceCredentialService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse alteredResponse = (HttpServletResponse) response;
        alteredResponse.setHeader("Access-Control-Allow-Origin", "*");
        alteredResponse.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        this.checkAuthorization((HttpServletRequest) request, alteredResponse, chain);
    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isAuthorized(request)) {
            LOG.info("CapAssetInventoryApi authorized " + request.getMethod() + " " + request.getPathInfo());
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        //todo switch to verify Cognito User Pool Key in request Header with Cognito public key
        return true;
    }

    @Override
    public void destroy() {
    }

    protected WebServiceCredentialService getWebServiceCredentialService() {
        if (this.webServiceCredentialService == null) {
            this.webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        }

        return this.webServiceCredentialService;
    }

}
