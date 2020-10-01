/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.krad.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.bo.AuthenticationValidationResponse;
import org.kuali.kfs.kns.service.CfAuthenticationService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.exception.AuthenticationException;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kim.api.identity.AuthenticationService;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.UUID;

public abstract class LoginFilterBase implements Filter {

    private static final String MDC_USER = "user";
    protected ParameterService parameterService;
    protected CfAuthenticationService cfAuthenticationService;
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
    }

    /**
     * Checks if the user who made the request has a UserSession established
     *
     * @param request the HTTPServletRequest object passed in
     * @return true if the user session has been established, false otherwise
     */
    protected boolean isUserSessionEstablished(HttpServletRequest request) {
        return KRADUtils.getUserSessionFromRequest(request) != null;
    }

    /**
     * Checks if a user can be authenticated and if so establishes a UserSession for that user.
     */
    protected void establishUserSession(HttpServletRequest request) {
        if (!isUserSessionEstablished(request)) {
            String principalName = ((AuthenticationService) GlobalResourceLoader.getResourceLoader().getService(
                    new QName("kimAuthenticationService"))).getPrincipalName(request);

            AuthenticationValidationResponse validationResponse = getCfAuthenticationService().validatePrincipalName(
                    principalName);
            switch (validationResponse) {
                case INVALID_PRINCIPAL_NAME_BLANK:
                    throw new AuthenticationException("Blank User from AuthenticationService - This should never " +
                            "happen.");
                case INVALID_PRINCIPAL_DOES_NOT_EXIST:
                    throw new AuthenticationException("Unknown User: " + principalName);
                case INVALID_PRINCIPAL_CANNOT_LOGIN:
                    throw new AuthenticationException("You cannot log in, because you are not an active Kuali " +
                            "user.\nPlease ask someone to activate your account if you need to use Kuali Systems." +
                            "\nThe user id provided was: " + principalName + ".\n");
                default:
                    break;
            }

            final UserSession userSession = new UserSession(principalName);
            if (userSession.getPerson() == null) {
                throw new AuthenticationException("Invalid User: " + principalName);
            }

            request.getSession().setAttribute(KRADConstants.USER_SESSION_KEY, userSession);

            // Set up rice session so configuration page will work
            final org.kuali.rice.krad.UserSession riceUserSession = new org.kuali.rice.krad.UserSession(principalName);
            request.getSession().setAttribute(org.kuali.rice.krad.util.KRADConstants.USER_SESSION_KEY, riceUserSession);
        }
    }

    /**
     * Creates a session id cookie if one does not exists.  Write the cookie out to the response with that session id.
     * Also, sets the cookie on the established user session.
     */
    protected void establishSessionCookie(HttpServletRequest request, HttpServletResponse response) {
        String kualiSessionId = this.getKualiSessionId(request.getCookies());
        if (kualiSessionId == null) {
            kualiSessionId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(KRADConstants.KUALI_SESSION_ID, kualiSessionId);
            cookie.setPath(filterConfig.getServletContext().getContextPath());
            cookie.setSecure(request.isSecure());
            response.addCookie(cookie);
        }
        KRADUtils.getUserSessionFromRequest(request).setKualiSessionId(kualiSessionId);
    }

    /**
     * gets the kuali session id from an array of cookies.  If a session id does not exist returns null.
     */
    private String getKualiSessionId(final Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (KRADConstants.KUALI_SESSION_ID.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * establishes the backdoor user on the established user id if backdoor capabilities are valid.
     */
    protected void establishBackdoorUser(HttpServletRequest request) {
        if (ConfigContext.getCurrentContextConfig().isProductionEnvironment()) {
            return;
        }

        final String backdoor = request.getParameter(KRADConstants.BACKDOOR_PARAMETER);
        if (StringUtils.isNotBlank(backdoor)) {
            AuthenticationValidationResponse response = getCfAuthenticationService().validatePrincipalName(backdoor);
            if (response == AuthenticationValidationResponse.VALID_AUTHENTICATION) {
                if (getParameterService().getParameterValueAsBoolean(KRADConstants.KUALI_RICE_WORKFLOW_NAMESPACE,
                        KRADConstants.DetailTypes.BACKDOOR_DETAIL_TYPE, KewApiConstants.SHOW_BACK_DOOR_LOGIN_IND)) {
                    try {
                        KRADUtils.getUserSessionFromRequest(request).setBackdoorUser(backdoor);
                    } catch (RiceRuntimeException re) {
                        //Ignore so BackdoorAction can redirect to invalid_backdoor_portal
                    }
                }
            }
        }
    }

    protected void addToMDC(HttpServletRequest request) {
        ThreadContext.put(MDC_USER, KRADUtils.getUserSessionFromRequest(request).getPrincipalName());
    }

    protected void removeFromMDC() {
        ThreadContext.remove(MDC_USER);
    }

    protected CfAuthenticationService getCfAuthenticationService() {
        if (cfAuthenticationService == null) {
            cfAuthenticationService = KNSServiceLocator.getCfAuthenticationService();
        }
        return cfAuthenticationService;
    }

    protected ParameterService getParameterService() {
        if (this.parameterService == null) {
            this.parameterService = CoreFrameworkServiceLocator.getParameterService();
        }

        return this.parameterService;
    }
}
