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
package org.kuali.kfs.kns.web.struts.action;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.OptimisticLockException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.api.action.WorkflowDocumentActionsService;
import org.kuali.kfs.kew.api.document.WorkflowDocumentService;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.group.GroupService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizerBase;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.question.Question;
import org.kuali.kfs.kns.question.RecallQuestion;
import org.kuali.kfs.kns.rule.PromptBeforeValidation;
import org.kuali.kfs.kns.rule.event.PromptBeforeValidationEvent;
import org.kuali.kfs.kns.service.BusinessObjectAuthorizationService;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.BlankFormFile;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteRecipient;
import org.kuali.kfs.krad.bo.AdHocRouteWorkgroup;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.DocumentPresentationController;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.exception.DocumentAuthorizationException;
import org.kuali.kfs.krad.exception.UnknownDocumentIdException;
import org.kuali.kfs.krad.rules.rule.event.AddAdHocRoutePersonEvent;
import org.kuali.kfs.krad.rules.rule.event.AddNoteEvent;
import org.kuali.kfs.krad.rules.rule.event.RouteDocumentEvent;
import org.kuali.kfs.krad.rules.rule.event.SendAdHocRequestsEvent;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.service.impl.AdHocRoutingService;
import org.kuali.kfs.krad.service.impl.SuperUserService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.NoteType;
import org.kuali.kfs.krad.util.SessionTicket;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.springmodules.orm.ojb.OjbOperationException;

import edu.cornell.kfs.krad.service.BlackListAttachmentService;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles all of the document handling related actions in terms of passing them from here at a central
 * point to the distributed transactions that actually implement document handling.
 */
/**
 * 
 * CU customization: fix issue introduced with 11/17/2021.
 *
 * CU customization: Added blacklist attachment processing so that failure-to-create-attachment
 *                   can be treated as a validation failure with a user friendly error message.
 * 
 * CU Customization: Increased the visibility of the MESSAGE_RELOADED key constant to protected.
 * 
 * CU Customization: Increased the visibility of the internal ReasonPrompt class and its related fields/methods/etc.
 */
public class KualiDocumentActionBase extends KualiAction {

    private static final Logger LOG = LogManager.getLogger();
    // blanket approve should not be allowed when adhoc route for completion request is newly added
    private static final String ERROR_ADHOC_COMPLETE_BLANKET_APPROVE_NOT_ALLOWED =
            "error.adhoc.complete.blanket.approve.not.allowed";
    private static final String ERROR_DOCUMENT_RECALL_REASON_REQUIRED = "error.document.recall.reasonRequired";
    private static final String ERROR_INVALID_ADHOC_WORKGROUP_NAMESPACECODE =
            "error.adhoc.invalid.workgroupNamespaceCode";
    private static final String ERROR_SEND_NOTE_NOTIFICATION_RECIPIENT = "error.send.note.notification.recipient";
    private static final String ERROR_SEND_NOTE_NOTIFICATION_DOCSTATUS = "error.send.note.notification.docStatus";
    private static final String ERROR_UPLOADFILE_EMPTY = "error.uploadFile.empty";
    private static final String MESSAGE_DISAPPROVAL_NOTE_TEXT_INTRO = "message.disapprove.noteTextIntro";
    private static final String MESSAGE_RECALL_NOTE_TEXT_INTRO = "message.recall.noteTextIntro";
    // CU Customization: Increased visibility of this constant to protected.
    protected static final String MESSAGE_RELOADED = "message.document.reloaded";
    private static final String MESSAGE_ROUTE_APPROVED = "message.route.approved";
    private static final String MESSAGE_ROUTE_DISAPPROVED = "message.route.disapproved";
    private static final String MESSAGE_ROUTE_ACKNOWLEDGED = "message.route.acknowledged";
    private static final String MESSAGE_ROUTE_FYIED = "message.route.fyied";
    private static final String MESSAGE_SEND_AD_HOC_REQUESTS_SUCCESSFUL = "message.sendAdHocRequests.successful";
    private static final String MESSAGE_SEND_NOTE_NOTIFICATION_SUCCESSFUL =
            "message.send.note.notification.successful";
    private static final String QUESTION_DISAPPROVE_DOCUMENT = "document.question.disapprove.text";
    private static final String QUESTION_RECALL_DOCUMENT = "document.question.recall.text";
    private static final String QUESTION_SENSITIVE_DATA_DOCUMENT = "document.question.sensitiveData.text";

    // COMMAND constants which cause docHandler to load an existing document instead of creating a new one
    protected static final String[] DOCUMENT_LOAD_COMMANDS = {
        KewApiConstants.ACTIONLIST_COMMAND,
        KewApiConstants.DOCSEARCH_COMMAND,
        KewApiConstants.SUPERUSER_COMMAND,
        KewApiConstants.HELPDESK_ACTIONLIST_COMMAND};

    private DataDictionaryService dataDictionaryService;
    private PersonService personService;
    private SuperUserService superUserService;
    private DocumentDictionaryService documentDictionaryService;
    private DocumentHelperService documentHelperService;
    private DocumentService documentService;
    private ConfigurationService kualiConfigurationService;
    private ParameterService parameterService;
    private KualiRuleService kualiRuleService;
    private GroupService groupService;
    private AttachmentService attachmentService;
    private NoteService noteService;
    private BusinessObjectAuthorizationService businessObjectAuthorizationService;
    private BusinessObjectService businessObjectService;
    private BusinessObjectMetaDataService businessObjectMetaDataService;
    private AdHocRoutingService adHocRoutingService;
    private WorkflowDocumentService workflowDocumentService;
    
    //CU customization: Added blacklist attachment processing
    private BlackListAttachmentService blackListAttachmentService;

    @Override
    protected void checkAuthorization(final ActionForm form, final String methodToCall) throws AuthorizationException {
        if (!(form instanceof KualiDocumentFormBase)) {
            super.checkAuthorization(form, methodToCall);
        }
    }

    /**
     * Entry point to all actions.
     */
    @Override
    public ActionForward execute(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        ActionForward returnForward = mapping.findForward(KFSConstants.MAPPING_BASIC);

        if (form instanceof KualiDocumentFormBase) {
            ((KualiDocumentFormBase) form).setLastActionTaken(null);
            final String methodToCall = findMethodToCall(form, request);
            if (StringUtils.isNotBlank(methodToCall)) {
                ((KualiDocumentFormBase) form).setLastActionTaken(methodToCall);
            }
        }

        // if found methodToCall, pass control to that method
        try {
            returnForward = super.execute(mapping, form, request, response);
        } catch (final OjbOperationException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OptimisticLockException) {
                final OptimisticLockException ole = (OptimisticLockException) cause;
                GlobalVariables.getMessageMap().putError(KRADConstants.DOCUMENT_ERRORS,
                        KFSKeyConstants.ERROR_OPTIMISTIC_LOCK);
                LOG.info("{}", () -> createOjbOptimisticLockExceptionLogMsg(ole));
            } else {
                // if exceptions are from 'save'
                throw e;
            }
        } finally {
            if (form instanceof KualiDocumentFormBase) {
                ((KualiDocumentFormBase) form).setMessageMapFromPreviousRequest(GlobalVariables.getMessageMap());
            }
        }

        if (form instanceof KualiDocumentFormBase
            && ((KualiDocumentFormBase) form).isHasWorkflowDocument()) {
            final KualiDocumentFormBase formBase = (KualiDocumentFormBase) form;
            final Document document = formBase.getDocument();

            final WorkflowDocument workflowDocument = formBase.getDocument().getDocumentHeader().getWorkflowDocument();
            formBase.populateHeaderFields(workflowDocument);
            formBase.setDocId(document.getDocumentNumber());

            // populates authorization-related fields in KualiDocumentFormBase instances, which are derived from
            // information which is contained in the form but which may be unavailable until this point
            populateAuthorizationFields(formBase);
            populateAdHocActionRequestCodes(formBase);

            //set the formBase into userSession if the document is a session document
            final UserSession userSession = (UserSession) request.getSession().getAttribute(KRADConstants.USER_SESSION_KEY);

            if (WebUtils.isDocumentSession(document, formBase)) {
                if (StringUtils.isBlank(formBase.getFormKey())
                        || userSession.retrieveObject(formBase.getFormKey()) == null) {
                    // generate doc form key here if it does not exist
                    final String formKey = GlobalVariables.getUserSession().addObjectWithGeneratedKey(form);
                    formBase.setFormKey(formKey);
                }
            }

            // below used by KualiHttpSessionListener to handle lock expiration
            request.getSession().setAttribute(KRADConstants.DOCUMENT_HTTP_SESSION_KEY, document.getDocumentNumber());
            // set returnToActionList flag, if needed
            if ("displayActionListView".equals(formBase.getCommand())) {
                formBase.setReturnToActionList(true);
            }

            final String attachmentEnabled =
                getKualiConfigurationService().getPropertyValueAsString(KRADConstants.NOTE_ATTACHMENT_ENABLED);
            // Override the document entry
            if (attachmentEnabled != null) {
                // This is a hack for KULRICE-1602 since the document entry is modified by a global configuration that
                // overrides the document templates without some sort of rules or control
                final DocumentEntry entry = getDocumentDictionaryService().getDocumentEntry(document.getClass().getName());
                entry.setAllowsNoteAttachments(Boolean.parseBoolean(attachmentEnabled));
            }
            // the request attribute will be used in KualiRequestProcess#processActionPerform
            if (exitingDocument()) {
                request.setAttribute(KRADConstants.EXITING_DOCUMENT, Boolean.TRUE);
            }

            // Pull in the pending action requests for the document and attach them to the form
            final List<ActionRequest> actionRequests = KewApiServiceLocator.getWorkflowDocumentService()
                    .getPendingActionRequests(formBase.getDocId());
            formBase.setActionRequests(actionRequests);

            if (LOG.isDebugEnabled()) {
                final StringBuilder message = new StringBuilder();

                message.append("Running action ");
                message.append(((KualiDocumentFormBase) form).getMethodToCall());
                if (StringUtils.isNotBlank(((KualiDocumentFormBase) form).getDocId())) {
                    message.append(" on document ");
                    message.append(((KualiDocumentFormBase) form).getDocId());
                    if (((KualiDocumentFormBase) form).isHasWorkflowDocument()) {
                        message.append(" for document at route level(s) ");
                        message.append(StringUtils.join(((KualiDocumentFormBase) form).getWorkflowDocument()
                                .getCurrentNodeNames(), ", "));
                    }
                }

                LOG.debug(message.toString());
            }

            String documentId = null;
            if (StringUtils.isNotEmpty(formBase.getDocumentId())) {
                documentId = formBase.getDocumentId();
            } else if (StringUtils.isNotEmpty(formBase.getDocId())) {
                documentId = formBase.getDocId();
            } else {
                throw new WorkflowRuntimeException("No paramater provided to fetch document");
            }

            final DocumentRouteHeaderValue routeHeader = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);

            fixActionRequestsPositions(routeHeader);
            populateRouteLogFormActionRequests(formBase, routeHeader);

            formBase.setLookFuture(routeHeader.getDocumentType().getLookIntoFuturePolicy().getPolicyValue());

            if (formBase.isShowFuture()) {
                try {
                    populateRouteLogFutureRequests(formBase, routeHeader);
                } catch (final Exception e) {
                    final String errorMsg = "Unable to determine Future Action Requests";
                    LOG.info(errorMsg, e);
                    formBase.setShowFutureError(errorMsg);
                }
            }
            request.setAttribute("routeHeader", routeHeader);
        }

        return returnForward;
    }

    /**
     * This method may be used to funnel all document handling through, we could do useful things like log and record
     * various openings and status Additionally it may be nice to have a single dispatcher that can know how to
     * dispatch to a redirect url for document specific handling but we may not need that as all we should need is the
     * document to be able to load itself based on document id and then which actionforward or redirect is pertinent
     * for the document type.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward docHandler(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final String command = kualiDocumentFormBase.getCommand();

        if (kualiDocumentFormBase.getDocId() != null
                && getDocumentService().getByDocumentHeaderId(kualiDocumentFormBase.getDocId()) == null) {
            return mapping.findForward(KFSConstants.MAPPING_INITIATED_DOCUMENT_ERROR);
        }
        // in all of the following cases we want to load the document
        if (ArrayUtils.contains(DOCUMENT_LOAD_COMMANDS, command) && kualiDocumentFormBase.getDocId() != null) {
            loadDocument(kualiDocumentFormBase);
        } else if (KewApiConstants.INITIATE_COMMAND.equals(command)) {
            createDocument(kualiDocumentFormBase);
        } else {
            LOG.error("docHandler called with invalid parameters");
            throw new IllegalArgumentException("docHandler called with invalid parameters");
        }

        // attach any extra JS from the data dictionary
        LOG.debug(
                "kualiDocumentFormBase.getAdditionalScriptFiles(): {}",
                kualiDocumentFormBase::getAdditionalScriptFiles
        );
        if (kualiDocumentFormBase.getAdditionalScriptFiles().isEmpty()) {
            final DocumentEntry docEntry = getDocumentDictionaryService().getDocumentEntry(
                    kualiDocumentFormBase.getDocument().getDocumentHeader().getWorkflowDocument()
                            .getDocumentTypeName());
            kualiDocumentFormBase.getAdditionalScriptFiles().addAll(docEntry.getWebScriptFiles());
        }
        if (KewApiConstants.SUPERUSER_COMMAND.equalsIgnoreCase(command)) {
            kualiDocumentFormBase.setSuppressAllButtons(true);
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method loads the document by its provided document header id. This has been abstracted out so that it can
     * be overridden in children if the need arises.
     *
     * @param kualiDocumentFormBase
     */
    protected void loadDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        final String docId = kualiDocumentFormBase.getDocId();
        final Document doc;
        doc = getDocumentService().getByDocumentHeaderId(docId);
        if (doc == null) {
            throw new UnknownDocumentIdException("Document no longer exists.  It may have been cancelled before " +
                    "being saved.");
        }
        final WorkflowDocument workflowDocument = doc.getDocumentHeader().getWorkflowDocument();
        if (!getDocumentHelperService().getDocumentAuthorizer(doc).canOpen(doc,
                GlobalVariables.getUserSession().getPerson())) {
            throw buildAuthorizationException("open", doc);
        }
        // re-retrieve the document using the current user's session - remove the system user from the
        // WorkflowDocument object
        if (workflowDocument != doc.getDocumentHeader().getWorkflowDocument()) {
            LOG.warn("Workflow document changed via canOpen check");
            doc.getDocumentHeader().setWorkflowDocument(workflowDocument);
        }
        kualiDocumentFormBase.setDocument(doc);
        final WorkflowDocument workflowDoc = doc.getDocumentHeader().getWorkflowDocument();
        kualiDocumentFormBase.setDocTypeName(workflowDoc.getDocumentTypeName());

        // KualiDocumentFormBase.populate() needs this updated in the session
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), workflowDoc);
    }

    /**
     * This method creates a new document of the type specified by the docTypeName property of the given form. This
     * has been abstracted out so that it can be overridden in children if the need arises.
     *
     * @param kualiDocumentFormBase
     */
    protected void createDocument(final KualiDocumentFormBase kualiDocumentFormBase) {
        final Document doc = getDocumentService().getNewDocument(kualiDocumentFormBase.getDocTypeName());
        UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(),
            doc.getDocumentHeader().getWorkflowDocument());

        kualiDocumentFormBase.setDocument(doc);
        kualiDocumentFormBase.setDocTypeName(doc.getDocumentHeader().getWorkflowDocument().getDocumentTypeName());
    }

    /**
     * This method will insert the new ad hoc person from the from into the list of ad hoc person recipients, put a
     * new new record in place and return like normal.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward insertAdHocRoutePerson(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();
        // check authorization for adding ad hoc route person
        final DocumentAuthorizer documentAuthorizer = getDocumentHelperService().getDocumentAuthorizer(document);
        if (!documentAuthorizer.canSendAdHocRequests(document,
                kualiDocumentFormBase.getNewAdHocRoutePerson().getActionRequested(),
                GlobalVariables.getUserSession().getPerson())) {
            throw buildAuthorizationException("ad-hoc route", document);
        }

        // check business rules
        final boolean rulePassed = getKualiRuleService().applyRules(new AddAdHocRoutePersonEvent(document,
                kualiDocumentFormBase.getNewAdHocRoutePerson()));

        // if the rule evaluation passed, let's add the ad hoc route person
        if (rulePassed) {
            kualiDocumentFormBase.getNewAdHocRoutePerson().setId(kualiDocumentFormBase.getNewAdHocRoutePerson().getId());
            kualiDocumentFormBase.getAdHocRoutePersons().add(kualiDocumentFormBase.getNewAdHocRoutePerson());
            final AdHocRoutePerson person = new AdHocRoutePerson();
            kualiDocumentFormBase.setNewAdHocRoutePerson(person);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will delete one of the ad hoc persons from the list of ad hoc persons to route to based on the line
     * number of the delete button that was clicked. then it will return to the form.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteAdHocRoutePerson(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        kualiDocumentFormBase.getAdHocRoutePersons().remove(getLineToDelete(request));
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will insert the new ad hoc workgroup into the list of ad hoc workgroup recipients put a new record
     * in place and then return like normal.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward insertAdHocRouteWorkgroup(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();

        final var currentUser = GlobalVariables.getUserSession().getPerson();
        final var newWorkgroup = kualiDocumentFormBase.getNewAdHocRouteWorkgroup();
        if (!getAdHocRoutingService().canSendAdHocRequest(document, newWorkgroup, currentUser)) {
            throw buildAuthorizationException("ad-hoc route", document);
        }

        final var addResult = getAdHocRoutingService().addAdHocRouteWorkgroup(document, newWorkgroup, currentUser);
        if (addResult) {
            // the addition was successfully added. We add a new workgroup as a placeholder for the form
            kualiDocumentFormBase.setNewAdHocRouteWorkgroup(new AdHocRouteWorkgroup());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will delete one of the ad hoc workgroups from the list of ad hoc workgroups to route to based on
     * the line number of the delete button that was clicked. then it will return
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteAdHocRouteWorkgroup(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

        kualiDocumentFormBase.getAdHocRouteWorkgroups().remove(getLineToDelete(request));
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward sendAdHocRequests(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();

        final boolean rulePassed = getKualiRuleService().applyRules(new SendAdHocRequestsEvent(document));

        if (rulePassed) {
            getDocumentService().sendAdHocRequests(document, kualiDocumentFormBase.getAnnotation(),
                    combineAdHocRecipients(kualiDocumentFormBase));
            KNSGlobalVariables.getMessageList().add(MESSAGE_SEND_AD_HOC_REQUESTS_SUCCESSFUL);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * This method will reload the document.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward reload(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();

        // prepare for the reload action - set doc id and command
        kualiDocumentFormBase.setDocId(document.getDocumentNumber());
        kualiDocumentFormBase.setCommand(DOCUMENT_LOAD_COMMANDS[1]);

        // forward off to the doc handler
        final ActionForward actionForward = docHandler(mapping, form, request, response);
        KNSGlobalVariables.getMessageList().add(MESSAGE_RELOADED);

        return actionForward;
    }

    /**
     * This method will save the document, which will then be available via the action list for the person who saved
     * the document.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward save(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);
        //get any possible changes to to adHocWorkgroups
        refreshAdHocRoutingWorkgroupLookups(request, kualiDocumentFormBase);
        final Document document = kualiDocumentFormBase.getDocument();

        final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.DOCUMENT_EXPLANATION, document.getDocumentHeader().getExplanation(), "save", "");
        if (forward != null) {
            return forward;
        }

        // save in workflow
        getDocumentService().saveDocument(document);

        KNSGlobalVariables.getMessageList().add(MESSAGE_SAVED);
        kualiDocumentFormBase.setAnnotation("");

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Checks if the given value matches patterns that indicate sensitive data and if configured to give a warning for
     * sensitive data will prompt the user to continue
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param fieldName  name of field with value being checked
     * @param fieldValue value to check for sensitive data
     * @param caller     method that should be called back from question
     * @param context    additional context that needs to be passed back with the question response
     * @return ActionForward which contains the question forward, or basic forward if user select no to prompt,
     *         otherwise will return null to indicate processing should continue
     * @throws Exception
     */
    protected ActionForward checkAndWarnAboutSensitiveData(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response, final String fieldName, final String fieldValue,
            final String caller, final String context)
        throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();

        final boolean containsSensitiveData = KRADUtils.containsSensitiveDataPatternMatch(fieldValue);

        // check if warning is configured in which case we will prompt, or if not business rules will thrown an error
        final boolean warnForSensitiveData = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsBoolean(
            KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.ALL_COMPONENT,
            KRADConstants.SystemGroupParameterNames.SENSITIVE_DATA_PATTERNS_WARNING_IND);

        // determine if the question has been asked yet
        final Map<String, String> ticketContext = new HashMap<>();
        ticketContext.put(KRADPropertyConstants.DOCUMENT_NUMBER, document.getDocumentNumber());
        ticketContext.put(KRADConstants.CALLING_METHOD, caller);
        ticketContext.put(KRADPropertyConstants.NAME, fieldName);

        final boolean questionAsked = GlobalVariables.getUserSession().hasMatchingSessionTicket(
            KRADConstants.SENSITIVE_DATA_QUESTION_SESSION_TICKET, ticketContext);

        // start in logic for confirming the sensitive data
        if (containsSensitiveData && warnForSensitiveData && !questionAsked) {
            final Object question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
            if (!KRADConstants.DOCUMENT_SENSITIVE_DATA_QUESTION.equals(question)) {

                // question hasn't been asked, prompt to continue
                return performQuestionWithoutInput(mapping, form, request, response,
                    KRADConstants.DOCUMENT_SENSITIVE_DATA_QUESTION, getKualiConfigurationService()
                        .getPropertyValueAsString(QUESTION_SENSITIVE_DATA_DOCUMENT),
                    KRADConstants.CONFIRMATION_QUESTION, caller, context);
            }

            final Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
            if (KRADConstants.DOCUMENT_SENSITIVE_DATA_QUESTION.equals(question)) {
                // if no button clicked just reload the doc
                if (ConfirmationQuestion.NO.equals(buttonClicked)) {

                    return mapping.findForward(KFSConstants.MAPPING_BASIC);
                }

                // answered yes, create session ticket so we not to ask question again if there are further question
                // requests
                final SessionTicket ticket = new SessionTicket(KRADConstants.SENSITIVE_DATA_QUESTION_SESSION_TICKET);
                ticket.setTicketContext(ticketContext);
                GlobalVariables.getUserSession().putSessionTicket(ticket);
            }
        }

        // return null to indicate processing should continue (no redirect)
        return null;
    }

    /**
     * route the document using the document service
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward route(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);

        kualiDocumentFormBase.setDerivedValuesOnForm(request);
        final ActionForward preRulesForward = promptBeforeValidation(mapping, form, request, response);
        if (preRulesForward != null) {
            return preRulesForward;
        }

        final Document document = kualiDocumentFormBase.getDocument();

        final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.DOCUMENT_EXPLANATION, document.getDocumentHeader().getExplanation(), "route", "");
        if (forward != null) {
            return forward;
        }

        getDocumentService().routeDocument(document, kualiDocumentFormBase.getAnnotation(),
                combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(KFSKeyConstants.MESSAGE_ROUTE_SUCCESSFUL);
        kualiDocumentFormBase.setAnnotation("");

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Calls the document service to blanket approve the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward blanketApprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);

        // KULRICE-7864: blanket approve should not be allowed when adhoc route for completion request is newly added
        final boolean hasPendingAdhocForCompletion = hasPendingAdhocForCompletion(kualiDocumentFormBase);
        if (hasPendingAdhocForCompletion) {
            GlobalVariables.getMessageMap().putError(KRADConstants.NEW_AD_HOC_ROUTE_WORKGROUP_PROPERTY_NAME,
                    ERROR_ADHOC_COMPLETE_BLANKET_APPROVE_NOT_ALLOWED);
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        kualiDocumentFormBase.setDerivedValuesOnForm(request);
        final ActionForward preRulesForward = promptBeforeValidation(mapping, form, request, response);
        if (preRulesForward != null) {
            return preRulesForward;
        }

        final Document document = kualiDocumentFormBase.getDocument();

        final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.DOCUMENT_EXPLANATION, document.getDocumentHeader().getExplanation(), "blanketApprove", "");
        if (forward != null) {
            return forward;
        }

        getDocumentService().blanketApproveDocument(document, kualiDocumentFormBase.getAnnotation(),
                combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(MESSAGE_ROUTE_APPROVED);
        kualiDocumentFormBase.setAnnotation("");
        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     * Calls the document service to approve the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward approve(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);

        kualiDocumentFormBase.setDerivedValuesOnForm(request);
        final ActionForward preRulesForward = promptBeforeValidation(mapping, form, request, response);
        if (preRulesForward != null) {
            return preRulesForward;
        }

        final Document document = kualiDocumentFormBase.getDocument();

        final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.DOCUMENT_EXPLANATION, document.getDocumentHeader().getExplanation(), "approve", "");
        if (forward != null) {
            return forward;
        }

        getDocumentService().approveDocument(document, kualiDocumentFormBase.getAnnotation(),
                combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(MESSAGE_ROUTE_APPROVED);
        kualiDocumentFormBase.setAnnotation("");
        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     * Calls the document service to disapprove the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward disapprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ReasonPrompt prompt = new ReasonPrompt(KRADConstants.DOCUMENT_DISAPPROVE_QUESTION,
                QUESTION_DISAPPROVE_DOCUMENT, KRADConstants.CONFIRMATION_QUESTION,
                KFSKeyConstants.ERROR_DOCUMENT_DISAPPROVE_REASON_REQUIRED, KRADConstants.MAPPING_DISAPPROVE,
                ConfirmationQuestion.NO, MESSAGE_DISAPPROVAL_NOTE_TEXT_INTRO);
        final ReasonPrompt.Response resp = prompt.ask(mapping, form, request, response);

        if (resp.forward != null) {
            return resp.forward;
        }

        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doDisapprove(kualiDocumentFormBase, request, resp.reason);
        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     *
     * @param form
     * @param request
     * @param reason
     *
     * @throws Exception
     */
    protected void doDisapprove(final KualiDocumentFormBase form, final HttpServletRequest request, final String reason) throws Exception {
        doProcessingAfterPost(form, request);
        getDocumentService().disapproveDocument(form.getDocument(), reason);
        KNSGlobalVariables.getMessageList().add(MESSAGE_ROUTE_DISAPPROVED);
        form.setAnnotation("");
    }

    /**
     * Calls the document service to cancel the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward cancel(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final Object question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
        // this should probably be moved into a private instance variable logic for cancel question
        if (question == null) {
            // ask question if not already asked
            return performQuestionWithoutInput(mapping, form, request, response,
                    KRADConstants.DOCUMENT_CANCEL_QUESTION, getKualiConfigurationService().getPropertyValueAsString(
                "document.question.cancel.text"), KRADConstants.CONFIRMATION_QUESTION,
                    KRADConstants.MAPPING_CANCEL, "");
        } else {
            final Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
            if (KRADConstants.DOCUMENT_CANCEL_QUESTION.equals(question)
                && ConfirmationQuestion.NO.equals(buttonClicked)) {
                // if no button clicked just reload the doc
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
            // else go to cancel logic below
        }

        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);
        // KULRICE-4447 Call cancelDocument() only if the document exists
        if (getDocumentService().documentExists(kualiDocumentFormBase.getDocId())) {
            getDocumentService().cancelDocument(kualiDocumentFormBase.getDocument(),
                    kualiDocumentFormBase.getAnnotation());
        }

        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     * Calls the document service to disapprove the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward recall(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final ReasonPrompt prompt = new ReasonPrompt(KRADConstants.DOCUMENT_RECALL_QUESTION,
                QUESTION_RECALL_DOCUMENT, KRADConstants.RECALL_QUESTION,
                ERROR_DOCUMENT_RECALL_REASON_REQUIRED, KRADConstants.MAPPING_RECALL, null,
                MESSAGE_RECALL_NOTE_TEXT_INTRO);
        final ReasonPrompt.Response resp = prompt.ask(mapping, form, request, response);

        if (resp.forward != null) {
            return resp.forward;
        }

        final boolean cancel = !(KRADConstants.DOCUMENT_RECALL_QUESTION.equals(resp.question)
                                 && RecallQuestion.RECALL_TO_ACTIONLIST.equals(resp.button));

        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);
        getDocumentService().recallDocument(kualiDocumentFormBase.getDocument(), resp.reason, cancel);

        // fix an issue where sometimes recalled doc hasn't been refreshed (which causes failures)
        reload(mapping, form, request, response);

        // just return to doc view
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Close the document and take the user back to the index; only after asking the user if they want to save the
     * document first. Only users who have the "canSave()" permission are given this option.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward close(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
        doProcessingAfterPost(docForm, request);
        final Document document = docForm.getDocument();
        // only want to prompt them to save if they already can save
        if (canSave(docForm)) {

            final Object question = getQuestion(request);
            // logic for close question
            if (question == null) {
                // KULRICE-7306: Unconverted Values not carried through during a saveOnClose action.
                // Stash the unconverted values to populate errors if the user elects to save
                saveUnconvertedValuesToSession(request, docForm);

                // ask question if not already asked
                return performQuestionWithoutInput(mapping, form, request, response,
                        KRADConstants.DOCUMENT_SAVE_BEFORE_CLOSE_QUESTION,
                        getKualiConfigurationService().getPropertyValueAsString(
                    KFSKeyConstants.QUESTION_SAVE_BEFORE_CLOSE), KRADConstants.CONFIRMATION_QUESTION,
                        KRADConstants.MAPPING_CLOSE, "");
            } else {
                final Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);

                // KULRICE-7306: Unconverted Values not carried through during a saveOnClose action.
                // Side effecting in that it clears the session attribute that holds the unconverted values.
                final Map<String, Object> unconvertedValues = restoreUnconvertedValuesFromSession(request, docForm);

                if (KRADConstants.DOCUMENT_SAVE_BEFORE_CLOSE_QUESTION.equals(question)
                    && ConfirmationQuestion.YES.equals(buttonClicked)) {
                    // if yes button clicked - save the doc

                    // KULRICE-7306: Unconverted Values not carried through during a saveOnClose action.
                    // If there were values that couldn't be converted, we attempt to populate them so that the
                    // the appropriate errors get set on those fields
                    if (MapUtils.isNotEmpty(unconvertedValues)) {
                        for (final Map.Entry<String, Object> entry : unconvertedValues.entrySet()) {
                            docForm.populateForProperty(entry.getKey(), entry.getValue(), unconvertedValues);
                        }
                    }

                    final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                            KRADPropertyConstants.DOCUMENT_EXPLANATION, document.getDocumentHeader().getExplanation(),
                            "save", "");
                    if (forward != null) {
                        return forward;
                    }

                    getDocumentService().saveDocument(docForm.getDocument());
                }
                // else go to close logic below
            }
        }

        return returnToSender(request, mapping, docForm);
    }

    // stash unconvertedValues in the session
    private void saveUnconvertedValuesToSession(final HttpServletRequest request, final KualiDocumentFormBase docForm) {
        if (MapUtils.isNotEmpty(docForm.getUnconvertedValues())) {
            request.getSession().setAttribute(getUnconvertedValuesSessionAttributeKey(docForm),
                    new HashMap(docForm.getUnconvertedValues()));
        }
    }

    // SIDE EFFECTING: clears out unconverted values from the Session and restores them to the form
    private Map<String, Object> restoreUnconvertedValuesFromSession(
            final HttpServletRequest request,
            final KualiDocumentFormBase docForm) {
        // first restore unconvertedValues and clear out of session
        final Map<String, Object> unconvertedValues =
            (Map<String, Object>) request.getSession().getAttribute(getUnconvertedValuesSessionAttributeKey(docForm));
        if (MapUtils.isNotEmpty(unconvertedValues)) {
            request.getSession().removeAttribute(getUnconvertedValuesSessionAttributeKey(docForm));
            // setting them here just for good measure
            docForm.setUnconvertedValues(unconvertedValues);
        }
        return unconvertedValues;
    }

    // create the key based on docId for stashing/retrieving unconvertedValues in the session
    private String getUnconvertedValuesSessionAttributeKey(final KualiDocumentFormBase docForm) {
        return "preCloseUnconvertedValues." + docForm.getDocId();
    }

    protected boolean canSave(final ActionForm form) {
        final KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
        return docForm.getDocumentActions().containsKey(KRADConstants.KUALI_ACTION_CAN_SAVE);
    }

    protected Object getQuestion(final HttpServletRequest request) {
        return request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
    }

    /**
     * call the document service to clear the fyis
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward fyi(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);
        getDocumentService().clearDocumentFyi(kualiDocumentFormBase.getDocument(), combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(MESSAGE_ROUTE_FYIED);
        kualiDocumentFormBase.setAnnotation("");
        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     * call the document service to acknowledge
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward acknowledge(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);
        getDocumentService().acknowledgeDocument(kualiDocumentFormBase.getDocument(),
                kualiDocumentFormBase.getAnnotation(), combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(MESSAGE_ROUTE_ACKNOWLEDGED);
        kualiDocumentFormBase.setAnnotation("");
        return returnToSender(request, mapping, kualiDocumentFormBase);
    }

    /**
     * redirect to the supervisor functions that exist.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward supervisorFunctions(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final String workflowSuperUserUrl = getKualiConfigurationService().getPropertyValueAsString(
            KFSConstants.APPLICATION_URL_KEY) + "/SuperUser.do?methodToCall=displaySuperUserDocument&documentId=" +
                                            kualiDocumentFormBase.getDocument().getDocumentHeader().getDocumentNumber();
        response.sendRedirect(workflowSuperUserUrl);

        return null;
    }

    /**
     * Convenience method to combine the two lists of ad hoc recipients into one which should be done before calling
     * any of the document service methods that expect a list of ad hoc recipients
     *
     * @param kualiDocumentFormBase
     * @return List
     */
    protected List<AdHocRouteRecipient> combineAdHocRecipients(final KualiDocumentFormBase kualiDocumentFormBase) {
        final List<AdHocRouteRecipient> adHocRecipients = new ArrayList<>();
        adHocRecipients.addAll(kualiDocumentFormBase.getAdHocRoutePersons());
        adHocRecipients.addAll(kualiDocumentFormBase.getAdHocRouteWorkgroups());
        return adHocRecipients;
    }

    /**
     * if the action desires to retain error messages generated by the rules framework for save/submit/etc. validation
     * after returning from a lookup.
     */
    @Override
    public ActionForward refresh(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiForm = (KualiDocumentFormBase) form;
        kualiForm.setDerivedValuesOnForm(request);

        super.refresh(mapping, form, request, response);
        refreshAdHocRoutingWorkgroupLookups(request, kualiForm);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * special refresh needed to get the workgroups populated correctly when coming back from workgroup lookups
     *
     * @param request
     * @param kualiForm
     */
    @SuppressWarnings("unchecked")
    protected void refreshAdHocRoutingWorkgroupLookups(final HttpServletRequest request, final KualiDocumentFormBase kualiForm) {
        for (final Enumeration<String> i = request.getParameterNames(); i.hasMoreElements(); ) {
            final String parameterName = i.nextElement();
            if ("newAdHocRouteWorkgroup.recipientName".equals(parameterName)
                    && StringUtils.isNotEmpty(request.getParameter(parameterName))) {
                //check for namespace
                String namespace = KFSConstants.CoreModuleNamespaces.KFS;
                // CU customization: use StringUtils.isNotBlank instead of StringUtils.isNotEmpty and trim which is not null safe
                if (StringUtils.isNotBlank(request.getParameter("newAdHocRouteWorkgroup.recipientNamespaceCode"))) {
                    namespace = request.getParameter("newAdHocRouteWorkgroup.recipientNamespaceCode").trim();
                }
                final Group group = getGroupService().getGroupByNamespaceCodeAndName(namespace, request.getParameter(
                    parameterName));
                if (group != null) {
                    kualiForm.getNewAdHocRouteWorkgroup().setId(group.getId());
                    kualiForm.getNewAdHocRouteWorkgroup().setRecipientName(group.getName());
                    kualiForm.getNewAdHocRouteWorkgroup().setRecipientNamespaceCode(group.getNamespaceCode());
                } else {
                    GlobalVariables.getMessageMap().putError("newAdHocRouteWorkgroup.recipientNamespaceCode",
                            ERROR_INVALID_ADHOC_WORKGROUP_NAMESPACECODE);
                    return;
                }
            }
            if (parameterName.startsWith("adHocRouteWorkgroup[") && StringUtils.isNotEmpty(request.getParameter(parameterName))) {
                if (parameterName.endsWith(".recipientName")) {
                    final int lineNumber = Integer.parseInt(StringUtils.substringBetween(parameterName, "[", "]"));
                    //check for namespace
                    String namespaceParam = "adHocRouteWorkgroup[" + lineNumber + "].recipientNamespaceCode";
                    String namespace = KFSConstants.CoreModuleNamespaces.KFS;
                    // CU customization: use StringUtils.isNotBlank instead of StringUtils.isNotEmpty and trim which is not null safe
                    if (StringUtils.isNotBlank(request.getParameter(namespaceParam))) {
                        namespace = request.getParameter(namespaceParam).trim();
                    }
                    final Group group = getGroupService().getGroupByNamespaceCodeAndName(namespace, request.getParameter(
                        parameterName));
                    if (group != null) {
                        kualiForm.getAdHocRouteWorkgroup(lineNumber).setId(group.getId());
                        kualiForm.getAdHocRouteWorkgroup(lineNumber).setRecipientName(group.getName());
                        kualiForm.getAdHocRouteWorkgroup(lineNumber).setRecipientNamespaceCode(group.getNamespaceCode());
                    } else {
                        GlobalVariables.getMessageMap().putError(namespaceParam,
                                ERROR_INVALID_ADHOC_WORKGROUP_NAMESPACECODE);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Cancels the pending attachment, if any.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward cancelBOAttachment(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;

        // blank current attachmentFile
        documentForm.setAttachmentFile(new BlankFormFile());

        // remove current attachment, if any
        final Note note = documentForm.getNewNote();
        note.removeAttachment();
        documentForm.setNewNote(note);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Downloads the selected attachment to the user's browser
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward downloadBOAttachment(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;

        final int attachmentIndex = selectedAttachmentIndex(request);
        if (attachmentIndex >= 0) {
            final Note note = documentForm.getDocument().getNote(attachmentIndex);
            final Attachment attachment = note.getAttachment();
            // make sure attachment is setup with backwards reference to note (rather then doing this we could also
            // just call the attachment service (with a new method that took in the note)
            attachment.setNote(note);

            // since we're downloading a file, all of the editable properties from the previous request will continue
            // to be editable.
            documentForm.copyPopulateEditablePropertiesToActionEditableProperties();

            logAttachmentDownload(documentForm, request, note, attachment);

            try (InputStream attachmentContents = attachment.getAttachmentContents()) {
                WebUtils.saveMimeInputStreamAsFile(
                        response,
                        attachment.getAttachmentMimeTypeCode(),
                        attachmentContents,
                        attachment.getAttachmentFileName(),
                        attachment.getAttachmentFileSize().intValue()
                );
            }

            return null;
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected void logAttachmentDownload(
            final KualiDocumentFormBase form, final HttpServletRequest request, final Note note,
            final Attachment attachment) {
        final String buf = "DownloadAttachment,docNum=" + form.getDocument().getDocumentNumber() + ",noteId=" +
                           attachment.getNoteIdentifier() + ",attId=" + attachment.getAttachmentIdentifier();
        KNSServiceLocator.getSecurityLoggingService().logCustomString(buf);
    }

    /**
     * @param request
     * @return index of the attachment whose download button was just pressed
     */
    protected int selectedAttachmentIndex(final HttpServletRequest request) {
        int attachmentIndex = -1;

        final String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            final String attachmentIndexParam = StringUtils.substringBetween(parameterName, ".attachment[", "].");

            try {
                attachmentIndex = Integer.parseInt(attachmentIndexParam);
            } catch (final NumberFormatException ignored) {
                // ignore
            }
        }

        return attachmentIndex;
    }

    /**
     * insert a note into the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward insertBONote(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();
        final Note newNote = kualiDocumentFormBase.getNewNote();
        newNote.setNotePostedTimestampToCurrent();

        String attachmentTypeCode = null;

        final FormFile attachmentFile = kualiDocumentFormBase.getAttachmentFile();
        if (attachmentFile == null) {
            GlobalVariables.getMessageMap().putError(
                String.format("%s.%s",
                    KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                    KRADConstants.NOTE_ATTACHMENT_FILE_PROPERTY_NAME),
                KFSKeyConstants.ERROR_UPLOADFILE_NULL);
        }

        if (newNote.getAttachment() != null) {
            attachmentTypeCode = newNote.getAttachment().getAttachmentTypeCode();
        }

        // check authorization for adding notes
        final DocumentAuthorizer documentAuthorizer = getDocumentHelperService().getDocumentAuthorizer(document);
        if (!documentAuthorizer.canAddNoteAttachment(document, attachmentTypeCode,
                GlobalVariables.getUserSession().getPerson())) {
            throw buildAuthorizationException("annotate", document);
        }

        // create the attachment first, so that failure-to-create-attachment can be treated as a validation failure

        Attachment attachment = null;
        if (attachmentFile != null && StringUtils.isNotBlank(attachmentFile.getFileName())) {
            if (attachmentFile.getFileSize() == 0) {
                GlobalVariables.getMessageMap().putError(String.format("%s.%s",
                        KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                        KRADConstants.NOTE_ATTACHMENT_FILE_PROPERTY_NAME),
                    ERROR_UPLOADFILE_EMPTY,
                    attachmentFile.getFileName());
                
              //CU customization: Added blacklist attachment processing  
            } else if (getBlackListAttachmentService().attachmentFileExtensionIsDisallowed(attachmentFile.getFileName())) {
                GlobalVariables.getMessageMap().putError(String.format("%s.%s",
                        KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                        KRADConstants.NOTE_ATTACHMENT_FILE_PROPERTY_NAME),
                        CUKFSKeyConstants.ERROR_UPLOADFILE_EXTENSION,
                    attachmentFile.getFileName());
                
            } else {
                String attachmentType = null;
                final Attachment newAttachment = kualiDocumentFormBase.getNewNote().getAttachment();
                if (newAttachment != null) {
                    attachmentType = newAttachment.getAttachmentTypeCode();
                }
                attachment = getAttachmentService().createAttachment(document.getNoteTarget(),
                        attachmentFile.getFileName(), attachmentFile.getContentType(), attachmentFile.getFileSize(),
                        attachmentFile.getInputStream(), attachmentType);
            }
        }

        final DocumentEntry entry = getDocumentDictionaryService().getDocumentEntry(document.getClass().getName());

        if (entry.getDisplayTopicFieldInNotes()) {
            final String topicText = kualiDocumentFormBase.getNewNote().getNoteTopicText();
            if (StringUtils.isBlank(topicText)) {
                GlobalVariables.getMessageMap().putError(String.format("%s.%s",
                        KRADConstants.NEW_DOCUMENT_NOTE_PROPERTY_NAME,
                        KRADConstants.NOTE_TOPIC_TEXT_PROPERTY_NAME), KFSKeyConstants.ERROR_REQUIRED,
                    "Note Topic (Note Topic)");
            }
        }

        // create a new note from the data passed in
        // TODO gah! this is awful
        final Person kualiUser = GlobalVariables.getUserSession().getPerson();
        if (kualiUser == null) {
            throw new IllegalStateException("Current UserSession has a null Person.");
        }
        final Note tmpNote = getNoteService().createNote(newNote, document.getNoteTarget(), kualiUser.getPrincipalId());

        final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                KRADPropertyConstants.NOTE, tmpNote.getNoteText(), "insertBONote", "");
        if (forward != null) {
            return forward;
        }

        // validate the note
        final boolean rulePassed = getKualiRuleService().applyRules(new AddNoteEvent(document, tmpNote));

        // if the rule evaluation passed, let's add the note
        if (rulePassed) {
            tmpNote.refresh();

            final DocumentHeader documentHeader = document.getDocumentHeader();

            // associate note with object now
            document.addNote(tmpNote);

            // persist the note if the document is already saved the getObjectId check is to get around a bug with
            // certain documents where "saved" doesn't really persist, if you notice any problems with missing notes
            // check this line maintenance document BO note should only be saved into table when document is in the
            // PROCESSED workflow status
            if (!documentHeader.getWorkflowDocument().isInitiated()
                    && StringUtils.isNotEmpty(document.getNoteTarget().getObjectId())
                    && !(document instanceof MaintenanceDocument
                    && NoteType.BUSINESS_OBJECT.getCode().equals(tmpNote.getNoteTypeCode()))) {
                getNoteService().save(tmpNote);
            }
            // adding the attachment after refresh gets called, since the attachment record doesn't get persisted
            // until the note does (and therefore refresh doesn't have any attachment to autoload based on the id, nor
            // does it autopopulate the id since the note hasn't been persisted yet)
            if (attachment != null) {
                tmpNote.addAttachment(attachment);
                // save again for attachment, note this is because sometimes the attachment is added first to the
                // above then ojb tries to save without the PK on the attachment I think it is safer then trying to
                // get the sequence manually
                if (!documentHeader.getWorkflowDocument().isInitiated()
                        && StringUtils.isNotEmpty(document.getNoteTarget().getObjectId())
                        && !(document instanceof MaintenanceDocument
                            && NoteType.BUSINESS_OBJECT.getCode().equals(tmpNote.getNoteTypeCode()))) {
                    getNoteService().save(tmpNote);
                }
            }

            // save maintenance documents after a note is added to make sure the document content is updated with the
            // new note
            if (!documentHeader.getWorkflowDocument().isInitiated() && document instanceof MaintenanceDocument
                    && NoteType.BUSINESS_OBJECT.getCode().equals(tmpNote.getNoteTypeCode())) {
                getDocumentService().saveDocument(document);
            }

            // reset the new note back to an empty one
            kualiDocumentFormBase.setNewNote(new Note());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * delete a note from the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteBONote(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();
        final int noteIndex = getLineToDelete(request);
        final Note note = document.getNote(noteIndex);

        deleteNoteFromDocument(document, note);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    protected void deleteNoteFromDocument(final Document document, final Note note) {
        Validate.isTrue(note != null, "Invalid Note ID");
        // ok to delete the note/attachment
        final Attachment attachment = note.getAttachment();
        String attachmentTypeCode = null;
        if (attachment != null) {
            attachmentTypeCode = attachment.getAttachmentTypeCode();
        }
        final String authorUniversalIdentifier = note.getAuthorUniversalIdentifier();
        if (!WebUtils.canDeleteNoteAttachment(document, attachmentTypeCode, authorUniversalIdentifier)) {
            throw buildAuthorizationException("annotate", document);
        }

        // only do this if the note has been persisted
        if (attachment != null) {
            //KFSMI-798 - refresh() changed to refreshNonUpdateableReferences()
            //All references for the business object Attachment are auto-update="none",
            //so refreshNonUpdateableReferences() should work the same as refresh()
            if (note.getNoteIdentifier() != null) {
                // KULRICE-2343 don't blow away note reference if the note wasn't persisted
                attachment.refreshNonUpdateableReferences();
            }
            getAttachmentService().deleteAttachmentContents(attachment);
        }
        // delete the note if the document is already saved
        if (!document.getDocumentHeader().getWorkflowDocument().isInitiated()) {
            getNoteService().deleteNote(note);
        }
        document.removeNote(note);
    }

    /**
     * Override this to customize which routing action to take when sending a note.  This method reads the system
     * parameter KFS-SYS/Document/SEND_NOTE_WORKFLOW_NOTIFICATION_ACTIONS to determine which action to take
     *
     * @param request
     * @param note
     * @return a value from {@link KewApiConstants}
     */
    public String determineNoteWorkflowNotificationAction(
            final HttpServletRequest request,
                                                          final KualiDocumentFormBase kualiDocumentFormBase, final Note note) {
        return getParameterService().getParameterValueAsString(KFSConstants.CoreModuleNamespaces.KFS,
                KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                KRADConstants.SEND_NOTE_WORKFLOW_NOTIFICATION_ACTIONS_PARM_NM);
    }

    public ActionForward sendNoteWorkflowNotification(
            final ActionMapping mapping, final ActionForm form,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        final Document document = kualiDocumentFormBase.getDocument();

        final Note note = document.getNote(getSelectedLine(request));

        // verify recipient was specified
        if (StringUtils.isBlank(note.getAdHocRouteRecipient().getId())) {
            GlobalVariables.getMessageMap().putError(KRADPropertyConstants.NEW_DOCUMENT_NOTE,
                    ERROR_SEND_NOTE_NOTIFICATION_RECIPIENT);
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else {
            // check recipient is valid
            note.getAdHocRouteRecipient().setActionRequested(determineNoteWorkflowNotificationAction(request,
                    kualiDocumentFormBase, note));

            final boolean rulePassed = getKualiRuleService().applyRules(new AddAdHocRoutePersonEvent(
                    KRADPropertyConstants.NEW_DOCUMENT_NOTE, document,
                    (AdHocRoutePerson) note.getAdHocRouteRecipient()));
            if (!rulePassed) {
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        // if document is saved, send notification
        if (!document.getDocumentHeader().getWorkflowDocument().isInitiated()) {
            getDocumentService().sendNoteRouteNotification(document, note, GlobalVariables.getUserSession().getPerson());

            // add success message
            KNSGlobalVariables.getMessageList().add(MESSAGE_SEND_NOTE_NOTIFICATION_SUCCESSFUL);
        } else {
            GlobalVariables.getMessageMap().putError(KRADPropertyConstants.NEW_DOCUMENT_NOTE,
                    ERROR_SEND_NOTE_NOTIFICATION_DOCSTATUS);
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Generates detailed log messages for OptimisticLockExceptions
     *
     * @param e
     */
    private String createOjbOptimisticLockExceptionLogMsg(final OptimisticLockException e) {
        final StringBuffer message = new StringBuffer("caught OptimisticLockException, caused by ");
        final Object sourceObject = e.getSourceObject();
        String infix;
        try {
            // try to add instance details
            infix = sourceObject.toString();
        } catch (final Exception e2) {
            // just use the class name
            infix = sourceObject.getClass().getName();
        }
        message.append(infix);

        if (sourceObject instanceof PersistableBusinessObject) {
            final PersistableBusinessObject persistableObject = (PersistableBusinessObject) sourceObject;
            message.append(" [versionNumber = ").append(persistableObject.getVersionNumber()).append("]");
        }

        return message.toString();
    }

    /**
     * Makes calls to the PromptBeforeValidation specified for the document. If the class returns an actionforward,
     * that forward will be returned (thus controlling how execution occurs), or null.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward promptBeforeValidation(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        return promptBeforeValidation(mapping, form, request, response, "route");
    }

    /**
     * Makes calls to the PromptBeforeValidation specified for the document. If the class returns an actionforward,
     * that forward will be returned (thus controlling how execution occurs), or null.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param methodToCall
     * @return
     * @throws Exception
     */
    public ActionForward promptBeforeValidation(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response, final String methodToCall) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;

        /* callback to any pre rules check class */
        final Class<? extends PromptBeforeValidation> promptBeforeValidationClass =
                getDataDictionaryService().getPromptBeforeValidationClass(kualiDocumentFormBase.getDocTypeName());
        LOG.debug("PromptBeforeValidationClass: {}", promptBeforeValidationClass);
        if (promptBeforeValidationClass != null) {
            final PromptBeforeValidation promptBeforeValidation = promptBeforeValidationClass.newInstance();
            final PromptBeforeValidationEvent event = new PromptBeforeValidationEvent("Pre Maint route Check",
                    "", kualiDocumentFormBase.getDocument());
            final boolean continueRoute = promptBeforeValidation.processPrompts(form, request, event);
            if (!continueRoute) {
                if (event.isPerformQuestion()) {
                    return super.performQuestionWithoutInput(mapping, kualiDocumentFormBase, request, response,
                            event.getQuestionId(), event.getQuestionText(), event.getQuestionType(), methodToCall,
                            event.getQuestionContext());
                } else {
                    // This error section is here to avoid a silent and very confusing failure. If the PreRule
                    // instance returns a null for the processPreRuleChecks above, but does not set an
                    // ActionForwardName on the event, processing will just silently fail here, and the user
                    // will be presented with a blank frame.
                    //
                    // If the processPreRuleCheck() returns a false, an ActionForwardName needs to be set before hand
                    // by the PreRule class.
                    final ActionForward actionForward = mapping.findForward(event.getActionForwardName());
                    if (actionForward == null) {
                        throw new RuntimeException("No ActionForwardName defined on this Event, no further actions " +
                                "will be processed.");
                    }
                    return actionForward;
                }
            }
        }

        return null;
    }

    /**
     * Convenience method for building authorization exceptions
     *
     * @param action
     * @param document
     */
    protected DocumentAuthorizationException buildAuthorizationException(final String action, final Document document) {
        return new DocumentAuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(),
                action, document.getDocumentNumber());
    }

    protected boolean exitingDocument() {
        final String methodCalledViaDispatch = (String) GlobalVariables.getUserSession()
                .retrieveObject(DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_OBJECT_KEY);
        final String methodCompleted = (String) GlobalVariables.getUserSession()
                .retrieveObject(DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_COMPLETE_OBJECT_KEY);
        return StringUtils.isNotEmpty(methodCompleted) && StringUtils.isNotEmpty(methodCalledViaDispatch)
                && methodCompleted.startsWith(methodCalledViaDispatch);
    }

    protected void setupDocumentExit() {
        final String methodCalledViaDispatch = (String) GlobalVariables.getUserSession()
                .retrieveObject(DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_OBJECT_KEY);
        if (StringUtils.isNotEmpty(methodCalledViaDispatch)) {
            GlobalVariables.getUserSession().addObject(
                    DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_COMPLETE_OBJECT_KEY,
                    methodCalledViaDispatch + DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_COMPLETE_MARKER);
        }
    }

    /**
     * If the given form has returnToActionList set to true, this method returns an ActionForward that should take the
     * user back to their action list; otherwise, it returns them to the portal.
     *
     * @param form
     * @return
     */
    protected ActionForward returnToSender(
            final HttpServletRequest request, final ActionMapping mapping,
            final KualiDocumentFormBase form) {
        final ActionForward dest;
        if (form.isReturnToActionList()) {
            final String applicationUrl = getKualiConfigurationService().getPropertyValueAsString(
                    KFSConstants.APPLICATION_URL_KEY);
            final String actionListUrl = applicationUrl + "/ActionList.do?title=Action%20List";

            dest = new ActionForward(actionListUrl, true);
        } else if (StringUtils.isNotBlank(form.getBackLocation())) {
            dest = new ActionForward(form.getBackLocation(), true);
        } else {
            dest = mapping.findForward(KRADConstants.MAPPING_PORTAL);
        }

        setupDocumentExit();
        return dest;
    }

    protected void populateAuthorizationFields(final KualiDocumentFormBase formBase) {
        if (formBase.isFormDocumentInitialized()) {
            final Document document = formBase.getDocument();
            final Person user = GlobalVariables.getUserSession().getPerson();
            final DocumentPresentationController documentPresentationController = KNSServiceLocator
                .getDocumentHelperService().getDocumentPresentationController(document);
            final DocumentAuthorizer documentAuthorizer = getDocumentHelperService().getDocumentAuthorizer(document);
            Set<String> documentActions = documentPresentationController.getDocumentActions(document);
            documentActions = documentAuthorizer.getDocumentActions(document, user, documentActions);
            formBase.setDocumentActions(convertSetToMap(documentActions));
        }
    }

    protected void populateAdHocActionRequestCodes(final KualiDocumentFormBase formBase) {
        final Document document = formBase.getDocument();
        final DocumentAuthorizer documentAuthorizer = getDocumentHelperService().getDocumentAuthorizer(document);
        final Map<String, String> adHocActionRequestCodes = new HashMap<>();

        final Person user = GlobalVariables.getUserSession().getPerson();
        if (documentAuthorizer.canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_FYI_REQ, user)) {
            adHocActionRequestCodes.put(KewApiConstants.ACTION_REQUEST_FYI_REQ,
                    KewApiConstants.ACTION_REQUEST_FYI_REQ_LABEL);
        }
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (!workflowDocument.isFinal()
                && documentAuthorizer.canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ,
                user)) {
            adHocActionRequestCodes.put(KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ,
                    KewApiConstants.ACTION_REQUEST_ACKNOWLEDGE_REQ_LABEL);
        }
        if (!(workflowDocument.isApproved() || workflowDocument.isProcessed() || workflowDocument.isFinal())
                && documentAuthorizer.canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_APPROVE_REQ,
                user)) {
            adHocActionRequestCodes.put(KewApiConstants.ACTION_REQUEST_APPROVE_REQ,
                    KewApiConstants.ACTION_REQUEST_APPROVE_REQ_LABEL);
        }

        if ((workflowDocument.isInitiated() || workflowDocument.isSaved())
                && documentAuthorizer.canSendAdHocRequests(document, KewApiConstants.ACTION_REQUEST_COMPLETE_REQ,
                user)) {
            // Check if there is already a request for completion pending for the document.
            adHocActionRequestCodes.put(KewApiConstants.ACTION_REQUEST_COMPLETE_REQ,
                    KewApiConstants.ACTION_REQUEST_COMPLETE_REQ_LABEL);
        }
        formBase.setAdHocActionRequestCodes(adHocActionRequestCodes);
    }

    @SuppressWarnings("unchecked")
    protected Map convertSetToMap(final Set s) {
        final Map map = new HashMap();
        for (final Object key : s) {
            map.put(key, KRADConstants.KUALI_DEFAULT_TRUE_VALUE);
        }
        return map;
    }

    protected DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }
        return dataDictionaryService;
    }

    protected DocumentDictionaryService getDocumentDictionaryService() {
        if (documentDictionaryService == null) {
            documentDictionaryService = KRADServiceLocatorWeb.getDocumentDictionaryService();
        }
        return documentDictionaryService;
    }

    protected DocumentHelperService getDocumentHelperService() {
        if (documentHelperService == null) {
            documentHelperService = KNSServiceLocator.getDocumentHelperService();
        }
        return documentHelperService;
    }

    public DocumentService getDocumentService() {
        if (documentService == null) {
            documentService = KRADServiceLocatorWeb.getDocumentService();
        }
        return documentService;
    }

    protected ConfigurationService getKualiConfigurationService() {
        if (kualiConfigurationService == null) {
            kualiConfigurationService = KRADServiceLocator.getKualiConfigurationService();
        }
        return kualiConfigurationService;
    }

    protected ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = CoreFrameworkServiceLocator.getParameterService();
        }
        return parameterService;
    }

    public KualiRuleService getKualiRuleService() {
        if (kualiRuleService == null) {
            kualiRuleService = KRADServiceLocatorWeb.getKualiRuleService();
        }
        return kualiRuleService;
    }

    protected GroupService getGroupService() {
        if (groupService == null) {
            groupService = KimApiServiceLocator.getGroupService();
        }
        return groupService;
    }

    protected AttachmentService getAttachmentService() {
        if (attachmentService == null) {
            attachmentService = KRADServiceLocator.getAttachmentService();
        }
        return attachmentService;
    }

    protected NoteService getNoteService() {
        if (noteService == null) {
            noteService = KRADServiceLocator.getNoteService();
        }
        return noteService;
    }

    // public for testing purposes
    public WorkflowDocumentService getWorkflowDocumentService() {
        if (workflowDocumentService == null) {
            workflowDocumentService = KewApiServiceLocator.getWorkflowDocumentService();
        }
        return workflowDocumentService;
    }

    // public for testing purposes
    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = SpringContext.getBean(BusinessObjectService.class);
        }
        return businessObjectService;
    }

    // public for testing purposes
    public SuperUserService getSuperUserService() {
        if (superUserService == null) {
            superUserService = SpringContext.getBean(SuperUserService.class);
        }
        return superUserService;
    }

    @Override
    protected BusinessObjectAuthorizationService getBusinessObjectAuthorizationService() {
        if (businessObjectAuthorizationService == null) {
            businessObjectAuthorizationService = KNSServiceLocator.getBusinessObjectAuthorizationService();
        }
        return businessObjectAuthorizationService;
    }

    public BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        if (businessObjectMetaDataService == null) {
            businessObjectMetaDataService = KNSServiceLocator.getBusinessObjectMetaDataService();
        }
        return businessObjectMetaDataService;
    }

    // public for testing purposes
    public AdHocRoutingService getAdHocRoutingService() {
        if (adHocRoutingService == null) {
            adHocRoutingService = SpringContext.getBean(AdHocRoutingService.class);
        }
        return adHocRoutingService;
    }

    public PersonService getPersonService() {
        if (personService == null) {
            personService = SpringContext.getBean(PersonService.class);
        }
        return personService;
    }
    

    //CU customization: Added blacklist attachment processing
    public BlackListAttachmentService getBlackListAttachmentService() {
        if (blackListAttachmentService == null) {
            blackListAttachmentService = SpringContext.getBean(BlackListAttachmentService.class);
        }
        return blackListAttachmentService;
    }

    @Override
    public ActionForward hideAllTabs(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response)
        throws Exception {
        if (form instanceof KualiDocumentFormBase) {
            WebUtils.reuseErrorMapFromPreviousRequest((KualiDocumentFormBase) form);
        }
        return super.hideAllTabs(mapping, form, request, response);
    }

    @Override
    public ActionForward showAllTabs(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response)
        throws Exception {
        if (form instanceof KualiDocumentFormBase) {
            WebUtils.reuseErrorMapFromPreviousRequest((KualiDocumentFormBase) form);
        }
        return super.showAllTabs(mapping, form, request, response);
    }

    @Override
    public ActionForward toggleTab(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response)
        throws Exception {
        if (form instanceof KualiDocumentFormBase) {
            WebUtils.reuseErrorMapFromPreviousRequest((KualiDocumentFormBase) form);
        }
        return super.toggleTab(mapping, form, request, response);
    }

    public ActionForward takeSuperUserActions(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;
        if (StringUtils.isBlank(documentForm.getSuperUserAnnotation())) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.takeactions.annotation.missing", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (documentForm.getSelectedActionRequests().isEmpty()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.takeactions.none.selected", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (!documentForm.isStateAllowsApproveSingleActionRequest()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.takeactions.not.allowed", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        for (final String actionRequestId : documentForm.getSelectedActionRequests()) {
            ActionRequest actionRequest = null;
            for (final ActionRequest pendingActionRequest : documentForm.getActionRequests()) {
                if (StringUtils.equals(pendingActionRequest.getActionRequestId(), actionRequestId)) {
                    actionRequest = pendingActionRequest;
                    break;
                }
            }
            if (actionRequest == null) {
                // If the action request isn't pending then skip it
                continue;
            }
            if (actionRequest.isCompleteRequest() || actionRequest.isApproveRequest()) {
                getDocumentService().validateAndPersistDocument(documentForm.getDocument(),
                        new RouteDocumentEvent(documentForm.getDocument()));
            }

            getSuperUserService().takeRequestedAction(
                    actionRequest,
                    documentForm.getDocId(),
                    GlobalVariables.getUserSession().getPerson(),
                    documentForm.getSuperUserAnnotation()
            );

            final String messageString;
            if (actionRequest.isAcknowledgeRequest()) {
                messageString = "general.routing.superuser.actionRequestAcknowledged";
            } else if (actionRequest.isFYIRequest()) {
                messageString = "general.routing.superuser.actionRequestFYI";
            } else if (actionRequest.isCompleteRequest()) {
                messageString = "general.routing.superuser.actionRequestCompleted";
            } else if (actionRequest.isApproveRequest()) {
                messageString = "general.routing.superuser.actionRequestApproved";
            } else {
                messageString = "general.routing.superuser.actionRequestApproved";
            }
            GlobalVariables.getMessageMap().putInfo("document", messageString, documentForm.getDocId(),
                    actionRequestId);
        }
        documentForm.setSuperUserAnnotation("");
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward superUserDisapprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;
        if (StringUtils.isBlank(documentForm.getSuperUserAnnotation())) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.disapprove.annotation.missing", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (!documentForm.getSelectedActionRequests().isEmpty()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.disapprove.when.actions.checked", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (!documentForm.isStateAllowsApproveOrDisapprove()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.disapprove.not.allowed", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        getSuperUserService().disapprove(
                documentForm.getDocId(),
                GlobalVariables.getUserSession().getPerson(),
                documentForm.getSuperUserAnnotation()
        );

        GlobalVariables.getMessageMap().putInfo("document",
                "general.routing.superuser.disapproved", documentForm.getDocId());
        documentForm.setSuperUserAnnotation("");
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward superUserApprove(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        final KualiDocumentFormBase documentForm = (KualiDocumentFormBase) form;
        if (StringUtils.isBlank(documentForm.getSuperUserAnnotation())) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.approve.annotation.missing", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (!documentForm.getSelectedActionRequests().isEmpty()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.approve.when.actions.checked", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } else if (!documentForm.isStateAllowsApproveOrDisapprove()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("superuser.errors",
                    "superuser.approve.not.allowed", "");
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        getSuperUserService().blanketApprove(
                documentForm.getDocId(),
                GlobalVariables.getUserSession().getPerson(),
                documentForm.getSuperUserAnnotation()
        );

        GlobalVariables.getMessageMap().putInfo("document", "general.routing.superuser.approved",
                documentForm.getDocId());
        documentForm.setSuperUserAnnotation("");
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public WorkflowDocumentActionsService getWorkflowDocumentActionsService() {
        return KewApiServiceLocator.getWorkflowDocumentActionsService();
    }

    /**
     * Complete document action
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward complete(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        doProcessingAfterPost(kualiDocumentFormBase, request);

        kualiDocumentFormBase.setDerivedValuesOnForm(request);
        final ActionForward preRulesForward = promptBeforeValidation(mapping, form, request, response);
        if (preRulesForward != null) {
            return preRulesForward;
        }

        final Document document = kualiDocumentFormBase.getDocument();

        getDocumentService().completeDocument(document, kualiDocumentFormBase.getAnnotation(),
                combineAdHocRecipients(kualiDocumentFormBase));
        KNSGlobalVariables.getMessageList().add(KFSKeyConstants.MESSAGE_ROUTE_SUCCESSFUL);
        kualiDocumentFormBase.setAnnotation("");

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * KULRICE-7864: blanket approve should not be allowed when adhoc route for completion request is newly added
     * <p>
     * determine whether any adhoc recipient in the given document has been just added for completion action
     */
    protected boolean hasPendingAdhocForCompletion(final KualiDocumentFormBase kualiDocumentFormBase) {
        final List<AdHocRouteRecipient> adHocRecipients = combineAdHocRecipients(kualiDocumentFormBase);

        for (final AdHocRouteRecipient receipients : adHocRecipients) {
            final String actionRequestedCode = receipients.getActionRequested();

            if (KewApiConstants.ACTION_REQUEST_COMPLETE_REQ.equals(actionRequestedCode)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Class that encapsulates the workflow for obtaining an reason from an action prompt.
     */
    // CU Customization: Increased class's visibility to protected.
    protected final class ReasonPrompt {
        final String questionId;
        final String questionTextKey;
        final String questionType;
        final String missingReasonKey;
        final String questionCallerMapping;
        final String abortButton;
        final String noteIntroKey;

        /**
         * @param questionId            the question id/instance,
         * @param questionTextKey       application resources key for question text
         * @param questionType          the {@link Question} question type
         * @param questionCallerMapping mapping of original action
         * @param abortButton           button value considered to abort the prompt and return (optional, may be null)
         * @param noteIntroKey          application resources key for question text prefix (optional, may be null)
         */
        // CU Customization: Increased method's visibility to public.
        public ReasonPrompt(
                final String questionId, final String questionTextKey, final String questionType, final String missingReasonKey,
                final String questionCallerMapping, final String abortButton, final String noteIntroKey) {
            this.questionId = questionId;
            this.questionTextKey = questionTextKey;
            this.questionType = questionType;
            this.questionCallerMapping = questionCallerMapping;
            this.abortButton = abortButton;
            this.noteIntroKey = noteIntroKey;
            this.missingReasonKey = missingReasonKey;
        }

        /**
         * Obtain a validated reason and button value via a Question prompt.  Reason is validated against
         * sensitive data patterns, and max Note text length
         *
         * @param mapping  Struts mapping
         * @param form     Struts form
         * @param request  http request
         * @param response http response
         * @return Response object representing *either*: 1) an ActionForward due to error or abort 2) a reason and
         *         button clicked
         * @throws Exception
         */
        public Response ask(
                final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
                final HttpServletResponse response) throws Exception {
            final String question = request.getParameter(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME);
            String reason = request.getParameter(KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME);

            if (StringUtils.isBlank(reason)) {
                final String context = request.getParameter(KRADConstants.QUESTION_CONTEXT);
                if (context != null
                        && StringUtils.contains(context, KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME + "=")) {
                    reason = StringUtils.substringAfter(context, KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME + "=");
                }
            }

            final String disapprovalNoteText;

            // start in logic for confirming the disapproval
            if (question == null) {
                // ask question if not already asked
                return new Response(question, performQuestionWithInput(mapping, form, request, response,
                        questionId,
                        getKualiConfigurationService().getPropertyValueAsString(questionTextKey),
                        questionType, questionCallerMapping, ""));
            }

            final String buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
            if (questionId.equals(question) && abortButton != null && abortButton.equals(buttonClicked)) {
                // if no button clicked just reload the doc
                return new Response(question, mapping.findForward(KFSConstants.MAPPING_BASIC));
            }

            // have to check length on value entered
            String introNoteMessage = "";
            if (noteIntroKey != null) {
                introNoteMessage = getKualiConfigurationService().getPropertyValueAsString(noteIntroKey) +
                                   KRADConstants.BLANK_SPACE;
            }

            // build out full message
            disapprovalNoteText = introNoteMessage + reason;

            // check for sensitive data in note
            final boolean warnForSensitiveData = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsBoolean(
                    KFSConstants.CoreModuleNamespaces.KFS, KfsParameterConstants.ALL_COMPONENT,
                    KRADConstants.SystemGroupParameterNames.SENSITIVE_DATA_PATTERNS_WARNING_IND);
            if (warnForSensitiveData) {
                final String context = KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME + "=" + reason;
                final ActionForward forward = checkAndWarnAboutSensitiveData(mapping, form, request, response,
                        KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME, disapprovalNoteText, questionCallerMapping,
                        context);
                if (forward != null) {
                    return new Response(question, forward);
                }
            } else {
                if (KRADUtils.containsSensitiveDataPatternMatch(disapprovalNoteText)) {
                    return new Response(question, performQuestionWithInputAgainBecauseOfErrors(mapping, form, request,
                            response, questionId,
                            getKualiConfigurationService().getPropertyValueAsString(questionTextKey),
                            questionType, questionCallerMapping, "", reason,
                            KFSKeyConstants.ERROR_DOCUMENT_FIELD_CONTAINS_POSSIBLE_SENSITIVE_DATA,
                            KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME, "reason"));
                }
            }

            final int disapprovalNoteTextLength = disapprovalNoteText.length();

            // get note text max length from DD
            final int noteTextMaxLength = getDataDictionaryService().getAttributeMaxLength(Note.class,
                    KRADConstants.NOTE_TEXT_PROPERTY_NAME);

            if (StringUtils.isBlank(reason) || disapprovalNoteTextLength > noteTextMaxLength) {

                if (reason == null) {
                    // prevent a NPE by setting the reason to a blank string
                    reason = "";
                }
                return new Response(question, performQuestionWithInputAgainBecauseOfErrors(mapping, form, request,
                        response, questionId,
                        getKualiConfigurationService().getPropertyValueAsString(questionTextKey),
                        questionType, questionCallerMapping, "", reason,
                        missingReasonKey, KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME,
                        Integer.toString(noteTextMaxLength)));
            }

            return new Response(question, disapprovalNoteText, buttonClicked);
        }

        // CU Customization: Increased visibility of this class and its fields to public.
        public class Response {
            public final String question;
            public final ActionForward forward;
            public final String reason;
            public final String button;

            Response(final String question, final ActionForward forward) {
                this(question, forward, null, null);
            }

            Response(final String question, final String reason, final String button) {
                this(question, null, reason, button);
            }

            private Response(final String question, final ActionForward forward, final String reason, final String button) {
                this.question = question;
                this.forward = forward;
                this.reason = reason;
                this.button = button;
            }
        }
    }
}
