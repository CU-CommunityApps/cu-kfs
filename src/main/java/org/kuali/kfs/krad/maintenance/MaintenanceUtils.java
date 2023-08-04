/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.krad.maintenance;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.impl.services.CoreImplServiceLocator;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Provides static utility methods for use within the maintenance framework
 * CU Customization KFSPTS-23120 investigate NPE
 */
public final class MaintenanceUtils {

    private static final Logger LOG = LogManager.getLogger();
    private static final String WARNING_MAINTENANCE_LOCKED = "warning.maintenance.locked";
    
    private static Cache blockingCache;
    public static final String LOCKING_ID_CACHE_NAME = "LockingIdCache";

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private MaintenanceUtils() {
    }

    /**
     * Determines if there is another maintenance document that has a lock on the same key as the given document, and
     * therefore will block the maintenance document from being submitted
     *
     * @param document               maintenance document instance to check locking for
     * @param throwExceptionIfLocked indicates if an exception should be thrown in the case of found locking document,
     *                               if false only an error will be added
     */
    public static void checkForLockingDocument(final MaintenanceDocument document, final boolean throwExceptionIfLocked) {
        LOG.info("starting checkForLockingDocument (by MaintenanceDocument)");

        // get the docHeaderId of the blocking docs, if any are locked and blocking
        final String blockingDocId = document.getNewMaintainableObject().getLockingDocumentId();
        checkDocumentBlockingDocumentId(blockingDocId, throwExceptionIfLocked);
    }

    public static String findLockingDocId(final MaintenanceDocument document) {
        if (shouldUseCache(document)) {
            LOG.debug("findLockingDocId, allow cache for this document type");
            final Cache cache = getBlockingCache();
            final String cacheKey = buildLockingDocumentCacheKey(document.getDocumentNumber());
            final ValueWrapper valueWrapper = cache.get(cacheKey);
            if (valueWrapper == null) {
                final String lockingDocId = document.getNewMaintainableObject().getLockingDocumentId();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findLockingDocId, locking ID not in cache, had to look it up: " + lockingDocId);
                }
                cache.put(cacheKey, lockingDocId);
                
                return lockingDocId;
            } else {
                final String lockingDocId = (String) valueWrapper.get();
                LOG.debug("findLockingDocId, found locking ID in cache: " + lockingDocId);
                return lockingDocId;
            }
        } else {
            LOG.debug("findLockingDocId, DO NOT allow cache for this document type");
            final String blockingDocId = document.getNewMaintainableObject().getLockingDocumentId();
            return blockingDocId;
        }
    }
    
    /*
     * If additional document types are added, logic needs to be added to clear the cache when new maintenance locks are added.
     * See CuAccountGlobalMaintainableImpl.doRouteStatusChange for example
     */
    public static boolean shouldUseCache(final MaintenanceDocument document) {
        final String docType = document.getDocumentHeader().getWorkflowDocumentTypeName();
        return StringUtils.equalsIgnoreCase(docType, CUKFSConstants.FinancialDocumentTypeCodes.ACCOUNT_GLOBAL);
    }
    
    public static String buildLockingDocumentCacheKey(final String documentNumber) {
        return CUKFSConstants.LOCKING_DOCUMENT_CACHE_KEY + documentNumber;
    }
    
    
    public static Cache getBlockingCache() {
        if (blockingCache == null) {
            final CacheManager cm = CoreImplServiceLocator.getCacheManager();
            blockingCache = cm.getCache(LOCKING_ID_CACHE_NAME);
        }
        return blockingCache;
    }
    
    public static void clearBlockingCache() {
        final Cache cache = getBlockingCache();
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearBlockingCache, clear all blocking cache ");
        }
        cache.clear();
    }
    
    public static boolean shouldClearCacheOnStatusChange(final DocumentHeader documentHeader) {
        final WorkflowDocument wd = documentHeader.getWorkflowDocument();
        return wd.isEnroute() || wd.isProcessed() || wd.isCanceled() || wd.isDisapproved() || wd.isFinal();
    }

    public static void checkDocumentBlockingDocumentId(final String blockingDocId, final boolean throwExceptionIfLocked) {
        // if we got nothing, then no docs are blocking, and we're done
        if (StringUtils.isBlank(blockingDocId)) {
            return;
        }

        LOG.info("Locking document found:  docId = {}.", blockingDocId);

        // load the blocking locked document
        WorkflowDocument lockedDocument = null;
        try {
            // need to perform this check to prevent an exception from being thrown by the
            // createWorkflowDocument call - the throw itself causes transaction rollback problems to
            // occur, even though the exception would be caught here
            if (KewApiServiceLocator.getWorkflowDocumentService().doesDocumentExist(blockingDocId)) {
                
                /*
                 * CU Customization KFSPTS-23120 investigate NPE
                 * Moved the getPerson to a variable to more clearly see what aspect is causing the NPE
                 */
                try {
                    final Person person = GlobalVariables.getUserSession().getPerson();
                    lockedDocument = KewApiServiceLocator.getWorkflowDocumentService()
                        .loadWorkflowDocument(blockingDocId, person);
                } catch (NullPointerException npe) {
                    LOG.error("checkDocumentBlockingDocumentId, caught an NPE getting the locked document with blockingDocId: " + 
                            blockingDocId, npe);
                    throw npe;
                }
                
                
            }
        } catch (final Exception ex) {
            // clean up the lock and notify the admins
            MaintenanceUtils.LOG.error(
                    "Unable to retrieve locking document specified in the maintenance lock table: {}",
                    blockingDocId,
                    ex
            );

            cleanOrphanLocks(blockingDocId);
            return;
        }
        if (lockedDocument == null) {
            MaintenanceUtils.LOG.warn("Locking document header for {}came back null.", blockingDocId);
            cleanOrphanLocks(blockingDocId);
        }

        // if we can ignore the lock (see method notes), then exit cause we're done
        if (lockCanBeIgnored(lockedDocument)) {
            return;
        }

        // build the link URL for the blocking document
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(KRADConstants.PARAMETER_DOC_ID, blockingDocId);
        parameters.put(KRADConstants.PARAMETER_COMMAND, KRADConstants.METHOD_DISPLAY_DOC_SEARCH_VIEW);
        final String blockingUrl = UrlFactory.parameterizeUrl(KRADServiceLocator.getKualiConfigurationService()
                .getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) + "/" + KRADConstants.DOC_HANDLER_ACTION,
                parameters);
        LOG.debug("blockingUrl = '{}'", blockingUrl);
        LOG.debug("Maintenance record: {}is locked.", lockedDocument::getApplicationDocumentId);
        final String[] errorParameters = {blockingUrl, blockingDocId};

        // If specified, add an error to the ErrorMap and throw an exception; otherwise, just add a warning to the
        // ErrorMap instead.
        if (throwExceptionIfLocked) {
            // post an error about the locked document
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS,
                    KFSKeyConstants.ERROR_MAINTENANCE_LOCKED, errorParameters);
            throw new ValidationException("Maintenance Record is locked by another document.");
        } else {
            // Post a warning about the locked document.
            GlobalVariables.getMessageMap().putWarning(KRADConstants.GLOBAL_MESSAGES,
                    WARNING_MAINTENANCE_LOCKED, errorParameters);
        }
    }

    /**
     * Guesses whether the current user should be allowed to change a document even though it is locked. It
     * probably should use Authorization instead? See KULNRVSYS-948
     *
     * @param lockedDocument
     * @return
     */
    private static boolean lockCanBeIgnored(final WorkflowDocument lockedDocument) {
        // TODO: implement real authorization for Maintenance Document Save/Route - KULNRVSYS-948
        if (lockedDocument == null) {
            return true;
        }

        // get the user-id. if no user-id, then we can do this test, so exit
        final String userId = GlobalVariables.getUserSession().getPrincipalId().trim();
        if (StringUtils.isBlank(userId)) {
            // dont bypass locking
            return false;
        }

        // if the current user is not the initiator of the blocking document
        if (!userId.equalsIgnoreCase(lockedDocument.getInitiatorPrincipalId().trim())) {
            return false;
        }

        // if the blocking document hasn't been routed, we can ignore it
        return lockedDocument.isInitiated();
    }

    protected static void cleanOrphanLocks(final String lockingDocumentNumber) {
        // put a try/catch around the whole thing - the whole reason we are doing this is to prevent data errors
        // from stopping a document
        try {
            // delete the locks for this document since it does not seem to exist
            KRADServiceLocatorWeb.getMaintenanceDocumentService().deleteLocks(lockingDocumentNumber);
        } catch (final Exception ex) {
            MaintenanceUtils.LOG.error("Unable to delete and notify upon locking document retrieval failure.", ex);
        }
    }

    public static boolean isMaintenanceDocumentCreatingNewRecord(final String maintenanceAction) {
        return !KRADConstants.MAINTENANCE_EDIT_ACTION.equalsIgnoreCase(maintenanceAction)
                && !KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION.equalsIgnoreCase(maintenanceAction)
                && !KRADConstants.MAINTENANCE_DELETE_ACTION.equalsIgnoreCase(maintenanceAction);
    }
}
