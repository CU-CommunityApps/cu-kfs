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
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum4;

/**
 * 
 */
public class MasterCardTransactionDetailAddendum4FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum4> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum4.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 4 (Rental Car) records.
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
                CuFPPropertyConstants.RENTAL_AGREEMENT_NUMBER,
                CuFPPropertyConstants.RENTER_NAME,
                CuFPPropertyConstants.RENTAL_RETURN_CITY,
                CuFPPropertyConstants.RENTAL_RETURN_STATE,
                CuFPPropertyConstants.RENTAL_RETURN_COUNTRY,
                CuFPPropertyConstants.RENTAL_RETURN_DATE,
                CuFPPropertyConstants.RETURN_LOCATION_ID,
                CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER,
                CuFPPropertyConstants.RENTAL_CLASS,
                CuFPPropertyConstants.DAILY_RENTAL_RATE,
                CuFPPropertyConstants.RATE_PER_MILE,
                CuFPPropertyConstants.TOTAL_MILES,
                CuFPPropertyConstants.MAX_FREE_MILES,
                CuFPPropertyConstants.INSURANCE_IND,
                CuFPPropertyConstants.INSURANCE_CHARGES,
                CuFPPropertyConstants.ADJUSTED_AMOUNT_IND,
                CuFPPropertyConstants.ADJUSTED_AMOUNT,
                CuFPPropertyConstants.PROGRAM_CODE,
                CuFPPropertyConstants.CHECKOUT_DATE,
                CuFPPropertyConstants.FILLER3
			};
	}

}
