package edu.cornell.kfs.sys.batch.service.impl;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Custom BatchFileAdminAuthorizationServiceImpl subclass that adds an alternative permission template
 * for controlling the "Download" link on the Batch File lookup. This allows for granting users
 * permission to download files without necessarily granting permission to delete them.
 */
public class CuBatchFileAdminAuthorizationServiceImpl extends BatchFileAdminAuthorizationService {
    private static final Logger LOG = LogManager.getLogger();
    private String preventDownloadDirectories;

    /**
     * Overridden to also check the "Download Batch File" template, as an alternative means
     * of authorizing the "Download" link. Uses code and logic similar to that of the superclass method,
     * in addition to calling the superclass method to check the "Administer Batch File" template.
     * 
     * @see org.kuali.kfs.sys.batch.service.impl.BatchFileAdminAuthorizationServiceImpl#canDownload(
     * org.kuali.kfs.sys.batch.BatchFile, org.kuali.kfs.kim.api.identity.Person)
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean canDownload(BatchFile batchFile, Person user) {
        if (isDownloadOfFilePrevented(batchFile)) {
            return false;
        }
        boolean isAuthorized = false;
        if (batchFile.getFileName().indexOf(PdpConstants.RESEARCH_PARTICIPANT_FILE_PREFIX) >= 0) {
            isAuthorized = getPermissionService().hasPermissionByTemplate(user.getPrincipalId(),
                    KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.DOWNLOAD_BATCH_FILE_PERMISSION_TEMPLATE_NAME,
                    generatePermissionDetails(batchFile));
        } else {
            isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(),
                    KFSConstants.CoreModuleNamespaces.KFS, CUKFSConstants.SysKimApiConstants.DOWNLOAD_BATCH_FILE_PERMISSION_TEMPLATE_NAME,
                    generatePermissionDetails(batchFile), new HashMap<>());
        }
        return isAuthorized || super.canDownload(batchFile, user);
    }
    
    public boolean isDownloadOfFilePrevented(BatchFile batchFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDownloadOfFilePrevented, checking batch file " + batchFile.getFileName());
            LOG.debug("isDownloadOfFilePrevented, directories that are configured to prevent download: " + preventDownloadDirectories);
        }
        if (StringUtils.isNotBlank(preventDownloadDirectories)) {
            for (String individualDirectory : StringUtils.split(preventDownloadDirectories, KFSConstants.COMMA)) {
                if (StringUtils.containsIgnoreCase(batchFile.getPath(), individualDirectory)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("isDownloadOfFilePrevented. batchFile can NOT be downloaded with a name of " + batchFile.getFileName() + 
                                " and a path of " + batchFile.getPath());
                    }
                    return true;
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDownloadOfFilePrevented. batchFile can be downloaded with a name of " + batchFile.getFileName() + 
                    " and a path of " + batchFile.getPath());
        }
        return false;
    }

    public String getPreventDownloadDirectories() {
        return preventDownloadDirectories;
    }

    public void setPreventDownloadDirectories(String preventDownloadDirectories) {
        this.preventDownloadDirectories = preventDownloadDirectories;
    }

}
