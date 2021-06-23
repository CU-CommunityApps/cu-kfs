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
package org.kuali.kfs.kew.impl.stuck;

import java.util.List;

/*
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 */
public interface StuckDocumentService {

    List<StuckDocument> findAllStuckDocuments();

    StuckDocumentIncident findIncident(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> findIncidents(List<String> stuckDocumentIncidentIds);

    List<StuckDocumentIncident> findAllIncidents();

    List<StuckDocumentIncident> findIncidentsByStatus(StuckDocumentIncident.Status status);

    List<StuckDocumentIncident> recordNewStuckDocumentIncidents();

    StuckDocumentFixAttempt recordNewIncidentFixAttempt(StuckDocumentIncident stuckDocumentIncident);

    List<StuckDocumentFixAttempt> findAllFixAttempts(String stuckDocumentIncidentId);

    int fixAttemptCount(String stuckDocumentIncidentId);

    List<StuckDocumentIncident> resolveIncidentsIfPossible(List<String> stuckDocumentIncidentIds);

    StuckDocumentIncident recordIncidentFailure(StuckDocumentIncident stuckDocumentIncident);

    StuckDocumentIncident startFixingIncident(StuckDocumentIncident stuckDocumentIncident);

    void tryToFix(StuckDocumentIncident incident);
}
