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
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetail;

/**
 * 
 */
public class MasterCardTransactionDetailFieldUtil extends BusinessObjectStringParserFieldUtils
{

	/**
	 * @see org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils#getBusinessObjectClass()
	 */
	@Override
	public Class<MasterCardTransactionDetail> getBusinessObjectClass()
	{
		return MasterCardTransactionDetail.class;
	}

	/**
	 * Returns property names of the MasterCardTransactionDetail class in parsing order (the order
	 * they are in the input flat file) for the addendum type 0 records.
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
				CuFPPropertyConstants.CREDIT_CARD_NUMBER,
				CuFPPropertyConstants.FILLER3,
				CuFPPropertyConstants.TRANS_REF_NUMBER,
				CuFPPropertyConstants.FILLER4, 
				CuFPPropertyConstants.DEBIT_CREDIT_IND,
				CuFPPropertyConstants.TRANSACTION_AMOUNT,
				CuFPPropertyConstants.POSTING_DATE,
				CuFPPropertyConstants.TRANSACTION_DATE,
				CuFPPropertyConstants.FILLER5, // unknown date
				CuFPPropertyConstants.MERCHANT_NAME,
				CuFPPropertyConstants.MERCHANT_CITY,
				CuFPPropertyConstants.MERCHANT_STATE,
				CuFPPropertyConstants.MERCHANT_COUNTRY,
				CuFPPropertyConstants.CATEGORY_CODE,
				CuFPPropertyConstants.ORIG_CURRENCY_AMOUNT,
				CuFPPropertyConstants.ORIG_CURRENCY_CODE,
				CuFPPropertyConstants.FILLER6,
				CuFPPropertyConstants.CUSTOMER_CODE,
				CuFPPropertyConstants.SALES_TAX_AMOUNT,
				CuFPPropertyConstants.FILLER7
			};
	}

}
