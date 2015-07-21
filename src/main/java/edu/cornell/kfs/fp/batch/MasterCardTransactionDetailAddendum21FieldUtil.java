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
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum21;


/**
 * 
 */
public class MasterCardTransactionDetailAddendum21FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum21> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum21.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 21 (Passenger Transport Leg) records.
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
                CuFPPropertyConstants.TRIP_LEG_NUMBER,
                CuFPPropertyConstants.CARRIER_CODE,
                CuFPPropertyConstants.SERVICE_CLASS,
                CuFPPropertyConstants.STOP_OVER_CODE,
                CuFPPropertyConstants.CITY_OF_ORIGIN,
                CuFPPropertyConstants.CONJUNCTION_TICKET,
                CuFPPropertyConstants.TRAVEL_DATE,
                CuFPPropertyConstants.EXCHANGE_TICKET,
                CuFPPropertyConstants.COUPON_NUMBER,
                CuFPPropertyConstants.CITY_OF_DESTINATION,
                CuFPPropertyConstants.FARE_BASE_CODE,
                CuFPPropertyConstants.FLIGHT_NUMBER,
                CuFPPropertyConstants.DEPARTURE_TIME,
                CuFPPropertyConstants.DEPARTURE_TIME_SEGMENT,
                CuFPPropertyConstants.ARRIVAL_TIME,
                CuFPPropertyConstants.ARRIVAL_TIME_SEGMENT,
                CuFPPropertyConstants.FARE,
                CuFPPropertyConstants.FEE,
                CuFPPropertyConstants.TAXES,
                CuFPPropertyConstants.ENDORSEMENTS,
                CuFPPropertyConstants.FILLER3
			};
	}

}
