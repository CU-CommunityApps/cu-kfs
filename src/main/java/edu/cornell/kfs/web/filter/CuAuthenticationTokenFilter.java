package edu.cornell.kfs.web.filter;

import org.kuali.kfs.sys.web.filter.AuthenticationTokenFilter;
import org.kuali.kfs.kns.util.CookieUtils;
import org.kuali.kfs.sys.util.BearerTokenContext;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Custom AuthenticationTokenFilter that mitigates session fixation by invalidating and recreating the session
 * after successful authentication.
 */
public class CuAuthenticationTokenFilter extends AuthenticationTokenFilter {

    private CookieUtils cuCookieUtils = new CookieUtils();
    private FilterConfig cuFilterConfig;

    @Override
    public void init(final FilterConfig filterConfig) {
        super.init(filterConfig);
        this.cuFilterConfig = filterConfig;
    }

    @Override
    protected void coreDoFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        final Optional<String> coreToken = cuCookieUtils.getCoreAuthToken(request);
        if (!coreToken.isPresent()) {
            throw new RuntimeException("Unable to access core token");
        }

        final Optional<String> financialsAuthToken = cuCookieUtils.getFinancialsAuthToken(request);
        if (financialsAuthToken.isPresent()) {
            try {
                final Optional<String> coreAuthUser = getCoreApiKeyAuthenticationService()
                        .getPrincipalIdFromApiKey(financialsAuthToken.get(),
                                org.kuali.kfs.krad.util.KRADUtils.getUserSessionFromRequest(request));
                if (coreAuthUser.isPresent()) {
                    // Session fixation mitigation: Invalidate and create new session after authentication
                    HttpSession session = request.getSession(false);
                    if (session != null && !session.isNew()) {
                        session.invalidate();
                        request.getSession(true);
                        // Redirect to ensure new JSESSIONID cookie is sent
                        response.sendRedirect(request.getRequestURI());
                        return;
                    }
                }
                BearerTokenContext.setBearerToken(financialsAuthToken.get());
                filterChain.doFilter(request, response);
                return;
            } catch (final RuntimeException e) {
                // continue to create new token
            } finally {
                BearerTokenContext.clear();
            }
        }

        final javax.servlet.http.Cookie financialsAuthCookie = cuCookieUtils.createFinancialsAuthCookie(request, coreToken.get(),
                cuFilterConfig.getServletContext().getContextPath());
        response.addCookie(financialsAuthCookie);

        filterChain.doFilter(request, response);
    }

    @Override
    protected void nonCoreDoFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        final String token;

        final Optional<String> financialsAuthToken = cuCookieUtils.getFinancialsAuthToken(request);
        if (financialsAuthToken.isPresent()) {
            try {
                getJwtService().decodeJwt(financialsAuthToken.get());
                // Session fixation mitigation: Invalidate and create new session after authentication
                HttpSession session = request.getSession(false);
                if (session != null && !session.isNew()) {
                    session.invalidate();
                    request.getSession(true);
                    // Redirect to ensure new JSESSIONID cookie is sent
                    response.sendRedirect(request.getRequestURI());
                    return;
                }
                BearerTokenContext.setBearerToken(financialsAuthToken.get());
                filterChain.doFilter(request, response);
                return;
            } catch (final RuntimeException e) {
                final org.kuali.kfs.sys.businessobject.JwtData jwtData = new org.kuali.kfs.sys.businessobject.JwtData(request.getRemoteUser(), getExpirationSeconds());
                token = getJwtService().generateJwt(jwtData);
            } finally {
                BearerTokenContext.clear();
            }
        } else {
            final org.kuali.kfs.sys.businessobject.JwtData jwtData = new org.kuali.kfs.sys.businessobject.JwtData(request.getRemoteUser(), getExpirationSeconds());
            token = getJwtService().generateJwt(jwtData);
        }

        final javax.servlet.http.Cookie financialsAuthCookie = cuCookieUtils.createFinancialsAuthCookie(request, token,
                cuFilterConfig.getServletContext().getContextPath());
        response.addCookie(financialsAuthCookie);

        filterChain.doFilter(request, response);
    }
}
