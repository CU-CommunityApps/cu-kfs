package edu.cornell.kfs.module.purap.web.filter;

import com.google.gson.Gson;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.apache.commons.lang3.StringUtils;
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

public class CuEinvoiceServerAuthFilter implements Filter {

    private WebServiceCredentialService webServiceCredentialService;

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
        String correctApiKey = getWebServiceCredentialService().getWebServiceCredentialValue(CUPurapConstants.Einvoice.EINVOICE, CUPurapConstants.Einvoice.EINVOICE_API_KEY_CREDENTIAL_NAME);
        String submittedApiKey = request.getHeader(CUPurapConstants.Einvoice.EINVOICE_API_KEY_CREDENTIAL_NAME);
        return !StringUtils.isEmpty(submittedApiKey) && submittedApiKey.equals(correctApiKey);
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
