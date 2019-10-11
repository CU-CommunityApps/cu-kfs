/*
 * Copyright 2011 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.module.ld.businessobject.inquiry;

import com.rsmart.kuali.kfs.module.ld.LdConstants;

public class LedgerBalanceInquirableImpl extends org.kuali.kfs.module.ld.businessobject.inquiry.LedgerBalanceInquirableImpl {
    
    protected String getBaseUrl() {
        return LdConstants.LD_MODIFIED_INQUIRY_ACTION;
    }
}
