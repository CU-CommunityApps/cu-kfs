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
package org.kuali.kfs.kew.actiontaken.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.actiontaken.dao.ActionTakenDAO;
import org.kuali.kfs.kew.actiontaken.service.ActionTakenService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.principal.Principal;

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

    private static final Logger LOG = LogManager.getLogger();
    private ActionTakenDAO actionTakenDAO;

    public ActionTaken load(String id) {
        return getActionTakenDAO().load(id);
    }

    public ActionTaken findByActionTakenId(String actionTakenId) {
        return getActionTakenDAO().findByActionTakenId(actionTakenId);
    }

    public ActionTaken getPreviousAction(ActionRequest actionRequest) {
        return getPreviousAction(actionRequest, null);
    }

    public ActionTaken getPreviousAction(
            ActionRequest actionRequest, List<ActionTaken> simulatedActionsTaken) {
        GroupService ims = KimApiServiceLocator.getGroupService();
        ActionTaken foundActionTaken = null;
        List<String> principalIds = new ArrayList<String>();
        if (actionRequest.isGroupRequest()) {
            principalIds.addAll(ims.getMemberPrincipalIds(actionRequest.getGroup().getId()));
        } else if (actionRequest.isUserRequest()) {
            principalIds.add(actionRequest.getPrincipalId());
        }

        for (String id : principalIds) {
            List<ActionTaken> actionsTakenByUser =
                    getActionTakenDAO().findByDocumentIdWorkflowId(actionRequest.getDocumentId(), id);
            if (simulatedActionsTaken != null) {
                for (ActionTaken simulatedAction : simulatedActionsTaken) {
                    if (id.equals(simulatedAction.getPrincipalId())) {
                        actionsTakenByUser.add(simulatedAction);
                    }
                }
            }

            for (ActionTaken actionTaken : actionsTakenByUser) {
                if (ActionRequest.compareActionCode(actionTaken.getActionTaken(),
                        actionRequest.getActionRequested(), true) >= 0) {
                    foundActionTaken = actionTaken;
                }
            }
        }

        return foundActionTaken;
    }

    public Collection findByDocIdAndAction(String docId, String action) {
        return getActionTakenDAO().findByDocIdAndAction(docId, action);
    }

    public Collection<ActionTaken> findByDocumentId(String documentId) {
        return getActionTakenDAO().findByDocumentId(documentId);
    }

    public List<ActionTaken> findByDocumentIdWorkflowId(String documentId, String workflowId) {
        return getActionTakenDAO().findByDocumentIdWorkflowId(documentId, workflowId);
    }

    public Collection getActionsTaken(String documentId) {
        return getActionTakenDAO().findByDocumentId(documentId);
    }

    public List findByDocumentIdIgnoreCurrentInd(String documentId) {
        return getActionTakenDAO().findByDocumentIdIgnoreCurrentInd(documentId);
    }

    public void saveActionTaken(ActionTaken actionTaken) {
        this.getActionTakenDAO().saveActionTaken(actionTaken);
    }

    public void delete(ActionTaken actionTaken) {
        getActionTakenDAO().deleteActionTaken(actionTaken);
    }

    public ActionTakenDAO getActionTakenDAO() {
        return actionTakenDAO;
    }

    public void setActionTakenDAO(ActionTakenDAO actionTakenDAO) {
        this.actionTakenDAO = actionTakenDAO;
    }

    public void deleteByDocumentId(String documentId) {
        actionTakenDAO.deleteByDocumentId(documentId);
    }

    public void validateActionTaken(ActionTaken actionTaken) {
        LOG.debug("Enter validateActionTaken(..)");
        List<WorkflowServiceErrorImpl> errors = new ArrayList<WorkflowServiceErrorImpl>();

        String documentId = actionTaken.getDocumentId();
        if (documentId == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken documentid null.", "actiontaken.documentid.empty",
                    actionTaken.getActionTakenId().toString()));
        } else if (getRouteHeaderService().getRouteHeader(documentId) == null) {
            errors.add(
                    new WorkflowServiceErrorImpl("ActionTaken documentid invalid.", "actiontaken.documentid.invalid",
                            actionTaken.getActionTakenId().toString()));
        }

        String principalId = actionTaken.getPrincipalId();
        if (StringUtils.isBlank(principalId)) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken personid null.", "actiontaken.personid.empty",
                    actionTaken.getActionTakenId().toString()));
        } else {
            Principal principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);
            if (principal == null) {
                errors.add(
                        new WorkflowServiceErrorImpl("ActionTaken personid invalid.", "actiontaken.personid.invalid",
                                actionTaken.getActionTakenId().toString()));
            }
        }
        String actionTakenCd = actionTaken.getActionTaken();
        if (actionTakenCd == null || actionTakenCd.trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken cd null.", "actiontaken.actiontaken.empty",
                    actionTaken.getActionTakenId().toString()));
        } else if (!KewApiConstants.ACTION_TAKEN_CD.containsKey(actionTakenCd)) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken invalid.", "actiontaken.actiontaken.invalid",
                    actionTaken.getActionTakenId().toString()));
        }
        if (actionTaken.getActionDate() == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken actiondate null.", "actiontaken.actiondate.empty",
                    actionTaken.getActionTakenId().toString()));
        }

        if (actionTaken.getDocVersion() == null) {
            errors.add(new WorkflowServiceErrorImpl("ActionTaken docversion null.", "actiontaken.docverion.empty",
                    actionTaken.getActionTakenId().toString()));
        }
        LOG.debug("Exit validateActionRequest(..) ");
        if (!errors.isEmpty()) {
            throw new WorkflowServiceErrorException("ActionRequest Validation Error", errors);
        }
    }

    public boolean hasUserTakenAction(String principalId, String documentId) {
        return getActionTakenDAO().hasUserTakenAction(principalId, documentId);
    }

    private RouteHeaderService getRouteHeaderService() {
        return (RouteHeaderService) KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
    }

    public Timestamp getLastApprovedDate(String documentId) {
        return getActionTakenDAO().getLastActionTakenDate(documentId, WorkflowAction.APPROVE);
    }
    
    public Timestamp getLastModifiedDate(String documentId) {
        return getActionTakenDAO().getLastModifiedDate(documentId);
    }

}
