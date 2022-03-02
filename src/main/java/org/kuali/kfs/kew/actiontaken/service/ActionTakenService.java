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
package org.kuali.kfs.kew.actiontaken.service;

import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * ====
 *  * CU Customization: Added handling of the custom "getLastModifiedDate" action list preference.
 * ====
 */

/**
 * Responsible for the data access for {@link ActionTaken} objects.
 */
public interface ActionTakenService {

    ActionTaken findByActionTakenId(String actionTakenId);

    void saveActionTaken(ActionTaken actionTaken);

    ActionTaken getPreviousAction(ActionRequest actionRequest);

    ActionTaken getPreviousAction(ActionRequest actionRequest, List<ActionTaken> simulatedActionsTaken);

    Collection<ActionTaken> findByDocumentId(String documentId);

    List<ActionTaken> findByDocumentIdWorkflowId(String documentId, String workflowId);

    void delete(ActionTaken actionTaken);

    List findByDocumentIdIgnoreCurrentInd(String documentId);

    boolean hasUserTakenAction(String principalId, String documentId);

    Timestamp getLastApprovedDate(String documentId);
    
    Timestamp getLastModifiedDate(String documentId);
}
