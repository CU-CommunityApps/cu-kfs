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
package org.kuali.kfs.core.api.util.io;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.cache.CopiedObject;

import java.io.Serializable;

/**
 * {@code SerializationUtils} is a set of utilities to add in the serialization of java objects.
 */
/* Cornell Customization: backport redis*/
public final class SerializationUtils extends org.apache.commons.lang3.SerializationUtils {

    private SerializationUtils() {
        throw new UnsupportedOperationException("Should never be invoked.");
    }

    /**
     * Serializes the given {@link Serializable} object and then executes a Base 64 encoding on it, returning the
     * encoded value as a String.
     *
     * @param object the object to serialize, cannot be null
     * @return a base 64-encoded representation of the serialized object
     * @throws IllegalArgumentException if the given object is null
     * @throws SerializationException   if the serialization fails
     */
    public static String serializeToBase64(Serializable object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot serialize a null object");
        }
        byte[] serializedBytes = org.apache.commons.lang3.SerializationUtils.serialize(object);
        return new Base64().encodeAsString(serializedBytes);
    }

    /**
     * Deserializes the given base 64-encoded string value to it's Serializable object representation.
     *
     * @param base64Value the base 64-encoded value to deserialize, must not be null or a blank string
     * @return the deserialized object
     * @throws IllegalArgumentException if the given value is is null or blank
     * @throws SerializationException   if the deserialization fails
     */
    public static Serializable deserializeFromBase64(String base64Value) {
        if (StringUtils.isBlank(base64Value)) {
            throw new IllegalArgumentException("Cannot deserialize a null or blank base64 string value.");
        }
        byte[] decoded = new Base64().decode(base64Value);
        return org.apache.commons.lang3.SerializationUtils.deserialize(decoded);
    }
}
