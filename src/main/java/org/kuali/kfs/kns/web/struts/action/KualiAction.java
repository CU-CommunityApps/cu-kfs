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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.actionrequest.service.ActionRequestService;
import org.kuali.kfs.kew.actiontaken.ActionTaken;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.engine.simulation.SimulationCriteria;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kew.util.Utilities;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizerBase;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.kns.service.BusinessObjectAuthorizationService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.kfs.kns.web.struts.form.KualiForm;
import org.kuali.kfs.kns.web.struts.form.LookupForm;
import org.kuali.kfs.kns.web.struts.form.pojo.PojoForm;
import org.kuali.kfs.kns.web.struts.form.pojo.PojoFormBase;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.rest.resource.responses.ErrorResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * The base {@link org.apache.struts.action.Action} class for all KNS-based Actions. Extends from the standard
 * {@link org.apache.struts.actions.DispatchAction} which allows for a <i>methodToCall</i> request parameter to
 * be used to indicate which method to invoke.
 * <p>
 * <p>
 * This Action overrides #execute to set methodToCall for image submits.  Also performs other setup required for KNS
 * framework calls.
 */
/**
 * 
 * CU customization: backport FINP-8250.
 * This backport can be removed when we upgrade to the
 * 2/22/2022 financials release.
 *
 */
public abstract class KualiAction extends DispatchAction {

    protected static final JsonMapper MAPPER = new JsonMapper();
    // protected so it can be accessed by Action classes in CSU overlay
    protected static final String MESSAGE_SAVED = "message.saved";

    protected static final Comparator<ActionRequest> ROUTE_LOG_ACTION_REQUEST_SORTER =
            new Utilities.RouteLogActionRequestSorter();

    private static final Logger LOG = LogManager.getLogger();

    private static KualiModuleService kualiModuleService = null;
    private static BusinessObjectAuthorizationService businessObjectAuthorizationService = null;
    private static EncryptionService encryptionService = null;
    private static String applicationBaseUrl = null;

    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is textAreaFieldName
     */
    public static final String TEXT_AREA_FIELD_NAME = "textAreaFieldName";
    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is textAreaFieldLabel
     */
    public static final String TEXT_AREA_FIELD_LABEL = "textAreaFieldLabel";
    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is textAreaReadOnly
     */
    public static final String TEXT_AREA_READ_ONLY = "textAreaReadOnly";
    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is textAreaFieldAnchor
     */
    public static final String TEXT_AREA_FIELD_ANCHOR = "textAreaFieldAnchor";
    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is textAreaFieldAnchor
     */
    public static final String TEXT_AREA_MAX_LENGTH = "textAreaMaxLength";
    /**
     * Constant defined to match with TextArea.jsp and updateTextArea function in core.js
     * <p>
     * Value is htmlFormAction
     */
    public static final String FORM_ACTION = "htmlFormAction";
    /**
     * Constant defined to match input parameter from URL and from TextArea.jsp.
     * <p>
     * Value is methodToCall
     */
    public static final String METHOD_TO_CALL = "methodToCall";
    /**
     * Constant defined to match with global forwarding in struts-config.xml
     * for Text Area Update.
     * <p>
     * Value is updateTextArea
     */
    public static final String FORWARD_TEXT_AREA_UPDATE = "updateTextArea";
    /**
     * Constant defined to match with method to call in TextArea.jsp.
     * <p>
     * Value is postTextAreaToParent
     */
    public static final String POST_TEXT_AREA_TO_PARENT = "postTextAreaToParent";
    /**
     * Constant defined to match with local forwarding in struts-config.xml
     * for the parent of the Updated Text Area.
     * <p>
     * Value is forwardNext
     */
    public static final String FORWARD_NEXT = "forwardNext";

    private Set<String> methodToCallsToNotCheckAuthorization = new HashSet<>();
    {
        methodToCallsToNotCheckAuthorization.add("performLookup");
        methodToCallsToNotCheckAuthorization.add("performQuestion");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithInput");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithInputAgainBecauseOfErrors");
        methodToCallsToNotCheckAuthorization.add("performQuestionWithoutInput");
        methodToCallsToNotCheckAuthorization.add("performWorkgroupLookup");
    }

    /**
     * Entry point to all actions.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ActionForward returnForward;

        String methodToCall = findMethodToCall(form, request);

        if (isModuleLocked(form, methodToCall, request)) {
            return mapping.findForward(KFSConstants.MODULE_LOCKED_MAPPING);
        }

        if (form instanceof KualiForm && StringUtils.isNotEmpty(((KualiForm) form).getMethodToCall())) {
            if (StringUtils.isNotBlank(getImageContext(request, KRADConstants.ANCHOR))) {
                ((KualiForm) form).setAnchor(getImageContext(request, KRADConstants.ANCHOR));
            } else if (StringUtils.isNotBlank(request.getParameter(KRADConstants.ANCHOR))) {
                ((KualiForm) form).setAnchor(request.getParameter(KRADConstants.ANCHOR));
            }
        }
        // if found methodToCall, pass control to that method, else return the basic forward
        if (StringUtils.isNotBlank(methodToCall)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("methodToCall: '" + methodToCall + "'");
            }
            returnForward = dispatchMethod(mapping, form, request, response, methodToCall);
        } else {
            returnForward = defaultDispatch(mapping, form, request, response);
        }

        // make sure the user can do what they're trying to according to the module that owns the functionality
        if (!methodToCallsToNotCheckAuthorization.contains(methodToCall)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + methodToCall + "' not in set of exempt methods: " +
                        methodToCallsToNotCheckAuthorization);
            }
            checkAuthorization(form, methodToCall);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("'" + methodToCall + "' is exempt from auth checks.");
            }
        }

        // Add the ActionForm to GlobalVariables
        // This will allow developers to retrieve both the Document and any request parameters that are not
        // part of the Form and make them available in ValueFinder classes and other places where they are needed.
        if (KNSGlobalVariables.getKualiForm() == null) {
            KNSGlobalVariables.setKualiForm((KualiForm) form);
        }

        return returnForward;
    }

    /**
     * When no methodToCall is specified, the defaultDispatch method is invoked.  Default implementation
     * returns the "basic" ActionForward.
     */
    protected ActionForward defaultDispatch(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    @Override
    protected ActionForward dispatchMethod(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, String methodToCall) throws Exception {
        GlobalVariables.getUserSession().addObject(DocumentAuthorizerBase.USER_SESSION_METHOD_TO_CALL_OBJECT_KEY,
                methodToCall);
        return super.dispatchMethod(mapping, form, request, response, methodToCall);
    }

    protected String findMethodToCall(ActionForm form, HttpServletRequest request) throws Exception {
        String methodToCall;
        if (form instanceof KualiForm && StringUtils.isNotEmpty(((KualiForm) form).getMethodToCall())) {
            methodToCall = ((KualiForm) form).getMethodToCall();
        } else {
            // call utility method to parse the methodToCall from the request.
            methodToCall = WebUtils.parseMethodToCall(form, request);
        }
        return methodToCall;
    }

    /**
     * Toggles the tab state in the ui
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward toggleTab(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        KualiForm kualiForm = (KualiForm) form;
        String tabToToggle = getTabToToggle(request);
        if (StringUtils.isNotBlank(tabToToggle)) {
            if (kualiForm.getTabState(tabToToggle).equals(KualiForm.TabState.OPEN.name())) {
                kualiForm.getTabStates().remove(tabToToggle);
                kualiForm.getTabStates().put(tabToToggle, KualiForm.TabState.CLOSE.name());
            } else {
                kualiForm.getTabStates().remove(tabToToggle);
                kualiForm.getTabStates().put(tabToToggle, KualiForm.TabState.OPEN.name());
            }
        }

        doProcessingAfterPost(kualiForm, request);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Toggles all tabs to open
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward showAllTabs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return this.doTabOpenOrClose(mapping, form, request, true);
    }

    /**
     * Toggles all tabs to closed
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward hideAllTabs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return this.doTabOpenOrClose(mapping, form, request, false);
    }

    /**
     * Toggles all tabs to open of closed depending on the boolean flag.
     *
     * @param mapping  the mapping
     * @param form     the form
     * @param request  the request
     * @param open     whether to open of close the tabs
     * @return the action forward
     */
    private ActionForward doTabOpenOrClose(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            boolean open) {
        KualiForm kualiForm = (KualiForm) form;

        Map<String, String> tabStates = kualiForm.getTabStates();
        Map<String, String> newTabStates = new HashMap<>();
        for (String tabKey : tabStates.keySet()) {
            newTabStates.put(tabKey, open ? "OPEN" : "CLOSE");
        }
        kualiForm.setTabStates(newTabStates);
        doProcessingAfterPost(kualiForm, request);
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Default refresh method. Called from returning frameworks.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Parses the method to call attribute to pick off the line number which should be deleted.
     *
     * @param request
     * @return
     */
    protected int getLineToDelete(HttpServletRequest request) {
        return getSelectedLine(request);
    }

    /**
     * Parses the method to call attribute to pick off the line number which should be edited.
     *
     * @param request
     * @return
     */
    protected int getLineToEdit(HttpServletRequest request) {
        return getSelectedLine(request);
    }

    /**
     * Parses the method to call attribute to pick off the line number which should have an action performed on it.
     *
     * @param request
     * @return
     */
    protected int getSelectedLine(HttpServletRequest request) {
        int selectedLine = -1;
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            String lineNumber = StringUtils.substringBetween(parameterName, ".line", ".");
            if (StringUtils.isEmpty(lineNumber)) {
                return selectedLine;
            }
            selectedLine = Integer.parseInt(lineNumber);
        }

        return selectedLine;
    }

    /**
     * Determines which tab was requested to be toggled
     *
     * @param request
     * @return
     */
    protected String getTabToToggle(HttpServletRequest request) {
        String tabToToggle = "";
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isNotBlank(parameterName)) {
            tabToToggle = StringUtils.substringBetween(parameterName, ".tab", ".");
        }

        return tabToToggle;
    }

    /**
     * @param request
     * @return the header tab to navigate to.
     */
    protected String getHeaderTabNavigateTo(HttpServletRequest request) {
        String headerTabNavigateTo = KFSConstants.MAPPING_BASIC;
        String imageContext = getImageContext(request, KRADConstants.NAVIGATE_TO);
        if (StringUtils.isNotBlank(imageContext)) {
            headerTabNavigateTo = imageContext;
        }
        return headerTabNavigateTo;
    }

    /**
     * @param request
     * @return the header tab dispatch.
     */
    protected String getHeaderTabDispatch(HttpServletRequest request) {
        String headerTabDispatch;
        String imageContext = getImageContext(request, KRADConstants.HEADER_DISPATCH);
        if (StringUtils.isNotBlank(imageContext)) {
            headerTabDispatch = imageContext;
        } else {
            // In some cases it might be in request params instead
            headerTabDispatch = request.getParameter(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        }
        return headerTabDispatch;
    }

    /**
     * @param request
     * @param contextKey
     * @return the image context
     */
    protected String getImageContext(HttpServletRequest request, String contextKey) {
        String imageContext = "";
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (StringUtils.isBlank(parameterName)) {
            parameterName = request.getParameter("methodToCallPath");
        }
        if (StringUtils.isNotBlank(parameterName)) {
            imageContext = StringUtils.substringBetween(parameterName, contextKey, ".");
        }
        return imageContext;
    }

    protected String getReturnLocation(HttpServletRequest request, ActionMapping mapping) {
        String mappingPath = mapping.getPath();
        String basePath = getApplicationBaseUrl();
        return basePath + mappingPath + ".do";
    }

    /**
     * Retrieves the value of a parameter to be passed into the lookup or inquiry frameworks. The default implementation
     * of this method will attempt to look in the request to determine whether the appropriate value exists as a request
     * parameter. If not, it will attempt to look through the form object to find the property.
     *
     * @param boClass                    a class implementing boClass, representing the BO that will be looked up
     * @param parameterName              the name of the parameter
     * @param parameterValuePropertyName the property (relative to the form object) where the value to be passed into
     *                                   the lookup/inquiry may be found
     * @param form
     * @param request
     * @return
     */
    protected String retrieveLookupParameterValue(Class<? extends BusinessObject> boClass, String parameterName,
            String parameterValuePropertyName, ActionForm form, HttpServletRequest request) throws Exception {
        String value;
        if (StringUtils.contains(parameterValuePropertyName, "'")) {
            value = StringUtils.replace(parameterValuePropertyName, "'", "");
        } else if (request.getParameterMap().containsKey(parameterValuePropertyName)) {
            value = request.getParameter(parameterValuePropertyName);
        } else if (request.getParameterMap().containsKey(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX +
                parameterValuePropertyName)) {
            value = request.getParameter(KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX +
                    parameterValuePropertyName);
        } else {
            if (form instanceof KualiForm) {
                value = ((KualiForm) form).retrieveFormValueForLookupInquiryParameters(parameterName,
                        parameterValuePropertyName);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unable to retrieve lookup/inquiry parameter value for parameter name " +
                            parameterName + " parameter value property " + parameterValuePropertyName);
                }
                value = null;
            }
        }

        if (value != null && boClass != null
                && getBusinessObjectAuthorizationService().attributeValueNeedsToBeEncryptedOnFormsAndLinks(boClass,
                    parameterName)) {
            if (CoreApiServiceLocator.getEncryptionService().isEnabled()) {
                value = getEncryptionService().encrypt(value) + EncryptionService.ENCRYPTION_POST_PREFIX;
            }
        }
        return value;
    }

    /**
     * Takes care of storing the action form in the User session and forwarding to the lookup action.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public ActionForward performLookup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // parse out the important strings from our methodToCall parameter
        String fullParameter = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        validateLookupInquiryFullParameter(request, form, fullParameter);

        KualiForm kualiForm = (KualiForm) form;

        // when we return from the lookup, our next request's method to call is going to be refresh
        kualiForm.registerEditableProperty(KRADConstants.DISPATCH_REQUEST_PARAMETER);

        // parse out the baseLookupUrl if there is one
        String baseLookupUrl = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_PARM14_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_PARM14_RIGHT_DEL);

        // parse out business object class name for lookup
        String boClassName = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_BOPARM_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_BOPARM_RIGHT_DEL);
        if (StringUtils.isBlank(boClassName)) {
            throw new RuntimeException("Illegal call to perform lookup, no business object class name specified.");
        }
        Class boClass = null;

        try {
            boClass = Class.forName(boClassName);
        } catch (ClassNotFoundException cnfex) {
            if (StringUtils.isNotEmpty(baseLookupUrl) && baseLookupUrl.startsWith(getApplicationBaseUrl() + "/")
                || StringUtils.isEmpty(baseLookupUrl)) {
                throw new IllegalArgumentException("The class (" + boClassName + ") cannot be found by this particular "
                    + "application. ApplicationBaseUrl: " + getApplicationBaseUrl() + " ; baseLookupUrl: " +
                        baseLookupUrl);
            } else {
                LOG.info("The class (" + boClassName + ") cannot be found by this particular application. "
                    + "ApplicationBaseUrl: " + getApplicationBaseUrl() + " ; baseLookupUrl: " + baseLookupUrl);
            }
        }

        // build the parameters for the lookup url
        Map<String, String> parameters = new HashMap<>();
        String conversionFields = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);
        if (StringUtils.isNotBlank(conversionFields)) {
            parameters.put(KRADConstants.CONVERSION_FIELDS_PARAMETER, conversionFields);

            // register each of the destination parameters of the field conversion string as editable
            String[] fieldConversions = conversionFields.split(KRADConstants.FIELD_CONVERSIONS_SEPARATOR);
            for (String fieldConversion : fieldConversions) {
                String destination = fieldConversion.split(KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR, 2)[1];
                kualiForm.registerEditableProperty(destination);
            }
        }

        // pass values from form that should be pre-populated on lookup search
        String parameterFields = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);
        if (LOG.isDebugEnabled()) {
            LOG.debug("fullParameter: " + fullParameter);
            LOG.debug("parameterFields: " + parameterFields);
        }
        if (StringUtils.isNotBlank(parameterFields)) {
            String[] lookupParams = parameterFields.split(KRADConstants.FIELD_CONVERSIONS_SEPARATOR);
            if (LOG.isDebugEnabled()) {
                LOG.debug("lookupParams: " + Arrays.toString(lookupParams));
            }
            for (String lookupParam : lookupParams) {
                String[] keyValue = lookupParam.split(KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR, 2);
                if (keyValue.length != 2) {
                    throw new RuntimeException("malformed field conversion pair: " + Arrays.toString(keyValue));
                }

                String lookupParameterValue = retrieveLookupParameterValue(boClass, keyValue[1], keyValue[0], form,
                        request);
                if (StringUtils.isNotBlank(lookupParameterValue)) {
                    parameters.put(keyValue[1], lookupParameterValue);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("keyValue[0]: " + keyValue[0]);
                    LOG.debug("keyValue[1]: " + keyValue[1]);
                }
            }
        }

        // pass values from form that should be read-Only on lookup search
        String readOnlyFields = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_PARM8_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_PARM8_RIGHT_DEL);
        if (StringUtils.isNotBlank(readOnlyFields)) {
            parameters.put(KRADConstants.LOOKUP_READ_ONLY_FIELDS, readOnlyFields);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("fullParameter: " + fullParameter);
            LOG.debug("readOnlyFields: " + readOnlyFields);
        }

        // grab whether or not the "return value" link should be hidden or not
        String hideReturnLink = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_PARM3_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_PARM3_RIGHT_DEL);
        if (StringUtils.isNotBlank(hideReturnLink)) {
            parameters.put(KRADConstants.HIDE_LOOKUP_RETURN_LINK, hideReturnLink);
        }

        // add the optional extra button source and parameters string
        String extraButtonSource = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM4_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM4_RIGHT_DEL);
        if (StringUtils.isNotBlank(extraButtonSource)) {
            parameters.put(KRADConstants.EXTRA_BUTTON_SOURCE, extraButtonSource);
        }
        String extraButtonParams = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM5_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM5_RIGHT_DEL);
        if (StringUtils.isNotBlank(extraButtonParams)) {
            parameters.put(KRADConstants.EXTRA_BUTTON_PARAMS, extraButtonParams);
        }

        String lookupAction = KRADConstants.LOOKUP_ACTION;

        // is this a multi-value return?
        boolean isMultipleValue = false;
        String multipleValues = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_PARM6_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_PARM6_RIGHT_DEL);
        if (Boolean.valueOf(multipleValues)) {
            parameters.put(KRADConstants.MULTIPLE_VALUE, multipleValues);
            lookupAction = KRADConstants.MULTIPLE_VALUE_LOOKUP_ACTION;
            isMultipleValue = true;
        }

        // the name of the collection being looked up (primarily for multivalue lookups
        String lookedUpCollectionName = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM11_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM11_RIGHT_DEL);
        if (StringUtils.isNotBlank(lookedUpCollectionName)) {
            parameters.put(KRADConstants.LOOKED_UP_COLLECTION_NAME, lookedUpCollectionName);
        }

        // grab whether or not the "suppress actions" column should be hidden or not
        String suppressActions = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM7_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM7_RIGHT_DEL);
        if (StringUtils.isNotBlank(suppressActions)) {
            parameters.put(KRADConstants.SUPPRESS_ACTIONS, suppressActions);
        }

        // grab the references that should be refreshed upon returning from the lookup
        String referencesToRefresh = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM10_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM10_RIGHT_DEL);
        if (StringUtils.isNotBlank(referencesToRefresh)) {
            parameters.put(KRADConstants.REFERENCES_TO_REFRESH, referencesToRefresh);
        }

        // anchor, if it exists
        if (form instanceof KualiForm && StringUtils.isNotEmpty(((KualiForm) form).getAnchor())) {
            parameters.put(KRADConstants.LOOKUP_ANCHOR, ((KualiForm) form).getAnchor());
        }

        // now add required parameters
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "start");

        // pass value from form that shows if autoSearch is desired for lookup search
        String autoSearch = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_PARM9_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_PARM9_RIGHT_DEL);

        if (StringUtils.isNotBlank(autoSearch)) {
            parameters.put(KRADConstants.LOOKUP_AUTO_SEARCH, autoSearch);
            if ("YES".equalsIgnoreCase(autoSearch)) {
                parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "search");
            }
        }

        parameters.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(form));
        parameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, boClassName);

        parameters.put(KRADConstants.RETURN_LOCATION_PARAMETER, getReturnLocation(request, mapping));

        if (form instanceof KualiDocumentFormBase) {
            String docNum = ((KualiDocumentFormBase) form).getDocument().getDocumentNumber();
            if (docNum != null) {
                parameters.put(KRADConstants.DOC_NUM, docNum);
            }
        } else if (form instanceof LookupForm) {
            String docNum = ((LookupForm) form).getDocNum();
            if (docNum != null) {
                parameters.put(KRADConstants.DOC_NUM, ((LookupForm) form).getDocNum());
            }
        }

        if (boClass != null) {
            ModuleService responsibleModuleService = getKualiModuleService().getResponsibleModuleService(boClass);
            if (responsibleModuleService != null && responsibleModuleService.isExternalizable(boClass)) {
                Map<String, String> parameterMap = new HashMap<>(parameters);
                return new ActionForward(responsibleModuleService.getExternalizableBusinessObjectLookupUrl(boClass,
                        parameterMap), true);
            }
        }

        if (StringUtils.isBlank(baseLookupUrl)) {
            baseLookupUrl = getApplicationBaseUrl() + "/" + lookupAction;
        } else {
            if (isMultipleValue) {
                LookupUtils.transformLookupUrlToMultiple(baseLookupUrl);
            }
        }
        String lookupUrl = UrlFactory.parameterizeUrl(baseLookupUrl, parameters);
        return new ActionForward(lookupUrl, true);
    }

    protected void validateLookupInquiryFullParameter(HttpServletRequest request, ActionForm form,
            String fullParameter) {
        PojoFormBase pojoFormBase = (PojoFormBase) form;
        if (WebUtils.isFormSessionDocument((PojoFormBase) form)) {
            if (!pojoFormBase.isPropertyEditable(fullParameter)) {
                throw new RuntimeException("The methodToCallAttribute is not registered as an editable property.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public ActionForward performInquiry(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // parse out the important strings from our methodToCall parameter
        String fullParameter = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        validateLookupInquiryFullParameter(request, form, fullParameter);

        // when javascript is disabled, the inquiry will appear in the same window as the document.  when we close the
        // inquiry, our next request's method to call is going to be refresh
        KualiForm kualiForm = (KualiForm) form;
        kualiForm.registerEditableProperty(KRADConstants.DISPATCH_REQUEST_PARAMETER);

        // parse out business object class name for lookup
        String boClassName = StringUtils.substringBetween(fullParameter, KRADConstants.METHOD_TO_CALL_BOPARM_LEFT_DEL,
                KRADConstants.METHOD_TO_CALL_BOPARM_RIGHT_DEL);
        if (StringUtils.isBlank(boClassName)) {
            throw new RuntimeException("Illegal call to perform inquiry, no business object class name specified.");
        }

        // build the parameters for the inquiry url
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, boClassName);

        parameters.put(KRADConstants.RETURN_LOCATION_PARAMETER, getReturnLocation(request, mapping));

        // pass values from form that should be pre-populated on inquiry
        String parameterFields = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM2_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);
        if (LOG.isDebugEnabled()) {
            LOG.debug("fullParameter: " + fullParameter);
            LOG.debug("parameterFields: " + parameterFields);
        }
        if (StringUtils.isNotBlank(parameterFields)) {
            // TODO : create a method for this to be used by both lookup & inquiry ?
            String[] inquiryParams = parameterFields.split(KRADConstants.FIELD_CONVERSIONS_SEPARATOR);
            if (LOG.isDebugEnabled()) {
                LOG.debug("inquiryParams: " + inquiryParams);
            }
            Class<? extends BusinessObject> boClass = (Class<? extends BusinessObject>) Class.forName(boClassName);
            for (String inquiryParam : inquiryParams) {
                String[] keyValue = inquiryParam.split(KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR, 2);

                String inquiryParameterValue = retrieveLookupParameterValue(boClass, keyValue[1], keyValue[0], form,
                        request);
                final var value = Objects.requireNonNullElse(inquiryParameterValue,
                        "directInquiryKeyNotSpecified");
                parameters.put(keyValue[1], value);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("keyValue[0]: " + keyValue[0]);
                    LOG.debug("keyValue[1]: " + keyValue[1]);
                }
            }
        }
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "start");
        parameters.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(form));
        String inquiryUrl = null;
        try {
            Class.forName(boClassName);
            inquiryUrl = getApplicationBaseUrl() + "/" + KRADConstants.DIRECT_INQUIRY_ACTION;
        } catch (ClassNotFoundException ex) {
            // allow inquiry url to be null (and therefore no inquiry link will be displayed) but at least log a warning
            LOG.warn("Class name does not represent a valid class which this application understands: " +
                    boClassName);
        }
        inquiryUrl = UrlFactory.parameterizeUrl(inquiryUrl, parameters);
        return new ActionForward(inquiryUrl, true);

    }

    /**
     * This method handles rendering the question component, but without any of the extra error fields
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param questionId
     * @param questionText
     * @param questionType
     * @param caller
     * @param context
     * @return ActionForward
     * @throws Exception
     */
    protected ActionForward performQuestionWithoutInput(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response, String questionId, String questionText,
            String questionType, String caller, String context) throws Exception {
        return performQuestion(mapping, form, request, questionId, questionText, questionType, caller,
                context, false, "", "", "", "");
    }

    /**
     * Handles rendering a question prompt - without a specified context.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param questionId
     * @param questionText
     * @param questionType
     * @param caller
     * @param context
     * @return ActionForward
     * @throws Exception
     */
    protected ActionForward performQuestionWithInput(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, String questionId, String questionText, String questionType, String caller,
            String context) throws Exception {
        return performQuestion(mapping, form, request, questionId, questionText, questionType, caller,
                context, true, "", "", "", "");
    }

    /**
     * Handles re-rendering a question prompt because of an error on what was submitted.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @param questionId
     * @param questionText
     * @param questionType
     * @param caller
     * @param context
     * @param reason
     * @param errorKey
     * @param errorPropertyName
     * @param errorParameter
     * @return ActionForward
     * @throws Exception
     */
    protected ActionForward performQuestionWithInputAgainBecauseOfErrors(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response, String questionId, String questionText,
            String questionType, String caller, String context, String reason, String errorKey,
            String errorPropertyName, String errorParameter) throws Exception {
        return performQuestion(mapping, form, request, questionId, questionText, questionType, caller,
                context, true, reason, errorKey, errorPropertyName, errorParameter);
    }

    /**
     * Handles rendering a question prompt - with a specified context.
     *
     * @param mapping
     * @param form
     * @param request
     * @param questionId
     * @param questionText
     * @param questionType
     * @param caller
     * @param context
     * @param showReasonField
     * @param reason
     * @param errorKey
     * @param errorPropertyName
     * @param errorParameter
     * @return ActionForward
     * @throws Exception
     */
    private ActionForward performQuestion(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            String questionId, String questionText, String questionType, String caller, String context,
            boolean showReasonField, String reason, String errorKey, String errorPropertyName,
            String errorParameter) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "start");
        parameters.put(KRADConstants.DOC_FORM_KEY, GlobalVariables.getUserSession().addObjectWithGeneratedKey(form));
        parameters.put(KRADConstants.CALLING_METHOD, caller);
        parameters.put(KRADConstants.QUESTION_INST_ATTRIBUTE_NAME, questionId);
        parameters.put(KRADConstants.QUESTION_IMPL_ATTRIBUTE_NAME, questionType);
        parameters.put(KRADConstants.RETURN_LOCATION_PARAMETER, getReturnLocation(request, mapping));
        parameters.put(KRADConstants.QUESTION_CONTEXT, context);
        parameters.put(KRADConstants.QUESTION_SHOW_REASON_FIELD, Boolean.toString(showReasonField));
        parameters.put(KRADConstants.QUESTION_REASON_ATTRIBUTE_NAME, reason);
        parameters.put(KRADConstants.QUESTION_ERROR_KEY, errorKey);
        parameters.put(KRADConstants.QUESTION_ERROR_PROPERTY_NAME, errorPropertyName);
        parameters.put(KRADConstants.QUESTION_ERROR_PARAMETER, errorParameter);
        parameters.put(KRADConstants.QUESTION_ANCHOR,
                form instanceof KualiForm ? Objects.toString(((KualiForm) form).getAnchor()) : "");
        String methodToCallAttribute = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        if (methodToCallAttribute != null) {
            parameters.put(KRADConstants.METHOD_TO_CALL_PATH, methodToCallAttribute);
            ((PojoForm) form).registerEditableProperty(methodToCallAttribute);
        }

        if (form instanceof KualiDocumentFormBase) {
            String docNum = ((KualiDocumentFormBase) form).getDocument().getDocumentNumber();
            if (docNum != null) {
                parameters.put(KRADConstants.DOC_NUM, ((KualiDocumentFormBase) form)
                        .getDocument().getDocumentNumber());
            }
        }

        // KULRICE-8077: PO Quote Limitation of Only 9 Vendors
        String questionTextAttributeName = KRADConstants.QUESTION_TEXT_ATTRIBUTE_NAME + questionId;
        GlobalVariables.getUserSession().addObject(questionTextAttributeName, questionText);

        String questionUrl = UrlFactory.parameterizeUrl(getApplicationBaseUrl() + "/" +
                KRADConstants.QUESTION_ACTION, parameters);
        return new ActionForward(questionUrl, true);
    }

    /**
     * Takes care of storing the action form in the User session and forwarding to the workflow workgroup lookup action.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward performWorkgroupLookup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String returnUrl = getApplicationBaseUrl() + mapping.getPath() + ".do";

        String fullParameter = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        String conversionFields = StringUtils.substringBetween(fullParameter,
                KRADConstants.METHOD_TO_CALL_PARM1_LEFT_DEL, KRADConstants.METHOD_TO_CALL_PARM1_RIGHT_DEL);

        String deploymentBaseUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
            KFSConstants.APPLICATION_URL_KEY);
        String workgroupLookupUrl = deploymentBaseUrl +
                "/Lookup.do?lookupableImplServiceName=WorkGroupLookupableImplService&methodToCall=start&docFormKey=" +
                GlobalVariables.getUserSession().addObjectWithGeneratedKey(form);

        if (conversionFields != null) {
            workgroupLookupUrl += "&conversionFields=" + conversionFields;
        }
        if (form instanceof KualiDocumentFormBase) {
            workgroupLookupUrl += "&docNum=" + ((KualiDocumentFormBase) form).getDocument().getDocumentNumber();
        }

        workgroupLookupUrl += "&returnLocation=" + returnUrl;

        return new ActionForward(workgroupLookupUrl, true);
    }

    /**
     * Handles requests that originate via Header Tabs.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public ActionForward headerTab(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // header tab actions can do two things - 1, call into an action and perform what needs to happen in there
        // and 2, forward to a new location.
        String headerTabDispatch = getHeaderTabDispatch(request);
        if (StringUtils.isNotEmpty(headerTabDispatch)) {
            ActionForward forward = dispatchMethod(mapping, form, request, response, headerTabDispatch);
            if (GlobalVariables.getMessageMap().getNumberOfPropertiesWithErrors() > 0) {
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
            this.doTabOpenOrClose(mapping, form, request, false);
            if (forward.getRedirect()) {
                return forward;
            }
        }
        return dispatchMethod(mapping, form, request, response, getHeaderTabNavigateTo(request));
    }

    /**
     * Override this method to provide action-level access controls to the application.
     *
     * @param form
     * @throws AuthorizationException
     */
    protected void checkAuthorization(ActionForm form, String methodToCall) throws AuthorizationException {
        String principalId = GlobalVariables.getUserSession().getPrincipalId();
        Map<String, String> roleQualifier = new HashMap<>(getRoleQualification(form, methodToCall));
        Map<String, String> permissionDetails = KRADUtils.getNamespaceAndActionClass(this.getClass());

        if (!KimApiServiceLocator.getPermissionService().isAuthorizedByTemplate(principalId,
            KFSConstants.CoreModuleNamespaces.KFS, KimConstants.PermissionTemplateNames.USE_SCREEN, permissionDetails,
            roleQualifier)) {
            throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(),
                methodToCall,
                this.getClass().getSimpleName());
        }
    }

    /**
     * override this method to add data from the form for role qualification in the authorization check
     */
    protected Map<String, String> getRoleQualification(ActionForm form, String methodToCall) {
        return new HashMap<>();
    }

    protected static KualiModuleService getKualiModuleService() {
        if (kualiModuleService == null) {
            kualiModuleService = KRADServiceLocatorWeb.getKualiModuleService();
        }
        return kualiModuleService;
    }

    /**
     * This method is invoked when Java Script is turned off from the web browser. It setup the information that the
     * update text area requires for copying current text in the calling page text area and returning to the calling
     * page. The information is passed to the JSP through Http Request attributes. All other parameters are forwarded
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward updateTextArea(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        if (LOG.isTraceEnabled()) {
            String lm = String.format("ENTRY %s%n%s", form.getClass().getSimpleName(),
                request.getRequestURI());
            LOG.trace(lm);
        }

        final String[] keyValue = getTextAreaParams(request);

        request.setAttribute(TEXT_AREA_FIELD_NAME, keyValue[0]);
        request.setAttribute(FORM_ACTION, keyValue[1]);
        request.setAttribute(TEXT_AREA_FIELD_LABEL, keyValue[2]);
        request.setAttribute(TEXT_AREA_READ_ONLY, keyValue[3]);
        request.setAttribute(TEXT_AREA_MAX_LENGTH, keyValue[4]);
        if (form instanceof KualiForm && StringUtils.isNotEmpty(((KualiForm) form).getAnchor())) {
            request.setAttribute(TEXT_AREA_FIELD_ANCHOR, ((KualiForm) form).getAnchor());
        }

        // Set document related parameter
        String docWebScope = (String) request.getAttribute(KRADConstants.DOCUMENT_WEB_SCOPE);
        if (docWebScope != null && docWebScope.trim().length() >= 0) {
            request.setAttribute(KRADConstants.DOCUMENT_WEB_SCOPE, docWebScope);
        }

        request.setAttribute(KRADConstants.DOC_FORM_KEY,
                GlobalVariables.getUserSession().addObjectWithGeneratedKey(form));

        ActionForward forward = mapping.findForward(FORWARD_TEXT_AREA_UPDATE);

        if (LOG.isTraceEnabled()) {
            String lm = String.format("EXIT %s", forward == null ? "null" : forward.getPath());
            LOG.trace(lm);
        }

        return forward;
    }

    /**
     * This method takes the {@link KRADConstants#METHOD_TO_CALL_ATTRIBUTE} out of the request and parses it returning
     * the required fields needed for a text area. The fields returned are the following in this order.
     * <ol>
     * <li>{@link #TEXT_AREA_FIELD_NAME}</li>
     * <li>{@link #FORM_ACTION}</li>
     * <li>{@link #TEXT_AREA_FIELD_LABEL}</li>
     * <li>{@link #TEXT_AREA_READ_ONLY}</li>
     * <li>{@link #TEXT_AREA_MAX_LENGTH}</li>
     * </ol>
     *
     * @param request the request to retrieve the textarea parameters
     * @return a string array holding the parsed fields
     */
    private String[] getTextAreaParams(HttpServletRequest request) {
        // parse out the important strings from our methodToCall parameter
        String fullParameter = (String) request.getAttribute(
            KRADConstants.METHOD_TO_CALL_ATTRIBUTE);

        // parse textfieldname:htmlformaction
        String parameterFields = StringUtils.substringBetween(fullParameter,
            KRADConstants.METHOD_TO_CALL_PARM2_LEFT_DEL,
            KRADConstants.METHOD_TO_CALL_PARM2_RIGHT_DEL);
        if (LOG.isDebugEnabled()) {
            LOG.debug("fullParameter: " + fullParameter);
            LOG.debug("parameterFields: " + parameterFields);
        }
        String[] keyValue = null;
        if (StringUtils.isNotBlank(parameterFields)) {
            String[] textAreaParams = parameterFields.split(
                KRADConstants.FIELD_CONVERSIONS_SEPARATOR);
            if (LOG.isDebugEnabled()) {
                LOG.debug("lookupParams: " + textAreaParams);
            }
            for (final String textAreaParam : textAreaParams) {
                keyValue = textAreaParam.split(KRADConstants.FIELD_CONVERSION_PAIR_SEPARATOR, 2);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("keyValue[0]: " + keyValue[0]);
                    LOG.debug("keyValue[1]: " + keyValue[1]);
                    LOG.debug("keyValue[2]: " + keyValue[2]);
                    LOG.debug("keyValue[3]: " + keyValue[3]);
                    LOG.debug("keyValue[4]: " + keyValue[4]);
                }
            }
        }

        return keyValue;
    }

    /**
     * This method is invoked from the TextArea.jsp for posting its value to the parent page that called the extended
     * text area page. The invocation is done through Struts action. The default forwarding id is
     * KFSConstants.MAPPING_BASIC. This can be overridden using the parameter key FORWARD_NEXT.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     */
    public ActionForward postTextAreaToParent(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) {
        if (LOG.isTraceEnabled()) {
            String lm = String.format("ENTRY %s%n%s", form.getClass().getSimpleName(),
                request.getRequestURI());
            LOG.trace(lm);
        }

        String forwardingId = request.getParameter(FORWARD_NEXT);
        if (forwardingId == null) {
            forwardingId = KFSConstants.MAPPING_BASIC;
        }
        ActionForward forward = mapping.findForward(forwardingId);

        if (LOG.isTraceEnabled()) {
            String lm = String.format("EXIT %s", forward == null ? "null" : forward.getPath());
            LOG.trace(lm);
        }

        return forward;
    }

    /**
     * Use to add a methodToCall to the a list which will not have authorization checks. This assumes that the call will
     * be redirected (as in the case of a lookup) that will perform the authorization.
     */
    protected final void addMethodToCallToUncheckedList(String methodToCall) {
        methodToCallsToNotCheckAuthorization.add(methodToCall);
    }

    /**
     * This method does all special processing on a document that should happen on each HTTP post (ie, save, route,
     * approve, etc).
     */
    protected void doProcessingAfterPost(KualiForm form, HttpServletRequest request) {

    }

    protected BusinessObjectAuthorizationService getBusinessObjectAuthorizationService() {
        if (businessObjectAuthorizationService == null) {
            businessObjectAuthorizationService = KNSServiceLocator.getBusinessObjectAuthorizationService();
        }
        return businessObjectAuthorizationService;
    }

    protected EncryptionService getEncryptionService() {
        if (encryptionService == null) {
            encryptionService = CoreApiServiceLocator.getEncryptionService();
        }
        return encryptionService;
    }

    public static String getApplicationBaseUrl() {
        if (applicationBaseUrl == null) {
            applicationBaseUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
                KFSConstants.APPLICATION_URL_KEY);
        }
        return applicationBaseUrl;
    }

    protected boolean isModuleLocked(ActionForm form, String methodToCall, HttpServletRequest request) {
        String boClass = request.getParameter(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE);
        ModuleService moduleService = null;
        if (StringUtils.isNotBlank(boClass)) {
            try {
                moduleService = getKualiModuleService().getResponsibleModuleService(Class.forName(boClass));
            } catch (ClassNotFoundException classNotFoundException) {
                LOG.warn("BO class not found: " + boClass, classNotFoundException);
            }
        } else {
            moduleService = getKualiModuleService().getResponsibleModuleService(this.getClass());
        }
        if (moduleService != null && moduleService.isLocked()) {
            String principalId = GlobalVariables.getUserSession().getPrincipalId();
            String namespaceCode = KFSConstants.CoreModuleNamespaces.KFS;
            String permissionName = KimConstants.PermissionNames.ACCESS_LOCKED_MODULE;
            Map<String, String> qualification = getRoleQualification(form, methodToCall);
            if (!KimApiServiceLocator.getPermissionService().isAuthorized(principalId, namespaceCode, permissionName,
                    qualification)) {
                ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
                String messageParamNamespaceCode = moduleService.getModuleConfiguration().getNamespaceCode();
                String messageParamComponentCode = KRADConstants.DetailTypes.ALL_DETAIL_TYPE;
                String messageParamName = KRADConstants.SystemGroupParameterNames.OLTP_LOCKOUT_MESSAGE_PARM;
                String lockoutMessage = parameterService.getParameterValueAsString(messageParamNamespaceCode,
                        messageParamComponentCode, messageParamName);

                if (StringUtils.isBlank(lockoutMessage)) {
                    String defaultMessageParamName = KRADConstants.SystemGroupParameterNames.MODULE_LOCKED_MESSAGE;
                    lockoutMessage = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.KFS,
                            messageParamComponentCode, defaultMessageParamName);
                }
                request.setAttribute(KRADConstants.MODULE_LOCKED_MESSAGE_REQUEST_PARAMETER, lockoutMessage);
                return true;
            }
        }
        return false;
    }

    /**
     * Write {@code error} (UTF-8/JSON) to the {@link HttpServletResponse}, setting the status appropriately.
     */
    protected void writeErrorToResponse(
            final HttpServletResponse response,
            final ErrorResponse error
    ) {
        final Optional<String> errorJson = serializeToJsonSafely(error);
        errorJson.ifPresent(json -> writeJsonToResponse(response, error.getStatus(), json));
    }

    /**
     * Serializes {@code T} to a JSON String safely/quietly; no exceptions.
     *
     * @param object The instance to be serialized to a JSON String.
     * @return {@code Optional<String>} which will be populated if the serialization was successful or empty if it
     * was not.
     */
    protected <T> Optional<String> serializeToJsonSafely(final T object) {
        try {
            final var json = MAPPER.writeValueAsString(object);
            return Optional.of(json);
        } catch (final JsonProcessingException e) {
            LOG.error("safeSerialize(...) - Could not serialize : object={}", object);
        }
        return Optional.empty();
    }

    protected void writeJsonToResponse(
            final HttpServletResponse response,
            final Response.Status status,
            final String json
    ) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setStatus(status.getStatusCode());
        try {
            final PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
        } catch (final IOException e) {
            LOG.error("writeJsonToResponse(...) - Unable to write response : status={}; json={}", status, json, e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    protected ActionRequest switchActionRequestPositionIfPrimaryDelegatePresent(ActionRequest actionRequest) {
        // The delegate is displayed at the top-most level correctly on action requests that are "rooted" at a "role"
        // request. If they are rooted at a principal or group request, then the display of the primary delegator at
        // the top-most level does not happen (instead it shows the delegator and you have to expand the request to see
        // the primary delegate). In the non "role" case, it will put the primary delegate as the outermost request
        // *except* in the case where there is more than one primary delegate.
        if (!actionRequest.isRoleRequest()) {
            List<ActionRequest> primaryDelegateRequests = actionRequest.getPrimaryDelegateRequests();
            // only display primary delegate request at top if there is only *one* primary delegate request
            if (primaryDelegateRequests.size() != 1) {
                return actionRequest;
            }
            ActionRequest primaryDelegateRequest = primaryDelegateRequests.get(0);
            actionRequest.getChildrenRequests().remove(primaryDelegateRequest);
            primaryDelegateRequest.setChildrenRequests(actionRequest.getChildrenRequests());
            primaryDelegateRequest.setParentActionRequest(actionRequest.getParentActionRequest());

            actionRequest.setChildrenRequests(new ArrayList<>(0));
            actionRequest.setParentActionRequest(primaryDelegateRequest);

            primaryDelegateRequest.getChildrenRequests().add(0, actionRequest);

            for (ActionRequest delegateRequest : primaryDelegateRequest.getChildrenRequests()) {
                delegateRequest.setParentActionRequest(primaryDelegateRequest);
            }

            return primaryDelegateRequest;
        }

        return actionRequest;
    }

    protected List<ActionRequest> switchActionRequestPositionsIfPrimaryDelegatesPresent(
            Collection<ActionRequest> actionRequests
    ) {
        List<ActionRequest> results = new ArrayList<>(actionRequests.size());
        for (ActionRequest actionRequest : actionRequests) {
            results.add(switchActionRequestPositionIfPrimaryDelegatePresent(actionRequest));
        }
        return results;
    }

    protected void fixActionRequestsPositions(DocumentRouteHeaderValue routeHeader) {
        for (ActionTaken actionTaken : routeHeader.getActionsTaken()) {
            Collections.sort((List<ActionRequest>) actionTaken.getActionRequests(), ROUTE_LOG_ACTION_REQUEST_SORTER);
            actionTaken.setActionRequests(actionTaken.getActionRequests());
        }
    }

    /**
     * This utility method returns a Set of LongS containing the IDs for the ActionRequestValueS associated with
     * this DocumentRouteHeaderValue.
     */
    protected Set<String> getActionRequestIds(DocumentRouteHeaderValue document) {
        Set<String> actionRequestIds = new HashSet<>();

        List<ActionRequest> actionRequests =
                KEWServiceLocator.getActionRequestService()
                        .findAllActionRequestsByDocumentId(document.getDocumentId());

        if (actionRequests != null) {
            for (ActionRequest actionRequest : actionRequests) {
                if (actionRequest.getActionRequestId() != null) {
                    actionRequestIds.add(actionRequest.getActionRequestId());
                }
            }
        }
        return actionRequestIds;
    }

    /**
     * This method creates ActionRequest objects from the DocumentRouteHeaderValue output from a workflow simulation.
     *
     * @param documentRouteHeaderValue    contains action requests from which the ActionRequestValues are reconstituted
     * @param preexistingActionRequestIds this is a Set of ActionRequest IDs that will not be reconstituted
     * @return the ActionRequestValueS that have been created
     */
    protected List<ActionRequest> reconstituteActionRequestValues(
            DocumentRouteHeaderValue documentRouteHeaderValue, Set<String> preexistingActionRequestIds
    ) {

        List<ActionRequest> actionRequests = documentRouteHeaderValue.getActionRequests();
        List<ActionRequest> futureActionRequests = new ArrayList<>();
        if (actionRequests != null) {
            for (ActionRequest actionRequest : actionRequests) {
                if (actionRequest != null) {
                    if (!preexistingActionRequestIds.contains(actionRequest.getActionRequestId())) {
                        futureActionRequests.add(actionRequest);
                    }
                }
            }
        }
        return futureActionRequests;
    }

    /**
     * executes a simulation of the future routing, and sets the futureRootRequests and futureActionRequestCount
     * properties on the provided RouteLogForm.
     *
     * @param form the Form --used in a write-only fashion.
     * @param document the DocumentRouteHeaderValue for the document whose future routing is being simulated.
     * @throws Exception
     */
    /* backport FINP-8250*/
    protected void populateRouteLogFutureRequests(final KualiForm form, final DocumentRouteHeaderValue document)
            throws Exception {
        final SimulationCriteria criteria =
                SimulationCriteria.createSimulationCritUsingDocumentId(document.getDocumentId());

        // gather the IDs for action requests that predate the simulation
        final Set<String> preexistingActionRequestIds = getActionRequestIds(document);

        // run the simulation
        final DocumentRouteHeaderValue documentRouteHeaderValue =
                KewApiServiceLocator.getWorkflowDocumentActionsService().executeSimulation(criteria);

        // fabricate our ActionRequestValueS from the results
        final List<ActionRequest> futureActionRequests =
                reconstituteActionRequestValues(documentRouteHeaderValue, preexistingActionRequestIds);

        futureActionRequests.sort(ROUTE_LOG_ACTION_REQUEST_SORTER);

        List<ActionRequest> futureActionRequestsForDisplay = new ArrayList<>(futureActionRequests);

        futureActionRequestsForDisplay =
                switchActionRequestPositionsIfPrimaryDelegatesPresent(futureActionRequestsForDisplay);

        int pendingActionRequestCount = 0;
        for (final ActionRequest actionRequest : futureActionRequestsForDisplay) {
            if (actionRequest.isPending()) {
                pendingActionRequestCount++;

                if (actionRequest.isInitialized()) {
                    actionRequest.setDisplayStatus("PENDING");
                } else if (actionRequest.isActive()) {
                    actionRequest.setDisplayStatus("IN ACTION LIST");
                }
            }
        }

        form.setFutureRootRequests(futureActionRequestsForDisplay);
        form.setFutureActionRequestCount(pendingActionRequestCount);
    }

    /* backport FINP-8250 */
    protected void populateRouteLogFormActionRequests(final KualiForm form,
            final DocumentRouteHeaderValue routeHeader) {
        final List<ActionRequest> rootRequests = getActionRequestService().getRootRequests(
                routeHeader.getActionRequests());
        rootRequests.sort(ROUTE_LOG_ACTION_REQUEST_SORTER);

        List<ActionRequest> rootRequestsForDisplay = new ArrayList<>(rootRequests);

        rootRequestsForDisplay = switchActionRequestPositionsIfPrimaryDelegatesPresent(rootRequestsForDisplay);
        int arCount = 0;
        for (final ActionRequest actionRequest : rootRequestsForDisplay) {
            if (actionRequest.isPending()) {
                arCount++;

                if (actionRequest.isInitialized()) {
                    actionRequest.setDisplayStatus("PENDING");
                } else if (actionRequest.isActive()) {
                    actionRequest.setDisplayStatus("IN ACTION LIST");
                }
            }
        }
        form.setRootRequests(rootRequestsForDisplay);
        form.setPendingActionRequestCount(arCount);
    }

    private ActionRequestService getActionRequestService() {
        return KEWServiceLocator.getActionRequestService();
    }
}
