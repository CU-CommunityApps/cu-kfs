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

import java.util.List;
import java.util.Map;

import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.rice.krad.bo.BusinessObject;

import com.rsmart.kuali.kfs.module.ld.businessobject.inquiry.BaseFundsInquirableImpl;

public class BaseFundsLookupableHelperServiceImpl extends org.kuali.kfs.module.ld.businessobject.lookup.BaseFundsLookupableHelperServiceImpl {

    @Override
    public HtmlData getInquiryUrl(BusinessObject bo, String propertyName) {
        return (new BaseFundsInquirableImpl()).getInquiryUrl(bo, propertyName);
    }

}
