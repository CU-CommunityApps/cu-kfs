package edu.cornell.kfs.web.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.web.filter.SessionExpirationFilter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Custom SessionExpirationFilter that invalidates the session and creates a new one on expiration,
 * then runs the original redirect logic.
 */
public class CuSessionExpirationFilter extends SessionExpirationFilter {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        if (req.getRequestURI().endsWith("/SessionInvalidateAction.do") && req.getSession(false) != null) {
            LOG.info("doFilter, Session fixation mitigation: invalidating and creating new session");
            req.getSession().invalidate();
            req.getSession(true);
        }

        // Run original redirect logic and filter chain
        applyRedirectHeader(request, response);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
