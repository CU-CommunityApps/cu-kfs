/*
 * Copyright 2006 The Kuali Foundation
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
package edu.cornell.kfs.fp.document.service.impl;

import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.core.util.ClassLoaderUtils;
import org.kuali.rice.core.util.ContextClassLoaderBinder;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.spring.Cached;

/**
 * This is the default implementation of the CULegacyTravelService interface.
 * 
 */

@NonTransactional
public class CULegacyTravelServiceImpl implements edu.cornell.kfs.fp.document.service.CULegacyTravelService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CULegacyTravelServiceImpl.class);

//    public static final String UPDATE_TRIP_WSDL = "https://servicesdev.dfa.cornell.edu/services/UpdateTripWebService?wsdl";
    
    public static final class DFA_TRAVEL_WS_METHODS {
    	public static final String GET_TRIP_ID = "getTripID";
    	public static final String UPDATE_TRIP = "updateTrip";
    }
    
    public static final String KFS_DOC_VOIDED = "VOID";
    public static final String KFS_DOC_APPROVED = "APPROVE";
    public static final String KFS_DOC_COMPLETED = "COMPLETE";
    
    private String updateTripWsdl;
    
	@Cached
	public boolean isLegacyTravelGeneratedKfsDocument(String docID) {
        Client client = null;
        // Need to grab copy of current class loader because call to web services wrecks class loader, so we want to restore it following call.
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        try {
			URL wsdlUrl = new URL(updateTripWsdl);
			
			JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			client = dcf.createClient(wsdlUrl);
			  
			Object[] results1 = client.invoke(DFA_TRAVEL_WS_METHODS.GET_TRIP_ID, docID);
			String tripID = (String)results1[0];
			return StringUtils.isNotBlank(tripID);
        } catch (Exception ex) {
        	LOG.error("Exception occurred while trying to identify KFS doc as travel KFS doc.", ex);
      	  	ex.printStackTrace();
      	  	return false;
        } finally {
        	if(client != null) {
        		client.destroy();
        	}
        	// Restore class loader that was grabbed before call to web service
        	ContextClassLoaderBinder.bind(classLoader);
        }
	}
	
    @Cached
	public String getLegacyTripID(String docID) {
        Client client = null;
        // Need to grab copy of current class loader because call to web services wrecks class loader, so we want to restore it following call.
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        try {
			URL wsdlUrl = new URL(updateTripWsdl);
			
			JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			client = dcf.createClient(wsdlUrl);
			  
			Object[] results1 = client.invoke(DFA_TRAVEL_WS_METHODS.GET_TRIP_ID, docID);
			String tripID = (String)results1[0];
			return tripID;
        } catch (Exception ex) {
        	LOG.error("Exception occurred while trying to retrieve Trip ID.", ex);
      	  	ex.printStackTrace();
      	  	return null;
        } finally {
        	if(client != null) {
        		client.destroy();
        	}
        	// Restore class loader that was grabbed before call to web service
        	ContextClassLoaderBinder.bind(classLoader);
        }
	}
	
    /**
	 * @param dvID
	 * @return
     */
	public boolean reopenLegacyTrip(String docID) {
		return reopenLegacyTrip(docID, "");
	}
	
	/**
	 * 
	 * @param dvID
	 * @param disapproveReason
	 * @return
	 */
	public boolean reopenLegacyTrip(String docID, String disapproveReason) {
        Client client = null;
        // Need to grab copy of current class loader because call to web services wrecks class loader, so we want to restore it following call.
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        try {
			URL wsdlUrl = new URL(updateTripWsdl);
			
			JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			client = dcf.createClient(wsdlUrl);
			  
			// Method signature on Travel side:  updateTrip(String status, String updaterNetID, String dvID, String voidReason) 
			Object[] results1 = client.invoke(DFA_TRAVEL_WS_METHODS.UPDATE_TRIP, KFS_DOC_VOIDED, GlobalVariables.getUserSession().getPrincipalName(), docID, disapproveReason);
			Boolean tripReopened = (Boolean)results1[0];
			return tripReopened;
        } catch (Exception ex) {
        	LOG.info("Exception occurred while trying to cancel a trip.");
      	  	ex.printStackTrace();
      	  	return false;
        } finally {
        	if(client != null) {
        		client.destroy();
        	}
        	// Restore class loader that was grabbed before call to web service
        	ContextClassLoaderBinder.bind(classLoader);
        }
	}
	
	public boolean updateLegacyTrip(String docID) {
		return true;
	}
	
    /**
	 * @return the updateTripWsdl
	 */
	public String getUpdateTripWsdl() {
		return updateTripWsdl;
	}

	/**
	 * @param updateTripWsdl the updateTripWsdl to set
	 */
	public void setUpdateTripWsdl(String updateTripWsdl) {
		this.updateTripWsdl = updateTripWsdl;
	}

}
