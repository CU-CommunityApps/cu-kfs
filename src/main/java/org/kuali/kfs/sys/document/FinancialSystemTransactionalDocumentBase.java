/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.document;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.document.TransactionalDocumentBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.dataaccess.FinancialSystemDocumentHeaderDao;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.kew.api.WorkflowRuntimeException;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteLevelChange;
import org.kuali.rice.kew.framework.postprocessor.DocumentRouteStatusChange;

import java.util.Map;
import java.util.Set;

/**
 * This class is a KFS specific TransactionalDocumentBase class
 */
public class FinancialSystemTransactionalDocumentBase extends TransactionalDocumentBase implements FinancialSystemTransactionalDocument {
    private static final Logger LOG = LogManager.getLogger(FinancialSystemTransactionalDocumentBase.class);

    protected static final String UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME = "UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_IND";

    private static transient BusinessObjectService businessObjectService;
    private static transient FinancialSystemDocumentService financialSystemDocumentService;
    private static transient ParameterService parameterService;

    private transient Map<String, Boolean> canEditCache;

    public FinancialSystemTransactionalDocumentBase() {
        super();
    }

    @Override
    public FinancialSystemDocumentHeader getFinancialSystemDocumentHeader() {
        return (FinancialSystemDocumentHeader) documentHeader;
    }

    /**
     * @see org.kuali.rice.krad.document.DocumentBase#setDocumentHeader(org.kuali.rice.krad.bo.DocumentHeader)
     */
    @Override
    public void setDocumentHeader(DocumentHeader documentHeader) {
        if ((documentHeader != null) && (!FinancialSystemDocumentHeader.class.isAssignableFrom(documentHeader.getClass()))) {
            throw new IllegalArgumentException("document header of class '" + documentHeader
                .getClass() + "' is not assignable from financial document header class '" + FinancialSystemDocumentHeader.class + "'");
        }
        this.documentHeader = documentHeader;
    }

    /**
     * If the document has a total amount, call method on document to get the total and set in doc header.
     *
     * @see org.kuali.rice.krad.document.Document#prepareForSave()
     */
    @Override
    public void prepareForSave() {
        if (this instanceof AmountTotaling) {
            getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(((AmountTotaling) this).getTotalDollarAmount());
        }
        captureWorkflowHeaderInformation();
        super.prepareForSave();
    }

    /**
     * Attempts to capture the document type name, initiator principal id, and KEW document status code to the Financial
     * System Document Header
     */
    protected void captureWorkflowHeaderInformation() {
        if (StringUtils.isBlank(getFinancialSystemDocumentHeader().getInitiatorPrincipalId())) {
            getFinancialSystemDocumentHeader().setInitiatorPrincipalId(
                getFinancialSystemDocumentHeader().getWorkflowDocument().getInitiatorPrincipalId());
        }
        if (StringUtils.isBlank(getFinancialSystemDocumentHeader().getWorkflowDocumentTypeName())) {
            getFinancialSystemDocumentHeader().setWorkflowDocumentTypeName(
                getFinancialSystemDocumentHeader().getWorkflowDocument().getDocumentTypeName());
        }
        if (ObjectUtils.isNull(getFinancialSystemDocumentHeader().getWorkflowCreateDate())) {
            getFinancialSystemDocumentHeader().setWorkflowCreateDate(new java.sql.Timestamp(
                getFinancialSystemDocumentHeader().getWorkflowDocument().getDateCreated().getMillis()));
        }
        final String statusCode = getWorkflowDocumentStatusCode(
            getFinancialSystemDocumentHeader().getWorkflowDocument().getStatus());
        getFinancialSystemDocumentHeader().setWorkflowDocumentStatusCode(statusCode);
    }

    /**
     * Given a DocumentStatus, returns the code for that status.  Allows us a shim to change initiated's into saved's
     *
     * @param status the status to return a code for
     * @return the code for that stat
     */
    protected String getWorkflowDocumentStatusCode(DocumentStatus status) {
        // we're preparing to save here.  If the save fails, the transaction should roll back - so the fact that the
        // doc header is in saved mode shouldn't cause problems.  And since
        // org.kuali.rice.krad.service.impl.PostProcessorServiceImpl#doRouteStatusChange will NOT save the document when
        // the DocStatus is saved, let's simply pre-anticipate that
        final String statusCode = status.equals(DocumentStatus.INITIATED) ?
            DocumentStatus.SAVED.getCode() :
            getFinancialSystemDocumentHeader().getWorkflowDocument().getStatus().getCode();
        return statusCode;
    }

    /**
     * This is the default implementation which ensures that document note attachment references are loaded.
     *
     * @see org.kuali.rice.krad.document.Document#processAfterRetrieve()
     */
    @Override
    public void processAfterRetrieve() {
        // set correctedByDocumentId manually, since OJB doesn't maintain that relationship
        boolean createdSessionForSystemUser = false;
        try {
            DocumentHeader correctingDocumentHeader = SpringContext.getBean(FinancialSystemDocumentHeaderDao.class)
                .getCorrectingDocumentHeader(getFinancialSystemDocumentHeader().getDocumentNumber());
            if (ObjectUtils.isNotNull(correctingDocumentHeader)) {
                // if we are sessionless, create session for system user
                if (GlobalVariables.getUserSession() == null) {
                    GlobalVariables.setUserSession(new UserSession(KRADConstants.SYSTEM_USER));
                    createdSessionForSystemUser = true;
                }

                WorkflowDocument workflowDocument = correctingDocumentHeader.getWorkflowDocument();
                if (!workflowDocument.isCanceled() && !workflowDocument.isDisapproved()) {
                    getFinancialSystemDocumentHeader().setCorrectedByDocumentId(correctingDocumentHeader.getDocumentNumber());
                }
            }
        } catch (Exception e) {
            LOG.error("Received WorkflowException trying to get route header id from workflow document.", e);
            throw new WorkflowRuntimeException(e);
        } finally {
            if (createdSessionForSystemUser) {
                GlobalVariables.setUserSession(null);
            }
        }
        // set the ad hoc route recipients too, since OJB doesn't maintain that relationship
        // TODO - see KULNRVSYS-1054

        super.processAfterRetrieve();
    }

    /**
     * This is the default implementation which checks for a different workflow statuses, and updates the Kuali status
     * accordingly.
     *
     * @see org.kuali.kfs.krad.document.Document#doRouteStatusChange(DocumentRouteStatusChange)
     */
    @Override
    public void doRouteStatusChange(DocumentRouteStatusChange statusChangeEvent) {
        FinancialSystemDocumentHeader financialSystemDocumentHeader = getFinancialSystemDocumentHeader();

        // set the route status
        financialSystemDocumentHeader.setWorkflowDocumentStatusCode(
            getWorkflowDocumentStatusCode(DocumentStatus.fromCode(statusChangeEvent.getNewRouteStatus())));
        financialSystemDocumentHeader.setApplicationDocumentStatus(
            financialSystemDocumentHeader.getWorkflowDocument().getApplicationDocumentStatus());

        WorkflowDocument workflowDocument = getDocumentHeader().getWorkflowDocument();
        if (workflowDocument.isCanceled()) {
            financialSystemDocumentHeader.setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.CANCELLED);
        } else if (workflowDocument.isEnroute()) {
            financialSystemDocumentHeader.setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.ENROUTE);
        }
        if (workflowDocument.isDisapproved()) {
            financialSystemDocumentHeader.setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.DISAPPROVED);
        }
        if (workflowDocument.isProcessed()) {
            financialSystemDocumentHeader.setFinancialDocumentStatusCode(KFSConstants.DocumentStatusCodes.APPROVED);
            financialSystemDocumentHeader.setProcessedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Document: " + statusChangeEvent.getDocumentId() + " -- Status is: "
                + financialSystemDocumentHeader.getFinancialDocumentStatusCode());
        }

        super.doRouteStatusChange(statusChangeEvent);
    }

    /**
     * This is the default implementation which, if parameter KFS-SYS / Document / UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_IND
     * is on, updates the document and resaves if needed
     *
     * @see org.kuali.kfs.krad.document.DocumentBase#doRouteLevelChange(DocumentRouteLevelChange)
     */
    @Override
    public void doRouteLevelChange(DocumentRouteLevelChange levelChangeEvent) {
        // grab the new app doc status
        getFinancialSystemDocumentHeader().setApplicationDocumentStatus(
            getFinancialSystemDocumentHeader().getWorkflowDocument().getApplicationDocumentStatus());

        if (this instanceof AmountTotaling
            && getDocumentHeader() != null
            && getParameterService() != null
            && getBusinessObjectService() != null
            && getParameterService().parameterExists(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)
            && getParameterService().getParameterValueAsBoolean(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, UPDATE_TOTAL_AMOUNT_IN_POST_PROCESSING_PARAMETER_NAME)) {
            final KualiDecimal currentTotal = ((AmountTotaling) this).getTotalDollarAmount();
            if (!currentTotal.equals(getFinancialSystemDocumentHeader().getFinancialDocumentTotalAmount())) {
                getFinancialSystemDocumentHeader().setFinancialDocumentTotalAmount(currentTotal);
            }
        }
        if (this instanceof AmountTotaling || !StringUtils.isBlank(getFinancialSystemDocumentHeader().getApplicationDocumentStatus())) {
            LOG.warn("We're going to save the document header because we've moved from level: " + levelChangeEvent
                .getOldNodeName() + " to " + levelChangeEvent.getNewNodeName());
            getBusinessObjectService().save(getFinancialSystemDocumentHeader());
        }
        super.doRouteLevelChange(levelChangeEvent);
    }

    /**
     * @see org.kuali.kfs.sys.document.Correctable#toErrorCorrection()
     */
    public void toErrorCorrection() throws WorkflowException, IllegalStateException {
        DocumentHelperService documentHelperService = SpringContext.getBean(DocumentHelperService.class);
        final Set<String> documentActionsFromPresentationController = documentHelperService
            .getDocumentPresentationController(this).getDocumentActions(this);
        final Set<String> documentActionsFromAuthorizer = documentHelperService.getDocumentAuthorizer(this)
            .getDocumentActions(this, GlobalVariables.getUserSession().getPerson(),
                documentActionsFromPresentationController);
        if (!documentActionsFromAuthorizer.contains(KFSConstants.KFS_ACTION_CAN_ERROR_CORRECT)) {
            throw new IllegalStateException(this.getClass().getName() + " does not support document-level error correction");
        }

        String sourceDocumentHeaderId = getDocumentNumber();
        setNewDocumentHeader();
        getFinancialSystemDocumentHeader().setFinancialDocumentInErrorNumber(sourceDocumentHeaderId);

        //clear out notes from previous bo
        getNotes().clear();

        addCopyErrorDocumentNote("error-correction for document " + sourceDocumentHeaderId);
    }

    @Override
    public boolean answerSplitNodeQuestion(String nodeName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
            "No split node logic defined for split node " + nodeName + " on " + this.getClass().getSimpleName());
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        FinancialSystemTransactionalDocumentBase.parameterService = parameterService;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        FinancialSystemTransactionalDocumentBase.businessObjectService = businessObjectService;
    }

    protected FinancialSystemDocumentService getFinancialSystemDocumentService() {
        if (financialSystemDocumentService == null) {
            financialSystemDocumentService = SpringContext.getBean(FinancialSystemDocumentService.class);
        }
        return financialSystemDocumentService;
    }

    @Override
    public void toCopy() throws WorkflowException, IllegalStateException {
        FinancialSystemDocumentHeader oldDocumentHeader = getFinancialSystemDocumentHeader();
        super.toCopy();

        getFinancialSystemDocumentService().prepareToCopy(oldDocumentHeader, this);
    }

    /**
     * Updates status of this document and saves the workflow data
     *
     * @param applicationDocumentStatus is the app doc status to save
     * @throws WorkflowException
     */
    @Override
    public void updateAndSaveAppDocStatus(String applicationDocumentStatus) throws WorkflowException {
        getFinancialSystemDocumentHeader().updateAndSaveAppDocStatus(applicationDocumentStatus);
    }

    /**
     * @return Returns the applicationDocumentStatus
     */
    @Override
    public String getApplicationDocumentStatus() {
        return getFinancialSystemDocumentHeader().getApplicationDocumentStatus();
    }

    /**
     * @param applicationDocumentStatus The applicationDocumentStatus to set.
     */
    @Override
    public void setApplicationDocumentStatus(String applicationDocumentStatus) {
        getFinancialSystemDocumentHeader().setApplicationDocumentStatus(applicationDocumentStatus);
    }
}
