/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.kew.doctype.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.kuali.kfs.core.api.impex.ExportDataSet;
import org.kuali.kfs.kew.api.doctype.RoutePath;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.dao.DocumentTypeDAO;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.engine.node.RouteNode;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.xml.DocumentTypeXmlParser;
import org.kuali.kfs.kew.xml.export.DocumentTypeXmlExporter;
import org.kuali.kfs.kim.impl.permission.Permission;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/* Cornell Customization: backport redis */
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private static final Logger LOG = LogManager.getLogger();
    private static final String XML_FILE_PARSE_ERROR = "general.error.parsexml";

    private DocumentTypeDAO documentTypeDAO;

    @Cacheable(cacheNames = DocumentType.CACHE_NAME,
            key = "'{BO}' + 'documentTypeId=' + #p0.getId() + '|' + 'name=' + #p0.getName() + '|' + 'label=' + " +
                    "#p0.getLabel() + '|' +'docGroupName=' + #p1 + '|' + 'climbHierarchy=' + #p2")
    public Collection<DocumentType> find(DocumentType documentType, String docTypeParentName, boolean climbHierarchy) {
        DocumentType docTypeParent = this.findByName(docTypeParentName);
        return getDocumentTypeDAO().find(documentType, docTypeParent, climbHierarchy);
    }

    @Cacheable(cacheNames = DocumentType.CACHE_NAME, key = "'{BO}' + 'documentTypeId=' + #p0")
    public DocumentType findById(String documentTypeId) {
        if (documentTypeId == null) {
            return null;
        }

        return getDocumentTypeDAO().findById(documentTypeId);
    }

    @Cacheable(cacheNames = DocumentType.CACHE_NAME, key = "'{BO}' + 'documentId=' + #p0")
    public DocumentType findByDocumentId(String documentId) {
        if (documentId == null) {
            return null;
        }
        String documentTypeId = getDocumentTypeDAO().findDocumentTypeIdByDocumentId(documentId);
        return findById(documentTypeId);
    }

    @Cacheable(cacheNames = DocumentType.CACHE_NAME, key = "'{BO}' + 'name=' + #p0")
    public DocumentType findByName(String name) {
        return this.findByName(name, true);
    }

    /**
     * This method searches for a DocumentType by document name.
     *
     * @param name name used to search for a doc type
     * @param caseSensitive whether the search is case sensitive
     * @return doc type if one was found, null if not, or if name is null
     */
    private DocumentType findByName(String name, boolean caseSensitive) {
        if (name == null) {
            return null;
        }
        return getDocumentTypeDAO().findByName(name, caseSensitive);
    }

    public DocumentType findByNameCaseInsensitive(String name) {
        return this.findByName(name, false);
    }

    @Override
    public DocumentType updateDocumentTypeIfNecessary(String documentTypeName, DocumentType currentDocumentType) {
        DocumentType documentType = currentDocumentType;

        if (StringUtils.isBlank(documentTypeName)) {
            documentType = null;
        } else if (currentDocumentType == null || !StringUtils.equals(documentTypeName,
                currentDocumentType.getName())) {
            documentType = getDocumentTypeByName(documentTypeName);
        }

        return documentType;
    }

    @CacheEvict(value = {DocumentType.CACHE_NAME, Permission.CACHE_NAME},
            allEntries = true)
    public void versionAndSave(DocumentType documentType) {
        // at this point this save is designed to version the document type by creating an entire new record if this
        // is going to be an update and not a create just throw and exception to be on the safe side
        if (documentType.getDocumentTypeId() != null && documentType.getVersionNumber() != null) {
            throw new RuntimeException("DocumentType configured for update and not versioning which we support");
        }

        // grab the old document. Don't Use Cached Version!
        DocumentType oldDocumentType = findByName(documentType.getName());
        // reset the children on the oldDocumentType
        //oldDocumentType.resetChildren();
        String existingDocTypeId = null;
        if (oldDocumentType != null) {
            existingDocTypeId = oldDocumentType.getDocumentTypeId();
            // set version number on the new doc type using the max version from the database
            Integer maxVersionNumber = documentTypeDAO.getMaxVersionNumber(documentType.getName());
            documentType.setVersion(
                    maxVersionNumber != null ? Integer.valueOf(maxVersionNumber + 1) : Integer.valueOf(0));
            oldDocumentType.setCurrentInd(Boolean.FALSE);
            if (LOG.isInfoEnabled()) {
                LOG.info("Saving old document type Id " + oldDocumentType.getDocumentTypeId() + " name '" +
                        oldDocumentType.getName() + "' (current = " + oldDocumentType.getCurrentInd() + ")");
            }
            save(oldDocumentType);
        }
        // check to see that no current documents exist in database
        if (!CollectionUtils.isEmpty(documentTypeDAO.findAllCurrentByName(documentType.getName()))) {
            String errorMsg = "Found invalid 'current' document with name '" + documentType.getName() +
                    "'.  None should exist.";
            LOG.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        // set up the previous current doc type on the new doc type
        documentType.setPreviousVersionId(existingDocTypeId);
        documentType.setCurrentInd(Boolean.TRUE);
        save(documentType);
        if (LOG.isInfoEnabled()) {
            LOG.info("Saved current document type Id " + documentType.getDocumentTypeId() + " name '" +
                    documentType.getName() + "' (current = " + documentType.getCurrentInd() + ")");
        }
        //attach the children to this new parent.  cloning the children would probably be a better way to go here...
        if (ObjectUtils.isNotNull(existingDocTypeId)) {
            // documentType.getPreviousVersion() should not be null at this point
            for (DocumentType child : getChildDocumentTypes(existingDocTypeId)) {
                child.setDocTypeParentId(documentType.getDocumentTypeId());
                save(child);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Saved child document type Id " + child.getDocumentTypeId() + " name '" +
                            child.getName() + "' (parent = " + child.getDocTypeParentId() + ", current = " +
                            child.getCurrentInd() + ")");
                }
            }
        }
        // initiate a save of this document type's parent document type, this will force a version check which should
        // reveal (via an optimistic lock exception) whether or not there is a concurrent transaction which has
        // modified the parent (and therefore made it non-current) be sure to get the parent doc type directly from
        // the db and not from the cache
        if (documentType.getDocTypeParentId() != null) {
            DocumentType parent = getDocumentTypeDAO().findById(documentType.getDocTypeParentId());
            save(parent);
            if (LOG.isInfoEnabled()) {
                LOG.info("Saved parent document type Id " + parent.getDocumentTypeId() + " name '" +
                        parent.getName() + "' (current = " + parent.getCurrentInd() + ")");
            }
        }
    }

    @CacheEvict(value = {DocumentType.CACHE_NAME, Permission.CACHE_NAME}, allEntries = true)
    public void save(DocumentType documentType) {
        getDocumentTypeDAO().save(documentType);
    }

    @Cacheable(cacheNames = DocumentType.CACHE_NAME, key = "'{BO}allCurrentRootDocuments'")
    public synchronized List<DocumentType> findAllCurrentRootDocuments() {
        return getDocumentTypeDAO().findAllCurrentRootDocuments();
    }

    @Cacheable(value = DocumentType.CACHE_NAME,
            key = "'{BO}{previousInstances}' + 'documentTypeName=' + #p0")
    public List<DocumentType> findPreviousInstances(String documentTypeName) {
        return getDocumentTypeDAO().findPreviousInstances(documentTypeName);
    }

    @Cacheable(value = DocumentType.CACHE_NAME,
            key = "'{BO}{root}' + 'documentTypeId=' + #p0.getId()")
    public DocumentType findRootDocumentType(DocumentType docType) {
        if (docType.getParentDocType() != null) {
            return findRootDocumentType(docType.getParentDocType());
        } else {
            return docType;
        }
    }

    public void loadXml(InputStream inputStream, String principalId) {
        DocumentTypeXmlParser parser = new DocumentTypeXmlParser();
        try {
            parser.parseDocumentTypes(inputStream);
        } catch (Exception e) {
            WorkflowServiceErrorException wsee =
                    new WorkflowServiceErrorException("Error parsing documentType XML file",
                            new WorkflowServiceErrorImpl("Error parsing documentType XML file",
                                    XML_FILE_PARSE_ERROR));
            wsee.initCause(e);
            throw wsee;
        }
    }

    public Element export(ExportDataSet dataSet) {
        DocumentTypeXmlExporter exporter = new DocumentTypeXmlExporter();
        return exporter.export(dataSet);
    }

    @Override
    public boolean supportPrettyPrint() {
        return true;
    }

    @Cacheable(value = DocumentType.CACHE_NAME,
            key = "'{BO}{childDocumentTypes}' + 'documentTypeId=' + #p0")
    public List<DocumentType> getChildDocumentTypes(String documentTypeId) {
        List<DocumentType> childDocumentTypes = new ArrayList<>();
        List<String> childIds = getDocumentTypeDAO().getChildDocumentTypeIds(documentTypeId);
        for (String childDocumentTypeId : childIds) {
            childDocumentTypes.add(findById(childDocumentTypeId));
        }
        return childDocumentTypes;
    }

    @Override
    public String getIdByName(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName was null or blank");
        }
        return documentTypeDAO.findDocumentTypeIdByName(documentTypeName);
    }

    @Cacheable(value = DocumentType.CACHE_NAME, key = "'documentTypeId=' + #p0")
    @Override
    public DocumentType getDocumentTypeById(String documentTypeId) {
        if (StringUtils.isBlank(documentTypeId)) {
            throw new IllegalArgumentException("documentTypeId was null or blank");
        }
        return documentTypeDAO.findById(documentTypeId);
    }

    @Cacheable(cacheNames = DocumentType.CACHE_NAME, key = "'{getDocumentTypeByName}|documentTypeName=' + #p0")
    @Override
    public DocumentType getDocumentTypeByName(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName was null or blank");
        }
        return documentTypeDAO.findByName(documentTypeName);
    }

    @Override
    public boolean isSuperUserForDocumentTypeId(String principalId, String documentTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Determining super user status [principalId=" + principalId + ", documentTypeId="
                    + documentTypeId + "]");
        }
        if (StringUtils.isBlank(principalId)) {
            throw new IllegalArgumentException("principalId was null or blank");
        }
        if (StringUtils.isBlank(documentTypeId)) {
            throw new IllegalArgumentException("documentTypeId was null or blank");
        }
        DocumentType documentType =
                KEWServiceLocator.getDocumentTypeService().findById(documentTypeId);
        boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService().canAdministerRouting(principalId,
                documentType);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Super user status is " + isSuperUser + ".");
        }
        return isSuperUser;

    }

    @Override
    public boolean canSuperUserApproveSingleActionRequest(String principalId, String documentTypeName,
            List<RouteNodeInstance> routeNodeInstances, String routeStatusCode) {

        checkSuperUserInput(principalId, documentTypeName);

        DocumentType documentType =
                KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        List<org.kuali.kfs.kew.engine.node.RouteNodeInstance> currentNodeInstances = null;
        if (routeNodeInstances != null && !routeNodeInstances.isEmpty()) {
            currentNodeInstances = KEWServiceLocator.getRouteNodeService()
                    .getCurrentNodeInstances(routeNodeInstances.get(0).getDocumentId());
        }

        boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService()
                .canSuperUserApproveSingleActionRequest(principalId, documentType,
                        currentNodeInstances, routeStatusCode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Super user approve single action request status is " + isSuperUser + ".");
        }
        return isSuperUser;
    }

    @Override
    public boolean canSuperUserApproveDocument(String principalId, String documentTypeName,
            List<RouteNodeInstance> routeNodeInstances, String routeStatusCode) {
        checkSuperUserInput(principalId, documentTypeName);

        DocumentType documentType =
                KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        List<org.kuali.kfs.kew.engine.node.RouteNodeInstance> currentNodeInstances = null;
        if (routeNodeInstances != null && !routeNodeInstances.isEmpty()) {
            currentNodeInstances = KEWServiceLocator.getRouteNodeService()
                    .getCurrentNodeInstances(routeNodeInstances.get(0).getDocumentId());
        }

        boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService().canSuperUserApproveDocument(
                principalId, documentType, currentNodeInstances, routeStatusCode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Super user approve document status is " + isSuperUser + ".");
        }
        return isSuperUser;
    }

    @Override
    public boolean canSuperUserDisapproveDocument(String principalId, String documentTypeName,
            List<RouteNodeInstance> routeNodeInstances, String routeStatusCode) {
        checkSuperUserInput(principalId, documentTypeName);

        DocumentType documentType =
                KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);

        List<org.kuali.kfs.kew.engine.node.RouteNodeInstance> currentNodeInstances = null;
        if (routeNodeInstances != null && !routeNodeInstances.isEmpty()) {
            currentNodeInstances = KEWServiceLocator.getRouteNodeService()
                    .getCurrentNodeInstances(routeNodeInstances.get(0).getDocumentId());
        }

        boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService()
                .canSuperUserDisapproveDocument(principalId, documentType,
                        currentNodeInstances, routeStatusCode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Super user disapprove document status is " + isSuperUser + ".");
        }
        return isSuperUser;
    }

    private void checkSuperUserInput(String principalId, String documentTypeName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Determining super user status [principalId=" + principalId + ", documentTypeName="
                    + documentTypeName + "]");
        }
        if (StringUtils.isBlank(principalId)) {
            throw new IllegalArgumentException("principalId was null or blank");
        }
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeId was null or blank");
        }
    }

    @Override
    public boolean hasRouteNodeForDocumentTypeName(String routeNodeName, String documentTypeName)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(routeNodeName)) {
            throw new IllegalArgumentException("routeNodeName was null or blank");
        }
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName was null or blank");
        }

        DocumentType documentType = getDocumentTypeByName(documentTypeName);
        if (documentType == null) {
            throw new IllegalArgumentException(
                    "Failed to locate a document type for the given name: " + documentTypeName);
        }
        RouteNode routeNode =
                KEWServiceLocator.getRouteNodeService().findRouteNodeByName(documentType.getId(), routeNodeName);

        if (routeNode == null) {
            if (documentType.getParentId() == null) {
                return false;
            } else {
                return hasRouteNodeForDocumentTypeId(routeNodeName, documentType.getParentId());
            }
        } else {
            return true;
        }
    }

    private boolean hasRouteNodeForDocumentTypeId(String routeNodeName, String documentTypeId)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(routeNodeName)) {
            throw new IllegalArgumentException("routeNodeName was null or blank");
        }
        if (StringUtils.isBlank(documentTypeId)) {
            throw new IllegalArgumentException("documentTypeId was null or blank");
        }

        DocumentType documentType = getDocumentTypeById(documentTypeId);
        if (documentType == null) {
            throw new IllegalArgumentException(
                    "Failed to locate a document type for the given id: " + documentTypeId);
        }
        RouteNode routeNode =
                KEWServiceLocator.getRouteNodeService().findRouteNodeByName(documentType.getId(), routeNodeName);

        if (routeNode == null) {
            if (documentType.getParentId() == null) {
                return false;
            } else {
                return hasRouteNodeForDocumentTypeId(routeNodeName, documentType.getParentId());
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean isActiveById(String documentTypeId) {
        if (StringUtils.isBlank(documentTypeId)) {
            throw new IllegalArgumentException("documentTypeId was null or blank");
        }
        DocumentType docType =
                KEWServiceLocator.getDocumentTypeService().findById(documentTypeId);
        return docType != null && docType.isActive();
    }

    @Override
    public boolean isActiveByName(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName was null or blank");
        }
        DocumentType docType =
                KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        return docType != null && docType.isActive();
    }

    @Cacheable(value = RoutePath.CACHE_NAME, key = "'{getRoutePathForDocumentTypeName}|documentTypeName=' + #p0")
    @Override
    public RoutePath getRoutePathForDocumentTypeName(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName was null or blank");
        }
        DocumentType docType =
                KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        if (docType == null) {
            return null;
        }
        return new RoutePath(docType.getProcesses());

    }

    public DocumentTypeDAO getDocumentTypeDAO() {
        return documentTypeDAO;
    }

    public void setDocumentTypeDAO(DocumentTypeDAO documentTypeDAO) {
        this.documentTypeDAO = documentTypeDAO;
    }
}
