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
package org.kuali.kfs.kew.routeheader.dao;

import org.kuali.kfs.kew.docsearch.SearchableAttributeValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValueContent;

import java.util.Collection;
import java.util.Set;

/* Cornell customization: backport FINP-8341 */
public interface DocumentRouteHeaderDAO {

    void saveRouteHeader(DocumentRouteHeaderValue routeHeader);

    /**
     * "Locks" the route header at the datasource level.
     */
    void lockRouteHeader(String documentId, boolean wait);

    DocumentRouteHeaderValue findRouteHeader(String documentId);

    DocumentRouteHeaderValue findRouteHeader(String documentId, boolean clearCache);

    Collection<DocumentRouteHeaderValue> findRouteHeaders(Collection<String> documentIds);

    Collection<DocumentRouteHeaderValue> findRouteHeaders(Collection<String> documentIds, boolean clearCache);

    void deleteRouteHeader(DocumentRouteHeaderValue routeHeader);

    String getNextDocumentId();

    Collection<String> findPendingByResponsibilityIds(Set<String> responsibilityIds);
    
    /* backport FINP-8341 */
    Collection<String> findPendingByResponsibilityIds(
            Set<String> responsibilityIds,
            Set<String> accountNumbers,
            Set<String> documentTypes
    );

    void clearRouteHeaderSearchValues(String documentId);

    DocumentRouteHeaderValueContent getContent(String documentId);

    boolean hasSearchableAttributeValue(String documentId, String searchableAttributeKey,
            String searchableAttributeValue);

    String getDocumentStatus(String documentId);

    void save(SearchableAttributeValue searchableAttribute);

    String getAppDocId(String documentId);

    String getAppDocStatus(String documentId);

    Collection findByDocTypeAndAppId(String documentTypeName, String appId);

}
