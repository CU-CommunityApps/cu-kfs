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
package org.kuali.kfs.module.purap.document.service;

import org.kuali.kfs.module.purap.businessobject.SciQuestPunchoutData;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.w3c.dom.Document;

/**
 * SciQuestService
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public interface SciQuestService {
	/**
	 * Creates Punchout Data records for the specified Requisition Document.
	 * Uses the XML in the poResponseXML parameter to retrieve the fields
	 * that will be populated.
	 * 
	 * @param req The Requisition Document
	 * @param poResponseXml The B2B Purchase Order Response
	 * @return new created SciQuestPunchoutData instance
	 */
    SciQuestPunchoutData createPunchoutDataForReq(RequisitionDocument req, 
    											  String poResponseXml);

	/**
	 * Creates Punchout Data records for the specified Requisition Document.
	 * Uses the DOm Document in the poResponseDoc parameter to retrieve the 
	 * fields that will be populated.
	 * 
	 * @param req The Requisition Document
	 * @param poResponseDoc The B2B Purchase Order Response Document
	 * @return new created SciQuestPunchoutData instance
	 */
    SciQuestPunchoutData createPunchoutDataForReq(RequisitionDocument req, 
    											  Document poResponseDoc);
    
    /**
     * Backfills the Requisition Document with information in the Punchout Data
     * instance, including Document Name and Ship To Information.
     * 
     * @param req The Requisition Document
     * @param punchoutData The SciQuestPunchoutData instance
     */
    void fillReqWithPunchoutData(RequisitionDocument req,
								 SciQuestPunchoutData punchoutData);    
}
