/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.ojb.broker.OptimisticLockException;
import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.InvalidCancelException;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.util.RequestUtils;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.service.SessionDocumentService;
import org.kuali.kfs.kns.util.ErrorContainer;
import org.kuali.kfs.kns.util.InfoContainer;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.util.WarningContainer;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.EditablePropertiesHistoryHolder;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.kns.web.struts.form.pojo.PojoForm;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.CsrfService;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springmodules.orm.ojb.OjbOperationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

/**
 * This class handles setup of user session and restoring of action form.
 */
/* Cornell customization: backport FINP-8908, can be removed when we upgrade to the 11/16/2022 version of financials*/
public class KualiRequestProcessor extends RequestProcessor {

    private static final String MDC_DOC_ID = "docId";
    private static final String PREVIOUS_REQUEST_EDITABLE_PROPERTIES_GUID_PARAMETER_NAME =
            "actionEditablePropertiesGuid";

    private static final Logger LOG = LogManager.getLogger();

    private SessionDocumentService sessionDocumentService;
    private PlatformTransactionManager transactionManager;
    private CsrfService csrfService;

    public void process(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
            ServletException {
        if (LOG.isInfoEnabled()) {
            LOG.info(new StringBuffer("Started processing request: '").append(request.getRequestURI()).append(
                    "' w/ query string: '").append(request.getQueryString()).append("'"));
        }

        strutsProcess(request, response);
        KNSGlobalVariables.setKualiForm(null);

        try {
            ActionForm form = WebUtils.getKualiForm(request);
            if (form instanceof KualiDocumentFormBase) {
                String docId = ((KualiDocumentFormBase) form).getDocId();
                if (docId != null) {
                    ThreadContext.put(MDC_DOC_ID, docId);
                }
            }

            String refreshCaller = request.getParameter(KRADConstants.REFRESH_CALLER);
            if (form != null && KualiDocumentFormBase.class.isAssignableFrom(form.getClass())
                    && !KRADConstants.QUESTION_REFRESH.equalsIgnoreCase(refreshCaller)) {
                KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
                Document document = docForm.getDocument();
                String docFormKey = docForm.getFormKey();

                UserSession userSession = (UserSession) request.getSession().getAttribute(
                        KRADConstants.USER_SESSION_KEY);

                if (WebUtils.isDocumentSession(document, docForm)) {
                    getSessionDocumentService().setDocumentForm(docForm, userSession, request.getRemoteAddr());
                }

                Boolean exitingDocument = (Boolean) request.getAttribute(KRADConstants.EXITING_DOCUMENT);

                if (exitingDocument != null && exitingDocument) {
                    // remove KualiDocumentFormBase object from session and table.
                    getSessionDocumentService().purgeDocumentForm(docForm.getDocument().getDocumentNumber(),
                            docFormKey, userSession, request.getRemoteAddr());
                }
            }

            if (LOG.isInfoEnabled()) {
                LOG.info(new StringBuffer("Finished processing request: '").append(request.getRequestURI())
                        .append("' w/ query string: '").append(request.getQueryString()).append("'"));
            }
        } finally {
            // MDC docId key is set above, and also during super.process() in the call to processActionForm
            ThreadContext.remove(MDC_DOC_ID);
        }
    }

    @Override
    /* backport FINP-8908 */
    protected boolean processPreprocess(HttpServletRequest request, HttpServletResponse response) {
    	response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    	
        final UserSession session = KRADUtils.getUserSessionFromRequest(request);

        if (session == null) {
            throw new IllegalStateException("the user session has not been established");
        }
        GlobalVariables.setUserSession(session);
        KNSGlobalVariables.clear();
        KNSGlobalVariables.setRemoteIpAddress(request.getRemoteAddr());
        return true;
    }

    /**
     * ProcessDefinition an {@link HttpServletRequest} and create the corresponding {@link HttpServletResponse} or
     * dispatch to another resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @throws IOException      if an input/output error occurs
     * @throws ServletException if a processing exception occurs
     */
    public void strutsProcess(HttpServletRequest request, HttpServletResponse response) throws IOException,
            ServletException {
        // Wrap multipart requests with a special wrapper
        request = processMultipart(request);

        // Identify the path component we will use to select a mapping
        String path = processPath(request, response);
        if (path == null) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing a '" + request.getMethod() + "' for path '" + path + "'");
        }

        // Select a Locale for the current user if requested
        processLocale(request, response);

        // Set the content type and no-caching headers if requested
        processContent(request, response);
        processNoCache(request, response);

        // General purpose preprocessing hook
        if (!processPreprocess(request, response)) {
            return;
        }

        this.processCachedMessages(request, response);

        // Identify the mapping for this request
        ActionMapping mapping = processMapping(request, response, path);
        if (mapping == null) {
            return;
        }

        // Check for any role required to perform this action
        if (!processRoles(request, response, mapping)) {
            return;
        }

        processFormActionAndForward(request, response, mapping);
    }

    public void processFormActionAndForward(final HttpServletRequest request, final HttpServletResponse response,
            final ActionMapping mapping) throws ServletException, IOException {
        ActionForm form = processActionForm(request, response, mapping);
        processPopulate(request, response, form, mapping);

        // need to make sure that we don't check CSRF until after the form is populated so that Struts will parse the
        // multipart parameters into the request if it's a multipart request
        LOG.debug("context path = " + request.getRequestURI());
        if (!getCsrfService().validateCsrfIfNecessary(request, response)) {
            LOG.error("Did not pass CSRF validation");
            return;
        }

        // Create or acquire the Action instance to process this request
        Action action = processActionCreate(request, response, mapping);

        if (action != null) {
            // Call the Action instance itself
            ActionForward forward = processActionPerform(request, response, action, form, mapping);

            if (forward != null) {
                // ProcessDefinition the returned ActionForward instance
                processForwardConfig(request, response, forward);
            }
        }
    }

    /**
     * This method gets the document number from the request. The request should have been processed already before
     * this is called if it is multipart.
     *
     * @param request
     * @return the document number, or null if one can't be found in the request.
     */
    private String getDocumentNumber(HttpServletRequest request) {
        String documentNumber = request.getParameter(KRADConstants.DOCUMENT_DOCUMENT_NUMBER);

        // from lookup pages.
        if (documentNumber == null) {
            documentNumber = request.getParameter(KRADConstants.DOC_NUM);
        }

        if (documentNumber == null) {
            documentNumber = request.getParameter("documentId");
        }

        if (documentNumber == null) {
            documentNumber = request.getParameter("docId");
        }

        return documentNumber;
    }

    /**
     * Hooks into populate process to call form populate method if form is an instanceof PojoForm.
     */
    @Override
    protected void processPopulate(HttpServletRequest request, HttpServletResponse response, ActionForm form,
            ActionMapping mapping) throws ServletException {
        if (form instanceof KualiForm) {
            // Add the ActionForm to GlobalVariables
            // This will allow developers to retrieve both the Document and any request parameters that are not
            // part of the Form and make them available in ValueFinder classes and other places where they are needed.
            KNSGlobalVariables.setKualiForm((KualiForm) form);
        }

        // if not PojoForm, call struts populate
        if (!(form instanceof PojoForm)) {
            super.processPopulate(request, response, form, mapping);
            return;
        }

        final String previousRequestGuid = request.getParameter(
                KualiRequestProcessor.PREVIOUS_REQUEST_EDITABLE_PROPERTIES_GUID_PARAMETER_NAME);

        ((PojoForm) form).clearEditablePropertyInformation();
        ((PojoForm) form).registerStrutsActionMappingScope(mapping.getScope());

        String multipart = mapping.getMultipartClass();
        if (multipart != null) {
            request.setAttribute(Globals.MULTIPART_KEY, multipart);
        }

        form.setServlet(this.servlet);
        form.reset(mapping, request);

        ((PojoForm) form).setPopulateEditablePropertiesGuid(previousRequestGuid);
        // call populate on ActionForm
        ((PojoForm) form).populate(request);
        request.setAttribute("UnconvertedValues", ((PojoForm) form).getUnconvertedValues().keySet());
        request.setAttribute("UnconvertedHash", ((PojoForm) form).getUnconvertedValues());
    }

    /**
     * Hooks into validate to catch any errors from the populate, and translate the ErrorMap to ActionMessages.
     */
    @Override
    protected boolean processValidate(HttpServletRequest request, HttpServletResponse response, ActionForm form,
            ActionMapping mapping) throws IOException, ServletException, InvalidCancelException {
        // skip form validate if we had errors from populate
        if (GlobalVariables.getMessageMap().hasNoErrors()) {
            if (form == null) {
                return true;
            }
            // Was this request cancelled?
            if (request.getAttribute(Globals.CANCEL_KEY) != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(" Cancelled transaction, skipping validation");
                }
                return true;
            }

            // Has validation been turned off for this mapping?
            if (!mapping.getValidate()) {
                return true;
            }

            // call super to call forms validate
            super.processValidate(request, response, form, mapping);
        }

        publishMessages(request);
        if (!GlobalVariables.getMessageMap().hasNoErrors()) {
            // Special handling for multipart request
            if (form.getMultipartRequestHandler() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  Rolling back multipart request");
                }
                form.getMultipartRequestHandler().rollback();
            }

            // Fix state that could be incorrect because of validation failure
            if (form instanceof PojoForm) {
                ((PojoForm) form).processValidationFail();
            }

            // Was an input path (or forward) specified for this mapping?
            String input = mapping.getInput();
            if (input == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("  Validation failed but no input form available");
                }
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        getInternal().getMessage("noInput", mapping.getPath()));
                return false;
            }

            if (moduleConfig.getControllerConfig().getInputForward()) {
                ForwardConfig forward = mapping.findForward(input);
                processForwardConfig(request, response, forward);
            } else {
                internalModuleRelativeForward(input, request, response);
            }

            return false;
        }
        return true;
    }

    /**
     * Checks for return from a lookup or question, and restores the action form stored under the request parameter
     * docFormKey.
     */
    @Override
    protected ActionForm processActionForm(HttpServletRequest request, HttpServletResponse response,
            ActionMapping mapping) {
        String documentNumber = getDocumentNumber(request);
        if (documentNumber != null) {
            ThreadContext.put(MDC_DOC_ID, documentNumber);
        }

        UserSession userSession = (UserSession) request.getSession().getAttribute(KRADConstants.USER_SESSION_KEY);

        String docFormKey = request.getParameter(KRADConstants.DOC_FORM_KEY);
        String methodToCall = request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER);
        String refreshCaller = request.getParameter(KRADConstants.REFRESH_CALLER);
        String documentWebScope = request.getParameter(KRADConstants.DOCUMENT_WEB_SCOPE);

        if (mapping.getPath().startsWith(KRADConstants.REFRESH_MAPPING_PREFIX)
                || KRADConstants.RETURN_METHOD_TO_CALL.equalsIgnoreCase(methodToCall)
                || KRADConstants.QUESTION_REFRESH.equalsIgnoreCase(refreshCaller)
                || KRADConstants.TEXT_AREA_REFRESH.equalsIgnoreCase(refreshCaller)
                || KRADConstants.SESSION_SCOPE.equalsIgnoreCase(documentWebScope)) {
            ActionForm form = null;
            // check for search result storage and clear
            GlobalVariables.getUserSession().removeObjectsByPrefix(KRADConstants.SEARCH_LIST_KEY_PREFIX);

            // We put different type of forms such as document form, lookup form in session but we only store document
            // form in database.
            if (userSession.retrieveObject(docFormKey) != null) {
                LOG.debug("getDecomentForm KualiDocumentFormBase from session");
                form = (ActionForm) userSession.retrieveObject(docFormKey);
            } else if (StringUtils.isNotBlank(documentNumber)) {
                form = getSessionDocumentService().getDocumentForm(documentNumber, docFormKey, userSession,
                        request.getRemoteAddr());
            }
            request.setAttribute(mapping.getAttribute(), form);
            if (!KRADConstants.SESSION_SCOPE.equalsIgnoreCase(documentWebScope)) {
                userSession.removeObject(docFormKey);
            }
            // we should check whether this is a multipart request because we could have had a combination of query
            // parameters and a multipart request
            String contentType = request.getContentType();
            String method = request.getMethod();
            if ("POST".equalsIgnoreCase(method) && contentType != null
                    && contentType.startsWith("multipart/form-data")) {
                // this method parses the multipart request and adds new non-file parameters into the request
                WebUtils.getMultipartParameters(request, null, form, mapping);
            }
            // The form can be null if the document is not a session document
            if (form != null) {
                return form;
            }
        }

        // Kuali Financials has the ability to limit file upload sizes on a per-form basis, so the max upload sizes may
        // be accessed by calling methods on PojoFormBase. This requires that we are able know the file upload size
        // limit (i.e. retrieve a form instance) before we parse a mulitpart request.
        ActionForm form = super.processActionForm(request, response, mapping);

        // for sessiondocument with multipart request
        String contentType = request.getContentType();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method) && StringUtils.isNotBlank(methodToCall) && form instanceof PojoForm
                && ((PojoForm) form).getMethodToCallsToBypassSessionRetrievalForGETRequests().contains(methodToCall)) {
            return createNewActionForm(mapping, request);
        }

        // if we have a multipart request, parse it and return the stored form from session if the doc form key is not
        // blank. If it is blank, then we just return the form generated from the superclass processActionForm method.
        // Either way, we need to parse the mulitpart request now so that we may determine what the value of the doc
        // form key is. This is generally against the contract of processActionForm, because processPopulate should be
        // responsible for parsing the mulitpart request, but we need to parse it now to determine the doc form key value.
        if ("POST".equalsIgnoreCase(method) && contentType != null && contentType.startsWith("multipart/form-data")) {
            WebUtils.getMultipartParameters(request, null, form, mapping);
            docFormKey = request.getParameter(KRADConstants.DOC_FORM_KEY);
            documentWebScope = request.getParameter(KRADConstants.DOCUMENT_WEB_SCOPE);

            documentNumber = getDocumentNumber(request);

            if (KRADConstants.SESSION_SCOPE.equalsIgnoreCase(documentWebScope)
                || form instanceof KualiDocumentFormBase
                && WebUtils.isDocumentSession(((KualiDocumentFormBase) form).getDocument(),
                    (KualiDocumentFormBase) form)) {

                Object userSessionObject = userSession.retrieveObject(docFormKey);
                if (userSessionObject instanceof ActionForm) {
                    LOG.debug("getDocumentForm KualiDocumentFormBase from session");
                    form = (ActionForm) userSessionObject;
                } else {
                    ActionForm tempForm = getSessionDocumentService().getDocumentForm(documentNumber, docFormKey,
                            userSession, request.getRemoteAddr());
                    if (tempForm != null) {
                        form = tempForm;
                    }
                }

                request.setAttribute(mapping.getAttribute(), form);
                if (form != null) {
                    return form;
                }
            }
        }
        return form;
    }

    /**
     * Hook into action perform to handle errors in the error map and catch exceptions.
     * <p>
     * <p>
     * A transaction is started prior to the execution of the action. This allows for the action code to execute
     * efficiently without the need for using PROPAGATION_SUPPORTS in the transaction definitions. The
     * PROPAGATION_SUPPORTS propagation type does not work well with JTA.
     */
    @Override
    protected ActionForward processActionPerform(final HttpServletRequest request, final HttpServletResponse response,
            final Action action, final ActionForm form, final ActionMapping mapping) throws IOException,
            ServletException {
        try {
            TransactionTemplate template = new TransactionTemplate(getTransactionManager());
            ActionForward forward;
            try {
                forward = (ActionForward) template.execute((TransactionCallback) status -> {
                    ActionForward actionForward;
                    try {
                        actionForward = action.execute(mapping, form, request, response);
                        if (GlobalVariables.getMessageMap().containsMessageKey(KFSKeyConstants.ERROR_OPTIMISTIC_LOCK)) {
                            // always rollback if we have an OLE, KualiDocumentActionBase was attempting to swallow
                            // the exception
                            status.setRollbackOnly();
                        }
                    } catch (Exception e) {
                        // the doInTransaction method has no means for throwing exceptions, so we will wrap the
                        // exception in a RuntimeException and re-throw. The one caveat here is that this will
                        // always result in the transaction being rolled back (since WrappedRuntimeException is a
                        // runtime exception).
                        throw new WrappedRuntimeException(e);
                    }
                    if (status.isRollbackOnly()) {
                        // this means that the struts action execution caused the transaction to rollback, we want to
                        // go ahead and trigger the rollback by throwing an exception here but then return the
                        // action forward from this method
                        throw new WrappedActionForwardRuntimeException(actionForward);
                    }
                    return actionForward;
                });
            } catch (WrappedActionForwardRuntimeException e) {
                forward = e.getActionForward();
            }

            publishMessages(request);
            saveMessages(request);

            if (form instanceof PojoForm) {
                if (((PojoForm) form).getEditableProperties() == null
                        || ((PojoForm) form).getEditableProperties().isEmpty()) {
                    EditablePropertiesHistoryHolder holder = (EditablePropertiesHistoryHolder) GlobalVariables
                            .getUserSession().getObjectMap().get(
                                    KRADConstants.EDITABLE_PROPERTIES_HISTORY_HOLDER_ATTR_NAME);
                    if (holder == null) {
                        holder = new EditablePropertiesHistoryHolder();
                    }

                    final String guid = holder.addEditablePropertiesToHistory(
                            ((PojoForm) form).getEditableProperties());
                    ((PojoForm) form).setActionEditablePropertiesGuid(guid);
                    GlobalVariables.getUserSession().addObject(
                            KRADConstants.EDITABLE_PROPERTIES_HISTORY_HOLDER_ATTR_NAME, holder);
                }
            }
            return forward;
        } catch (Exception e) {
            if (e instanceof WrappedRuntimeException) {
                e = (Exception) e.getCause();
            }
            if (e instanceof ValidationException) {
                // add a generic error message if there are none
                if (GlobalVariables.getMessageMap().hasNoErrors()) {
                    GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_CUSTOM,
                            e.getMessage());
                }

                if (form instanceof PojoForm) {
                    if (((PojoForm) form).getEditableProperties() == null
                            || ((PojoForm) form).getEditableProperties().isEmpty()) {
                        EditablePropertiesHistoryHolder holder = (EditablePropertiesHistoryHolder) GlobalVariables
                                .getUserSession().getObjectMap().get(
                                        KRADConstants.EDITABLE_PROPERTIES_HISTORY_HOLDER_ATTR_NAME);
                        if (holder == null) {
                            holder = new EditablePropertiesHistoryHolder();
                        }

                        final String guid = holder.addEditablePropertiesToHistory(
                                ((PojoForm) form).getEditableProperties());
                        ((PojoForm) form).setActionEditablePropertiesGuid(guid);
                        GlobalVariables.getUserSession().addObject(
                                KRADConstants.EDITABLE_PROPERTIES_HISTORY_HOLDER_ATTR_NAME, holder);
                    }
                }
                // display error messages and return to originating page
                publishMessages(request);
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }

            publishMessages(request);
            return processException(request, response, e, form, mapping);
        }
    }

    /**
     * Adds more detailed logging for unhandled exceptions
     */
    @Override
    protected ActionForward processException(HttpServletRequest request, HttpServletResponse response,
            Exception exception, ActionForm form, ActionMapping mapping) throws IOException, ServletException {
        ActionForward actionForward;

        try {
            actionForward = super.processException(request, response, exception, form, mapping);
        } catch (IOException e) {
            logException(e);
            throw e;
        } catch (ServletException e) {
            // special case, to make OptimisticLockExceptions easier to read
            Throwable rootCause = e.getRootCause();
            if (rootCause instanceof OjbOperationException) {
                OjbOperationException ooe = (OjbOperationException) rootCause;

                Throwable subcause = ooe.getCause();
                if (subcause instanceof OptimisticLockException) {
                    OptimisticLockException ole = (OptimisticLockException) subcause;

                    StringBuffer message = new StringBuffer(e.getMessage());

                    Object sourceObject = ole.getSourceObject();
                    if (sourceObject != null) {
                        message.append(" (sourceObject is ");
                        message.append(sourceObject.getClass().getName());
                        message.append(")");
                    }

                    e = new ServletException(message.toString(), rootCause);
                }
            }

            logException(e);
            throw e;
        }
        return actionForward;
    }

    private void logException(Exception e) {
        LOG.error("unhandled exception thrown by KualiRequestProcessor.processActionPerform", e);
    }

    /**
     * Checks for errors in the error map and transforms them to struts action messages then stores in the request.
     */
    private void publishMessages(HttpServletRequest request) {
        MessageMap errorMap = GlobalVariables.getMessageMap();
        if (!errorMap.hasNoErrors()) {
            ErrorContainer errorContainer = new ErrorContainer(errorMap);

            request.setAttribute("ErrorContainer", errorContainer);
            request.setAttribute(Globals.ERROR_KEY, errorContainer.getRequestErrors());
            request.setAttribute("ErrorPropertyList", errorContainer.getErrorPropertyList());
        }

        if (errorMap.hasWarnings()) {
            WarningContainer warningsContainer = new WarningContainer(errorMap);

            request.setAttribute("WarningContainer", warningsContainer);
            request.setAttribute("WarningActionMessages", warningsContainer.getRequestMessages());
            request.setAttribute("WarningPropertyList", warningsContainer.getMessagePropertyList());
        }

        if (errorMap.hasInfo()) {
            InfoContainer infoContainer = new InfoContainer(errorMap);

            request.setAttribute("InfoContainer", infoContainer);
            request.setAttribute("InfoActionMessages", infoContainer.getRequestMessages());
            request.setAttribute("InfoPropertyList", infoContainer.getMessagePropertyList());
        }
    }

    /**
     * Checks for messages in GlobalVariables and places list in request attribute.
     */
    private void saveMessages(HttpServletRequest request) {
        if (!KNSGlobalVariables.getMessageList().isEmpty()) {
            request.setAttribute(KRADConstants.GLOBAL_MESSAGES, KNSGlobalVariables.getMessageList().toActionMessages());
        }
    }

    public SessionDocumentService getSessionDocumentService() {
        if (sessionDocumentService == null) {
            sessionDocumentService = KNSServiceLocator.getSessionDocumentService();
        }
        return this.sessionDocumentService;
    }

    public PlatformTransactionManager getTransactionManager() {
        if (transactionManager == null) {
            transactionManager = KRADServiceLocatorInternal.getTransactionManager();
        }
        return this.transactionManager;
    }

    public CsrfService getCsrfService() {
        if (csrfService == null) {
            csrfService = KRADServiceLocatorWeb.getCsrfService();
        }
        return csrfService;
    }

    private ActionForm createNewActionForm(ActionMapping mapping, HttpServletRequest request) {
        String name = mapping.getName();
        FormBeanConfig config = moduleConfig.findFormBeanConfig(name);
        if (config == null) {
            LOG.warn("No FormBeanConfig found under '" + name + "'");
            return null;
        }
        ActionForm instance = RequestUtils.createActionForm(config, servlet);
        if ("request".equals(mapping.getScope())) {
            request.setAttribute(mapping.getAttribute(), instance);
        } else {
            HttpSession session = request.getSession();
            session.setAttribute(mapping.getAttribute(), instance);
        }
        return instance;
    }

    private static class WrappedActionForwardRuntimeException extends RuntimeException {
        private final ActionForward actionForward;

        WrappedActionForwardRuntimeException(ActionForward actionForward) {
            this.actionForward = actionForward;
        }

        public ActionForward getActionForward() {
            return actionForward;
        }
    }

    /**
     * A simple exception that allows us to wrap an exception that is thrown out of a transaction template.
     */
    @SuppressWarnings("serial")
    private static class WrappedRuntimeException extends RuntimeException {
        WrappedRuntimeException(Exception e) {
            super(e);
        }
    }
}
