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
package org.kuali.kfs.kew.routeheader.service;

import org.kuali.kfs.kew.actionitem.ActionItemActionListExtension;
import org.kuali.kfs.kew.docsearch.SearchableAttributeValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValueContent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A service providing data access for documents (a.k.a route headers).
 *
 * @see DocumentRouteHeaderValue
 */
/* Cornell customization: backport FINP-8341 */
public interface RouteHeaderService {

    DocumentRouteHeaderValue getRouteHeader(String documentId);

    DocumentRouteHeaderValue getRouteHeader(String documentId, boolean clearCache);

    Map<String, DocumentRouteHeaderValue> getRouteHeadersForActionItems(
            Collection<ActionItemActionListExtension> actionItems);

    void lockRouteHeader(String documentId, boolean wait);

    void saveRouteHeader(DocumentRouteHeaderValue routeHeader);

    void deleteRouteHeader(DocumentRouteHeaderValue routeHeader);

    void validateRouteHeader(DocumentRouteHeaderValue routeHeader);

    Collection<String> findPendingByResponsibilityIds(Set<String> responsibilityIds);

    Collection<String> findPendingByResponsibilityIds(
            Set<String> responsibilityIds,
            Set<String> accountNumbers,
            Set<String> documentTypes
    );

    /**
     * Updates the searchable attribute values for the document with the given id to the given values.
     * This method will clear existing search attribute values and replace with the ones given.
     */
    void updateRouteHeaderSearchValues(String documentId, List<SearchableAttributeValue> searchAttributes);

    DocumentRouteHeaderValueContent getContent(String documentId);

    boolean hasSearchableAttributeValue(String documentId, String searchableAttributeKey,
            String searchableAttributeValue);

    String getDocumentStatus(String documentId);

    /**
     * This method is a more direct way to get the searchable attribute values
     *
     * @param documentId
     * @param key
     * @return
     */
    List<String> getSearchableAttributeStringValuesByKey(String documentId, String key);
}
