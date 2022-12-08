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
package org.kuali.kfs.kim.document.rule;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.core.impl.services.CoreImplServiceLocator;
import org.kuali.kfs.kim.bo.ui.KimDocumentAttributeDataBusinessObjectBase;
import org.kuali.kfs.kim.impl.common.attribute.KimAttribute;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization (KFSPTS-23531):
 * 
 * Updated the generation of the cache keys for improved compatibility with Redis.
 */
public class AttributeValidationHelper {

    private static final Logger LOG = LogManager.getLogger();
    private static final String DOCUMENT_PROPERTY_PREFIX = KRADConstants.DOCUMENT_PROPERTY_NAME + ".";

    // CU Customization (KFSPTS-23531): Add MessageFormat-related constants for generating various cache keys.
    private static final String ATTRIBUTE_BY_NAME_CACHE_KEY_PATTERN = "'{getAttributeDefinitionByName}'-name={0}";

    protected BusinessObjectService businessObjectService;

    protected KimAttribute getAttributeDefinitionById(String id) {
        CacheManager cm = CoreImplServiceLocator.getCacheManagerRegistry()
                .getCacheManagerByCacheName(KimAttribute.CACHE_NAME);
        Cache cache = cm.getCache(KimAttribute.CACHE_NAME);
        String cacheKey = "{" + KimAttribute.CACHE_NAME + "}id=" + id;
        ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null) {
            return (KimAttribute) valueWrapper.get();
        }

        KimAttribute attribute = getBusinessObjectService().findBySinglePrimaryKey(KimAttribute.class, id);
        cache.put(cacheKey, attribute);

        return attribute;
    }

    protected KimAttribute getAttributeDefinitionByName(String attributeName) {
        CacheManager cm = CoreImplServiceLocator.getCacheManagerRegistry()
                .getCacheManagerByCacheName(KimAttribute.CACHE_NAME);
        Cache cache = cm.getCache(KimAttribute.CACHE_NAME);
        // CU Customization (KFSPTS-23531): Build a more specific cache key
        String cacheKey = MessageFormat.format(ATTRIBUTE_BY_NAME_CACHE_KEY_PATTERN, attributeName);
        ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null) {
            return (KimAttribute) valueWrapper.get();
        }

        Map<String, String> criteria = new HashMap<>();
        criteria.put(KRADPropertyConstants.ATTRIBUTE_NAME, attributeName);
        List<KimAttribute> attributeImpls = (List<KimAttribute>) getBusinessObjectService()
                .findMatching(KimAttribute.class, criteria);
        KimAttribute attribute = null;
        if (!attributeImpls.isEmpty()) {
            attribute = attributeImpls.get(0);
        }

        cache.put(cacheKey, attribute);

        return attribute;
    }

    public Map<String, String> convertAttributesToMap(List<? extends KimAttributeData> attributes) {
        Map<String, String> m = new HashMap<>();
        for (KimAttributeData data : attributes) {
            KimAttribute attrib = getAttributeDefinitionById(data.getKimAttributeId());
            if (attrib != null) {
                m.put(attrib.getAttributeName(), data.getAttributeValue());
            } else {
                LOG.error("Unable to get attribute name for ID:{}", data::getKimAttributeId);
            }
        }
        return m;
    }

    public Map<String, String> convertQualifiersToMap(
            List<? extends KimDocumentAttributeDataBusinessObjectBase> qualifiers) {
        Map<String, String> m = new HashMap<>();
        for (KimDocumentAttributeDataBusinessObjectBase data : qualifiers) {
            KimAttribute attrib = getAttributeDefinitionById(data.getKimAttrDefnId());
            if (attrib != null) {
                m.put(attrib.getAttributeName(), data.getAttrVal());
            } else {
                LOG.error("Unable to get attribute name for ID:{}", data::getKimAttrDefnId);
            }
        }
        return m;
    }

    public Map<String, String> getBlankValueQualifiersMap(List<KimTypeAttribute> attributes) {
        Map<String, String> m = new HashMap<>();
        for (KimTypeAttribute attribute : attributes) {
            KimAttribute attrib = getAttributeDefinitionById(attribute.getKimAttribute().getId());
            if (attrib != null) {
                m.put(attrib.getAttributeName(), "");
            } else {
                LOG.error("Unable to get attribute name for ID:{}", attribute::getId);
            }
        }
        return m;
    }

    public Map<String, String> convertQualifiersToAttrIdxMap(
            List<? extends KimDocumentAttributeDataBusinessObjectBase> qualifiers) {
        Map<String, String> m = new HashMap<>();
        int i = 0;
        for (KimDocumentAttributeDataBusinessObjectBase data : qualifiers) {
            KimAttribute attrib = getAttributeDefinitionById(data.getKimAttrDefnId());
            if (attrib != null) {
                m.put(attrib.getAttributeName(), Integer.toString(i));
            } else {
                LOG.error("Unable to get attribute name for ID:{}", data::getKimAttrDefnId);
            }
            i++;
        }
        return m;
    }

    public void moveValidationErrorsToErrorMap(List<AttributeError> validationErrors) {
        // FIXME: the above code would overwrite messages on the same attributes (namespaceCode) but on different rows
        for (AttributeError error : validationErrors) {
            for (String errMsg : error.getErrors()) {
                String[] splitMsg = StringUtils.split(errMsg, ":");

                // if the property name starts with "document." then don't prefix with the error path
                if (error.getAttributeName().startsWith(DOCUMENT_PROPERTY_PREFIX)) {
                    GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(error.getAttributeName(),
                            splitMsg[0], splitMsg.length > 1 ? StringUtils.split(splitMsg[1], ";") :
                                    new String[]{});
                } else {
                    GlobalVariables.getMessageMap().putError(error.getAttributeName(), splitMsg[0],
                            splitMsg.length > 1 ? StringUtils.split(splitMsg[1], ";") : new String[]{});
                }
            }
        }
    }

    public List<AttributeError> convertErrorsForMappedFields(String errorPath,
            List<AttributeError> localErrors) {
        List<AttributeError> errors = new ArrayList<>();
        if (errorPath == null) {
            errorPath = KFSConstants.EMPTY_STRING;
        } else if (StringUtils.isNotEmpty(errorPath)) {
            errorPath = errorPath + ".";
        }
        for (AttributeError error : localErrors) {
            KimAttribute attribute = getAttributeDefinitionByName(error.getAttributeName());
            String attributeDefnId = attribute == null ? "" : attribute.getId();
            errors.add(AttributeError.Builder.create(errorPath + "qualifier(" + attributeDefnId +
                    ").attrVal", error.getErrors()).build());
        }
        return errors;
    }

    public List<AttributeError> convertErrors(String errorPath, Map<String, String> attrIdxMap,
            List<AttributeError> localErrors) {
        List<AttributeError> errors = new ArrayList<>();
        if (errorPath == null) {
            errorPath = KFSConstants.EMPTY_STRING;
        } else if (StringUtils.isNotEmpty(errorPath)) {
            errorPath = errorPath + ".";
        }
        for (AttributeError error : localErrors) {
            for (String errMsg : error.getErrors()) {
                errors.add(AttributeError.Builder.create(errorPath + "qualifiers[" +
                        attrIdxMap.get(error.getAttributeName()) + "].attrVal", errMsg).build());
            }
        }
        return errors;
    }

    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }
}
