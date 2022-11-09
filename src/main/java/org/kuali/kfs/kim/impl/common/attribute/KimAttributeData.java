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
package org.kuali.kfs.kim.impl.common.attribute;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.mo.common.Identifiable;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.api.type.KimTypeInfoService;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Cornell customization: backport FINP-8310
 */
public abstract class KimAttributeData extends PersistableBusinessObjectBase implements Identifiable {

    private static final Logger LOG = LogManager.getLogger();
    private static final long serialVersionUID = 1L;

    private static KimTypeInfoService kimTypeInfoService;

    private String attributeValue;

    private String kimAttributeId;

    private KimAttribute kimAttribute;

    private String kimTypeId;

    private KimType kimType;

    public static <T extends KimAttributeData> Map<String, String> toAttributes(Collection<T> bos) {
        Map<String, String> m = new HashMap<>();
        if (CollectionUtils.isNotEmpty(bos)) {
            for (T it : bos) {
                if (it != null) {
                    KimTypeAttribute attribute = null;
                    if (it.getKimType() != null) {
                        attribute = it.getKimType().getAttributeDefinitionById(it.getKimAttributeId());
                    }
                    if (attribute != null) {
                        m.put(attribute.getKimAttribute().getAttributeName(), it.getAttributeValue());
                    } else {
                        m.put(it.getKimAttribute().getAttributeName(), it.getAttributeValue());
                    }
                }
            }
        }
        return m;
    }

    /**
     * creates a list of KimAttributeData from attributes, kimTypeId, and assignedToId.
     */
    public static <T extends KimAttributeData> List<T> createFrom(Class<T> type, Map<String, String> attributes,
                                                                  String kimTypeId) {
        if (attributes == null) {
            //purposely not using Collections.emptyList() b/c we do not want to return an unmodifiable list.
            return new ArrayList<>();
        }
        List<T> attrs = new ArrayList<>();
        for (Map.Entry<String, String> it : attributes.entrySet()) {
            //return attributes.entrySet().collect {
            KimTypeAttribute attr = getKimTypeInfoService().getKimType(kimTypeId).getAttributeDefinitionByName(
                    it.getKey());
            if (attr == null) {
                LOG.error("Attribute " + it.getKey() + " was not found for kimType " +
                        getKimTypeInfoService().getKimType(kimTypeId).getName());
            }
            KimType theType = getKimTypeInfoService().getKimType(kimTypeId);
            if (attr != null && StringUtils.isNotBlank(it.getValue())) {
                try {
                    T newDetail = type.newInstance();
                    newDetail.setKimAttributeId(attr.getKimAttribute().getId());
                    newDetail.setKimAttribute(attr.getKimAttribute());
                    newDetail.setKimTypeId(kimTypeId);
                    newDetail.setKimType(theType);
                    newDetail.setAttributeValue(it.getValue());
                    attrs.add(newDetail);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return attrs;
    }

    public static KimTypeInfoService getKimTypeInfoService() {
        if (kimTypeInfoService == null) {
            kimTypeInfoService = KimApiServiceLocator.getKimTypeInfoService();
        }
        return kimTypeInfoService;
    }

    public abstract void setId(String id);

    public abstract void setAssignedToId(String assignedToId);

    public KimAttribute getKimAttribute() {
        if (this.kimAttribute == null && StringUtils.isNotBlank(kimAttributeId)) {
            refreshReferenceObject("kimAttribute");
        }
        return kimAttribute;
    }

    public void setKimAttribute(KimAttribute kimAttribute) {
        this.kimAttribute = kimAttribute;
    }

    // Cornell customization: backport FINP-8310
    public KimType getKimType() {
        return kimType;
    }

    public void setKimType(KimType kimType) {
        this.kimType = kimType;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public String getKimAttributeId() {
        return kimAttributeId;
    }

    public void setKimAttributeId(String kimAttributeId) {
        this.kimAttributeId = kimAttributeId;
    }

    public String getKimTypeId() {
        return kimTypeId;
    }

    public void setKimTypeId(String kimTypeId) {
        this.kimTypeId = kimTypeId;
    }
}
