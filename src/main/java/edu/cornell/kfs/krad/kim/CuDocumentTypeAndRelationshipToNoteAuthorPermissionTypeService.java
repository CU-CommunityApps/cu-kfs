package edu.cornell.kfs.krad.kim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.permission.PermissionBo;
import org.kuali.kfs.krad.kim.DocumentTypeAndNodeAndRouteStatusPermissionTypeServiceImpl;
import org.kuali.rice.kim.api.permission.Permission;

/**
 * Custom DocumentTypeAndNodeAndRouteStatusPermissionTypeServiceImpl subclass
 * that also supports relationship-to-note-author permission details.
 * It is intended to override DocumentTypeAndRelationshipToNoteAuthorPermissionTypeService
 * so that route node/status can also affect attachment deletion permission matching.
 * 
 * The override of the performPermissionMatches() method is simply a copy of the related method
 * from the DocumentTypeAndRelationshipToNoteAuthorPermissionTypeService superclass.
 */
public class CuDocumentTypeAndRelationshipToNoteAuthorPermissionTypeService
        extends DocumentTypeAndNodeAndRouteStatusPermissionTypeServiceImpl {

    @Override
    protected List<Permission> performPermissionMatches(
        Map<String, String> requestedDetails,
        List<Permission> permissionsList) {

        List<Permission> matchingPermissions = new ArrayList<>();
        if (requestedDetails == null) {
            return matchingPermissions;
        }

        // loop over the permissions, checking the non-document-related ones
        for (Permission permission : permissionsList) {
            PermissionBo bo = PermissionBo.from(permission);
            if (Boolean.parseBoolean(requestedDetails.get(KimConstants.AttributeConstants.CREATED_BY_SELF))) {
                if (Boolean.parseBoolean(bo.getDetails().get(KimConstants.AttributeConstants.CREATED_BY_SELF_ONLY))) {
                    matchingPermissions.add(permission);
                }
            } else {
                if (!Boolean.parseBoolean(bo.getDetails().get(KimConstants.AttributeConstants.CREATED_BY_SELF_ONLY))) {
                    matchingPermissions.add(permission);
                }
            }
        }

        matchingPermissions = super.performPermissionMatches(requestedDetails, matchingPermissions);
        return matchingPermissions;
    }

}
