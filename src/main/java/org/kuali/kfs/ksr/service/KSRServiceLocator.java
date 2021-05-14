/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.kfs.ksr.service;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;

/**
 * ====
 * CU Customization:
 * Remediated this class as needed for Rice 2.x compatibility.
 * ====
 * 
 * Service locator for KSR
 * 
 * @author rSmart Development Team
 */
public class KSRServiceLocator {
    private static final Logger LOG = LogManager.getLogger(KSRServiceLocator.class);

    public static Object getService(String serviceName) {
        return getBean(serviceName);
    }

    public static Object getBean(String serviceName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching service " + serviceName);
        }

        return GlobalResourceLoader.getResourceLoader().getService(new QName(serviceName));
    }

    public static final String SECURITY_REQUEST_DOCUMENT_SERVICE = "securityRequestDocumentService";
    public static SecurityRequestDocumentService getSecurityRequestDocumentService() {
        return (SecurityRequestDocumentService) getService(SECURITY_REQUEST_DOCUMENT_SERVICE);
    }
    
    public static final String SECURITY_REQUEST_POST_PROCESSING_SERVICE = "securityRequestPostProcessingService";
    public static SecurityRequestPostProcessingService getSecurityRequestPostProcessingService() {
        return (SecurityRequestPostProcessingService) getService(SECURITY_REQUEST_POST_PROCESSING_SERVICE);
    }

    public static final String SECURITY_REQUEST_DATASOURCE_NAME = "ksrDataSource";
    public static DataSource getDataSource() {
        return (DataSource) getService(SECURITY_REQUEST_DATASOURCE_NAME);
    }

}
