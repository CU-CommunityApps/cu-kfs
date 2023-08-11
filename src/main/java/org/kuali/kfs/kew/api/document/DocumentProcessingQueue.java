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
package org.kuali.kfs.kew.api.document;

public interface DocumentProcessingQueue {

    void process(String documentId) throws IllegalArgumentException;

    void processWithOptions(String documentId, DocumentProcessingOptions options) throws IllegalArgumentException;

    /*
     * CU Customization: Added a separate method for handling the FINP-8541 functionality (which checks whether
     * a document has been routed and, if not, will add a Complete request if one doesn't exist). This is needed
     * to fix a DB transaction issue, where merely retrieving the route header can cause it to remain stale
     * when subsequently invoking the workflow engine.
     */
    default boolean shouldProceedWithDocumentProcessing(String documentId) {
        return true;
    }
}
