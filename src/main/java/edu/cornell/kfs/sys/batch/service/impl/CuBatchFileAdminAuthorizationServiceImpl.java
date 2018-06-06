package edu.cornell.kfs.sys.batch.service.impl;

import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.rice.kim.api.identity.Person;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Custom BatchFileAdminAuthorizationServiceImpl subclass that adds an alternative permission template
 * for controlling the "Download" link on the Batch File lookup. This allows for granting users
 * permission to download files without necessarily granting permission to delete them.
 */
public class CuBatchFileAdminAuthorizationServiceImpl extends BatchFileAdminAuthorizationService {

    /**
     * Overridden to also check the "Download Batch File" template, as an alternative means
     * of authorizing the "Download" link. Uses code and logic similar to that of the superclass method,
     * in addition to calling the superclass method to check the "Administer Batch File" template.
     * 
     * @see org.kuali.kfs.sys.batch.service.impl.BatchFileAdminAuthorizationServiceImpl#canDownload(
     * org.kuali.kfs.sys.batch.BatchFile, org.kuali.rice.kim.api.identity.Person)
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canDownload(BatchFile batchFile, Person user) {
        boolean isAuthorized = false;
        if (batchFile.getFileName().indexOf(PdpConstants.RESEARCH_PARTICIPANT_FILE_PREFIX) >= 0) {
            isAuthorized = getIdentityManagementService().hasPermissionByTemplateName(user.getPrincipalId(),
                    KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.DOWNLOAD_BATCH_FILE_PERMISSION_TEMPLATE_NAME,
                    generateDownloadCheckPermissionDetails(batchFile, user));
        } else {
            isAuthorized = getIdentityManagementService().isAuthorizedByTemplateName(user.getPrincipalId(),
                    KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.DOWNLOAD_BATCH_FILE_PERMISSION_TEMPLATE_NAME,
                    generateDownloadCheckPermissionDetails(batchFile, user), generateDownloadCheckRoleQualifiers(batchFile, user));
        }
        return isAuthorized || super.canDownload(batchFile, user);
    }

}
