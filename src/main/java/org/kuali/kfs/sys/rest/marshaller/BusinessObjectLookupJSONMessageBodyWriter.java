/**
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2019 Kuali, Inc.
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
package org.kuali.kfs.sys.rest.marshaller;

import com.google.gson.Gson;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.LookupResultAttributeDefinition;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.sys.businessobject.serialization.BusinessObjectSerializationService;
import org.kuali.kfs.sys.rest.util.KualiMediaType;
import org.kuali.rice.kim.api.identity.Person;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Produces(KualiMediaType.LOOKUP_JSON)
public class BusinessObjectLookupJSONMessageBodyWriter extends BusinessObjectMessageBodyWriter {
    private static final Logger LOG = LogManager.getLogger();
    private Gson gson = new Gson();

    @Override
    public void writeTo(List<BusinessObjectBase> businessObjects, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws WebApplicationException {

        List<Map<String, Object>> serializedResults;
        if (businessObjects.isEmpty()) {
            serializedResults = Collections.emptyList();
        } else {
            Person user = getCurrentUser();
            BusinessObjectBase firstBo = businessObjects.get(0);
            Map<String, BiFunction<BusinessObjectBase, Object, Object>> mappers = buildDataMappers(businessObjects, user);
            Set<String> fieldsToSerialize = getBusinessObjectDictionaryService()
                    .getLookupResultAttributeDefinitions(firstBo.getClass()).stream()
                    .map(LookupResultAttributeDefinition::getName).collect(Collectors.toSet());

            BusinessObjectRestrictions businessObjectRestrictions = getSearchServiceForBusinessObject(firstBo)
                    .getBusinessObjectAuthorizationService().getLookupResultRestrictions(firstBo, user);

            BusinessObjectSerializationService serializationService =
                    new BusinessObjectSerializationService(fieldsToSerialize, businessObjectRestrictions, true);

            Stream<Map<String, Object>> stream = businessObjects.stream().map(businessObject -> {
                Map<String, Object> serialized = serializationService.serializeBusinessObject(businessObject);
                mappers.keySet().stream().forEach(key -> {
                    try {
                        Object value = PropertyUtils.getNestedProperty(serialized, key);
                        Object mappedValue = mappers.get(key).apply(businessObject, value);
                        PropertyUtils.setNestedProperty(serialized, key, mappedValue);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        LOG.warn("Unable to map serialized value at key:" + key);
                    }
                });
                return serialized;
            });
            serializedResults = stream.collect(Collectors.toList());
        }

        String json = gson.toJson(serializedResults);
        try {
            entityStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            LOG.error("Unable to write data to response for business object " + ioe.getMessage());
            throw new WebApplicationException();
        }
    }

    private Map<String, BiFunction<BusinessObjectBase, Object, Object>> buildDataMappers(
            List<BusinessObjectBase> businessObjects, Person user) {
        BusinessObjectBase exampleBo = businessObjects.get(0);
        LookupSearchService searchService = getSearchServiceForBusinessObject(exampleBo);
        LinkedHashMap<String, BiFunction<BusinessObjectBase, Object, Object>> mappers = new LinkedHashMap<>();
        mappers.put("actions", (businessObject, originalValue) -> {
            List<Map<String, Object>> actionLinks = searchService.getActionLinks(businessObject, user);
            return actionLinks.isEmpty() ? null : actionLinks;
        });

        List<LookupResultAttributeDefinition> resultsAttributes = getBusinessObjectDictionaryService()
                .getLookupResultAttributeDefinitions(exampleBo.getClass());
        if (resultsAttributes == null) {
            return mappers;
        }

        for (LookupResultAttributeDefinition field: resultsAttributes) {
            String fieldName = field.getName();
            AttributeDefinition.Type fieldType = field.getType();
            BiFunction<BusinessObjectBase, Object, Object> mapper = null;

            // Empty/null values won't have an inquiry, so we need to check all the fields to see if any have an
            // inquiry, not just the first. Otherwise, we might end up with fields that don't have an inquiry link when
            // they should, in the case where the first BO field is empty/null, but subsequent fields have values.
            boolean anyFieldHasInquiry = businessObjects.stream()
                    .anyMatch(businessObject -> searchService.hasInquiry(businessObject, field));

            if (anyFieldHasInquiry) {
                mapper = buildInquiryMapper(fieldName, searchService);
            } else if (fieldType.equals(AttributeDefinition.Type.DATE_RANGE)) {
                mapper = buildDateMapper();
            }

            if (mapper != null) {
                mappers.put(fieldName, mapper);
            }
        }

        return mappers;
    }

    private BiFunction<BusinessObjectBase, Object, Object> buildInquiryMapper(String fieldName,
                                                                              LookupSearchService searchService) {
        return (bo, originalValue) -> {
            Map<String, Object> inquiry = new LinkedHashMap<>();
            inquiry.put("type", "inquiry");
            inquiry.put("url", searchService.inquiryUrl(bo, fieldName));
            inquiry.put("value", originalValue);
            return inquiry;
        };
    }

    private BiFunction<BusinessObjectBase, Object, Object> buildDateMapper() {
        return (businessObject, date) -> {
            if (date instanceof Date) {
                return ((Date) date).getTime();
            } else {
                return date;
            }
        };
    }
}
