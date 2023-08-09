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
package org.kuali.kfs.sys.rest.resource.businessobject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.Control;
import org.kuali.kfs.datadictionary.FormAttribute;
import org.kuali.kfs.datadictionary.LookupDictionary;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.keyvalues.HierarchicalControlValuesFinder;
import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.rest.resource.responses.LookupResponse;

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

    private final HttpServletRequest servletRequest;
    private final BusinessObjectEntry businessObjectEntry;

    LookupResource(final HttpServletRequest servletRequest, final BusinessObjectEntry businessObjectEntry) {
        this.servletRequest = servletRequest;

        if (businessObjectEntry == null) {
            throw new NotFoundException();
        }

        this.businessObjectEntry = businessObjectEntry;
    }

    @GET
    public Response getLookup() {
        final Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            final Person user = getUserSessionFromRequest(servletRequest).getPerson();
            final AuthorizationException authorizationException = new AuthorizationException(user.getPrincipalName(),
                    "lookup", classForType.getName());
            final Response.ResponseBuilder responseBuilder = Response.status(Response.Status.FORBIDDEN);
            responseBuilder.entity(authorizationException);
            throw new ForbiddenException(responseBuilder.build());
        }

        final List<FormAttribute> lookupAttributes = getLookupAttributeForClass(classForType);
        for (final FormAttribute lookupAttribute : lookupAttributes) {
            setNestedLookupFields(lookupAttribute, classForType);
        }

        String title = getLookupDictionary().getLookupTitle(classForType);
        if (StringUtils.isEmpty(title)) {
            title = businessObjectEntry.getObjectLabel() + " Lookup";
        }
        final SearchService searchService = getLookupDictionary().getSearchService(classForType);
        if (searchService == null) {
            LOG.error(
                    "{} seems to be missing a SearchService! A lookup cannot be queried without a SearchService.",
                    businessObjectEntry::getName
            );
            throw new InternalServerErrorException("The requested lookup is currently unavailable.");
        }

        LookupResponse.Create create = null;
        if (shouldCreateNewUrlBeIncluded(classForType)) {
            create = getCreateBlock(classForType);
        }

        final LookupResponse.Results results = new LookupResponse.Results(
                searchService.getSearchResultsAttributes(classForType),
                getBusinessObjectDictionaryService().getLookupDefaultSortFieldNames(classForType));
        final LookupResponse lookupResponse = new LookupResponse(title, lookupAttributes, create, results);
        return Response.ok(lookupResponse).build();
    }

    @GET
    @Path("values")
    public Response getLookupControlValues() {
        final Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        return Response.ok(controlValuesMap).build();
    }

    @GET
    @Path("values/{attrDefnName}")
    public Response getLookupControlValues(@PathParam("attrDefnName") final String attrDefnName) {
        final Map<String, Object> controlValuesMap = buildLookupControlValuesMap(businessObjectEntry);
        Object value = controlValuesMap.get(attrDefnName);
        if (value == null) {
            if (attrDefnName != null && !doesAttrWithGivenNameExistForClass(businessObjectEntry, attrDefnName)) {
                throw new NotFoundException("Could not find the " + attrDefnName + " attribute for the "
                                + businessObjectEntry.getName() + " business object.");
            }
            value = Collections.emptyList();
        }
        return Response.ok(value).build();
    }

    protected void setNestedLookupFields(final FormAttribute lookupAttribute, final Class boClass) {
        String attributeName = lookupAttribute.getName();

        final boolean disableLookup = lookupAttribute.getDisableLookup();

        DataObjectRelationship relationship;

        if (!disableLookup) {
            relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(null, boClass,
                    attributeName, "", false);

            if (relationship == null) {
                final Class c = getPropertyType(businessObjectEntry, lookupAttribute.getName(), getPersistenceStructureService());
                if (c != null) {
                    if (lookupAttribute.getName().contains(".")) {
                        attributeName = StringUtils.substringBeforeLast(attributeName, ".");
                    }

                    final RelationshipDefinition ddReference = getBusinessObjectMetaDataService()
                            .getBusinessObjectRelationshipDefinition(boClass, attributeName);
                    relationship = getBusinessObjectMetaDataService().getBusinessObjectRelationship(ddReference,
                            null, boClass, attributeName, "", false);
                }
            }

            if (relationship != null) {
                lookupAttribute.setCanLookup(true);
                final String lookupClassName = relationship.getRelatedClass().getSimpleName();
                lookupAttribute.setLookupClassName(lookupClassName);
                lookupAttribute.setLookupRelationshipMappings(relationship.getParentToChildReferences());
            }
        }
    }

    private boolean doesAttrWithGivenNameExistForClass(final BusinessObjectEntry businessObjectEntry, final String attributeName) {
        final Class boClass = businessObjectEntry.getBusinessObjectClass();
        final List<FormAttribute> attributeDefinitions = getLookupAttributeForClass(boClass);
        for (final FormAttribute attributeDefn : attributeDefinitions) {
            if (attributeDefn.getName().equalsIgnoreCase(attributeName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildLookupControlValuesMap(final BusinessObjectEntry businessObjectEntry) {
        final Class classForType = businessObjectEntry.getBusinessObjectClass();
        if (!isAuthorizedForLookup(classForType)) {
            throw new ForbiddenException();
        }

        final Map<String, Object> valuesMap = new LinkedHashMap<>();
        final List<FormAttribute> attributes = getLookupAttributeForClass(classForType);
        for (final FormAttribute attribute: attributes) {
            final Control control = attribute.getControl();
            if (control == null) {
                continue;
            }

            final String singleAttributeName = attribute.getName();

            if (control.getType() == Control.Type.TREE) {
                // we have to do this bean resolution here b/c batch file (the only tree) is still a snowflake
                // and the DDD doesn't do the bean lookup for us (mainly b/c of the typing); hope to get rid of the
                // need for special VF type eventually
                final String valuesFinderName = control.getValuesFinderName();
                if (StringUtils.isBlank(valuesFinderName)) {
                    LOG.warn(
                            "A tree control without ValuesFinder name is most likely a mistake. BOE: {} attribute: {}",
                            businessObjectEntry::getName,
                            () -> singleAttributeName
                    );
                    continue;
                }
                final HierarchicalControlValuesFinder valuesFinder = getDataDictionaryService()
                        .getDDBean(HierarchicalControlValuesFinder.class, valuesFinderName);
                if (valuesFinder == null) {
                    LOG.warn(
                            "A tree control without a valid HierarchicalControlValuesFinder is most likely a mistake."
                            + " BOE:{} attribute: {}",
                            businessObjectEntry::getName,
                            () -> singleAttributeName
                    );
                    continue;
                }
                final List<HierarchicalData> values = valuesFinder.getHierarchicalControlValues();
                valuesMap.put(singleAttributeName, values);
            } else {
                final KeyValuesFinder valuesFinder = control.getValuesFinder();
                if (valuesFinder == null) {
                    continue;
                }
                // CU Customization: keyValues list now comes from the helper method below.
                final List<KeyValue> keyValues = getKeyValuesForLookup(valuesFinder);
                valuesMap.put(singleAttributeName, keyValues);
            }
        }
        return valuesMap;
    }

    private LookupResponse.Create getCreateBlock(final Class classForType) {
        final var actionsProvider = businessObjectEntry.getActionsProvider();
        return new LookupResponse.Create(actionsProvider.getCreateUrl(classForType), "Create New");
    }

    /*
     * CU Customization: Added this method and the one right below it,
     * to forcibly add a blank key-value entry if the values finder does not return one.
     */
    private List<KeyValue> getKeyValuesForLookup(final KeyValuesFinder valuesFinder) {
        final List<KeyValue> keyValues = valuesFinder.getKeyValues();
        if (hasEntryForBlankKey(keyValues)) {
            return keyValues;
        } else {
            final KeyValue blankKeyValue = new ConcreteKeyValue(StringUtils.EMPTY, StringUtils.EMPTY);
            return Stream.concat(Stream.of(blankKeyValue), keyValues.stream())
                    .collect(Collectors.toList());
        }
    }

    private boolean hasEntryForBlankKey(final List<KeyValue> keyValues) {
        return keyValues.stream()
                .anyMatch(keyValue -> StringUtils.isBlank(keyValue.getKey()));
    }

    private boolean shouldCreateNewUrlBeIncluded(final Class<? extends BusinessObjectBase> classForType) {
        final BusinessObjectAdminService adminService = getBusinessObjectDictionaryService().getBusinessObjectAdminService(
                classForType);
        if (adminService == null) {
            LOG.debug("{}doesn't have a BusinessObjectAdminService!", classForType::getSimpleName);
            return false;
        }

        final Person person = getUserSessionFromRequest(servletRequest).getPerson();
        return adminService.allowsNew(classForType, person) && adminService.allowsCreate(classForType, person);
    }

    // todo move this to converter code
    protected List<FormAttribute> getLookupAttributeForClass(final Class classForType) {
        final List<FormAttribute> attributes = getLookupDictionary().getLookupAttributes(classForType);
        attributes.forEach(attribute -> {
            final DefaultValueFinder defaultValueFinder = getDefaultValueFinderIfItExists(attribute);
            if (defaultValueFinder != null) {
                final String defaultValue = defaultValueFinder.getDefaultValue();
                attribute.setDefaultValue(defaultValue);
            }
        });
        return attributes;
    }

    private DefaultValueFinder getDefaultValueFinderIfItExists(final FormAttribute formAttribute) {
        final Control control = formAttribute.getControl();
        return control == null ? null : control.getDefaultValueFinder();
    }

    private boolean isAuthorizedForLookup(final Class boClass) {
        return getPermissionService().isAuthorizedByTemplate(getPrincipalId(), KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                getNamespaceAndComponentSimpleName(boClass), Collections.emptyMap());
    }

    protected String getPrincipalId() {
        return KRADUtils.getPrincipalIdFromRequest(servletRequest);
    }

    private BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = SpringContext.getBean(BusinessObjectDictionaryService.class);
        }
        return businessObjectDictionaryService;
    }

    protected void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    private BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        if (businessObjectMetaDataService == null) {
            businessObjectMetaDataService = KNSServiceLocator.getBusinessObjectMetaDataService();
        }
        return businessObjectMetaDataService;
    }

    protected void setBusinessObjectMetaDataService(final BusinessObjectMetaDataService businessObjectMetaDataService) {
        this.businessObjectMetaDataService = businessObjectMetaDataService;
    }

    public DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        }
        return dataDictionaryService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    private LookupDictionary getLookupDictionary() {
        if (lookupDictionary == null) {
            lookupDictionary = SpringContext.getBean(LookupDictionary.class);
        }
        return lookupDictionary;
    }

    protected void setLookupDictionary(final LookupDictionary lookupDictionary) {
        this.lookupDictionary = lookupDictionary;
    }

    private PermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = SpringContext.getBean(PermissionService.class);
        }
        return permissionService;
    }

    protected void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    private PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    protected void setPersistenceStructureService(final PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     */
    UserSession getUserSessionFromRequest(final HttpServletRequest request) {
        return KRADUtils.getUserSessionFromRequest(request);
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     */
    Map getNamespaceAndComponentSimpleName(final Class boClass) {
        return KRADUtils.getNamespaceAndComponentSimpleName(boClass);
    }

    /*
     * Wrapping static utility class in a method so tests can use a spy to mock this call; this way,
     * static mocking is not necessary.
     */
    Class getPropertyType(
            final BusinessObjectEntry businessObjectEntry,
            final String attrName,
            final PersistenceStructureService persistenceStructureService
    ) {
        return ObjectUtils.getPropertyType(businessObjectEntry, attrName, getPersistenceStructureService());
    }

}
