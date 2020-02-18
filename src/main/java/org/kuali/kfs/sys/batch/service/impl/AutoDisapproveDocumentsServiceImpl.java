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
package org.kuali.kfs.sys.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.workflow.service.WorkflowDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AutoDisapproveDocumentsStep;
import org.kuali.kfs.sys.batch.service.AutoDisapproveDocumentsService;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.service.FinancialSystemDocumentService;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.doctype.DocumentType;
import org.kuali.rice.kew.api.doctype.DocumentTypeService;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Transactional
public class AutoDisapproveDocumentsServiceImpl implements AutoDisapproveDocumentsService {

    private static final Logger LOG = LogManager.getLogger(AutoDisapproveDocumentsServiceImpl.class);
    public static final String WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY = "routeHeaderId";

    private DocumentService documentService;
    protected DocumentTypeService documentTypeService;

    private DateTimeService dateTimeService;
    private ParameterService parameterService;

    protected NoteService noteService;
    protected PersonService personService;

    private ReportWriterService autoDisapproveErrorReportWriterService;
    private FinancialSystemDocumentService financialSystemDocumentService;
    protected WorkflowDocumentService workflowDocumentService;

    /**
     * Gathers all documents that are in ENROUTE status and auto disapproves them.
     */
    @Override
    public boolean autoDisapproveDocumentsInEnrouteStatus() {
        boolean success = true;

        if (systemParametersForAutoDisapproveDocumentsJobExist()) {
            if (canAutoDisapproveJobRun()) {
                LOG.debug("autoDisapproveDocumentsInEnrouteStatus() started");

                Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);

                String principalId = systemUser.getPrincipalId();
                String annotationForAutoDisapprovalDocument = getParameterService()
                        .getParameterValueAsString(AutoDisapproveDocumentsStep.class,
                                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_ANNOTATION);

                success = processAutoDisapproveDocuments(principalId, annotationForAutoDisapprovalDocument);
            }
        }

        return success;
    }

    /**
     * This method checks if the System parameters have been set up for this batch job.
     *
     * @result return true if the system parameters exist, else false
     */
    protected boolean systemParametersForAutoDisapproveDocumentsJobExist() {
        LOG.debug("systemParametersForAutoDisapproveDocumentsJobExist() started.");

        boolean systemParametersExists = checkIfRunDateParameterExists();
        systemParametersExists &= checkIfParentDocumentTypeParameterExists();
        systemParametersExists &= checkIfDocumentCompareCreateDateParameterExists();
        systemParametersExists &= checkIfDocumentTypesExceptionParameterExists();
        systemParametersExists &= checkIfAnnotationForDisapprovalParameterExists();

        return systemParametersExists;
    }

    /**
     * This method checks for the system parameter for YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE
     *
     * @return true if YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE exists else false
     */
    protected boolean checkIfRunDateParameterExists() {
        // check to make sure the system parameter for run date check has already been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE System parameter does not exist in the parameters " +
                            "list.  The job can not continue without this parameter");
            return false;
        }

        return true;
    }

    /**
     * This method checks for the system parameter for YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE
     *
     * @return true if YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE exists else false
     */
    protected boolean checkIfParentDocumentTypeParameterExists() {
        // check to make sure the system parameter for Parent Document Type = FP has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist in the " +
                            "parameters list.  The job can not continue without this parameter");
            return false;
        }

        return true;
    }

    /**
     * This method checks for the system parameter for YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE
     *
     * @return true if YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE exists else false
     */
    protected boolean checkIfDocumentCompareCreateDateParameterExists() {
        // check to make sure the system parameter for create date to compare has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the " +
                    "parameters list.  The job can not continue without this parameter");
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist in the parameters " +
                            "list.  The job can not continue without this parameter");
            return false;
        }

        return true;
    }

    /**
     * This method checks for the system parameter for YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES
     *
     * @return true if YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES exists else false
     */
    protected boolean checkIfDocumentTypesExceptionParameterExists() {
        // check to make sure the system parameter for Document Types that are exceptions has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters " +
                    "list.  The job can not continue without this parameter");
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist in the parameters list. " +
                            "The job can not continue without this parameter");
            return false;
        }

        return true;
    }

    /**
     * This method checks for the system parameter for YEAR_END_AUTO_DISAPPROVE_ANNOTATION
     *
     * @return true if YEAR_END_AUTO_DISAPPROVE_ANNOTATION exists else false
     */
    protected boolean checkIfAnnotationForDisapprovalParameterExists() {
        // check to make sure the system parameter for annotation for notes has been setup...
        if (!getParameterService().parameterExists(AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_ANNOTATION)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_ANNOTATION System parameter does not exist in the parameters list. " +
                    "The job can not continue without this parameter");
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine("YEAR_END_AUTO_DISAPPROVE_ANNOTATION " +
                    "System parameter does not exist in the parameters list.  The job can not continue without this" +
                    "parameter");
            return false;
        }

        return true;
    }

    /**
     * This method will compare today's date to the system parameter for year end auto disapproval run date
     *
     * @return true if today's date equals to the system parameter run date
     */
    protected boolean canAutoDisapproveJobRun() {
        boolean autoDisapproveCanRun = true;

        // IF trunc(SYSDATE - 14/24) = v_yec_cncl_doc_run_dt THEN...FIS CODE equivalent here...
        String yearEndAutoDisapproveRunDate = getParameterService().getParameterValueAsString(
                AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE);

        String today = getDateTimeService().toDateString(getDateTimeService().getCurrentDate());

        if (!yearEndAutoDisapproveRunDate.equals(today)) {
            LOG.warn("YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE: Automatic disapproval bypassed. The date on " +
                    "which the auto disapproval step should run: " + yearEndAutoDisapproveRunDate +
                    " does not equal to today's date: " + today);
            String message = ("YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE: Automatic disapproval bypassed. The " +
                    "date on which the auto disapproval step should run: ")
                    .concat(yearEndAutoDisapproveRunDate).concat(" does not equal to today's date: ").concat(today);
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
            autoDisapproveCanRun = false;
        }

        return autoDisapproveCanRun;
    }

    /**
     * This method will use DocumentSearchCriteria to search for the documents that are in enroute status and
     * disapproves them
     *
     * @param principalId         The principal id which is KFS-SYS System user to run the process under.
     * @param annotation          The annotation to be set as note in the note of the document.
     */
    protected boolean processAutoDisapproveDocuments(String principalId, String annotation) {
        Collection<FinancialSystemDocumentHeader> documentList = getDocumentsToDisapprove();

        LOG.info("Total documents to process " + documentList.size());

        for (FinancialSystemDocumentHeader financialSystemDocumentHeader : documentList) {

            if (checkIfDocumentEligibleForAutoDispproval(financialSystemDocumentHeader)) {
                try {
                    autoDisapprovalYearEndDocument(financialSystemDocumentHeader, annotation);
                    sendAcknowledgement(financialSystemDocumentHeader, annotation);
                    LOG.info("The document with header id: " + financialSystemDocumentHeader.getDocumentNumber() +
                            " is automatically disapproved by this job.");
                } catch (Exception e) {
                    LOG.error("Exception encountered trying to auto disapprove the document " + e.getMessage());
                    String message = "Exception encountered trying to auto disapprove the document: "
                            .concat(financialSystemDocumentHeader.getDocumentNumber());
                    autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
                }
            }
        }

        return true;
    }

    protected Collection<FinancialSystemDocumentHeader> getDocumentsToDisapprove() {
        List<String> excludeWorkflowDocumentTypeNames = Arrays.asList(StringUtils.split(getParameterService()
                .getParameterValueAsString(AutoDisapproveDocumentsStep.class,
                        KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES), ";"));

        Timestamp documentCreatedDateWithMidNightTimpStamp = new Timestamp(getDocumentCompareDateParameter().getTime());

        return financialSystemDocumentService.findDocumentsByArguments(DocumentStatus.ENROUTE,
                excludeWorkflowDocumentTypeNames, documentCreatedDateWithMidNightTimpStamp);
    }

    /**
     * This method will check the document's document type against the parent document type as specified in the system parameter
     *
     * @param documentHeader
     * @return true if  document type of the document is a child of the parent document.
     */
    protected boolean checkIfDocumentEligibleForAutoDispproval(DocumentHeader documentHeader) {
        String yearEndAutoDisapproveParentDocumentType = getParameterService().getParameterValueAsString(
                AutoDisapproveDocumentsStep.class,
                KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE);

        DocumentType parentDocumentType = documentTypeService
                .getDocumentTypeByName(yearEndAutoDisapproveParentDocumentType);

        String documentTypeName = documentHeader.getWorkflowDocument().getDocumentTypeName();
        DocumentType childDocumentType = documentTypeService.getDocumentTypeByName(documentTypeName);
        return childDocumentType.getParentId().equals(parentDocumentType.getId());
    }

    /**
     * This method finds the date in the system parameters that will be used to compare the create date.
     * It then adds 23 hours, 59 minutes and 59 seconds to the compare date.
     *
     * @return documentCompareDate returns YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE from the system parameter
     */
    protected Date getDocumentCompareDateParameter() {
        Date documentCompareDate;

        String yearEndAutoDisapproveDocumentDate = getParameterService()
                .getParameterValueAsString(AutoDisapproveDocumentsStep.class,
                        KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE);

        if (ObjectUtils.isNull(yearEndAutoDisapproveDocumentDate)) {
            LOG.warn("Exception: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE can not be determined.");
            String message = "Exception: The value for System Parameter " +
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE can not be determined.  The auto disapproval job " +
                    "is stopped.";
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
            throw new RuntimeException("Exception: AutoDisapprovalStep job stopped because System Parameter " +
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE is null");
        }

        try {
            Date compareDate = getDateTimeService().convertToDate(yearEndAutoDisapproveDocumentDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(compareDate);
            calendar.set(Calendar.HOUR, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            documentCompareDate = calendar.getTime();
        } catch (ParseException pe) {
            LOG.warn("ParseException: System Parameter YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE can not " +
                    "be determined.");
            String message = "ParseException: The value for System Parameter " +
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE is invalid.  The auto disapproval job is stopped.";
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
            throw new RuntimeException("ParseException: AutoDisapprovalStep job stopped because System Parameter " +
                    "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE is invalid");
        }

        return documentCompareDate;
    }

    /**
     * autoDisapprovalYearEndDocument uses DocumentServiceImpl to  mark as disapproved by calling
     * DocumentServiceImpl's disapproveDocument method.
     *
     * @param documentHeader                       The document that needs to be auto disapproved in this process
     * @param annotationForAutoDisapprovalDocument The annotationForAutoDisapprovalDocument that is set as annotations
     *                                             when canceling the edoc.
     */
    protected void autoDisapprovalYearEndDocument(DocumentHeader documentHeader,
            String annotationForAutoDisapprovalDocument) throws Exception {
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);

        Note approveNote = noteService.createNote(new Note(), documentHeader, systemUser.getPrincipalId());
        approveNote.setNoteText(annotationForAutoDisapprovalDocument);

        approveNote.setAuthorUniversalIdentifier(systemUser.getPrincipalId());

        approveNote.setNotePostedTimestampToCurrent();

        noteService.save(approveNote);

        Document document = documentService.getByDocumentHeaderId(documentHeader.getDocumentNumber());

        document.addNote(approveNote);

        documentService.superUserDisapproveDocumentWithoutSaving(document,
                "Disapproval of Outstanding Documents - Year End Cancellation Process");
    }

    protected void sendAcknowledgement(DocumentHeader documentHeader, String annotation) {
        ArrayList<AdHocRouteRecipient> recipients = new ArrayList<>();

        try {
            if (ObjectUtils.isNotNull(documentHeader.getWorkflowDocument())) {
                Person initiator = personService.getPerson(documentHeader.getWorkflowDocument()
                        .getInitiatorPrincipalId());

                AdHocRoutePerson adHocRoutePerson = new AdHocRoutePerson();
                adHocRoutePerson.setActionRequested(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ);
                adHocRoutePerson.setId(initiator.getPrincipalName());
                recipients.add(adHocRoutePerson);

                if (KEWServiceLocator.getDocumentTypePermissionService().canReceiveAdHocRequest(initiator.getPrincipalId(),
                        DocumentRouteHeaderValue.from(documentHeader.getWorkflowDocument().getDocument()),
                        KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ)) {
                    workflowDocumentService.acknowledge(documentHeader.getWorkflowDocument(), annotation, recipients);
                } else {
                    String message = ("The principal '" + initiator.getPrincipalName() +
                            "' does not have permission to receive an acknowledgment for documentNumber=")
                            .concat(documentHeader.getDocumentNumber());
                    LOG.debug(message);
                    autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
                }
            }
        } catch (WorkflowException ex) {
            LOG.error("Exception encountered trying to send  acknowledge for document" + ex.getMessage());
            String message = "Exception encountered trying to send  acknowledge for document : "
                    .concat(documentHeader.getDocumentNumber());
            autoDisapproveErrorReportWriterService.writeFormattedMessageLine(message);
        }
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public DocumentService getDocumentService() {
        return documentService;
    }

    protected ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setDocumentTypeService(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    protected ReportWriterService getAutoDisapproveErrorReportWriterService() {
        return autoDisapproveErrorReportWriterService;
    }

    public void setAutoDisapproveErrorReportWriterService(ReportWriterService autoDisapproveErrorReportWriterService) {
        this.autoDisapproveErrorReportWriterService = autoDisapproveErrorReportWriterService;
    }

    public FinancialSystemDocumentService getFinancialSystemDocumentService() {
        return financialSystemDocumentService;
    }

    public void setFinancialSystemDocumentService(FinancialSystemDocumentService financialSystemDocumentService) {
        this.financialSystemDocumentService = financialSystemDocumentService;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
