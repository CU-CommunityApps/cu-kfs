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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.sys.businessobject.serialization.BusinessObjectSerializationService;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
public class BusinessObjectJSONMessageBodyWriter extends BusinessObjectMessageBodyWriter {
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
            BusinessObjectBase firstBo = businessObjects.get(0);
            BusinessObjectRestrictions businessObjectRestrictions = getSearchServiceForBusinessObject(firstBo)
                    .getBusinessObjectAuthorizationService().getLookupResultRestrictions(firstBo, getCurrentUser());
            BusinessObjectSerializationService serializationService =
                    new BusinessObjectSerializationService(Collections.emptySet(), businessObjectRestrictions, false);

            serializedResults = businessObjects.stream()
                    .map(serializationService::serializeBusinessObject)
                    .collect(Collectors.toList());
        }

        String json = gson.toJson(serializedResults);
        try {
            entityStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            LOG.error("Unable to write data to response for business object " + ioe.getMessage());
            throw new WebApplicationException();
        }
    }
}
