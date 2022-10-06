/*
 * Copyright 2014 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.sys.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.TypeUtils;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kns.datadictionary.InquirySectionDefinition;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.service.XmlObjectSerializerService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.datadictionary.FinancialSystemBusinessObjectEntry;
import org.kuali.kfs.sys.businessobject.lookup.LookupableSpringContext;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import edu.cornell.kfs.sys.util.RestXmlUtil;

@Controller
public class DataObjectRestServiceController {

    private static final String LOOKUPABLE_HELPER_SERVICE = "lookupableHelperService";
    private static final String MAX_OBJECTS_TO_RETURN = "maxObjectsToReturn";
    private static final String LIMIT_BY_PARAMETER = "limitByParameter";

    private static final Logger LOG = LogManager.getLogger(DataObjectRestServiceController.class);

    private DataDictionaryService dataDictionaryService;
    private PersistenceStructureService persistenceStructureService;
    private ParameterService parameterService;
    private PermissionService permissionService;

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Not authorized.")
    public void handleAccessDeniedException(AccessDeniedException ex, HttpServletResponse response) {
    }

    @ExceptionHandler(NoSuchBeanDefinitionException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Data object not found.")
    public void handleNoSuchBeanDefinitionException(NoSuchBeanDefinitionException ex, HttpServletResponse response) {
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Unexpected exception has occured.")
    public String handleRuntimeException(RuntimeException ex, HttpServletResponse response) {
        return ExceptionUtils.getStackTrace(ex);
    }

    @RequestMapping(value = "/{namespace}/{dataobject}.json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> getDataObjectsAsJSON(@PathVariable("namespace") String namespace, @PathVariable("dataobject") String dataobject, HttpServletRequest request) throws Exception {
        FinancialSystemBusinessObjectEntry boe = getBusinessObject(dataobject);
        validateRequest(boe, namespace, dataobject, request);

        try {
            List<Map<String, String>> resultMap = generateResultMap(request, boe);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

            String jsonData = null;
            if (resultMap.size() == 1) {
                jsonData = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(resultMap.get(0))
                        .replaceFirst(HashMap.class.getSimpleName(), boe.getBusinessObjectClass().getName());
            } else {
                jsonData = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(resultMap)
                        .replaceFirst(ArrayList.class.getSimpleName(), ArrayList.class.getSimpleName()+"<"+boe.getBusinessObjectClass().getName()+">");
            }

            return new ResponseEntity<String>(jsonData, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Unexpected exception has occured.", e);
            throw new RuntimeException("Unexpected exception has occured.");
        }
    }

    @RequestMapping(value = "/{namespace}/{dataobject}.xml", method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public ResponseEntity<String> getDataObjectsAsXML(@PathVariable("namespace") String namespace, @PathVariable("dataobject") String dataobject, HttpServletRequest request) throws Exception {
        FinancialSystemBusinessObjectEntry boe = getBusinessObject(dataobject);
        validateRequest(boe, namespace, dataobject, request);

        try {
            List<Map<String, String>> resultMap = generateResultMap(request, boe);

            String xml = null;
            if (resultMap.size() == 1) {
                xml = RestXmlUtil.toXML(boe, resultMap.get(0));
            } else {
                xml = RestXmlUtil.toXML(boe, resultMap);
            }

            return new ResponseEntity<String>(xml, HttpStatus.OK);
        } catch (Exception e) {
            LOG.error("Unexpected exception has occured.", e);
            throw new RuntimeException("Unexpected exception has occured.");
        }
    }

    protected List<Map<String, String>> generateResultMap(HttpServletRequest request, FinancialSystemBusinessObjectEntry boe) {
        List<? extends BusinessObject> results = getSearchResults(request, boe);
        List<String> inquiryFields = getInquiryFields(boe);

        List<Map<String, String>> resultMap = new ArrayList<Map<String, String>>();
        for (BusinessObject bo : results) {
            Map<String, String> objectMap = new HashMap<String, String>();
            Object object = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
            for (String propertyName : inquiryFields) {
                Object propertyValue;
                try {
                    propertyValue = ObjectUtils.getPropertyValue(bo, propertyName);
                } catch (RuntimeException e) {
                    continue;
                }

                Class<?> propertyType = ObjectUtils.getPropertyType(bo, propertyName, getPersistenceStructureService());
                if (isPropertyTypeValid(propertyType)) {
                    objectMap.put(propertyName, propertyValue + "");
                }
            }

            resultMap.add(objectMap);
        }

        return resultMap;
    }

    protected boolean isAuthorized(FinancialSystemBusinessObjectEntry boe) throws Exception {
        if (boe != null) {
            return getPermissionService().isAuthorizedByTemplate( GlobalVariables.getUserSession().getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS, KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS, KRADUtils.getNamespaceAndComponentSimpleName(boe.getBusinessObjectClass()), Collections.<String, String> emptyMap());
        } else {
            if (boe == null) {
                LOG.warn("boe is null");
                return false;
            }
        }

        return false;
    }

    protected boolean isPropertyTypeValid(Class<?> propertyType) {
        if (propertyType != null && (TypeUtils.isStringClass(propertyType)
                || TypeUtils.isIntegralClass(propertyType)
                || TypeUtils.isDecimalClass(propertyType)
                || TypeUtils.isTemporalClass(propertyType)
                || TypeUtils.isBooleanClass(propertyType))) {
            return true;
        }

        return false;
    }

    protected List<String> getInquiryFields(FinancialSystemBusinessObjectEntry boe) {
        List<String> inquiryFields = new ArrayList<String>();
        for (InquirySectionDefinition section : boe.getInquiryDefinition().getInquirySections()) {
            inquiryFields.addAll(section.getInquiryFieldNames());
        }

        return inquiryFields;
    }

    protected List<? extends BusinessObject> getSearchResults(HttpServletRequest request, FinancialSystemBusinessObjectEntry boe) {
        Map<String, String> fieldValues = new HashMap<String, String>();
        for (Object o : request.getParameterMap().keySet()) {
            String[] value = (String[]) request.getParameterMap().get(o);
            fieldValues.put(o.toString(), value[0]);
        }

        LookupableHelperService lookupableHelperService = getLookupableHelperService(boe.getLookupDefinition().getLookupableID());
        lookupableHelperService.setBusinessObjectClass(boe.getBusinessObjectClass());

        String limitByParameter = fieldValues.remove(LIMIT_BY_PARAMETER);
        String maxObjectsToReturn = fieldValues.remove(MAX_OBJECTS_TO_RETURN);

        List<? extends BusinessObject> searchResults;
        if (StringUtils.isEmpty(limitByParameter) || limitByParameter.equalsIgnoreCase("Y")) {
            searchResults = lookupableHelperService.getSearchResults(fieldValues);
        } else {
            try {
                searchResults = lookupableHelperService.getSearchResultsUnbounded(fieldValues);
            } catch (UnsupportedOperationException e) {
                LOG.warn("lookupableHelperService.getSearchResultsUnbounded failed. Retrying the lookup using the default search.", e);
                searchResults = lookupableHelperService.getSearchResults(fieldValues);
            }
        }

        if (StringUtils.isNotEmpty(maxObjectsToReturn)) {
            int searchLimit = Integer.parseInt(maxObjectsToReturn);
            if (searchLimit > 0) {
                return searchResults.subList(0, Math.min(searchResults.size(), searchLimit));
            }
        }

        return searchResults;
    }

    protected void validateRequest(FinancialSystemBusinessObjectEntry boe, String namespace, String dataobject, HttpServletRequest request) throws Exception {
        if (boe == null) {
            LOG.debug("BusinessObjectEntry is null.");
            throw new NoSuchBeanDefinitionException("Data object not found.");
        }

        Boolean isModuleLocked = getParameterService().getParameterValueAsBoolean(namespace, KfsParameterConstants.PARAMETER_ALL_DETAIL_TYPE, KRADConstants.SystemGroupParameterNames.MODULE_LOCKED_IND);
        boolean notAuthorized = !isAuthorized(boe);
        boolean moduleIsLocked = isModuleLocked != null && isModuleLocked;
        boolean noInquiryDefinition = !boe.hasInquiryDefinition();

        if (notAuthorized || moduleIsLocked || noInquiryDefinition) {
            LOG.debug("notAuthorized: " + notAuthorized);
            LOG.debug("moduleIsLocked: " + moduleIsLocked);
            LOG.debug("noInquiryDefinition: " + noInquiryDefinition);

            throw new AccessDeniedException("Not authorized.");
        }
    }

    protected FinancialSystemBusinessObjectEntry getBusinessObject(String dataobject) {
        try {
            return (FinancialSystemBusinessObjectEntry) getDataDictionaryService().getDictionaryObject(dataobject);
        } catch (NoSuchBeanDefinitionException e) {
            LOG.debug("Failed to retrieve data dictionary object.", e);
        }

        return null;
    }

    protected LookupableHelperService getLookupableHelperService(String lookupableID) {
        if (lookupableID != null) {
            return LookupableSpringContext.getLookupable(lookupableID).getLookupableHelperService();
        } else {
            return LookupableSpringContext.getLookupableHelperService(LOOKUPABLE_HELPER_SERVICE);
        }
    }

    public DataDictionaryService getDataDictionaryService() {
        if (this.dataDictionaryService == null) {
            this.dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }
        return this.dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public ParameterService getParameterService() {
        if (parameterService == null) {
            parameterService = SpringContext.getBean(ParameterService.class);
        }
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public XmlObjectSerializerService getXmlObjectSerializerService() {
        return KRADServiceLocator.getXmlObjectSerializerService();
    }

    public PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = KimApiServiceLocator.getPermissionService();
        }
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

}
