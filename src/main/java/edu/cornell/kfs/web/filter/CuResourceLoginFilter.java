package edu.cornell.kfs.web.filter;

import org.kuali.kfs.web.filter.ResourceLoginFilter;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.web.filter.LoginFilterBase;
import org.kuali.kfs.krad.UserSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Custom ResourceLoginFilter for Cornell to mitigate session fixation in API authentication.
 */
public class CuResourceLoginFilter extends ResourceLoginFilter {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    protected void setUserSession(HttpServletRequest request, String principalName) {
        HttpSession session = request.getSession(false);
        boolean shouldInvalidate = false;

        if (session != null) {
            Object userSessionObj = session.getAttribute(KRADConstants.USER_SESSION_KEY);
            if (userSessionObj instanceof UserSession) {
                UserSession currentUserSession = (UserSession) userSessionObj;
                // Only invalidate if the principalName is changing (i.e., login event)
                if (!principalName.equals(currentUserSession.getPrincipalName())) {
                    LOG.info("Invalidating session due to principalName change: {} -> {}", currentUserSession.getPrincipalName(), principalName);
                    shouldInvalidate = true;
                }
            } else {
                // If no valid UserSession, treat as privilege change
                shouldInvalidate = true;
            }
        }

        if (shouldInvalidate && session != null) {
            session.invalidate();
            session = null;
        }

        HttpSession newSession = (session == null) ? request.getSession(true) : session;
        newSession.setAttribute(KRADConstants.USER_SESSION_KEY, new UserSession(principalName));
    }
}
