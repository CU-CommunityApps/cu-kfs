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
package com.rsmart.kuali.kfs.module.ld.businessobject.lookup;

import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.rice.krad.bo.BusinessObject;

import com.rsmart.kuali.kfs.module.ld.businessobject.inquiry.PositionDataInquirableImpl;

public class PositionDataLookupableHelperServiceImpl extends org.kuali.kfs.module.ld.businessobject.lookup.PositionDataLookupableHelperServiceImpl {

    /**
     * @see org.kuali.kfs.kns.lookup.Lookupable#getInquiryUrl(org.kuali.kfs.kns.bo.BusinessObject, java.lang.String)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String propertyName) {
        if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            return (new PositionDataDetailsInquirableImpl()).getInquiryUrl(businessObject, propertyName);
        }
        return (new PositionDataInquirableImpl()).getInquiryUrl(businessObject, propertyName);
    }
}
