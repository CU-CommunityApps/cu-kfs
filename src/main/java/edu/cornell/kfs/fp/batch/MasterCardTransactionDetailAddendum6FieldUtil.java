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
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum6;

/**
 * 
 */
public class MasterCardTransactionDetailAddendum6FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum6> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum6.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 6 (Fuel) records.
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
                CuFPPropertyConstants.OIL_COMPANY_BRAND,
                CuFPPropertyConstants.MERCHANT_STREET_ADDRESS,
                CuFPPropertyConstants.MERCHANT_POSTAL_CODE,
                CuFPPropertyConstants.TIME_OF_PURCHASE,
                CuFPPropertyConstants.MOTOR_FUEL_SERVICE_TYPE,
                CuFPPropertyConstants.MOTOR_FUEL_PRODUCT_CODE,
                CuFPPropertyConstants.MOTOR_FUEL_UNIT_PRICE,
                CuFPPropertyConstants.MOTOR_FUEL_UOM,
                CuFPPropertyConstants.MOTOR_FUEL_QUANTITY,
                CuFPPropertyConstants.MOTOR_FUEL_SALE_AMOUNT,
                CuFPPropertyConstants.ODOMETER_READING,
                CuFPPropertyConstants.VEHICLE_NUMBER,
                CuFPPropertyConstants.DRIVER_NUMBER,
                CuFPPropertyConstants.MAGNETIC_STRIPE_PRODUCT_TYPE_CODE,
                CuFPPropertyConstants.COUPON_DISCOUNT_AMOUNT,
                CuFPPropertyConstants.TAX_EXEMPT_AMOUNT,
                CuFPPropertyConstants.TAX_AMOUNT1,
                CuFPPropertyConstants.TAX_AMOUNT2,
                CuFPPropertyConstants.FILLER3
			};
	}

}
