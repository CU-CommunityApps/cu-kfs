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
package org.kuali.kfs.krad.datadictionary;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.datadictionary.exception.AttributeValidationException;
import org.kuali.kfs.krad.datadictionary.exception.CompletionException;
import org.kuali.kfs.krad.datadictionary.parse.StringListConverter;
import org.kuali.kfs.krad.datadictionary.parse.StringMapConverter;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.uif.UifConstants.ViewType;
import org.kuali.kfs.krad.uif.util.ComponentBeanPostProcessor;
import org.kuali.kfs.krad.uif.view.View;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.util.ResourceLoaderUtil;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Collection of named BusinessObjectEntry objects, each of which contains information relating to the display,
 * validation, and general maintenance of a BusinessObject.
 * <p>
 * THIS OVERRIDE OF THE RICE DATA DICTIONARY IS A TOTAL BAND-AID.
 * It allows us to pass in the Spring ApplicationContext to retrieve resources, which means we can use file globs
 * to pull in those resources.  Hopefully, as KFS starts pulling in Rice client functionality, the Rice DataDictionary
 * will be improved to pull multiple files in.
 */
public class DataDictionary {

    private static final Log LOG = LogFactory.getLog(DataDictionary.class);
    private static Map<String, Map<String, PropertyDescriptor>> cache = new TreeMap<>();
    static boolean validateEBOs = false;
    public static PersistenceStructureService persistenceStructureService;

    protected DefaultListableBeanFactory ddBeans = new DefaultListableBeanFactory();
    protected XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ddBeans);

    /**
     * The encapsulation of DataDictionary indices
     */
    protected DataDictionaryIndex ddIndex = new DataDictionaryIndex(ddBeans);

    // View indices
    protected UifDictionaryIndex uifIndex = new UifDictionaryIndex(ddBeans);

    /**
     * The DataDictionaryMapper
     * The default mapper simply consults the initialized indices on workflow document type.
     */
    protected DataDictionaryMapper ddMapper = new DataDictionaryIndexMapper();

    protected List<String> configFileLocations = new ArrayList<String>();

    public List<String> getConfigFileLocations() {
        return this.configFileLocations;
    }

    public void setConfigFileLocations(List<String> configFileLocations) {
        this.configFileLocations = configFileLocations;
    }

    public void addConfigFileLocation(String location) throws IOException {
        indexSource(location);
    }

    /**
     * ApplicationContext aware version of method
     */
    public void addConfigFileLocation(String location, ApplicationContext applicationContext) throws IOException {
        indexSource(location, applicationContext);
    }

    /**
     * Sets the DataDictionaryMapper
     *
     * @param mapper the datadictionary mapper
     */
    public void setDataDictionaryMapper(DataDictionaryMapper mapper) {
        this.ddMapper = mapper;
    }

    private void indexSource(String sourceName) throws IOException {
        if (sourceName == null) {
            throw new DataDictionaryException("Source Name given is null");
        }

        if (!sourceName.endsWith(".xml")) {
            Resource resource = ResourceLoaderUtil.getFileResource(sourceName);
            if (resource.exists()) {
                indexSource(resource.getFile());
            } else {
                LOG.warn("Could not find " + sourceName);
                throw new DataDictionaryException("DD Resource " + sourceName + " not found");
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding sourceName " + sourceName + " ");
            }
            Resource resource = ResourceLoaderUtil.getFileResource(sourceName);
            if (!resource.exists()) {
                throw new DataDictionaryException("DD Resource " + sourceName + " not found");
            }

            String indexName = sourceName.substring(sourceName.lastIndexOf("/") + 1, sourceName.indexOf(".xml"));
            configFileLocations.add(sourceName);
        }
    }

    /**
     * ApplicationContext aware version of method
     */
    private void indexSource(String sourceName, ApplicationContext applicationContext) throws IOException {
        if (sourceName == null) {
            throw new DataDictionaryException("Source Name given is null");
        }

        if (sourceName.endsWith(".xml")) {
            final Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(applicationContext).getResources(sourceName);
            for (Resource resource : resources) {
                if (resource.exists()) {
                    final String resourcePath = ResourceLoaderUtil.parseResourcePathFromUrl(resource);
                    if (!StringUtils.isBlank(resourcePath)) {
                        configFileLocations.add(resourcePath);
                    }
                } else {
                    LOG.warn("Could not find " + sourceName);
                    throw new DataDictionaryException("DD Resource " + sourceName + " not found");
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("adding sourceName " + sourceName + " ");
            }
            Resource resource = ResourceLoaderUtil.getFileResource(sourceName, applicationContext);
            if (!resource.exists()) {
                throw new DataDictionaryException("DD Resource " + sourceName + " not found");
            }

            String indexName = sourceName.substring(sourceName.lastIndexOf("/") + 1, sourceName.indexOf(".xml"));
            configFileLocations.add(sourceName);
        }
    }

    private void indexSource(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                indexSource(file);
            } else if (file.getName().endsWith(".xml")) {
                configFileLocations.add("file:" + file.getAbsolutePath());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping non xml file " + file.getAbsolutePath() + " in DD load");
                }
            }
        }
    }

    public void parseDataDictionaryConfigurationFiles(boolean allowConcurrentValidation) {
        // configure the bean factory, setup component decorator post processor and allow Spring EL
        try {
            BeanPostProcessor idPostProcessor = ComponentBeanPostProcessor.class.newInstance();
            ddBeans.addBeanPostProcessor(idPostProcessor);
            ddBeans.setBeanExpressionResolver(new StandardBeanExpressionResolver());

            GenericConversionService conversionService = new GenericConversionService();
            conversionService.addConverter(new StringMapConverter());
            conversionService.addConverter(new StringListConverter());
            ddBeans.setConversionService(conversionService);
        } catch (Exception e1) {
            LOG.error("Cannot create component decorator post processor: " + e1.getMessage(), e1);
            throw new RuntimeException("Cannot create component decorator post processor: " + e1.getMessage(), e1);
        }

        // expand configuration locations into files
        LOG.info("Starting DD XML File Load");

        String[] configFileLocationsArray = new String[configFileLocations.size()];
        configFileLocationsArray = configFileLocations.toArray(configFileLocationsArray);
        configFileLocations.clear(); // empty the list out so other items can be added
        try {
            xmlReader.loadBeanDefinitions(configFileLocationsArray);
        } catch (Exception e) {
            LOG.error("Error loading bean definitions", e);
            throw new DataDictionaryException("Error loading bean definitions: " + e.getLocalizedMessage());
        }
        LOG.info("Completed DD XML File Load");

        // indexing
        if (allowConcurrentValidation) {
            Thread t = new Thread(ddIndex);
            t.start();
        } else {
            ddIndex.run();
        }
    }

    public void validateDD(boolean validateEbos) {
        DataDictionary.validateEBOs = validateEbos;
        Map<String, DataObjectEntry> doBeans = ddBeans.getBeansOfType(DataObjectEntry.class);
        for (DataObjectEntry entry : doBeans.values()) {
            entry.completeValidation();
        }
        Map<String, DocumentEntry> docBeans = ddBeans.getBeansOfType(DocumentEntry.class);
        for (DocumentEntry entry : docBeans.values()) {
            entry.completeValidation();
        }
    }

    public void validateDD() {
        validateDD(validateEBOs);
    }

    /**
     * @param className
     * @return BusinessObjectEntry for the named class, or null if none exists
     */
    public BusinessObjectEntry getBusinessObjectEntry(String className) {
        return ddMapper.getBusinessObjectEntry(ddIndex, className);
    }

    /**
     * @param className
     * @return DataObjectEntry for the named class, or null if none exists
     */
    public DataObjectEntry getDataObjectEntry(String className) {
        return ddMapper.getDataObjectEntry(ddIndex, className);
    }

    /**
     * @param className
     * @return the business object entry for a concrete class
     */
    public BusinessObjectEntry getBusinessObjectEntryForConcreteClass(String className) {
        return ddMapper.getBusinessObjectEntryForConcreteClass(ddIndex, className);
    }

    /**
     * @return List of businessObject class names
     */
    public List<String> getBusinessObjectClassNames() {
        return ddMapper.getBusinessObjectClassNames(ddIndex);
    }

    /**
     * @return Map of (classname, BusinessObjectEntry) pairs
     */
    public Map<String, BusinessObjectEntry> getBusinessObjectEntries() {
        return ddMapper.getBusinessObjectEntries(ddIndex);
    }

    /**
     * @param className
     * @return DataDictionaryEntryBase for the named class, or null if none exists
     */
    public DataDictionaryEntry getDictionaryObjectEntry(String className) {
        return ddMapper.getDictionaryObjectEntry(ddIndex, className);
    }

    /**
     * Returns the KNS document entry for the given lookup key. The documentTypeDDKey is interpreted successively in
     * the following ways until a mapping is found (or none if found):
     * <ol>
     * <li>KEW/workflow document type</li>
     * <li>business object class name</li>
     * <li>maintainable class name</li>
     * </ol>
     * This mapping is compiled when DataDictionary files are parsed on startup (or demand). Currently this means the
     * mapping is static, and one-to-one (one KNS document maps directly to one and only one key).
     *
     * @param documentTypeDDKey the KEW/workflow document type name
     * @return the KNS DocumentEntry if it exists
     */
    public DocumentEntry getDocumentEntry(String documentTypeDDKey) {
        return ddMapper.getDocumentEntry(ddIndex, documentTypeDDKey);
    }

    /**
     * Note: only MaintenanceDocuments are indexed by businessObject Class
     * <p>
     * This is a special case that is referenced in one location. Do we need another map for this stuff??
     *
     * @param businessObjectClass
     * @return DocumentEntry associated with the given Class, or null if there is none
     */
    public MaintenanceDocumentEntry getMaintenanceDocumentEntryForBusinessObjectClass(Class<?> businessObjectClass) {
        return ddMapper.getMaintenanceDocumentEntryForBusinessObjectClass(ddIndex, businessObjectClass);
    }

    public Map<String, DocumentEntry> getDocumentEntries() {
        return ddMapper.getDocumentEntries(ddIndex);
    }

    /**
     * @param viewId unique id for view
     * @return View instance associated with the id
     */
    public View getViewById(String viewId) {
        return ddMapper.getViewById(uifIndex, viewId);
    }

    /**
     * @param viewTypeName type name for the view
     * @param indexKey     Map of index key parameters, these are the parameters the indexer used to index the view
     *                     initially and needs to identify an unique view instance
     * @return View instance that matches the view type name and index
     */
    public View getViewByTypeIndex(ViewType viewTypeName, Map<String, String> indexKey) {
        return ddMapper.getViewByTypeIndex(uifIndex, viewTypeName, indexKey);
    }

    /**
     * @param viewTypeName type name for the view
     * @param indexKey     Map of index key parameters, these are the parameters the indexer used to index the view
     *                     initially and needs to identify an unique view instance
     * @return boolean true if View exists for the given view type and index information, false if not
     */
    public boolean viewByTypeExist(ViewType viewTypeName, Map<String, String> indexKey) {
        return ddMapper.viewByTypeExist(uifIndex, viewTypeName, indexKey);
    }

    /**
     * @param viewTypeName view type name to retrieve
     * @return List<View> prototypes with the given type name, or empty list
     */
    public List<View> getViewsForType(ViewType viewTypeName) {
        return ddMapper.getViewsForType(uifIndex, viewTypeName);
    }

    /**
     * Returns an object from the dictionary by its spring bean name
     *
     * @param beanName id or name for the bean definition
     * @return object instance created or the singleton being maintained
     */
    public Object getDictionaryObject(String beanName) {
        return ddBeans.getBean(beanName);
    }

    /**
     * @param id id of the bean to check for
     * @return boolean true if dictionary contains bean with the given id, false otherwise
     */
    public boolean containsDictionaryObject(String id) {
        return ddBeans.containsBean(id);
    }

    /**
     * Retrieves the configured property values for the view bean definition associated with the given id
     * <p>
     * <p>
     * Since constructing the View object can be expensive, when metadata only is needed this method can be used
     * to retrieve the configured property values. Note this looks at the merged bean definition
     *
     * @param viewId id for the view to retrieve
     * @return PropertyValues configured on the view bean definition, or null if view is not found
     */
    public PropertyValues getViewPropertiesById(String viewId) {
        return ddMapper.getViewPropertiesById(uifIndex, viewId);
    }

    /**
     * Retrieves the configured property values for the view bean definition associated with the given type and
     * index
     * <p>
     * <p>
     * Since constructing the View object can be expensive, when metadata only is needed this method can be used
     * to retrieve the configured property values. Note this looks at the merged bean definition
     *
     * @param viewTypeName type name for the view
     * @param indexKey     Map of index key parameters, these are the parameters the indexer used to index the view
     *                     initially and needs to identify an unique view instance
     * @return PropertyValues configured on the view bean definition, or null if view is not found
     */
    public PropertyValues getViewPropertiesByType(ViewType viewTypeName, Map<String, String> indexKey) {
        return ddMapper.getViewPropertiesByType(uifIndex, viewTypeName, indexKey);
    }

    /**
     * @param targetClass
     * @param propertyName
     * @return true if the given propertyName names a property of the given class
     * @throws CompletionException if there is a problem accessing the named property on the given class
     */
    public static boolean isPropertyOf(Class targetClass, String propertyName) {
        if (targetClass == null) {
            throw new IllegalArgumentException("invalid (null) targetClass");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("invalid (blank) propertyName");
        }

        PropertyDescriptor propertyDescriptor = buildReadDescriptor(targetClass, propertyName);

        boolean isPropertyOf = (propertyDescriptor != null);
        return isPropertyOf;
    }

    /**
     * @param targetClass
     * @param propertyName
     * @return true if the given propertyName names a Collection property of the given class
     * @throws CompletionException if there is a problem accessing the named property on the given class
     */
    public static boolean isCollectionPropertyOf(Class targetClass, String propertyName) {
        boolean isCollectionPropertyOf = false;

        PropertyDescriptor propertyDescriptor = buildReadDescriptor(targetClass, propertyName);
        if (propertyDescriptor != null) {
            Class clazz = propertyDescriptor.getPropertyType();

            if ((clazz != null) && Collection.class.isAssignableFrom(clazz)) {
                isCollectionPropertyOf = true;
            }
        }

        return isCollectionPropertyOf;
    }

    /**
     * @return the persistenceStructureService
     */
    public static PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    /**
     * This method determines the Class of the attributeName passed in. Null will be returned if the member is not
     * available, or if a reflection exception is thrown.
     *
     * @param boClass       Class that the attributeName property exists in.
     * @param attributeName Name of the attribute you want a class for.
     * @return The Class of the attributeName, if the attribute exists on the rootClass. Null otherwise.
     */
    public static Class getAttributeClass(Class boClass, String attributeName) {
        // fail loudly if the attributeName isn't a member of rootClass
        if (!isPropertyOf(boClass, attributeName)) {
            throw new AttributeValidationException("unable to find attribute '" + attributeName + "' in rootClass '" + boClass.getName() + "'");
        }

        //Implementing Externalizable Business Object Services...
        //The boClass can be an interface, hence handling this separately,
        //since the original method was throwing exception if the class could not be instantiated.
        if (boClass.isInterface()) {
            return getAttributeClassWhenBOIsInterface(boClass, attributeName);
        } else {
            return getAttributeClassWhenBOIsClass(boClass, attributeName);
        }
    }

    /**
     * @param boClass
     * @param attributeName
     * @return the property type of the given attributeName when the bo class is a concrete class.
     */
    private static Class getAttributeClassWhenBOIsClass(Class boClass, String attributeName) {
        Object boInstance;
        try {
            boInstance = boClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate Data Object: " + boClass, e);
        }

        // attempt to retrieve the class of the property
        try {
            return ObjectUtils.getPropertyType(boInstance, attributeName, getPersistenceStructureService());
        } catch (Exception e) {
            throw new RuntimeException("Unable to determine property type for: " + boClass.getName() + "." + attributeName, e);
        }
    }

    /**
     * This method gets the property type of the given attributeName when the bo class is an interface. This method will
     * also work if the bo class is not an interface, but that case requires special handling, hence a separate method
     * getAttributeClassWhenBOIsClass.
     *
     * @param boClass
     * @param attributeName
     * @return
     */
    private static Class getAttributeClassWhenBOIsInterface(Class boClass, String attributeName) {
        if (boClass == null) {
            throw new IllegalArgumentException("invalid (null) boClass");
        }
        if (StringUtils.isBlank(attributeName)) {
            throw new IllegalArgumentException("invalid (blank) attributeName");
        }

        PropertyDescriptor propertyDescriptor = null;

        String[] intermediateProperties = attributeName.split("\\.");
        int lastLevel = intermediateProperties.length - 1;
        Class currentClass = boClass;

        for (int i = 0; i <= lastLevel; ++i) {

            String currentPropertyName = intermediateProperties[i];
            propertyDescriptor = buildSimpleReadDescriptor(currentClass, currentPropertyName);

            if (propertyDescriptor != null) {

                Class propertyType = propertyDescriptor.getPropertyType();
                if (propertyType.equals(PersistableBusinessObjectExtension.class)) {
                    propertyType = getPersistenceStructureService().getBusinessObjectAttributeClass(currentClass,
                            currentPropertyName);
                }
                if (Collection.class.isAssignableFrom(propertyType)) {
                    // TODO: determine property type using generics type definition
                    throw new AttributeValidationException("Can't determine the Class of Collection elements because " +
                            "when the business object is an (possibly ExternalizableBusinessObject) interface.");
                } else {
                    currentClass = propertyType;
                }
            } else {
                throw new AttributeValidationException("Can't find getter method of " + boClass.getName() +
                        " for property " + attributeName);
            }
        }
        return currentClass;
    }

    /**
     * This method determines the Class of the elements in the collectionName passed in.
     *
     * @param boClass        Class that the collectionName collection exists in.
     * @param collectionName the name of the collection you want the element class for
     * @return
     */
    public static Class getCollectionElementClass(Class boClass, String collectionName) {
        if (boClass == null) {
            throw new IllegalArgumentException("invalid (null) boClass");
        }
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("invalid (blank) collectionName");
        }

        PropertyDescriptor propertyDescriptor = null;

        String[] intermediateProperties = collectionName.split("\\.");
        Class currentClass = boClass;

        for (int i = 0; i < intermediateProperties.length; ++i) {

            String currentPropertyName = intermediateProperties[i];
            propertyDescriptor = buildSimpleReadDescriptor(currentClass, currentPropertyName);

            if (propertyDescriptor != null) {

                Class type = propertyDescriptor.getPropertyType();
                if (Collection.class.isAssignableFrom(type)) {

                    if (getPersistenceStructureService().isPersistable(currentClass)) {

                        Map<String, Class> collectionClasses = new HashMap<String, Class>();
                        collectionClasses = getPersistenceStructureService().listCollectionObjectTypes(currentClass);
                        currentClass = collectionClasses.get(currentPropertyName);

                    } else {
                        throw new RuntimeException("Can't determine the Class of Collection elements because persistenceStructureService.isPersistable(" + currentClass.getName() + ") returns false.");
                    }

                } else {

                    currentClass = propertyDescriptor.getPropertyType();

                }
            }
        }

        return currentClass;
    }

    /**
     * @param propertyClass
     * @param propertyName
     * @return PropertyDescriptor for the getter for the named property of the given class, if one exists.
     */
    public static PropertyDescriptor buildReadDescriptor(Class propertyClass, String propertyName) {
        if (propertyClass == null) {
            throw new IllegalArgumentException("invalid (null) propertyClass");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("invalid (blank) propertyName");
        }

        PropertyDescriptor propertyDescriptor = null;

        String[] intermediateProperties = propertyName.split("\\.");
        int lastLevel = intermediateProperties.length - 1;
        Class currentClass = propertyClass;

        for (int i = 0; i <= lastLevel; ++i) {
            String currentPropertyName = intermediateProperties[i];
            propertyDescriptor = buildSimpleReadDescriptor(currentClass, currentPropertyName);

            if (i < lastLevel) {
                if (propertyDescriptor != null) {
                    Class propertyType = propertyDescriptor.getPropertyType();
                    if (propertyType.equals(PersistableBusinessObjectExtension.class)) {
                        propertyType = getPersistenceStructureService().getBusinessObjectAttributeClass(currentClass,
                                currentPropertyName);
                    }
                    if (Collection.class.isAssignableFrom(propertyType)) {

                        if (getPersistenceStructureService().isPersistable(currentClass)) {
                            Map<String, Class> collectionClasses = new HashMap<String, Class>();
                            collectionClasses = getPersistenceStructureService().listCollectionObjectTypes(currentClass);
                            currentClass = collectionClasses.get(currentPropertyName);

                        } else {

                            throw new RuntimeException("Can't determine the Class of Collection elements because " +
                                    "persistenceStructureService.isPersistable(" + currentClass.getName() + ") returns false.");
                        }

                    } else {

                        currentClass = propertyType;

                    }
                }
            }
        }
        return propertyDescriptor;
    }

    /**
     * @param propertyClass
     * @param propertyName
     * @return PropertyDescriptor for the getter for the named property of the given class, if one exists.
     */
    public static PropertyDescriptor buildSimpleReadDescriptor(Class propertyClass, String propertyName) {
        if (propertyClass == null) {
            throw new IllegalArgumentException("invalid (null) propertyClass");
        }
        if (StringUtils.isBlank(propertyName)) {
            throw new IllegalArgumentException("invalid (blank) propertyName");
        }

        PropertyDescriptor p = null;

        // check to see if we've cached this descriptor already. if yes, return true.
        String propertyClassName = propertyClass.getName();
        Map<String, PropertyDescriptor> m = cache.get(propertyClassName);
        if (null != m) {
            p = m.get(propertyName);
            if (null != p) {
                return p;
            }
        }

        // Use PropertyUtils.getPropertyDescriptors instead of manually constructing PropertyDescriptor because of
        // issues with introspection and generic/co-variant return types
        // See https://issues.apache.org/jira/browse/BEANUTILS-340 for more details

        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(propertyClass);
        if (ArrayUtils.isNotEmpty(descriptors)) {
            for (PropertyDescriptor descriptor : descriptors) {
                if (descriptor.getName().equals(propertyName)) {
                    p = descriptor;
                }
            }
        }

        // cache the property descriptor if we found it.
        if (p != null) {
            if (m == null) {
                m = new TreeMap<>();
                cache.put(propertyClassName, m);
            }
            m.put(propertyName, p);
        }

        return p;
    }

    public Set<InactivationBlockingMetadata> getAllInactivationBlockingMetadatas(Class blockedClass) {
        return ddMapper.getAllInactivationBlockingMetadatas(ddIndex, blockedClass);
    }

    /**
     * This method gathers beans of type BeanOverride and invokes each one's performOverride() method.
     */
    // KULRICE-4513
    public void performBeanOverrides() {
        Collection<BeanOverride> beanOverrides = ddBeans.getBeansOfType(BeanOverride.class).values();

        if (beanOverrides.isEmpty()) {
            LOG.info("DataDictionary.performOverrides(): No beans to override");
        }
        for (BeanOverride beanOverride : beanOverrides) {
            Object bean = ddBeans.getBean(beanOverride.getBeanName());
            beanOverride.performOverride(bean);
            LOG.info("DataDictionary.performOverrides(): Performing override on bean: " + bean.toString());
        }
    }

    public Map<String, String> getChildAttributesParent() {
        return ddIndex.getChildAttributesParent();
    }
}
