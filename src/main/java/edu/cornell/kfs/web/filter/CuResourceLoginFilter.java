package edu.cornell.kfs.web.filter;

import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.web.filter.ResourceLoginFilter;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Custom ResourceLoginFilter that mitigates session fixation by invalidating and recreating the session
 * when a new principal is authenticated.
 */
public class CuResourceLoginFilter extends ResourceLoginFilter {

    @Override
    protected void setUserSession(HttpServletRequest request, String principalName) {
        UserSession userSession = KRADUtils.getUserSessionFromRequest(request);
        boolean principalChanged = userSession == null
                || userSession.getActualPerson() == null
                || !StringUtils.equals(userSession.getActualPerson().getPrincipalName(), principalName);

        if (principalChanged) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            // Create a new session and set the UserSession
            HttpSession newSession = request.getSession(true);
            UserSession newUserSession = new UserSession(principalName);
            newSession.setAttribute(KRADConstants.USER_SESSION_KEY, newUserSession);
        }
    }
}
