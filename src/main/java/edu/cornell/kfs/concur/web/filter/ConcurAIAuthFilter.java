package edu.cornell.kfs.concur.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;

import com.google.gson.Gson;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ApiAuthentizationService;

public class ConcurAIAuthFilter implements Filter {
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson gson = new Gson();

    private ApiAuthentizationService apiAuthentizationService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        httpServletResponse.setHeader(CUKFSConstants.ACCESS_CONTROL_HEADER_NAME, HttpMethod.GET);

        checkAuthorization(httpServletRequest, httpServletResponse, chain);

    }

    private void checkAuthorization(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {
        try {
            if (isAuthorized(request)) {
                chain.doFilter(request, response);
            } else {
                LOG.warn("checkAuthorization unauthorized {} {}", request.getMethod(), request.getPathInfo());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println(gson.toJson(CUKFSConstants.UNAUTHORIZED));
            }
        } catch (Exception ex) {
            LOG.error("checkAuthorization", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println(gson.toJson(CUKFSConstants.UNAUTHORIZED));
        }
    }

    private boolean isAuthorized(HttpServletRequest request) {
        return getApiAuthentizationService().isAuthorized("concAcctDetail", request);
    }
    
    public ApiAuthentizationService getApiAuthentizationService() {
        if (apiAuthentizationService == null) {
            apiAuthentizationService = SpringContext.getBean(ApiAuthentizationService.class);
        }
        return apiAuthentizationService;
    }

}
