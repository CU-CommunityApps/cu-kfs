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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.web.format.CollectionFormatter;
import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.datadictionary.ActionsProvider;
import org.kuali.kfs.datadictionary.Attribute;
import org.kuali.kfs.datadictionary.DisplayAttribute;
import org.kuali.kfs.datadictionary.LookupDictionary;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.bo.BusinessObjectValueConverter;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.businessobject.serialization.BusinessObjectSerializationService;
import org.kuali.kfs.sys.businessobject.serialization.BusinessObjectSerializerManager;
import org.kuali.kfs.sys.businessobject.service.DetailsUrlService;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.rest.presentation.Link;
import org.kuali.kfs.sys.rest.presentation.LinkType;
import org.kuali.kfs.sys.rest.resource.responses.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ====
 * CU Customization: Backported the FINP-10237 changes into this file.
 * This overlay can be removed when we upgrade to the 2023-09-20 version of financials.
 * ====
 * 
 * A {@link BusinessObjectsConverter} which converts the {@code List<? extends BusinessObjectBase>} to a "Lookup JSON"
 * (i.e. not "regular JSON" or "detail JSON") response.
 */
@Component("boToLookupJsonConverter")
public class BusinessObjectsToLookupJsonConverter extends AbstractBusinessObjectsConverter {

    private static final Logger LOG = LogManager.getLogger();

    protected final BusinessObjectDictionaryService businessObjectDictionaryService;
    private final CollectionFormatter collectionFormatter;
    protected final DetailsUrlService detailsUrlService;
    private final JsonMapper jsonMapper;
    private final LookupDictionary lookupDictionary;

    @Autowired
    public BusinessObjectsToLookupJsonConverter(
            final BusinessObjectDictionaryService businessObjectDictionaryService,
            final DetailsUrlService detailsUrlService,
            @Qualifier("jsonMapperWithJavaTime") final JsonMapper jsonMapper,
            final LookupDictionary lookupDictionary
    ) {
        Validate.isTrue(businessObjectDictionaryService != null, "businessObjectDictionaryService must be provided");
        this.businessObjectDictionaryService = businessObjectDictionaryService;
        Validate.isTrue(detailsUrlService != null, "detailsUrlService must be provided");
        this.detailsUrlService = detailsUrlService;
        Validate.isTrue(jsonMapper != null, "jsonMapper must be provided");
        this.jsonMapper = jsonMapper;
        Validate.isTrue(lookupDictionary != null, "lookupDictionary must be provided");
        this.lookupDictionary = lookupDictionary;

        collectionFormatter = new CollectionFormatter();
    }

    @Override
    public String convert(final List<? extends BusinessObjectBase> businessObjects) {
        LOG.debug("convert(...) - Enter : businessObjects={}", businessObjects);
        Validate.isTrue(businessObjects != null, "businessObjects must be provided");

        final List<Map<String, Object>> serializedResults;
        if (businessObjects.isEmpty()) {
            serializedResults = List.of();
        } else {
            final BusinessObjectBase exampleBo = businessObjects.get(0);

            final Class<? extends BusinessObjectBase> businessObjectClass = exampleBo.getClass();

            final SearchService searchService = lookupDictionary.getSearchService(businessObjectClass);

            final BusinessObjectSerializationService serializationService = createBusinessObjectSerializationService(
                    exampleBo,
                    businessObjectClass,
                    searchService
            );

            final Map<String, BusinessObjectValueConverter> mappers =
                    buildDataMappers(exampleBo, businessObjectClass, businessObjects, searchService);

            final Stream<Map<String, Object>> stream = businessObjects
                    .stream()
                    .map(businessObject -> {
                        final Map<String, Object> serializedBo =
                                serializationService.serializeBusinessObject(businessObject);
                        mappers.forEach((key, mapper) -> {
                            try {
                                final Object value = PropertyUtils.getNestedProperty(serializedBo, key);
                                final Object mappedValue = mapper.convertValue(businessObject, value);
                                PropertyUtils.setNestedProperty(serializedBo, key, mappedValue);
                            } catch (final IllegalAccessException | InvocationTargetException |
                                           NoSuchMethodException e) {
                                LOG.warn("convert(...) - Unable to map serializedBo value : key={}", key);
                            }
                        });
                        return serializedBo;
                    });
            serializedResults = stream.collect(Collectors.toList());
        }

        try {
            final String json = jsonMapper.writeValueAsString(serializedResults);
            LOG.debug("convert(...) - Exit : json={}", json);
            return json;
        } catch (final JsonProcessingException e) {
            throw new BusinessObjectsConversionException(e);
        }
    }

    @Override
    protected Set<String> determineFieldsToSerialize(
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final BusinessObjectBase exampleBo
    ) {
        final Set<String> fieldsToSerialize =
                lookupDictionary.getLookupResultAttributes(businessObjectClass)
                        .stream()
                        .map(DisplayAttribute::getName)
                        .collect(Collectors.toSet());
        if (exampleBo instanceof PersistableBusinessObject) {
            fieldsToSerialize.add(KRADPropertyConstants.OBJECT_ID);
        }
        return fieldsToSerialize;
    }

    @Override
    protected BusinessObjectSerializerManager createBusinessObjectSerializerManager(
            final Class<? extends BusinessObjectBase> businessObjectClass
    ) {
        return new BusinessObjectSerializerManager(null, true);
    }

    protected Map<String, BusinessObjectValueConverter> buildDataMappers(
            final BusinessObjectBase exampleBo,
            final Class<? extends BusinessObjectBase> businessObjectClass,
            final List<? extends BusinessObjectBase> businessObjects,
            final SearchService searchService
    ) {
        final Person currentUser = GlobalVariables.getUserSession().getPerson();

        final Map<String, BusinessObjectValueConverter> mappers = new LinkedHashMap<>();
        mappers.put("actions", buildActionMapper(exampleBo, currentUser));

        final List<DisplayAttribute> resultsAttributes =
                lookupDictionary.getLookupResultAttributes(businessObjectClass);
        if (resultsAttributes == null) {
            return mappers;
        }

        for (final DisplayAttribute field : resultsAttributes) {
            final String fieldName = field.getName();
            final Attribute.Type fieldType = field.getType();

            BusinessObjectValueConverter mapper = null;
            if (fieldType == Attribute.Type.LINK) {
                mapper = buildLinkMapper(LinkType.link, bo -> searchService.urlForProperty(bo, fieldName));
            } else if (fieldType == Attribute.Type.DATE_RANGE) {
                mapper = buildDateMapper();
            } else {
                mapper = buildAnyBoDetailsOrInquiryMapper(businessObjects, fieldName);

                if (mapper == null) {
                    final Field propertyField = FieldUtils.getField(businessObjectClass, fieldName, true);
                    if (propertyField != null && Collection.class.isAssignableFrom(propertyField.getType())) {
                        // ==== CU Customization: Updated this line with the FINP-10237 changes. ====
                        mapper = buildCollectionMapper(fieldName);
                    }
                }
            }

            if (mapper != null) {
                mappers.put(fieldName, mapper);
            }
        }

        return mappers;
    }

    private BusinessObjectValueConverter buildActionMapper(
            final BusinessObjectBase bob,
            final Person user
    ) {
        final ActionsProvider actionsProvider =
                businessObjectDictionaryService.getBusinessObjectEntry(bob.getClass().getName()).getActionsProvider();
        return (bo, originalValue) -> {
            final List<Action> actionMaps = actionsProvider.getActionLinks(bo, user);
            if (CollectionUtils.isEmpty(actionMaps)) {
                return null;
            }
            return actionMaps
                    .stream()
                    .map(ActionResponse::from)
                    .collect(Collectors.toList());
        };
    }

    private static BusinessObjectValueConverter buildLinkMapper(
            final LinkType linkType,
            final Function<? super BusinessObjectBase, String> linkFunction
    ) {
        return (bo, originalValue) -> {
            if (originalValue == null || StringUtils.isBlank(originalValue.toString())) {
                return null;
            }

            final String url = linkFunction.apply(bo);
            if (StringUtils.isBlank(url)) {
                return originalValue.toString();
            }
            return new Link(originalValue.toString(), url, linkType);
        };
    }

    private static BusinessObjectValueConverter buildDateMapper() {
        return (businessObject, date) -> {
            if (date instanceof Date) {
                return ((Date) date).getTime();
            }
            return date;
        };
    }

    private BusinessObjectValueConverter buildAnyBoDetailsOrInquiryMapper(final List<? extends BusinessObjectBase> businessObjects, final String fieldName) {
        BusinessObjectValueConverter mapper = null;
        final boolean anyBoHasDetailsLink = businessObjects
                .stream()
                .anyMatch(bo ->
                        detailsUrlService.isStringDetailsLink(lookupDictionary.getDetailsUrl(bo, fieldName)));
        if (anyBoHasDetailsLink) {
            mapper = buildLinkMapper(LinkType.details, bo -> lookupDictionary.getDetailsUrl(bo, fieldName));
        } else {
            final boolean anyBoHasInquiryField = businessObjects
                    .stream()
                    .anyMatch(bo -> StringUtils.isNotEmpty(lookupDictionary.getDetailsUrl(bo, fieldName)));
            if (anyBoHasInquiryField) {
                mapper = buildLinkMapper(LinkType.inquiry, bo -> lookupDictionary.getDetailsUrl(bo, fieldName));
            }
        }
        return mapper;
    }

    // ==== CU Customization: Updated this method with the FINP-10237 changes. ====
    private BusinessObjectValueConverter buildCollectionMapper(final String fieldName) {
        return (businessObject, collection) -> {
            try {
                final Object value = PropertyUtils.getProperty(businessObject, fieldName);
                if (value == null) {
                    return collection;
                }
                final String formattedString = (String) collectionFormatter.format(value);
                return StringUtils.stripEnd(StringUtils.stripStart(formattedString, "["), "]");
            } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return collection;
            }
        };
    }

}
