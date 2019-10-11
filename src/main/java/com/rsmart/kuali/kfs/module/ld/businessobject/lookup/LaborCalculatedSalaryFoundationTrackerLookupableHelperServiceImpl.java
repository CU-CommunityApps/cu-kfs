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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.integration.ld.businessobject.inquiry.AbstractPositionDataDetailsInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.LaborCalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.ld.businessobject.inquiry.AbstractLaborInquirableImpl;
import org.kuali.kfs.module.ld.businessobject.inquiry.PositionDataDetailsInquirableImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.krad.bo.BusinessObject;

import com.rsmart.kuali.kfs.module.ld.businessobject.inquiry.LaborCalculatedSalaryFoundationTrackerInquirableImpl;

public class LaborCalculatedSalaryFoundationTrackerLookupableHelperServiceImpl extends org.kuali.kfs.module.ld.businessobject.lookup.LaborCalculatedSalaryFoundationTrackerLookupableHelperServiceImpl {

    /**
     * @see org.kuali.kfs.kns.lookup.AbstractLookupableHelperServiceImpl#getInquiryUrl(org.kuali.kfs.kns.bo.BusinessObject,
     *      java.lang.String)
     */
    @Override
    public HtmlData getInquiryUrl(BusinessObject bo, String propertyName) {
        if (KFSPropertyConstants.POSITION_NUMBER.equals(propertyName)) {
            LaborCalculatedSalaryFoundationTracker CSFTracker = (LaborCalculatedSalaryFoundationTracker) bo;
            AbstractPositionDataDetailsInquirableImpl positionDataDetailsInquirable =
                    new PositionDataDetailsInquirableImpl();
            
            //KUALI-1321  Gets the Person object via the employee ID and populates the name property.
            Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(CSFTracker.getEmplid());
            if (person != null){
                //CSFTracker.setName(person.getLastName() + ", " + person.getFirstName());
            }
            
            Map<String, String> fieldValues = new HashMap<>();
            fieldValues.put(propertyName, CSFTracker.getPositionNumber());

            BusinessObject positionData = positionDataDetailsInquirable.getBusinessObject(fieldValues);

            return positionData == null ? new AnchorHtmlData(KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING) :
                positionDataDetailsInquirable.getInquiryUrl(positionData, propertyName);
        }
        return (new LaborCalculatedSalaryFoundationTrackerInquirableImpl()).getInquiryUrl(bo, propertyName);
    }

}
