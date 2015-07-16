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
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetailAddendum1;


/**
 * 
 */
public class MasterCardTransactionDetailAddendum1FieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetailAddendum1> getBusinessObjectClass()
	{
		return MasterCardTransactionDetailAddendum1.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 1 (Purchasing Card) records.
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
                CuFPPropertyConstants.PRODUCT_CODE,
                CuFPPropertyConstants.ITEM_DESCRIPTION,
                CuFPPropertyConstants.ITEM_QUANTITY,
                CuFPPropertyConstants.ITEM_UOM,
                CuFPPropertyConstants.EXT_ITEM_AMOUNT,
                CuFPPropertyConstants.DEBIT_CREDIT_IND,
                CuFPPropertyConstants.NET_GROSS_IND,
                CuFPPropertyConstants.TAX_RATE_APPLIED,
                CuFPPropertyConstants.TAX_TYPE_APPLIED,
                CuFPPropertyConstants.TAX_AMOUNT,
                CuFPPropertyConstants.DISCOUNT_IND,
                CuFPPropertyConstants.DISCOUNT_AMOUNT,
                CuFPPropertyConstants.FILLER3
			};
	}

}
