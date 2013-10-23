/*
 * Copyright 2009 The Kuali Foundation
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
package edu.cornell.kfs.fp.document.authorization;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.kuali.kfs.fp.document.DisbursementVoucherConstants;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.fp.document.authorization.DisbursementVoucherDocumentPresentationController;
import org.kuali.kfs.sys.KfsAuthorizationConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;

@SuppressWarnings("serial")
public class CuDisbursementVoucherDocumentPresentationController extends DisbursementVoucherDocumentPresentationController {
    
    
    /**
     * If at a proper route node, adds the ability to edit the travel information on the disbursement voucher
     * @param document the disbursement voucher document authorization is being sought on
     * @param editModes the edit modes so far, which can be added to
     */
   
    protected void addTravelEntryMode(Document document, Set<String> editModes) {
        final WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();

        final Set<String> currentRouteLevels = workflowDocument.getCurrentNodeNames();
        if (CollectionUtils.isNotEmpty(currentRouteLevels)) {
            if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.ACCOUNT)) {  
                //FO?
                editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
            } else if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.TAX)) { 
                //tax manager? Then only allow this if we're going to route to travel node anyway
                if (((DisbursementVoucherDocument) document).isTravelReviewRequired()) {
                    editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
                }
            } else if (currentRouteLevels.contains(DisbursementVoucherConstants.RouteLevelNames.PAYMENT_METHOD) 
                    && ((DisbursementVoucherDocument) document).getDisbVchrPaymentMethodCode().equals(DisbursementVoucherConstants.PAYMENT_METHOD_DRAFT)) {
                editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY);
            } else {
                editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY); 
                // we're not FO? Then always add it, as KIM permissions will take it out if we shouldn't have it
            }
        } else {
            editModes.add(KfsAuthorizationConstants.DisbursementVoucherEditMode.TRAVEL_ENTRY); 
            // we're not FO? Then always add it, as KIM permissions will take it out if we shouldn't have it
        }
    }
    
   
}
