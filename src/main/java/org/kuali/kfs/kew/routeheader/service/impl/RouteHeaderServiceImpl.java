/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.kew.routeheader.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionitem.ActionItemActionListExtension;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.docsearch.SearchableAttributeValue;
import org.kuali.kfs.kew.docsearch.dao.SearchableAttributeDAO;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValueContent;
import org.kuali.kfs.kew.routeheader.dao.DocumentRouteHeaderDAO;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.principal.Principal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* Cornell customization: backport FINP-8341 */
public class RouteHeaderServiceImpl implements RouteHeaderService {

    private static final Logger LOG = LogManager.getLogger();

    private DocumentRouteHeaderDAO routeHeaderDAO;
    private SearchableAttributeDAO searchableAttributeDAO;

    @Override
    public DocumentRouteHeaderValue getRouteHeader(String documentId) {
        return getRouteHeaderDAO().findRouteHeader(documentId);
    }

    @Override
    public DocumentRouteHeaderValue getRouteHeader(String documentId, boolean clearCache) {
        return getRouteHeaderDAO().findRouteHeader(documentId, clearCache);
    }

    private Collection<DocumentRouteHeaderValue> getRouteHeaders(Collection<String> documentIds) {
        return getRouteHeaderDAO().findRouteHeaders(documentIds);
    }

    @Override
    public Map<String, DocumentRouteHeaderValue> getRouteHeadersForActionItems(
            Collection<ActionItemActionListExtension> actionItems) {
        Map<String, DocumentRouteHeaderValue> routeHeaders = new HashMap<>();
        List<String> documentIds = new ArrayList<>(actionItems.size());
        for (ActionItem actionItem : actionItems) {
            documentIds.add(actionItem.getDocumentId());
        }
        Collection<DocumentRouteHeaderValue> actionItemRouteHeaders = getRouteHeaders(documentIds);
        if (actionItemRouteHeaders != null) {
            for (DocumentRouteHeaderValue routeHeader : actionItemRouteHeaders) {
                routeHeaders.put(routeHeader.getDocumentId(), routeHeader);
            }
        }
        return routeHeaders;
    }

    @Override
    public void lockRouteHeader(String documentId, boolean wait) {
        getRouteHeaderDAO().lockRouteHeader(documentId, wait);
        LOG.debug("Successfully locked document [docId={}]", documentId);
    }

    @Override
    public void saveRouteHeader(DocumentRouteHeaderValue routeHeader) {
        this.getRouteHeaderDAO().saveRouteHeader(routeHeader);
    }

    @Override
    public void deleteRouteHeader(DocumentRouteHeaderValue routeHeader) {
        getRouteHeaderDAO().deleteRouteHeader(routeHeader);
    }

    @Override
    public Collection<String> findPendingByResponsibilityIds(Set responsibilityIds) {
        return routeHeaderDAO.findPendingByResponsibilityIds(responsibilityIds);
    }

    @Override
    public Collection<String> findPendingByResponsibilityIds(
            final Set<String> responsibilityIds,
            final Set<String> accountNumbers,
            final Set<String> documentTypes
    ) {
        return routeHeaderDAO.findPendingByResponsibilityIds(responsibilityIds, accountNumbers, documentTypes);
    }

    @Override
    public void updateRouteHeaderSearchValues(String documentId, List<SearchableAttributeValue> searchAttributes) {
        getRouteHeaderDAO().clearRouteHeaderSearchValues(documentId);
        for (SearchableAttributeValue searchAttribute : searchAttributes) {
            getRouteHeaderDAO().save(searchAttribute);
        }
    }

    @Override
    public void validateRouteHeader(DocumentRouteHeaderValue routeHeader) {
        LOG.debug("Enter validateRouteHeader(..)");
        List errors = new ArrayList();

        if (routeHeader.getDocRouteStatus() == null || routeHeader.getDocRouteStatus().trim().equals("")) {
            errors.add(
                    new WorkflowServiceErrorImpl("RouteHeader route status null.", "routeheader.routestatus.empty"));
        } else if (!KewApiConstants.DOCUMENT_STATUSES.containsKey(routeHeader.getDocRouteStatus())) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader route status invalid.",
                    "routeheader.routestatus.invalid"));
        }

        if (routeHeader.getDocRouteLevel() == null || routeHeader.getDocRouteLevel() < 0) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader route level invalid.",
                    "routeheader.routelevel.invalid"));
        }

        if (routeHeader.getDateLastModified() == null) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader status modification date empty.",
                    "routeheader.statusmoddate.empty"));
        }

        if (routeHeader.getCreateDate() == null) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader status create date empty.",
                    "routeheader.createdate.empty"));
        }
        if (routeHeader.getDocVersion() == null || routeHeader.getDocVersion() < 0) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader doc version invalid.",
                    "routeheader.docversion.invalid"));
        }

        if (routeHeader.getInitiatorWorkflowId() == null || routeHeader.getInitiatorWorkflowId().trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("RouteHeader initiator null.", "routeheader.initiator.empty"));
        } else {
            Principal principal =
                    KimApiServiceLocator.getIdentityService().getPrincipal(routeHeader.getInitiatorWorkflowId());
            if (principal == null) {
                errors.add(new WorkflowServiceErrorImpl("RouteHeader initiator id invalid.",
                        "routeheader.initiator.invalid"));
            }
        }

        if (StringUtils.isNotBlank(routeHeader.getDocumentTypeId())) {
            DocumentType docType =
                    KEWServiceLocator.getDocumentTypeService().findById(routeHeader.getDocumentTypeId());
            if (docType == null) {
                errors.add(new WorkflowServiceErrorImpl("RouteHeader document type id invalid.",
                        "routeheader.doctypeid.invalid"));
            }
        }

        LOG.debug("Exit validateRouteHeader(..) ");
        if (!errors.isEmpty()) {
            throw new WorkflowServiceErrorException("RouteHeader Validation Error", errors);
        }
    }

    @Override
    public DocumentRouteHeaderValueContent getContent(String documentId) {
        if (documentId == null) {
            return new DocumentRouteHeaderValueContent();
        }
        DocumentRouteHeaderValueContent content = getRouteHeaderDAO().getContent(documentId);
        if (content == null) {
            content = new DocumentRouteHeaderValueContent(documentId);
        }
        return content;
    }

    @Override
    public boolean hasSearchableAttributeValue(
            String documentId, String searchableAttributeKey, String searchableAttributeValue) {
        return getRouteHeaderDAO()
                .hasSearchableAttributeValue(documentId, searchableAttributeKey, searchableAttributeValue);
    }

    @Override
    public String getDocumentStatus(String documentId) {
        return getRouteHeaderDAO().getDocumentStatus(documentId);
    }

    public DocumentRouteHeaderDAO getRouteHeaderDAO() {
        return routeHeaderDAO;
    }

    public void setRouteHeaderDAO(DocumentRouteHeaderDAO routeHeaderDAO) {
        this.routeHeaderDAO = routeHeaderDAO;
    }

    @Override
    public List<String> getSearchableAttributeStringValuesByKey(
            String documentId, String key) {

        return getSearchableAttributeDAO().getSearchableAttributeStringValuesByKey(documentId, key);
    }

    public void setSearchableAttributeDAO(SearchableAttributeDAO searchableAttributeDAO) {
        this.searchableAttributeDAO = searchableAttributeDAO;
    }

    public SearchableAttributeDAO getSearchableAttributeDAO() {
        return searchableAttributeDAO;
    }

}
