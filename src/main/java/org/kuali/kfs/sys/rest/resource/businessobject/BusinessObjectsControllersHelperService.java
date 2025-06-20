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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.ConcreteKeyValue;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.ActionsProvider;
import org.kuali.kfs.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.datadictionary.Control;
import org.kuali.kfs.datadictionary.DisplayAttribute;
import org.kuali.kfs.datadictionary.FormAttribute;
import org.kuali.kfs.datadictionary.LookupDictionary;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.api.permission.PermissionService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.keyvalues.HierarchicalControlValuesFinder;
import org.kuali.kfs.krad.keyvalues.HierarchicalData;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.rest.resource.responses.LookupResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ====
 * CU Customization:
 * Updated the code that retrieves the key-value pairs from the values finder,
 * so that it will add an empty key-value pair if one is not present.
 * ====
 *
 *
 * This class has extracted shared logic between {@link BusinessObjectsController},
 * {@link BusinessObjectsLookupController}, and {@link BatchFileController}.
 *
 */
@Component
class BusinessObjectsControllersHelperService {

    private static final Logger LOG = LogManager.getLogger();

    private final BusinessObjectDictionaryService businessObjectDictionaryService;
    private final BusinessObjectMetaDataService businessObjectMetaDataService;
    private final DataDictionaryService dataDictionaryService;
    private final KualiModuleService kualiModuleService;
    private final LookupDictionary lookupDictionary;
    private final ParameterService parameterService;
    private final PermissionService permissionService;

    @Autowired
    BusinessObjectsControllersHelperService(
            final BusinessObjectDictionaryService businessObjectDictionaryService,
            final BusinessObjectMetaDataService businessObjectMetaDataService,
            final DataDictionaryService dataDictionaryService,
            final KualiModuleService kualiModuleService,
            final LookupDictionary lookupDictionary,
            final ParameterService parameterService,
            final PermissionService permissionService
    ) {
        Validate.isTrue(businessObjectDictionaryService != null, "businessObjectDictionaryService must be provided");
        this.businessObjectDictionaryService = businessObjectDictionaryService;
        Validate.isTrue(businessObjectMetaDataService != null, "businessObjectMetaDataService must be provided");
        this.businessObjectMetaDataService = businessObjectMetaDataService;
        Validate.isTrue(dataDictionaryService != null, "dataDictionaryService must be provided");
        this.dataDictionaryService = dataDictionaryService;
        Validate.isTrue(kualiModuleService != null, "kualiModuleService must be provided");
        this.kualiModuleService = kualiModuleService;
        Validate.isTrue(lookupDictionary != null, "lookupDictionary must be provided");
        this.lookupDictionary = lookupDictionary;
        Validate.isTrue(parameterService != null, "parameterService must be provided");
        this.parameterService = parameterService;
        Validate.isTrue(permissionService != null, "permissionService must be provided");
        this.permissionService = permissionService;
    }

    LookupResponse getLookup(
            final UserSession userSession, final String businessObjectName
    ) throws InstantiationException, IllegalAccessException {
        LOG.debug("getLookup(...) - Enter : businessObjectName={}", businessObjectName);
        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);

        final Class<? extends BusinessObject> businessObjectClass = businessObjectEntry.getBusinessObjectClass();

        final Person user = userSession.getPerson();
        final ModuleService moduleService = kualiModuleService.getResponsibleModuleService(businessObjectClass);

        if (isModuleLocked(businessObjectClass, user, moduleService)) {
            final String messageParamNamespaceCode = moduleService.getModuleConfiguration().getNamespaceCode();
            final String messageParamComponentCode = KRADConstants.DetailTypes.ALL_DETAIL_TYPE;
            final String messageParamName = KRADConstants.SystemGroupParameterNames.OLTP_LOCKOUT_MESSAGE_PARM;
            String lockoutMessage = parameterService.getParameterValueAsString(messageParamNamespaceCode,
                    messageParamComponentCode,
                    messageParamName
            );

            if (StringUtils.isBlank(lockoutMessage)) {
                final String defaultMessageParamName = KRADConstants.SystemGroupParameterNames.MODULE_LOCKED_MESSAGE;
                lockoutMessage = parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.KFS,
                        messageParamComponentCode,
                        defaultMessageParamName
                );
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, lockoutMessage);
        }

        if (notAuthorizedToView(businessObjectClass, user.getPrincipalId())) {
            // TODO: Of the 3 methods, this one creates this AuthorizationException. Keeping it like this for now for
            //       parity with the existing APIs. Revisit and standard (one way or the other) and use
            //       ensureAuthorization(...) here
            final AuthorizationException authorizationException = new AuthorizationException(
                    user.getPrincipalName(),
                    "lookup",
                    businessObjectClass.getName()
            );
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized", authorizationException);
        }

        final Class<? extends BusinessObjectBase> businessObjectBaseClass =
                (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();

        final SearchService searchService = getSearchService(businessObjectBaseClass);

        final List<FormAttribute> lookupAttributes = searchService.getFormAttributes(businessObjectBaseClass);
        // We are skipping nested lookup configuration for BatchFile, as it doesn't have any, and a BatchFile object
        // cannot be instantiated without a file path or File object to determine the relationship.
        // TODO: Move all of the BatchFile stuff into it's own thing at some point
        if (!Objects.equals(businessObjectBaseClass, BatchFile.class)) {
            for (final FormAttribute lookupAttribute : lookupAttributes) {
                setNestedLookupFields(lookupAttribute, businessObjectClass);
            }
        }

        String title = lookupDictionary.getLookupTitle(businessObjectBaseClass);
        if (StringUtils.isEmpty(title)) {
            title = businessObjectEntry.getObjectLabel() + " Lookup";
        }

        final LookupResponse.Create create = shouldCreateNewUrlBeIncluded(businessObjectBaseClass, user)
                ? getCreateBlock(businessObjectEntry, businessObjectClass)
                : null;

        final List<DisplayAttribute> searchResultsAttributes =
                searchService.getSearchResultsAttributes(businessObjectClass);
        final List<String> lookupDefaultSortFieldNames =
                businessObjectDictionaryService.getLookupDefaultSortFieldNames(businessObjectClass);
        final LookupResponse.Results results =
                new LookupResponse.Results(searchResultsAttributes, lookupDefaultSortFieldNames);

        final LookupResponse response = new LookupResponse(title, lookupAttributes, create, results);
        LOG.debug("getLookup(...) - Exit : response={}", response);
        return response;
    }

    Map<String, Object> getLookupValues(
            final UserSession userSession, final String businessObjectName
    ) {
        LOG.debug("getLookupValues(...) - Enter : businessObjectName={}", businessObjectName);

        final BusinessObjectEntry businessObjectEntry =
                businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);

        ensureAuthorization(userSession, businessObjectEntry);

        final Map<String, Object> valuesMap = new LinkedHashMap<>();

        final Class<? extends BusinessObjectBase> businessObjectBaseClass =
                (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
        final SearchService searchService = getSearchService(businessObjectBaseClass);
        final List<FormAttribute> attributes = searchService.getFormAttributes(businessObjectBaseClass);
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
                            "getLookupValues(...) - Cannot find a ValuesFinder : businessObjectName={}; attributename={}",
                            businessObjectEntry::getName,
                            () -> singleAttributeName
                    );
                    continue;
                }
                final HierarchicalControlValuesFinder valuesFinder =
                        dataDictionaryService.getDDBean(HierarchicalControlValuesFinder.class, valuesFinderName);
                if (valuesFinder == null) {
                    LOG.warn(
                            "getLookupValues(...) - Cannot find a HierarchicalControlValuesFinder : businessObjectName={}; attributename={}",
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

        LOG.debug("getLookupValues(...) - Exit : response={}", valuesMap);
        return valuesMap;
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
                    .collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private boolean hasEntryForBlankKey(final List<KeyValue> keyValues) {
        return keyValues.stream()
                .anyMatch(keyValue -> StringUtils.isBlank(keyValue.getKey()));
    }

    Object getLookupValuesAttributeDefinition(
            final UserSession userSession, final String businessObjectName, final String attributeName
    ) {
        LOG.debug(
                "getLookupValuesAttributeDefinition(...) - Enter : businessObjectName={}; attributeName={}",
                businessObjectName,
                attributeName
        );

        final Map<String, Object> lookupValues = getLookupValues(userSession, businessObjectName);

        Object value = lookupValues.get(attributeName);
        if (value == null) {
            final BusinessObjectEntry businessObjectEntry =
                    businessObjectDictionaryService.getBusinessObjectEntry(businessObjectName);

            final Class<? extends BusinessObjectBase> businessObjectBaseClass =
                    (Class<? extends BusinessObjectBase>) businessObjectEntry.getBusinessObjectClass();
            if (attributeName != null
                && !doesAttrWithGivenNameExistForClass(businessObjectBaseClass, attributeName)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Could not find the " + attributeName + " attribute for the "
                        + businessObjectEntry.getName() + " business object."
                );
            }
            value = List.of();
        }

        LOG.debug("getLookupValuesAttributeDefinition(...) - Exit : response={}", value);
        return value;
    }

    private void ensureAuthorization(
            final UserSession userSession,
            final BusinessObjectEntry businessObjectEntry
    ) {
        final Class<? extends BusinessObject> businessObjectClass = businessObjectEntry.getBusinessObjectClass();

        if (notAuthorizedToView(businessObjectClass, userSession.getPrincipalId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private boolean doesAttrWithGivenNameExistForClass(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final String attributeDefinitionName
    ) {
        final SearchService searchService = getSearchService(businessObjectClass);
        final List<FormAttribute> attributeDefinitions = searchService.getFormAttributes(businessObjectClass);
        for (final FormAttribute attributeDefinition : attributeDefinitions) {
            if (attributeDefinition.getName().equalsIgnoreCase(attributeDefinitionName)) {
                return true;
            }
        }
        return false;
    }

    private void setNestedLookupFields(
            final FormAttribute lookupAttribute,
            final Class<? extends BusinessObject> businessObjectClass
    ) throws InstantiationException, IllegalAccessException {
        final boolean disableLookup = lookupAttribute.getDisableLookup();
        if (disableLookup) {
            return;
        }

        final String attributeName = lookupAttribute.getName();

        final DataObjectRelationship relationship = businessObjectMetaDataService.getBusinessObjectRelationship(
                businessObjectClass.newInstance(),
                businessObjectClass,
                attributeName,
                "",
                false
        );

        if (relationship != null) {
            lookupAttribute.setCanLookup(true);
            final String lookupClassName = relationship.getRelatedClass().getSimpleName();
            lookupAttribute.setLookupClassName(lookupClassName);
            lookupAttribute.setLookupRelationshipMappings(relationship.getParentToChildReferences());
        }
    }

    private boolean shouldCreateNewUrlBeIncluded(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final Person user
    ) {
        final BusinessObjectAdminService adminService =
                businessObjectDictionaryService.getBusinessObjectAdminService(businessObjectClass);
        if (adminService == null) {
            LOG.warn(
                    "shouldCreateNewUrlBeIncluded(...) - Cannot find a BusinessObjectAdminService : "
                    + "businessObjectClass={}",
                    businessObjectClass::getSimpleName
            );
            return false;
        }

        final boolean allowsNew = adminService.allowsNew(businessObjectClass, user);
        final boolean allowsCreate = adminService.allowsCreate(businessObjectClass, user);
        return allowsNew && allowsCreate;
    }

    static LookupResponse.Create getCreateBlock(
            final BusinessObjectEntry businessObjectEntry,
            final Class<? extends BusinessObject> businessObjectClass
    ) {
        final ActionsProvider actionsProvider = businessObjectEntry.getActionsProvider();
        final String createUrl = actionsProvider.getCreateUrl(businessObjectClass);
        return new LookupResponse.Create(createUrl, "Create New");
    }

    boolean notAuthorizedToView(
            final Class<?> boClass, final String principalId
    ) {
        final Map<String, String> permissionDetails = KRADUtils.getNamespaceAndComponentSimpleName(boClass);
        final boolean authorized = permissionService.isAuthorizedByTemplate(
                principalId,
                KFSConstants.CoreModuleNamespaces.KFS,
                KimConstants.PermissionTemplateNames.LOOK_UP_RECORDS,
                permissionDetails,
                Map.of()
        );
        LOG.debug("notAuthorizedToView(...) - Exit : authorized={}", authorized);
        return !authorized;
    }

    private SearchService getSearchService(final Class businessObjectBaseClass) {
        final SearchService searchService = lookupDictionary.getSearchService(businessObjectBaseClass);
        if (searchService == null) {
            LOG.error(
                    "getLookup(...) - Cannot find a SearchService; cannot query the Lookup : businessObjectName={}",
                    businessObjectBaseClass
            );
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The requested lookup is currently unavailable."
            );
        }
        return searchService;
    }

    private boolean isModuleLocked(final Class<?> boClass, final Person user, final ModuleService moduleService) {
        if (moduleService != null && moduleService.isLocked()) {
            final String principalId = user.getPrincipalId();
            final String namespaceCode = KFSConstants.CoreModuleNamespaces.KFS;
            final String permissionName = KimConstants.PermissionNames.ACCESS_LOCKED_MODULE;
            final Map<String, String> qualification = KRADUtils.getNamespaceAndComponentSimpleName(boClass);

            if (!permissionService.isAuthorized(principalId, namespaceCode, permissionName, qualification)) {
                return true;
            }
        }
        return false;
    }

}
