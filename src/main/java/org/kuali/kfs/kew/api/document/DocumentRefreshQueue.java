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

/**
 * CU Customization: Backported the FINP-9050 changes into this file, adjusting for compatibility as needed.
 * This overlay can be removed when we upgrade to the 2023-02-08 financials patch.
 * ============
 * 
 * Defines the contract for a message queue which "refreshes" a document at its current node.  The refresh process will
 * delete all pending action requests at the current node(s) on the document and then send the document back through at
 * its current node(s) so requests can be regenerated.
 */
public interface DocumentRefreshQueue {

    void refreshDocument(String documentId);

    void refreshDocument(String documentId, String annotation);
}
