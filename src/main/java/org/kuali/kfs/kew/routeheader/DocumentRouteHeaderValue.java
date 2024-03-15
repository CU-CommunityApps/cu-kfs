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
package org.kuali.kfs.kew.routeheader;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.kfs.kew.actionitem.ActionItem;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.WorkflowAction;
import org.kuali.kfs.kew.api.document.DocumentContract;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.api.document.DocumentUpdate;
import org.kuali.kfs.kew.api.exception.InvalidActionTakenException;
import org.kuali.kfs.kew.api.util.CodeTranslator;
import org.kuali.kfs.kew.doctype.ApplicationDocumentStatus;
import org.kuali.kfs.kew.doctype.Policy;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.engine.node.Branch;
import org.kuali.kfs.kew.engine.node.BranchState;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.mail.CustomEmailAttribute;
import org.kuali.kfs.kew.mail.CustomEmailAttributeImpl;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A document within KEW.  A document effectively represents a process that moves through the workflow engine. It is
 * created from a particular {@link DocumentType} and follows the route path defined by that DocumentType.
 *
 * <p>During a document's lifecycle it progresses through a series of statuses, starting with INITIATED and moving to
 * one of the terminal states (such as FINAL, CANCELED, etc.). The list of status on a document are defined in the
 * {@link KewApiConstants} class and include the constants starting with "ROUTE_HEADER_" and ending with "_CD".
 *
 * <p>Associated with the document is the document content.  The document content is XML which represents the content
 * of that document.  This XML content is typically used to make routing decisions for the document.
 *
 * <p>A document has associated with it a set of {@link ActionRequest} object and {@link ActionTaken}
 * objects.  Action Requests represent requests for user action (such as Approve, Acknowledge, etc).  Action Takens
 * represent action that users have performed on the document, such as approvals or cancelling of the document.
 *
 * <p>The instantiated route path of a document is defined by its graph of {@link RouteNodeInstance} objects. The
 * path starts at the initial node of the document and progresses from there following the next nodes of each node
 * instance. The current active nodes on the document are defined by the "active" flag on the node instance where are
 * not marked as "complete".
 *
 * @see DocumentType
 * @see ActionRequest
 * @see ActionItem
 * @see ActionTaken
 * @see RouteNodeInstance
 * @see KewApiConstants
 */
public class DocumentRouteHeaderValue extends PersistableBusinessObjectBase implements DocumentContract {

    private static final long serialVersionUID = -4700736340527913220L;
    private static final Logger LOG = LogManager.getLogger();

    private static final String CURRENT_ROUTE_NODE_NAME_DELIMITER = ", ";

    private String documentTypeId;
    private String docRouteStatus;
    private Integer docRouteLevel;
    private Timestamp dateModified;
    private Timestamp createDate;
    private Timestamp approvedDate;
    private Timestamp finalizedDate;
    private DocumentRouteHeaderValueContent documentContent;
    private String docTitle;
    private String appDocId;
    private Integer docVersion = KewApiConstants.DocumentContentVersions.NODAL;
    private String initiatorWorkflowId;
    private String routedByUserWorkflowId;
    private Timestamp routeStatusDate;
    private String appDocStatus;
    private Timestamp appDocStatusDate;

    private String documentId;

    /**
     * The appDocStatusHistory keeps a list of Application Document Status transitions
     * for the document.  It tracks the previous status, the new status, and a timestamp of the
     * transition for each status transition.
     */
    private List<DocumentStatusTransition> appDocStatusHistory = new ArrayList<>();

    private List<ActionRequest> simulatedActionRequests;

    private static final boolean FINAL_STATE = true;
    private static final Map<String, String> legalActions;
    private static final Map<String, String> stateTransitionMap;

    private List<RouteNodeInstance> initialRouteNodeInstances = new ArrayList<>();

    // an empty list of target document statuses or legal actions
    private static final String TERMINAL = "";

    static {
        stateTransitionMap = new HashMap<>();
        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_INITIATED_CD,
                KewApiConstants.ROUTE_HEADER_SAVED_CD + KewApiConstants.ROUTE_HEADER_ENROUTE_CD +
                        KewApiConstants.ROUTE_HEADER_CANCEL_CD);

        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_SAVED_CD,
                KewApiConstants.ROUTE_HEADER_SAVED_CD + KewApiConstants.ROUTE_HEADER_ENROUTE_CD +
                        KewApiConstants.ROUTE_HEADER_CANCEL_CD + KewApiConstants.ROUTE_HEADER_PROCESSED_CD);

        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD +
                KewApiConstants.ROUTE_HEADER_CANCEL_CD + KewApiConstants.ROUTE_HEADER_PROCESSED_CD +
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD + KewApiConstants.ROUTE_HEADER_SAVED_CD +
                DocumentStatus.RECALLED.getCode());
        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD, TERMINAL);
        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_CANCEL_CD, TERMINAL);
        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_FINAL_CD, TERMINAL);
        stateTransitionMap.put(DocumentStatus.RECALLED.getCode(), TERMINAL);
        stateTransitionMap.put(KewApiConstants.ROUTE_HEADER_EXCEPTION_CD,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD + KewApiConstants.ROUTE_HEADER_ENROUTE_CD +
                        KewApiConstants.ROUTE_HEADER_CANCEL_CD + KewApiConstants.ROUTE_HEADER_PROCESSED_CD +
                        KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD + KewApiConstants.ROUTE_HEADER_SAVED_CD);
        stateTransitionMap.put(
                KewApiConstants.ROUTE_HEADER_PROCESSED_CD,
                KewApiConstants.ROUTE_HEADER_FINAL_CD + KewApiConstants.ROUTE_HEADER_PROCESSED_CD);

        legalActions = new HashMap<>();
        legalActions.put(KewApiConstants.ROUTE_HEADER_INITIATED_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD +
                        KewApiConstants.ACTION_TAKEN_SAVED_CD + KewApiConstants.ACTION_TAKEN_COMPLETED_CD +
                        KewApiConstants.ACTION_TAKEN_ROUTED_CD + KewApiConstants.ACTION_TAKEN_CANCELED_CD +
                        KewApiConstants.ACTION_TAKEN_ADHOC_CD + KewApiConstants.ACTION_TAKEN_BLANKET_APPROVE_CD);
        legalActions.put(KewApiConstants.ROUTE_HEADER_SAVED_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD +
                        KewApiConstants.ACTION_TAKEN_SAVED_CD + KewApiConstants.ACTION_TAKEN_COMPLETED_CD +
                        KewApiConstants.ACTION_TAKEN_ROUTED_CD + KewApiConstants.ACTION_TAKEN_APPROVED_CD +
                        KewApiConstants.ACTION_TAKEN_CANCELED_CD + KewApiConstants.ACTION_TAKEN_ADHOC_CD +
                        KewApiConstants.ACTION_TAKEN_BLANKET_APPROVE_CD);
        /* ACTION_TAKEN_ROUTED_CD not included in enroute state
         * ACTION_TAKEN_SAVED_CD removed as of version 2.4
         */
        legalActions.put(KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                        KewApiConstants.ACTION_TAKEN_APPROVED_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD +
                                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ADHOC_CD +
                                KewApiConstants.ACTION_TAKEN_BLANKET_APPROVE_CD +
                                KewApiConstants.ACTION_TAKEN_CANCELED_CD + KewApiConstants.ACTION_TAKEN_COMPLETED_CD +
                                KewApiConstants.ACTION_TAKEN_DENIED_CD + KewApiConstants.ACTION_TAKEN_SU_APPROVED_CD +
                                KewApiConstants.ACTION_TAKEN_SU_CANCELED_CD +
                                KewApiConstants.ACTION_TAKEN_SU_DISAPPROVED_CD +
                                KewApiConstants.ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED_CD +
                                KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD +
                                KewApiConstants.ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD +
                                WorkflowAction.RECALL.getCode());
        /* ACTION_TAKEN_ROUTED_CD not included in exception state
         * ACTION_TAKEN_SAVED_CD removed as of version 2.4.2
         */
        legalActions.put(KewApiConstants.ROUTE_HEADER_EXCEPTION_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD +
                        KewApiConstants.ACTION_TAKEN_ADHOC_CD +
                        KewApiConstants.ACTION_TAKEN_APPROVED_CD + KewApiConstants.ACTION_TAKEN_BLANKET_APPROVE_CD +
                        KewApiConstants.ACTION_TAKEN_CANCELED_CD + KewApiConstants.ACTION_TAKEN_COMPLETED_CD +
                        KewApiConstants.ACTION_TAKEN_DENIED_CD + KewApiConstants.ACTION_TAKEN_SU_APPROVED_CD +
                        KewApiConstants.ACTION_TAKEN_SU_CANCELED_CD + KewApiConstants.ACTION_TAKEN_SU_DISAPPROVED_CD +
                        KewApiConstants.ACTION_TAKEN_SU_ROUTE_LEVEL_APPROVED_CD +
                        KewApiConstants.ACTION_TAKEN_RETURNED_TO_PREVIOUS_CD +
                        KewApiConstants.ACTION_TAKEN_SU_RETURNED_TO_PREVIOUS_CD);
        legalActions.put(KewApiConstants.ROUTE_HEADER_FINAL_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD);
        legalActions.put(KewApiConstants.ROUTE_HEADER_CANCEL_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD);
        legalActions.put(KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD);
        legalActions.put(KewApiConstants.ROUTE_HEADER_PROCESSED_CD,
                KewApiConstants.ACTION_TAKEN_FYI_CD + KewApiConstants.ACTION_TAKEN_ACKNOWLEDGED_CD);
        legalActions.put(DocumentStatus.RECALLED.getCode(), TERMINAL);
    }

    public DocumentRouteHeaderValue() {
    }

    public Person getInitiatorPerson() {
        // if we are running a simulation, there will be no initiator
        if (initiatorWorkflowId == null) {
            return null;
        }
        return getPerson(initiatorWorkflowId);
    }

    private static Person getPerson(final String principalId) {
        final Person person = KimApiServiceLocator.getPersonService().getPerson(principalId);
        if (person == null) {
            throw new IllegalArgumentException(
                    "Could not locate a person with the given principal id of " + principalId);
        }
        return person;
    }

    public String getInitiatorDisplayName() {
        // ==== CU Customization: Return potentially masked Person name instead. ====
        return getPerson(initiatorWorkflowId).getNameMaskedIfNecessary();
    }

    /**
     * Used by ActionList.jsp, routeLog.tag and routeLogPageBody.tag
     */
    public String getCurrentRouteLevelName() {
        final List<String> currentNodeNames = getCurrentNodeNames();
        return StringUtils.join(currentNodeNames, CURRENT_ROUTE_NODE_NAME_DELIMITER);
    }

    public List<String> getCurrentNodeNames() {
        return KEWServiceLocator.getRouteNodeService().getCurrentRouteNodeNames(documentId);
    }

    public String getRouteStatusLabel() {
        return CodeTranslator.getRouteStatusLabel(docRouteStatus);
    }

    public String getDocRouteStatusLabel() {
        return CodeTranslator.getRouteStatusLabel(docRouteStatus);
    }

    /**
     * Used by routeLog.tag and routeLogPageBody.tag
     *
     * @return the Document Status Policy for the document type associated with this Route Header. The Document Status
     *         Policy denotes whether the KEW Route Status, or the Application Document Status, or both are to be
     *         displayed.
     */
    public String getDocStatusPolicy() {
        return getDocumentType().getDocumentStatusPolicy().getPolicyStringValue();
    }

    public List<ActionTaken> getActionsTaken() {
        return (List<ActionTaken>) KEWServiceLocator.getActionTakenService()
                .findByDocumentIdIgnoreCurrentInd(documentId);
    }

    public List<ActionRequest> getActionRequests() {
        if (simulatedActionRequests == null || simulatedActionRequests.isEmpty()) {
            return KEWServiceLocator.getActionRequestService().findByDocumentIdIgnoreCurrentInd(documentId);
        } else {
            return simulatedActionRequests;
        }
    }

    public List<ActionRequest> getSimulatedActionRequests() {
        if (simulatedActionRequests == null) {
            simulatedActionRequests = new ArrayList<>();
        }
        return simulatedActionRequests;
    }

    public void setSimulatedActionRequests(final List<ActionRequest> simulatedActionRequests) {
        this.simulatedActionRequests = simulatedActionRequests;
    }

    public DocumentType getDocumentType() {
        return KEWServiceLocator.getDocumentTypeService().findById(documentTypeId);
    }

    public String getAppDocId() {
        return appDocId;
    }

    public void setAppDocId(final String appDocId) {
        this.appDocId = appDocId;
    }

    public Timestamp getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(final Timestamp approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(final Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getDocContent() {
        return getDocumentContent().getDocumentContent();
    }

    public void setDocContent(final String docContent) {
        final DocumentRouteHeaderValueContent content = getDocumentContent();
        content.setDocumentContent(docContent);
    }

    public Integer getDocRouteLevel() {
        return docRouteLevel;
    }

    public void setDocRouteLevel(final Integer docRouteLevel) {
        this.docRouteLevel = docRouteLevel;
    }

    public String getDocRouteStatus() {
        return docRouteStatus;
    }

    public void setDocRouteStatus(final String docRouteStatus) {
        this.docRouteStatus = docRouteStatus;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(final String docTitle) {
        this.docTitle = docTitle;
    }

    @Override
    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(final String documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public Integer getDocVersion() {
        return docVersion;
    }

    public void setDocVersion(final Integer docVersion) {
        this.docVersion = docVersion;
    }

    public Timestamp getFinalizedDate() {
        return finalizedDate;
    }

    public void setFinalizedDate(final Timestamp finalizedDate) {
        this.finalizedDate = finalizedDate;
    }

    public String getInitiatorWorkflowId() {
        return initiatorWorkflowId;
    }

    public void setInitiatorWorkflowId(final String initiatorWorkflowId) {
        this.initiatorWorkflowId = initiatorWorkflowId;
    }

    public String getRoutedByUserWorkflowId() {
        if (isEnroute() && StringUtils.isBlank(routedByUserWorkflowId)) {
            return initiatorWorkflowId;
        }
        return routedByUserWorkflowId;
    }

    public void setRoutedByUserWorkflowId(final String routedByUserWorkflowId) {
        this.routedByUserWorkflowId = routedByUserWorkflowId;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public Timestamp getRouteStatusDate() {
        return routeStatusDate;
    }

    public void setRouteStatusDate(final Timestamp routeStatusDate) {
        this.routeStatusDate = routeStatusDate;
    }

    public Timestamp getDateModified() {
        return dateModified;
    }

    public void setDateModified(final Timestamp dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * @return the Application Document Status. This status is an alternative to the Route Status that may be used for
     *         a document. It is configurable per document type.
     * @see ApplicationDocumentStatus
     * @see Policy
     */
    public String getAppDocStatus() {
        if (StringUtils.isEmpty(appDocStatus)) {
            return KewApiConstants.UNKNOWN_STATUS;
        }
        return appDocStatus;
    }

    public void setAppDocStatus(final String appDocStatus) {
        this.appDocStatus = appDocStatus;
    }

    /**
     * Used by ActionList.jsp
     *
     * @return a combination of the route status label and the app doc status.
     */
    public String getCombinedStatus() {
        String routeStatus = getRouteStatusLabel();
        final String appStatus = getAppDocStatus();
        if (routeStatus != null && !routeStatus.isEmpty()) {
            if (!appStatus.isEmpty()) {
                routeStatus += ", " + appStatus;
            }
        } else {
            return appStatus;
        }
        return routeStatus;
    }

    /**
     * This method sets the appDocStatus.
     * It firsts validates the new value against the defined acceptable values, if defined.
     * It also updates the AppDocStatus date, and saves the status transition information
     *
     * @param appDocStatus new appDocStatus
     * @throws WorkflowRuntimeException
     */
    public void updateAppDocStatus(final String appDocStatus) throws WorkflowRuntimeException {
        //validate against allowable values if defined
        if (appDocStatus != null && !appDocStatus.isEmpty() && !appDocStatus.equalsIgnoreCase(this.appDocStatus)) {
            final DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findById(documentTypeId);
            if (documentType.getValidApplicationStatuses() != null
                && !documentType.getValidApplicationStatuses().isEmpty()) {
                final Iterator<ApplicationDocumentStatus> iter = documentType.getValidApplicationStatuses().iterator();
                boolean statusValidated = false;
                while (iter.hasNext()) {
                    final ApplicationDocumentStatus myAppDocStat = iter.next();
                    if (appDocStatus.compareToIgnoreCase(myAppDocStat.getStatusName()) == 0) {
                        statusValidated = true;
                        break;
                    }
                }
                if (!statusValidated) {
                    final WorkflowRuntimeException xpee =
                            new WorkflowRuntimeException("AppDocStatus value " + appDocStatus + " not allowable.");
                    LOG.error(
                            "Error validating nextAppDocStatus name: {} against acceptable values.",
                            appDocStatus,
                            xpee
                    );
                    throw xpee;
                }
            }

            // set the status value
            final String oldStatus = this.appDocStatus;
            this.appDocStatus = appDocStatus;

            // update the timestamp
            appDocStatusDate = new Timestamp(System.currentTimeMillis());

            // save the status transition
            appDocStatusHistory.add(new DocumentStatusTransition(documentId, oldStatus, appDocStatus));
        }
    }

    public Timestamp getAppDocStatusDate() {
        return appDocStatusDate;
    }

    public void setAppDocStatusDate(final Timestamp appDocStatusDate) {
        this.appDocStatusDate = appDocStatusDate;
    }

    /**
     * @return True if the document is in the state of Initiated
     */
    public boolean isStateInitiated() {
        return KewApiConstants.ROUTE_HEADER_INITIATED_CD.equals(docRouteStatus);
    }

    /**
     * @return True if the document is in the state of Saved
     */
    public boolean isStateSaved() {
        return KewApiConstants.ROUTE_HEADER_SAVED_CD.equals(docRouteStatus);
    }

    /**
     * @return true if the document has ever been inte enroute state
     */
    public boolean isRouted() {
        return !(isStateInitiated() || isStateSaved());
    }

    public boolean isInException() {
        return KewApiConstants.ROUTE_HEADER_EXCEPTION_CD.equals(docRouteStatus);
    }

    public boolean isDisapproved() {
        return KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD.equals(docRouteStatus);
    }

    public boolean isCanceled() {
        return KewApiConstants.ROUTE_HEADER_CANCEL_CD.equals(docRouteStatus);
    }

    public boolean isFinal() {
        return KewApiConstants.ROUTE_HEADER_FINAL_CD.equals(docRouteStatus);
    }

    public boolean isEnroute() {
        return KewApiConstants.ROUTE_HEADER_ENROUTE_CD.equals(docRouteStatus);
    }

    /**
     * @return true if the document is in the processed state
     */
    public boolean isProcessed() {
        return KewApiConstants.ROUTE_HEADER_PROCESSED_CD.equals(docRouteStatus);
    }

    public boolean isRoutable() {
        return KewApiConstants.ROUTE_HEADER_ENROUTE_CD.equals(docRouteStatus)
                || KewApiConstants.ROUTE_HEADER_SAVED_CD.equals(docRouteStatus)
                || KewApiConstants.ROUTE_HEADER_PROCESSED_CD.equals(docRouteStatus);
    }

    /**
     * Return true if the given action code is valid for this document's current state. This method only verifies
     * statically defined action/state transitions, it does not perform full action validation logic.
     *
     * @param actionCd The action code to be tested.
     * @return True if the action code is valid for the document's status.
     * @see org.kuali.kfs.kew.actions.ActionRegistry#getValidActions(Person, DocumentRouteHeaderValue)
     */
    public boolean isValidActionToTake(final String actionCd) {
        final String actions = legalActions.get(docRouteStatus);
        return actions.contains(actionCd);
    }

    boolean isValidStatusChange(final String newStatus) {
        return stateTransitionMap.get(docRouteStatus).contains(newStatus);
    }

    private void setRouteStatus(final String newStatus, final boolean finalState) throws InvalidActionTakenException {
        if (!Objects.equals(newStatus, docRouteStatus)) {
            // only modify the status mod date if the status actually changed
            routeStatusDate = new Timestamp(System.currentTimeMillis());
        }
        if (stateTransitionMap.get(docRouteStatus).contains(newStatus)) {
            LOG.debug("changing status");
            docRouteStatus = newStatus;
        } else {
            LOG.debug("unable to change status");
            throw new InvalidActionTakenException(
                    "Document status " + CodeTranslator.getRouteStatusLabel(docRouteStatus)
                    + " cannot transition to status " + CodeTranslator.getRouteStatusLabel(newStatus));
        }
        dateModified = new Timestamp(System.currentTimeMillis());
        if (finalState) {
            LOG.debug("setting final timeStamp");
            finalizedDate = new Timestamp(System.currentTimeMillis());
        }
    }

    /**
     * Mark the document as being processed.
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentProcessed() throws InvalidActionTakenException {
        LOG.debug("{} marked processed", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_PROCESSED_CD, !FINAL_STATE);
    }

    /**
     * Mark document canceled.
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentCanceled() throws InvalidActionTakenException {
        LOG.debug("{} marked canceled", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_CANCEL_CD, FINAL_STATE);
    }

    /**
     * Mark document disapproved
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentDisapproved() throws InvalidActionTakenException {
        LOG.debug("{} marked disapproved", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD, FINAL_STATE);
    }

    /**
     * Mark document saved
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentSaved() throws InvalidActionTakenException {
        LOG.debug("{} marked saved", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_SAVED_CD, !FINAL_STATE);
    }

    /**
     * Mark the document as being actively routed.
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentEnroute() throws InvalidActionTakenException {
        LOG.debug("{} marked enroute", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_ENROUTE_CD, !FINAL_STATE);
    }

    /**
     * Mark document finalized.
     *
     * @throws InvalidActionTakenException
     */
    public void markDocumentFinalized() throws InvalidActionTakenException {
        LOG.debug("{} marked finalized", this);
        setRouteStatus(KewApiConstants.ROUTE_HEADER_FINAL_CD, FINAL_STATE);
    }

    public void applyDocumentUpdate(final DocumentUpdate documentUpdate) {
        if (documentUpdate != null) {
            final String thisDocTitle = docTitle == null ? "" : docTitle;
            final String updateDocTitle = documentUpdate.getTitle() == null ? "" : documentUpdate.getTitle();
            if (!StringUtils.equals(thisDocTitle, updateDocTitle)) {
                KEWServiceLocator.getActionListService()
                        .updateActionItemsForTitleChange(documentId, documentUpdate.getTitle());
            }
            docTitle = updateDocTitle;
            appDocId = documentUpdate.getApplicationDocumentId();
            dateModified = new Timestamp(System.currentTimeMillis());
            updateAppDocStatus(documentUpdate.getApplicationDocumentStatus());

            final Map<String, String> variables = documentUpdate.getVariables();
            for (final Map.Entry<String, String> entry : variables.entrySet()) {
                setVariable(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Convenience method that returns the branch of the first (and presumably only?) initial node
     *
     * @return the branch of the first (and presumably only?) initial node
     */
    public Branch getRootBranch() {
        if (!initialRouteNodeInstances.isEmpty()) {
            return getInitialRouteNodeInstance().getBranch();
        }
        return null;
    }

    /**
     * Looks up a variable (embodied in a "BranchState" key/value pair) in the
     * branch state table.
     */
    private BranchState findVariable(final String name) {
        final Branch rootBranch = getRootBranch();
        if (rootBranch != null) {
            final List<BranchState> branchState = rootBranch.getBranchState();
            for (final BranchState state : branchState) {
                if (Objects.equals(state.getKey(), BranchState.VARIABLE_PREFIX + name)) {
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * Sets a variable
     *
     * @param name  variable name
     * @param value variable value, or null if variable should be removed
     */
    public void setVariable(final String name, final String value) {
        BranchState state = findVariable(name);
        final Branch rootBranch = getRootBranch();
        if (rootBranch != null) {
            final List<BranchState> branchState = rootBranch.getBranchState();
            if (state == null) {
                if (value == null) {
                    LOG.debug("set non existent variable '{}' to null value", name);
                    return;
                }
                LOG.debug("Adding branch state: '{}'='{}'", name, value);
                state = new BranchState();
                state.setBranch(rootBranch);
                state.setKey(BranchState.VARIABLE_PREFIX + name);
                state.setValue(value);
                rootBranch.addBranchState(state);
            } else {
                if (value == null) {
                    LOG.debug("Removing value: {}={}", state::getKey, state::getValue);
                    branchState.remove(state);
                } else {
                    LOG.debug("Setting value of variable '{}' to '{}'", name, value);
                    state.setValue(value);
                }
            }
        }
    }

    public List<BranchState> getRootBranchState() {
        if (getRootBranch() != null) {
            return getRootBranch().getBranchState();
        }
        return null;
    }

    public CustomEmailAttribute getCustomEmailAttribute() {
        CustomEmailAttribute customEmailAttribute;
        try {
            if (getDocumentType() != null) {
                customEmailAttribute = getDocumentType().getCustomEmailAttribute();
                if (customEmailAttribute != null) {
                    customEmailAttribute.setRouteHeader(this);
                    return customEmailAttribute;
                }
            }
        } catch (final Exception e) {
            LOG.debug("Error in retrieving custom email attribute", e);
        }
        customEmailAttribute = new CustomEmailAttributeImpl();
        customEmailAttribute.setRouteHeader(this);
        return customEmailAttribute;
    }

    private RouteNodeInstance getInitialRouteNodeInstance() {
        return initialRouteNodeInstances.get(0);
    }

    public List<RouteNodeInstance> getInitialRouteNodeInstances() {
        return initialRouteNodeInstances;
    }

    public void setInitialRouteNodeInstances(final List<RouteNodeInstance> initialRouteNodeInstances) {
        this.initialRouteNodeInstances = initialRouteNodeInstances;
    }

    public DocumentRouteHeaderValueContent getDocumentContent() {
        if (documentContent == null) {
            documentContent = KEWServiceLocator.getRouteHeaderService().getContent(documentId);
        }
        return documentContent;
    }

    public void setDocumentContent(final DocumentRouteHeaderValueContent documentContent) {
        this.documentContent = documentContent;
    }

    public List<DocumentStatusTransition> getAppDocStatusHistory() {
        return appDocStatusHistory;
    }

    public void setAppDocStatusHistory(final List<DocumentStatusTransition> appDocStatusHistory) {
        this.appDocStatusHistory = appDocStatusHistory;
    }

    @Override
    public DocumentStatus getStatus() {
        return DocumentStatus.fromCode(docRouteStatus);
    }

    @Override
    public DateTime getDateCreated() {
        if (createDate == null) {
            return null;
        }
        return new DateTime(createDate.getTime());
    }

    @Override
    public DateTime getDateLastModified() {
        if (dateModified == null) {
            return null;
        }
        return new DateTime(dateModified.getTime());
    }

    @Override
    public DateTime getDateApproved() {
        if (approvedDate == null) {
            return null;
        }
        return new DateTime(approvedDate.getTime());
    }

    @Override
    public DateTime getDateFinalized() {
        if (finalizedDate == null) {
            return null;
        }
        return new DateTime(finalizedDate.getTime());
    }

    @Override
    public String getTitle() {
        return docTitle;
    }

    @Override
    public String getApplicationDocumentId() {
        return appDocId;
    }

    @Override
    public String getInitiatorPrincipalId() {
        return initiatorWorkflowId;
    }

    @Override
    public String getRoutedByPrincipalId() {
        return routedByUserWorkflowId;
    }

    @Override
    public String getDocumentTypeName() {
        return getDocumentType().getName();
    }

    @Override
    public String getDocumentHandlerUrl() {
        return getDocumentType().getResolvedDocumentHandlerUrl();
    }

    @Override
    public String getApplicationDocumentStatus() {
        return appDocStatus;
    }

    @Override
    public DateTime getApplicationDocumentStatusDate() {
        if (appDocStatusDate == null) {
            return null;
        }
        return new DateTime(appDocStatusDate.getTime());
    }

    @Override
    public Map<String, String> getVariables() {
        final Map<String, String> documentVariables = new HashMap<>();
        /* populate the routeHeaderVO with the document variables */
        // FIXME: we assume there is only one for now
        final Branch routeNodeInstanceBranch = getRootBranch();
        // Ok, we are using the "branch state" as the arbitrary convenient repository for flow/process/edoc variables,
        // so we need to stuff them into the VO
        if (routeNodeInstanceBranch != null) {
            final List<BranchState> listOfBranchStates = routeNodeInstanceBranch.getBranchState();
            for (final BranchState bs : listOfBranchStates) {
                if (bs.getKey() != null && bs.getKey().startsWith(BranchState.VARIABLE_PREFIX)) {
                    LOG.debug("Setting branch state variable on vo: {}={}", bs::getKey, bs::getValue);
                    documentVariables.put(bs.getKey().substring(BranchState.VARIABLE_PREFIX.length()), bs.getValue());
                }
            }
        }
        return documentVariables;
    }

    public DocumentRouteHeaderValue deepCopy(final Map<Object, Object> visited) {
        if (visited.containsKey(this)) {
            return (DocumentRouteHeaderValue) visited.get(this);
        }
        final DocumentRouteHeaderValue copy = new DocumentRouteHeaderValue();
        visited.put(this, copy);
        copy.documentId = documentId;
        copy.documentTypeId = documentTypeId;
        copy.docRouteStatus = docRouteStatus;
        copy.docRouteLevel = docRouteLevel;
        copy.dateModified = copyTimestamp(dateModified);
        copy.createDate = copyTimestamp(createDate);
        copy.approvedDate = copyTimestamp(approvedDate);
        copy.finalizedDate = copyTimestamp(finalizedDate);
        copy.docTitle = docTitle;
        copy.appDocId = appDocId;
        copy.docVersion = docVersion;
        copy.initiatorWorkflowId = initiatorWorkflowId;
        copy.routedByUserWorkflowId = routedByUserWorkflowId;
        copy.routeStatusDate = copyTimestamp(routeStatusDate);
        copy.appDocStatus = appDocStatus;
        copy.appDocStatusDate = copyTimestamp(appDocStatusDate);
        if (documentContent != null) {
            copy.documentContent = documentContent.deepCopy(visited);
        }
        if (initialRouteNodeInstances != null) {
            final List<RouteNodeInstance> copies = new ArrayList<>();
            for (final RouteNodeInstance routeNodeInstance : initialRouteNodeInstances) {
                copies.add(routeNodeInstance.deepCopy(visited));
            }
            copy.initialRouteNodeInstances = copies;
        }
        if (appDocStatusHistory != null) {
            final List<DocumentStatusTransition> copies = new ArrayList<>();
            for (final DocumentStatusTransition documentStatusTransition : appDocStatusHistory) {
                copies.add(documentStatusTransition.deepCopy(visited));
            }
            copy.appDocStatusHistory = copies;
        }

        return copy;
    }

    private static Timestamp copyTimestamp(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Timestamp(timestamp.getTime());
    }
}
