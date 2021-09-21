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
package org.kuali.kfs.krad.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.framework.persistence.jta.TransactionalNoValidationExceptionRollback;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.AdHocRouteWorkgroup;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.dao.DocumentDao;
import org.kuali.kfs.krad.datadictionary.exception.UnknownDocumentTypeException;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.DocumentAuthorizer;
import org.kuali.kfs.krad.document.DocumentPresentationController;
import org.kuali.kfs.krad.exception.DocumentAuthorizationException;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.rules.rule.event.ApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.BlanketApproveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.CompleteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.KualiDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SaveEvent;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentHeaderService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.dao.OptimisticLockingFailureException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for the Document structure. It contains all of the document level type of processing and
 * calling back into documents for various centralization of functionality. This is the default, Kuali delivered
 * implementation which utilizes Workflow.
 */
@TransactionalNoValidationExceptionRollback
public class DocumentServiceImpl implements DocumentService {

    private static final Logger LOG = LogManager.getLogger();
    private static final String MESSAGE_NOTE_NOTIFICATION_ANNOTATION = "message.note.notification.annotation";

    private DocumentDao documentDao;

    private DateTimeService dateTimeService;
    private NoteService noteService;
    private WorkflowDocumentService workflowDocumentService;
    private BusinessObjectService businessObjectService;
    private DataDictionaryService dataDictionaryService;
    private DocumentHeaderService documentHeaderService;
    private DocumentDictionaryService documentDictionaryService;
    private PersonService personService;
    private ConfigurationService kualiConfigurationService;

    @Override
    public Document saveDocument(Document document) throws WorkflowException, ValidationException {
        return saveDocument(document, SaveDocumentEvent.class);
    }

    @Override
    public Document saveDocument(Document document, Class<? extends KualiDocumentEvent> kualiDocumentEventClass)
            throws WorkflowException, ValidationException {
        checkForNulls(document);
        if (kualiDocumentEventClass == null) {
            throw new IllegalArgumentException("invalid (null) kualiDocumentEventClass");
        }
        // if event is not an instance of a SaveDocumentEvent or a SaveOnlyDocumentEvent
        if (!SaveEvent.class.isAssignableFrom(kualiDocumentEventClass)) {
            throw new ConfigurationException("The KualiDocumentEvent class '" + kualiDocumentEventClass.getName() +
                "' does not implement the class '" + SaveEvent.class.getName() + "'");
        }

        document.prepareForSave();
        Document savedDocument = validateAndPersistDocumentAndSaveAdHocRoutingRecipients(document,
            generateKualiDocumentEvent(document, kualiDocumentEventClass));
        prepareWorkflowDocument(savedDocument);
        getWorkflowDocumentService().save(savedDocument.getDocumentHeader().getWorkflowDocument(), null);

        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
            savedDocument.getDocumentHeader().getWorkflowDocument());

        return savedDocument;
    }

    private KualiDocumentEvent generateKualiDocumentEvent(Document document,
            Class<? extends KualiDocumentEvent> eventClass) throws ConfigurationException {
        String potentialErrorMessage = "Found error trying to generate Kuali Document Event using event class '" +
                eventClass.getName() + "' for document " + document.getDocumentNumber();

        try {
            Constructor<?> usableConstructor = null;
            List<Object> paramList = new ArrayList<>();
            for (Constructor<?> currentConstructor : eventClass.getConstructors()) {
                for (Class<?> parameterClass : currentConstructor.getParameterTypes()) {
                    if (Document.class.isAssignableFrom(parameterClass)) {
                        usableConstructor = currentConstructor;
                        paramList.add(document);
                    } else {
                        paramList.add(null);
                    }
                }
                if (ObjectUtils.isNotNull(usableConstructor)) {
                    break;
                }
            }
            if (usableConstructor == null) {
                throw new RuntimeException("Cannot find a constructor for class '" + eventClass.getName() +
                    "' that takes in a document parameter");
            }
            return (KualiDocumentEvent) usableConstructor.newInstance(paramList.toArray());
        } catch (SecurityException | IllegalArgumentException | InstantiationException | InvocationTargetException |
                IllegalAccessException e) {
            throw new ConfigurationException(potentialErrorMessage, e);
        }
    }

    @Override
    public Document routeDocument(Document document, String annotation, List<AdHocRouteRecipient> adHocRecipients)
            throws ValidationException, WorkflowException {
        checkForNulls(document);
        document.prepareForSave();
        Document savedDocument = validateAndPersistDocument(document, new RouteDocumentEvent(document));
        prepareWorkflowDocument(savedDocument);
        getWorkflowDocumentService().route(savedDocument.getDocumentHeader().getWorkflowDocument(), annotation,
                adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                savedDocument.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(savedDocument);
        return savedDocument;
    }

    @Override
    public Document approveDocument(Document document, String annotation, List<AdHocRouteRecipient> adHocRecipients)
            throws ValidationException, WorkflowException {
        checkForNulls(document);
        document.prepareForSave();
        Document savedDocument = validateAndPersistDocument(document, new ApproveDocumentEvent(document));
        prepareWorkflowDocument(savedDocument);
        getWorkflowDocumentService().approve(savedDocument.getDocumentHeader().getWorkflowDocument(), annotation,
                adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                savedDocument.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(savedDocument);
        return savedDocument;
    }

    @Override
    public Document superUserApproveDocument(Document document, String annotation) throws WorkflowException {
        getDocumentDao().save(document);
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().superUserApprove(document.getDocumentHeader().getWorkflowDocument(), annotation);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document superUserCancelDocument(Document document, String annotation) throws WorkflowException {
        getDocumentDao().save(document);
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().superUserCancel(document.getDocumentHeader().getWorkflowDocument(), annotation);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document superUserDisapproveDocument(Document document, String annotation) throws WorkflowException {
        getDocumentDao().save(document);
        return superUserDisapproveDocumentWithoutSaving(document, annotation);
    }

    @Override
    public Document superUserDisapproveDocumentWithoutSaving(Document document, String annotation) throws
            WorkflowException {
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().superUserDisapprove(document.getDocumentHeader().getWorkflowDocument(),
                annotation);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document disapproveDocument(Document document, String annotation) throws Exception {
        checkForNulls(document);

        Note note = createNoteFromDocument(document, annotation);
        //if note type is BO, override and link disapprove notes to Doc Header
        if (document.getNoteType().equals(NoteType.BUSINESS_OBJECT)) {
            note.setNoteTypeCode(NoteType.DOCUMENT_HEADER.getCode());
            note.setRemoteObjectIdentifier(document.getDocumentHeader().getObjectId());
        }
        document.addNote(note);

        // SAVE THE NOTE
        // Note: This save logic is replicated here and in KualiDocumentAction, when to save (based on doc state)
        // should be moved into a doc service method
        getNoteService().save(note);

        prepareWorkflowDocument(document);
        getWorkflowDocumentService().disapprove(document.getDocumentHeader().getWorkflowDocument(), annotation);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document cancelDocument(Document document, String annotation) throws WorkflowException {
        checkForNulls(document);
        if (document instanceof MaintenanceDocument) {
            MaintenanceDocument maintDoc = (MaintenanceDocument) document;
            if (maintDoc.getOldMaintainableObject() != null
                    && maintDoc.getOldMaintainableObject().getDataObject() instanceof BusinessObject) {
                ((BusinessObject) maintDoc.getOldMaintainableObject().getDataObject()).refresh();
            }

            if (maintDoc.getNewMaintainableObject().getDataObject() instanceof BusinessObject) {
                ((BusinessObject) maintDoc.getNewMaintainableObject().getDataObject()).refresh();
            }
        }
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().cancel(document.getDocumentHeader().getWorkflowDocument(), annotation);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
            document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document recallDocument(Document document, String annotation, boolean cancel) throws WorkflowException {
        checkForNulls(document);

        Note note = createNoteFromDocument(document, annotation);
        document.addNote(note);
        getNoteService().save(note);

        prepareWorkflowDocument(document);
        getWorkflowDocumentService().recall(document.getDocumentHeader().getWorkflowDocument(), annotation, cancel);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document acknowledgeDocument(Document document, String annotation,
            List<AdHocRouteRecipient> adHocRecipients) throws WorkflowException {
        checkForNulls(document);
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().acknowledge(document.getDocumentHeader().getWorkflowDocument(), annotation,
                adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document blanketApproveDocument(Document document, String annotation,
            List<AdHocRouteRecipient> adHocRecipients) throws ValidationException, WorkflowException {
        checkForNulls(document);
        document.prepareForSave();
        Document savedDocument = validateAndPersistDocument(document, new BlanketApproveDocumentEvent(document));
        prepareWorkflowDocument(savedDocument);
        getWorkflowDocumentService()
            .blanketApprove(savedDocument.getDocumentHeader().getWorkflowDocument(), annotation, adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
            savedDocument.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(savedDocument);
        return savedDocument;
    }

    @Override
    public Document clearDocumentFyi(Document document, List<AdHocRouteRecipient> adHocRecipients) throws
            WorkflowException {
        checkForNulls(document);
        // populate document content so searchable attributes will be indexed properly
        document.populateDocumentForRouting();
        getWorkflowDocumentService().clearFyi(document.getDocumentHeader().getWorkflowDocument(), adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
            document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
        return document;
    }

    @Override
    public Document completeDocument(Document document, String annotation, List<AdHocRouteRecipient> adHocRecipients)
            throws WorkflowException {
        checkForNulls(document);

        document.prepareForSave();
        validateAndPersistDocument(document, new CompleteDocumentEvent(document));

        prepareWorkflowDocument(document);
        getWorkflowDocumentService().complete(document.getDocumentHeader().getWorkflowDocument(), annotation,
                adHocRecipients);

        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());

        removeAdHocPersonsAndWorkgroups(document);

        return document;
    }

    protected void checkForNulls(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("invalid (null) document");
        }
        if (document.getDocumentNumber() == null) {
            throw new IllegalStateException("invalid (null) documentHeaderId");
        }
    }

    private Document validateAndPersistDocumentAndSaveAdHocRoutingRecipients(Document document,
            KualiDocumentEvent event) {
        // Using this method to wrap validateAndPersistDocument to keep everything in one transaction. This avoids
        // modifying the signature on validateAndPersistDocument method
        List<AdHocRouteRecipient> adHocRoutingRecipients = new ArrayList<>();
        adHocRoutingRecipients.addAll(document.getAdHocRoutePersons());
        adHocRoutingRecipients.addAll(document.getAdHocRouteWorkgroups());

        for (AdHocRouteRecipient recipient : adHocRoutingRecipients) {
            recipient.setdocumentNumber(document.getDocumentNumber());
        }
        Map<String, String> criteria = new HashMap<>();
        criteria.put("documentNumber", document.getDocumentNumber());
        getBusinessObjectService().deleteMatching(AdHocRouteRecipient.class, criteria);

        getBusinessObjectService().save(adHocRoutingRecipients);
        return validateAndPersistDocument(document, event);
    }

    @Override
    public boolean documentExists(String documentHeaderId) {
        // validate parameters
        if (StringUtils.isBlank(documentHeaderId)) {
            throw new IllegalArgumentException("invalid (blank) documentHeaderId");
        }

        boolean internalUserSession = false;
        try {
            // KFSMI-2543 - allowed method to run without a user session so it can be used by workflow processes
            if (GlobalVariables.getUserSession() == null) {
                internalUserSession = true;
                GlobalVariables.setUserSession(new UserSession(KRADConstants.SYSTEM_USER));
                GlobalVariables.clear();
            }

            // look for workflowDocumentHeader, since that supposedly won't break the transaction
            if (getWorkflowDocumentService().doesDocumentExist(documentHeaderId)) {
                // look for docHeaderId, since that fails without breaking the transaction
                return getDocumentHeaderService().getDocumentHeaderById(documentHeaderId) != null;
            }

            return false;
        } finally {
            // if a user session was established for this call, clear it our
            if (internalUserSession) {
                GlobalVariables.clear();
                GlobalVariables.setUserSession(null);
            }
        }
    }

    /**
     * Creates a new document by class.
     */
    @Override
    public Document getNewDocument(Class<? extends Document> documentClass) throws WorkflowException {
        if (documentClass == null) {
            throw new IllegalArgumentException("invalid (null) documentClass");
        }
        if (!Document.class.isAssignableFrom(documentClass)) {
            throw new IllegalArgumentException("invalid (non-Document) documentClass");
        }

        String documentTypeName = getDataDictionaryService().getDocumentTypeNameByClass(documentClass);
        if (StringUtils.isBlank(documentTypeName)) {
            throw new UnknownDocumentTypeException(
                "unable to get documentTypeName for unknown documentClass '" + documentClass.getName() + "'");
        }
        return getNewDocument(documentTypeName);
    }

    /**
     * Creates a new document by document type name. The principal name passed in will be used as the document
     * initiator. If the  initiatorPrincipalNm is null or blank, the current user will be used.
     */
    @Override
    public Document getNewDocument(String documentTypeName, String initiatorPrincipalNm) throws WorkflowException {
        // argument validation
        String watchName = "DocumentServiceImpl.getNewDocument";
        StopWatch watch = new StopWatch();
        watch.start();
        if (LOG.isDebugEnabled()) {
            LOG.debug(watchName + ": started");
        }
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("invalid (blank) documentTypeName");
        }
        if (GlobalVariables.getUserSession() == null) {
            throw new IllegalStateException(
                "GlobalVariables must be populated with a valid UserSession before a new document can be created");
        }

        // get the class for this docTypeName
        Class<? extends Document> documentClass = getDocumentClassByTypeName(documentTypeName);

        // get the initiator
        Person initiator;
        if (StringUtils.isBlank(initiatorPrincipalNm)) {
            initiator = GlobalVariables.getUserSession().getPerson();
        } else {
            initiator = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(initiatorPrincipalNm);
            if (ObjectUtils.isNull(initiator)) {
                initiator = GlobalVariables.getUserSession().getPerson();
            }
        }

        // get the authorization
        DocumentAuthorizer documentAuthorizer = getDocumentDictionaryService().getDocumentAuthorizer(documentTypeName);
        DocumentPresentationController documentPresentationController =
            getDocumentDictionaryService().getDocumentPresentationController(documentTypeName);
        // make sure this person is authorized to initiate
        LOG.debug("calling canInitiate from getNewDocument()");
        if (!documentPresentationController.canInitiate(documentTypeName) ||
            !documentAuthorizer.canInitiate(documentTypeName, initiator)) {
            throw new DocumentAuthorizationException(initiator.getPrincipalName(), "initiate", documentTypeName);
        }

        // initiate new workflow entry, get the workflow doc
        WorkflowDocument workflowDocument = getWorkflowDocumentService().createWorkflowDocument(documentTypeName,
                initiator);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), workflowDocument);

        // create a new document header object
        DocumentHeader documentHeader;
        try {
            // create a new document header object
            Class<? extends DocumentHeader> documentHeaderClass =
                getDocumentHeaderService().getDocumentHeaderBaseClass();
            documentHeader = documentHeaderClass.newInstance();
            documentHeader.setWorkflowDocument(workflowDocument);
            documentHeader.setDocumentNumber(workflowDocument.getDocumentId());
            // status and notes are initialized correctly in the constructor
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Error instantiating DocumentHeader", e);
        }

        // build Document of specified type
        Document document;
        try {
            // all maintenance documents have same class
            if (MaintenanceDocumentBase.class.isAssignableFrom(documentClass)) {
                Class<?>[] defaultConstructor = new Class[]{String.class};
                Constructor<? extends Document> cons = documentClass.getConstructor(defaultConstructor);
                if (ObjectUtils.isNull(cons)) {
                    throw new ConfigurationException("Could not find constructor with document type name parameter " +
                            "needed for Maintenance Document Base class");
                }
                document = cons.newInstance(documentTypeName);
            } else {
                // non-maintenance document
                document = documentClass.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Error instantiating Document", e);
        } catch (SecurityException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Error instantiating Maintenance Document", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error instantiating Maintenance Document: No constructor with String " +
                    "parameter found", e);
        }

        document.setDocumentHeader(documentHeader);
        document.setDocumentNumber(documentHeader.getDocumentNumber());

        watch.stop();
        if (LOG.isDebugEnabled()) {
            LOG.debug(watchName + ": " + watch.toString());
        }

        return document;
    }

    /**
     * Creates a new document by document type name.
     */
    @Override
    public Document getNewDocument(String documentTypeName) throws WorkflowException {
        return getNewDocument(documentTypeName, null);
    }

    /**
     * This is temporary until workflow 2.0 and reads from a table to get documents whose status has changed to A
     * (approved - no outstanding approval actions requested)
     *
     * @param documentHeaderId
     * @return Document
     * @throws WorkflowException
     */
    @Override
    public Document getByDocumentHeaderId(String documentHeaderId) throws WorkflowException {
        if (documentHeaderId == null) {
            throw new IllegalArgumentException("invalid (null) documentHeaderId");
        }
        boolean internalUserSession = false;
        try {
            // KFSMI-2543 - allowed method to run without a user session so it can be used by workflow processes
            if (GlobalVariables.getUserSession() == null) {
                internalUserSession = true;
                GlobalVariables.setUserSession(new UserSession(KRADConstants.SYSTEM_USER));
                GlobalVariables.clear();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieving doc id: " + documentHeaderId + " from workflow service.");
            }
            WorkflowDocument workflowDocument = getWorkflowDocumentService().loadWorkflowDocument(
                    documentHeaderId, GlobalVariables.getUserSession().getPerson());
            UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), workflowDocument);

            Class<? extends Document> documentClass = getDocumentClassByTypeName(workflowDocument.getDocumentTypeName());

            // retrieve the Document
            Document document = getDocumentDao().findByDocumentHeaderId(documentClass, documentHeaderId);

            return postProcessDocument(documentHeaderId, workflowDocument, document);
        } finally {
            // if a user session was established for this call, clear it out
            if (internalUserSession) {
                GlobalVariables.clear();
                GlobalVariables.setUserSession(null);
            }
        }
    }

    @Override
    public Document getByDocumentHeaderIdSessionless(String documentHeaderId) throws WorkflowException {
        if (documentHeaderId == null) {
            throw new IllegalArgumentException("invalid (null) documentHeaderId");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving doc id: " + documentHeaderId + " from workflow service.");
        }

        Person person = getPersonService().getPersonByPrincipalName(KRADConstants.SYSTEM_USER);
        WorkflowDocument workflowDocument = workflowDocumentService.loadWorkflowDocument(documentHeaderId, person);

        Class<? extends Document> documentClass = getDocumentClassByTypeName(workflowDocument.getDocumentTypeName());

        // retrieve the Document
        Document document = getDocumentDao().findByDocumentHeaderId(documentClass, documentHeaderId);

        return postProcessDocument(documentHeaderId, workflowDocument, document);
    }

    private Class<? extends Document> getDocumentClassByTypeName(String documentTypeName) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("invalid (blank) documentTypeName");
        }

        Class<? extends Document> clazz = getDataDictionaryService().getDocumentClassByTypeName(documentTypeName);
        if (clazz == null) {
            throw new UnknownDocumentTypeException("unable to get class for unknown documentTypeName '" +
                    documentTypeName + "'");
        }
        return clazz;
    }

    /**
     * Loads the Notes for the note target on this Document.
     *
     * @param document the document for which to load the notes
     */
    protected void loadNotes(Document document) {
        if (isNoteTargetReady(document)) {
            List<Note> notes = new ArrayList<>();
            if (StringUtils.isNotBlank(document.getNoteTarget().getObjectId())) {
                notes.addAll(getNoteService().getByRemoteObjectId(document.getNoteTarget().getObjectId()));
            }
            //notes created on 'disapprove' are linked to Doc Header, so this checks that even if notetype = BO
            if (document.getNoteType().equals(NoteType.BUSINESS_OBJECT)
                && document.getDocumentHeader().getWorkflowDocument().isDisapproved()) {
                notes.addAll(getNoteService().getByRemoteObjectId(document.getDocumentHeader().getObjectId()));
            }

            // KULRNE-5692 - force a refresh of the attachments
            // they are not (non-updateable) references and don't seem to update properly upon load
            for (Note note : notes) {
                note.refreshReferenceObject("attachment");
            }
            document.setNotes(notes);
        }
    }

    /**
     * Performs required post-processing for every document from the documentDao
     *
     * @param documentHeaderId
     * @param workflowDocument
     * @param document
     */
    protected Document postProcessDocument(String documentHeaderId, WorkflowDocument workflowDocument,
            Document document) {
        if (document != null) {
            document.getDocumentHeader().setWorkflowDocument(workflowDocument);
            document.processAfterRetrieve();
            loadNotes(document);
        }
        return document;
    }

    /**
     * The default implementation - this retrieves all documents by a list of documentHeader for a given class.
     */
    @Override
    public List<Document> getDocumentsByListOfDocumentHeaderIds(Class<? extends Document> documentClass,
            List<String> documentHeaderIds) throws WorkflowException {
        // validate documentHeaderIdList and contents
        if (documentHeaderIds == null) {
            throw new IllegalArgumentException("invalid (null) documentHeaderId list");
        }
        int index = 0;
        for (String documentHeaderId : documentHeaderIds) {
            if (StringUtils.isBlank(documentHeaderId)) {
                throw new IllegalArgumentException("invalid (blank) documentHeaderId at list index " + index);
            }
            index++;
        }

        boolean internalUserSession = false;
        try {
            // KFSMI-2543 - allowed method to run without a user session so it can be used by workflow processes
            if (GlobalVariables.getUserSession() == null) {
                internalUserSession = true;
                GlobalVariables.setUserSession(new UserSession(KRADConstants.SYSTEM_USER));
                GlobalVariables.clear();
            }

            // retrieve all documents that match the document header ids
            List<? extends Document> rawDocuments =
                getDocumentDao().findByDocumentHeaderIds(documentClass, documentHeaderIds);

            // post-process them
            List<Document> documents = new ArrayList<>();
            for (Document document : rawDocuments) {
                WorkflowDocument workflowDocument = getWorkflowDocumentService().loadWorkflowDocument(
                        document.getDocumentNumber(), GlobalVariables.getUserSession().getPerson());

                documents.add(postProcessDocument(document.getDocumentNumber(), workflowDocument, document));
            }
            return documents;
        } finally {
            // if a user session was established for this call, clear it our
            if (internalUserSession) {
                GlobalVariables.clear();
                GlobalVariables.setUserSession(null);
            }
        }
    }

    @Override
    public Document validateAndPersistDocument(Document document, KualiDocumentEvent event) throws ValidationException {
        if (document == null) {
            LOG.error("document passed to validateAndPersist was null");
            throw new IllegalArgumentException("invalid (null) document");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("validating and preparing to persist document " + document.getDocumentNumber());
        }

        document.validateBusinessRules(event);
        document.prepareForSave(event);

        // save the document
        Document savedDocument;
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("storing document " + document.getDocumentNumber());
            }
            savedDocument = getDocumentDao().save(document);
        } catch (OptimisticLockingFailureException e) {
            LOG.error("exception encountered on store of document " + e.getMessage());
            throw e;
        }

        boolean notesSaved = saveDocumentNotes(document);
        if (!notesSaved) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Notes not saved during validateAndPersistDocument, likely means that note save needs to " +
                        "be deferred because note target is not ready.");
            }
        }

        savedDocument.postProcessSave(event);

        return savedDocument;
    }

    /**
     * Sets the title and app document id in the flex document
     *
     * @param document
     * @throws org.kuali.kfs.kew.api.exception.WorkflowException
     */
    @Override
    public void prepareWorkflowDocument(Document document) {
        // populate document content so searchable attributes will be indexed properly
        document.populateDocumentForRouting();

        // make sure we push the document title into the workflowDocument
        populateDocumentTitle(document);

        // make sure we push the application document id into the workflowDocument
        populateApplicationDocumentId(document);
    }

    /**
     * This method will grab the generated document title from the document and add it to the workflowDocument so that
     * it gets pushed into workflow when routed.
     *
     * @param document
     * @throws org.kuali.kfs.kew.api.exception.WorkflowException
     */
    private void populateDocumentTitle(Document document) {
        String documentTitle = document.getDocumentTitle();
        if (StringUtils.isNotBlank(documentTitle)) {
            document.getDocumentHeader().getWorkflowDocument().setTitle(documentTitle);
        }
    }

    /**
     * This method will grab the organization document number from the document and add it to the workflowDocument so
     * that it gets pushed
     * into workflow when routed.
     *
     * @param document
     */
    private void populateApplicationDocumentId(Document document) {
        String organizationDocumentNumber = document.getDocumentHeader().getOrganizationDocumentNumber();
        if (StringUtils.isNotBlank(organizationDocumentNumber)) {
            document.getDocumentHeader().getWorkflowDocument().setApplicationDocumentId(organizationDocumentNumber);
        }
    }

    /**
     * This is to allow for updates of document statuses and other related requirements for updates outside of the
     * initial save and route
     */
    @Override
    public Document updateDocument(Document document) {
        checkForNulls(document);
        return getDocumentDao().save(document);
    }

    @Override
    public Note createNoteFromDocument(Document document, String text) {
        Note note = new Note();

        note.setNotePostedTimestamp(getDateTimeService().getCurrentTimestamp());
        note.setVersionNumber(1L);
        note.setNoteText(text);
        note.setNoteTypeCode(document.getNoteType().getCode());

        PersistableBusinessObject bo = document.getNoteTarget();
        // TODO gah! this is awful
        Person kualiUser = GlobalVariables.getUserSession().getPerson();
        if (kualiUser == null) {
            throw new IllegalStateException("Current UserSession has a null Person.");
        }
        return bo == null ? null : getNoteService().createNote(note, bo, kualiUser.getPrincipalId());
    }

    @Override
    public boolean saveDocumentNotes(Document document) {
        if (isNoteTargetReady(document)) {
            List<Note> notes = document.getNotes();
            for (Note note : document.getNotes()) {
                linkNoteRemoteObjectId(note, document.getNoteTarget());
            }
            getNoteService().saveNoteList(notes);
            return true;
        }
        return false;
    }

    @Override
    public void sendNoteRouteNotification(Document document, Note note, Person sender) throws WorkflowException {
        AdHocRouteRecipient routeRecipient = note.getAdHocRouteRecipient();

        // build notification request
        Person requestedUser = this.getPersonService().getPersonByPrincipalName(routeRecipient.getId());
        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String requestedName = requestedUser.getFirstName() + " " + requestedUser.getLastName();

        String notificationText = kualiConfigurationService.getPropertyValueAsString(
                MESSAGE_NOTE_NOTIFICATION_ANNOTATION);
        if (StringUtils.isBlank(notificationText)) {
            throw new RuntimeException("No annotation message found for note notification. Message needs added to " +
                    "application resources with key:" + MESSAGE_NOTE_NOTIFICATION_ANNOTATION);
        }
        notificationText = MessageFormat.format(notificationText, senderName, requestedName, note.getNoteText());

        List<AdHocRouteRecipient> routeRecipients = new ArrayList<>();
        routeRecipients.add(routeRecipient);

        workflowDocumentService.sendWorkflowNotification(document.getDocumentHeader().getWorkflowDocument(),
                notificationText, routeRecipients, KRADConstants.NOTE_WORKFLOW_NOTIFICATION_REQUEST_LABEL);

        // clear recipient allowing an notification to be sent to another person
        note.setAdHocRouteRecipient(new AdHocRoutePerson());
    }

    /**
     * Determines if the given document's note target is ready for notes to be attached and persisted against it. This
     * method verifies that the document's note target is non-null as well as checking that it has a non-empty object
     * id.
     *
     * @param document the document on which to check for note target readiness
     * @return true if the note target is ready, false otherwise
     */
    protected boolean isNoteTargetReady(Document document) {
        //special case for disapproved documents
        if (document.getDocumentHeader().getWorkflowDocument().isDisapproved()) {
            return true;
        }
        PersistableBusinessObject noteTarget = document.getNoteTarget();
        return noteTarget != null && StringUtils.isNotBlank(noteTarget.getObjectId());
    }

    private void linkNoteRemoteObjectId(Note note, PersistableBusinessObject noteTarget) {
        String objectId = noteTarget.getObjectId();
        if (StringUtils.isBlank(objectId)) {
            throw new IllegalStateException("Attempted to link a Note with a PersistableBusinessObject with no " +
                    "object id");
        }
        note.setRemoteObjectIdentifier(noteTarget.getObjectId());
    }

    @Override
    public void sendAdHocRequests(Document document, String annotation, List<AdHocRouteRecipient> adHocRecipients)
            throws WorkflowException {
        prepareWorkflowDocument(document);
        getWorkflowDocumentService().sendWorkflowNotification(document.getDocumentHeader().getWorkflowDocument(),
                annotation, adHocRecipients);
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
                document.getDocumentHeader().getWorkflowDocument());
        removeAdHocPersonsAndWorkgroups(document);
    }

    private void removeAdHocPersonsAndWorkgroups(Document document) {
        List<AdHocRoutePerson> adHocRoutePersons = new ArrayList<>();
        List<AdHocRouteWorkgroup> adHocRouteWorkgroups = new ArrayList<>();
        getBusinessObjectService().delete(document.getAdHocRoutePersons());
        getBusinessObjectService().delete(document.getAdHocRouteWorkgroups());
        document.setAdHocRoutePersons(adHocRoutePersons);
        document.setAdHocRouteWorkgroups(adHocRouteWorkgroups);
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    protected DateTimeService getDateTimeService() {
        if (this.dateTimeService == null) {
            this.dateTimeService = CoreApiServiceLocator.getDateTimeService();
        }
        return this.dateTimeService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    protected NoteService getNoteService() {
        if (this.noteService == null) {
            this.noteService = KRADServiceLocator.getNoteService();
        }
        return this.noteService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (this.businessObjectService == null) {
            this.businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return this.businessObjectService;
    }

    public void setWorkflowDocumentService(WorkflowDocumentService workflowDocumentService) {
        this.workflowDocumentService = workflowDocumentService;
    }

    protected WorkflowDocumentService getWorkflowDocumentService() {
        if (this.workflowDocumentService == null) {
            this.workflowDocumentService = KewApiServiceLocator.getWorkflowDocumentService();
        }
        return this.workflowDocumentService;
    }

    public void setDocumentDao(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    protected DocumentDao getDocumentDao() {
        if (this.documentDao == null) {
            this.documentDao = KRADServiceLocatorInternal.getDocumentDao();
        }
        return documentDao;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    protected DataDictionaryService getDataDictionaryService() {
        if (this.dataDictionaryService == null) {
            this.dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }
        return this.dataDictionaryService;
    }

    public void setDocumentHeaderService(DocumentHeaderService documentHeaderService) {
        this.documentHeaderService = documentHeaderService;
    }

    protected DocumentHeaderService getDocumentHeaderService() {
        if (this.documentHeaderService == null) {
            this.documentHeaderService = KRADServiceLocatorWeb.getDocumentHeaderService();
        }
        return this.documentHeaderService;
    }

    protected DocumentDictionaryService getDocumentDictionaryService() {
        if (documentDictionaryService == null) {
            documentDictionaryService = KRADServiceLocatorWeb.getDocumentDictionaryService();
        }
        return documentDictionaryService;
    }

    public void setDocumentDictionaryService(DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }

    public PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }

    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

}
