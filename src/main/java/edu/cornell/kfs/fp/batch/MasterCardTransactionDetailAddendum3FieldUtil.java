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
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum3;


/**
 * 
 */
public class MasterCardTransactionDetailAddendum3FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum3> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum3.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 3 (Lodging) records.
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
                CuFPPropertyConstants.ARRIVAL_DATE,
                CuFPPropertyConstants.DEPARTURE_DATE,
                CuFPPropertyConstants.FOLIO_NUMBER,
                CuFPPropertyConstants.PROPERTY_PHONE_NUMBER,
                CuFPPropertyConstants.CUSTOMER_SERVICE_NUMBER,
                CuFPPropertyConstants.ROOM_RATE,
                CuFPPropertyConstants.ROOM_TAX,
                CuFPPropertyConstants.PROGRAM_CODE,
                CuFPPropertyConstants.TELEPHONE_CHARGES,
                CuFPPropertyConstants.ROOM_SERVICE,
                CuFPPropertyConstants.BAR_CHARGES,
                CuFPPropertyConstants.GIFT_SHOP_CHARGES,
                CuFPPropertyConstants.LAUNDRY_CHARGES,
                CuFPPropertyConstants.OTHER_SERVICES_CODE,
                CuFPPropertyConstants.OTHER_SERVICES_CHARGES,
                CuFPPropertyConstants.BILLING_ADJUSTMENT_IND,
                CuFPPropertyConstants.BILLING_ADJUSTMENT_AMOUNT,
                CuFPPropertyConstants.FILLER3
			};
	}

}
