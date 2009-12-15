package edu.cornell.kfs.sys.context;

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
    private static final String ADDITIONAL_KFS_CONFIG_LOCATIONS_PARAM = "additional.kfs.config.location";
   // private static final 
    
    private Properties props = new Properties();
    private boolean testMode;
    private boolean secureMode;
    
    public Object getObject() throws Exception {
    	
        loadBaseProperties();
        props.putAll(BASE_PROPERTIES);
               
        if (secureMode) {
            loadPropertyList(props,SECURITY_PROPERTY_FILE_NAME_KEY);
        } else {
            loadPropertyList(props,PROPERTY_FILE_NAMES_KEY);
            if (testMode) {
                loadPropertyList(props,PROPERTY_TEST_FILE_NAMES_KEY);
            }          
        }
        
        //check if additional kfs config was provided and override properties
    	String externalConfigLocationPath = System.getProperty(PropertyLoadingFactoryBean.ADDITIONAL_KFS_CONFIG_LOCATIONS_PARAM);
    	if (StringUtils.isNotEmpty(externalConfigLocationPath)) {
    		loadProperties(props, new StringBuffer("file:").append(externalConfigLocationPath).toString());
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
        for (String propertyFileName : getBaseListProperty(listPropertyName)) {
            loadProperties(props,propertyFileName);
        }
    }

    private static void loadProperties( Properties props, String propertyFileName) {
        InputStream propertyFileInputStream = null;
        try {
            try {
            	Resource properties = new DefaultResourceLoader(ClassLoaderUtils.getDefaultClassLoader()).getResource(propertyFileName);
            	if (properties.exists()) {
	                propertyFileInputStream = properties.getInputStream();
	                props.load(propertyFileInputStream);
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

    private static void loadExternalProperties( Properties props, String propertyFileName) {
        InputStream propertyFileInputStream = null;
        try {
            try {
                propertyFileInputStream = new FileInputStream(propertyFileName);
                props.load(propertyFileInputStream);
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
        	String externalConfigLocationPath = System.getProperty(PropertyLoadingFactoryBean.ADDITIONAL_KFS_CONFIG_LOCATIONS_PARAM);
        	if (StringUtils.isNotEmpty(externalConfigLocationPath)) {
        		loadProperties(BASE_PROPERTIES, new StringBuffer("file:").append(externalConfigLocationPath).toString());
        	}
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