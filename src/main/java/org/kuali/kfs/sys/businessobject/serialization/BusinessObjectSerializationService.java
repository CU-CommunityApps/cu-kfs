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
package org.kuali.kfs.sys.businessobject.serialization;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.filter.SquigglyPropertyFilter;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.krad.bo.BusinessObjectBase;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BusinessObjectSerializationService {
    private static final Logger LOG = LogManager.getLogger();
    private ObjectMapper objectMapper;
    private Set<String> fieldsToSerialize;
    private SimpleFilterProvider filterProvider;
    private BusinessObjectRestrictions businessObjectRestrictions;
    private BusinessObjectSerializers businessObjectSerializers;

    public BusinessObjectSerializationService(Set<String> fields, BusinessObjectRestrictions businessObjectRestrictions,
                                              boolean serializeProxies) {
        this.setFieldsToSerialize(fields);
        this.businessObjectRestrictions = businessObjectRestrictions;
        this.businessObjectSerializers = new BusinessObjectSerializers(serializeProxies);
    }

    public Map<String, Object> serializeBusinessObject(BusinessObjectBase businessObject) {
        ObjectMapper mapper = getObjectMapper();
        final Map<String, Object> map = mapper.convertValue(businessObject, Map.class);
        maskSerializedObject(map);
        return map;
    }

    private void maskSerializedObject(Map<String, Object> map) {
        if (businessObjectRestrictions != null && businessObjectRestrictions.hasAnyFieldRestrictions()) {
            businessObjectRestrictions.getAllFieldRestrictionNames().stream()
                    .map(businessObjectRestrictions::getFieldRestriction)
                    .forEach(restriction -> {
                        try {
                            Object value = PropertyUtils.getNestedProperty(map, restriction.getFieldName());
                            String maskedValue = restriction.getMaskFormatter().maskValue(value);
                            PropertyUtils.setNestedProperty(map, restriction.getFieldName(), maskedValue);
                        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            LOG.info("Unable to set masked property " + restriction.getFieldName());
                        }
                    });
        }
    }

    public ObjectMapper getObjectMapper() {
        SimpleFilterProvider filterProvider = getFilterProvider();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setFilterProvider(filterProvider);

            SimpleModule module = new SimpleModule();
            module.setSerializers(businessObjectSerializers);
            objectMapper.registerModule(module);
        }
        return objectMapper;
    }

    public SimpleFilterProvider getFilterProvider() {
        if (filterProvider == null) {
            filterProvider = new SimpleFilterProvider();
        }
        return filterProvider;
    }

    public void setBusinessObjectRestrictions(BusinessObjectRestrictions boRestrictions) {
        this.businessObjectRestrictions = boRestrictions;
    }

    /**
     * Updates the filter on the object mapper to set which fields should be serialized. The filter being used is the
     * SquigglyPropertyFilter which allows for a robust graph like language for selecting fields, child fields,
     * wildcards etc. More information can be found here: https://github.com/bohnman/squiggly-java
     *
     * @param fields A set of strings that will be matched against the bo keys during serialization
     */
    public void setFieldsToSerialize(Set<String> fields) {
        fieldsToSerialize = fields;
        if (fieldsToSerialize.size() > 0) {
            // Property filters work through mixins on a class. In order to inject into this mechanism during runtime
            // we have to create a mixin with our placeholder class and set a filter with the same name
            Map<Class<?>, Class<?>> mixins = new HashMap<>();
            mixins.put(Object.class, BusinessObjectSerializationService.PropertyFilterMixIn.class);
            getObjectMapper().setMixIns(mixins);
            getFilterProvider().removeFilter("resultFieldNames");

            String filterString = String.join(",", fieldsToSerialize);
            SquigglyPropertyFilter filter = new SquigglyPropertyFilter(
                    new SimpleSquigglyContextProvider(new SquigglyParser(), filterString));
            getFilterProvider().addFilter("resultFieldNames", filter);
        }
    }

    /**
     * Dummy class to use with object mappers filter. Filters work off annotations so we need a class with
     * the same annotation name as our filter name. Using this class as a mixin in setFieldsToSerialize, we are able
     * to mark any Object as having the filter "resultFieldNames"
     */
    @JsonFilter("resultFieldNames")
    class PropertyFilterMixIn {

    }
}
