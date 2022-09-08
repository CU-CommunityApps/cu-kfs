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

import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.fp.document.interfaces.CULegacyTravelIntegrationInterface;

/**
 * This is the default implementation of the CULegacyTravelService interface.
 * 
 */
public class CULegacyTravelServiceImpl implements edu.cornell.kfs.fp.document.service.CULegacyTravelService {

    public static final class DFA_TRAVEL_WS_METHODS {
    	public static final String GET_TRIP_ID = "getTripID";
    	public static final String UPDATE_TRIP = "updateTrip";
    }
    
    public static final class TRIP_ASSOCIATIONS {
        public static final String IS_TRIP_DOC = "1";
        public static final String IS_NOT_TRIP_DOC = "0";
    }
    
    public static final String KFS_DOC_VOIDED = "VOID";
    public static final String KFS_DOC_APPROVED = "APPROVE";
    public static final String KFS_DOC_COMPLETED = "COMPLETE";
    
    private String travelUrl;
    private String updateTripWsdl;
    private String updateTripEndpoint;

	
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
     * Determines if the passed in CULegacyTravelIntegrationInterface (Disbursement Voucher Document, or Distribution of Incum Document
     * is associated with a trip.  Either way, this function sets internval of document for the trip association status code and the trip ID.
     */
    public boolean isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(CULegacyTravelIntegrationInterface cuLegacyTravelIntegrationInterace) {
        return false;
    }
    
    /**
     * Returns the TRIP ID of the passed in CULegacyTravelIntegrationInterface.  If the CULegacyTravelIntegrationInterface is not
     * associated with a trip, an empty string is returned.
     */
    public String getLegacyTripIDFromCULegacyTravelIntegrationInterface(CULegacyTravelIntegrationInterface cuLegacyTravelIntegrationInterace) {
        if (isCULegacyTravelIntegrationInterfaceAssociatedWithTrip(cuLegacyTravelIntegrationInterace)) {
            return cuLegacyTravelIntegrationInterace.getTripId();
        } else {
            return StringUtils.EMPTY;
        }
    }   
	
}
