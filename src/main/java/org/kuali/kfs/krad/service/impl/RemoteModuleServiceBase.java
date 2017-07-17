/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2017 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.krad.service.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.coreservice.framework.CoreFrameworkServiceLocator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.bo.ModuleConfiguration;
import org.kuali.kfs.krad.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.krad.datadictionary.PrimitiveAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.RelationshipDefinition;
import org.kuali.kfs.krad.service.BusinessObjectNotLookupableException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.KualiModuleService;
import org.kuali.kfs.krad.service.LookupService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.uif.UifParameters;
import org.kuali.kfs.krad.util.ExternalizableBusinessObjectUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.bo.ExternalizableBusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import edu.cornell.kfs.sys.CUKFSAuthorizationConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public abstract class RemoteModuleServiceBase implements ModuleService {
    protected static final Logger LOG = Logger.getLogger(RemoteModuleServiceBase.class);

    protected ModuleConfiguration moduleConfiguration;
    protected KualiModuleService kualiModuleService;
    protected ApplicationContext applicationContext;
    protected ConfigurationService kualiConfigurationService;
    protected LookupService lookupService;

    /**
     * @see ModuleService#isResponsibleFor(java.lang.Class)
     */
    public boolean isResponsibleFor(Class businessObjectClass) {
        if (getModuleConfiguration() == null) {
            throw new IllegalStateException("Module configuration has not been initialized for the module service.");
        }

        if (getModuleConfiguration().getPackagePrefixes() == null || businessObjectClass == null) {
            return false;
        }
        for (String prefix : getModuleConfiguration().getPackagePrefixes()) {
            Package pkg = businessObjectClass.getPackage();
            String name = pkg.getName();
            if (businessObjectClass.getPackage().getName().startsWith(prefix)) {
                return true;
            }
        }
        if (ExternalizableBusinessObject.class.isAssignableFrom(businessObjectClass)) {
            Class externalizableBusinessObjectInterface =
                ExternalizableBusinessObjectUtils.determineExternalizableBusinessObjectSubInterface(
                    businessObjectClass);
            if (externalizableBusinessObjectInterface != null) {
                for (String prefix : getModuleConfiguration().getPackagePrefixes()) {
                    if (externalizableBusinessObjectInterface.getPackage().getName().startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Utility method to check for the presence of a non blank value in the map for the given key
     * Note: returns false if a null map is passed in.
     *
     * @param map the map to retrieve the value from
     * @param key the key to use
     * @return true if there is a non-blank value in the map for the given key.
     */
    protected static boolean isNonBlankValueForKey(Map<String, Object> map, String key) {
        if (map == null) return false;

        Object result = map.get(key);
        if (result instanceof String) {
            return !StringUtils.isBlank((String) result);
        }
        return result != null;
    }

    /**
     * @see ModuleService#isResponsibleFor(java.lang.Class)
     */
    public boolean isResponsibleForJob(String jobName) {
        if (getModuleConfiguration() == null) {
            throw new IllegalStateException("Module configuration has not been initialized for the module service.");
        }

        if (getModuleConfiguration().getJobNames() == null || StringUtils.isEmpty(jobName)) {
            return false;
        }

        return getModuleConfiguration().getJobNames().contains(jobName);
    }


    public List listPrimaryKeyFieldNames(Class businessObjectInterfaceClass) {
        Class clazz = getExternalizableBusinessObjectImplementation(businessObjectInterfaceClass);
        return KRADServiceLocator.getPersistenceStructureService().listPrimaryKeyFieldNames(clazz);
    }

    /**
     * @see ModuleService#getExternalizableBusinessObjectDictionaryEntry(java.lang.Class)
     */
    public BusinessObjectEntry getExternalizableBusinessObjectDictionaryEntry(Class businessObjectInterfaceClass) {
        Class boClass = getExternalizableBusinessObjectImplementation(businessObjectInterfaceClass);

        return boClass == null ? null : KRADServiceLocatorWeb.getDataDictionaryService().getDataDictionary()
            .getBusinessObjectEntryForConcreteClass(boClass.getName());
    }

    /**
     * @see ModuleService#getExternalizableDataObjectInquiryUrl(java.lang.Class,
     * java.util.Properties)
     */
    public String getExternalizableDataObjectInquiryUrl(Class<?> inquiryDataObjectClass, Properties parameters) {
        String baseUrl = getBaseInquiryUrl();

        // if external business object, replace data object in request with the actual impl object class
        if (ExternalizableBusinessObject.class.isAssignableFrom(inquiryDataObjectClass)) {
            Class implementationClass = getExternalizableBusinessObjectImplementation(inquiryDataObjectClass.asSubclass(
                ExternalizableBusinessObject.class));
            if (implementationClass == null) {
                throw new RuntimeException("Can't find ExternalizableBusinessObject implementation class for "
                    + inquiryDataObjectClass.getName());
            }

            parameters.put(UifParameters.DATA_OBJECT_CLASS_NAME, implementationClass.getName());
        }

        return UrlFactory.parameterizeUrl(baseUrl, parameters);
    }

    /**
     * Returns the base URL to use for inquiry requests to objects within the module
     *
     * @return String base inquiry URL
     */
    protected String getBaseInquiryUrl() {
        return getKualiConfigurationService().getPropertyValueAsString(KRADConstants.KRAD_INQUIRY_URL_KEY);
    }

    /**
     * @see ModuleService#getExternalizableDataObjectLookupUrl(java.lang.Class,
     * java.util.Properties)
     */
    public String getExternalizableDataObjectLookupUrl(Class<?> lookupDataObjectClass, Properties parameters) {
        String baseUrl = getBaseLookupUrl();

        // if external business object, replace data object in request with the actual impl object class
        if (ExternalizableBusinessObject.class.isAssignableFrom(lookupDataObjectClass)) {
            Class implementationClass = getExternalizableBusinessObjectImplementation(lookupDataObjectClass.asSubclass(
                ExternalizableBusinessObject.class));
            if (implementationClass == null) {
                throw new RuntimeException("Can't find ExternalizableBusinessObject implementation class for "
                    + lookupDataObjectClass.getName());
            }

            parameters.put(UifParameters.DATA_OBJECT_CLASS_NAME, implementationClass.getName());
        }

        return UrlFactory.parameterizeUrl(baseUrl, parameters);
    }

    /**
     * Returns the base lookup URL for the Rice server
     *
     * @return String base lookup URL
     */
    protected String getRiceBaseLookupUrl() {
        return BaseLookupUrlsHolder.remoteKradBaseLookupUrl;
    }

    // Lazy initialization holder class idiom, see Effective Java item #71
    protected static final class BaseLookupUrlsHolder {

        public static final String localKradBaseLookupUrl;
        public static final String remoteKradBaseLookupUrl;

        static {
            remoteKradBaseLookupUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(KRADConstants.KRAD_SERVER_LOOKUP_URL_KEY);
            localKradBaseLookupUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(KRADConstants.KRAD_LOOKUP_URL_KEY);
        }
    }

    /**
     * Returns the base URL to use for lookup requests to objects within the module
     *
     * @return String base lookup URL
     */
    protected String getBaseLookupUrl() {
        return getRiceBaseLookupUrl();
    }

    @Deprecated
    public String getExternalizableBusinessObjectInquiryUrl(Class inquiryBusinessObjectClass,
                                                            Map<String, String[]> parameters) {
        if (!isExternalizable(inquiryBusinessObjectClass)) {
            return KRADConstants.EMPTY_STRING;
        }
        String businessObjectClassAttribute;

        Class implementationClass = getExternalizableBusinessObjectImplementation(inquiryBusinessObjectClass);
        if (implementationClass == null) {
            LOG.error("Can't find ExternalizableBusinessObject implementation class for " + inquiryBusinessObjectClass
                .getName());
            throw new RuntimeException("Can't find ExternalizableBusinessObject implementation class for interface "
                + inquiryBusinessObjectClass.getName());
        }
        businessObjectClassAttribute = implementationClass.getName();
        return UrlFactory.parameterizeUrl(getInquiryUrl(inquiryBusinessObjectClass), getUrlParameters(
            businessObjectClassAttribute, parameters));
    }

    /**
     * This overridden method ...
     *
     * @see ModuleService#getExternalizableBusinessObjectLookupUrl(java.lang.Class,
     * java.util.Map)
     */
    @Deprecated
    @Override
    public String getExternalizableBusinessObjectLookupUrl(Class inquiryBusinessObjectClass,
                                                           Map<String, String> parameters) {
        Properties urlParameters = new Properties();

        String riceBaseUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
            KRADConstants.KUALI_RICE_URL_KEY);
        String lookupUrl = riceBaseUrl;
        if (!lookupUrl.endsWith("/")) {
            lookupUrl = lookupUrl + "/";
        }
        if (parameters.containsKey(KRADConstants.MULTIPLE_VALUE)) {
            lookupUrl = lookupUrl + KRADConstants.MULTIPLE_VALUE_LOOKUP_ACTION;
        } else {
            lookupUrl = lookupUrl + KRADConstants.LOOKUP_ACTION;
        }
        for (String paramName : parameters.keySet()) {
            urlParameters.put(paramName, parameters.get(paramName));
        }

        Class clazz = getExternalizableBusinessObjectImplementation(inquiryBusinessObjectClass);
        urlParameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, clazz == null ? "" : clazz.getName());

        return UrlFactory.parameterizeUrl(lookupUrl, urlParameters);
    }

    /**
     * @see ModuleService#getExternalizableBusinessObjectsListForLookup(java.lang.Class,
     * java.util.Map, boolean)
     */
    public <T extends ExternalizableBusinessObject> List<T> getExternalizableBusinessObjectsListForLookup(
        Class<T> externalizableBusinessObjectClass, Map<String, Object> fieldValues, boolean unbounded) {
        Class<? extends ExternalizableBusinessObject> implementationClass =
            getExternalizableBusinessObjectImplementation(externalizableBusinessObjectClass);
        if (isExternalizableBusinessObjectLookupable(implementationClass)) {
            Map<String, String> searchCriteria = new HashMap<String, String>();
            for (Map.Entry<String, Object> fieldValue : fieldValues.entrySet()) {
                if (fieldValue.getValue() != null) {
                    searchCriteria.put(fieldValue.getKey(), fieldValue.getValue().toString());
                } else {
                    searchCriteria.put(fieldValue.getKey(), null);
                }
            }
            return (List<T>) getLookupService().findCollectionBySearchHelper(implementationClass, searchCriteria,
                unbounded);
        } else {
            throw new BusinessObjectNotLookupableException(
                "External business object is not a Lookupable:  " + implementationClass);
        }
    }

    /**
     * This method assumes that the property type for externalizable relationship in the business object is an interface
     * and gets the concrete implementation for it
     *
     * @see ModuleService#retrieveExternalizableBusinessObjectIfNecessary(org.kuali.rice.krad.bo.BusinessObject,
     * org.kuali.rice.krad.bo.BusinessObject, java.lang.String)
     */
    public <T extends ExternalizableBusinessObject> T retrieveExternalizableBusinessObjectIfNecessary(
        BusinessObject businessObject, T currentInstanceExternalizableBO, String externalizableRelationshipName) {

        if (businessObject == null) {
            return null;
        }
        Class clazz;
        try {
            Class<? extends ExternalizableBusinessObject> propertyType =
                    (Class<? extends ExternalizableBusinessObject>)PropertyUtils.getPropertyType(businessObject, externalizableRelationshipName);
            clazz = getExternalizableBusinessObjectImplementation(propertyType);
        } catch (Exception iex) {
            LOG.warn("Exception:"
                + iex
                + " thrown while trying to get property type for property:"
                + externalizableRelationshipName
                + " from business object:"
                + businessObject);
            return null;
        }

        //Get the business object entry for this business object from data dictionary
        //using the class name (without the package) as key
        BusinessObjectEntry entry =
            KRADServiceLocatorWeb.getDataDictionaryService().getDataDictionary().getBusinessObjectEntries().get(
                businessObject.getClass().getSimpleName());
        RelationshipDefinition relationshipDefinition = entry.getRelationshipDefinition(externalizableRelationshipName);
        List<PrimitiveAttributeDefinition> primitiveAttributeDefinitions =
            relationshipDefinition.getPrimitiveAttributes();

        Map<String, Object> fieldValuesInEBO = new HashMap<String, Object>();
        Object sourcePropertyValue;
        Object targetPropertyValue = null;
        boolean sourceTargetPropertyValuesSame = true;
        for (PrimitiveAttributeDefinition primitiveAttributeDefinition : primitiveAttributeDefinitions) {
            sourcePropertyValue = ObjectUtils.getPropertyValue(businessObject,
                primitiveAttributeDefinition.getSourceName());
            if (currentInstanceExternalizableBO != null) {
                targetPropertyValue = ObjectUtils.getPropertyValue(currentInstanceExternalizableBO,
                    primitiveAttributeDefinition.getTargetName());
            }
            if (sourcePropertyValue == null) {
                return null;
            } else if (targetPropertyValue == null || (targetPropertyValue != null && !targetPropertyValue.equals(
                sourcePropertyValue))) {
                sourceTargetPropertyValuesSame = false;
            }
            fieldValuesInEBO.put(primitiveAttributeDefinition.getTargetName(), sourcePropertyValue);
        }

        if (!sourceTargetPropertyValuesSame) {
            return (T) getExternalizableBusinessObject(clazz, fieldValuesInEBO);
        }
        return currentInstanceExternalizableBO;
    }

    /**
     * This method assumes that the externalizableClazz is an interface
     * and gets the concrete implementation for it
     *
     * @see ModuleService#retrieveExternalizableBusinessObjectIfNecessary(org.kuali.rice.krad.bo.BusinessObject,
     * org.kuali.rice.krad.bo.BusinessObject, java.lang.String)
     */
    @Override
    public List<? extends ExternalizableBusinessObject> retrieveExternalizableBusinessObjectsList(
        BusinessObject businessObject, String externalizableRelationshipName, Class externalizableClazz) {

        if (businessObject == null) {
            return null;
        }
        //Get the business object entry for this business object from data dictionary
        //using the class name (without the package) as key
        String className = businessObject.getClass().getName();
        String key = className.substring(className.lastIndexOf(".") + 1);
        BusinessObjectEntry entry =
            KRADServiceLocatorWeb.getDataDictionaryService().getDataDictionary().getBusinessObjectEntries().get(
                key);
        RelationshipDefinition relationshipDefinition = entry.getRelationshipDefinition(externalizableRelationshipName);
        List<PrimitiveAttributeDefinition> primitiveAttributeDefinitions =
            relationshipDefinition.getPrimitiveAttributes();
        Map<String, Object> fieldValuesInEBO = new HashMap<String, Object>();
        Object sourcePropertyValue;
        for (PrimitiveAttributeDefinition primitiveAttributeDefinition : primitiveAttributeDefinitions) {
            sourcePropertyValue = ObjectUtils.getPropertyValue(businessObject,
                primitiveAttributeDefinition.getSourceName());
            if (sourcePropertyValue == null) {
                return null;
            }
            fieldValuesInEBO.put(primitiveAttributeDefinition.getTargetName(), sourcePropertyValue);
        }
        return getExternalizableBusinessObjectsList(getExternalizableBusinessObjectImplementation(externalizableClazz),
            fieldValuesInEBO);
    }

    /**
     * @see ModuleService#getExternalizableBusinessObjectImplementation(java.lang.Class)
     */
    @Override
    public <E extends ExternalizableBusinessObject> Class<E> getExternalizableBusinessObjectImplementation(
        Class<E> externalizableBusinessObjectInterface) {
        if (getModuleConfiguration() == null) {
            throw new IllegalStateException("Module configuration has not been initialized for the module service.");
        }
        Map<Class, Class> ebos = getModuleConfiguration().getExternalizableBusinessObjectImplementations();
        if (ebos == null) {
            return null;
        }
        if (ebos.containsValue(externalizableBusinessObjectInterface)) {
            return externalizableBusinessObjectInterface;
        } else {
            Class<E> implementationClass = ebos.get(externalizableBusinessObjectInterface);
            if (implementationClass == null) {
                LOG.info("Can't find ExternalizableBusinessObject implementation class for " +
                    externalizableBusinessObjectInterface.getName());

                Iterator it = ebos.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    LOG.info(pair.getKey() + " = " + pair.getValue());
                }
            }
            int implClassModifiers = implementationClass.getModifiers();
            if (Modifier.isInterface(implClassModifiers) || Modifier.isAbstract(implClassModifiers)) {
                throw new RuntimeException("Implementation class must be non-abstract class: ebo interface: "
                    + externalizableBusinessObjectInterface.getName()
                    + " impl class: "
                    + implementationClass.getName()
                    + " module: "
                    + getModuleConfiguration().getNamespaceCode());
            }
            return implementationClass;
        }

    }

    @Deprecated
    protected Properties getUrlParameters(String businessObjectClassAttribute, Map<String, String[]> parameters) {
        Properties urlParameters = new Properties();
        for (String paramName : parameters.keySet()) {
            String[] parameterValues = parameters.get(paramName);
            if (parameterValues.length > 0) {
                urlParameters.put(paramName, parameterValues[0]);
            }
        }
        urlParameters.put(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE, businessObjectClassAttribute);
        urlParameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, KRADConstants.CONTINUE_WITH_INQUIRY_METHOD_TO_CALL);
        return urlParameters;
    }

    @Deprecated
    protected String getInquiryUrl(Class inquiryBusinessObjectClass) {
        String riceBaseUrl = KRADServiceLocator.getKualiConfigurationService().getPropertyValueAsString(
            KRADConstants.KUALI_RICE_URL_KEY);
        String inquiryUrl = riceBaseUrl;
        if (!inquiryUrl.endsWith("/")) {
            inquiryUrl = inquiryUrl + "/";
        }
        return inquiryUrl + KRADConstants.INQUIRY_ACTION;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        KualiModuleService kualiModuleService = null;
        try {
            kualiModuleService = KRADServiceLocatorWeb.getKualiModuleService();
            if (kualiModuleService == null) {
                kualiModuleService = ((KualiModuleService) applicationContext.getBean(
                    KRADServiceLocatorWeb.KUALI_MODULE_SERVICE));
            }
        } catch (NoSuchBeanDefinitionException ex) {
            kualiModuleService = ((KualiModuleService) applicationContext.getBean(
                KRADServiceLocatorWeb.KUALI_MODULE_SERVICE));
        }
        kualiModuleService.getInstalledModuleServices().add(this);
    }

    /**
     * @return the moduleConfiguration
     */
    public ModuleConfiguration getModuleConfiguration() {
        return this.moduleConfiguration;
    }

    /**
     * @param moduleConfiguration the moduleConfiguration to set
     */
    public void setModuleConfiguration(ModuleConfiguration moduleConfiguration) {
        this.moduleConfiguration = moduleConfiguration;
    }

    /**
     * @see ModuleService#isExternalizable(java.lang.Class)
     */
    @Override
    public boolean isExternalizable(Class boClazz) {
        if (boClazz == null) {
            return false;
        }
        return ExternalizableBusinessObject.class.isAssignableFrom(boClazz);
    }

    public <T extends ExternalizableBusinessObject> T createNewObjectFromExternalizableClass(Class<T> boClass) {
        try {
            return (T) getExternalizableBusinessObjectImplementation(boClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create externalizable business object class", e);
        }
    }

    public DataObjectRelationship getBusinessObjectRelationship(Class boClass, String attributeName,
                                                                String attributePrefix) {
        return null;
    }


    /**
     * @return the kualiModuleService
     */
    public KualiModuleService getKualiModuleService() {
        return this.kualiModuleService;
    }

    /**
     * @param kualiModuleService the kualiModuleService to set
     */
    public void setKualiModuleService(KualiModuleService kualiModuleService) {
        this.kualiModuleService = kualiModuleService;
    }

    protected ConfigurationService getKualiConfigurationService() {
        if (this.kualiConfigurationService == null) {
            this.kualiConfigurationService = KRADServiceLocator.getKualiConfigurationService();
        }

        return this.kualiConfigurationService;
    }

    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * This overridden method ...
     *
     * @see ModuleService#listAlternatePrimaryKeyFieldNames(java.lang.Class)
     */
    @Override
    public List<List<String>> listAlternatePrimaryKeyFieldNames(Class businessObjectInterfaceClass) {
        return null;
    }

    /**
     * This method determines whether or not this module is currently locked
     *
     * @see ModuleService#isLocked()
     */
    @Override
    public boolean isLocked() {
        LOG.debug("isLocked, entering Cornell customization");
        boolean shouldLock = false;
        ModuleConfiguration configuration = this.getModuleConfiguration();
        if (configuration != null) {
            String namespaceCode = configuration.getNamespaceCode();
            String componentCode = KRADConstants.DetailTypes.ALL_DETAIL_TYPE;
            String parameterName = KRADConstants.SystemGroupParameterNames.OLTP_LOCKOUT_ACTIVE_IND;
            ParameterService parameterService = CoreFrameworkServiceLocator.getParameterService();
            String shouldLockout = parameterService.getParameterValueAsString(namespaceCode, componentCode, parameterName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("isLocked, nameSpace: " + namespaceCode + " componentCode: " + componentCode + " parameterName: " + parameterName);
                LOG.debug("isLocked, shouldLockout: " + shouldLockout);
            }
            if (StringUtils.isNotBlank(shouldLockout)) {
                shouldLock = parameterService.getParameterValueAsBoolean(namespaceCode, componentCode, parameterName) && !isUserInLockoutOverrideList();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("isLocked, shouldLock: " + shouldLock);
        }
        return shouldLock;
    }
    
    protected boolean isUserInLockoutOverrideList() {
        String[] overrideList = StringUtils.split(getOverrideUsersList(), ",");
        String loggedInUserPrincipleName = GlobalVariables.getUserSession().getPerson().getPrincipalName();
        Object[] filteredList = Arrays.stream(overrideList).filter(x -> x.equalsIgnoreCase(loggedInUserPrincipleName)).toArray();
        boolean isInOverrideList = filteredList.length > 0;
        if (LOG.isDebugEnabled()) {
            LOG.debug("isUserInLockoutOverrideList, is " + loggedInUserPrincipleName + "  In OverrideList: " + isInOverrideList);
        }
        return isInOverrideList;
    }
    
    protected String getOverrideUsersList() {
        String overrideList = CoreFrameworkServiceLocator.getParameterService().getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.KFS, CUKFSParameterKeyConstants.ALL_COMPONENTS, 
                CUKFSParameterKeyConstants.OLTP_LOCK_OVERRIDE_LIST);
        if (StringUtils.isBlank(overrideList)) {
            return StringUtils.EMPTY;
        } else {
            return overrideList;
        }
    }

    /**
     * Gets the lookupService attribute.
     *
     * @return Returns the lookupService.
     */
    protected LookupService getLookupService() {
        return lookupService != null ? lookupService : KRADServiceLocatorWeb.getLookupService();
    }

    @Override
    public boolean goToCentralRiceForInquiry() {
        return false;
    }

    @Override
    public boolean isExternal(Class boClass) {
        return false;
    }
}
