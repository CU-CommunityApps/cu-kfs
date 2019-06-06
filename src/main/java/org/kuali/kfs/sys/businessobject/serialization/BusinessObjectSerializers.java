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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import org.apache.ojb.broker.core.proxy.CollectionProxyDefaultImpl;
import org.kuali.kfs.kns.util.KNSConstants;

/**
 * Serializers for BusinessObjects that can provide a custom serializer depending on the data type currently being
 * serialized
 */
public class BusinessObjectSerializers extends SimpleSerializers {
    private boolean serializeProxies;

    public BusinessObjectSerializers(boolean serializeProxies) {
        this.serializeProxies = serializeProxies;
    }

    /**
     * Checks to see if the object mapper is being asked to serialize a proxy type. This can be determined by checking
     * the class name for the characters "CGLIB". If we find a match, we provide a custom serializer to handle this data.
     * Otherwise, defer to the parent for determining the serializer
     *
     * @param config not used here
     * @param type contains type information of the item in question
     * @param beanDesc not used here
     * @return
     */
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class rawClass = type.getRawClass();
        if (rawClass != null &&
                rawClass.getName().contains(KNSConstants.PROXY_OBJECT_CLASS_NAME_INDICATOR) ||
                CollectionProxyDefaultImpl.class.isAssignableFrom(rawClass)) {
            return new ProxyBusinessObjectJsonSerializer(serializeProxies);
        } else {
            return super.findSerializer(config, type, beanDesc);
        }
    }
}
