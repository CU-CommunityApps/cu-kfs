package edu.cornell.kfs.module.cam.web.filter;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.module.cam.CuCamsConstants;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import org.kuali.kfs.krad.util.ObjectUtils;
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
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "DELETE, PUT, POST, GET, OPTIONS");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers, capital_asset_scanning_api_key");
        if (httpServletRequest.getMethod().equalsIgnoreCase("options")) {
            chain.doFilter(request, response);
        } else {
            this.checkAuthorization(httpServletRequest, httpServletResponse, chain);
        }
    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isAuthorized(request)) {
            chain.doFilter(request, response);
        } else {
            LOG.warn("CapAssetInventoryApi checkAuthorization unauthorized " + request.getMethod() + " " + request.getPathInfo());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(new Gson().toJson(CuCamsConstants.CapAssetApi.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        //todo switch to verify Cognito User Pool Key in request Header with Cognito public key
        String correctApiKey = getWebServiceCredentialService().getWebServiceCredentialValue(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_CREDENTIAL_GROUP_CODE,
                CuCamsConstants.CapAssetApi.CAPITAL_ASSET_API_KEY_CREDENTIAL_NAME);
        String submittedApiKey = request.getHeader(CuCamsConstants.CapAssetApi.CAPITAL_ASSET_API_KEY_CREDENTIAL_NAME);
        return !StringUtils.isEmpty(correctApiKey) && !StringUtils.isEmpty(submittedApiKey) && submittedApiKey.equals(correctApiKey);
    }

    @Override
    public void destroy() {
    }

    protected WebServiceCredentialService getWebServiceCredentialService() {
        if (ObjectUtils.isNull(this.webServiceCredentialService)) {
            this.webServiceCredentialService = SpringContext.getBean(WebServiceCredentialService.class);
        }

        return this.webServiceCredentialService;
    }

}
