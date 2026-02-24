/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.web.filter;

import org.apache.commons.lang3.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.bo.AuthenticationValidationResponse;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.exception.AuthenticationException;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.web.filter.LoginFilterBase;
import org.kuali.kfs.sys.businessobject.JwtData;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.CoreApiKeyAuthenticationService;
import org.kuali.kfs.sys.service.JwtService;
import org.kuali.kfs.sys.util.BearerTokenContext;
import org.springframework.http.HttpHeaders;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

// CU customization: backport FINP-11480; this can be removed with the upgrade to the 10/09/2024 upgrade
public class ResourceLoginFilter extends LoginFilterBase {

    private static final Logger LOG = LogManager.getLogger();

    static final String UNAUTHORIZED_JSON = "[ \"Unauthorized\" ]";
    static final String FORBIDDEN_JSON = "[ \"Forbidden\" ]";

    private static final Set<String> ENDPOINTS_EXEMPT_FROM_AUTHENTICATION = Set.of(
            "/health/check",
            "/health/integrity"
    );

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        LOG.debug("doFilter() started");

        try {
            if (pathRequiresAuthentication(request)) {
                final Optional<String> user = getPrincipalNameFromRequest(request);
                if (user.isEmpty()) {
                    sendError(response);
                    removeFromMDC();
                    return;
                }

                if (isInactive(user.get())) {
                    sendForbidden(response);
                    removeFromMDC();
                    return;
                }

                setUserSession(request, user.get());
                establishUserSession(request, response);
                setBearerTokenContext(request);
            }

            chain.doFilter(request, response);
        } catch (AuthenticationException | IllegalArgumentException e) {
            LOG.error("doFilter() AuthenticationException", e);
            sendError(response);
        } finally {
            removeFromMDC();
        }
    }

    private boolean pathRequiresAuthentication(final HttpServletRequest request) {
        LOG.debug("pathRequiresAuthentication(...) - Enter");
        final String requestedPath = request.getPathInfo();
        final boolean pathRequiresAuthentication = !getEndpointsExemptFromAuthentication().contains(requestedPath);
        LOG.debug("pathRequiresAuthentication(...) - Exit : pathRequiresAuthentication={}", pathRequiresAuthentication);
        return pathRequiresAuthentication;
    }

    /**
     * This can be overridden in an Overlay, to accommodate custom endpoints. It should only be used in exceptional
     * situations. For example, Jaggaer/SciQuest needs to call into KFS but cannot use our regular authentication.
     * <p>
     * NOTE: This method may NOT be 'private' or 'static'.
     *
     * @return An immutable collection of {@code String}s, denoting endpoints do not fall under standard KFS
     * authentication.
     */
    protected Set<String> getEndpointsExemptFromAuthentication() {
        return ENDPOINTS_EXEMPT_FROM_AUTHENTICATION;
    }

    private boolean isInactive(final String principalName) {
        final AuthenticationValidationResponse validationResponse =
                getCfAuthenticationService().validatePrincipalName(principalName);
        return validationResponse == AuthenticationValidationResponse.INVALID_PRINCIPAL_CANNOT_LOGIN;
    }

    protected void establishUserSession(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        final UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
        if (userSession != null) {
            GlobalVariables.setUserSession(userSession);
        }

        establishSessionCookie(request, response);
        establishBackdoorUser(request);

        addToMDC(request);
    }

    private Optional<String> getPrincipalNameFromRequest(final HttpServletRequest request) {
        Optional<String> user = Optional.empty();
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
        if (StringUtils.isNotBlank(authorizationHeader)) {
            user = getPrincipalNameFromHeader(authorizationHeader, userSession);
        } else if (isUserSessionEstablished(request)) {
            user = Optional.of(userSession.getActualPerson().getPrincipalName());
        }

        return user;
    }

    private Optional<String> getPrincipalNameFromHeader(
            final String authorizationHeader,
            final UserSession userSession
    ) {
        if (authorizationHeader == null) {
            return Optional.empty();
        }
        final Optional<String> oKey = getApiKey(authorizationHeader);
        if (oKey.isPresent()) {
            if (getCoreApiKeyAuthenticationService().useCore()) {
                return getCoreApiKeyAuthenticationService().getPrincipalIdFromApiKey(oKey.get(), userSession);

            } else {
                try {
                    final JwtData data = getJwtService().decodeJwt(oKey.get());
                    return Optional.of(data.getPrincipalName());
                } catch (final RuntimeException e) {
                    LOG.debug("getPrincipalNameFromHeader() invalid financials token", e);
                }
            }
        }

        return Optional.empty();
    }

    private static void sendError(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().println(UNAUTHORIZED_JSON);
    }

    private static void sendForbidden(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().println(FORBIDDEN_JSON);
    }

    protected void setUserSession(
            final HttpServletRequest request,
            final String principalName
    ) {
        final UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
        if (userSession == null || userSession.getActualPerson() == null
                || !StringUtils.equals(userSession.getActualPerson().getPrincipalName(), principalName)) {
            final UserSession newUserSession = new UserSession(principalName);
            request.getSession().setAttribute(KRADConstants.USER_SESSION_KEY, newUserSession);
        }
    }

    private static Optional<String> getApiKey(final String authorizationHeader) {
        if (!authorizationHeader.toLowerCase(Locale.US).startsWith("bearer")) {
            LOG.error("getApiKey() authorization header missing Bearer prefix");
            return Optional.empty();
        }

        final String[] split = authorizationHeader.split("\\s+");
        if (split.length != 2) {
            LOG.error("doFilter() authorization header should be two parts");
            return Optional.empty();
        }

        return Optional.of(split[1]);
    }

    protected CoreApiKeyAuthenticationService getCoreApiKeyAuthenticationService() {
        return SpringContext.getBean(CoreApiKeyAuthenticationService.class);
    }

    protected JwtService getJwtService() {
        return SpringContext.getBean(JwtService.class);
    }

    private static void setBearerTokenContext(final HttpServletRequest request) {
        LOG.debug("setBearerTokenContext(...) - Enter : request={}", request);
        extractBearerToken(request).ifPresent(BearerTokenContext::setBearerToken);
        LOG.debug("setBearerTokenContext(...) - Exit");
    }

    private static Optional<String> extractBearerToken(final HttpServletRequest request) {
        LOG.debug("extractBearerToken(...) - Enter : request={}", request);

        final String authorizationHeaderValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        LOG.debug("extractBearerToken(...) - authorizationHeaderValue={}", authorizationHeaderValue);

        if (StringUtils.isNotBlank(authorizationHeaderValue)) {
            final String[] tokens = authorizationHeaderValue.split("\\s+");
            if (tokens.length == 2) {
                final String bearerToken = tokens[1];
                LOG.debug("extractBearerToken(...) - Exit : bearerToken={}", bearerToken);
                return Optional.of(bearerToken);
            }
        }

        LOG.debug("extractBearerToken(...) - Exit; empty");
        return Optional.empty();
    }

}
