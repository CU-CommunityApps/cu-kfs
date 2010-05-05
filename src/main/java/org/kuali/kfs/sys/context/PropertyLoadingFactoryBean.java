package org.kuali.kfs.sys.context;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.util.ClassLoaderUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;


/**
 * This is a customization of PropertyLoadingFactoryBean from KNS.
 * 
 * Adds an ability to load additional properties based 
 * on system variable called: additional.kfs.config.location
 * 
 * CU Note:
 * The properties traditionally set via 'property.files' in the KFS build should be able to be
 * set via the runtime kfs-config.properties.  This bean loads up the properties and then injects
 * them into beans in Spring (including Rice).  So the KualiConfigurationService should still work.
 * But we may need to double check next time we actually have a custom property to set.
 *
 */
public class PropertyLoadingFactoryBean implements FactoryBean {
		
    private static final String PROPERTY_FILE_NAMES_KEY = "property.files";
    private static final String PROPERTY_TEST_FILE_NAMES_KEY = "property.test.files";
    private static final String SECURITY_PROPERTY_FILE_NAME_KEY = "security.property.file";
    private static final String CONFIGURATION_FILE_NAME = "configuration";
    private static final Properties BASE_PROPERTIES = new Properties();
    private static final String HTTP_URL_PROPERTY_NAME = "http.url";
    private static final String KSB_REMOTING_URL_PROPERTY_NAME = "ksb.remoting.url";
    private static final String REMOTING_URL_SUFFIX = "/remoting";
    private static final String APPLICATION_URL_KEY = "application.url";
    private static final String ENVIRONMENT_KEY = "environment";
    private static final String ADDITIONAL_KFS_CONFIG_LOCATION_PARAM = "additional.kfs.config.location";
    // introduced a second parameter so that existing installations weren't broken by csv paths
    private static final String ADDITIONAL_KFS_CONFIG_LOCATIONS_PARAM = "additional.kfs.config.locations";
    
    private Properties props = new Properties();
    private boolean testMode;
    private boolean secureMode;
    
    /**
     * Loads properties from an external file.  Also merges in all System properties
     * @param props the properties object
     */
    private static void loadExternalProperties(Properties props) {
    	//check if additional kfs config was provided and override properties
    	String externalConfigLocationPath = System.getProperty(PropertyLoadingFactoryBean.ADDITIONAL_KFS_CONFIG_LOCATION_PARAM);
    	if (StringUtils.isNotEmpty(externalConfigLocationPath)) {
    	    System.err.println("Loading properties from " + externalConfigLocationPath);
    	    loadProperties(props, new StringBuffer("file:").append(externalConfigLocationPath).toString());
    	}

    	String externalConfigLocationPaths = System.getProperty(PropertyLoadingFactoryBean.ADDITIONAL_KFS_CONFIG_LOCATIONS_PARAM);
    	if (StringUtils.isNotEmpty(externalConfigLocationPaths)) {
        	String[] files = externalConfigLocationPaths.split(","); 
        	for (String f: files) { 
        	    if (StringUtils.isNotEmpty(f)) { 
        	        System.err.println("Loading properties from " + f);
        	        loadProperties(props, new StringBuffer("file:").append(f).toString()); 
        	    } 
        	}
    	}

    	props.putAll(System.getProperties());
    }
    
    public Object getObject() throws Exception {
    	
        loadBaseProperties();
        props.putAll(BASE_PROPERTIES);
               
        if (secureMode) {
        	// don't fail on missing, since this file is typically stored externally
        	// and it might not be present
            loadPropertyList(props,SECURITY_PROPERTY_FILE_NAME_KEY, false);
        } else {
        	// by default these are application resources properties...
        	// to override these we'd have to double-load the external properties
        	// or override this key
        	// I don't know why some methods access the base properties directly
        	// while the generated properties object contains this extra layer 
            loadPropertyList(props,PROPERTY_FILE_NAMES_KEY);
            if (testMode) {
                loadPropertyList(props,PROPERTY_TEST_FILE_NAMES_KEY);
            }          
        }
        
        if (StringUtils.isBlank(System.getProperty(HTTP_URL_PROPERTY_NAME))) {
        	props.put(KSB_REMOTING_URL_PROPERTY_NAME, props.getProperty(PropertyLoadingFactoryBean.APPLICATION_URL_KEY) + REMOTING_URL_SUFFIX);
        }
        else {
        	props.put(KSB_REMOTING_URL_PROPERTY_NAME, new StringBuffer("http://").append(System.getProperty(HTTP_URL_PROPERTY_NAME)).append("/kfs-").append(props.getProperty(PropertyLoadingFactoryBean.ENVIRONMENT_KEY)).append(REMOTING_URL_SUFFIX).toString());
        }
        
        //printProperties(props);
        //printProperties(BASE_PROPERTIES);
        
        return props;
    }

    public Class getObjectType() {
        return Properties.class;
    }

    public boolean isSingleton() {
        return true;
    }

    private static void loadPropertyList(Properties props, String listPropertyName) {
    	loadPropertyList(props, listPropertyName, true);
    }
    private static void loadPropertyList(Properties props, String listPropertyName, boolean failOnMissing) {
        for (String propertyFileName : getBaseListProperty(listPropertyName)) {
            loadProperties(props,propertyFileName, failOnMissing);
        }
    }

    private static void loadProperties( Properties props, String propertyFileName) {
    	loadProperties(props, propertyFileName, true);
    }

    private static void loadProperties( Properties props, String propertyFileName, boolean failOnMissing) {
        InputStream propertyFileInputStream = null;
        try {
            try {
            	Resource properties = new DefaultResourceLoader(ClassLoaderUtils.getDefaultClassLoader()).getResource(propertyFileName);
            	boolean attemptLoad = failOnMissing || properties.exists();
            	if (attemptLoad) {
            	    System.err.println("Reading properties " + propertyFileName);
	                propertyFileInputStream = properties.getInputStream();
	                props.load(propertyFileInputStream);
	                // XXX: REMOVE THIS
	                props.store(System.err, "printing for debugging purposes");
            	}
            }
            finally {
                if (propertyFileInputStream != null) {
                    propertyFileInputStream.close();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("PropertyLoadingFactoryBean unable to load property file: " + propertyFileName);
        }
    }
    
    public static String getBaseProperty(String propertyName) {
        loadBaseProperties();
        return BASE_PROPERTIES.getProperty(propertyName);
    }
    
    private void printProperties(Properties props) {
    	Enumeration keys = props.keys();
        while (keys.hasMoreElements()) {
        	String key = (String)keys.nextElement();
        	String value = (String)props.get(key);
        	System.out.println(key + ": " + value);
        }
    }

    protected static List<String> getBaseListProperty(String propertyName) {
        loadBaseProperties();
        return Arrays.asList(BASE_PROPERTIES.getProperty(propertyName).split(","));
    }

    protected static void loadBaseProperties() {
        if (BASE_PROPERTIES.isEmpty()) {
            loadProperties(BASE_PROPERTIES, new StringBuffer("classpath:").append(CONFIGURATION_FILE_NAME).append(".properties").toString());
            
        	//check if additional kfs config was provided and override BASE_PROPERTIES
            // CU customization to load external properties
            loadExternalProperties(BASE_PROPERTIES);
       	
            BASE_PROPERTIES.putAll(System.getProperties());

        }
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public void setSecureMode(boolean secureMode) {
        this.secureMode = secureMode;
    }
    
    public static void clear() {
        BASE_PROPERTIES.clear();
    }
}