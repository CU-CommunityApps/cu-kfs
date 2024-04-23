package edu.cornell.kfs.kim.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.context.SpringContext;

public final class CuKimUtils {

    private static UiDocumentService uiDocumentService;

    private CuKimUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    /*
     * Convenience method that copies most of the code and logic from base code's Person.canViewAddress() method.
     */
    public static boolean canOverridePrivacyPreferencesForUser(final String principalId) {
        final UserSession userSession = GlobalVariables.getUserSession();
        if (userSession == null) {
            // internal system call - no need to check permission
            return true;
        }
        final String currentUserPrincipalId = userSession.getPrincipalId();
        return StringUtils.equals(currentUserPrincipalId, principalId) ||
               getUiDocumentService().canModifyPerson(currentUserPrincipalId, principalId);
    }

    public static boolean canModifyPerson(final String principalId) {
        final UserSession userSession = GlobalVariables.getUserSession();
        if (userSession == null) {
            throw new IllegalStateException("No user session detected");
        }
        return getUiDocumentService().canModifyPerson(userSession.getPrincipalId(), principalId);
    }

    private static UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = SpringContext.getBean(UiDocumentService.class);
        }
        return uiDocumentService;
    }

}
