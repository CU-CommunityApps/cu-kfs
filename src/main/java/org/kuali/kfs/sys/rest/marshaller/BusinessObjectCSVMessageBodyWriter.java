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

import com.opencsv.CSVWriter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.LookupResultAttributeDefinition;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.sys.rest.util.KualiMediaType;
import org.kuali.kfs.sys.businessobject.serialization.BusinessObjectSerializationService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Produces(KualiMediaType.TEXT_CSV)
public class BusinessObjectCSVMessageBodyWriter extends BusinessObjectMessageBodyWriter {

    private static final Logger LOG = LogManager.getLogger();
    private static final String CSV_DATE_TIME_FORMAT = "MM/dd/YYYY hh:mm a zz";
    private static final String CSV_DATE_FORMAT = "MM/dd/YYYY";
    private DataDictionaryService dataDictionaryService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(BusinessObjectCSVMessageBodyWriter.CSV_DATE_FORMAT);
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(BusinessObjectCSVMessageBodyWriter.CSV_DATE_TIME_FORMAT);

    @Context
    private HttpServletResponse response;

    @Override
    public void writeTo(List<BusinessObjectBase> businessObjects, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws WebApplicationException {

        response.setHeader("Content-type", KualiMediaType.TEXT_CSV);

        if (!businessObjects.isEmpty()) {
            BusinessObjectBase exampleBo = businessObjects.get(0);
            Class<? extends BusinessObjectBase> businessObjectClass = exampleBo.getClass();

            List<LookupResultAttributeDefinition> resultAttributeDefinitions = getBusinessObjectDictionaryService()
                    .getLookupResultAttributeDefinitions(businessObjectClass);
            Set<String> fieldsToSerialize = resultAttributeDefinitions.stream()
                    .map(LookupResultAttributeDefinition::getName).collect(Collectors.toSet());

            BusinessObjectRestrictions businessObjectRestrictions = getSearchServiceForBusinessObject(exampleBo)
                    .getBusinessObjectAuthorizationService().getLookupResultRestrictions(exampleBo, getCurrentUser());

            BusinessObjectSerializationService serializationService =
                    new BusinessObjectSerializationService(fieldsToSerialize, businessObjectRestrictions, true);

            Map<String, Object> exampleSerializedBo = serializationService.serializeBusinessObject(exampleBo);

            // If we don't set the headers up before we start writing to the output stream, large responses cause the
            // stream to flush and we lose these values.
            String businessObjectName = businessObjectClass.getSimpleName();
            response.setHeader("Content-Disposition", "attachment; filename=" + businessObjectName +
                    "-export.csv");

            Stream<Map<String, Object>> stream = businessObjects.stream()
                    .map(businessObject -> serializationService.serializeBusinessObject(businessObject));

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(entityStream);
            CSVWriter csvWriter = new CSVWriter(outputStreamWriter);

            String[] headers = getColumnHeaders(businessObjectClass, exampleSerializedBo, resultAttributeDefinitions);
            csvWriter.writeNext(headers);
            stream.map(serialized -> getRowOfDataRepresentingThisResult(serialized, resultAttributeDefinitions))
                    .forEach(csvWriter::writeNext);

            try {
                csvWriter.close();
            } catch (IOException ioe) {
                LOG.error("Unable to close csv writer for business object" + businessObjectName
                        + " " + ioe.getMessage());
                throw new WebApplicationException();
            }
        } else {
            try {
                entityStream.write(null);
            } catch (IOException ioe) {
                LOG.error("Unable to write null to output writer for business object" + ioe.getMessage());
                throw new WebApplicationException();
            }
        }
    }

    private String[] getColumnHeaders(Class<? extends BusinessObjectBase> businessObjectClass,
                                      Map<String, Object> exampleResult, List<LookupResultAttributeDefinition> attributesToInclude) {
        return attributesToInclude
                .stream()
                .map(LookupResultAttributeDefinition::getName)
                .filter(attributeName -> hasProperty(exampleResult, attributeName))
                .map(attributeName -> getCorrespondingLabel(businessObjectClass, attributeName))
                .toArray(String[]::new);
    }

    private String getCorrespondingLabel(Class<? extends BusinessObjectBase> businessObjectClass, String fieldName) {
        String label = dataDictionaryService.getAttributeLabel(businessObjectClass, fieldName);
        if (label == null) {
            label = "*error*";
            LOG.warn("While attempting to return search results as csv, we were unable to locate a label for " +
                    "business object: " + businessObjectClass.getSimpleName() + " fieldName: " + fieldName);
        }
        return label;
    }

    private String[] getRowOfDataRepresentingThisResult(Map<String, Object> singleResult,
                                                        List<LookupResultAttributeDefinition> attributesToInclude) {
        List<String> columns = new LinkedList<>();
        attributesToInclude.stream()
                .forEach(attribute -> {
                    try {
                        String transformedValue;
                        Object value = PropertyUtils.getNestedProperty(singleResult, attribute.getName());
                        if (attribute.getType() == AttributeDefinition.Type.DATE_RANGE && value instanceof Long) {
                            transformedValue = dateFormat.format(new Date((Long) value));
                        } else if (attribute.getType() == AttributeDefinition.Type.DATE_TIME && value instanceof Long) {
                            transformedValue = dateTimeFormat.format(new Date((Long) value));
                        } else {
                            transformedValue = value == null ? "" : value.toString();
                        }
                        columns.add(transformedValue);
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        LOG.warn("No such attribute named " + attribute.getName() + " exists on object " + singleResult);
                    }
                });
        return columns.toArray(new String[0]);
    }

    private boolean hasProperty(Map<String, Object> map, String key) {
        DefaultResolver resolver = new DefaultResolver();
        while (resolver.hasNested(key)) {
            final String next = resolver.next(key);
            Object value = map.get(next);
            if (value instanceof Map) {
                map = (Map) value;
            }
            key = resolver.remove(key);
        }
        return map.containsKey(key);
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
}
