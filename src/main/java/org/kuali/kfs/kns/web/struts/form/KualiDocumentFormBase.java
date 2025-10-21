/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kns.web.struts.form;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.web.format.NoOpStringFormatter;
import org.kuali.kfs.core.web.format.TimestampAMPMFormatter;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kew.api.document.DocumentStatus;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.engine.node.RouteNodeInstance;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.datadictionary.HeaderNavigation;
import org.kuali.kfs.kns.inquiry.Inquirable;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.derivedvaluesetter.DerivedValuesSetter;
import org.kuali.kfs.kns.web.ui.HeaderField;
import org.kuali.kfs.krad.UserSessionUtils;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.AdHocRouteWorkgroup;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.springframework.util.AutoPopulatingList;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * This class is the base action form for all documents.
 */
public abstract class KualiDocumentFormBase extends KualiForm implements Serializable {

    private static final long serialVersionUID = 916061016201941821L;

    private static final Logger LOG = LogManager.getLogger();

    private static final String ERROR_DOCUMENT_ANNOTATION_MAX_LENGTH_EXCEEDED =
            "error.document.annotation.maxLength.exceeded";

    private Document document;
    private String annotation = "";
    private String command;

    private String docId;
    private String docTypeName;

    private List<String> additionalScriptFiles;

    private AdHocRoutePerson newAdHocRoutePerson;
    private AdHocRouteWorkgroup newAdHocRouteWorkgroup;

    private Note newNote;

    //TODO: is this still needed? I think it's obsolete now
    private List boNotes;

    protected FormFile attachmentFile = new BlankFormFile();

    protected Map editingMode;
    protected Map documentActions;
    protected boolean suppressAllButtons;

    protected Map adHocActionRequestCodes;
    private boolean returnToActionList;

    // for session enhancement
    private String formKey;
    private String docNum;

    private List<ActionRequest> actionRequests;
    private List<String> selectedActionRequests;
    private String superUserAnnotation;

    private String lastActionTaken;

    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private DocumentDictionaryService documentDictionaryService;
    private UniversityDateService universityDateService;

    /*
       Stores the error map from previous requests, so that we can continue to display error messages displayed during
       a previous request
     */
    private MessageMap errorMapFromPreviousRequest;

    // private fields for superuser checks
    private DocumentType documentType;
    private boolean superuserForDocumentType;
    private String documentStatus;
    private List<RouteNodeInstance> routeNodeInstances;
    private boolean superUserFieldsInitialized;

    private String documentId;
    private boolean showFuture;
    private String showFutureError;
    private boolean removeHeader;
    private boolean lookFuture;
    private boolean showNotes;
    private String returnUrlLocation;
    private boolean showCloseButton;
    private boolean showBackButton;
    private int internalNavCount;

    @SuppressWarnings("unchecked")
    public KualiDocumentFormBase() {
        super();

        instantiateDocument();
        newNote = new Note();
        editingMode = new HashMap();
        additionalScriptFiles = new AutoPopulatingList<>(String.class);

        // set the initial record for persons up
        newAdHocRoutePerson = new AdHocRoutePerson();

        // set the initial record for workgroups up
        newAdHocRouteWorkgroup = new AdHocRouteWorkgroup();

        // to make sure it posts back the correct time
        setFormatterType("document.documentHeader.note.finDocNotePostedDttmStamp", TimestampAMPMFormatter.class);
        setFormatterType("document.documentHeader.note.attachment.finDocNotePostedDttmStamp",
                TimestampAMPMFormatter.class);
        //overriding note formatter to make sure they post back the full timestamp
        setFormatterType("document.documentHeader.boNote.notePostedTimestamp", TimestampAMPMFormatter.class);
        setFormatterType("document.documentBusinessObject.boNote.notePostedTimestamp",
                TimestampAMPMFormatter.class);

        setFormatterType("editingMode", NoOpStringFormatter.class);
        setFormatterType("editableAccounts", NoOpStringFormatter.class);

        setDocumentActions(new HashMap());
        suppressAllButtons = false;

        initializeHeaderNavigationTabs();
    }

    @Override
    public void addRequiredNonEditableProperties() {
        super.addRequiredNonEditableProperties();
        registerRequiredNonEditableProperty(KRADConstants.DOCUMENT_TYPE_NAME);
        registerRequiredNonEditableProperty(KRADConstants.FORM_KEY);
        registerRequiredNonEditableProperty(KRADConstants.NEW_NOTE_NOTE_TYPE_CODE);
    }

    public String getDocNum() {
        return docNum;
    }

    public void setDocNum(final String docNum) {
        this.docNum = docNum;
    }

    /**
     * Setup workflow doc in the document.
     */
    @Override
    public void populate(final HttpServletRequest request) {
        super.populate(request);

        clearFieldsForSuperUserChecks();
        WorkflowDocument workflowDocument = null;

        if (hasDocumentId()) {
            // populate workflowDocument in documentHeader, if needed
            // KULRICE-4444 Obtain Document Header using the Workflow Service to minimize overhead
            workflowDocument = UserSessionUtils.getWorkflowDocument(GlobalVariables.getUserSession(),
                    getDocument().getDocumentNumber());
            if (workflowDocument == null) {
                // gets the workflow document from doc service, doc service will also set the workflow document
                // in the user's session
                Person person = GlobalVariables.getUserSession().getPerson();
                if (ObjectUtils.isNull(person)) {
                    person = KimApiServiceLocator.getPersonService().getPersonByPrincipalName(
                            KRADConstants.SYSTEM_USER);
                }
                workflowDocument = KewApiServiceLocator.getWorkflowDocumentService()
                        .loadWorkflowDocument(getDocument().getDocumentNumber(), person);
                UserSessionUtils.addWorkflowDocument(GlobalVariables.getUserSession(), workflowDocument);
                if (workflowDocument == null) {
                    throw new RuntimeException("Unable to retrieve workflow document # " +
                            getDocument().getDocumentNumber() +
                            " from workflow document service createWorkflowDocument");
                } else {
                    LOG.debug("Retrieved workflow Document ID: {}", workflowDocument::getDocumentId);
                }
            }

            getDocument().getDocumentHeader().setWorkflowDocument(workflowDocument);
        }
        if (workflowDocument != null) {
            //Populate Document Header attributes
            populateHeaderFields(workflowDocument);
        }
        showFuture = Boolean.valueOf(getParameter(request, "showFuture"));
    }

    protected String getPersonInquiryUrl(final Person user) {
        if (user != null) {
            final Class<Inquirable> inquirableClass = getBusinessObjectDictionaryService()
                    .getInquirableClass(Person.class);
            final Inquirable inquirable;
            try {
                inquirable = inquirableClass.newInstance();

                final HtmlData.AnchorHtmlData inquiryUrl = (HtmlData.AnchorHtmlData) inquirable.getInquiryUrl(user,
                        KimConstants.AttributeConstants.PRINCIPAL_ID, true);
                return inquiryUrl.getHref();
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.warn("Unable to get inquirable for person. The inquiry url in the header isn't gonna work.");
            }
        }
        return null;
    }

    protected String getPersonInquiryUrlLink(final Person user, final String linkBody) {
        final StringBuffer urlBuffer = new StringBuffer();

        final String personInquiryUrl = getPersonInquiryUrl(user);
        if (personInquiryUrl != null && StringUtils.isNotEmpty(linkBody) &&
                !StringUtils.equals(KimConstants.EntityTypes.SYSTEM, user.getEntityTypeCode())) {
            urlBuffer.append("<a href='")
                .append(KRADServiceLocator.getKualiConfigurationService()
                        .getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY))
                .append("/" + personInquiryUrl)
                .append("&mode=modal")
                .append("' ")
                .append("data-remodal-target='modal'")
                .append(" ")
                .append("title='Person Inquiry'>")
                .append(linkBody)
                .append("</a>");
        } else {
            urlBuffer.append(linkBody);
        }
        return urlBuffer.toString();
    }

    protected String getDocumentHandlerUrl(final String documentId) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(KRADConstants.PARAMETER_DOC_ID, documentId);
        parameters.put(KRADConstants.PARAMETER_COMMAND, KRADConstants.METHOD_DISPLAY_DOC_SEARCH_VIEW);
        return UrlFactory.parameterizeUrl(
            KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY) + "/" + KRADConstants.DOC_HANDLER_ACTION, parameters);
    }

    protected String buildHtmlLink(final String url, final String linkBody) {
        final StringBuffer urlBuffer = new StringBuffer();

        if (StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(linkBody)) {
            urlBuffer.append("<a href='").append(url).append("'>").append(linkBody).append("</a>");
        }

        return urlBuffer.toString();
    }

    /**
     * This method is used to populate the list of header field objects (see {@link KualiForm#getDocInfo()}) displayed
     * on the Kuali document form display pages.
     *
     * @param workflowDocument the workflow document of the document being displayed (null is allowed)
     */
    public void populateHeaderFields(final WorkflowDocument workflowDocument) {
        getDocInfo().clear();
        getDocInfo().addAll(getStandardHeaderFields(workflowDocument));
    }

    /**
     * This method returns a list of {@link HeaderField} objects that are used by default on Kuali document display
     * pages. To use this list and override an individual {@link HeaderField} object the id constants from
     * {@link KRADConstants.DocumentFormHeaderFieldIds} can be used to identify items from the list.
     *
     * @param workflowDocument - the workflow document of the document being displayed (null is allowed)
     * @return a list of the standard fields displayed by default for all Kuali documents
     */
    protected List<HeaderField> getStandardHeaderFields(final WorkflowDocument workflowDocument) {
        final List<HeaderField> headerFields = new ArrayList<>();
        setNumColumns(2);
        // check for a document template number as that will dictate column numbering
        HeaderField docTemplateNumber = null;
        if (ObjectUtils.isNotNull(getDocument()) && ObjectUtils.isNotNull(getDocument().getDocumentHeader())
            && StringUtils.isNotBlank(getDocument().getDocumentHeader().getDocumentTemplateNumber())) {
            final String templateDocumentNumber = getDocument().getDocumentHeader().getDocumentTemplateNumber();
            docTemplateNumber = new HeaderField(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_TEMPLATE_NUMBER,
                    "DataDictionary.DocumentHeader.attributes.documentTemplateNumber",
                templateDocumentNumber, buildHtmlLink(getDocumentHandlerUrl(templateDocumentNumber),
                    templateDocumentNumber));
        }
        //Document Number
        final HeaderField docNumber = new HeaderField("DataDictionary.DocumentHeader.attributes.documentNumber",
                workflowDocument != null ? getDocument().getDocumentNumber() : null);
        docNumber.setId(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_NUMBER);
        final HeaderField docStatus = new HeaderField(
                "DataDictionary.AttributeReferenceDummy.attributes.workflowDocumentStatus",
                workflowDocument != null ? workflowDocument.getStatus().getLabel() : null);
        docStatus.setId(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_WORKFLOW_STATUS);
        String initiatorNetworkId = null;
        Person user = null;
        if (workflowDocument != null) {
            user = getInitiator();
            if (user == null) {
                LOG.warn("User Not Found while attempting to build inquiry link for document header fields");
            } else {
                initiatorNetworkId = user.getPrincipalName();
            }
        }
        final String inquiryUrl = getPersonInquiryUrlLink(user, workflowDocument != null ? initiatorNetworkId : null);

        final HeaderField docInitiator = new HeaderField(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_INITIATOR,
                "DataDictionary.AttributeReferenceDummy.attributes.initiatorNetworkId",
            workflowDocument != null ? initiatorNetworkId : null, workflowDocument != null ? inquiryUrl : null);

        String createDateStr = null;
        if (workflowDocument != null && workflowDocument.getDateCreated() != null) {
            final DateTimeService dateTimeService = CoreApiServiceLocator.getDateTimeService();
            final Date dateCreated = dateTimeService.getUtilDate(workflowDocument.getDateCreated());
            createDateStr = CoreApiServiceLocator.getDateTimeService().toString(dateCreated, "hh:mm a MM/dd/yyyy");
        }

        final HeaderField docCreateDate = new HeaderField("DataDictionary.AttributeReferenceDummy.attributes.createDate",
                createDateStr);
        docCreateDate.setId(KRADConstants.DocumentFormHeaderFieldIds.DOCUMENT_CREATE_DATE);
        if (ObjectUtils.isNotNull(docTemplateNumber)) {
            setNumColumns(3);
        }

        headerFields.add(docNumber);
        headerFields.add(docStatus);
        if (ObjectUtils.isNotNull(docTemplateNumber)) {
            headerFields.add(docTemplateNumber);
        }
        headerFields.add(docInitiator);
        headerFields.add(docCreateDate);
        if (ObjectUtils.isNotNull(docTemplateNumber)) {
            // adding an empty field so implementors do not have to worry about additional fields being put on the
            // wrong row
            headerFields.add(HeaderField.EMPTY_FIELD);
        }
        return headerFields;
    }

    @Override
    public ActionErrors validate(final ActionMapping mapping, final HttpServletRequest request) {
        // check that annotation does not exceed 2000 characters
        setAnnotation(StringUtils.stripToNull(getAnnotation()));
        final int diff = StringUtils.defaultString(getAnnotation()).length() - KRADConstants.DOCUMENT_ANNOTATION_MAX_LENGTH;
        if (diff > 0) {
            GlobalVariables.getMessageMap().putError("annotation",
                    ERROR_DOCUMENT_ANNOTATION_MAX_LENGTH_EXCEEDED,
                    Integer.toString(KRADConstants.DOCUMENT_ANNOTATION_MAX_LENGTH), Integer.toString(diff));
        }
        return super.validate(mapping, request);
    }

    /**
     * @return true if this document was properly initialized with a DocumentHeader and related KualiWorkflowDocument
     */
    public final boolean isFormDocumentInitialized() {
        boolean initialized = false;

        if (document != null) {
            if (document.getDocumentHeader() != null) {
                initialized = document.getDocumentHeader().hasWorkflowDocument();
            }
        }

        return initialized;
    }

    /**
     * @return Map of editingModes for this document, as set during the most recent call to
     * populate(javax.servlet.http.HttpServletRequest)
     */
    @SuppressWarnings("unchecked")
    public Map getEditingMode() {
        return editingMode;
    }

    @SuppressWarnings("unchecked")
    public void setEditingMode(final Map editingMode) {
        this.editingMode = editingMode;
    }

    @SuppressWarnings("unchecked")
    public Map getDocumentActions() {
        return documentActions;
    }

    @SuppressWarnings("unchecked")
    public void setDocumentActions(final Map documentActions) {
        this.documentActions = documentActions;
    }

    @SuppressWarnings("unchecked")
    public void setAdHocActionRequestCodes(final Map adHocActionRequestCodes) {
        this.adHocActionRequestCodes = adHocActionRequestCodes;
    }

    /**
     * @return a map of the possible action request codes that takes into account the users context on the document
     */
    @SuppressWarnings("unchecked")
    public Map getAdHocActionRequestCodes() {
        return adHocActionRequestCodes;
    }

    public List<AdHocRoutePerson> getAdHocRoutePersons() {
        return document.getAdHocRoutePersons();
    }

    public FormFile getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(final FormFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public void setAdHocRoutePersons(final List<AdHocRoutePerson> adHocRouteRecipients) {
        document.setAdHocRoutePersons(adHocRouteRecipients);
    }

    public List<AdHocRouteWorkgroup> getAdHocRouteWorkgroups() {
        return document.getAdHocRouteWorkgroups();
    }

    public void setAdHocRouteWorkgroups(final List<AdHocRouteWorkgroup> adHocRouteWorkgroups) {
        document.setAdHocRouteWorkgroups(adHocRouteWorkgroups);
    }

    /**
     * Special getter based on index to work with multi rows for ad hoc routing to persons struts page
     *
     * @param index
     * @return
     */
    public AdHocRoutePerson getAdHocRoutePerson(final int index) {
        while (getAdHocRoutePersons().size() <= index) {
            getAdHocRoutePersons().add(new AdHocRoutePerson());
        }
        return getAdHocRoutePersons().get(index);
    }

    /**
     * Special getter based on index to work with multi rows for ad hoc routing to workgroups struts page
     *
     * @param index
     * @return
     */
    public AdHocRouteWorkgroup getAdHocRouteWorkgroup(final int index) {
        while (getAdHocRouteWorkgroups().size() <= index) {
            getAdHocRouteWorkgroups().add(new AdHocRouteWorkgroup());
        }
        return getAdHocRouteWorkgroups().get(index);
    }

    public AdHocRoutePerson getNewAdHocRoutePerson() {
        return newAdHocRoutePerson;
    }

    public void setNewAdHocRoutePerson(final AdHocRoutePerson newAdHocRoutePerson) {
        this.newAdHocRoutePerson = newAdHocRoutePerson;
    }

    public AdHocRouteWorkgroup getNewAdHocRouteWorkgroup() {
        return newAdHocRouteWorkgroup;
    }

    public void setNewAdHocRouteWorkgroup(final AdHocRouteWorkgroup newAdHocRouteWorkgroup) {
        this.newAdHocRouteWorkgroup = newAdHocRouteWorkgroup;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(final Document document) {
        this.document = document;
        if (document != null && StringUtils.isNotEmpty(document.getDocumentNumber())) {
            populateHeaderFields(document.getDocumentHeader().getWorkflowDocument());
        }
    }

    public WorkflowDocument getWorkflowDocument() {
        return getDocument().getDocumentHeader().getWorkflowDocument();
    }

    /**
     * Null-safe check to see if the workflow document object exists before attempting to retrieve it.
     * (Which, if called, will throw an exception.)
     */
    public boolean isHasWorkflowDocument() {
        if (getDocument() == null || getDocument().getDocumentHeader() == null) {
            return false;
        }
        return getDocument().getDocumentHeader().hasWorkflowDocument();
    }

    /*
     * TODO rk implemented to account for caps coming from kuali user service from workflow
     */
    public boolean isUserDocumentInitiator() {
        if (getWorkflowDocument() != null) {
            return getWorkflowDocument().getInitiatorPrincipalId().equalsIgnoreCase(
                GlobalVariables.getUserSession().getPrincipalId());
        }
        return false;
    }

    public Person getInitiator() {
        final String initiatorPrincipalId = getWorkflowDocument().getInitiatorPrincipalId();
        return KimApiServiceLocator.getPersonService().getPerson(initiatorPrincipalId);
    }

    /**
     * @return true if the workflowDocument associated with this form is currently enroute
     */
    public boolean isDocumentEnRoute() {
        return getWorkflowDocument().isEnroute();
    }

    public void setAnnotation(final String annotation) {
        this.annotation = annotation;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(final String docId) {
        this.docId = docId;
    }

    public String getDocTypeName() {
        return docTypeName;
    }

    public void setDocTypeName(final String docTypeName) {
        this.docTypeName = docTypeName;
    }

    public String getInitiatorNetworkId() {
        return getWorkflowDocument().getInitiatorPrincipalId();
    }

    public final boolean isSuppressAllButtons() {
        return suppressAllButtons;
    }

    public final void setSuppressAllButtons(final boolean suppressAllButtons) {
        this.suppressAllButtons = suppressAllButtons;
    }

    /**
     * @return true if this form's getDocument() method returns a Document, and if that Document's
     *         getDocumentHeaderId method returns a non-null
     */
    public boolean hasDocumentId() {
        boolean hasDocId = false;

        final Document d = getDocument();
        if (d != null) {
            final String docHeaderId = d.getDocumentNumber();

            hasDocId = StringUtils.isNotBlank(docHeaderId);
        }

        return hasDocId;
    }

    /**
     * Sets flag indicating whether upon completion of approve, blanketApprove, cancel, or disapprove, the user should
     * be returned to the actionList instead of to the portal
     *
     * @param returnToActionList
     */
    public void setReturnToActionList(final boolean returnToActionList) {
        this.returnToActionList = returnToActionList;
    }

    public boolean isReturnToActionList() {
        return returnToActionList;
    }

    public List<String> getAdditionalScriptFiles() {
        return additionalScriptFiles;
    }

    public void setAdditionalScriptFiles(final List<String> additionalScriptFiles) {
        this.additionalScriptFiles = additionalScriptFiles;
    }

    public void setAdditionalScriptFile(final int index, final String scriptFile) {
        additionalScriptFiles.set(index, scriptFile);
    }

    public String getAdditionalScriptFile(final int index) {
        return additionalScriptFiles.get(index);
    }

    public Note getNewNote() {
        return newNote;
    }

    public void setNewNote(final Note newNote) {
        this.newNote = newNote;
    }

    @SuppressWarnings("unchecked")
    public List getBoNotes() {
        return boNotes;
    }

    @SuppressWarnings("unchecked")
    public void setBoNotes(final List boNotes) {
        this.boNotes = boNotes;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(final String formKey) {
        this.formKey = formKey;
    }

    @Override
    public void reset(final ActionMapping mapping, final HttpServletRequest request) {
        super.reset(mapping, request);
        setMethodToCall(null);
        setRefreshCaller(null);
        setAnchor(null);
        setCurrentTabIndex(0);
        setSelectedActionRequests(new ArrayList<>());
    }

    /**
     * Adds the attachment file size to the list of max file sizes.
     */
    @Override
    protected void customInitMaxUploadSizes() {
        super.customInitMaxUploadSizes();
        final String attachmentSize = getParameterService().getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, KRADConstants.DetailTypes.DOCUMENT_DETAIL_TYPE,
                KRADConstants.ATTACHMENT_FILE_SIZE
        );
        if (StringUtils.isNotBlank(attachmentSize)) {
            addMaxUploadSize(attachmentSize);
        }
    }

    /**
     * IMPORTANT: any overrides of this method must ensure that nothing in the HTTP request will be used to determine
     * whether document is in session
     */
    @Override
    public boolean shouldPropertyBePopulatedInForm(final String requestParameterName, final HttpServletRequest request) {
        // CU Customization: Exclude "additionalScriptFiles" and "additionalScriptFile[x]" from being editable.
        if (StringUtils.startsWithIgnoreCase(requestParameterName, CUKFSPropertyConstants.ADDITIONAL_SCRIPT_FILE_PREFIX)) {
            return false;
        }
        // End CU Customization

        for (final String prefix : KRADConstants.ALWAYS_VALID_PARAMETER_PREFIXES) {
            if (requestParameterName.startsWith(prefix)) {
                return true;
            }
        }

        if (StringUtils.equalsIgnoreCase(getMethodToCall(), KRADConstants.DOC_HANDLER_METHOD)) {
            return true;
        }
        if (WebUtils.isDocumentSession(getDocument(), this)) {
            return isPropertyEditable(requestParameterName) || isPropertyNonEditableButRequired(requestParameterName);
        }
        return true;
    }

    @Override
    public boolean shouldMethodToCallParameterBeUsed(
            final String methodToCallParameterName, final String methodToCallParameterValue,
            final HttpServletRequest request) {
        if (StringUtils.equals(methodToCallParameterName, KRADConstants.DISPATCH_REQUEST_PARAMETER) &&
            StringUtils.equals(methodToCallParameterValue, KRADConstants.DOC_HANDLER_METHOD)) {
            return true;
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName,
            methodToCallParameterValue, request);
    }

    public MessageMap getMessageMapFromPreviousRequest() {
        return errorMapFromPreviousRequest;
    }

    public void setMessageMapFromPreviousRequest(final MessageMap errorMapFromPreviousRequest) {
        this.errorMapFromPreviousRequest = errorMapFromPreviousRequest;
    }

    @Override
    public void setDerivedValuesOnForm(final HttpServletRequest request) {
        super.setDerivedValuesOnForm(request);

        final String docTypeName = getDocTypeName();
        if (StringUtils.isNotBlank(docTypeName)) {
            final Class<? extends DerivedValuesSetter> derivedValuesSetterClass;
            final DocumentEntry documentEntry = getDocumentDictionaryService().getDocumentEntry(docTypeName);
            derivedValuesSetterClass = documentEntry.getDerivedValuesSetterClass();

            if (derivedValuesSetterClass != null) {
                final DerivedValuesSetter derivedValuesSetter;
                try {
                    derivedValuesSetter = derivedValuesSetterClass.newInstance();
                } catch (final Exception e) {
                    LOG.error("Unable to instantiate class {}", derivedValuesSetterClass::getName, () -> e);
                    throw new RuntimeException("Unable to instantiate class " + derivedValuesSetterClass.getName(), e);
                }
                derivedValuesSetter.setDerivedValues(this, request);
            }
        }
    }

    protected String getDefaultDocumentTypeName() {
        return "";
    }

    /**
     * will instantiate a new document setting it on the form if
     * {@link KualiDocumentFormBase#getDefaultDocumentTypeName()} is overridden to return a valid value.
     */
    protected void instantiateDocument() {
        if (document == null && StringUtils.isNotBlank(getDefaultDocumentTypeName())) {
            final Class<? extends Document> documentClass = getDocumentClass();
            try {
                final Document document = documentClass.newInstance();
                setDocument(document);
            } catch (final Exception e) {
                LOG.error(
                        "Unable to instantiate document class {} document type {}",
                        documentClass::getName,
                        this::getDefaultDocumentTypeName
                );
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return the document class from the data dictionary if {@link KualiDocumentFormBase#getDefaultDocumentTypeName()}
     * is overridden to return a valid value otherwise behavior is nondeterministic.
     */
    private Class<? extends Document> getDocumentClass() {
        return KNSServiceLocator.getDataDictionaryService().getDocumentClassByTypeName(getDefaultDocumentTypeName());
    }

    /**
     * initializes the header tabs from what is defined in the datadictionary if
     * {@link KualiDocumentFormBase#getDefaultDocumentTypeName()} is overridden to return a valid value.
     */
    protected void initializeHeaderNavigationTabs() {
        if (StringUtils.isNotBlank(getDefaultDocumentTypeName())) {
            final DocumentEntry docEntry = getDocumentDictionaryService().getDocumentEntry(
                    getDocumentClass().getName());
            final List<HeaderNavigation> navList = docEntry.getHeaderNavigationList();
            final HeaderNavigation[] list = new HeaderNavigation[navList.size()];
            super.setHeaderNavigationTabs(navList.toArray(list));
        }
    }

    public List<ActionRequest> getActionRequests() {
        return actionRequests;
    }

    public void setActionRequests(final List<ActionRequest> actionRequests) {
        this.actionRequests = actionRequests;
    }

    public List<String> getSelectedActionRequests() {
        return selectedActionRequests;
    }

    public void setSelectedActionRequests(final List<String> selectedActionRequests) {
        this.selectedActionRequests = selectedActionRequests;
    }

    public List<ActionRequest> getActionRequestsRequiringApproval() {
        final List<ActionRequest> actionRequests = getActionRequests();
        final List<ActionRequest> actionRequestsApprove = new ArrayList<>();

        for (final ActionRequest actionRequest : actionRequests) {
            if (actionRequest.isApproveRequest() || actionRequest.isCompleteRequest()) {
                actionRequestsApprove.add(actionRequest);
            }
        }
        return actionRequestsApprove;
    }

    public String getSuperUserAnnotation() {
        return superUserAnnotation;
    }

    public void setSuperUserAnnotation(final String superUserAnnotation) {
        this.superUserAnnotation = superUserAnnotation;
    }

    public boolean isSuperUserActionAvaliable() {
        final List<ActionRequest> actionRequests = getActionRequestsRequiringApproval();
        boolean canSuperUserApprove = false;
        boolean canSuperUserDisapprove = false;

        initializeFieldsForSuperUserChecks();
        final boolean hasSingleActionToTake = isSuperUserApproveSingleActionRequestAuthorized_preInitialized()
                                              && isStateAllowsApproveSingleActionRequest() && !actionRequests.isEmpty();
        if (!hasSingleActionToTake) {
            canSuperUserApprove = isSuperUserApproveDocumentAuthorized_preInitialized()
                    && isStateAllowsApproveOrDisapprove();
        }
        if (!canSuperUserApprove) {
            canSuperUserDisapprove = isSuperUserDisapproveDocumentAuthorized_preInitialized()
                    && isStateAllowsApproveOrDisapprove();
        }

        return hasSingleActionToTake || canSuperUserApprove || canSuperUserDisapprove;
    }

    private void initializeFieldsForSuperUserChecks() {
        if (!superUserFieldsInitialized) {
            final String principalId = GlobalVariables.getUserSession().getPrincipalId();
            final String docId = getDocId();

            documentType = KEWServiceLocator.getDocumentTypeService().getDocumentTypeByName(docTypeName);
            String docTypeId = null;
            if (documentType != null) {
                docTypeId = documentType.getId();
            }

            routeNodeInstances = KewApiServiceLocator.getWorkflowDocumentService().getCurrentRouteNodeInstances(docId);

            documentStatus = getDocument().getDocumentHeader().getWorkflowDocument().getStatus().getCode();
            superuserForDocumentType = KEWServiceLocator.getDocumentTypeService().isSuperUserForDocumentTypeId(
                    principalId, docTypeId);

            superUserFieldsInitialized = true;
        }
    }

    private void clearFieldsForSuperUserChecks() {
        documentType = null;
        routeNodeInstances = null;
        documentStatus = null;
        superuserForDocumentType = false;
        superUserFieldsInitialized = false;
    }

    public boolean isSuperUserApproveSingleActionRequestAuthorized() {
        initializeFieldsForSuperUserChecks();
        return isSuperUserApproveSingleActionRequestAuthorized_preInitialized();
    }

    // this ridiculousness was done to reduce db calls. the calling method in this class was calling initialize over
    // and over again -- which is expensive. I wanted to remove these methods entirely (in favor of one well done
    // method) but struts is using them to construct the dang tag. When we remove struts kill these double methods.
    private boolean isSuperUserApproveSingleActionRequestAuthorized_preInitialized() {
        if (superuserForDocumentType) {
            return true;
        }

        final String principalId = GlobalVariables.getUserSession().getPrincipalId();
        return KEWServiceLocator.getDocumentTypeService().canSuperUserApproveSingleActionRequest(
            principalId, getDocTypeName(), routeNodeInstances, documentStatus);
    }

    public boolean isSuperUserApproveDocumentAuthorized() {
        initializeFieldsForSuperUserChecks();
        return isSuperUserApproveDocumentAuthorized_preInitialized();
    }

    // this ridiculousness was done to reduce db calls. the calling method in this class was calling initialize over
    // and over again -- which is expensive. I wanted to remove these methods entirely (in favor of one well done
    // method) but struts is using them to construct the dang tag. When we remove struts kill these double methods.
    private boolean isSuperUserApproveDocumentAuthorized_preInitialized() {
        if (superuserForDocumentType) {
            return true;
        }

        final String principalId = GlobalVariables.getUserSession().getPrincipalId();
        return KEWServiceLocator.getDocumentTypeService().canSuperUserApproveDocument(
            principalId, getDocTypeName(), routeNodeInstances, documentStatus);
    }

    public boolean isSuperUserDisapproveDocumentAuthorized() {
        initializeFieldsForSuperUserChecks();
        return isSuperUserDisapproveDocumentAuthorized_preInitialized();
    }

    // this ridiculousness was done to reduce db calls. the calling method in this class was calling initialize over
    // and over again -- which is expensive. I wanted to remove these methods entirely (in favor of one well done
    // method) but struts is using them to construct the dang tag. When we remove struts kill these double methods.
    private boolean isSuperUserDisapproveDocumentAuthorized_preInitialized() {
        if (superuserForDocumentType) {
            return true;
        }

        final String principalId = GlobalVariables.getUserSession().getPrincipalId();
        return KEWServiceLocator.getDocumentTypeService().canSuperUserDisapproveDocument(
            principalId, getDocTypeName(), routeNodeInstances, documentStatus);
    }

    public boolean isSuperUserAuthorized() {
        initializeFieldsForSuperUserChecks();
        if (superuserForDocumentType) {
            return true;
        }

        final String principalId = GlobalVariables.getUserSession().getPrincipalId();
        return KEWServiceLocator.getDocumentTypeService().canSuperUserApproveSingleActionRequest(
            principalId, getDocTypeName(), routeNodeInstances, documentStatus)
                || KEWServiceLocator.getDocumentTypeService().canSuperUserApproveDocument(
                    principalId, getDocTypeName(), routeNodeInstances, documentStatus)
                || KEWServiceLocator.getDocumentTypeService().canSuperUserDisapproveDocument(
                    principalId, getDocTypeName(), routeNodeInstances, documentStatus);
    }

    public boolean isStateAllowsApproveOrDisapprove() {
        if (getDocument().getDocumentHeader().hasWorkflowDocument()) {
            final DocumentStatus status = getDocument().getDocumentHeader().getWorkflowDocument().getStatus();
            return !(isStateProcessedOrDisapproved(status) || isStateInitiatedFinalCancelled(status)
                    || StringUtils.equals(status.getCode(), DocumentStatus.SAVED.getCode()));
        } else {
            return false;
        }
    }

    public boolean isStateAllowsApproveSingleActionRequest() {
        if (getDocument().getDocumentHeader().hasWorkflowDocument()) {
            final DocumentStatus status = getDocument().getDocumentHeader().getWorkflowDocument().getStatus();
            return !isStateInitiatedFinalCancelled(status);
        } else {
            return false;
        }
    }

    public boolean isStateProcessedOrDisapproved(final DocumentStatus status) {
        return StringUtils.equals(status.getCode(), DocumentStatus.PROCESSED.getCode())
                || StringUtils.equals(status.getCode(), DocumentStatus.DISAPPROVED.getCode());
    }

    public boolean isStateInitiatedFinalCancelled(final DocumentStatus status) {
        return StringUtils.equals(status.getCode(), DocumentStatus.INITIATED.getCode())
                || StringUtils.equals(status.getCode(), DocumentStatus.FINAL.getCode())
                || StringUtils.equals(status.getCode(), DocumentStatus.CANCELED.getCode());
    }

    public String getLastActionTaken() {
        return lastActionTaken;
    }

    public void setLastActionTaken(final String lastActionTaken) {
        this.lastActionTaken = lastActionTaken;
    }

    public boolean isShowCloseButton() {
        return showCloseButton;
    }

    public void setShowCloseButton(final boolean showCloseButton) {
        this.showCloseButton = showCloseButton;
    }

    public String getReturnUrlLocation() {
        return returnUrlLocation;
    }

    public void setReturnUrlLocation(final String returnUrlLocation) {
        this.returnUrlLocation = returnUrlLocation;
    }

    public boolean isShowFutureHasError() {
        return StringUtils.isNotEmpty(getShowFutureError());
    }

    public String getShowFutureError() {
        return showFutureError;
    }

    public void setShowFutureError(final String showFutureError) {
        this.showFutureError = showFutureError;
    }

    public boolean isShowFuture() {
        return showFuture;
    }

    public void setShowFuture(final boolean showFuture) {
        this.showFuture = showFuture;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }

    public boolean isRemoveHeader() {
        return removeHeader;
    }

    public void setRemoveHeader(final boolean removeBar) {
        removeHeader = removeBar;
    }

    public boolean isLookFuture() {
        return lookFuture;
    }

    public void setLookFuture(final boolean showFutureLink) {
        lookFuture = showFutureLink;
    }

    public boolean isShowNotes() {
        return showNotes;
    }

    public void setShowNotes(final boolean showNotes) {
        this.showNotes = showNotes;
    }

    public boolean isShowBackButton() {
        return showBackButton;
    }

    public void setShowBackButton(final boolean showBackButton) {
        this.showBackButton = showBackButton;
    }

    public int getInternalNavCount() {
        return internalNavCount;
    }

    public void setInternalNavCount(final int internalNavCount) {
        this.internalNavCount = internalNavCount;
    }

    public int getNextNavCount() {
        return getInternalNavCount() + 1;
    }

    public int getBackCount() {
        return -getNextNavCount();
    }

    public Integer getCurrentFiscalYear() {
        return getUniversityDateService().getCurrentFiscalYear();
    }

    private DocumentDictionaryService getDocumentDictionaryService() {
        if (documentDictionaryService == null) {
            documentDictionaryService = KRADServiceLocatorWeb.getDocumentDictionaryService();
        }
        return documentDictionaryService;
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = KNSServiceLocator.getBusinessObjectDictionaryService();
        }
        return businessObjectDictionaryService;
    }

    private UniversityDateService getUniversityDateService() {
        if (universityDateService == null) {
            universityDateService = SpringContext.getBean(UniversityDateService.class);
        }
        return universityDateService;
    }
}
