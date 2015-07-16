/*
 * Copyright 2012 The Kuali Foundation.
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
package edu.cornell.kfs.fp.batch;

import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum2;

/**
 * 
 */
public class MasterCardTransactionDetailAddendum2FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum2> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum2.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 2 (Passenger Transport) records.
	 * 
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getOrderedProperties()
	 */
	@Override
	public String[] getOrderedProperties()
	{
		return new String[]
			{
                CuFPPropertyConstants.RECORD_TYPE,
                CuFPPropertyConstants.FILLER1,
                CuFPPropertyConstants.ADDENDUM_TYPE,
                CuFPPropertyConstants.FILLER2,
                CuFPPropertyConstants.PASSENGER_NAME,
                CuFPPropertyConstants.DEPARTURE_DATE,
                CuFPPropertyConstants.AIRPORT_CODE,
                CuFPPropertyConstants.TRAVEL_AGENCY_CODE,
                CuFPPropertyConstants.TRAVEL_AGENCY_NAME,
                CuFPPropertyConstants.TICKET_NUMBER,
                CuFPPropertyConstants.CUSTOMER_CODE,
                CuFPPropertyConstants.ISSUE_DATE,
                CuFPPropertyConstants.ISSUING_CARRIER,
                CuFPPropertyConstants.TOTAL_FARE,
                CuFPPropertyConstants.TOTAL_FEES,
                CuFPPropertyConstants.TOTAL_TAXES,
                CuFPPropertyConstants.FILLER3
			};
	}

}
