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
package org.kuali.kfs.kns.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServletWrapper;
import org.apache.struts.upload.CommonsMultipartRequestHandler;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.datadictionary.legacy.MaintenanceDocumentDictionaryService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.action.RecipientType;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.role.RoleLite;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.datadictionary.MaintenanceDocumentEntry;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.struts.action.KualiMultipartRequestHandler;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.kns.web.struts.form.KualiMaintenanceForm;
import org.kuali.kfs.kns.web.struts.form.pojo.PojoFormBase;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.DataDictionaryEntry;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatter;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.SessionDocument;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ====
 * CU Customization: Modified Person name references to use the potentially masked equivalents instead.
 * ====
 * 
 * General helper methods for handling requests.
 */
public final class WebUtils {

    private static final Logger LOG = LogManager.getLogger();

    private static final String ERROR_UPLOADFILE_SIZE = "error.uploadFile.size";
    private static final String IMAGE_COORDINATE_CLICKED_X_EXTENSION = ".x";
    private static final String IMAGE_COORDINATE_CLICKED_Y_EXTENSION = ".y";

    private static final String APPLICATION_IMAGE_URL_PROPERTY_PREFIX = "application.custom.image.url";
    private static final String DEFAULT_IMAGE_URL_PROPERTY_NAME = "externalizable.images.url";

    /**
     * Prefixes indicating an absolute url
     */
    private static final String[] SCHEMES = {"http://", "https://"};

    public static String KEY_KUALI_FORM_IN_SESSION = "KualiForm";

    private static ConfigurationService configurationService;
    private static final URLCodec urlCodec = new URLCodec("UTF-8");

    /**
     * Private Constructor since this is a util class that should never be instantiated.
     */
    private WebUtils() {
    }

    /**
     * Checks for methodToCall parameter, and picks off the value using set dot notation. Handles the problem of image
     * submits.
     *
     * @param request
     * @return methodToCall String
     */
    public static String parseMethodToCall(final ActionForm form, final HttpServletRequest request) {
        String methodToCall = null;

        // check if is specified cleanly
        if (StringUtils.isNotBlank(request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER))) {
            if (form instanceof KualiForm
                && !((KualiForm) form).shouldMethodToCallParameterBeUsed(KRADConstants.DISPATCH_REQUEST_PARAMETER,
                request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER), request)) {
                throw new RuntimeException("Cannot verify that the methodToCall should be "
                    + request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER));
            }
            methodToCall = request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER);
            // include .x at the end of the parameter to make it consistent w/other parameters
            request.setAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE, KRADConstants.DISPATCH_REQUEST_PARAMETER + "."
                + methodToCall + IMAGE_COORDINATE_CLICKED_X_EXTENSION);
        }

        /*
            The reason why we are checking for a ".x" at the end of the parameter name: It is for the image names that
            in addition to sending the form data, the web browser sends the x,y coordinate of where the user clicked on
            the image. If the image input is not given a name then the browser sends the x and y coordinates as the "x"
            and "y" input fields. If the input image does have a name, the x and y coordinates are sent using the
            format name.x and name.y.
         */
        if (methodToCall == null) {
            // iterate through parameters looking for methodToCall
            for (final Enumeration i = request.getParameterNames(); i.hasMoreElements(); ) {
                final String parameterName = (String) i.nextElement();

                // check if the parameter name is a specifying the methodToCall
                if (isMethodToCall(parameterName)) {
                    methodToCall = getMethodToCallSettingAttribute(form, request, parameterName);
                    break;
                } else {
                    // KULRICE-1218: Check if the parameter's values match (not just the name)
                    for (final String value : request.getParameterValues(parameterName)) {
                        // adding period to startsWith check - don't want to get confused with methodToCallFoobar
                        if (isMethodToCall(value)) {
                            methodToCall = getMethodToCallSettingAttribute(form, request, value);
                            // why is there not a break outer loop here?
                        }
                    }
                }
            }
        }

        return methodToCall;
    }

    /**
     * Checks if a string signifies a methodToCall string
     *
     * @param string the string to check
     * @return true if is a methodToCall
     */
    private static boolean isMethodToCall(final String string) {
        // adding period to startsWith check - don't want to get confused with methodToCallFoobar
        return string.startsWith(KRADConstants.DISPATCH_REQUEST_PARAMETER + ".");
    }

    /**
     * Parses out the methodToCall command and also sets the request attribute for the methodToCall.
     *
     * @param form    the ActionForm
     * @param request the request to set the attribute on
     * @param string  the methodToCall string
     * @return the methodToCall command
     */
    private static String getMethodToCallSettingAttribute(final ActionForm form, final HttpServletRequest request, final String string) {
        if (form instanceof KualiForm && !((KualiForm) form).shouldMethodToCallParameterBeUsed(string,
                request.getParameter(string), request)) {
            throw new RuntimeException("Cannot verify that the methodToCall should be " + string);
        }
        // always adding a coordinate even if not an image
        final String attributeValue = endsWithCoordinates(string) ? string : string
                + IMAGE_COORDINATE_CLICKED_X_EXTENSION;
        final String methodToCall = StringUtils.substringBetween(attributeValue,
                KRADConstants.DISPATCH_REQUEST_PARAMETER + ".", ".");
        request.setAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE, attributeValue);
        return methodToCall;
    }

    /**
     * A file that is not of type text/plain or text/html can be output through the response using this method.
     *
     * @param response
     * @param contentType
     * @param byteArrayOutputStream
     * @param fileName
     */
    public static void saveMimeOutputStreamAsFile(
            final HttpServletResponse response, final String contentType,
            final ByteArrayOutputStream byteArrayOutputStream, final String fileName) throws IOException {
        // If there are quotes in the name, we should replace them to avoid issues.
        // The filename will be wrapped with quotes below when it is set in the header
        final String updateFileName;
        if (fileName.contains("\"")) {
            updateFileName = fileName.replaceAll("\"", "");
        } else {
            updateFileName = fileName;
        }

        // set response
        response.setContentType(contentType);
        response.setHeader("Content-disposition", "attachment; filename=\"" + updateFileName + "\"");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentLength(byteArrayOutputStream.size());

        // write to output
        final OutputStream outputStream = response.getOutputStream();
        byteArrayOutputStream.writeTo(response.getOutputStream());
        outputStream.flush();
        outputStream.close();
    }

    /**
     * A file that is not of type text/plain or text/html can be output through
     * the response using this method.
     *
     * @param response
     * @param contentType
     * @param inStream
     * @param fileName
     */
    public static void saveMimeInputStreamAsFile(
            final HttpServletResponse response, final String contentType,
            final InputStream inStream, final String fileName, final int fileSize) throws IOException {
        // If there are quotes in the name, we should replace them to avoid issues.
        // The filename will be wrapped with quotes below when it is set in the header
        final String updateFileName;
        if (fileName.contains("\"")) {
            updateFileName = fileName.replaceAll("\"", "");
        } else {
            updateFileName = fileName;
        }

        // set response
        response.setContentType(contentType);
        response.setHeader("Content-disposition", "attachment; filename=\"" + updateFileName + "\"");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentLength(fileSize);

        // write to output
        final OutputStream out = response.getOutputStream();
        final BufferedInputStream is = new BufferedInputStream(inStream);
        int data;
        while ((data = is.read()) != -1) {
            out.write(data);
        }
        is.close();
        out.flush();
    }

    /**
     * @param form
     * @param tabKey
     * @return the tab state of the tab from the form.
     */
    public static String getTabState(final KualiForm form, final String tabKey) {
        return form.getTabState(tabKey);
    }

    public static void incrementTabIndex(final KualiForm form, final String tabKey) {
        form.incrementTabIndex();
    }

    /**
     * Attempts to reopen sub tabs which would have been closed for inactive records
     *
     * @param sections       the list of Sections whose rows and fields to set the open tab state on
     * @param tabStates      the map of tabKey->tabState.  This map will be modified to set entries to "OPEN"
     * @param collectionName the name of the collection reopening
     */
    public static void reopenInactiveRecords(
            final List<Section> sections, final Map<String, String> tabStates,
            final String collectionName) {
        for (final Section section : sections) {
            for (final Row row : section.getRows()) {
                for (final Field field : row.getFields()) {
                    if (field != null) {
                        if (Field.CONTAINER.equals(field.getFieldType())
                                && StringUtils.startsWith(field.getContainerName(), collectionName)) {
                            final String tabKey = WebUtils.generateTabKey(FieldUtils.generateCollectionSubTabName(field));
                            tabStates.put(tabKey, KualiForm.TabState.OPEN.name());
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a String from the title that can be used as a Map key.
     *
     * @param tabTitle
     * @return
     */
    public static String generateTabKey(final String tabTitle) {
        String key = "";
        if (StringUtils.isNotBlank(tabTitle)) {
            key = tabTitle.replaceAll("\\W", "");
        }

        return key;
    }

    public static void getMultipartParameters(
            final HttpServletRequest request, final ActionServletWrapper servletWrapper,
            final ActionForm form, final ActionMapping mapping) {
        try {
            final CommonsMultipartRequestHandler multipartHandler = new CommonsMultipartRequestHandler();
            // Set servlet and mapping info
            if (servletWrapper != null) {
                // from pojoformbase
                // servlet only affects tempdir on local disk
                servletWrapper.setServletFor(multipartHandler);
            }
            multipartHandler.setMapping((ActionMapping) request.getAttribute(Globals.MAPPING_KEY));
            // Initialize multipart request class handler
            multipartHandler.handleRequest(request);

            final Collection<FormFile> files = multipartHandler.getFileElements().values();
            final Enumeration keys = multipartHandler.getFileElements().keys();

            while (keys.hasMoreElements()) {
                final Object key = keys.nextElement();
                final FormFile file = (FormFile) multipartHandler.getFileElements().get(key);
                final long maxSize = WebUtils.getMaxUploadSize(form);
                LOG.debug(file::getFileSize);
                if (maxSize > 0 && Long.parseLong(file.getFileSize() + "") > maxSize) {
                    GlobalVariables.getMessageMap().putError(key.toString(), ERROR_UPLOADFILE_SIZE,
                            file.getFileName(), Long.toString(maxSize));
                }
            }

            // get file elements for kualirequestprocessor
            if (servletWrapper == null) {
                request.setAttribute(KRADConstants.UPLOADED_FILE_REQUEST_ATTRIBUTE_KEY,
                    getFileParametersForMultipartRequest(request, multipartHandler));
            }
        } catch (final ServletException e) {
            throw new ValidationException("unable to handle multipart request " + e.getMessage(), e);
        }
    }

    public static long getMaxUploadSize(final ActionForm form) {
        long max = 0L;
        final KualiMultipartRequestHandler multipartHandler = new KualiMultipartRequestHandler();
        if (form instanceof PojoFormBase) {
            max = multipartHandler.calculateMaxUploadSizeToMaxOfList(((PojoFormBase) form).getMaxUploadSizes());
        }
        LOG.debug("Max File Upload Size: {}", max);
        return max;
    }

    static Map<String, Object> getFileParametersForMultipartRequest(
            final HttpServletRequest request,
            final MultipartRequestHandler multipartHandler
    ) {
        final Map<String, Object> parameters = new HashMap<>(multipartHandler.getFileElements());

        if (request instanceof MultipartRequestWrapper) {
            final MultipartRequestWrapper multipartRequestWrapper = (MultipartRequestWrapper) request;
            final HttpServletRequest wrappedRequest = (HttpServletRequest) multipartRequestWrapper.getRequest();
            final Map<String, String[]> wrappedRequestParameters = wrappedRequest.getParameterMap();
            parameters.putAll(wrappedRequestParameters);
        } else {
            LOG.debug("Gathering multipart parameters for unwrapped request");
        }
        return parameters;
    }

    public static void registerEditableProperty(final PojoFormBase form, final String editablePropertyName) {
        form.registerEditableProperty(editablePropertyName);
    }

    public static boolean isDocumentSession(final Document document, final PojoFormBase docForm) {
        final boolean sessionDoc = document instanceof SessionDocument;
        boolean dataDictionarySessionDoc = false;
        if (!sessionDoc) {
            if (docForm instanceof KualiMaintenanceForm) {
                final KualiMaintenanceForm maintenanceForm = (KualiMaintenanceForm) docForm;
                if (maintenanceForm.getDocTypeName() != null) {
                    final MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService =
                            KNSServiceLocator.getMaintenanceDocumentDictionaryService();
                    final MaintenanceDocumentEntry maintenanceDocumentEntry = maintenanceDocumentDictionaryService
                            .getMaintenanceDocumentEntry(maintenanceForm.getDocTypeName());
                    dataDictionarySessionDoc = maintenanceDocumentEntry.isSessionDocument();
                }
            } else if (document != null) {
                final DocumentDictionaryService documentDictionaryService =
                        KRADServiceLocatorWeb.getDocumentDictionaryService();
                final DocumentEntry documentEntry = documentDictionaryService.getDocumentEntry(document.getClass().getName());
                dataDictionarySessionDoc = documentEntry.isSessionDocument();
            }
        }
        return sessionDoc || dataDictionarySessionDoc;
    }

    public static boolean isFormSessionDocument(final PojoFormBase form) {
        Document document = null;
        if (KualiDocumentFormBase.class.isAssignableFrom(form.getClass())) {
            final KualiDocumentFormBase docForm = (KualiDocumentFormBase) form;
            document = docForm.getDocument();
        }
        return isDocumentSession(document, form);
    }

    public static ActionForm getKualiForm(final PageContext pageContext) {
        return getKualiForm((HttpServletRequest) pageContext.getRequest());
    }

    public static ActionForm getKualiForm(final HttpServletRequest request) {
        if (request.getAttribute(KEY_KUALI_FORM_IN_SESSION) != null) {
            return (ActionForm) request.getAttribute(KEY_KUALI_FORM_IN_SESSION);
        } else {
            final HttpSession session = request.getSession(false);
            return session != null ? (ActionForm) session.getAttribute(KEY_KUALI_FORM_IN_SESSION) : null;
        }
    }

    public static boolean isPropertyEditable(final Set<String> editableProperties, final String propertyName) {
        LOG.debug("isPropertyEditable({})", propertyName);

        final boolean returnVal = editableProperties == null
                                  || editableProperties.contains(propertyName)
                                  || getIndexOfCoordinateExtension(propertyName) != -1
                && editableProperties.contains(propertyName.substring(0, getIndexOfCoordinateExtension(propertyName)));
        if (!returnVal) {
            LOG.debug("isPropertyEditable({}) == false / editableProperties: {}", propertyName, editableProperties);
        }
        return returnVal;
    }

    public static boolean endsWithCoordinates(final String parameter) {
        return parameter.endsWith(WebUtils.IMAGE_COORDINATE_CLICKED_X_EXTENSION)
            || parameter.endsWith(WebUtils.IMAGE_COORDINATE_CLICKED_Y_EXTENSION);
    }

    public static int getIndexOfCoordinateExtension(final String parameter) {
        int indexOfCoordinateExtension = parameter.lastIndexOf(WebUtils.IMAGE_COORDINATE_CLICKED_X_EXTENSION);
        if (indexOfCoordinateExtension == -1) {
            indexOfCoordinateExtension = parameter.lastIndexOf(WebUtils.IMAGE_COORDINATE_CLICKED_Y_EXTENSION);
        }
        return indexOfCoordinateExtension;
    }

    public static boolean isInquiryHiddenField(
            final String className, final String fieldName, final Object formObject,
            final String propertyName) {
        boolean isHidden = false;
        final String hiddenInquiryFields = getKualiConfigurationService().getPropertyValueAsString(className + ".hidden");
        if (StringUtils.isEmpty(hiddenInquiryFields)) {
            return isHidden;
        }
        final List hiddenFields = Arrays.asList(hiddenInquiryFields.replaceAll(" ", "").split(","));
        if (hiddenFields.contains(fieldName.trim())) {
            isHidden = true;
        }
        return isHidden;
    }

    public static String getFullyMaskedValue(
            final String className, final String fieldName, final Object formObject,
            final String propertyName) {
        String displayMaskValue = null;
        final Object propertyValue = ObjectUtils.getPropertyValue(formObject, propertyName);

        final DataDictionaryEntry entry = getDictionaryObjectEntry(className);
        final AttributeDefinition a = entry.getAttributeDefinition(fieldName);

        final AttributeSecurity attributeSecurity = a.getAttributeSecurity();
        if (attributeSecurity != null && attributeSecurity.isMask()) {
            final MaskFormatter maskFormatter = attributeSecurity.getMaskFormatter();
            displayMaskValue = maskFormatter.maskValue(propertyValue);
        }
        return displayMaskValue;
    }

    public static String getPartiallyMaskedValue(
            final String className, final String fieldName, final Object formObject,
            final String propertyName) {
        String displayMaskValue = null;
        final Object propertyValue = ObjectUtils.getPropertyValue(formObject, propertyName);

        final DataDictionaryEntry entry = getDictionaryObjectEntry(className);
        final AttributeDefinition a = entry.getAttributeDefinition(fieldName);

        final AttributeSecurity attributeSecurity = a.getAttributeSecurity();
        if (attributeSecurity != null && attributeSecurity.isPartialMask()) {
            final MaskFormatter partialMaskFormatter = attributeSecurity.getPartialMaskFormatter();
            displayMaskValue = partialMaskFormatter.maskValue(propertyValue);
        }
        return displayMaskValue;
    }

    public static boolean canFullyUnmaskField(final String businessObjectClassName, final String fieldName, final KualiForm form) {
        final Class businessObjClass;
        try {
            businessObjClass = Class.forName(businessObjectClassName);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to resolve class name: " + businessObjectClassName);
        }
        if (form instanceof KualiDocumentFormBase) {
            return KNSServiceLocator.getBusinessObjectAuthorizationService().canFullyUnmaskField(
                GlobalVariables.getUserSession().getPerson(), businessObjClass, fieldName,
                ((KualiDocumentFormBase) form).getDocument());
        } else {
            return KNSServiceLocator.getBusinessObjectAuthorizationService().canFullyUnmaskField(
                GlobalVariables.getUserSession().getPerson(), businessObjClass, fieldName, null);
        }
    }

    public static boolean canPartiallyUnmaskField(final String businessObjectClassName, final String fieldName, final KualiForm form) {
        final Class businessObjClass;
        try {
            businessObjClass = Class.forName(businessObjectClassName);
        } catch (final Exception e) {
            throw new RuntimeException("Unable to resolve class name: " + businessObjectClassName);
        }
        if (form instanceof KualiDocumentFormBase) {
            return KNSServiceLocator.getBusinessObjectAuthorizationService().canPartiallyUnmaskField(
                GlobalVariables.getUserSession().getPerson(), businessObjClass, fieldName,
                ((KualiDocumentFormBase) form).getDocument());
        } else {
            return KNSServiceLocator.getBusinessObjectAuthorizationService().canPartiallyUnmaskField(
                GlobalVariables.getUserSession().getPerson(), businessObjClass, fieldName, null);
        }
    }

    public static boolean canAddNoteAttachment(final Document document) {
        final DocumentAuthorizer documentAuthorizer = KNSServiceLocator.getDocumentHelperService().getDocumentAuthorizer(
            document);
        return documentAuthorizer.canAddNoteAttachment(document, null,
                GlobalVariables.getUserSession().getPerson());
    }

    public static boolean canViewNoteAttachment(final Document document, final String attachmentTypeCode) {
        final DocumentAuthorizer documentAuthorizer = KNSServiceLocator.getDocumentHelperService().getDocumentAuthorizer(
            document);
        return documentAuthorizer.canViewNoteAttachment(document, attachmentTypeCode,
                GlobalVariables.getUserSession().getPerson());
    }

    public static boolean canDeleteNoteAttachment(
            final Document document, final String attachmentTypeCode,
            final String authorUniversalIdentifier) {
        final DocumentAuthorizer documentAuthorizer = KNSServiceLocator.getDocumentHelperService().getDocumentAuthorizer(
            document);
        boolean canDeleteNoteAttachment = documentAuthorizer.canDeleteNoteAttachment(document, attachmentTypeCode,
                "false", GlobalVariables.getUserSession().getPerson());
        if (canDeleteNoteAttachment) {
            return canDeleteNoteAttachment;
        } else {
            canDeleteNoteAttachment = documentAuthorizer.canDeleteNoteAttachment(document, attachmentTypeCode, "true",
                GlobalVariables.getUserSession().getPerson());
            if (canDeleteNoteAttachment
                 && !authorUniversalIdentifier.equals(GlobalVariables.getUserSession().getPerson().getPrincipalId())) {
                canDeleteNoteAttachment = false;
            }
        }
        return canDeleteNoteAttachment;
    }

    public static void reuseErrorMapFromPreviousRequest(final KualiDocumentFormBase kualiDocumentFormBase) {
        if (kualiDocumentFormBase.getMessageMapFromPreviousRequest() == null) {
            LOG.error("Error map from previous request is null!");
            return;
        }
        final MessageMap errorMapFromGlobalVariables = GlobalVariables.getMessageMap();
        if (kualiDocumentFormBase.getMessageMapFromPreviousRequest() == errorMapFromGlobalVariables) {
            // if we've switched them already, then return early and do nothing
            return;
        }
        if (!errorMapFromGlobalVariables.hasNoErrors()) {
            throw new RuntimeException("Cannot replace error map because it is not empty");
        }
        GlobalVariables.setMessageMap(kualiDocumentFormBase.getMessageMapFromPreviousRequest());
        GlobalVariables.getMessageMap().clearErrorPath();
    }

    /**
     * Escapes out HTML to prevent XSS attacks, and replaces the following strings to allow for a limited set of HTML
     * tags
     * <p>
     * <li>[X] and [/X], where X represents any 1 or 2 letter string may be used to specify the equivalent tag in HTML
     * (i.e. &lt;X&gt; and &lt;/X&gt;) </li>
     * <li> [font COLOR], where COLOR represents any valid html color (i.e. color name or hexcode proceeded by #) will
     * be filtered into &lt;font color="COLOR"/&gt; </li>
     * <li>[/font] will be filtered into &lt;/font&gt; </li>
     * <li> [table CLASS], where CLASS gives the style class to use, will be filter into &lt;table class="CLASS"/&gt;
     * </li>
     * <li>[/table] will be filtered into &lt;/table&gt; <li>[td CLASS], where CLASS gives the style class to use,
     * will be filter into &lt;td class="CLASS"/&gt; </li>
     *
     * @param inputString
     * @return
     */
    public static String filterHtmlAndReplaceRiceMarkup(final String inputString) {
        String outputString = StringEscapeUtils.escapeHtml4(inputString);
        // string has been escaped of all <, >, and & (and other characters)

        final Map<String, String> findAndReplacePatterns = new LinkedHashMap<>();

        // now replace our rice custom markup into html

        // DON'T ALLOW THE SCRIPT TAG OR ARBITRARY IMAGES/URLS/ETC. THROUGH

        //strip out instances where javascript precedes a URL
        findAndReplacePatterns.put("\\[a ((javascript|JAVASCRIPT|JavaScript).+)\\]", "");
        //turn passed a href value into appropriate tag
        findAndReplacePatterns.put("\\[a (.+)\\]", "<a href=\"$1\">");
        findAndReplacePatterns.put("\\[/a\\]", "</a>");

        // filter any one character tags
        findAndReplacePatterns.put("\\[([A-Za-z])\\]", "<$1>");
        findAndReplacePatterns.put("\\[/([A-Za-z])\\]", "</$1>");
        // filter any two character tags
        findAndReplacePatterns.put("\\[([A-Za-z]{2})\\]", "<$1>");
        findAndReplacePatterns.put("\\[/([A-Za-z]{2})\\]", "</$1>");
        // filter the font tag
        findAndReplacePatterns.put("\\[font (#[0-9A-Fa-f]{1,6}|[A-Za-z]+)\\]", "<font color=\"$1\">");
        findAndReplacePatterns.put("\\[/font\\]", "</font>");
        // filter the table tag
        findAndReplacePatterns.put("\\[table\\]", "<table>");
        findAndReplacePatterns.put("\\[table ([A-Za-z]+)\\]", "<table class=\"$1\">");
        findAndReplacePatterns.put("\\[/table\\]", "</table>");
        // filter td with class
        findAndReplacePatterns.put("\\[td ([A-Za-z]+)\\]", "<td class=\"$1\">");

        for (final String findPattern : findAndReplacePatterns.keySet()) {
            final Pattern p = Pattern.compile(findPattern);
            final Matcher m = p.matcher(outputString);
            if (m.find()) {
                final String replacePattern = findAndReplacePatterns.get(findPattern);
                outputString = m.replaceAll(replacePattern);
            }
        }

        return outputString;
    }

    /**
     * Determines and returns the URL for question button images; looks first for a property
     * "application.custom.image.url", and if that is missing, uses the image url returned by
     * getDefaultButtonImageUrl()
     *
     * @param imageName the name of the image to find a button for
     * @return the URL where question button images are located
     */
    public static String getButtonImageUrl(final String imageName) {
        String buttonImageUrl = getKualiConfigurationService().getPropertyValueAsString(
            WebUtils.APPLICATION_IMAGE_URL_PROPERTY_PREFIX + "." + imageName);
        if (StringUtils.isBlank(buttonImageUrl)) {
            buttonImageUrl = getDefaultButtonImageUrl(imageName);
        }
        return buttonImageUrl;
    }

    public static String getAttachmentImageForUrl(final String contentType) {
        final String image = getKualiConfigurationService().getPropertyValueAsString(KRADConstants.ATTACHMENT_IMAGE_PREFIX +
                                                                                     contentType);
        if (StringUtils.isEmpty(image)) {
            return getKualiConfigurationService().getPropertyValueAsString(KRADConstants.ATTACHMENT_IMAGE_DEFAULT);
        }
        return image;
    }

    /**
     * Generates a default button image URL, in the form of:
     * ${externalizable.images.url}buttonsmall_${imageName}.gif
     *
     * @param imageName the image name to generate a default button name for
     * @return the default button image url
     */
    public static String getDefaultButtonImageUrl(final String imageName) {
        return getKualiConfigurationService().getPropertyValueAsString(WebUtils.DEFAULT_IMAGE_URL_PROPERTY_NAME)
            + "buttonsmall_" + imageName + ".gif";
    }

    public static ConfigurationService getKualiConfigurationService() {
        if (configurationService == null) {
            configurationService = KRADServiceLocator.getKualiConfigurationService();
        }
        return configurationService;
    }

    /**
     * Takes a string an converts the whitespace which would be ignored in an HTML document into HTML elements so the
     * whitespace is preserved
     *
     * @param startingString The string to preserve whitespace in
     * @return A string whose whitespace has been converted to HTML elements to preserve the whitespace in an HTML
     *         document
     */
    public static String preserveWhitespace(final String startingString) {
        String convertedString = startingString.replaceAll("\n", "<br />");
        convertedString = convertedString.replaceAll("  ", "&nbsp;&nbsp;")
                .replaceAll("(&nbsp; | &nbsp;)", "&nbsp;&nbsp;");
        return convertedString;
    }

    public static String getKimGroupDisplayName(final String groupId) {
        if (StringUtils.isBlank(groupId)) {
            throw new IllegalArgumentException("Group ID must have a value");
        }
        return KimApiServiceLocator.getGroupService().getGroup(groupId).getName();
    }

    public static String getPrincipalDisplayName(final String principalId) {
        if (StringUtils.isBlank(principalId)) {
            throw new IllegalArgumentException("Principal ID must have a value");
        }
        final Person person = SpringContext.getBean(PersonService.class).getPerson(principalId);
        if (person == null) {
            return "";
        } else {
            return person.getNameMaskedIfNecessary();
        }
    }

    /**
     * Takes an {@link ActionRequest} with a recipient type of
     * {@link org.kuali.kfs.kew.api.action.RecipientType#ROLE} and returns the display name for the role.
     *
     * @param actionRequest the action request
     * @return the display name for the role
     * @throws IllegalArgumentException if the action request is null, or the recipient type is not ROLE
     */
    public static String getRoleDisplayName(final ActionRequest actionRequest) {
        final String result;

        if (actionRequest == null) {
            throw new IllegalArgumentException("actionRequest must be non-null");
        }
        if (!RecipientType.ROLE.getCode().equals(actionRequest.getRecipientTypeCd())) {
            throw new IllegalArgumentException("actionRequest recipient must be a Role");
        }

        final RoleLite role = KimApiServiceLocator.getRoleService().getRoleWithoutMembers(actionRequest.getRoleName());

        if (role != null) {
            result = role.getName();
        } else if (StringUtils.isNotBlank(actionRequest.getQualifiedRoleNameLabel())) {
            result = actionRequest.getQualifiedRoleNameLabel();
        } else {
            result = actionRequest.getRoleName();
        }

        return result;
    }

    /**
     * Returns an absolute URL which is a combination of a base part plus path, or in the case that the path is
     * already an absolute URL, the path alone
     *
     * @param base the url base path
     * @param path the path to append to base
     * @return an absolute URL representing the combination of base+path, or path alone if it is already absolute
     */
    public static String toAbsoluteURL(final String base, String path) {
        boolean abs = false;
        if (StringUtils.isBlank(path)) {
            path = "";
        } else {
            for (final String scheme : SCHEMES) {
                if (path.startsWith(scheme)) {
                    abs = true;
                    break;
                }
            }
        }
        if (abs) {
            return path;
        }
        return base + path;
    }

    public static String sanitizeBackLocation(String backLocation) {
        try {
            backLocation = urlCodec.decode(backLocation);
            final Pattern pattern = Pattern.compile(ConfigContext.getCurrentContextConfig()
                    .getProperty(KRADConstants.BACK_LOCATION_ALLOWED_REGEX));
            if (StringUtils.isNotEmpty(backLocation) && pattern.matcher(backLocation).matches()) {
                return HtmlUtils.htmlEscape(backLocation);
            }
        } catch (final DecoderException de) {
            LOG.debug("Failed to decode backLocation: {}", backLocation, de);
        }
        return ConfigContext.getCurrentContextConfig().getProperty(KRADConstants.BACK_LOCATION_DEFAULT_URL);
    }

    private static DataDictionaryEntry getDictionaryObjectEntry(final String className) {
        return KNSServiceLocator.getDataDictionaryService().getDictionaryObjectEntry(className);
    }
}
