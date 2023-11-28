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
package org.kuali.kfs.kew.actiontaken.service.impl;

import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.actiontaken.dao.ActionTakenDAO;
import org.kuali.kfs.kew.actiontaken.service.ActionTakenService;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kim.api.group.GroupService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ====
 *  * CU Customization: Added handling of the custom "getLastModifiedDate" action list preference.
 * ====
 */

/**
 * Default implementation of the {@link ActionTakenService}.
 */
public class ActionTakenServiceImpl implements ActionTakenService {

    private ActionTakenDAO actionTakenDAO;

    private GroupService groupService;

    @Override
    public ActionTaken findByActionTakenId(final String actionTakenId) {
        return getActionTakenDAO().findByActionTakenId(actionTakenId);
    }

    @Override
    public ActionTaken getPreviousAction(final ActionRequest actionRequest) {
        return getPreviousAction(actionRequest, null);
    }

    @Override
    public ActionTaken getPreviousAction(
            final ActionRequest actionRequest, final List<ActionTaken> simulatedActionsTaken) {
        final GroupService ims = groupService;
        ActionTaken foundActionTaken = null;
        final List<String> principalIds = new ArrayList<>();
        if (actionRequest.isGroupRequest()) {
            principalIds.addAll(ims.getMemberPrincipalIds(actionRequest.getGroup().getId()));
        } else if (actionRequest.isUserRequest()) {
            principalIds.add(actionRequest.getPrincipalId());
        }

        for (final String id : principalIds) {
            final List<ActionTaken> actionsTakenByUser =
                    getActionTakenDAO().findByDocumentIdWorkflowId(actionRequest.getDocumentId(), id);
            if (simulatedActionsTaken != null) {
                for (final ActionTaken simulatedAction : simulatedActionsTaken) {
                    if (id.equals(simulatedAction.getPrincipalId())) {
                        actionsTakenByUser.add(simulatedAction);
                    }
                }
            }

            for (final ActionTaken actionTaken : actionsTakenByUser) {
                if (ActionRequest.compareActionCode(actionTaken.getActionTaken(),
                        actionRequest.getActionRequested(), true) >= 0) {
                    foundActionTaken = actionTaken;
                }
            }
        }

        return foundActionTaken;
    }

    @Override
    public Collection<ActionTaken> findByDocumentId(final String documentId) {
        return getActionTakenDAO().findByDocumentId(documentId);
    }

    @Override
    public List<ActionTaken> findByDocumentIdWorkflowId(final String documentId, final String workflowId) {
        return getActionTakenDAO().findByDocumentIdWorkflowId(documentId, workflowId);
    }

    @Override
    public List findByDocumentIdIgnoreCurrentInd(final String documentId) {
        return getActionTakenDAO().findByDocumentIdIgnoreCurrentInd(documentId);
    }

    @Override
    public void saveActionTaken(final ActionTaken actionTaken) {
        getActionTakenDAO().saveActionTaken(actionTaken);
    }

    @Override
    public void delete(final ActionTaken actionTaken) {
        getActionTakenDAO().deleteActionTaken(actionTaken);
    }

    public ActionTakenDAO getActionTakenDAO() {
        return actionTakenDAO;
    }

    public void setActionTakenDAO(final ActionTakenDAO actionTakenDAO) {
        this.actionTakenDAO = actionTakenDAO;
    }

    @Override
    public boolean hasUserTakenAction(final String principalId, final String documentId) {
        return getActionTakenDAO().hasUserTakenAction(principalId, documentId);
    }

    @Override
    public Timestamp getLastApprovedDate(final String documentId) {
        return getActionTakenDAO().getLastActionTakenDate(documentId, WorkflowAction.APPROVE);
    }
    
    public Timestamp getLastModifiedDate(String documentId) {
        return getActionTakenDAO().getLastModifiedDate(documentId);
    }

    public void setGroupService(final GroupService groupService) {
        this.groupService = groupService;
    }
}
