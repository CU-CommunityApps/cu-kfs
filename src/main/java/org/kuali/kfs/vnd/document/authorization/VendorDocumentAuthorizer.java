/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.vnd.document.authorization;

import java.util.Set;

import org.kuali.kfs.sys.document.authorization.FinancialSystemMaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.util.KNSConstants;

/**
 * Authorizer class for Vendor maintenance document
 */
public class VendorDocumentAuthorizer extends FinancialSystemMaintenanceDocumentAuthorizerBase {
    
    @Override
    public Set<String> getSecurePotentiallyReadOnlySectionIds() {
        Set<String> readOnlySectionIds = super.getSecurePotentiallyReadOnlySectionIds();
        
        // vendor contracts and commodity codes are the potentially readonly sections
        readOnlySectionIds.add(VendorPropertyConstants.VENDOR_CONTRACT);
        readOnlySectionIds.add(VendorPropertyConstants.VENDOR_COMMODITIES_CODE);

        return readOnlySectionIds;
    }

	@Override
	public Set<String> getDocumentActions(Document document, Person user,
			Set<String> documentActions) {
		Set<String> documentActions1 = super.getDocumentActions(document, user, documentActions);
		// KFSPTS-2055
		if (documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_APPROVE)
				&& !documentActions1.contains(KNSConstants.KUALI_ACTION_CAN_SAVE)) {
			documentActions1.add(KNSConstants.KUALI_ACTION_CAN_SAVE);
		}
		return documentActions1;
	}    
    
}

