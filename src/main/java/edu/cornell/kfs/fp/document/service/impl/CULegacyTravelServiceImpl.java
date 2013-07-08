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
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.*;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
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

    public static final class DFA_TRAVEL_WS_METHODS {
    	public static final String GET_TRIP_ID = "getTripID";
    	public static final String UPDATE_TRIP = "updateTrip";
    }
    
    public static final String KFS_DOC_VOIDED = "VOID";
    public static final String KFS_DOC_APPROVED = "APPROVE";
    public static final String KFS_DOC_COMPLETED = "COMPLETE";
    
    private String travelUrl;
    private String updateTripWsdl;
    private String updateTripEndpoint;
    private String updateTripUser;
    private String updateTripPassword;
    
	@Cached
	public boolean isLegacyTravelGeneratedKfsDocument(String docID) {
        try {
        	String tripID = getLegacyTripID(docID);
        	
        	if(tripID == null) {
        		LOG.error("KFS could not determine if the the associated edoc: "+docID+" is related to a trip in the DFA Travel system.  The web service call failed to connect.");
        	}
        	
			return StringUtils.isNotBlank(tripID);
        } catch (Exception ex) {
        	LOG.error("Exception occurred while trying to identify KFS doc as travel KFS doc.", ex);
      	  	ex.printStackTrace();
      	  	return false;
        }
	}
	
	/**
	 * @param docID the KFS e-doc id that will be used to try and find an associated trip in the DFA Travel application
	 * @return If a corresponding trip can be found in the DFA travel application, the Trip ID will be returned, if no corresponding trip can be found, an empty string is returned.
	 * 
	 * NOTE : If the call fails to successfully retrieve a value or a blank string from the DFA Travel application, a null value is returned to indicate the call failed.
	 */
    @Cached
	public String getLegacyTripID(String docID) {
        Client client = null;
        // Need to grab copy of current class loader because call to web services wrecks class loader, so we want to restore it following call.
        ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
        try {
			URL wsdlUrl = new URL(updateTripWsdl);
			
			JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			client = dcf.createClient(wsdlUrl);
			
			configureWebServiceClient(client);
			  
			Object[] results1 = client.invoke(DFA_TRAVEL_WS_METHODS.GET_TRIP_ID, docID);
			String tripID = (String)results1[0];
			return tripID;
        } catch (SoapFault sf) {
        	LOG.error(sf.getMessage());
        	sf.printStackTrace();
        	return null; // Returning null is used as an indicator that the call might have failed
        } catch (Exception ex) {
        	LOG.error("Exception occurred while trying to retrieve Trip ID.", ex);
      	  	ex.printStackTrace();
      	  	return null; // Returning null is used as an indicator that the call might have failed
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
			  
			configureWebServiceClient(client);
			  
			// Method signature on Travel side:  updateTrip(String status, String updaterNetID, String dvID, String voidReason) 
			Object[] results1 = client.invoke(DFA_TRAVEL_WS_METHODS.UPDATE_TRIP, KFS_DOC_VOIDED, GlobalVariables.getUserSession().getPrincipalName(), docID, disapproveReason);
			Boolean tripReopened = (Boolean)results1[0];
			return tripReopened;
        } catch (SoapFault sf) {
        	LOG.error(sf.getMessage());
        	sf.printStackTrace();
        	return false;
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

    /**
	 * @return the updateTripUser
	 */
	public String getUpdateTripUser() {
		return updateTripUser;
	}

	/**
	 * @param updateTripUser the updateTripUser to set
	 */
	public void setUpdateTripUser(String updateTripUser) {
		this.updateTripUser = updateTripUser;
	}

    /**
	 * @return the updateTripPassword
	 */
	public String getUpdateTripPassword() {
		return updateTripPassword;
	}

	/**
	 * @param updateTripPassword the updateTripPassword to set
	 */
	public void setUpdateTripPassword(String updateTripPassword) {
		this.updateTripPassword = updateTripPassword;
	}

    /**
	 * @return the updateTripEndpoint
	 */
	public String getUpdateTripEndpoint() {
		return updateTripEndpoint;
	}

	/**
	 * @param updateTripEndpoint the updateTripEndpoint to set
	 */
	public void setUpdateTripEndpoint(String updateTripEndpoint) {
		this.updateTripEndpoint = updateTripEndpoint;
	}

    /**
	 * @return the travelUrl
	 */
	public String getTravelUrl() {
		return travelUrl;
	}

	/**
	 * @param travelUrl the travelUrl to set
	 */
	public void setTravelUrl(String travelUrl) {
		this.travelUrl = travelUrl;
	}

	
	/**
	 * @param client
	 */
	private void configureWebServiceClient(Client client) {
		Endpoint endpoint = client.getConduitSelector().getEndpoint();
	    endpoint.getEndpointInfo().setAddress(updateTripEndpoint);
	    client.getConduitSelector().setEndpoint(endpoint);

	    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
	    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
	    httpClientPolicy.setConnectionTimeout(36000);
	    httpClientPolicy.setAllowChunking(false);
	    httpConduit.setClient(httpClientPolicy);

	    httpConduit.setAuthSupplier(new KfsWebServiceAuthSupplier());
	}

	/**
	 * Internal class that implements an authorization mechanism to support the required username and password being passed to the 
	 * associated endpoint for the KFS Web Services.
	 * 
	 * @author Dennis Friends
	 *
	 */
	public class KfsWebServiceAuthSupplier extends HttpAuthSupplier {
		public KfsWebServiceAuthSupplier() {
		    super();
		}
	
		public boolean requiresRequestCaching() {
		    return false;
		}
		    
		private String createUserPass(String userName, String passwd) {
		    String userAndPass = userName + ":" + passwd;
		    return "Basic " + Base64Utility.encode(userAndPass.getBytes());
		}

		public String getPreemptiveAuthorization(HTTPConduit conduit, URL currentURL, Message message) {
			return createUserPass(updateTripUser, updateTripPassword);
		}
		
		public String getAuthorizationForRealm(HTTPConduit conduit, URL currentURL, Message message, String reqestedRealm, String fullHeader) {
			return createUserPass(updateTripUser, updateTripPassword);
		}
	}

	
}
