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
package org.kuali.kfs.kew.documentoperation.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionlist.service.ActionListService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.actiontaken.service.ActionTakenService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.WorkflowDocumentFactory;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.ActionInvocation;
import org.kuali.kfs.kew.api.action.ActionInvocationQueue;
import org.kuali.kfs.kew.api.action.ActionType;
import org.kuali.kfs.kew.api.document.DocumentOrchestrationConfig;
import org.kuali.kfs.kew.api.document.DocumentOrchestrationQueue;
import org.kuali.kfs.kew.api.document.DocumentProcessingOptions;
import org.kuali.kfs.kew.api.document.DocumentProcessingQueue;
import org.kuali.kfs.kew.api.document.DocumentRefreshQueue;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.engine.node.Branch;
import org.kuali.kfs.kew.engine.node.BranchState;
import org.kuali.kfs.kew.engine.node.NodeState;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.engine.node.service.BranchService;
import org.kuali.kfs.kew.engine.node.service.RouteNodeService;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.web.KewKualiAction;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Struts Action for doing editing of workflow documents.
 */
public class DocumentOperationAction extends KewKualiAction {

    private static final Logger LOG = LogManager.getLogger();
    private static final String DEFAULT_LOG_MSG = "Admin change via document operation";

    private BusinessObjectService businessObjectService;

    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        return mapping.findForward("basic");
    }

    public ActionForward getDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        String docId = null;

        // check if we have a plausible docId first
        if (StringUtils.isEmpty(docForm.getDocumentId())) {
            GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_REQUIRED, "Document ID");
        } else {
            try {
                docId = docForm.getDocumentId().trim();
            } catch (NumberFormatException nfe) {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_NUMERIC, "Document ID");
            }
        }

        if (docId != null) {
            //to clear Document Field first;
            docForm.resetOps();
            DocumentRouteHeaderValue routeHeader = getRouteHeaderService().getRouteHeader(docId);
            List routeNodeInstances = getRouteNodeService().findRouteNodeInstances(docId);
            Map branches1 = new HashMap();
            List branches = new ArrayList();

            if (routeHeader == null) {
                GlobalVariables.getMessageMap().putError("documentId", KFSKeyConstants.ERROR_EXISTENCE, "document");
            } else {
                docForm.setRouteHeader(routeHeader);
                setRouteHeaderTimestampsToString(docForm);
                docForm.setRouteHeaderOp(KewApiConstants.NOOP);
                docForm.setDocumentId(docForm.getDocumentId().trim());
                String initials = "";
                for (Iterator lInitials = routeHeader.getInitialRouteNodeInstances().iterator(); lInitials
                        .hasNext(); ) {
                    String initial = ((RouteNodeInstance) lInitials.next()).getRouteNodeInstanceId();
                    LOG.debug(initial);
                    initials = initials + initial + ", ";
                }
                if (initials.trim().length() > 1) {
                    initials = initials.substring(0, initials.lastIndexOf(","));
                }
                docForm.setInitialNodeInstances(initials);
                request.getSession().setAttribute("routeNodeInstances", routeNodeInstances);
                docForm.setRouteNodeInstances(routeNodeInstances);
                if (routeNodeInstances != null) {
                    Iterator routeNodeInstanceIter = routeNodeInstances.iterator();
                    while (routeNodeInstanceIter.hasNext()) {
                        RouteNodeInstance routeNodeInstance = (RouteNodeInstance) routeNodeInstanceIter.next();
                        Branch branch = routeNodeInstance.getBranch();
                        if (!branches1.containsKey(branch.getName())) {
                            branches1.put(branch.getName(), branch);
                            branches.add(branch);
                            LOG.debug(branch.getName() + "; " + branch.getBranchState());
                        }
                    }
                    if (branches.size() < 1) {
                        branches = null;
                    }
                }
                branches1.clear();
                request.getSession().setAttribute("branches", branches);
                docForm.setBranches(branches);
            }
        }

        return mapping.findForward("basic");
    }

    public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        docForm.setRouteHeader(new DocumentRouteHeaderValue());
        docForm.setDocumentId(null);
        return mapping.findForward("basic");
    }

    public ActionMessages establishRequiredState(HttpServletRequest request, ActionForm form) throws Exception {
        return null;
    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        boolean change = false;

        String routeHeaderOp = docForm.getRouteHeaderOp();
        if (!KewApiConstants.UPDATE.equals(routeHeaderOp) && !KewApiConstants.NOOP.equals(routeHeaderOp)) {
            throw new WorkflowServiceErrorException("Document operation not defined",
                    new WorkflowServiceErrorImpl("Document operation not defined", "docoperation.operation.invalid"));
        }
        if (KewApiConstants.UPDATE.equals(routeHeaderOp)) {
            setRouteHeaderTimestamps(docForm);
            DocumentRouteHeaderValue dHeader = docForm.getRouteHeader();
            String initials = docForm.getInitialNodeInstances();
            List<RouteNodeInstance> lInitials = new ArrayList<RouteNodeInstance>();
            if (StringUtils.isNotEmpty(initials)) {
                StringTokenizer tokenInitials = new StringTokenizer(initials, ",");
                while (tokenInitials.hasMoreTokens()) {
                    String instanceId = tokenInitials.nextToken().trim();
                    LOG.debug(instanceId);
                    RouteNodeInstance instance = getRouteNodeService().findRouteNodeInstanceById(instanceId);
                    lInitials.add(instance);
                }
            }
            dHeader.setInitialRouteNodeInstances(lInitials);
            /*
             * FINP-7381 changes from KualiCo patch release 2021-02-26 applied to
             * original KEW-to-KFS KualiCo patch release 2021-01-28 version of the file.
             */
            getRouteHeaderService().validateRouteHeader(dHeader);
            getRouteHeaderService().saveRouteHeader(dHeader);
            change = true;
        }

        for (Iterator actionRequestIter = docForm.getActionRequestOps().iterator(); actionRequestIter.hasNext(); ) {
            DocOperationIndexedParameter actionRequestOp = (DocOperationIndexedParameter) actionRequestIter.next();
            int index = actionRequestOp.getIndex();
            String opValue = actionRequestOp.getValue();
            ActionRequest actionRequest = docForm.getActionRequests().get(index);
            String createDateParamName = "actionRequests[" + index + "].createDateString";

            if (!KewApiConstants.UPDATE.equals(opValue) && !KewApiConstants.DELETE.equals(opValue)
                    && !KewApiConstants.NOOP.equals(opValue)) {
                throw new WorkflowServiceErrorException("Action request operation not defined",
                        new WorkflowServiceErrorImpl("Action request operation not defined",
                                "docoperation.actionrequest.operation.invalid"));
            }
            if (KewApiConstants.UPDATE.equals(opValue)) {
                try {
                    actionRequest.setCreateDate(new Timestamp(
                            KFSConstants.getDefaultDateFormat().parse(request.getParameter(createDateParamName))
                                    .getTime()));
                    actionRequest.setCreateDateString(
                            KFSConstants.getDefaultDateFormat().format(actionRequest.getCreateDate()));
                    actionRequest.setDocumentId(docForm.getRouteHeader().getDocumentId());

                    if (StringUtils.isNotBlank(actionRequest.getParentActionRequestId())) {
                        actionRequest.setParentActionRequest(getActionRequestService()
                                .findByActionRequestId(actionRequest.getParentActionRequestId()));
                    }

                    if (StringUtils.isNotBlank(actionRequest.getActionTakenId())) {
                        actionRequest.setActionTaken(
                                getActionTakenService().findByActionTakenId(actionRequest.getActionTakenId()));
                    }

                    if (actionRequest.getNodeInstance() != null
                            && actionRequest.getNodeInstance().getRouteNodeInstanceId() == null) {
                        actionRequest.setNodeInstance(null);
                    } else if (actionRequest.getNodeInstance() != null
                            && actionRequest.getNodeInstance().getRouteNodeInstanceId() != null) {
                        actionRequest.setNodeInstance(KEWServiceLocator
                                .getRouteNodeService()
                                .findRouteNodeInstanceById(actionRequest.getNodeInstance().getRouteNodeInstanceId()));
                    }
                    getActionRequestService().saveActionRequest(actionRequest);
                    change = true;
                } catch (ParseException pe) {
                    throw new WorkflowServiceErrorException("Action request create date parsing error",
                            new WorkflowServiceErrorImpl("Action request create date parsing error",
                                    "docoperation.actionrequests.dateparsing.error",
                                    actionRequest.getActionRequestId()));
                }

            }
            if (KewApiConstants.DELETE.equals(opValue)) {
                getActionRequestService().deleteActionRequestGraph(actionRequest);
                change = true;
            }
        }

        for (Iterator actionTakenIter = docForm.getActionTakenOps().iterator(); actionTakenIter.hasNext(); ) {
            DocOperationIndexedParameter actionTakenOp = (DocOperationIndexedParameter) actionTakenIter.next();
            int index = actionTakenOp.getIndex();
            String opValue = actionTakenOp.getValue();

            String actionDateParamName = "actionsTaken[" + index + "].actionDateString";
            ActionTaken actionTaken = docForm.getActionsTaken().get(index);
            if (!KewApiConstants.UPDATE.equals(opValue) && !KewApiConstants.DELETE.equals(opValue)
                    && !KewApiConstants.NOOP.equals(opValue)) {
                throw new WorkflowServiceErrorException("Action taken operation not defined",
                        new WorkflowServiceErrorImpl("Action taken operation not defined",
                                "docoperation.actiontaken.operation.invalid"));
            }
            if (KewApiConstants.UPDATE.equals(opValue)) {
                try {
                    actionTaken.setActionDate(new Timestamp(
                            KFSConstants.getDefaultDateFormat().parse(request.getParameter(actionDateParamName))
                                    .getTime()));
                    actionTaken.setActionDateString(
                            KFSConstants.getDefaultDateFormat().format(actionTaken.getActionDate()));
                    getActionTakenService().saveActionTaken(actionTaken);
                    change = true;
                } catch (ParseException pe) {
                    throw new WorkflowServiceErrorException("Action taken action date parsing error",
                            new WorkflowServiceErrorImpl("Action taken action date parse error",
                                    "docoperation.actionstaken.dateparsing.error",
                                    actionTaken.getActionTakenId()));
                }
            }
            if (KewApiConstants.DELETE.equals(opValue)) {
                getActionTakenService().delete(actionTaken);
                change = true;
            }
        }

        for (Iterator actionItemIter = docForm.getActionItemOps().iterator(); actionItemIter.hasNext(); ) {
            DocOperationIndexedParameter actionItemOp = (DocOperationIndexedParameter) actionItemIter.next();
            int index = actionItemOp.getIndex();
            String opValue = actionItemOp.getValue();

            String dateAssignedParamName = "actionItems[" + index + "].dateAssignedStringValue";
            ActionItem actionItem = docForm.getActionItems().get(index);

            if (!KewApiConstants.UPDATE.equals(opValue) && !KewApiConstants.DELETE.equals(opValue)
                    && !KewApiConstants.NOOP.equals(opValue)) {
                throw new WorkflowServiceErrorException("Action Item operation not defined",
                        new WorkflowServiceErrorImpl("Action Item operation not defined",
                                "docoperation.operation.invalid"));
            }
            if (KewApiConstants.UPDATE.equals(opValue)) {
                try {
                    actionItem.setDateAssigned(new Timestamp(
                            KFSConstants.getDefaultDateFormat().parse(request.getParameter(dateAssignedParamName))
                                    .getTime()));
                    actionItem.setDateAssignedStringValue(
                            KFSConstants.getDefaultDateFormat().format(actionItem.getDateAssigned()));
                    actionItem.setDocumentId(docForm.getRouteHeader().getDocumentId());
                    getActionListService().saveActionItem(actionItem);
                    change = true;
                } catch (ParseException pe) {
                    throw new WorkflowServiceErrorException("Action item date assigned parsing error",
                            new WorkflowServiceErrorImpl("Action item date assigned parse error",
                                    "docoperation.actionitem.dateassignedparsing.error", actionItem.getId()));
                }
            }
            if (KewApiConstants.DELETE.equals(opValue)) {
                try {
                    actionItem.setDateAssigned(new Timestamp(
                            KFSConstants.getDefaultDateFormat().parse(request.getParameter(dateAssignedParamName))
                                    .getTime()));
                    actionItem.setDateAssignedStringValue(
                            KFSConstants.getDefaultDateFormat().format(actionItem.getDateAssigned()));
                    actionItem.setDocumentId(docForm.getRouteHeader().getDocumentId());
                    getActionListService().deleteActionItem(actionItem);
                    change = true;
                } catch (ParseException pe) {
                    throw new WorkflowServiceErrorException("Action item date assigned parsing error",
                            new WorkflowServiceErrorImpl("Action item date assigned parse error",
                                    "docoperation.actionitem.dateassignedparsing.error", actionItem.getId()));
                }
            }
        }

        List routeNodeInstances = (List) (request.getSession().getAttribute("routeNodeInstances"));
        String ids = (docForm.getNodeStatesDelete() != null) ? docForm.getNodeStatesDelete().trim() : null;
        List statesToBeDeleted = new ArrayList();
        if (ids != null && !"".equals(ids)) {
            StringTokenizer idSets = new StringTokenizer(ids);
            while (idSets.hasMoreTokens()) {
                String id = idSets.nextToken().trim();
                statesToBeDeleted.add(Long.valueOf(id));
            }
        }

        for (Iterator routeNodeInstanceIter = docForm.getRouteNodeInstanceOps().iterator(); routeNodeInstanceIter
                .hasNext(); ) {
            DocOperationIndexedParameter routeNodeInstanceOp =
                    (DocOperationIndexedParameter) routeNodeInstanceIter.next();
            int index = routeNodeInstanceOp.getIndex();
            String opValue = routeNodeInstanceOp.getValue();
            LOG.debug(opValue);
            RouteNodeInstance routeNodeInstance = (RouteNodeInstance) (routeNodeInstances.get(index));
            RouteNodeInstance routeNodeInstanceNew = docForm.getRouteNodeInstance(index);
            if (!KewApiConstants.UPDATE.equals(opValue) && !KewApiConstants.DELETE.equals(opValue)
                    && !KewApiConstants.NOOP.equals(opValue)) {
                throw new WorkflowServiceErrorException("Route Node Instance Operation not defined",
                        new WorkflowServiceErrorImpl("Route Node Instance Operation not defined",
                                "docoperation.routenodeinstance.operation.invalid"));
            }
            if (KewApiConstants.UPDATE.equals(opValue)) {
                routeNodeInstance.setActive(routeNodeInstanceNew.isActive());
                LOG.debug(Boolean.toString(routeNodeInstanceNew.isActive()));
                routeNodeInstance.setComplete(routeNodeInstanceNew.isComplete());
                routeNodeInstance.setInitial(routeNodeInstanceNew.isInitial());
                List<NodeState> nodeStates = routeNodeInstance.getState();
                List<NodeState> nodeStatesNew = routeNodeInstanceNew.getState();

                if (nodeStates != null) {
                    for (int i = 0; i < nodeStates.size(); i++) {
                        NodeState nodeState = nodeStates.get(i);
                        NodeState nodeStateNew = nodeStatesNew.get(i);
                        if (nodeStateNew.getKey() != null && !nodeStateNew.getKey().trim().equals("")) {
                            nodeState.setKey(nodeStateNew.getKey());
                            LOG.debug(nodeState.getKey());
                            nodeState.setValue(nodeStateNew.getValue());
                            LOG.debug(nodeState.getValue());
                        }
                    }
                }
                getRouteNodeService().save(routeNodeInstance);
                LOG.debug("saved");
                change = true;
            }

            if (KewApiConstants.DELETE.equals(opValue)) {
                List<NodeState> nodeStates = routeNodeInstance.getState();
                List<NodeState> nodeStatesNew = routeNodeInstanceNew.getState();

                if (nodeStates != null) {
                    for (int i = 0; i < nodeStates.size(); i++) {
                        NodeState nodeState = nodeStates.get(i);
                        NodeState nodeStateNew = nodeStatesNew.get(i);
                        if (nodeStateNew.getKey() == null || nodeStateNew.getKey().trim().equals("")) {
                            statesToBeDeleted.remove(nodeState.getNodeStateId());
                        }
                    }
                }
                getRouteNodeService().deleteByRouteNodeInstance(routeNodeInstance);
                LOG.debug(routeNodeInstance.getRouteNodeInstanceId() + " is deleted");
                change = true;
                break;
            }

            if (KewApiConstants.NOOP.equals(opValue)) {
                routeNodeInstanceNew.setActive(routeNodeInstance.isActive());
                routeNodeInstanceNew.setComplete(routeNodeInstance.isComplete());
                routeNodeInstanceNew.setInitial(routeNodeInstance.isInitial());
                List<NodeState> nodeStates = routeNodeInstance.getState();
                List<NodeState> nodeStatesNew = routeNodeInstanceNew.getState();
                if (nodeStates != null) {
                    for (int i = 0; i < nodeStates.size(); i++) {
                        NodeState nodeState = nodeStates.get(i);
                        NodeState nodeStateNew = nodeStatesNew.get(i);
                        if (nodeStateNew.getKey() == null || nodeStateNew.getKey().trim().equals("")) {
                            statesToBeDeleted.remove(nodeState.getNodeStateId());
                        }
                        nodeStateNew.setKey(nodeState.getKey());
                        nodeStateNew.setValue(nodeState.getValue());
                    }
                }
            }
        }

        if (statesToBeDeleted != null && statesToBeDeleted.size() > 0) {
            getRouteNodeService().deleteNodeStates(statesToBeDeleted);
        }

        List branches = (List) request.getSession().getAttribute("branches");
        String branchStateIds =
                (docForm.getBranchStatesDelete() != null) ? docForm.getBranchStatesDelete().trim() : null;
        List<Long> branchStatesToBeDeleted = new ArrayList<>();
        if (branchStateIds != null && !"".equals(branchStateIds)) {
            StringTokenizer idSets = new StringTokenizer(branchStateIds);
            while (idSets.hasMoreTokens()) {
                String id = idSets.nextToken().trim();
                branchStatesToBeDeleted.add(Long.valueOf(id));
            }
        }

        for (Iterator branchesOpIter = docForm.getBranchOps().iterator(); branchesOpIter.hasNext(); ) {
            DocOperationIndexedParameter branchesOp = (DocOperationIndexedParameter) branchesOpIter.next();
            int index = branchesOp.getIndex();
            String opValue = branchesOp.getValue();
            LOG.debug(opValue);
            Branch branch = (Branch) (branches.get(index));
            Branch branchNew = docForm.getBranche(index);
            if (!KewApiConstants.UPDATE.equals(opValue) && !KewApiConstants.NOOP.equals(opValue)) {
                throw new WorkflowServiceErrorException("Route Node Instance Operation not defined",
                        new WorkflowServiceErrorImpl("Route Node Instance Operation not defined",
                                "docoperation.routenodeinstance.operation.invalid"));
            }
            if (KewApiConstants.UPDATE.equals(opValue)) {
                branch.setName(branchNew.getName());
                List<BranchState> branchStates = branch.getBranchState();
                List<BranchState> branchStatesNew = branchNew.getBranchState();
                if (branchStates != null) {
                    for (int i = 0; i < branchStates.size(); i++) {
                        BranchState branchState = branchStates.get(i);
                        if (i < branchStatesNew.size()) {
                            BranchState branchStateNew = branchStatesNew.get(i);
                            if (branchStateNew.getKey() != null && !branchStateNew.getKey().trim().equals("")) {
                                branchState.setKey(branchStateNew.getKey());
                                LOG.debug(branchState.getKey());
                                branchState.setValue(branchStateNew.getValue());
                                LOG.debug(branchState.getValue());
                            }
                        }
                    }
                }
                getBranchService().save(branch);
                LOG.debug("branch saved");
                change = true;
            }

            if (KewApiConstants.NOOP.equals(opValue)) {
                branchNew.setName(branch.getName());
                List<BranchState> branchStates = branch.getBranchState();
                List<BranchState> branchStatesNew = branchNew.getBranchState();
                if (branchStates != null) {
                    for (int i = 0; i < branchStates.size(); i++) {
                        BranchState branchState = branchStates.get(i);
                        BranchState branchStateNew = branchStatesNew.get(i);
                        if (branchStateNew.getKey() == null || branchStateNew.getKey().trim().equals("")) {
                            branchStatesToBeDeleted.remove(branchState.getBranchStateId());
                        }
                        branchStateNew.setKey(branchState.getKey());
                        LOG.debug(branchState.getKey());
                        branchStateNew.setValue(branchState.getValue());
                        LOG.debug(branchState.getValue());
                    }
                }
            }
        }

        if (branchStatesToBeDeleted != null && branchStatesToBeDeleted.size() > 0) {
            List<BranchState> branchStatesToDelete = new ArrayList<>();
            List<String> branchStateIdsToBeDeleted = new ArrayList<>(branchStatesToBeDeleted.size());
            //Converting a list of Long values to list of String values
            for (Long branchStateToBeDeleted : branchStatesToBeDeleted) {
                branchStateIdsToBeDeleted.add(String.valueOf(branchStateToBeDeleted));
            }

            for (String branchStateId : branchStateIdsToBeDeleted) {
                BranchState branchState = getBusinessObjectService().findBySinglePrimaryKey(BranchState.class,
                        branchStateId);
                branchStatesToDelete.add(branchState);
            }

            getBranchService().deleteBranchStates(branchStatesToDelete);
        }

        WorkflowDocument workflowDocument = WorkflowDocumentFactory
                .loadDocument(GlobalVariables.getUserSession().getPrincipalId(), docForm.getDocumentId());

        String annotation = docForm.getAnnotation();
        if (StringUtils.isEmpty(annotation)) {
            annotation = DEFAULT_LOG_MSG;
        }
        workflowDocument.logAnnotation(annotation);

        ActionMessages messages = new ActionMessages();
        String forward = null;
        if (change) {
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("docoperation.operation.saved"));
            docForm.setRouteHeader(getRouteHeaderService().getRouteHeader(docForm.getRouteHeader().getDocumentId()));
            forward = "summary";
        } else {
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("docoperation.operation.noop"));
            forward = "basic";
        }
        saveMessages(request, messages);
        return mapping.findForward(forward);
    }

    private RouteHeaderService getRouteHeaderService() {
        return (RouteHeaderService) KEWServiceLocator.getService(KEWServiceLocator.DOC_ROUTE_HEADER_SRV);
    }

    private RouteNodeService getRouteNodeService() {
        return (RouteNodeService) KEWServiceLocator.getService(KEWServiceLocator.ROUTE_NODE_SERVICE);
    }

    private ActionRequestService getActionRequestService() {
        return (ActionRequestService) KEWServiceLocator.getService(KEWServiceLocator.ACTION_REQUEST_SRV);
    }

    private ActionTakenService getActionTakenService() {
        return (ActionTakenService) KEWServiceLocator.getService(KEWServiceLocator.ACTION_TAKEN_SRV);
    }

    private ActionListService getActionListService() {
        return KEWServiceLocator.getActionListService();
    }

    private void setRouteHeaderTimestamps(DocumentOperationForm docForm) {
        if (docForm.getCreateDate() == null || docForm.getCreateDate().trim().equals("")) {
            throw new WorkflowServiceErrorException("Document create date empty",
                    new WorkflowServiceErrorImpl("Document create date empty",
                            "docoperation.routeheader.createdate.empty"));
        } else {
            try {
                docForm.getRouteHeader().setCreateDate(new Timestamp(
                        KFSConstants.getDefaultDateAndTimeFormat().parse(docForm.getCreateDate()).getTime()));
            } catch (ParseException pe) {
                throw new WorkflowServiceErrorException("RouteHeader create date parsing error",
                        new WorkflowServiceErrorImpl("Date parsing error",
                                "docoperation.routeheader.createdate.invalid"));
            }
        }

        if (docForm.getDateModified() == null || docForm.getDateModified().trim().equals("")) {
            throw new WorkflowServiceErrorException("Document doc status mod date empty",
                    new WorkflowServiceErrorImpl("Document doc status mod date empty",
                            "docoperation.routeheader.statusmoddate.empty"));
        } else {
            try {
                docForm.getRouteHeader().setDateModified(new Timestamp(
                        KFSConstants.getDefaultDateAndTimeFormat().parse(docForm.getDateModified()).getTime()));
            } catch (ParseException pe) {
                throw new WorkflowServiceErrorException("Document doc status date parsing error",
                        new WorkflowServiceErrorImpl("Document doc status mod date parsing error",
                                "docoperation.routeheader.statusmoddate.invalid"));
            }
        }

        if (docForm.getApprovedDate() != null && !docForm.getApprovedDate().trim().equals("")) {
            try {
                docForm.getRouteHeader().setApprovedDate(new Timestamp(
                        KFSConstants.getDefaultDateAndTimeFormat().parse(docForm.getApprovedDate()).getTime()));
            } catch (ParseException pe) {
                throw new WorkflowServiceErrorException("Document approved date parsing error",
                        new WorkflowServiceErrorImpl("Document approved date parsing error",
                                "docoperation.routeheader.approveddate.invalid"));
            }
        }

        if (docForm.getFinalizedDate() != null && !docForm.getFinalizedDate().trim().equals("")) {
            try {
                docForm.getRouteHeader().setFinalizedDate(new Timestamp(
                        KFSConstants.getDefaultDateAndTimeFormat().parse(docForm.getFinalizedDate()).getTime()));
            } catch (ParseException pe) {
                throw new WorkflowServiceErrorException("Document finalized date parsing error",
                        new WorkflowServiceErrorImpl("Document finalized date parsing error",
                                "docoperation.routeheader.finalizeddate.invalid"));
            }
        }

        if (docForm.getRouteStatusDate() != null && !docForm.getRouteStatusDate().trim().equals("")) {
            try {
                docForm.getRouteHeader().setRouteStatusDate(new Timestamp(
                        KFSConstants.getDefaultDateAndTimeFormat().parse(docForm.getRouteStatusDate()).getTime()));
            } catch (ParseException pe) {
                throw new WorkflowServiceErrorException("Document route status date parsing error",
                        new WorkflowServiceErrorImpl("Document route status date parsing error",
                                "docoperation.routeheader.routestatusdate.invalid"));
            }
        }
    }

    private void setRouteHeaderTimestampsToString(DocumentOperationForm docForm) {
        try {
            docForm.setCreateDate(KFSConstants.getDefaultDateAndTimeFormat().format(
                    docForm.getRouteHeader().getCreateDate()));
            docForm.setDateModified(KFSConstants.getDefaultDateAndTimeFormat().format(
                    docForm.getRouteHeader().getDateModified()));
            docForm.setApprovedDate(KFSConstants.getDefaultDateAndTimeFormat().format(
                    docForm.getRouteHeader().getApprovedDate()));
            docForm.setFinalizedDate(KFSConstants.getDefaultDateAndTimeFormat().format(
                    docForm.getRouteHeader().getFinalizedDate()));
            docForm.setRouteStatusDate(KFSConstants.getDefaultDateAndTimeFormat().format(
                    docForm.getRouteHeader().getRouteStatusDate()));

        } catch (Exception e) {
            LOG.info("One or more of the dates in routeHeader may be null");
        }
    }

    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        docForm.getRouteHeader().setDocumentId(docForm.getDocumentId());
        return mapping.findForward("basic");
    }

    public ActionForward queueDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        try {
            DocumentOperationForm docForm = (DocumentOperationForm) form;
            DocumentRouteHeaderValue document = docForm.getRouteHeader();
            DocumentProcessingQueue documentProcessingQueue =
                    KewApiServiceLocator.getDocumentProcessingQueue(document.getDocumentId());
            documentProcessingQueue.process(docForm.getDocumentId());
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("general.message", "Document was successfully queued"));
            saveMessages(request, messages);
            return mapping.findForward("basic");
        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    public ActionForward indexSearchableAttributes(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        queue.indexDocument(docForm.getRouteHeader().getDocumentId());
        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("general.message", "Searchable Attribute Indexing was successfully scheduled"));
        saveMessages(request, messages);
        return mapping.findForward("basic");
    }

    public ActionForward queueDocumentRefresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        DocumentOperationForm docForm = (DocumentOperationForm) form;
        DocumentRefreshQueue docRequeue = KewApiServiceLocator
                .getDocumentRequeuerService(docForm.getRouteHeader().getDocumentId(), 0L);
        docRequeue.refreshDocument(docForm.getRouteHeader().getDocumentId());
        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE,
                new ActionMessage("general.message", "Document Requeuer was successfully scheduled"));
        saveMessages(request, messages);
        return mapping.findForward("basic");
    }

    public ActionForward blanketApproveDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        try {
            DocumentOperationForm docForm = (DocumentOperationForm) form;
            String blanketApproverUser = docForm.getBlanketApproveUser();
            if (StringUtils.isBlank(blanketApproverUser)) {
                throw new WorkflowServiceErrorException("No user was provided in the Blanket Approve User field",
                        new WorkflowServiceErrorImpl("No user was provided in the Blanket Approve User field",
                                "docoperation.operation.invalid"));
            }
            String principalId =
                    KimApiServiceLocator.getPersonService().getPersonByPrincipalName(docForm.getBlanketApproveUser())
                            .getPrincipalId();
            Set<String> nodeNames = new HashSet<>();
            if (StringUtils.isNotBlank(docForm.getBlanketApproveNodes())) {
                String[] nodeNameArray = docForm.getBlanketApproveNodes().split(",");
                for (String nodeName : nodeNameArray) {
                    nodeNames.add(nodeName.trim());
                }
            }
            DocumentRouteHeaderValue document = docForm.getRouteHeader();
            DocumentOrchestrationQueue blanketApprove = KewApiServiceLocator.getDocumentOrchestrationQueue(
                    document.getDocumentId());
            DocumentOrchestrationConfig documentOrchestrationConfig =
                    DocumentOrchestrationConfig.create(docForm.getBlanketApproveActionTakenId(), nodeNames);
            DocumentProcessingOptions options = DocumentProcessingOptions.createDefault();
            blanketApprove.orchestrateDocument(docForm.getRouteHeader().getDocumentId(), principalId,
                    documentOrchestrationConfig, options);
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("general.message", "Blanket Approve Processor was successfully scheduled"));
            saveMessages(request, messages);
            return mapping.findForward("basic");
        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    public ActionForward moveDocument(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        try {
            DocumentOperationForm docForm = (DocumentOperationForm) form;
            String principalId = KEWServiceLocator.getIdentityHelperService()
                    .getIdForPrincipalName(docForm.getBlanketApproveUser());
            Set<String> nodeNames = new HashSet<>();
            if (StringUtils.isNotBlank(docForm.getBlanketApproveNodes())) {
                String[] nodeNameArray = docForm.getBlanketApproveNodes().split(",");
                for (String nodeName : nodeNameArray) {
                    nodeNames.add(nodeName.trim());
                }
            }
            DocumentRouteHeaderValue document = docForm.getRouteHeader();
            DocumentOrchestrationQueue orchestrationQueue = KewApiServiceLocator.getDocumentOrchestrationQueue(
                    document.getDocumentId());
            DocumentOrchestrationConfig documentOrchestrationConfig =
                    DocumentOrchestrationConfig.create(docForm.getBlanketApproveActionTakenId(), nodeNames);
            DocumentProcessingOptions options = DocumentProcessingOptions.create(true, true, false);
            orchestrationQueue
                    .orchestrateDocument(docForm.getDocumentId(), principalId, documentOrchestrationConfig, options);

            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("general.message", "Move Document Processor was successfully scheduled"));
            saveMessages(request, messages);
            return mapping.findForward("basic");
        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    public ActionForward queueActionInvocation(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        try {
            DocumentOperationForm docForm = (DocumentOperationForm) form;
            String principalId = KEWServiceLocator.getIdentityHelperService()
                    .getIdForPrincipalName(docForm.getActionInvocationUser());
            ActionInvocation invocation = ActionInvocation.create(ActionType.fromCode(
                    docForm.getActionInvocationActionCode()), docForm.getActionInvocationActionItemId());
            DocumentRouteHeaderValue document = docForm.getRouteHeader();
            ActionInvocationQueue actionInvocationQueue = KewApiServiceLocator.getActionInvocationProcessorService(
                    document.getDocumentId());
            actionInvocationQueue.invokeAction(principalId, docForm.getRouteHeader().getDocumentId(), invocation);
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE,
                    new ActionMessage("general.message", "Action Invocation Processor was successfully scheduled"));
            saveMessages(request, messages);
            return mapping.findForward("basic");
        } catch (Exception e) {
            throw new WorkflowRuntimeException(e);
        }
    }

    private DocumentTypeService getDocumentTypeService() {
        return (DocumentTypeService) KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE);
    }

    private BranchService getBranchService() {
        return (BranchService) KEWServiceLocator.getService(KEWServiceLocator.BRANCH_SERVICE);
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }
}
