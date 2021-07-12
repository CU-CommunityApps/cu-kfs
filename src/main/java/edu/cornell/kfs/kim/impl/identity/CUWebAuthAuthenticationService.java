package edu.cornell.kfs.kim.impl.identity;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.AuthenticationServiceImpl;

/**
 * RemoteUserWebAuthenticationService which can be configured to look for the HTTP headers
 * that CUWebAuth exposes.
 * WARNING: only enable headers if Tomcat is locked down and only accessible via CUWebAuth 
 * 
 * @see https://confluence.cornell.edu/display/CUWAL/Integrating+CUWebAuth+with+your+application#IntegratingCUWebAuthwithyourapplication-CGIenvironmentvariablesandHTTPrequestheaders
 * @author Aaron Hamid (arh14 at cornell dot edu)
 */
public class CUWebAuthAuthenticationService extends AuthenticationServiceImpl {
    private static final Logger LOG = LogManager.getLogger(CUWebAuthAuthenticationService.class);

    /**
     * The HTTP header CUWebAuth will populate the remote user in 
     */
    public static final String CUWAL_REMOTE_USER_HEADER = "remote_user";

    /**
     * By default don't use the CUWebAuth HTTP headers
     */
    private boolean useHttpHeaders = false;
    
    /**
     * @param useHttpHeaders whether to check the CUWebAuth http headers
     */
    public void setUseHttpHeaders(boolean useHttpHeaders) {
        this.useHttpHeaders = useHttpHeaders;
    }

    @Override
    public String getPrincipalName(HttpServletRequest request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Obtaining user from request (" + (useHttpHeaders ? "from '" + CUWAL_REMOTE_USER_HEADER + "' HTTP header" : "from REMOTE_USER CGI variable") + request);
        }
        logSystemProperties();
        // if we are not checking the headers, we are really just a normal RemoteUserWebAuthenticationService
        String id;
        if (!useHttpHeaders) {
            id = super.getPrincipalName(request);
        } else {
            // ok we need to check the CUWebAuth headers
            String user = System.getProperty(CUWAL_REMOTE_USER_HEADER);
            if (user == null) {
                user = System.getProperty(CUWAL_REMOTE_USER_HEADER.toUpperCase(Locale.US));
            }
            id = user;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found: " + id);
        }
        return id;
    }
    
    private void logSystemProperties() {
        for (Object key : System.getProperties().keySet()) {
            String keyString = String.valueOf(key);
            if (StringUtils.containsIgnoreCase(keyString, "pass")) {
                LOG.info("logSystemProperties, system property key='" + keyString + "' contains pass, so not displaying value");
            } else {
                String propertyValue = System.getProperty(keyString);
                LOG.info("logSystemProperties, property key='" + keyString + "', property value='" + propertyValue + "'");
            }
        }
    }
}
