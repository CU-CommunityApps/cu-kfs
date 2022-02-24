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
package org.kuali.kfs.kew.actiontaken.dao;

import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.action.WorkflowAction;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * ====
 *  * CU Customization: Added handling of the custom "getLastModifiedDate" action list preference.
 * ====
 */

/**
 * Data Access Object for {@link ActionTaken}s.
 */
public interface ActionTakenDAO {

    ActionTaken load(String id);

    void saveActionTaken(ActionTaken actionTaken);

    void deleteActionTaken(ActionTaken actionTaken);

    ActionTaken findByActionTakenId(String actionTakenId);

    Collection<ActionTaken> findByDocumentId(String documentId);

    Collection<ActionTaken> findByDocIdAndAction(String docId, String action);

    List<ActionTaken> findByDocumentIdWorkflowId(String documentId, String workflowId);

    List findByDocumentIdIgnoreCurrentInd(String documentId);

    void deleteByDocumentId(String documentId);

    boolean hasUserTakenAction(String workflowId, String documentId);

    Timestamp getLastActionTakenDate(String documentId, WorkflowAction actionType);
    
    Timestamp getLastModifiedDate(String documentId);
}
