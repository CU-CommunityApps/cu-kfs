package org.kuali.kfs.sys.businessobject.lookup;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.kns.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.dao.LookupDao;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.LookupAttributeDefinition;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.util.DateRangeUtil;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.BusinessObject;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link LookupSearchService} implementation for vanilla DB lookups. If search parameter manipulation is needed, this
 * impl isn't gonna do it for you. Perusing the comments before using this impl is recommended.
 */
public class VendorLookupSearchServiceImpl extends LookupSearchService {

    protected LookupDao lookupDao;
    private BusinessObjectMetaDataService businessObjectMetaDataService;
    private BusinessObjectAdminService businessObjectAdminService;
    private DataDictionaryService dataDictionaryService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);

    public Pair<Collection<? extends BusinessObjectBase>, Integer> getSearchResults(
            Class<? extends BusinessObjectBase> businessObjectClass, MultivaluedMap<String, String> fieldValues,
            int skip, int limit, String sortField, boolean sortAscending) {
        MultivaluedMap<String, String> transformedFieldValues = transformSearchParams(businessObjectClass, fieldValues);
        Map<String, String> searchProps = convertMultiToRegularMap(transformedFieldValues);

        return executeSearch(businessObjectClass, skip, limit, sortField, sortAscending, searchProps);
    }

    protected Pair<Collection<? extends BusinessObjectBase>, Integer> executeSearch(
            Class<? extends BusinessObjectBase> businessObjectClass, int skip, int limit, String sortField,
            boolean sortAscending, Map<String, String> searchProps) {
        Pair<? extends Collection<? extends BusinessObjectBase>, Integer> results =
                lookupDao.findObjects(businessObjectClass, searchProps, skip, limit, sortField, sortAscending);
        return (Pair<Collection<? extends BusinessObjectBase>, Integer>) results;
    }

    /*
     * This method will only consider the first instance of a key in the map. Based on search of lookups at the time,
     * this behavior seems sufficient. Should support for multiple instances be needed, this method could be updated
     * or overridden.
     */
    private Map<String, String> convertMultiToRegularMap(MultivaluedMap<String, String> multivaluedMap) {
        Map<String, String> map = new LinkedHashMap<>();
        if (multivaluedMap == null) {
            return map;
        }
        for (String key : multivaluedMap.keySet()) {
            map.put(key, multivaluedMap.getFirst(key));
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> getActionLinks(BusinessObjectBase businessObject, Person user) {
        List<Map<String, Object>> actionLinks = new LinkedList<>();
        if (businessObjectAdminService.allowsEdit(businessObject, user)) {
            actionLinks.add(buildAction("Edit", "GET",
                    generateMaintenanceUrl(businessObject, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL)));
        }

        if (businessObjectAdminService.allowsCopy(businessObject, user)) {
            actionLinks.add(buildAction("Copy", "GET",
                    generateMaintenanceUrl(businessObject, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL)));
        }

        return actionLinks;
    }

    /**
     * Can transform search params for business objects to be consumed by a lookupDao expecting different values
     * than what is provided by the Rest interface. This method currently attempts to transform any date range values
     * based on an implementation's transformDateString method.
     *
     * @param boClass The business object class used to identify which lookup attributes to use
     * @param searchParams A set of parameters to potentially transform
     * @return A map of transformed search parameters based on the provided search params and boClass
     */
    MultivaluedMap<String, String> transformSearchParams(Class<? extends BusinessObjectBase> boClass,
                                                         MultivaluedMap<String, String> searchParams) {
        MultivaluedMap<String, String> transformedParams = new MultivaluedHashMap<>();
        searchParams.keySet().forEach(key -> {
            LookupAttributeDefinition attributeDefinition =
                    getBusinessObjectDictionaryService().getLookupAttributeDefinition(boClass, key);
            List<String> values = searchParams.get(key);
            if (attributeDefinition != null) {
                Stream<String> valueStream = values.parallelStream();
                LookupAttributeDefinition.Type attributeType = attributeDefinition.getType();
                if (attributeType == LookupAttributeDefinition.Type.DATE_RANGE) {
                    valueStream = valueStream.map(this::transformDateString);
                } else if (attributeType == LookupAttributeDefinition.Type.DATE_TIME) {
                    valueStream = valueStream.map(Long::parseLong).map(Date::new).map(dateFormat::format);
                }
                transformedParams.put(key, valueStream.collect(Collectors.toList()));
            } else {
                transformedParams.put(key, searchParams.get(key));
            }
        });
        return transformedParams;
    }

    private String transformDateString(String dateString) {
        DateRangeUtil dateRange = new DateRangeUtil(dateFormat);
        dateRange.setDateStringWithLongValues(dateString);
        return dateRange.toDateString();
    }

    private String generateMaintenanceUrl(BusinessObjectBase businessObject, String methodToCall) {
        Class businessObjectClass = businessObject.getClass();
        List<String> pkNames = businessObjectMetaDataService.listPrimaryKeyFieldNames(businessObjectClass);

        Properties params = new Properties();
        params.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, methodToCall);
        params.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, businessObjectClass.getName());
        params.putAll(getParametersFromPrimaryKey(businessObject, pkNames));

        return KRAD_URL_PREFIX + UrlFactory.parameterizeUrl(KRADConstants.MAINTENANCE_ACTION, params);
    }

    protected Map<String, Object> buildAction(String label, String method, String url) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("label", label);
        action.put("method", method);
        action.put("url", url);
        return action;
    }

    private Properties getParametersFromPrimaryKey(BusinessObject businessObject, List<String> pkNames) {
        Properties parameters = new Properties();
        String businessObjectClassName = businessObject.getClass().getName();
        for (String fieldName : pkNames) {
            AttributeDefinition attributeDefinition =
                    getDataDictionaryService().getAttributeDefinition(businessObjectClassName, fieldName);
            if (attributeDefinition != null) {
                boolean attributeShouldBeEncrypted = getBusinessObjectAuthorizationService()
                        .attributeValueNeedsToBeEncryptedOnFormsAndLinks(businessObject.getClass(), fieldName);
                if (!attributeShouldBeEncrypted) {
                    String fieldVal = KRADConstants.EMPTY_STRING;
                    try {
                        fieldVal = PropertyUtils.getProperty(businessObject, fieldName).toString();
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    parameters.put(fieldName, fieldVal);
                }
            }
        }
        return parameters;
    }

    public void setLookupDao(LookupDao lookupDao) {
        this.lookupDao = lookupDao;
    }

    public void setBusinessObjectMetaDataService(BusinessObjectMetaDataService businessObjectMetaDataService) {
        this.businessObjectMetaDataService = businessObjectMetaDataService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setBusinessObjectAdminService(BusinessObjectAdminService businessObjectAdminService) {
        this.businessObjectAdminService = businessObjectAdminService;
    }
}

