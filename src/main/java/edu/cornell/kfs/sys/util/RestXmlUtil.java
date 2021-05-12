/*
 * Copyright 2014 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.sys.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.util.KfsDateUtils;
import org.kuali.kfs.core.api.util.type.TypeUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;

import com.thoughtworks.xstream.XStream;

public class RestXmlUtil {

	private static final Logger LOG = LogManager.getLogger(RestXmlUtil.class);

    private DataDictionaryService dataDictionaryService;

    private static final String[] DATE_FORMAT = {"MM/dd/yyyy", "yyyy-MM-dd"};

    /**
     *
     * This method converts a map into xml in the format of <key>value</key>
     *
     * @param boName
     * @param o
     * @return
     */
    public static String toXML(BusinessObjectEntry boe, Object o) {
        XStream xStream = getXStream(boe.getBusinessObjectClass().getName());

        return xStream.toXML(o);
    }

    /**
     *
     * This method converts the xml back to map.
     *
     * @param boName
     * @param xml
     * @return
     */
    public static Object fromXML(BusinessObjectEntry boe, String xml) {
        XStream xStream = getXStream(boe.getBusinessObjectClass().getName());

        return xStream.fromXML(xml);
    }

    /**
     *
     * This method parses the xml back to list of business objects.
     *
     * @param boe
     * @param xml
     * @return
     * @throws Exception
     */
    public static List<?> parseXML(BusinessObjectEntry boe, String xml) {
        List<Object> list = new ArrayList<Object>();

        Object fromXML = fromXML(boe, xml);
        if (fromXML instanceof Collection) {
            Collection<Object> c = (Collection<Object>)fromXML;

            for (Object o : c) {
                Map<?,?> m = (Map<?,?>) o;
                list.add(populateBusinessObject(boe, m));
            }
        } else if (fromXML instanceof Map) {
            Map<?,?> m = (Map<?,?>) fromXML;
            list.add(populateBusinessObject(boe, m));
        }

        return list;
    }

    protected static Object populateBusinessObject(BusinessObjectEntry boe, Map<?,?> m) {
        Object bo = ObjectUtils.createNewObjectFromClass(boe.getBusinessObjectClass());
        PersistenceStructureService persistenceStructureService = SpringContext.getBean(PersistenceStructureService.class);

        for (Object key : m.keySet()) {
            String propertyName = (String) key;
            Class<?> propertyType = ObjectUtils.getPropertyType(bo, propertyName, persistenceStructureService);

            if (propertyType != null) {
                try {
                    Object propertyValue = m.get(key);
                    if (propertyValue != null && !propertyValue.equals("null")) {
                        String value = (String) propertyValue;
                        if (TypeUtils.isIntegralClass(propertyType)) {
                            propertyValue = Integer.parseInt(value);
                        } else if (TypeUtils.isDecimalClass(propertyType)) {
                            propertyValue = Float.parseFloat(value);
                        } else if (TypeUtils.isTemporalClass(propertyType)) {
                            propertyValue = KfsDateUtils.convertToSqlDate(DateUtils.parseDate(value, DATE_FORMAT));
                        } else if (TypeUtils.isBooleanClass(propertyType)) {
                            propertyValue = Boolean.parseBoolean(value);
                        }
                    } else {
                        propertyValue = null;
                    }

                    ObjectUtils.setObjectProperty(bo, propertyName, propertyValue);
                } catch (Exception ex) {
                    LOG.error(ex);
                }
            }
        }

        return bo;
    }

    protected static XStream getXStream(String className) {
        XStream xStream = new XStream();
        xStream.registerConverter(new MapEntryConverter());
        xStream.alias(List.class.getName(), List.class);
        xStream.alias(className, Map.class);

        return xStream;
    }

    public DataDictionaryService getDataDictionaryService() {
        if (this.dataDictionaryService == null) {
            this.dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }
        return this.dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
}