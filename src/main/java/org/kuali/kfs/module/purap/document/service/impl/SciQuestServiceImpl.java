/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.document.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.module.purap.businessobject.ReceivingAddress;
import org.kuali.kfs.module.purap.businessobject.SciQuestPunchoutData;
import org.kuali.kfs.module.purap.dataaccess.SciQuestDataAccess;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.service.SciQuestService;
import org.kuali.kfs.module.purap.util.cxml.PunchoutDataParser;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.w3c.dom.Document;

/**
 * SciQuestServiceImpl
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public class SciQuestServiceImpl implements SciQuestService {
	protected final Logger LOG = Logger.getLogger(getClass());
	
    private SciQuestDataAccess sciquestDataAccess;
    private BusinessObjectService businessObjectService;
    
    public SciQuestPunchoutData createPunchoutDataForReq(RequisitionDocument req, 
    											 		 String poResponseXml) {
    	PunchoutDataParser pdp = new PunchoutDataParser();
    	pdp.parse(poResponseXml);
    	return processPunchoutData(pdp, req);
    }
    
    public SciQuestPunchoutData createPunchoutDataForReq(RequisitionDocument req, 
	 		 Document poResponseDoc) {
		PunchoutDataParser pdp = new PunchoutDataParser();
		pdp.parse(poResponseDoc);
    	return processPunchoutData(pdp, req);		
	}
    
    private SciQuestPunchoutData processPunchoutData(PunchoutDataParser pdp,
    									    		 RequisitionDocument req) {
		SciQuestPunchoutData punchoutData = pdp.getPunchoutData();
		
		Integer reqID = req.getPurapDocumentIdentifier();
		punchoutData.setRequisitionId(reqID);
		
		getBusinessObjectService().save(punchoutData);
		
		return punchoutData;
    }
    
    public void fillReqWithPunchoutData(RequisitionDocument req,
    									SciQuestPunchoutData punchoutData) {
		BusinessObjectService bos = getBusinessObjectService();
		
    	// Copy the Requisition Name to the the Document Description
    	String reqName = punchoutData.getRequisitionName();
    	req.getDocumentHeader().setDocumentDescription(reqName);

    	// Copy the Receiving Address Information if there is a ShipTo Code
    	String addressCode = punchoutData.getShipToAddressCode();
    	if ( StringUtils.isNotEmpty(addressCode) ) {
            Map<String, Object> pkMap = new HashMap<String, Object>();
            pkMap.put("receivingAddressIdentifier", addressCode);
    		ReceivingAddress addr = (ReceivingAddress)bos.findByPrimaryKey(
    				ReceivingAddress.class, pkMap);

    		if ( addr != null ) {
        		LOG.debug("Setting Receiving Address Information");    			
	    		req.setReceivingCityName(addr.getReceivingCityName());
	    		req.setReceivingCountryCode(addr.getReceivingCountryCode());
	    		req.setReceivingLine1Address(addr.getReceivingLine1Address());
	    		req.setReceivingLine2Address(addr.getReceivingLine2Address());
	    		req.setReceivingName(addr.getReceivingName());
	    		req.setReceivingPostalCode(addr.getReceivingPostalCode());
	    		req.setReceivingStateCode(addr.getReceivingStateCode());
    		}
    		else {
        		LOG.error("Could not find Receiving Address ("+addressCode+")");
    		}
    	}
    	
    	bos.save(req);
    }
    
    public void setSciquestDataAccess(SciQuestDataAccess sciquestDataAccess) {
        this.sciquestDataAccess = sciquestDataAccess;
    }
    
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

	public SciQuestDataAccess getSciquestDataAccess() {
		return sciquestDataAccess;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}
}
