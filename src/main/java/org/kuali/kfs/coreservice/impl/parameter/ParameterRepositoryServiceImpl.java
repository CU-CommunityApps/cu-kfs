/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.coreservice.impl.parameter;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.util.Truth;
import org.kuali.kfs.coreservice.api.parameter.ParameterKey;
import org.kuali.kfs.coreservice.api.parameter.ParameterRepositoryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* Cornell Customization: backport redis*/
public class ParameterRepositoryServiceImpl implements ParameterRepositoryService {

    private static final String SUB_PARAM_SEPARATOR = "=";

    private BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;

    @CacheEvict(value = Parameter.CACHE_NAME, allEntries = true)
    @Override
    public Parameter createParameter(Parameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null");
        }

        final ParameterKey key = ParameterKey.create(parameter.getNamespaceCode(), parameter.getComponentCode(),
                parameter.getName());
        final Parameter existing = getParameter(key);
        if (existing != null) {
            throw new IllegalStateException("the parameter to create already exists: " + parameter);
        }

        return businessObjectService.save(parameter);
    }

    @CacheEvict(value = Parameter.CACHE_NAME, allEntries = true)
    @Override
    public Parameter updateParameter(Parameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter is null");
        }

        final ParameterKey key = ParameterKey.create(parameter.getNamespaceCode(), parameter.getComponentCode(),
                parameter.getName());
        final Parameter existing = getParameter(key);
        if (existing == null) {
            throw new IllegalStateException("the parameter does not exist: " + parameter);
        }

        return businessObjectService.save(parameter);
    }

    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{getParameter}-key=' + #p0.getCacheKey()")
    @Override
    public Parameter getParameter(ParameterKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("name", key.getName());
        map.put("namespaceCode", key.getNamespaceCode());
        map.put("componentCode", key.getComponentCode());

        return businessObjectService.findByPrimaryKey(Parameter.class, Collections.unmodifiableMap(map));
    }

    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{getParameterValueAsString}' + 'key=' + #p0.getCacheKey()")
    @Override
    public String getParameterValueAsString(ParameterKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("name", key.getName());
        map.put("namespaceCode", key.getNamespaceCode());
        map.put("componentCode", key.getComponentCode());

        final Parameter p = businessObjectService.findByPrimaryKey(Parameter.class, Collections.unmodifiableMap(map));
        return p != null ? p.getValue() : null;
    }

    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{getParameterValueAsBoolean}' + 'key=' + #p0.getCacheKey()")
    @Override
    public Boolean getParameterValueAsBoolean(ParameterKey key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("name", key.getName());
        map.put("namespaceCode", key.getNamespaceCode());
        map.put("componentCode", key.getComponentCode());

        final Parameter p = businessObjectService.findByPrimaryKey(Parameter.class, Collections.unmodifiableMap(map));
        final String value = p != null ? p.getValue() : null;
        return Truth.strToBooleanIgnoreCase(value);
    }

    @Cacheable(cacheNames = Parameter.CACHE_NAME, key = "'{getParameterValuesAsString}' + 'key=' + #p0.getCacheKey()")
    @Override
    public Collection<String> getParameterValuesAsString(ParameterKey key) {
        return splitOn(getParameterValueAsString(key), ";");
    }

    @Override
    public String getSubParameterValueAsString(ParameterKey key, String subParameterName) {
        if (StringUtils.isBlank(subParameterName)) {
            throw new IllegalArgumentException("subParameterName is blank");
        }

        Collection<String> values = getParameterValuesAsString(key);
        return getSubParameter(values, subParameterName);
    }

    @Override
    public Collection<String> getSubParameterValuesAsString(ParameterKey key, String subParameterName) {
        return splitOn(getSubParameterValueAsString(key, subParameterName), ",");
    }

    private String getSubParameter(Collection<String> values, String subParameterName) {
        for (String value : values) {
            if (subParameterName.equals(StringUtils.substringBefore(value, SUB_PARAM_SEPARATOR))) {
                return StringUtils.trimToNull(StringUtils.substringAfter(value, SUB_PARAM_SEPARATOR));
            }
        }
        return null;
    }

    private Collection<String> splitOn(String strValues, String delim) {
        if (StringUtils.isEmpty(delim)) {
            throw new IllegalArgumentException("delim is empty");
        }

        if (strValues == null || StringUtils.isBlank(strValues)) {
            return Collections.emptyList();
        }

        final Collection<String> values = new ArrayList<>();
        for (String value : strValues.split(delim)) {
            values.add(value.trim());
        }

        return Collections.unmodifiableCollection(values);
    }

    @Override
    public GenericQueryResults<Parameter> findParameters(QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new IllegalArgumentException("queryByCriteria is null");
        }

        return criteriaLookupService.lookup(Parameter.class, queryByCriteria);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }
}
