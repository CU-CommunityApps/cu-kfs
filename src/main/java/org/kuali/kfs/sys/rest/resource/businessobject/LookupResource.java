/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.sys.rest.resource.businessobject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.Control;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.datadictionary.LookupDictionary;
import org.kuali.kfs.kns.datadictionary.control.MultiselectControlDefinition;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.datadictionary.LookupAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.kns.datadictionary.control.ControlDefinition;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.keyvalues.HierarchicalControlValuesFinder;
import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.permission.PermissionService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * CU Customization:
 * Updated the code that retrieves the key-value pairs from the values finder,
 * so that it will add an empty key-value pair if one is not present.
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LookupResource {

    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectDictionaryService businessObjectDictionaryService;
    private BusinessObjectMetaDataService businessObjectMetaDataService;
    private DataDictionaryService dataDictionaryService;
    private LookupDictionary lookupDictionary;
    private PermissionService permissionService;
    private PersistenceStructureService persistenceStructureService;

    private HttpServletRequest servletRequest;
    private BusinessObjectEntry businessObjectEntry;

    LookupResource(HttpServletRequest servletRequest, BusinessObjectEntry businessObjectEntry) {
        this.servletRequest = servletRequest;

        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        this.businessObjectEntry = businessObjectEntry;
    }

    @GET
    public Response getLookupForm() {
        Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            Person user = KRADUtils.getUserSessionFromRequest(this.servletRequest).getPerson();
            AuthorizationException authorizationException = new AuthorizationException(user.getPrincipalName(),
                    "lookup", classForType.getName());
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.FORBIDDEN);
            responseBuilder.entity(authorizationException);
            throw new ForbiddenException(responseBuilder.build());
        }

        List<LookupAttributeDefinition> lookupAttributeDefns = getLookupAttributeDefinitionsForClass(classForType);

        for (LookupAttributeDefinition lookupAttributeDefn : lookupAttributeDefns) {
            setNestedLookupFields(lookupAttributeDefn, classForType);
        }

        String title = getLookupDictionary().getLookupTitle(classForType);
        if (StringUtils.isEmpty(title)) {
            title = businessObjectEntry.getObjectLabel() + " Lookup";
        }
        LookupSearchService searchService = getLookupDictionary().getLookupSearchService(classForType);
        if (searchService == null) {
            LOG.error(businessObjectEntry.getName() + " seems to be missing a LookupSearchService! A lookup cannot " +
                    "be queried without a LookupSearchService.");
            throw new InternalServerErrorException("The requested lookup is currently unavailable.");
        }

        Map<String, Object> resultsList = new LinkedHashMap<>();
        resultsList.put("fields", searchService.getSearchResultsAttributes(classForType));
        resultsList.put("defaultSortFields",
                getBusinessObjectDictionaryService().getLookupDefaultSortFieldNames(classForType));

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("title", title);
        if (shouldCreateNewUrlBeIncluded(classForType)) {
            responseData.put("create", getCreateBlock(classForType));
        }
        responseData.put("form", lookupAttributeDefns);
        responseData.put("results", resultsList);

        return Response.ok(responseData).build();
    }

    @GET
    @Path("values")
    public Response getLookupControlValues() {
        Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        return Response.ok(controlValuesMap).build();
    }

    @GET
    @Path("values/{attrDefnName}")
    public Response getLookupControlValues(@PathParam("attrDefnName") String attrDefnName) {
        Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        Object value = controlValuesMap.get(attrDefnName);
        if (value == null) {
            if (attrDefnName != null && !doesAttrDefnWithGivenNameExistForClass(businessObjectEntry, attrDefnName)) {
                throw new NotFoundException("Could not find the " + attrDefnName + " attribute for the "
                                + businessObjectEntry.getName() + " business object.");
            }
            value = Collections.emptyList();
        }
        return Response.ok(value).build();
    }

    protected void setNestedLookupFields(LookupAttributeDefinition lookupAttributeDefn, Class boClass) {
        String attributeName = lookupAttributeDefn.getName();

        boolean disableLookup = lookupAttributeDefn.getDisableLookup();

        DataObjectRelationship relationship;

        if (!disableLookup) {
            relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(null, boClass,
                    attributeName, "", false);

            if (relationship == null) {
                Class c = ObjectUtils.getPropertyType(businessObjectEntry, lookupAttributeDefn.getName(),
                        getPersistenceStructureService());
                if (c != null) {
                    if (lookupAttributeDefn.getName().contains(".")) {
                        attributeName = StringUtils.substringBeforeLast(attributeName, ".");
                    }

                    RelationshipDefinition ddReference = getBusinessObjectMetaDataService()
                            .getBusinessObjectRelationshipDefinition(boClass, attributeName);
                    relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(ddReference,
                            null, boClass, attributeName, "", false);
                }
            }

            if (relationship != null) {
                lookupAttributeDefn.setCanLookup(true);
                String lookupClassName = relationship.getRelatedClass().getSimpleName();
                lookupAttributeDefn.setLookupClassName(lookupClassName);
                lookupAttributeDefn.setLookupRelationshipMappings(relationship.getParentToChildReferences());
            }
        }
    }

    private boolean doesAttrDefnWithGivenNameExistForClass(BusinessObjectEntry businessObjectEntry, String attrDefnName) {
        Class boClass = businessObjectEntry.getBusinessObjectClass();
        List<LookupAttributeDefinition> attributeDefinitions = getLookupAttributeDefinitionsForClass(boClass);
        for (LookupAttributeDefinition attributeDefn : attributeDefinitions) {
            if (attributeDefn.getName().equalsIgnoreCase(attrDefnName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildLookupControlValuesMap(BusinessObjectEntry businessObjectEntry) {
        Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            throw new ForbiddenException();
        }

        Map<String, Object> valuesMap = new LinkedHashMap<>();
        List<LookupAttributeDefinition> attributes = getLookupAttributeDefinitionsForClass(classForType);
        for (LookupAttributeDefinition attribute: attributes) {
            Control control = attribute.getControlNew();
            String singleAttributeName = attribute.getName();

            if (control.getType() == Control.Type.TREE) {
                // we have to do this bean resolution here b/c batch file (the only tree) is still a snowflake
                // and the DDD doesn't do the bean lookup for us (mainly b/c of the typing); hope to get rid of the
                // need for special VF type eventually
                String valuesFinderName = control.getValuesFinderName();
                if (StringUtils.isBlank(valuesFinderName)) {
                    LOG.warn("A tree control without ValuesFinder name is most likely a mistake. BOE: " +
                            businessObjectEntry.getName() + " attribute: " + singleAttributeName);
                    continue;
                }
                HierarchicalControlValuesFinder valuesFinder = getDataDictionaryService()
                        .getDDBean(HierarchicalControlValuesFinder.class, valuesFinderName);
                if (valuesFinder == null) {
                    LOG.warn("A tree control without a valid HierarchicalControlValuesFinder is most likely a " +
                            "mistake. BOE:" + businessObjectEntry.getName() + " attribute: " + singleAttributeName);
                    continue;
                }
                List<HierarchicalData> values = valuesFinder.getHierarchicalControlValues();
                valuesMap.put(singleAttributeName, values);
            } else {
                KeyValuesFinder valuesFinder = control.getValuesFinder();
                if (valuesFinder == null) {
                    continue;
                }
                // CU Customization: keyValues list now comes from the helper method below.
                List<KeyValue> keyValues = getKeyValuesForLookup(valuesFinder);
                valuesMap.put(singleAttributeName, keyValues);
            }
        }
        return valuesMap;
    }

    /*
     * CU Customization: Added this method and the one right below it,
     * to forcibly add a blank key-value entry if the values finder does not return one.
     */
    private List<KeyValue> getKeyValuesForLookup(KeyValuesFinder valuesFinder) {
        List<KeyValue> keyValues = valuesFinder.getKeyValues();
        if (hasEntryForBlankKey(keyValues)) {
            return keyValues;
        } else {
            KeyValue blankKeyValue = new ConcreteKeyValue(StringUtils.EMPTY, StringUtils.EMPTY);
            return Stream.concat(Stream.of(blankKeyValue), keyValues.stream())
                    .collect(Collectors.toList());
        }
    }

    private boolean hasEntryForBlankKey(List<KeyValue> keyValues) {
        return keyValues.stream()
                .anyMatch(keyValue -> StringUtils.isBlank(keyValue.getKey()));
    }

    private Map<String, String> getCreateBlock(Class classForType) {
        String url = getCreateNewUrl(classForType);
        Map<String, String> createBlock = new LinkedHashMap<>();
        createBlock.put("url", "kr/" + url);
        createBlock.put("label", "Create New");
        return createBlock;
    }

    private String getCreateNewUrl(Class<? extends BusinessObjectBase> classForType) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.MAINTENANCE_NEW_METHOD_TO_CALL);
        parameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, classForType.getName());
        return UrlFactory.parameterizeUrl(KRADConstants.MAINTENANCE_ACTION, parameters);
    }

    private boolean shouldCreateNewUrlBeIncluded(Class<? extends BusinessObjectBase> classForType) {
        BusinessObjectAdminService adminService = getBusinessObjectDictionaryService().getBusinessObjectAdminService(
                classForType);
        if (adminService == null) {
            LOG.debug(classForType.getSimpleName() + "doesn't have a BusinessObjectAdminService!");
            return false;
        }

        Person person = KRADUtils.getUserSessionFromRequest(this.servletRequest).getPerson();
        return adminService.allowsNew(classForType, person) && adminService.allowsCreate(classForType, person);
    }

    private boolean isHierarchical(ControlDefinition controlDefn) {
        return controlDefn instanceof MultiselectControlDefinition &&
                ((MultiselectControlDefinition) controlDefn).isHierarchical();
    }

    protected List<LookupAttributeDefinition> getLookupAttributeDefinitionsForClass(Class classForType) {
        List<LookupAttributeDefinition> attributeDefinitions = getLookupDictionary().getLookupAttributes(classForType);
        attributeDefinitions.forEach(attributeDefinition -> {
            DefaultValueFinder defaultValueFinder = attributeDefinition.getDefaultValueFinder();
            if (defaultValueFinder != null) {
                String defaultValue = defaultValueFinder.getDefaultValue();
                attributeDefinition.setDefaultValue(defaultValue);
            }
        });
        return attributeDefinitions;
    }

    private boolean isAuthorizedForLookup(Class boClass) {
        return getPermissionService().isAuthorizedByTemplate(getPrincipalId(), KRADConstants.KNS_NAMESPACE,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                KRADUtils.getNamespaceAndComponentSimpleName(boClass), Collections.emptyMap());
    }

    private String getPrincipalId() {
        return KRADUtils.getPrincipalIdFromRequest(servletRequest);
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = SpringContext.getBean(BusinessObjectDictionaryService.class);
        }
        return businessObjectDictionaryService;
    }

    protected void setBusinessObjectDictionaryService(
            BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    private BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        if (businessObjectMetaDataService == null) {
            businessObjectMetaDataService = KNSServiceLocator.getBusinessObjectMetaDataService();
        }
        return businessObjectMetaDataService;
    }

    protected void setBusinessObjectMetaDataService(BusinessObjectMetaDataService businessObjectMetaDataService) {
        this.businessObjectMetaDataService = businessObjectMetaDataService;
    }

    public DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        }
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    private LookupDictionary getLookupDictionary() {
        if (lookupDictionary == null) {
            lookupDictionary = SpringContext.getBean(LookupDictionary.class);
        }
        return lookupDictionary;
    }

    protected void setLookupDictionary(LookupDictionary lookupDictionary) {
        this.lookupDictionary = lookupDictionary;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = SpringContext.getBean(PermissionService.class);
        }
        return permissionService;
    }

    protected void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    protected void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }
}
