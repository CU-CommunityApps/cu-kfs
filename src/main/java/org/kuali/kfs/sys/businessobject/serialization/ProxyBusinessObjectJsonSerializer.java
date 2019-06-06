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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.apache.ojb.broker.core.proxy.CollectionProxyDefaultImpl;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.exception.ClassNotPersistableException;
import org.kuali.kfs.krad.exception.IntrospectionException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.PersistenceService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Class to serialize proxied versions of BusinessObjectBase to Json
 */
public class ProxyBusinessObjectJsonSerializer extends JsonSerializer<Object> {
    private PersistenceService persistenceService;
    private ObjectMapper objectMapper;
    private boolean serializeProxies;

    public ProxyBusinessObjectJsonSerializer(boolean serializeProxies) {
        this.serializeProxies = serializeProxies;
    }

    /**
     * Serialize method for proxy business objects. Using the persistence service, we construct a real object from the
     * proxy and pass it to a standard serializer for serialization
     *
     * @param value The proxy object to serialize
     * @param gen
     * @param serializers
     * @throws IOException
     */
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value instanceof PersistableBusinessObjectBase) {
            if (serializeProxies) {
                Object obj = getPersistenceService().resolveProxy(value);
                JsonSerializer serializer = getSerializerFactory()
                        .createSerializer(serializers, SimpleType.construct(obj.getClass()));
                serializer.serialize(obj, gen, serializers);
            } else {
                String id = generateId((PersistableBusinessObject) value);
                if (id == null) {
                    gen.writeNull();
                } else {
                    gen.writeString(id);
                }
            }
        } else if (CollectionProxyDefaultImpl.class.isAssignableFrom(value.getClass())) {
            gen.writeStartArray();
            for (Object bo: (CollectionProxyDefaultImpl) value) {
                if (bo instanceof PersistableBusinessObjectBase) {
                    String id = generateId((PersistableBusinessObject) bo);
                    if (id != null) {
                        gen.writeString(id);
                    }
                }
            }
            gen.writeEndArray();
        }
    }

    /**
     * Converts a BusinessObject's potentially compound primary keys to a single value that is more easily transported
     * throughout the system
     * @param bo business object used to generate the id
     * @return String base64 encoded json string of the primary keys and values map
     */
    private String generateId(PersistableBusinessObject bo) {
        try {
            Map primaryKeyFields = getPersistenceService().getPrimaryKeyFieldValues(bo);
            String json = getObjectMapper().writeValueAsString(primaryKeyFields);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException | IntrospectionException | IllegalArgumentException |
                ClassNotPersistableException e) {
            return null;
        }
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    protected BeanSerializerFactory getSerializerFactory() {
        return BeanSerializerFactory.instance;
    }

    protected PersistenceService getPersistenceService() {
        if (persistenceService == null) {
            persistenceService = KRADServiceLocator.getPersistenceService();
        }
        return persistenceService;
    }

    protected ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }
}
